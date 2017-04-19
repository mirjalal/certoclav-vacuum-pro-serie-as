package com.certoclav.library.certocloud;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;



public class CloudDatabase {
	
	public CloudDatabase(){
		//default conditions for certoclav connect - should be moved to application code (not library)
		conditionList = new ArrayList<Condition>();
		conditionList.add(new Condition(Condition.ID_IF_ERROR,"If an error occured, send notification",CloudUser.getInstance().getEmail(),"",""));
		conditionList.add(new Condition(Condition.ID_IF_MAINTENANCE,"If maintenance required, send notification",CloudUser.getInstance().getEmail(),"",""));
		conditionList.add(new Condition(Condition.ID_IF_SUCCESSFUL,"If program finished successfully, send notification",CloudUser.getInstance().getEmail(),"",""));
	
	}
	
	
	static CloudDatabase instance = null;
	
	static public CloudDatabase getInstance(){
		if(instance == null){
			instance = new CloudDatabase();
		}
		return instance;
		
	}


	public ArrayList<Condition> getConditionList() {
		return conditionList;
	}


	public void setConditionList(ArrayList<Condition> conditionList) {
		this.conditionList = conditionList;
	}


	private ArrayList<Condition> conditionList = new ArrayList<Condition>();

	
	public boolean updateConditions(ArrayList<Condition> conditionList){
	
		this.conditionList = conditionList;
		
		PostUtil postUtil = new PostUtil();
		JSONObject body = new JSONObject();
		try{
			//jsonObjectCondition.put("userid", CloudUser.getInstance().ge)
			
			JSONArray jsonArrayCondition = new JSONArray();
			for(Condition condition : conditionList){
				jsonArrayCondition.put(condition.getJsonCondition());
			}
			JSONObject jsonObjectCondition = new JSONObject();
			jsonObjectCondition.put("devicekey", CloudUser.getInstance().getCurrentDeviceKey());
			jsonObjectCondition.put("conditions", jsonArrayCondition);
			body.put("condition", jsonObjectCondition);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int retval = postUtil.postToCertocloud(body.toString(), CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_CONDITIONS_UPDATE, true);
		if(retval == PostUtil.RETURN_OK){
			return true;
		}
		return false;
	}
	

}
