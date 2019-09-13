package com.certoclav.app.util;

import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.certoclav.app.license.LicenseCountModel;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.responsemodels.DeviceProgramsResponseModel;
import com.certoclav.app.responsemodels.ResponseModel;
import com.certoclav.app.responsemodels.UserInfoResponseModel;
import com.certoclav.app.responsemodels.UserProtocolResponseModel;
import com.certoclav.app.responsemodels.UserProtocolsResponseModel;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.CloudUser;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by musaq on 2/13/2017.
 */

public class Requests {

    private static final String TAG = "requests";
    private static Gson gson;
    private static Requests requests;

    private Requests() {

    }

    public static Requests getInstance() {
        if (requests == null) {
            requests = new Requests();
            gson = new Gson();
        }
        return requests;
    }


    public void getUserInfo(String username, String token, MyCallback myCallback, int requestId) {
        String url = Uri.parse(CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_GET_USER)
                .buildUpon()
                .build().toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("token", token);
        headers.put("username", username);

        sendRequest(url, myCallback, requestId, null, headers, null, UserInfoResponseModel.class, null, Request.Method.GET);
    }


    public void getLicenseCount(MyCallback myCallback, int requestId) {
        String url = Uri.parse(CertocloudConstants.SERVER_CERTOCLOUD_URL + CertocloudConstants.REST_API_GET_LICENSE_COUNT)
                .buildUpon()
                .build().toString();

        sendRequest(url, myCallback, requestId, null, null, null, LicenseCountModel.class, null, Request.Method.POST);
    }

    public void activateAutoclave(MyCallback myCallback, int requestId) {
        String url = Uri.parse(CertocloudConstants.SERVER_CERTOCLOUD_URL + CertocloudConstants.REST_API_GET_LICENSE_COUNT)
                .buildUpon()
                .build().toString();

        sendRequest(url, myCallback, requestId, null, null, null, LicenseCountModel.class, null, Request.Method.POST);
    }

    public void enableDeviceFDA(MyCallback myCallback, boolean isEnabled, int requestId) {
        String url = Uri.parse(CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_ENABLE_FDA)
                .buildUpon()
                .build().toString();
        Map<String, String> headers = new HashMap<>();

        JSONObject body = new JSONObject();
        try {
            body.put("isEnabledFDA", isEnabled + "");
            body.put("devicekey", Autoclave.getInstance().getController().getSavetyKey());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        headers.put("Content-Type", "application/json");

        sendRequest(url, myCallback, requestId, null, headers, body.toString(), LicenseCountModel.class, null, Request.Method.POST);
    }

    public void enableDeviceFDAAccess(MyCallback myCallback, float hours, int requestId) {
        String url = Uri.parse(CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_ENABLE_FDA_PERMISSION)
                .buildUpon()
                .build().toString();
        Map<String, String> headers = new HashMap<>();

        JSONObject body = new JSONObject();
        try {
            body.put("allowHours", hours);
            body.put("devicekey", Autoclave.getInstance().getController().getSavetyKey());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        headers.put("X-Access-Token", CloudUser.getInstance().getToken());
        headers.put("X-Key", CloudUser.getInstance().getEmail());
        headers.put("Content-Type", "application/json");

        sendRequest(url, myCallback, requestId, null, headers, body.toString(), LicenseCountModel.class, null, Request.Method.POST);
    }


    public void getUserProtocols(MyCallback myCallback, int requestId) {
        String url = Uri.parse(CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_GET_PROTOCOLS + Autoclave.getInstance().getController().getSavetyKey())
                .buildUpon()
                .build().toString();
        Map<String, String> headers = new HashMap<>();

        headers.put("X-Access-Token", CloudUser.getInstance().getToken());
        headers.put("X-Key", CloudUser.getInstance().getEmail());
        headers.put("Content-Type", "application/json");

        DefaultRetryPolicy policy = new DefaultRetryPolicy(
                200000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        sendRequest(url, myCallback, requestId, null, headers, null, UserProtocolsResponseModel.class, policy, Request.Method.GET);
    }


    public void updateUserPassword(MyCallback myCallback, String passwordCurrent, String passwordNew, int requestId) {
        String url = Uri.parse(CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_POST_USER_PASSWORD)
                .buildUpon()
                .build().toString();
        Map<String, String> headers = new HashMap<>();

        headers.put("X-Access-Token", CloudUser.getInstance().getToken());
        headers.put("X-Key", CloudUser.getInstance().getEmail());
        headers.put("Content-Type", "application/json");

        JSONObject body = new JSONObject();
        try {
            JSONObject passwords = new JSONObject();
            passwords.put("currPwd", passwordCurrent);
            passwords.put("newPwd", passwordNew);
            body.put("pwdSet", passwords);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DefaultRetryPolicy policy = new DefaultRetryPolicy(
                200000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        sendRequest(url, myCallback, requestId, null, headers, body.toString(), ResponseModel.class, policy, Request.Method.PUT);
    }

    public void getCloudPrograms(MyCallback myCallback, int requestId) {
        String url = Uri.parse(CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_GET_DEVICE_PROFILES + Autoclave.getInstance().getController().getSavetyKey())
                .buildUpon()
                .build().toString();
        Map<String, String> headers = new HashMap<>();

        headers.put("X-Access-Token", CloudUser.getInstance().getToken());
        headers.put("X-Key", CloudUser.getInstance().getEmail());
        headers.put("Content-Type", "application/json");

        DefaultRetryPolicy policy = new DefaultRetryPolicy(
                200000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        sendRequest(url, myCallback, requestId, null, headers, null, DeviceProgramsResponseModel.class, policy, Request.Method.GET);
    }


    public void getUserProtocol(MyCallback myCallback, String protocolId, int requestId) {
        String url = Uri.parse(CertocloudConstants.getServerUrl() + CertocloudConstants.REST_API_GET_PROTOCOL + protocolId)
                .buildUpon()
                .build().toString();
        Map<String, String> headers = new HashMap<>();

        headers.put("X-Access-Token", CloudUser.getInstance().getToken());
        headers.put("X-Key", CloudUser.getInstance().getEmail());
        headers.put("Content-Type", "application/json");

        DefaultRetryPolicy policy = new DefaultRetryPolicy(
                200000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        sendRequest(url, myCallback, requestId, null, headers, null, UserProtocolResponseModel.class, policy, Request.Method.GET);
    }

    private void sendRequest(String url, final MyCallback myCallback, final int requestId, final Map<String, String> params, final Map<String, String> headers, final String body, final Class type, RetryPolicy policy, int requestMethod) {
        String tag_json_obj = "json_obj_req";
        if (myCallback != null)
            myCallback.onStart(requestId);
        Log.e("url", url.toString());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(requestMethod,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (myCallback != null)
                            myCallback.onSuccess(gson.fromJson(response.toString(), type), requestId);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                ErrorModel errorModel = new ErrorModel();
                String body = null;
                //get status code here
                //get response body and parse with appropriate encoding
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    errorModel.setStatusCode(error.networkResponse.statusCode);
                    try {
                        if (error.networkResponse.statusCode == 404) {
                            errorModel.setMessage("Something went wrong.");
                        } else
                            body = new String(error.networkResponse.data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                if (body != null && body.length() > 400) body = "Something went wrong.";
                errorModel.setMessage(body != null ? body : error.getMessage() != null ? error.getMessage().toString() : "No Internet");
                if (myCallback != null)
                    myCallback.onError(errorModel, requestId);
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                return params != null ? params : new HashMap<String, String>();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (headers != null)
                    return headers;
                return super.getHeaders();
            }

            @Override
            public byte[] getBody() {
                if (body != null)
                    return body.getBytes();
                return super.getBody();
            }
        };
        if (policy != null)
            jsonObjReq.setRetryPolicy(policy);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }

}
