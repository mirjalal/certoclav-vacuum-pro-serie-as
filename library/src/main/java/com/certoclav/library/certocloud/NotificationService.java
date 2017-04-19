package com.certoclav.library.certocloud;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class NotificationService {

	
	public void executePostSmsTask(final String phoneNumber, final String message){
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try{
					for(int i = 0; i< 5;i++){
						if(sendSms(phoneNumber, message)){
							break;
						}
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
		
		
	}
	
	
	public void executePostEmailTask(final String emailAddressReceiver, final String nameOfReceiver, final String subjectOfMail, final String contentOfMail){
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				for(int i = 0; i< 5;i++){
					if(sendEmail(emailAddressReceiver, nameOfReceiver, subjectOfMail, contentOfMail)){
						break;
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
				}
				return null;
			}
		}.execute();
		
		
	}
	
	
	public boolean sendSms(String phoneNumber, String message) {
	
		PostUtil postUtil = new PostUtil();
		
		JSONObject body = new JSONObject();
		try {
			body.put("phone", phoneNumber);
			body.put("body", message);
		} catch (Exception e) {

		}
		
		int returnVal = postUtil.postToCertocloud(body.toString(), CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_SMS, true);
		if(returnVal == PostUtil.RETURN_OK){
				return true;
		}
		return false;
	}
	

	
	public boolean sendEmail(String emailAddressReceiver, String nameOfReceiver, String subjectOfMail, String contentOfMail) {
		
		PostUtil postUtil = new PostUtil();
		
		JSONObject body = new JSONObject();
		try {
			body.put("to", emailAddressReceiver); //to email
			body.put("username", nameOfReceiver);//name of receiver
			body.put("subject", subjectOfMail); //subject of mail
			body.put("title", subjectOfMail); //title of mail
			body.put("description", contentOfMail); //content of mail
		} catch (Exception e) {

		}
		
		int returnVal = postUtil.postToCertocloud(body.toString(), CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_EMAIL, true);
		if(returnVal == PostUtil.RETURN_OK){
			Log.e("NotificationService", "Email has been sent successfully");	
			return true;
				
		}
		Log.e("NotificationService", "Sending email failed");
		return false;
	}
	
	
}
