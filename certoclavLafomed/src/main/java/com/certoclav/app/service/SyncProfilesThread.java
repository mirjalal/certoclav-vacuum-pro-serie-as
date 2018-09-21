package com.certoclav.app.service;

import android.os.Handler;
import android.util.Log;

import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.DeletedProfileModel;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.User;
import com.certoclav.app.model.Autoclave;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.DeleteUtil;
import com.certoclav.library.certocloud.GetUtil;
import com.certoclav.library.certocloud.PostUtil;
import com.certoclav.library.util.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Class that inherits {@link Thread} and manages the communication with the
 * microcontroller in order to read values from temperature and pressure sensors.
 *
 * @author Iulia Rasinar &lt;iulia.rasinar@nordlogic.com&gt;
 */
public class SyncProfilesThread extends Thread {

    private Handler mParentHandler;
    List<Profile> listCloudProfiles = new ArrayList<Profile>();
    private DatabaseService db;


    public SyncProfilesThread(Handler parentHandler) {
        mParentHandler = parentHandler;
        db = DatabaseService.getInstance();
    }


    /**
     * Reads the temperature and pressure values from the microcontroller.
     */
    @Override
    public void run() {

// this thread repeats following steps endless often	
// first step: get list of all profiles offline available on device
// second step: if an offline profile has no cloud_id then upload it and set cloud id of profile
// third step: get list of all profiles available from cloud
// fourth step: if an offline profile has a cloud_id, but is not online on certocloud anymore, then delete it from device
// fifth step: if an online profile is not existent in offline profiles, then insert it to database

        try {

            if (CloudUser.getInstance().isLoggedIn() == false) {
                return;//continue; //sleep for another 60 seconds
            }
            //get all offline profiles from db
            DatabaseService databaseService = DatabaseService.getInstance();
            List<Profile> listLocalDbProfiles = databaseService.getProfiles(Autoclave.getInstance().getUser().getUserId());


            listCloudProfiles.clear();
            DeleteUtil deleteUtil = new DeleteUtil();

            for (DeletedProfileModel deletedProfile : db.getDeletedProfiles()) {
                int returnval = deleteUtil.deleteToCertocloud(CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_DELETE_PROFILE + deletedProfile.getCloudId(), true);
                if (returnval == DeleteUtil.RETURN_OK) {
                    db.deleteDeletedProfile(deletedProfile);
                }
            }

            //upload offline profiles, which have never been successfully uploaded yet
            //a isUploaded flag and unique CloudId will be stored in database if upload has been successful
            //do not upload a profile, if there are already 10 profiles in cloud

            for (Profile profile : listLocalDbProfiles) {
                if (profile.isLocal() == true) {
                    postProfileToCertocloud(profile); //may updates isUploaded and Cloud_id database entries of localProfiles
                }
            }


            //get all profiles stored in certocloud
            //profiles will be stored in arrays: listCloudProfiles and listOfListCloudCommands
            boolean success = getAndParseProfilesFromCertocloud();
            if (success == false) {
                return;//continue; //skip rest of loop and sleep 60 seconds
            }
            //get updated profiles 
            listLocalDbProfiles = databaseService.getProfiles(Autoclave.getInstance().getUser().getUserId());
            //if an profile from db has been uploaded in past && is not online on certocloud anymore, then delete it from device
            for (Profile dbProfile : listLocalDbProfiles) {
                try {
                    if (dbProfile.isLocal() == false) { //profile has been uploaded in past
                        boolean dbProfileIsStillOnline = false;
                        for (Profile cloudProfile : listCloudProfiles) {
                            if (cloudProfile.getCloudId().equals(dbProfile.getCloudId())) {
                                dbProfileIsStillOnline = true;
                                Log.e("SyncProfilesThread", "Profile is still online - do not delete local version");
                                break;
                            }
                        }
                        if (dbProfileIsStillOnline == false) {
                            databaseService.deleteProfile(dbProfile);
                            Log.e("SyncProfilesThread", "Profile is still online - delete local version");
                        }
                    }
                } catch (Exception e) {
                    Log.e("SyncProfileTask", "exception: " + e.toString());
                    return;//continue;
                }
            }//end for

            //if some profiles from cloud are not inserted in db yet, then insert them:
            for (int i = 0; i < listCloudProfiles.size(); i++) {

                for (Profile dbProfile : listLocalDbProfiles) {
                    if (dbProfile.getCloudId().equals(listCloudProfiles.get(i).getCloudId())) {
                        listCloudProfiles.get(i).setRecentUsedDate(dbProfile.getRecentUsedDate());
                        db.deleteProfile(dbProfile);
                        break;
                    }
                }

                Log.e("SyncProfilesThread", "Add profile from cloud to local database because it is not in database");
                //if cloud profile does not exist in DB, denn insert it
                db.insertProfile(listCloudProfiles.get(i));

            }//end for

        } catch (Exception e) {
            Log.e("SyncProfileTask", "exception: " + e.toString());
            return;//continue;
        }

        sendMessage();
//			}//end while true
    }

    /**
     * Sends message on parent thread.
     */
    private void sendMessage() {


        if (mParentHandler != null) {
            mParentHandler.sendEmptyMessage(0);
        }
    }

    private boolean getAndParseProfilesFromCertocloud() {


        //GET PROFILES form CertoCloud
        GetUtil getUtil = new GetUtil();
        int success = getUtil.getFromCertocloud(CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_GET_PROFILES);
        String result = "";
        if (success == GetUtil.RETURN_OK) {
            result = getUtil.getResponseBody();
        } else {
            return false;
        }

        try {

            Log.e("ProfileRestService", result);

            JSONObject json = new JSONObject(result);//in json is saved the result

            JSONArray profileJSONArray = json.getJSONArray("programs");

            JSONObject profileJSONObject; //current profile
            JSONArray commandJSONArray; //to current profile corresponding commandsArray
            JSONObject commandJSONObject;
            JSONObject commandDurationJSONObject;


            //parse new cloud profiles, which are up to date
            for (int i = 0; i < profileJSONArray.length(); i++) {

                profileJSONObject = profileJSONArray.getJSONObject(i);
                commandJSONArray = profileJSONObject.getJSONArray("commands");

                User user = Autoclave.getInstance().getUser();
                if (user == null) {
                    return false;
                }

/*
                Profile currentProfile = new Profile(profileJSONObject.getString("_id"),
                        profileJSONObject.getInt("__v"),
                        profileJSONObject.has("title") ? profileJSONObject.getString("title") : "Untitled",
                        profileJSONObject.has("note") ? profileJSONObject.getString("note") : "",
                        user,
                        false,
                        true,
                        Autoclave.getInstance().getController(),
                        0);
                listCloudProfiles.add(currentProfile);*/

            }

        } catch (Exception e) {
            Log.e("ProfileRestService", e.toString());
            return false;
        }
        return true;


    }


    private void postProfileToCertocloud(Profile profile) {
        //syntax:

		/*
         *{
			    "program":{
			        "msensor":true,
			        "vent":true,
			        "lidopen":true,
			        "tmpbuffer":"2",
			        "commands":
			            [
			                {"tmp":100,"dur":{"h":22,"m":33,"s":44},"slowcool":true},
			                {"tmp":100,"dur":{"h":20,"m":50,"s":55},"slowcool":true}
			            ],
			      "title":"ThisIsAProgramName2",
			      "note":"ThisIsAShortDiscription"
			    }

		 */

        //PROGRAM OBJECT

        try {
            //Generate Commands of the program


            //Generate Profile properties
            Log.e("SyncProfilesThread", "gnerate program info for json");
            JSONObject programJsonObject = new JSONObject();
            programJsonObject.put("lidopen", false);
            programJsonObject.put("tmpbuffer", 0);
            programJsonObject.put("title", profile.getName());
            programJsonObject.put("note", profile.getDescription());
            if (profile.getCloudId() != null && profile.getCloudId().length() > 0)
                programJsonObject.put("_id", profile.getCloudId());

            JSONObject programWrapper = new JSONObject();
            programWrapper.put("program", programJsonObject);


            String body = programWrapper.toString();

            PostUtil postUtil = new PostUtil();
            Response response = postUtil.postToCertocloud(body, CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_PROFILE, true);

            if (response.getStatus() == PostUtil.RETURN_OK) {
                DatabaseService db = DatabaseService.getInstance();
//                db.updateProfileIsLocal(profile.getProfile_id(), false); //not neccessary
                JSONObject jsonResponse = new JSONObject(postUtil.getResponseBody());
                JSONObject jsonResponseProgram = jsonResponse.getJSONObject("program");
                String cloudId = jsonResponseProgram.getString("_id");
                Log.e("SyncProfilesThread", "parsed cloud id from response: " + cloudId);
//                db.updateProfileCloudId(profile.getProfile_id(), cloudId);
            }
        } catch (Exception e) {
            Log.e("SyncProfileThread", "exception: " + e.toString());
        }


    }


    public void endThread() {
        //no runFlag exists here because of no loop usage
    }

/*    private void showDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
        pDialog = new SweetAlertDialog(, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setCancelable(false);
        pDialog.setTitleText(getString(R.string.loading));
        pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismissWithAnimation();
    }*/
}