package com.certoclav.app.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.responsemodels.UserProtocolsResponseModel;

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
                databaseService.deleteProtocolEntry(databaseService.getProtocols());
                databaseService.deleteSyncedProtocols();

                int current = 0;
                int max = params[0].getProtocols().size();
                for (Protocol protocol : params[0].getProtocols()) {
                    Protocol temp = new Protocol("",
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
                    for (ProtocolEntry protocolEntry : protocol.getProtocolEntries()) {
                        protocolEntry.setProtocol(temp);

                        calendar.setTime(startDate);
                        calendar.add(Calendar.SECOND, (int) (protocolEntry.getTs() * 60));
                        protocolEntry.setTimestamp(calendar.getTime());
                    }
                    databaseService.insertProtocolEntry(protocol.getProtocolEntries());
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


}
