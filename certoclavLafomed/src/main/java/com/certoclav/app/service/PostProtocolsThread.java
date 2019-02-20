package com.certoclav.app.service;

import android.util.Log;

import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.ServerConfigs;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.PostUtil;
import com.certoclav.library.util.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Class that inherits {@link Thread} and manages the communication with the
 * microcontroller in order to read values from temperature and pressure sensors.
 *
 * @author Iulia Rasinar &lt;iulia.rasinar@nordlogic.com&gt;
 */
public class PostProtocolsThread extends Thread {
    //private boolean runFlag = true;
    DatabaseService databaseService = DatabaseService.getInstance();

    public PostProtocolsThread() {
    }


    @Override
    public void run() {

        // Loop as long as the interface is open and stop command was not sent

        try {
            //upload new protocols to certocloud every 60 seconds
            if (Autoclave.getInstance().getState() == AutoclaveState.NOT_RUNNING) {
                if ((ApplicationController.getInstance().isNetworkAvailable() || ServerConfigs.getInstance(ApplicationController.getContext()).getUrl() != null) && !CloudUser.getInstance().getToken().isEmpty()) {
                    List<Protocol> protocols = databaseService.getProtocolsWhereNotUploaded();
                    if (protocols != null) {
                        for (Protocol protocol : protocols) {
                            Log.e("AutoclaveMonitor", "protocol is not uploaded yet");

                            //PROTOCOL-ENTRYS ARRAY
                            JSONArray entryJSONArray = new JSONArray();
                            Date startTime = protocol.getStartTime();
                            Date lastEntry = null;
                            for (ProtocolEntry protocolEntry : protocol.getProtocolEntry()) {
                                JSONObject entryJSONObject = new JSONObject();
                                entryJSONObject.put("ts", String.format(Locale.US, "%.2f", ((float) (protocolEntry.getTimestamp().getTime() - startTime.getTime())) / (1000.0 * 60.0)));
                                entryJSONObject.put("tmp", String.format(Locale.US, "%.2f", protocolEntry.getTemperature()));
                                entryJSONObject.put("mtmp", String.format(Locale.US, "%.2f", protocolEntry.getMediaTemperature()));
                                entryJSONObject.put("prs", String.format(Locale.US, "%.2f", protocolEntry.getPressure() ));
                                entryJSONObject.put("mtmp", String.format(Locale.US, "%.2f", protocolEntry.getMediaTemperature()));
                                entryJSONObject.put("input", protocolEntry.getDebugInput());
                                entryJSONObject.put("output", protocolEntry.getDebugOutput());
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
                            programJsonObject.put("note", protocol.getProfileDescription() + "\n" + Helper.generateProfileDescription(protocol));
                            programJsonObject.put("commands", jsonCommandArray);

                            JSONArray programJsonArray = new JSONArray();
                            programJsonArray.put(programJsonObject);


                            JSONObject jsonProtocolObject = new JSONObject();
                            jsonProtocolObject.put("devicekey", Autoclave.getInstance().getController().getSavetyKey());
                            jsonProtocolObject.put("program", programJsonArray);
                            jsonProtocolObject.put("start", protocol.getStartTime().getTime());
                            if (protocol.getEndTime() != null)
                                jsonProtocolObject.put("end", protocol.getEndTime().getTime());
                            else if (lastEntry != null)
                                jsonProtocolObject.put("end", lastEntry.getTime());
                            else
                                jsonProtocolObject.put("end", protocol.getStartTime().getTime());

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
                            jsonProtocolObject.put("entries", entryJSONArray);

                            JSONObject jsonProtocolWrapper = new JSONObject();
                            jsonProtocolWrapper.put("protocol", jsonProtocolObject);
                            String body = jsonProtocolWrapper.toString();


                            //POST the Json object to CertoCloud
                            PostUtil postUtil = new PostUtil();
                            Response response = postUtil.postToCertocloud(body, CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_POST_PROTOCOLS, true);

                            if (response.getStatus() == PostUtil.RETURN_OK) {

                                JSONObject json = new JSONObject(postUtil.getResponseBody());//in json is saved the result
                                JSONObject protocolJSONObject = json.getJSONObject("message");
                                String cloudId = protocolJSONObject.getString("_id");
                                Log.e("PostProtocolThread", "parsedCloudId: " + cloudId);
                                databaseService.updateProtocolIsUploaded(protocol.getProtocol_id(), true);
                                databaseService.updateProtocolCloudId(protocol.getProtocol_id(), cloudId);
                            }
                            Thread.sleep(5000);
                        }
                    }
                }

            }

            Thread.sleep(60000);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void endThread() {
        //	runFlag = false;
        Log.e("PostProtocolsThread", "close and destroy thread");
        //wenn runflag false ist, dann l�uft die run() Methode zu ende und der Thread wird zerst�rt.
    }

}