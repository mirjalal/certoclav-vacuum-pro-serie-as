package com.certoclav.library.certocloud;

import android.os.AsyncTask;
import android.util.Log;
import com.certoclav.library.util.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class PostSoftwareUpdateService {

    private String deviceKey;
    private String softwareVersion;
    private PostUtil postUtil;

    public void postDeviceData(String deviceKey, String softwareVersion) {
        this.softwareVersion = softwareVersion;
        this.deviceKey = deviceKey;
        new PostDeviceDataTask().execute();
    }

    private class PostDeviceDataTask extends AsyncTask<String, String, Response> {

        protected Response doInBackground(String... pw) {
            try {
                JSONObject object = new JSONObject();
                try {
                    object.put("devicekey", deviceKey);
                    object.put("softwareVersion", softwareVersion);

                } catch (Exception ex) {
                    Log.e("PostDeviceDataTask", ex.toString());
                }
                String body = object.toString();
                //Post information to CertoCloud
                postUtil = new PostUtil();
                return postUtil.postToCertocloud(body, CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_POST_DEVICE_SOFTWARE, true);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Response response) {
            if (response != null && response.isOK()) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(postUtil.getResponseBody());
                    Log.e("SOFTWARE DATA POSTED", jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}