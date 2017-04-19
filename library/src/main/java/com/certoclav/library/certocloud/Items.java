package com.certoclav.library.certocloud;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Items {



	public static final String JSON_PROTPERTY_ID = "_id";
	
	public static final int ID_IF_SUCCESSFUL = 1;
	public static final int ID_IF_MAINTENANCE = 3;
	public static final int ID_IF_ERROR = 2;
	public static final int ERROR_UNAUTHORIZED = 4;
	public static final int ERROR_UNKNOWN = 5;
	private ArrayList<String> itemJsonStringArray = new ArrayList<String>();
	
	public Items() {
			
	}	
	

	public int getItemsFromCloud(){
		
		Integer success = 0;
		try {
			
			if(CloudUser.getInstance().isLoggedIn() ==false){
				return ERROR_UNAUTHORIZED;
			}
			
			
			GetUtil getUtil = new GetUtil();
			success = getUtil.getFromCertocloud(CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_GET_IEMS + CloudUser.getInstance().getCurrentDeviceKey());
			
			if(success == GetUtil.RETURN_OK){
				

				JSONObject jsonObjectResult = new JSONObject(getUtil.getResponseBody());
				CloudUser.getInstance().setPremiumAccount(jsonObjectResult.getBoolean("ispremium"));

				
				JSONArray jsonItemArray = jsonObjectResult.getJSONArray("items");
						
		       //only overwrite content of already existing conditions
				for(int i = 0; i< jsonItemArray.length();i++){
					itemJsonStringArray.add(jsonItemArray.getJSONObject(i).toString());
				}
			}

		}catch (Exception e) {
		return ERROR_UNKNOWN;
	}
		return success;
		
	}
	
	

}