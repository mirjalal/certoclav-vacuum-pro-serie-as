package com.certoclav.library.certocloud;

import android.os.AsyncTask;
import android.util.Log;

import com.certoclav.library.util.Response;

import org.json.JSONObject;


public class PostMessageService {


    public interface PostMessageFinishedListener {
        void onTaskFinished(int responseCode);
    }

    private PostMessageFinishedListener postMessageFinishedListener = null;

    private String messagetype;
    private String title;
    private String description;
    private PostUtil postUtil;


    // Get profiles from the server
    public void postMessage(String messageType, String title, String message) {
        this.messagetype = messageType;
        this.title = title;
        this.description = message;
        PostMessageTask postMessageTask = new PostMessageTask();
        postMessageTask.execute();
    }


    private class PostMessageTask extends AsyncTask<String, String, Response> {

        protected Response doInBackground(String... pw) {
            try {

                //prepare JSON body with login information
                JSONObject object = new JSONObject();
                try {
                    object.put("messagetype", messagetype);
                    object.put("title", title);
                    object.put("description", description);
                } catch (Exception ex) {
                    Log.e("RestUserLoginTask", ex.toString());
                }

                String body = object.toString();

                //Post login information to CertoCloud
                postUtil = new PostUtil();
                return postUtil.postToCertocloud(body, CertocloudConstants.getServerUrl() + CertocloudConstants.REST_POST_SUPPORT, true);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }


        /*
         * parameter responseCode == RETURN_OK if message has been successfully sent
         *
         */
        protected void onPostExecute(Response response) {
            //Log.e("PostMessageService", "responseCode: " + response.getStatus());
            if (postMessageFinishedListener != null) {
                postMessageFinishedListener.onTaskFinished(response.getStatus());
            }
        }


    }


    /*
     *
     *
     * if username or password wrong: return value is HTTP_UNAUTHORISED (401)
     * {
     * 		"status": 401,
     *		"message": "Invalid Username or Password"
     *	}
     *
     *
     */
    public void setOnTaskFinishedListener(PostMessageFinishedListener listener) {
        this.postMessageFinishedListener = listener;

    }


}
