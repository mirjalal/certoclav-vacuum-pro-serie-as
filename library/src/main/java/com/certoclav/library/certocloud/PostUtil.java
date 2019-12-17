package com.certoclav.library.certocloud;

import android.util.Log;

import com.certoclav.library.util.Response;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class PostUtil {

    private String responseMessage = "";
    private String responseBody = "";
    private int responseCode = -1;

    public static final int RETURN_OK = 200;
    public static final int RETURN_ERROR_PASSWORD_EXPIRED = 406;
    public static final int RETURN_ERROR_USER_BLOCKED = 402;
    public static final int RETURN_OK_200 = 200;
    public static final int RETURN_ERROR_TIMEOUT = 1;
    public static final int RETURN_ERROR_UNKNOWN_HOST = 2;
    public static final int RETURN_ERROR_UNAUTHORISED_PASSWORD = 401; //equals returned responsecode
    public static final int RETURN_ERROR_UNAUTHORISED_MAIL = 404;//equals returned responsecode
    public static final int RETURN_ERROR_NO_DEVICE = 405;//equals returned responsecode
    public static final int RETURN_ERROR_ACCOUNT_NOT_ACTIVATED = 403;//equals returned responsecode
    public static final int RETURN_ERROR = 4;
    public static final int RETURN_UNKNOWN = 5;


    /*
     * Posts the given JSON body to CertoCloud
     * returns status flags see constants RETURN_XXX
     *
     *
     */
    public Response postToCertocloud(String body, String urlpath, boolean auth) {
        Log.e("PostUtil", "send to Server: " + body);
        Response response = new Response();
        int returnval = RETURN_UNKNOWN;
        String token = "bypass";
        if (auth) {
            token = CloudUser.getInstance().getToken();
            Log.e("POST TOKEN", token);
            if (token.isEmpty()) {
                response.setError(true);
                response.setMessage("Invalid Token or Key");
                return response;
                //return error because there must be a valid token available for auth messages
            }
        }
        try {

            URL url = new URL(urlpath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000); //timeout if wifi is slow
            conn.setReadTimeout(7000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("X-Access-Token", token);
            if (auth == true) {
                conn.setRequestProperty("X-Key", CloudUser.getInstance().getEmail());
                Log.e("EMAIL", CloudUser.getInstance().getEmail());
            }
            conn.setRequestProperty("Content-Type", "application/json");
            Log.e("PostUtil", "before conn.getoutputstream");
            OutputStream os = conn.getOutputStream(); //if host not available this function throws unknownhostexteption
            Log.e("PostUtil", "before os write");
            os.write(body.getBytes("UTF-8"));
            Log.e("PostUtil", "before os close");
            os.close();
            Log.e("PostUtil", "before getResponseCode");


            InputStream in = null;
            int responseCode = -1;
            try {
                responseCode = conn.getResponseCode();
                response.setStatus(responseCode);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    responseCode = conn.getResponseCode();
                    response.setStatus(responseCode);
                } catch (Exception e1) {

                }
            }
            if (conn.getResponseCode() != -1 && conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                in = conn.getInputStream();
            } else {
                /* error from server */
                in = conn.getErrorStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(in)));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                if (!reader.ready()) {
                    break;
                }
            }
            reader.close();
            responseBody = result.toString();
            Gson gson = new Gson();
            try {
                response = gson.fromJson(responseBody, Response.class);
                response.setStatus(responseCode);
            } catch (Exception e) {
                // response is not null
            }

            Log.e("PostUtil", "Response body" + responseBody);

        } catch (Exception e) {
            if (e instanceof UnknownHostException) {
                response.setStatus(RETURN_ERROR_UNKNOWN_HOST);
            } else if (e instanceof SocketTimeoutException) {
                response.setStatus(RETURN_ERROR_TIMEOUT);
            } else {

            }
            e.printStackTrace();
            Log.e("ProstUtil", "Exception " + e.toString());
        }
        return response;

    }


    public Response putToCertocloud(String body, String urlpath, boolean auth) {
        Log.e("PutUtil", "send to Server: " + body);
        Response response = new Response();
        int returnval = RETURN_UNKNOWN;
        String token = "bypass";
        if (auth) {
            token = CloudUser.getInstance().getToken();
            Log.e("POST TOKEN", token);
            if (token.isEmpty()) {
                response.setError(true);
                response.setMessage("Invalid Token or Key");
                return response;
                //return error because there must be a valid token available for auth messages
            }
        }
        try {

            URL url = new URL(urlpath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000); //timeout if wifi is slow
            conn.setReadTimeout(7000);
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("X-Access-Token", token);
            if (auth == true) {
                conn.setRequestProperty("X-Key", CloudUser.getInstance().getEmail());
                Log.e("EMAIL", CloudUser.getInstance().getEmail());
            }
            conn.setRequestProperty("Content-Type", "application/json");
            Log.e("PostUtil", "before conn.getoutputstream");
            OutputStream os = conn.getOutputStream(); //if host not available this function throws unknownhostexteption
            Log.e("PostUtil", "before os write");
            os.write(body.getBytes("UTF-8"));
            Log.e("PostUtil", "before os close");
            os.close();
            Log.e("PostUtil", "before getResponseCode");


            InputStream in = null;
            int responseCode = -1;
            try {
                responseCode = conn.getResponseCode();
                response.setStatus(responseCode);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    responseCode = conn.getResponseCode();
                    response.setStatus(responseCode);
                } catch (Exception e1) {

                }
            }
            if (conn.getResponseCode() != -1 && conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                in = conn.getInputStream();
            } else {
                /* error from server */
                in = conn.getErrorStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(in)));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                if (!reader.ready()) {
                    break;
                }
            }
            reader.close();
            responseBody = result.toString();
            Gson gson = new Gson();
            try {
                response = gson.fromJson(responseBody, Response.class);
                response.setStatus(responseCode);
            } catch (Exception e) {
                // response is not null
            }

            Log.e("PostUtil", "Response body" + responseBody);

        } catch (Exception e) {
            if (e instanceof UnknownHostException) {
                response.setStatus(RETURN_ERROR_UNKNOWN_HOST);
            } else if (e instanceof SocketTimeoutException) {
                response.setStatus(RETURN_ERROR_TIMEOUT);
            } else {

            }
            e.printStackTrace();
            Log.e("ProstUtil", "Exception " + e.toString());
        }
        return response;

    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
