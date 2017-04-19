package com.certoclav.library.certocloud;

import org.json.JSONException;
import org.json.JSONObject;

public class Condition {


	public static final String JSON_PROPERTY_IF = "if";
	public static final String JSON_PROPERTY_IF_CODE = "uid";
	public static final String JSON_VALUE_MAINTENANCE = "On maintenance required";
	public static final String JSON_VALUE_CANCELLED = "On program cancelled";
	public static final String JSON_VALUE_ERROR = "On program finished";
	
	public static final String JSON_PROPERTY_ERROR = "error";
	public static final String JSON_PROPERTY_FINISH = "finished";
	public static final String JSON_PROTPERTY_THEN= "then";
	public static final String JSON_PROTPERTY_THEN_PHONE = "phone";
	public static final String JSON_PROTPERTY_THEN_SMS = "sms";
	public static final String JSON_PROTPERTY_THEN_EMAIL = "email";
	public static final String JSON_PROTPERTY_ID = "_id";
	
	public static final int ID_IF_SUCCESSFUL = 1;
	public static final int ID_IF_MAINTENANCE = 3;
	public static final int ID_IF_ERROR = 2;
	
	
	
	public Condition(int ifCode, String title, String email, String phone, String sms) {
		try {
			jsonCondition = new JSONObject();
			jsonCondition.put(JSON_PROPERTY_IF, title);
			jsonCondition.put(JSON_PROPERTY_IF_CODE, ifCode);
			jsonCondition.put(JSON_PROTPERTY_THEN, new JSONObject()
				.put(JSON_PROTPERTY_THEN_EMAIL, email)
				.put(JSON_PROTPERTY_THEN_SMS, sms)
				.put(JSON_PROTPERTY_THEN_PHONE, phone));
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
			
	}	
	public Condition(JSONObject jsonCondition) {
		this.jsonCondition = jsonCondition;
	}

	private JSONObject jsonCondition = null;

	public JSONObject getJsonCondition() {
		return jsonCondition;
	}

	public void setJsonCondition(JSONObject jsonCondition) {
		this.jsonCondition = jsonCondition;
	}
	
	public String getPhoneNumber(){

		String phone = "";
		
		try {
			JSONObject jsonObjectThen = jsonCondition.getJSONObject(JSON_PROTPERTY_THEN);
			phone =  jsonObjectThen.getString(JSON_PROTPERTY_THEN_PHONE);
		} catch (Exception e) {
		}
		
		return phone;
	
	}
	
	public Integer getIfCode(){
		Integer code = 0;
		
		try {
			code = jsonCondition.getInt(JSON_PROPERTY_IF_CODE);
		} catch (Exception e) {
			code = 0;
			e.printStackTrace();
		}
		
		return code;
	}
	
	public String getEmailAddress(){

		String email = "";
		
		try {
			JSONObject jsonObjectThen = jsonCondition.getJSONObject(JSON_PROTPERTY_THEN);
			email =  jsonObjectThen.getString(JSON_PROTPERTY_THEN_EMAIL);
		} catch (Exception e) {
		}
		
		return email;
	
	}
	
	
	public String getSMSNumber(){

		String sms = "";
		
		try {
			JSONObject jsonObjectThen = jsonCondition.getJSONObject(JSON_PROTPERTY_THEN);
			sms =  jsonObjectThen.getString(JSON_PROTPERTY_THEN_SMS);
		} catch (Exception e) {
		}
		
		return sms;
	
	}
	
	
	public void setIfDescription(String description){
		
		try {
			jsonCondition.put(JSON_PROPERTY_IF, description);
		} catch (JSONException e) {
			// TODO Auto-generated catch block	
		}
	}
	
	public String getIfDescription(){
		
		String name = "";
		try {
			name = jsonCondition.getString(JSON_PROPERTY_IF);
		} catch (Exception e) {
		}
		return name;
	}
	
	public void setEmail(String emailAddress) {
		try {
			jsonCondition.getJSONObject(JSON_PROTPERTY_THEN).put(JSON_PROTPERTY_THEN_EMAIL, emailAddress);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void setSMS(String smsNumber) {
		try {
			jsonCondition.getJSONObject(JSON_PROTPERTY_THEN).put(JSON_PROTPERTY_THEN_SMS, smsNumber);
		} catch (JSONException e) {
			// TODO Auto-generated catch block	
		}
	}
	
	public void setPhone(String phoneNumber) {
		try {
			jsonCondition.getJSONObject(JSON_PROTPERTY_THEN).put(JSON_PROTPERTY_THEN_PHONE,phoneNumber);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
		
	}
	}
}