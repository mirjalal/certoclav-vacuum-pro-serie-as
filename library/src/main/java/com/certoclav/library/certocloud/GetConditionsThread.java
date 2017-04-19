package com.certoclav.library.certocloud;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;





public class GetConditionsThread extends Thread {

	private Handler mParentHandler;
	
	public GetConditionsThread(Handler parentHandler) {
		mParentHandler =parentHandler;
	}
	



	@Override
	public void run() {
	

				try {
				
					if(CloudUser.getInstance().isLoggedIn() ==false){
						return;//continue; //sleep for another 60 seconds
					}
					
					
					GetUtil getUtil = new GetUtil();
					Integer success = getUtil.getFromCertocloud(CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_GET_CONDITIONS + CloudUser.getInstance().getCurrentDeviceKey());
					
					if(success == GetUtil.RETURN_OK){
						

						JSONObject jsonObjectResult = new JSONObject(getUtil.getResponseBody());
						CloudUser.getInstance().setPremiumAccount(jsonObjectResult.getBoolean("ispremium"));
						boolean conditionsAlreadyCreatedInCloud = false;
						try{
							jsonObjectResult.getJSONObject("condition").getString("_id");
							conditionsAlreadyCreatedInCloud = true;
						}catch(Exception e){
							Log.e("GetConditionThread", "Need to create conditions in cloud");
									conditionsAlreadyCreatedInCloud = false;	
			
											PostUtil postUtil = new PostUtil();
											
										
											JSONObject body = new JSONObject();
											try {
												JSONArray jsonArrayCondition = new JSONArray();
												for(int i = 0; i< CloudDatabase.getInstance().getConditionList().size();i++){
													jsonArrayCondition.put(CloudDatabase.getInstance().getConditionList().get(i).getJsonCondition());
												}
												JSONObject jsonObjectCondition = new JSONObject();
												jsonObjectCondition.put("devicekey", CloudUser.getInstance().getCurrentDeviceKey());
												jsonObjectCondition.put("conditions", jsonArrayCondition);
												body.put("condition", jsonObjectCondition);
											} catch (Exception e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
											postUtil.postToCertocloud(body.toString(), CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_CONDITIONS_CREATE, true);

						}
						
						if(conditionsAlreadyCreatedInCloud){
						
							JSONArray jsonConditionArray = jsonObjectResult.getJSONObject("condition").getJSONArray("conditions");
								
					       //only overwrite content of already existing conditions
							for(int i = 0; i< jsonConditionArray.length();i++){
									
									JSONObject jsonCondition = jsonConditionArray.getJSONObject(i);
									Condition conditionFromCloud = new Condition(jsonCondition);
									Log.e("GetConditionThread", "received condition: " + "id: " + conditionFromCloud.getIfCode() + " mail: "+ conditionFromCloud.getEmailAddress()+ " sms:" + conditionFromCloud.getSMSNumber());
									for(Condition conditionLocal : CloudDatabase.getInstance().getConditionList()){
										if(conditionFromCloud.getIfCode() != 0 && conditionFromCloud.getIfCode() == conditionLocal.getIfCode()){
											conditionLocal.setEmail(conditionFromCloud.getEmailAddress());
											conditionLocal.setSMS(conditionFromCloud.getSMSNumber());
											conditionLocal.setPhone(conditionFromCloud.getPhoneNumber());
										}
									}
								}
						}
					
					
					}
					
					
					
					
					
					
					
					
					
				
			sendMessage();
				}catch (Exception e) {
				Log.e("GetConditionThread", "Error: "+ e.toString());
			}
	}


	private void sendMessage() {

		
		if (mParentHandler != null) {
			mParentHandler.sendEmptyMessage(0);
		}
	}
	
	public void endThread(){
		//no runFlag exists here because of no loop usage
	}




}