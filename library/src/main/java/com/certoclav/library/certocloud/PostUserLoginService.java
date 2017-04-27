package com.certoclav.library.certocloud;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;


public class PostUserLoginService {


    public interface PutUserLoginTaskFinishedListener {
        void onTaskFinished(int responseCode);
    }

    private PutUserLoginTaskFinishedListener putUserTaskFinishedListener = null;

    private String email;
    private String password;
    private String deviceKey;
    private PostUtil postUtil;


    // Get profiles from the server
    public void loginUser(String email, String password, String deviceKey) {
        this.email = email;
        this.password = password;
        this.deviceKey = deviceKey;
        new PutUserLoginTask().execute();
    }


    private class PutUserLoginTask extends AsyncTask<String, String, Integer> {

        protected Integer doInBackground(String... pw) {
            int responseCode = -1;
            try {

                //prepare JSON body with login information
                JSONObject object = new JSONObject();
                try {
                    object.put("username", email);
                    object.put("password", password);
                } catch (Exception ex) {
                    Log.e("RestUserLoginTask Exception", ex.toString());
                }

                String body = object.toString();

                //Post login information to CertoCloud
                postUtil = new PostUtil();
                responseCode = postUtil.postToCertocloud(body, CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_LOGIN, false);

            } catch (Exception e) {
            }


            return responseCode;

        }


        protected void onPostExecute(Integer responseCode) {

            if (responseCode == PostUtil.RETURN_OK) {
                JSONObject loginJSONObject;
                try {
                    loginJSONObject = new JSONObject(postUtil.getResponseBody());

                    CloudUser.getInstance().setEmail(email);
                    CloudUser.getInstance().setLoggedIn(true);
                    CloudUser.getInstance().setToken(loginJSONObject.getString("token"));
                    CloudUser.getInstance().setCurrentDeviceKey(deviceKey);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (putUserTaskFinishedListener != null) {
                putUserTaskFinishedListener.onTaskFinished(responseCode);
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
