package com.certoclav.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.certoclav.app.AppConstants;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.responsemodels.UserProtocolsResponseModel;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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


}
