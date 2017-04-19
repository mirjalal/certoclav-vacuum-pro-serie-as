package com.certoclav.app.menu;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.button.EditTextItem;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.PostUtil;

public class ActivateCloudAccountActivity extends Activity {






private LinearLayout linEditTextItemContainer;
private Button buttonActivateAccount;
private Button buttonResendActivationKey;
private EditTextItem editActivationKeyItem;




	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login_activate_account);
		CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
		navigationbar.setHeadText(getString(R.string.activate)+" " + Autoclave.getInstance().getUser().getEmail());
	
		
		linEditTextItemContainer = (LinearLayout) findViewById(R.id.register_container_edit_text_items);	

	

		
		editActivationKeyItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
		editActivationKeyItem.setHint(getString(R.string.activation_key));	
		editActivationKeyItem.getEditTextView().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		editActivationKeyItem.addTextChangedListner(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().length() == 6)
					editActivationKeyItem.setHasValidString(true);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
				
			}
		});
		linEditTextItemContainer.addView(editActivationKeyItem);
		

	
		


		
		
		
		buttonResendActivationKey = (Button) findViewById(R.id.register_button_resend_mail);
		buttonResendActivationKey.setText("Send activation key to " + Autoclave.getInstance().getUser().getEmail());
		buttonResendActivationKey.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AsyncTask<Boolean, Boolean, Boolean>() {

					@Override
					protected void onPreExecute() {
						buttonResendActivationKey.setEnabled(false);
						super.onPreExecute();
					}

					@Override
					protected Boolean doInBackground(Boolean... params) {

						JSONObject jsonEmail = new JSONObject();
						try {
							jsonEmail.put("username", Autoclave.getInstance().getUser().getEmail());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						PostUtil postUtil = new PostUtil();
						int returnval = postUtil.postToCertocloud(jsonEmail.toString(), CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_SIGNUP_RESEND_KEY, false);
						if(returnval == PostUtil.RETURN_OK){
							return true;
						}
						return false;
					}

					@Override
					protected void onPostExecute(Boolean result) {
						buttonResendActivationKey.setEnabled(true);
						if(result){
							Toast.makeText(ActivateCloudAccountActivity.this, getString(R.string.email_successfully_sent), Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(ActivateCloudAccountActivity.this, getString(R.string.sending_email_failed), Toast.LENGTH_LONG).show();
						}
						super.onPostExecute(result);
					}
					
					
					
				}.execute();
				
			}
		});
		buttonActivateAccount = (Button) findViewById(R.id.register_button_activate);
		buttonActivateAccount.setText("Activate account");
		
		buttonActivateAccount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				



					if(!editActivationKeyItem.hasValidString()){
						Toast.makeText(ActivateCloudAccountActivity.this, "Entered code is not valid", Toast.LENGTH_LONG).show();
						return;
					}
				
		
			
				new AsyncTask<Boolean, Boolean, Boolean>() {

					
					
					@Override
					protected void onPreExecute() {
						buttonActivateAccount.setEnabled(false);
						super.onPreExecute();
					}
					
					@Override
					protected Boolean doInBackground(Boolean... params) {
						PostUtil postUtil = new PostUtil();
						JSONObject jsonActivateObject = new JSONObject();
						
						
						try {
							int code = Integer.parseInt(editActivationKeyItem.getText());
							jsonActivateObject.put("username", Autoclave.getInstance().getUser().getEmail());
							jsonActivateObject.put("resetcode", code );
						} catch (Exception e) {
							e.printStackTrace();
							return false;
							
						}
						int postReturnVal = postUtil.postToCertocloud(jsonActivateObject.toString(), CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_SIGNUP_ACTIVATE, false);
						if(postReturnVal != PostUtil.RETURN_OK){
							return false;
						}
						return true;
					}

					@Override
					protected void onPostExecute(Boolean result) {
						buttonActivateAccount.setEnabled(true);
						if(result){
							Toast.makeText(ActivateCloudAccountActivity.this, "Account successfully activiated", Toast.LENGTH_LONG).show();
							finish();
						}else{
							Toast.makeText(ActivateCloudAccountActivity.this, "Account activation failed", Toast.LENGTH_LONG).show();
						}
						super.onPostExecute(result);
					}
					
					
					
				}.execute();
				
	
		
				
			}
		});
		

		
		
		
		


	  	 
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

	    View v = getCurrentFocus();
	    boolean ret = super.dispatchTouchEvent(event);

	    if (v instanceof EditText) {
	        View w = getCurrentFocus();
	        int scrcoords[] = new int[2];
	        w.getLocationOnScreen(scrcoords);
	        float x = event.getRawX() + w.getLeft() - scrcoords[0];
	        float y = event.getRawY() + w.getTop() - scrcoords[1];

	        Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
	        if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) { 

	            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
	        }
	    }
	return ret;
	}
	
}
