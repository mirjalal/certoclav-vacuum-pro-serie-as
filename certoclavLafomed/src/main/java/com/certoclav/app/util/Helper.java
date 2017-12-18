package com.certoclav.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.listener.BroadcastListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.responsemodels.UserProtocolResponseModel;
import com.certoclav.app.responsemodels.UserProtocolsResponseModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by musaq on 7/11/2017.
 */

public class Helper {
    private final static String KEY_ADMIN_PASSWORD = "adminpassword";

    public static String getTimeStamp(String dateStr) {
        Calendar calendar = Calendar.getInstance();
        String today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(dateStr);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            Log.e("timeZone", tz.getDisplayName());
            format.setTimeZone(tz);
            String date1 = format.format(date);
            timestamp = date1.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

    public static Date getDate(String dateStr) {
        Calendar calendar = Calendar.getInstance();
        String today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void downloadProtocols(final Context context, final MyCallback myCallback) {

        class SyncPrtocolsAsync extends AsyncTask<UserProtocolsResponseModel, Integer, Void> {

            @Override
            protected Void doInBackground(UserProtocolsResponseModel... params) {
                DatabaseService databaseService = new DatabaseService(context);
                //     databaseService.deleteProtocolEntry(databaseService.getProtocols());
                //     databaseService.deleteSyncedProtocols();

                int current = 0;
                int max = params[0].getProtocols().size();
                for (Protocol protocol : params[0].getProtocols()) {
                    Protocol temp = new Protocol(protocol.getCloudId(),
                            1,
                            protocol.getStartTime(),
                            protocol.getEndTime(), //init the EndTime, in order to avoid nullpointer exceptions after power loss
                            protocol.getZyklusNumber(),
                            Autoclave.getInstance().getController(),
                            Autoclave.getInstance().getUser(),
                            protocol.getProgram(),
                            protocol.getErrorCode(), // power loss
                            true);

                    databaseService.insertProtocol(temp);
                    Calendar calendar = Calendar.getInstance();
                    Date startDate = temp.getStartTime();
                    if (protocol.getProtocolEntries() != null) {
                        for (ProtocolEntry protocolEntry : protocol.getProtocolEntries()) {
                            protocolEntry.setProtocol(temp);

                            calendar.setTime(startDate);
                            calendar.add(Calendar.SECOND, (int) (protocolEntry.getTs() * 60));
                            protocolEntry.setTimestamp(calendar.getTime());
                        }
                        databaseService.insertProtocolEntry(protocol.getProtocolEntries());
                    }
                    publishProgress(++current, max);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                myCallback.onProgress(values[0], values[1]);

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                myCallback.onSuccess(false, 1);
                super.onPostExecute(aVoid);
            }
        }
        Requests.getInstance().getUserProtocols(new MyCallback() {
            @Override
            public void onSuccess(Object response, int requestId) {
                UserProtocolsResponseModel userProtocolsResponseModel = (UserProtocolsResponseModel) response;
                new SyncPrtocolsAsync().execute(userProtocolsResponseModel);
                myCallback.onSuccess(true, 1);
            }

            @Override
            public void onError(ErrorModel error, int requestId) {
                myCallback.onError(error, requestId);
            }

            @Override
            public void onStart(int requestId) {
                myCallback.onStart(requestId);

            }

            @Override
            public void onProgress(int current, int max) {

            }
        }, 1);
    }

    public static void downloadProtocol(final Context context, Protocol protocol, final MyCallback myCallback) {

        class SyncPrtocolsAsync extends AsyncTask<UserProtocolResponseModel, Integer, Void> {

            @Override
            protected Void doInBackground(UserProtocolResponseModel... params) {
                Protocol protocol = params[0].getProtocol();
                DatabaseService databaseService = new DatabaseService(context);
                databaseService.deleteProtocol(protocol);


                Protocol temp = new Protocol(protocol.getCloudId(),
                        1,
                        protocol.getStartTime(),
                        protocol.getEndTime(), //init the EndTime, in order to avoid nullpointer exceptions after power loss
                        protocol.getZyklusNumber(),
                        Autoclave.getInstance().getController(),
                        Autoclave.getInstance().getUser(),
                        protocol.getProgram(),
                        protocol.getErrorCode(), // power loss
                        true);

                databaseService.insertProtocol(temp);
                Calendar calendar = Calendar.getInstance();
                Date startDate = temp.getStartTime();
                if (protocol.getProtocolEntries() != null) {
                    for (ProtocolEntry protocolEntry : protocol.getProtocolEntries()) {
                        protocolEntry.setProtocol(temp);
                        protocolEntry.setPressure(protocolEntry.getPressure() - 1);
                        calendar.setTime(startDate);
                        calendar.add(Calendar.SECOND, (int) (protocolEntry.getTs() * 60));
                        protocolEntry.setTimestamp(calendar.getTime());
                    }
                    databaseService.insertProtocolEntry(protocol.getProtocolEntries());
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                myCallback.onProgress(values[0], values[1]);

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                myCallback.onSuccess(true, 2);
                super.onPostExecute(aVoid);
            }
        }
        Requests.getInstance().getUserProtocol(new MyCallback() {
            @Override
            public void onSuccess(Object response, int requestId) {
                UserProtocolResponseModel userProtocolsResponseModel = (UserProtocolResponseModel) response;
                myCallback.onSuccess(userProtocolsResponseModel.getProtocol() != null, 1);
                if (userProtocolsResponseModel.getProtocol() != null)
                    new SyncPrtocolsAsync().execute(userProtocolsResponseModel);
            }

            @Override
            public void onError(ErrorModel error, int requestId) {
                myCallback.onError(error, requestId);
            }

            @Override
            public void onStart(int requestId) {
                myCallback.onStart(requestId);

            }

            @Override
            public void onProgress(int current, int max) {

            }
        }, protocol.getCloudId(), 1);
    }

    public static void printProtocols(final Context context, final Protocol protocol, final MyCallback myCallback) {

        new AsyncTask<Protocol, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                myCallback.onStart(-1);
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Protocol... params) {
                try {
                    ESCPos printUtils = new ESCPos();
                    printUtils.printProtocol(params[0], context);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean)
                    myCallback.onSuccess(null, -1);
                else
                    myCallback.onError(null, -1);
                super.onPostExecute(aBoolean);
            }
        }.execute(protocol);


    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String SHA1(String text) {
        MessageDigest md = null;
        byte[] textBytes = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            textBytes = text.getBytes("iso-8859-1");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public static boolean checkAdminPassword(Context context, String password) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return SHA1(password).equals(pref.getString(KEY_ADMIN_PASSWORD, SHA1(AppConstants.DEFAULT_ADMIN_PASSWORD)));
    }

    public static boolean updateAdminPassword(Context context, String newPassword) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.edit().putString(KEY_ADMIN_PASSWORD, SHA1(newPassword)).commit();
    }

    public static Map<String, String> getKeyValueFromStringArray(Context ctx) {
        String[] array = ctx.getResources().getStringArray(R.array.error_codes);
        Map<String, String> result = new HashMap<>();
        for (String str : array) {
            String[] splittedItem = str.split("\\|");
            int len = splittedItem.length;
            for (int i = 0; i < len - 1; i++)
                result.put(splittedItem[i], splittedItem[len - 1]);
        }
        return result;
    }


    public static void sendBroadcast(Context context, String messageStr, final BroadcastListener listener) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(context), 41234);
            socket.send(sendPacket);
            Log.e("Broadcast", "Broadcast packet sent to: " + getBroadcastAddress(context).getHostAddress());
            socket.close();
        } catch (IOException e) {
            Log.e("Broadcast", "IOException: " + e.getMessage());
        }

        try {
            //Keep a socket open to listen to all the UDP trafic that is destined for this port
            final DatagramSocket socket = new DatagramSocket(41234, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            Handler handler = new Handler(Looper.getMainLooper());
            Runnable callback;
            handler.postDelayed(callback = new Runnable() {
                @Override
                public void run() {
                    socket.disconnect();
                    socket.close();
                    listener.onTimeout();
                }
            }, 2000);


            Log.i("Broadcast", "Ready to receive broadcast packets!");
            //Receive a packet
            byte[] recvBuf = new byte[15000];
            DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(packet);

            //Packet received
            Log.i("Broadcast", "Packet received from: " + packet.getAddress().getHostAddress());
            String data = new String(packet.getData()).trim();
            Log.i("Broadcast", "Packet received; data: " + data);
            if (data.length() > 0) {
                handler.removeCallbacks(callback);
                JSONObject serverConfig = new JSONObject(data);
                serverConfig.put("url", "http://" + packet.getAddress().getHostAddress());
                listener.onReceived(serverConfig);
            }

            socket.close();
        } catch (IOException ex) {
            //  listener.onFailed();
            Log.i("Broadcast", "Oops" + ex.getMessage());
        } catch (JSONException e) {
            listener.onFailed();
            e.printStackTrace();
        }
    }

    static InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    public static boolean installEnthernet(Context context) {
        AssetManager assetManager = context.getAssets();
    /*    PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo("com.fsl.ethernet", PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }*/

        InputStream in = null;
        OutputStream out = null;
        String filename = Environment.getExternalStorageDirectory() + "/ethernet.apk";

        try {
            in = assetManager.open("ethernet.apk");
            out = new FileOutputStream(filename);

            byte[] buffer = new byte[1024];

            int read;
            while ((read = in.read(buffer)) != -1) {

                out.write(buffer, 0, read);

            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

            File file = new File(filename);
            if (file.exists()) {
                try {
                    String command;
                    command = "adb install -r " + filename;
                    Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                    proc.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
