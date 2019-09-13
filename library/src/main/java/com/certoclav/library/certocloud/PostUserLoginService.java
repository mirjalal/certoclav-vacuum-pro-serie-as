package com.certoclav.library.certocloud;

import android.os.AsyncTask;
import android.util.Log;

import com.certoclav.library.models.DeviceModel;
import com.certoclav.library.util.Response;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;


public class PostUserLoginService {


    public interface PutUserLoginTaskFinishedListener {
        void onTaskFinished(Response response);
    }

    private PutUserLoginTaskFinishedListener putUserTaskFinishedListener = null;

    private String email;
    private String password;
    private DeviceModel device;
    private PostUtil postUtil;


    // Get profiles from the server
    public void loginUser(String email, String password, DeviceModel device) {
        this.email = email;
        this.password = password;
        this.device = device;
        new PutUserLoginTask().execute();
    }


    private class PutUserLoginTask extends AsyncTask<String, String, Response> {

        protected Response doInBackground(String... pw) {
            int responseCode = -1;
            try {

                //prepare JSON body with login information
                JSONObject object = new JSONObject();
                try {
                    object.put("username", email);
                    object.put("password", password);
                    object.put("devices", new Gson().toJson(device));

                } catch (Exception ex) {
                    Log.e("RestUserLoginTask", ex.toString());
                }

                String body = object.toString();

                //Post login information to CertoCloud
                postUtil = new PostUtil();
                return postUtil.postToCertocloud(body, CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_POST_LOGIN, false);

            } catch (Exception e) {
            }


            return null;

        }


        protected void onPostExecute(Response response) {

            if (response != null && response.isOK()) {
                JSONObject loginJSONObject;
                try {
                    loginJSONObject = new JSONObject(postUtil.getResponseBody());

                    CloudUser.getInstance().setEmail(email);
                    CloudUser.getInstance().setLoggedIn(true);
                    if (loginJSONObject.has("token"))
                        CloudUser.getInstance().setToken(loginJSONObject.getString("token"));
                    CloudUser.getInstance().setCurrentDeviceKey(device.getDeviceKey());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else
                //Password has expired
                if (response != null && response.getStatus() == 406) {
                    JSONObject loginJSONObject;
                    try {
                        loginJSONObject = new JSONObject(postUtil.getResponseBody());
                        CloudUser.getInstance().setEmail(email);
                        if (loginJSONObject.has("token"))
                            CloudUser.getInstance().setToken(loginJSONObject.getString("token"));
                        CloudUser.getInstance().setCurrentDeviceKey(device.getDeviceKey());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            if (putUserTaskFinishedListener != null) {
                putUserTaskFinishedListener.onTaskFinished(response);
            }
        }


    }


    /*
     * if login successfull, return value is HTTP_OK (200)
     *
     * if username or password wrong: return value is HTTP_UNAUTHORISED (401)
     * {
     * 		"status": 401,
     *		"message": "Invalid Username or Password"
     *	}
     *
     *
     */
    public void setOnTaskFinishedListener(PutUserLoginTaskFinishedListener listener) {
        this.putUserTaskFinishedListener = listener;

    }


}
