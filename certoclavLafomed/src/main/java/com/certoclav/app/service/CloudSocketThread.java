package com.certoclav.app.service;

import android.os.Handler;
import android.os.Looper;

import com.certoclav.app.R;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.Log;
import com.certoclav.app.util.AppController;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.Helper;
import com.certoclav.library.certocloud.SocketService;
import com.certoclav.library.certocloud.SocketService.SocketEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;


/**
 * Class that inherits {@link Thread} and manages the communication with the
 * microcontroller in order to read values from temperature and pressure sensors.
 *
 * @author Iulia Rasinar &lt;iulia.rasinar@nordlogic.com&gt;
 */
public class CloudSocketThread extends Thread implements SocketEventListener {


    private int counterSendLiveDataToServer = 0;
    private JSONObject jsonLiveMessageObj = null;
    private boolean runFlag = true;
    private int index = 0;

    public CloudSocketThread() {


    }


    @Override
    public void run() {

        try {
            SocketService.getInstance().setOnSocketEventListener(this);
            SocketService.getInstance().setDeviceKey("");
            if (Autoclave.getInstance().getController().getSavetyKey() == null) {
                endThread();
                return;
            }
            SocketService.getInstance().connectToCertocloud();
            jsonLiveMessageObj = new JSONObject();
            counterSendLiveDataToServer = 0;
        } catch (Exception e) {
            Log.e("SocketTread", "Thread init exception: " + e.toString());
        }

        while (runFlag) {
            //if(! SocketService.getInstance().getSocket().connected()){
            //	Log.e("SocketTread", "trying to connect to socket");
            //	SocketService.getInstance().connectToCertocloud();
            //}
            if (counterSendLiveDataToServer > 0) {
                counterSendLiveDataToServer--;
                if (SocketService.getInstance().getSocket().connected()) {
                    jsonLiveMessageObj = new JSONObject();

                    String timeSinceStartString = "";
                    try {


                        int seconds = (int) (Autoclave.getInstance().getSecondsSinceStart() % 60);
                        int minutes = (int) ((Autoclave.getInstance().getSecondsSinceStart() / 60) % 60);
                        int hours = (int) ((Autoclave.getInstance().getSecondsSinceStart() / 60 / 60) % 24);
                        StringBuilder sBuilder = new StringBuilder();
                        sBuilder.append(String.format("%02d", hours))
                                .append(":")
                                .append(String.format("%02d", minutes))
                                .append(":")
                                .append(String.format("%02d", seconds));
                        timeSinceStartString = sBuilder.toString();
                        //loadingBar.getLayoutParams().width = 300 - (3* ((monitorService.getRemainingTime()*100) / monitorService.getAbsoluteTime(Autoclave.getInstance().getProfile())));
                    } catch (Exception e) {
                        //do nothing
                    }

                    String profileName = "";
                    try {
                        profileName = Autoclave.getInstance().getProfile().getName();
                    } catch (Exception e) {
                        profileName = "";
                    }

                    Integer cycleNumber = 0;
                    try {
                        cycleNumber = Autoclave.getInstance().getController().getCycleNumber();
                    } catch (Exception e) {
                        cycleNumber = 0;
                    }

                    String deviceKey = "";
                    try {
                        deviceKey = Autoclave.getInstance().getController().getSavetyKey();
                    } catch (Exception e) {
                        deviceKey = "";
                    }

                    String errorMessage = "";
                    try {
                        if (AutoclaveMonitor.getInstance().getErrorList().size() > 0) {
                            errorMessage = AutoclaveMonitor.getInstance().getErrorList().get(0).getMsg();
                        }
                    } catch (Exception e) {
                        errorMessage = "";
                    }


                    try {
                        jsonLiveMessageObj.put("device_key", deviceKey);
                        jsonLiveMessageObj.put("index", index);

                        JSONObject jsonLiveMessageDataObj = new JSONObject();


                        jsonLiveMessageDataObj.put(AppController.getInstance().
                                getApplicationContext().getString(R.string.cycle), cycleNumber);

                        if (Autoclave.getInstance().getState() != AutoclaveState.LOCKED) {
                            jsonLiveMessageDataObj.put(AppController.getInstance().
                                            getApplicationContext().getString(R.string.user_lower)
                                    , Autoclave.getInstance().getUser().getEmail());
                        }

                        if (!errorMessage.equals("")) {
                            jsonLiveMessageDataObj.put(AppController.getInstance().
                                            getApplicationContext().getString(R.string.warning_lower)
                                    , errorMessage);
                        }
                        jsonLiveMessageDataObj.put(AppController.getInstance().
                                        getApplicationContext().getString(R.string.status)
                                , Autoclave.getInstance().getState().toString().replace("_", " "));

                        if (Autoclave.getInstance().getState() == AutoclaveState.RUNNING) {
                            jsonLiveMessageDataObj.put(AppController.getInstance().
                                            getApplicationContext().getString(R.string.stage)
                                    , Helper.getStateText());
                        }
                        //jsonLiveMessageDataObj.put("Time", Calendar.getInstance().getTime().toGMTString().replace(" GMT", ""));

                        if (Autoclave.getInstance().getState() != AutoclaveState.NOT_RUNNING && Autoclave.getInstance().getState() != AutoclaveState.LOCKED) {
                            jsonLiveMessageDataObj.put(AppController.getInstance().
                                            getApplicationContext().getString(R.string.program_lower)
                                    , profileName);
                            //if(Autoclave.getInstance().getProfile().getIsMediaSensor()){
                            //	jsonLiveMessageDataObj.put("Media temperature", Autoclave.getInstance().getData().getTemp2().getValueString() + " ℃");
                            //}else{
                            jsonLiveMessageDataObj.put(AppController.getInstance().
                                            getApplicationContext().getString(R.string.vessel_temperature)
                                    , Autoclave.getInstance().getData().getTemp1().getValueString() + " \u2103");


                            jsonLiveMessageDataObj.put(AppController.getInstance().
                                            getApplicationContext().getString(R.string.media_lower),
                                    Autoclave.getInstance().getData().getTemp2().getValueString() + " \u2103");

                            if (Autoclave.getInstance().getProfile() != null &&
                                    Autoclave.getInstance().getProfile().isContByFlexProbe2Enabled())
                                jsonLiveMessageDataObj.put(AppController.getInstance().
                                                getApplicationContext().getString(R.string.media_2_lower),
                                        Autoclave.getInstance().getData().getTemp3().getValueString() + " \u2103");
                            //}
                            jsonLiveMessageDataObj.put(AppController.getInstance().
                                            getApplicationContext().getString(R.string.pressure_lower)
                                    , Autoclave.getInstance().getData().getPress().getCurrentValue() + " bar");
                            jsonLiveMessageDataObj.put(AppController.getInstance().
                                            getApplicationContext().getString(R.string.time_since_start)
                                    , timeSinceStartString);

                            float timeLeftSeconds = Autoclave.getInstance().getTimeOrPercent();

                            jsonLiveMessageDataObj.put(AppController.getInstance().
                                            getApplicationContext().getString(Autoclave.getInstance().getProfile().isF0Enabled()?
                                            R.string.time_left_f:
                                            R.string.time_left)
                                    , Autoclave.getInstance().getProfile().isF0Enabled()?
                                            String.format("%.1f",timeLeftSeconds)+ "%":
                                            (String.format("%02d:%02d:%02d",
                                    (((int)timeLeftSeconds) / 60 / 60) % 24,
                                    (((int)timeLeftSeconds) / 60) % 60,
                                                    ((int)timeLeftSeconds) % 60)));
                        }
                        jsonLiveMessageObj.put("data", jsonLiveMessageDataObj);
                        Log.e("CloudSocketThread", "sending: " + jsonLiveMessageObj.toString().replace("{", "[").replace("}", "]"));

                    } catch (Exception e) {
                        Log.e("CloudSocketTread", "exception json: " + e.toString());
                    }

                    SocketService.getInstance().getSocket().emit(SocketService.EVENT_SEND_DATA_FROM_ANDROID_TO_SERVER, jsonLiveMessageObj);
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }


    }


    @Override
    public void onSocketEvent(String eventIdentifier, Object... args) {
        if (eventIdentifier.equals(SocketService.EVENT_START_SEND)) {

            int tempIndex = 0;
            //counterSendLiveDataToServer = 60;
            Log.e("MainActivity", "SOCKET RECEIVED EVENT TO START");
            String deviceKeyFromJson = "unknown_key";
            try {
                JSONObject obj = (JSONObject) args[0];
                if (obj == null) {
                    Log.e("MainActivity", "jsonObj == null");
                }
                deviceKeyFromJson = obj.getString("device_key");
                Autoclave.getInstance().setDebugMode(obj.has("debug_mode") && obj.getBoolean("debug_mode"));
                tempIndex = obj.getInt("index");


            } catch (Exception e) {
                Log.e("MainActivity", "Exception parsing json: " + e.toString());
                e.printStackTrace();
                deviceKeyFromJson = "unknown_key";
            }
            try {
                if (deviceKeyFromJson.equals(Autoclave.getInstance().getController().getSavetyKey())) {
                    index = tempIndex;
                    Log.e("MainActivity", "SOCKET DEVICE KEY MATCHES");
                    counterSendLiveDataToServer = 60;
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error matching savetykeys");
            }


        } else if (eventIdentifier.equals(Socket.EVENT_CONNECT)) {
            Log.e("MainActivity", "SOCKET CONNECTED");
            JSONObject content = new JSONObject();
            try {
                content.put("device_key", Autoclave.getInstance().getController().getSavetyKey());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SocketService.getInstance().getSocket().emit(SocketService.EVENT_REGISTER, content);
        } else if (eventIdentifier.equals(SocketService.EVENT_PROGRAM_EDIT)) {
            Log.e("Received", "Program Edited");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    Helper.getCloudPrograms(AppController.getContext());
                }
            });
        } else if (eventIdentifier.equals(SocketService.EVENT_GET_LIVE_DEBUG)) {
            Log.e("Received", "Get Live Debug");
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {

                    if(Autoclave.getInstance().getState() != AutoclaveState.RUNNING) {
                        JSONObject content = new JSONObject();
                        try {
                            content.put("isRunning", false);
                            content.put("device_key",AutoclaveModelManager.getInstance().getSerialNumber());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        SocketService.getInstance().getSocket().emit(SocketService.EVENT_SEND_LIVE_DEBUG,
                                content);
                    }else{
                        Helper.uploadLiveDebug(AppController.getContext());
                    }
                }
            });
        }


    }


    public void endThread() {
        runFlag = false;
        SocketService.getInstance().endService();
        Log.e("SocketThread", "close and destroy thread");
        //wenn runflag false ist, dann l�uft die run() Methode zu ende und der Thread wird zerst�rt.
    }
}