package com.certoclav.app.menu;

import java.util.Date;

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
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.database.UserController;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.PostUtil;

public class RegisterCloudAccountActivity extends Activity {






private LinearLayout linEditTextItemContainer;
private Button buttonRegister;

private EditTextItem editEmailItem;
private EditTextItem editPasswordItem;
private EditTextItem editPasswordItemConfirm;
private EditTextItem editMobile;
private EditTextItem editFirstName;
private EditTextItem editLastName;



	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("LoginActivity", "onCreate");
		setContentView(R.layout.login_register);
		CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
		navigationbar.setHeadText(getString(R.string.register_new_user));
		
	
		
		linEditTextItemContainer = (LinearLayout) findViewById(R.id.register_container_edit_text_items);	

	

		
		editEmailItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
		editEmailItem.setHint(getString(R.string.email));	
		editEmailItem.addTextChangedListner(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().contains("@") && s.toString().contains("."))
					editEmailItem.setHasValidString(true);
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
		linEditTextItemContainer.addView(editEmailItem);
	
		
		
		
		editFirstName = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
		editFirstName.setHint("First name");	
		editFirstName.addTextChangedListner(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().isEmpty() == false)
					editFirstName.setHasValidString(true);
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
		linEditTextItemContainer.addView(editFirstName);
		
		
	
		editLastName = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
		editLastName.setHint("Last name");	
		editLastName.addTextChangedListner(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().isEmpty() == false)
					editLastName.setHasValidString(true);
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
		linEditTextItemContainer.addView(editLastName);
		
		
		editMobile = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
		editMobile.setHint("Mobile number");	
		editMobile.addTextChangedListner(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().isEmpty() == false && (s.toString().startsWith("+") || s.toString().startsWith("00"))){
					editMobile.setHasValidString(true);
				}
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
		linEditTextItemContainer.addView(editMobile);
		
		
		
		
		
		
		editPasswordItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
		editPasswordItem.setHint(getString(R.string.password));	
		editPasswordItem.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		editPasswordItem.addTextChangedListner(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().length()>3)
					editPasswordItem.setHasValidString(true);
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
		linEditTextItemContainer.addView(editPasswordItem);
		
		
		
		
		editPasswordItemConfirm = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
		editPasswordItemConfirm.setHint(getString(R.string.password));	
		editPasswordItemConfirm.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		editPasswordItemConfirm.addTextChangedListner(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(editPasswordItem.getText().equals(s.toString())){
					editPasswordItemConfirm.setHasValidString(true);
				}else{
					editPasswordItemConfirm.setHasValidString(false);
				}
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
		linEditTextItemContainer.addView(editPasswordItemConfirm);
		
		
		
		final DatabaseService databaseService = new DatabaseService(RegisterCloudAccountActivity.this);		
		buttonRegister = (Button) findViewById(R.id.register_button_ok);
		
		buttonRegister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				boolean isEmailAlreadyExists = false;
				for(User user : databaseService.getUsers()){
					if(editEmailItem.getText().equals(user.getEmail())){
						isEmailAlreadyExists = true;
						Toast.makeText(RegisterCloudAccountActivity.this, "Email already exists", Toast.LENGTH_LONG).show();
					}
				}
				
			
					Log.e("RegisterActivity", "onclickRegisterButton");
				if(!editPasswordItem.hasValidString() || !editPasswordItemConfirm.hasValidString()){
					Toast.makeText(RegisterCloudAccountActivity.this, getString(R.string.passwords_do_not_match), Toast.LENGTH_LONG).show();
					return;
				}
				if(!editEmailItem.hasValidString()){
					Toast.makeText(RegisterCloudAccountActivity.this, getString(R.string.please_enter_a_valid_email_address), Toast.LENGTH_LONG).show();
					return;
				}
				if(isEmailAlreadyExists){
					Toast.makeText(RegisterCloudAccountActivity.this, getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
					return;
				}
			
				new AsyncTask<Boolean, Boolean, Boolean>() {

					@Override
					protected Boolean doInBackground(Boolean... params) {

						JSONObject jsonObjectRegister;
						try {
							jsonObjectRegister = new JSONObject();
							jsonObjectRegister.put("username", editEmailItem.getText().toString());
							jsonObjectRegister.put("password", editPasswordItem.getText().toString());
							jsonObjectRegister.put("passwordRepeat", editPasswordItem.getText().toString());
							jsonObjectRegister.put("firstname", editFirstName.getText());
							jsonObjectRegister.put("lastname", editLastName.getText());
							jsonObjectRegister.put("mobile", editMobile.getText());
						} catch (JSONException e) {
							e.printStackTrace();
							return false;
							
						}
												

						PostUtil postUtil = new PostUtil();
						int returnValPost = postUtil.postToCertocloud(jsonObjectRegister.toString(), CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_POST_SIGNUP, false);
						if(postUtil.getResponseBody() != null){
							Log.e("RegisterCloudUser", "response: " + postUtil.getResponseBody());
						}
						if(returnValPost != PostUtil.RETURN_OK){
							return false;
						}
						
						
						User user = new User(
								editFirstName.getText(), 
								"",
								editLastName.getText(), 
								editEmailItem.getText(),
								editMobile.getText(),
								"",
								"",
								"",
								"",
								BCrypt.hashpw(editPasswordItem.getText(), BCrypt.gensalt()),
								new Date(),
								false,
								true);
						
						databaseService.insertUser(user);
						
			
						
						
						
						return true;
					}

					@Override
					protected void onPostExecute(Boolean result) {
						if(result){
							Toast.makeText(getApplicationContext(), R.string.account_created,
			                      Toast.LENGTH_LONG).show();
							finish();
						}else{
							Toast.makeText(getApplicationContext(), "Sign up account failed",
				                      Toast.LENGTH_LONG).show();	
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
