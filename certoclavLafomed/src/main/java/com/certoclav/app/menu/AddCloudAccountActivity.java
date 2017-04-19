package com.certoclav.app.menu;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.button.EditTextItem;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.database.UserController;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.PostUserLoginService;
import com.certoclav.library.certocloud.PostUserLoginService.PutUserLoginTaskFinishedListener;
import com.certoclav.library.certocloud.PostUtil;

public class AddCloudAccountActivity extends Activity {






private LinearLayout linEditTextItemContainer;
private Button buttonRegister;

private EditTextItem editEmailItem;
private EditTextItem editPasswordItem;
private 	ProgressBar progressBar;



	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("LoginActivity", "onCreate");
		setContentView(R.layout.login_register);
		CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
		navigationbar.setHeadText(getString(R.string.add_existing_certocloud_account));
		progressBar = (ProgressBar)findViewById(R.id.register_progressbar);
	
		
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
		
		
		
		
		

		
		
		final DatabaseService databaseService = new DatabaseService(AddCloudAccountActivity.this);		
		buttonRegister = (Button) findViewById(R.id.register_button_ok);
		buttonRegister.setText(getString(R.string.add_account));
		
		buttonRegister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				boolean isEmailAlreadyExists = false;
				for(User user : databaseService.getUsers()){
					if(editEmailItem.getText().equals(user.getEmail())){
						isEmailAlreadyExists = true;
						Toast.makeText(AddCloudAccountActivity.this, getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
					}
				}
				
			


					if(!editEmailItem.hasValidString()){
						Toast.makeText(AddCloudAccountActivity.this, getString(R.string.please_enter_a_valid_email_address), Toast.LENGTH_LONG).show();
						return;
					}
				
				if(isEmailAlreadyExists){
					Toast.makeText(AddCloudAccountActivity.this, getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
					return;
				}
			
				PostUserLoginService userLoginSerice = new PostUserLoginService();
				userLoginSerice.setOnTaskFinishedListener(new PutUserLoginTaskFinishedListener() {
					
					@Override
					public void onTaskFinished(int responseCode) {
						progressBar.setVisibility(View.GONE);
						if(responseCode == PostUtil.RETURN_OK){
							Boolean isLocal = false;
								
									User user = new User(
											"", 
											"",
											"", 
											editEmailItem.getText(),
											"",
											"",
											"",
											"",
											"",
											BCrypt.hashpw(editPasswordItem.getText(), BCrypt.gensalt()),
											new Date(),
											false,
											isLocal);
									
									databaseService.insertUser(user);
									
								
									
									Toast.makeText(getApplicationContext(), getString(R.string.account_added),
						                      Toast.LENGTH_LONG).show();
									finish();
						}else{
							Toast.makeText(getApplicationContext(), getString(R.string.linking_certocloud_account_failed),
				                      Toast.LENGTH_LONG).show();
						}
						
					}
				});
				
				userLoginSerice.loginUser(editEmailItem.getText(), editPasswordItem.getText(),Autoclave.getInstance().getController().getSavetyKey());
			
				progressBar.setVisibility(View.VISIBLE);
	
		
				
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
