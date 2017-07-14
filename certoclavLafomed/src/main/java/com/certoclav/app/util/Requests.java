package com.certoclav.app.util;

import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.responsemodels.UserInfoResponseModel;
import com.certoclav.app.responsemodels.UserProtocolsResponseModel;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.CloudUser;
import com.google.gson.Gson;

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
        String url = Uri.parse(CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_GET_USER)
                .buildUpon()
                .build().toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("token", token);
        headers.put("username", username);

        sendRequest(url, myCallback, requestId, null, headers, null, UserInfoResponseModel.class, null, Request.Method.GET);
    }

    public void getUserProtocols(MyCallback myCallback, int requestId) {
        String url = Uri.parse(CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_GET_PROTOCOLS)
                .buildUpon()
                .build().toString();
        Map<String, String> headers = new HashMap<>();

        headers.put("X-Access-Token", CloudUser.getInstance().getToken());
        headers.put("X-Key", CloudUser.getInstance().getEmail());
        headers.put("Content-Type", "application/json");


        sendRequest(url, myCallback, requestId, null, headers, null, UserProtocolsResponseModel.class, null, Request.Method.GET);
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
