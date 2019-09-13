package com.certoclav.app.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.listener.BroadcastListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.responsemodels.DeviceProgramsResponseModel;
import com.certoclav.app.responsemodels.UserProtocolResponseModel;
import com.certoclav.app.responsemodels.UserProtocolsResponseModel;
import com.certoclav.app.service.CloudSocketThread;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.app.settings.SettingsActivity;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.PostUtil;
import com.certoclav.library.certocloud.SocketService;
import com.certoclav.library.util.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import needle.Needle;
import needle.UiRelatedTask;

/**
 * Created by musaq on 7/11/2017.
 */

public class Helper {
    private final static String KEY_ADMIN_PASSWORD = "adminpassword";
    private static Helper instance;

    private Helper() {

    }

    public static Helper getInstance() {
        if (instance == null)
            instance = new Helper();
        return instance;
    }

    public String getTimeStamp(String dateStr) {
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

    public Date getDate(String dateStr) {
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

    public void downloadProtocols(final Context context, final MyCallback myCallback) {

        class SyncPrtocolsAsync extends AsyncTask<UserProtocolsResponseModel, Integer, Void> {

            @Override
            protected Void doInBackground(UserProtocolsResponseModel... params) {
                DatabaseService databaseService = DatabaseService.getInstance();
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

    public void downloadProtocol(final Context context, Protocol protocol, final MyCallback myCallback) {

        class SyncPrtocolsAsync extends AsyncTask<UserProtocolResponseModel, Integer, Void> {

            @Override
            protected Void doInBackground(UserProtocolResponseModel... params) {
                Protocol protocol = params[0].getProtocol();
                DatabaseService databaseService = DatabaseService.getInstance();
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
                        protocolEntry.setPressure((float) ((protocolEntry.getPressure() - 1.0) * 100.0));

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

    public void printProtocols(final Context context, final Protocol protocol, final MyCallback myCallback) {

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

    public String SHA1(String text) {
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

    public boolean checkAdminPassword(Context context, String password) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return SHA1(password).equals(pref.getString(KEY_ADMIN_PASSWORD, SHA1(AppConstants.DEFAULT_ADMIN_PASSWORD)));
    }

    public boolean updateAdminPassword(Context context, String newPassword) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.edit().putString(KEY_ADMIN_PASSWORD, SHA1(newPassword)).commit();
    }

    public Map<String, String> getKeyValueFromStringArray(Context ctx) {
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


    public void sendBroadcast(Context context, String messageStr, final BroadcastListener listener) {
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

    public boolean installEnthernet(Context context) {
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


    public HashMap<Integer, Integer> getPreferenceTitles() {

        HashMap<Integer, Integer> preferenceMap = new HashMap<>();

        Context context = ApplicationController.getContext();
        int prefs[] = new int[]{
                R.xml.preference_sterilization,
                R.xml.preferences_device,
                R.xml.preferences_glp,
                R.xml.preferences_language,
                R.xml.preferences_lockout,
                R.xml.preferences_network,
                R.xml.preference_autoclave
        };
        final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";

        for (int res : prefs) {
            XmlResourceParser xpp = context.getResources().getXml(res);
            int eventType;
            try {
                eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getAttributeCount() > 0) {
                            String key = xpp.getAttributeValue(ANDROID_NAMESPACE, "key");
                            if (key != null && key.contains("@"))
                                key = context.getString(xpp.getAttributeResourceValue(ANDROID_NAMESPACE, "key", -1));
                            if (key != null) {
                                preferenceMap.put(key.hashCode(), xpp.getAttributeResourceValue(ANDROID_NAMESPACE, "title", -1));
                            }
                        }
                    }
                    eventType = xpp.next();
                }
                xpp.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return preferenceMap;
    }


    public Drawable changeColorToWhite(Drawable drawable) {
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }

    public void uploadLiveDebug(final Context context) {
        Needle.onBackgroundThread().execute(new UiRelatedTask<Boolean>() {
            @Override
            protected Boolean doWork() {

                try {
                    Protocol protocol = DatabaseService.getInstance().getProtocolById(Autoclave.getInstance().getProtocol().getProtocol_id());
                    //PROTOCOL-ENTRYS ARRAY
                    JSONArray entryJSONArray = new JSONArray();
                    Date startTime = protocol.getStartTime();
                    Date lastEntry = null;
                    for (ProtocolEntry protocolEntry : protocol.getProtocolEntry()) {
                        if (protocolEntry.getTimestamp().getTime() < startTime.getTime()) continue;
                        JSONObject entryJSONObject = new JSONObject();
                        entryJSONObject.put("ts", String.format(Locale.US, "%.2f",
                                ((float) (protocolEntry.getTimestamp().getTime() - startTime.getTime())) / (1000.0 * 60.0)));
                        entryJSONObject.put("tmp", String.format(Locale.US, "%.2f",
                                protocolEntry.getTemperature()));
                        entryJSONObject.put("mtmp", String.format(Locale.US, "%.2f",
                                protocolEntry.getMediaTemperature()));
                        entryJSONObject.put("mtmp_2", String.format(Locale.US, "%.2f",
                                protocolEntry.getMediaTemperature2()));
                        entryJSONObject.put("prs", String.format(Locale.US, "%.2f", protocolEntry.getPressure()));
                        entryJSONObject.put("input", protocolEntry.getDebugInput());
                        entryJSONObject.put("output", protocolEntry.getDebugOutput());
                        if (lastEntry == null || lastEntry.before(protocolEntry.getTimestamp()))
                            lastEntry = protocolEntry.getTimestamp();
                        entryJSONArray.put(entryJSONObject);
                    }

                    //PROGRAM OBJECT
                    //COMMANDS-ARRAY of Program
                    JSONArray jsonCommandArray = new JSONArray();
                    JSONObject commandJSONObject = new JSONObject();
                    jsonCommandArray.put(commandJSONObject);


                    // PROGRAM parameters
                    JSONObject programJsonObject = new JSONObject();
                    programJsonObject.put("msensor", false);
                    programJsonObject.put("vent", false);
                    programJsonObject.put("lidopen", false);
                    programJsonObject.put("tbuffer", 0);
                    programJsonObject.put("title", protocol.getProfileName());
                    programJsonObject.put("note", protocol.getProfileDescription() + "\n" + generateProfileDescription(protocol));
                    programJsonObject.put("commands", jsonCommandArray);

                    JSONArray programJsonArray = new JSONArray();
                    programJsonArray.put(programJsonObject);


                    JSONObject jsonProtocolObject = new JSONObject();
                    jsonProtocolObject.put("devicekey", Autoclave.getInstance().getController().getSavetyKey());
                    jsonProtocolObject.put("program", programJsonArray);
                    jsonProtocolObject.put("start", protocol.getStartTime().getTime());
                    jsonProtocolObject.put("end", lastEntry != null ? lastEntry.getTime() : protocol.getStartTime().getTime());
                    jsonProtocolObject.put("cycle", protocol.getZyklusNumber());
                    /*
                     *  The cloud will interpret the error codes as following:
                     * 	0 Successfully completed
                     *	6 Heater error
                     *	8 Temperature overshoot error
                     *	10 Temperature sensor broken error
                     *	13 Temperature unsteadiness error
                     *	14 Program cancelled by error
                     *	15 Program cancelled by user
                     *	16 Connection lost error
                     */
                    int errorCodeCloud = 0;
                    try {
                        errorCodeCloud = protocol.getErrorCode();
                    } catch (Exception e) {
                        errorCodeCloud = 15;
                    }
                    //switch (protocol.getErrorCode()) {
                    //    case 1:
                    //        errorCodeCloud = 14;
                    //        break;
                    //    case 2:
                    //        errorCodeCloud = 15;
                    //        break;
                    //    case 3:
                    //        errorCodeCloud = 16;
                    //        break;
                    //    case -1:
                    //        errorCodeCloud = 15;
                    //        break;
                    //}
                    jsonProtocolObject.put("errcode", errorCodeCloud);
                    jsonProtocolObject.put("is_cont_by_flex_probe_1", protocol.isContByFlexProbe1());
                    jsonProtocolObject.put("is_cont_by_flex_probe_2", protocol.isContByFlexProbe2());
                    jsonProtocolObject.put("temp_unit", "C"/*AutoclaveModelManager.getInstance().getTemperatureUnit()*/);
                    jsonProtocolObject.put("entries", entryJSONArray);

                    JSONObject jsonProtocolWrapper = new JSONObject();
                    jsonProtocolWrapper.put("protocol", jsonProtocolObject);
                    String body = jsonProtocolWrapper.toString();


                    //POST the Json object to CertoCloud
                    PostUtil postUtil = new PostUtil();
                    Response response = postUtil.postToCertocloud(body,
                            CertocloudConstants.getServerUrl() +
                                    CertocloudConstants.REST_API_POST_PROTOCOL_LIVE, true);

                    if (response.getStatus() == PostUtil.RETURN_OK) {

                        JSONObject json = new JSONObject(postUtil.getResponseBody());//in json is saved the result
                        JSONObject protocolJSONObject = json.getJSONObject("message");
                        String cloudId = protocolJSONObject.getString("_id");
                        Log.e("PostProtocolThread", "parsedCloudId: " + cloudId);

                        JSONObject content = new JSONObject();
                        try {
                            content.put("isRunning", true);
                            content.put("device_key", AutoclaveModelManager.getInstance().getSerialNumber());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        SocketService.getInstance().getSocket().emit(SocketService.EVENT_SEND_LIVE_DEBUG,
                                content);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void thenDoUiRelatedWork(Boolean aBoolean) {

            }
        });
    }

    public void getCloudPrograms(final Context context) {
        final SweetAlertDialog barProgressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        barProgressDialog.setTitleText(context.getString(R.string.loading));
        barProgressDialog.setContentText(null);
        barProgressDialog.showCancelButton(false);
        barProgressDialog.setCanceledOnTouchOutside(false);

        barProgressDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {

                Log.e("Helper", "CLEAR PROGRAM LIST");
                sweetAlertDialog.dismissWithAnimation();
            }
        });

        barProgressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                barProgressDialog.setConfirmText(null);
                barProgressDialog.setCancelText(null);
                barProgressDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                barProgressDialog.setContentText(null);
                barProgressDialog.setTitleText(context.getString(R.string.loading));
                getCloudPrograms(context);
            }
        });

        Requests.getInstance().getCloudPrograms(new MyCallback() {
            @Override
            public void onSuccess(Object response, int requestId) {
                List<Profile> oldProfiles = new ArrayList<>(Autoclave.getInstance().getProfilesFromAutoclave());
                Autoclave.getInstance().getProfilesFromAutoclave().clear();
                for (Profile profile : ((DeviceProgramsResponseModel) response).getPrograms()) {
                    if (oldProfiles.contains(profile)) {
                        profile.setRecentUsedDate(oldProfiles.get(oldProfiles.indexOf(profile)).getRecentUsedDate());
                        profile.setDescription(getProfileDesc(profile, context));
                        oldProfiles.remove(profile);
                    }
                    Autoclave.getInstance().getProfilesFromAutoclave().add(profile);
                    profile.setLocal(false);
                    ReadAndParseSerialService.getInstance().setProgram(profile);
                }

                //Check are there any deleted profile in cloud, setting name to VOID means disable
                // the profile in the autoclave
                for (Profile deletedProfiles : oldProfiles) {
                    deletedProfiles.setName("VOID");
                    ReadAndParseSerialService.getInstance().setProgram(deletedProfiles);
                }
                oldProfiles.clear();
                barProgressDialog.dismiss();
                Autoclave.getInstance().notifyProfilesHasBeenSynced();
            }

            @Override
            public void onError(ErrorModel error, int requestId) {
                barProgressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                barProgressDialog.setTitleText(context.getString(R.string.failed));
                barProgressDialog.setContentText(context.getString(R.string.something_went_wrong_try_again));
                barProgressDialog.setCancelText(context.getString(R.string.close));
                barProgressDialog.setConfirmText(context.getString(R.string.try_again));
            }

            @Override
            public void onStart(int requestId) {
//                barProgressDialog.show();
            }

            @Override
            public void onProgress(int current, int max) {

            }
        }, 10);
    }

    static Runnable runnableTimeout = null;
    static int failCount = 0;

    public void getPrograms(final Context context) {

        failCount = 0;
        if (AppConstants.isIoSimulated) {
            Autoclave.getInstance();
            return;
        }

        final SweetAlertDialog barProgressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        barProgressDialog.setTitleText(context.getString(R.string.loading));
        barProgressDialog.setContentText(null);
        barProgressDialog.showCancelButton(false);
        barProgressDialog.setCancelable(false);
        barProgressDialog.setCanceledOnTouchOutside(false);
        final Handler handler = new Handler();
        final int TIMEOUT = 4000;
        final int MAX_TRY = 5;

        final MyCallback callbackProfile = new MyCallback() {

            @Override
            public void onSuccess(Object response, int requestId) {

                if (requestId != ReadAndParseSerialService.HANDLER_MSG_ACK_PROGRAMS)
                    return;
                if (response != null && ((List<Profile>) response).size() == 0) {
                    onError(null, requestId);
                    return;
                }
                if (AutoclaveModelManager.getInstance().getSerialNumber() == null
                        || AutoclaveModelManager.getInstance().getModel() == null) {
                    onError(null, requestId);
                    return;
                }
                if (runnableTimeout != null)
                    handler.removeCallbacks(runnableTimeout);

                List<Profile> oldProfiles = new ArrayList<>(Autoclave.getInstance().getProfilesFromAutoclave());
                Autoclave.getInstance().getProfilesFromAutoclave().clear();
                for (Profile profile : ((List<Profile>) response)) {
                    if (oldProfiles.contains(profile)) {
                        Log.e("Helper", "PUT PROFILE " + profile.getName() + " " + oldProfiles.get(oldProfiles.indexOf(profile)).getName());
                        profile.setRecentUsedDate(oldProfiles.get(oldProfiles.indexOf(profile)).getRecentUsedDate());
                    }
                    //Set Description
                    profile.setDescription(getProfileDesc(profile, context));
                    Autoclave.getInstance().getProfilesFromAutoclave().add(profile);
                    syncProgramWithCloud(profile);
                }
                oldProfiles.clear();
                ReadAndParseSerialService.getInstance().removeCallback(this);
                barProgressDialog.dismiss();
                Autoclave.getInstance().notifyProfilesHasBeenSynced();

            }

            @Override
            public void onError(ErrorModel error, int requestId) {
                com.certoclav.app.model.Log.e("program getting error");
                failCount++;
                if (runnableTimeout != null)
                    handler.removeCallbacks(runnableTimeout);

                if (failCount > MAX_TRY) {
                    ReadAndParseSerialService.getInstance().removeCallback(this);
                    failCount = 0;
                    barProgressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    barProgressDialog.setTitleText(context.getString(R.string.failed));
                    barProgressDialog.setContentText(context.getString(R.string.something_went_wrong_try_again));
                    barProgressDialog.setConfirmText(context.getString(R.string.try_again));
                    barProgressDialog.setCancelText(context.getString(R.string.settings));
                    barProgressDialog.showCancelButton(true);

                } else {
                    ReadAndParseSerialService.getInstance().getPrograms();
                    if (runnableTimeout != null) {
                        handler.postDelayed(runnableTimeout, TIMEOUT);
                    }
                }
            }

            @Override
            public void onStart(int requestId) {

            }

            @Override
            public void onProgress(int current, int max) {

            }
        };

        runnableTimeout = new Runnable() {
            @Override
            public void run() {
                failCount++;
                if (failCount > MAX_TRY) {
                    ReadAndParseSerialService.getInstance().removeCallback(callbackProfile);
                    failCount = 0;
                    barProgressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    barProgressDialog.setTitleText(context.getString(R.string.failed));
                    barProgressDialog.setContentText(context.getString(R.string.something_went_wrong_try_again));
                    barProgressDialog.setConfirmText(context.getString(R.string.try_again));
                    barProgressDialog.setCancelText(context.getString(R.string.settings));
                    barProgressDialog.showCancelButton(true);
                } else {
                    ReadAndParseSerialService.getInstance().getPrograms();
                    if (runnableTimeout != null) {
                        handler.postDelayed(runnableTimeout, TIMEOUT);
                    }
                }
            }
        };

        ReadAndParseSerialService.getInstance().addCallback(callbackProfile);


        barProgressDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {

                final SweetAlertDialog dialog = new SweetAlertDialog(context, R.layout.dialog_admin_password, SweetAlertDialog.WARNING_TYPE);
                dialog.setContentView(R.layout.dialog_admin_password);
                dialog.setTitle(R.string.register_new_user);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                final EditText editTextPassword = dialog.findViewById(R.id.editTextDesc);
                Button buttonLogin = (Button) dialog
                        .findViewById(R.id.dialogButtonLogin);
                Button buttonCancel = (Button) dialog
                        .findViewById(R.id.dialogButtonCancel);
                buttonLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Helper.getInstance().checkAdminPassword(context, editTextPassword.getText().toString())) {
                            Intent intent2 = new Intent(context, SettingsActivity.class);
                            intent2.putExtra("isAdmin", true);
                            context.startActivity(intent2);
                            dialog.dismiss();
                        } else {
                            Toasty.error(context, context.getString(R.string.admin_password_wrong), Toast.LENGTH_SHORT, true).show();
                        }
                    }
                });

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismissWithAnimation();
                    }
                });

                dialog.show();

            }

        });

        barProgressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                ReadAndParseSerialService.getInstance().addCallback(callbackProfile);
                barProgressDialog.setConfirmText(null);
                barProgressDialog.setCancelText(null);
                barProgressDialog.showCancelButton(false);
                barProgressDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                barProgressDialog.setContentText(null);
                barProgressDialog.setTitleText(context.getString(R.string.loading));
                ReadAndParseSerialService.getInstance().getPrograms();
                if (runnableTimeout != null)
                    handler.postDelayed(runnableTimeout, TIMEOUT);
            }
        });
        Log.e("Helper", "CLEAR PROGRAM LIST");
        barProgressDialog.show();
        ReadAndParseSerialService.getInstance().getPrograms();
        handler.postDelayed(runnableTimeout, TIMEOUT);
    }

    public void setProgram(final Context context, final Profile profile, final MyCallback callback) {
        if (AppConstants.isIoSimulated) {
            Autoclave.getInstance();
            return;
        }
//        final Context context = ApplicationController.getContext();
        failCount = 0;
        final SweetAlertDialog barProgressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        barProgressDialog.setTitleText(context.getString(R.string.loading));
        barProgressDialog.setContentText(null);
        barProgressDialog.showCancelButton(false);
        barProgressDialog.setCanceledOnTouchOutside(false);
        final Handler handler = new Handler();
        final int TIMEOUT = 4000;
        final int MAX_TRY = 3;

        final MyCallback callbackProfile = new MyCallback() {

            @Override
            public void onSuccess(Object response, int requestId) {

                if (requestId != ReadAndParseSerialService.HANDLER_MSG_ACK_PROGRAM)
                    return;
                if (response != null && (response instanceof Integer && ((Integer) response) == 0)) {
                    onError(null, requestId);
                    return;
                }
                if (callback != null)
                    callback.onSuccess(1, -1);

                if (runnableTimeout != null) {
                    handler.removeCallbacks(runnableTimeout);
                }
                profile.setRecentUsedDate(new Date().getTime());
                ReadAndParseSerialService.getInstance().removeCallback(this);
                barProgressDialog.dismiss();

            }

            @Override
            public void onError(ErrorModel error, int requestId) {
                com.certoclav.app.model.Log.e("program getting error");
                failCount++;
                if (runnableTimeout != null)
                    handler.removeCallbacks(runnableTimeout);

                if (failCount > MAX_TRY) {
                    ReadAndParseSerialService.getInstance().removeCallback(this);
                    failCount = 0;
                    barProgressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    barProgressDialog.setTitleText(context.getString(R.string.failed));
                    barProgressDialog.setContentText(context.getString(R.string.something_went_wrong_try_again));
                    barProgressDialog.setCancelText(context.getString(R.string.close));
                    barProgressDialog.setConfirmText(context.getString(R.string.try_again));
                } else {
                    ReadAndParseSerialService.getInstance().setProgram(profile);
                    if (runnableTimeout != null) {
                        handler.postDelayed(runnableTimeout, TIMEOUT);
                    }
                }
            }

            @Override
            public void onStart(int requestId) {

            }

            @Override
            public void onProgress(int current, int max) {

            }
        };

        runnableTimeout = new Runnable() {
            @Override
            public void run() {
                failCount++;
                if (failCount > MAX_TRY) {
                    ReadAndParseSerialService.getInstance().removeCallback(callbackProfile);
                    failCount = 0;
                    barProgressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    barProgressDialog.setTitleText(context.getString(R.string.failed));
                    barProgressDialog.setContentText(context.getString(R.string.something_went_wrong_try_again));
                    barProgressDialog.setCancelText(context.getString(R.string.close));
                    barProgressDialog.setConfirmText(context.getString(R.string.try_again));
                } else {
                    ReadAndParseSerialService.getInstance().setProgram(profile);
                    if (runnableTimeout != null) {
                        handler.postDelayed(runnableTimeout, TIMEOUT);
                    }
                }
            }
        };

        ReadAndParseSerialService.getInstance().addCallback(callbackProfile);

        barProgressDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {

                Log.e("Helper", "CLEAR PROGRAM LIST");
                sweetAlertDialog.dismissWithAnimation();
            }
        });

        barProgressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                ReadAndParseSerialService.getInstance().addCallback(callbackProfile);
                barProgressDialog.setConfirmText(null);
                barProgressDialog.setCancelText(null);
                barProgressDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                barProgressDialog.setContentText(null);
                barProgressDialog.setTitleText(context.getString(R.string.loading));
                ReadAndParseSerialService.getInstance().setProgram(profile);
                if (runnableTimeout != null)
                    handler.postDelayed(runnableTimeout, TIMEOUT);
            }
        });
        Log.e("Helper", "CLEAR PROGRAM LIST");
        barProgressDialog.show();
        ReadAndParseSerialService.getInstance().setProgram(profile);
        handler.postDelayed(runnableTimeout, TIMEOUT);
    }

    public void syncProgramWithCloud(final Profile profile) {
        if (!Autoclave.getInstance().isOnlineMode(ApplicationController.getContext()))
            return;
        Needle.onBackgroundThread().execute(new UiRelatedTask<Boolean>() {
            @Override
            protected Boolean doWork() {

                try {
                    //Generate Profile properties
                    Log.e("SyncProfilesThread", "gnerate program info for json");
                    JSONObject programJsonObject = new JSONObject();
                    JSONObject sterlizationTime = new JSONObject();
                    sterlizationTime.put("h", profile.getSterilisationTime() / 60);
                    sterlizationTime.put("m", profile.getSterilisationTime() % 60);

                    JSONObject dryTime = new JSONObject();
                    dryTime.put("h", profile.getDryTime() / 60);
                    dryTime.put("m", profile.getDryTime() % 60);

                    programJsonObject.put("title", profile.getName());
                    programJsonObject.put("note", profile.getDescription(true));
                    programJsonObject.put("id", profile.getIndex());
                    programJsonObject.put("tmp", profile.getSterilisationTemperature(true));
                    programJsonObject.put("is_liquid", profile.isLiquidProgram());
                    programJsonObject.put("is_cont_by_flex_probe_1", profile.isContByFlexProbe1Enabled());
                    programJsonObject.put("is_cont_by_flex_probe_2", profile.isContByFlexProbe2Enabled());
                    programJsonObject.put("is_maintain_enabled", profile.isMaintainEnabled());
                    programJsonObject.put("final_temp", profile.getFinalTemp(true));
                    programJsonObject.put("use_f_function", profile.isF0Enabled());
                    programJsonObject.put("dur", sterlizationTime);
                    programJsonObject.put("dry_dur", dryTime);
                    programJsonObject.put("f0_value", profile.getF0Value());
                    programJsonObject.put("z_value", profile.getzValue(true));
                    programJsonObject.put("is_from_android", true);
                    programJsonObject.put("deviceKey", Autoclave.getInstance()
                            .getController().getSavetyKey());
                    programJsonObject.put("model", AutoclaveModelManager.getInstance().getModel());


                    JSONObject programWrapper = new JSONObject();
                    programWrapper.put("program", programJsonObject);


                    String body = programWrapper.toString();

                    PostUtil postUtil = new PostUtil();
                    Response response = postUtil.postToCertocloud(body,
                            CertocloudConstants.getServerUrl() +
                                    CertocloudConstants.REST_API_POST_PROFILE, false);

                    Log.e("Response", response.toString());

                    if (response.getStatus() == PostUtil.RETURN_OK) {
                        DatabaseService db = DatabaseService.getInstance();
//                db.updateProfileIsLocal(profile.getProfile_id(), false); //not neccessary
                        JSONObject jsonResponse = new JSONObject(postUtil.getResponseBody());
                        JSONObject jsonResponseProgram = jsonResponse.getJSONObject("program");
                        String cloudId = jsonResponseProgram.getString("_id");
                        return cloudId != null && cloudId.length() > 0;
                    }
                } catch (Exception e) {
                    Log.e("SyncProfileThread", "exception: " + e.toString());
                }
                return false;
            }

            @Override
            protected void thenDoUiRelatedWork(Boolean result) {
                profile.setLocal(!result);
                Autoclave.getInstance().notifyProfilesHasBeenSynced();
            }
        });
    }

    public void askConfirmation(Context context, String title, String content, final SweetAlertDialog.OnSweetClickListener onConfirm,
                                SweetAlertDialog.OnCancelListener onCancel) {
        final SweetAlertDialog dialogConfirmation = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        dialogConfirmation.showCancelButton(true);
        dialogConfirmation.setCancelText(context.getString(R.string.cancel));
        dialogConfirmation.setCanceledOnTouchOutside(false);
        dialogConfirmation.setConfirmText(context.getString(R.string.yes));
        dialogConfirmation.setTitleText(title);
        dialogConfirmation.setContentText(content);
        dialogConfirmation
                .setConfirmClickListener(onConfirm);
        dialogConfirmation.setOnCancelListener(onCancel);
        dialogConfirmation.show();

    }

    public String getProfileDesc(Profile profile, Context context) {
        StringBuilder sbuilder = new StringBuilder();
        if (profile.getSterilisationTemperature(false) != 0) {
            sbuilder.append("[")
                    .append(profile.getSterilisationTemperature(true))
                    .append("]");
        }
        if (profile.getSterilisationPressure() != 0) {
            sbuilder.append(Float.toString(profile.getSterilisationPressure()))
                    .append(" " + context.getString(R.string.bar))
                    .append(" ");
        }
        if (profile.getSterilisationTime() != 0 && !profile.isF0Enabled()) {
            sbuilder.append(profile.getSterilisationTime())
                    .append(" " + context.getString(R.string.min))
                    .append("\n");
        } else {
            sbuilder.append("\n");
        }
        if (profile.getVacuumTimes() != 0) {
            sbuilder.append(context.getString(R.string.vacuum_times) + " ")
                    .append(profile.getVacuumTimes())
                    .append("\n");
        }
        if (profile.getVacuumPersistTemperature() != 0) {
            sbuilder.append(context.getString(R.string.vacuum_persist_temperature) + " ")
                    .append(profile.getVacuumPersistTemperature())
                    .append(" " + getTemperatureUnitText(null))
                    .append("\n");
        }
        if (profile.getVacuumPersistTime() != 0) {
            sbuilder.append(context.getString(R.string.vacuum_persist_time) + " ")
                    .append(profile.getVacuumPersistTime())
                    .append(" " + context.getString(R.string.min))
                    .append("\n");
        }
        if (profile.getDryTime() != 0 && AutoclaveModelManager.getInstance().isDryTimeExists()
                && !profile.isLiquidProgram()) {
            sbuilder.append(context.getString(R.string.drying_time) + " ")
                    .append(profile.getDryTime())
                    .append(" " + context.getString(R.string.min))
                    .append("\n");
        }
        if (profile.isF0Enabled()) {
            sbuilder.append(context.getString(R.string.f0_value_format, profile.getF0Value()))
                    .append("\t")
                    .append(context.getString(R.string.z_value_format, profile.getzValue(false),
                            Helper.getInstance().getTemperatureUnitText(null)))
                    .append("\n");
        }
        if (profile.isMaintainEnabled()) {
            sbuilder.append(context.getString(R.string.maintain_final_temp_format, profile.getFinalTemp(false),
                    getTemperatureUnitText(null)))
                    .append("\n");
        }
        if (profile.isContByFlexProbe1Enabled()) {
            sbuilder.append(context.getString(R.string.cont_by_flex_probe_1))
                    .append("\n");
        }
        if (profile.isContByFlexProbe2Enabled()) {
            sbuilder.append(context.getString(R.string.cont_by_flex_probe_2))
                    .append("\n");
        }

        return sbuilder.toString();
    }

    public String getStateText() {
        Context context = AppController.getContext();
        switch (Autoclave.getInstance().getProgramStep()) {
            case VACUUM_PULSE_1:
            case VACUUM_PULSE_1_:
                return context.getString(R.string.current_program_step_vacuum_desc, 1,
                        Autoclave.getInstance().getProfile().getVacuumTimes());
            case VACUUM_PULSE_2:
            case VACUUM_PULSE_2_:
                return context.getString(R.string.current_program_step_vacuum_desc, 2,
                        Autoclave.getInstance().getProfile().getVacuumTimes());

            case VACUUM_PULSE_3:
            case VACUUM_PULSE_3_:
                return context.getString(R.string.current_program_step_vacuum_desc, 3,
                        Autoclave.getInstance().getProfile().getVacuumTimes());

            case HEATING:
                return context.getString(R.string.current_program_step_heating_desc,
                        Autoclave.getInstance().getProfile().getSterilisationTemperature(false));

            case STERILIZATION:
                return context.getString(R.string.sterilisation);

            case DRYING:
                return context.getString(R.string.current_program_step_drying_desc);

            case DISCHARGE:
                return context.getString(R.string.current_program_step_discharging_desc);

            case LEVELING:
                return context.getString(R.string.current_program_step_leveling_desc);

            case WARMING_UP:
                return context.getString(R.string.current_program_step_warming_up_desc);

            case VENTILATION:
                return context.getString(R.string.current_program_step_ventilation_up_desc);

            case MAINTAIN_TEMP:
                return context.getString(R.string.success_sterilization_and_maintain_temp,
                        Autoclave.getInstance().getProfile().getFinalTemp(false));

            case STABILIZATION:
                return context.getString(R.string.current_program_step_stabilization_desc);

            case COOLING_DOWN:
                return context.getString(R.string.current_program_step_cooling_down_desc);
            case FINISHED:
                return context.getString(R.string.state_finished);
            case START_PROCESS:
                return context.getString(R.string.state_start_process);
            case EMPTY_WAIT:
                return context.getString(R.string.state_empty_wait);
            case EMPTY_TEST:
                return context.getString(R.string.state_empty_test);
            case V_LEVELING:
                return context.getString(R.string.current_program_step_leveling_desc);
            case NOT_DEFINED:
                return "---";
        }
        return "---";
    }

    public String generateProfileDescription(Protocol protocol) {

        try {
            StringBuilder sbuilder = new StringBuilder();
            if (protocol.getVacuumTimes() != 0) {
                sbuilder.append("\r\nVacuum times: ")
                        .append(protocol.getVacuumTimes())
                        .append("\r\n");
            }

            if (protocol.getSterilisationTemperature() != 0) {
                sbuilder.append("Sterilisation temperature: ")
                        .append(protocol.getSterilisationTemperature())
                        .append(Helper.getInstance().getTemperatureUnitText(AutoclaveModelManager.getInstance().getTemperatureUnit()))
                        .append("\r\n");
            }

            if (protocol.getSterilisationPressure() != 0) {
                sbuilder.append("Sterilisation pressure: ")
                        .append(roundFloat((protocol.getSterilisationPressure() * 0.01f) + 1f).toString())
                        .append(" bar")
                        .append("\r\n");
            }

            if (protocol.getSterilisationTime() != 0) {
                sbuilder.append("Sterilisation holding time: ")
                        .append(protocol.getSterilisationTime())
                        .append(" min")
                        .append("\r\n");
            }

            if (protocol.getVacuumPersistTemperature() != 0) {
                sbuilder.append("Vacuum persist temperature: ")
                        .append(protocol.getVacuumPersistTemperature())
                        .append(Helper.getInstance().getTemperatureUnitText(AutoclaveModelManager.getInstance().getTemperatureUnit()))
                        .append("\r\n");
            }
            if (protocol.getVacuumPersistTime() != 0) {
                sbuilder.append("Vacuum persist time: ")
                        .append(protocol.getVacuumPersistTime())
                        .append(" min")
                        .append("\r\n");
            }
            if (protocol.getDryTime() != 0) {
                sbuilder.append("Drying time: ")
                        .append(protocol.getDryTime())
                        .append(" min");
            }
            return sbuilder.toString();
        } catch (Exception e) {
            return "";
        }
    }


    public Double roundFloat(float f) {
        int tempnumber = (int) ((f + 0.00001) * 100);
        Double roundedfloat = (double) ((double) tempnumber / 100.0);
        return roundedfloat;
    }

    public float roundFloat2(float f) {
        String str = String.format(Locale.US, "%.2f", f);
        return Float.valueOf(str);
    }

    public String getTemperatureUnitText(String unit) {
        if (unit == null)
            return AutoclaveModelManager.getInstance().
                    getTemperatureUnit().equals("F") ? "\u2109" : "\u2103";

        return (unit != null && unit.equals("F"))
                ? "\u2109" : "\u2103";
    }

    public float currentUnitToCelsius(float temp) {
        switch (AutoclaveModelManager.getInstance().getTemperatureUnit()) {
            case "F":
                return roundFloat2(((temp - 32) * 5) / 9);
        }
        return roundFloat2(temp);
    }


    public float celsiusToCurrentUnit(float temp) {
        switch (AutoclaveModelManager.getInstance().getTemperatureUnit()) {
            case "F":
                return roundFloat2((temp * 9) / 5 + 32);
        }
        return roundFloat2(temp);
    }


    public SweetAlertDialog getDialog(Context context, String title, String content,
                                      Boolean showCancel, Boolean isCancelable,
                                      SweetAlertDialog.OnSweetClickListener onCancel,
                                      SweetAlertDialog.OnSweetClickListener onConfirm) {
        final SweetAlertDialog barProgressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        barProgressDialog.setTitleText(title);
        barProgressDialog.setContentText(content);
        barProgressDialog.showCancelButton(showCancel);
        barProgressDialog.setCancelable(isCancelable);
        barProgressDialog.setCanceledOnTouchOutside(false);

        barProgressDialog.setCancelClickListener(onCancel);
        barProgressDialog.setConfirmClickListener(onConfirm);

        return barProgressDialog;
    }

    public void askPermissionFDA(final CloudSocketThread socket) {

        if (!CloudUser.getInstance().isLoggedIn()) {
            socket.sendPermissionResponse(false);
            return;
        }
        Context context = AppController.getContext();
        final SweetAlertDialog dialogConfirmation = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        dialogConfirmation.showCancelButton(true);
        dialogConfirmation.setCancelText(context.getString(R.string.deny));
        dialogConfirmation.setCanceledOnTouchOutside(false);
        dialogConfirmation.setConfirmText(context.getString(R.string.allow));
        dialogConfirmation.setTitleText(context.getString(R.string.raypa_access));
        dialogConfirmation.setContentText(context.getString(R.string.would_you_like_allow_raypa_to_access_your_data));
        dialogConfirmation.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);

        dialogConfirmation.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                socket.sendPermissionResponse(false);
                sweetAlertDialog.dismissWithAnimation();
            }
        });
        dialogConfirmation
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        Requests.getInstance().enableDeviceFDAAccess(new MyCallback() {
                            @Override
                            public void onSuccess(Object response, int requestId) {
                                socket.sendPermissionResponse(true);
                            }

                            @Override
                            public void onError(ErrorModel error, int requestId) {
                                socket.sendPermissionResponse(false);
                            }

                            @Override
                            public void onStart(int requestId) {

                            }

                            @Override
                            public void onProgress(int current, int max) {

                            }
                        }, AppConstants.PERMISSION_TIMEOUT_FDA, 13);
                    }
                });

        dialogConfirmation.show();
    }


}
