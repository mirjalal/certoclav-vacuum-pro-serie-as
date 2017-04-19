package com.certoclav.library.certocloud;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;




public class PostUserSignupService {

	
	public interface PostUserSignupTaskFinishedListener{
		void onTaskFinished(Integer result);
	}

	private PostUserSignupTaskFinishedListener putUserTaskFinishedListener = null;

	private String email;
	private String password;
	private String mobile;
	private String firstname;
	private String lastname;
	
	private PostUtil postUtil;


	// Get profiles from the server
	public void signUpUser(String email, String password, String mobile, String firstname, String lastname) {
		this.email = email;
		this.password = password;
		this.mobile = mobile;
		this.firstname = firstname;
		this.lastname = lastname;
		new PutUserSignupTask().execute(CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_SIGNUP);
	}
	
	
	
	
	
	private class PutUserSignupTask extends AsyncTask<String , String, Integer> {

			protected Integer doInBackground(String... pw) {
				try {
			        JSONObject object = new JSONObject();
		

			            object.put("username", email);
			            object.put("password", password);
			            object.put("passwordRepeat", password);
			            object.put("mobile", mobile);
			            object.put("firstname", firstname);
			            object.put("lastname", lastname);
			    
			        String body = object.toString();
			        

			        postUtil = new PostUtil();
			        return postUtil.postToCertocloud(body, CertocloudConstants.SERVER_URL+ CertocloudConstants.REST_API_POST_SIGNUP, false);
			    } catch (Exception ex) {
		        	Log.e("PostUserSignupService Exception", ex.toString());
		        }
				return PostUtil.RETURN_ERROR;

			}

			
			
			protected void onPostExecute(Integer result) {	
			
				if(putUserTaskFinishedListener != null){
					putUserTaskFinishedListener.onTaskFinished(result);
				}
			}
			
			
			
	}
			

		public void setOnTaskFinishedListener(PostUserSignupTaskFinishedListener listener) {
			this.putUserTaskFinishedListener = listener;
			
		}



		
		
		
		
}
