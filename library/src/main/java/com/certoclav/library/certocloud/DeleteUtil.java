package com.certoclav.library.certocloud;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

public class DeleteUtil {

	private String responseMessage = "";
	private String responseBody = "";
	
	public static final int RETURN_OK = 0;
	public static final int RETURN_ERROR_TIMEOUT = 1;
	public static final int RETURN_ERROR_UNKNOWN_HOST = 2;
	public static final int RETURN_ERROR_UNAUTHORISED = 3;
	public static final int RETURN_ERROR = 4;
	public static final int RETURN_UNKNOWN = 5;
	




	/*
	 * Posts the given JSON body to CertoCloud
	 * returns status flags see constants RETURN_XXX
	 * 
	 * 
	 */
	public int deleteToCertocloud(String urlpath, boolean auth){

	     int returnval = RETURN_UNKNOWN;
	     	if(auth == true){
	     		if(CloudUser.getInstance().getToken().isEmpty()){
	     			return -1;
	     			//return error because there must be a valid token available for auth messages
	     		}
	     	}
	        try{
				URL url = new URL(urlpath);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(2000); //timeout if wifi is slow
				conn.setReadTimeout(7000);
				conn.setRequestMethod("DELETE");
				conn.setDoOutput(false); //on delete request, set this to false
				if(auth == true){
					conn.setRequestProperty("X-Access-Token", CloudUser.getInstance().getToken());
					conn.setRequestProperty("X-Key", CloudUser.getInstance().getEmail());
				}
				conn.setRequestProperty("Content-Type", "application/json");

				// read the response
				int responseCode = -1;
				try{
					responseCode = conn.getResponseCode();
					if(responseCode == HttpsURLConnection.HTTP_OK){
						returnval = RETURN_OK;
					} 
				}catch(Exception e){
					returnval = RETURN_ERROR_UNAUTHORISED;
					//this workaround is neccessary, because server doesn't reply with WWW-Authenticate in header in case of Responsecode 401
				}

				responseMessage = conn.getResponseMessage();

				
			
				
				InputStream in = new BufferedInputStream(conn.getInputStream());
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				StringBuilder result = new StringBuilder();
				String line;
				while((line = reader.readLine()) != null) {
				    result.append(line);
				}
				responseBody = result.toString();
				

				Log.e("DeleteProtocolTask", "Response body" + responseBody);
 
	        }catch(Exception e){
	        	if(e instanceof UnknownHostException){
	        		returnval = RETURN_ERROR_UNKNOWN_HOST;
	        	}else if(e instanceof SocketTimeoutException){
	        		returnval = RETURN_ERROR_TIMEOUT;
	        	}
	        	Log.e("DELETEUtil", "IO Exception " + e.toString());
	        }
			return returnval;

	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public String getResponseBody() {
		return responseBody;
	}
}
