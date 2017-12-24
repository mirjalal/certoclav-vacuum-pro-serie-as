/*
 *  This file is part of Language Picker Widget.
 *
 *  Language Picker Widget is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Language Picker Widget is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Language Picker Widget.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.certoclav.app.settings;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.library.certocloud.PostMessageService;
import com.certoclav.library.certocloud.PostMessageService.PostMessageFinishedListener;
import com.certoclav.library.certocloud.PostUtil;

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
public class SettingsEmailActivity extends CertoclavSuperActivity {
	
	private ArrayAdapter<CharSequence> dataAdapter;
	private SpinnerAdapter spinnerAdapter;
	Button buttonSend;
	Spinner spinnerSubject;
	EditText txtMessage;
	String subject = "";
	TextView txtName;

	
	
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
 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_email_activity);
		CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
		navigationbar.setHeadText(getString(R.string.title_contact_support));

		buttonSend = (Button) findViewById(R.id.buttonSend);
		spinnerSubject =  (Spinner) findViewById(R.id.email_spinner);
		txtMessage = (EditText) findViewById(R.id.editTextMessage);
		txtName = (TextView) findViewById(R.id.email_message_name);


	    
	    
		dataAdapter = ArrayAdapter.createFromResource(SettingsEmailActivity.this, R.array.email_subjects_array,  R.layout.simple_list_item_support);
		dataAdapter.setDropDownViewResource(R.layout.simple_list_item_support);
		spinnerSubject.setAdapter(dataAdapter);   
		spinnerSubject.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					
					subject = dataAdapter.getItem(position).toString();
					Log.e("SettingsEmailActivity", dataAdapter.getItem(position) + " selected");
					
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
				}
			});
		    
	   
	    
		
		if(Autoclave.getInstance().getUser() != null){
			txtName.setText(Autoclave.getInstance().getUser().getFirstName() + " " + Autoclave.getInstance().getUser().getLastName());
		}else{
			txtName.setText("");
		}
		
		buttonSend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String to = Autoclave.getInstance().getUser().getEmail();
				String message = txtMessage.getText().toString();
				if (to != null && to.length() == 0) {
					Toast.makeText(SettingsEmailActivity.this,
							R.string.you_forgot_to_enter_the_email_id,
							Toast.LENGTH_SHORT).show();

				} else if (to != null && to.length() > 0 && !isEmailValid(to)) {
					Toast.makeText(SettingsEmailActivity.this,
							R.string.entered_email_id_is_not_valid, Toast.LENGTH_SHORT)
							.show();
				} else if (subject != null && subject.length() == 0) {
					Toast.makeText(SettingsEmailActivity.this,
							R.string.you_forgot_to_enter_the_subject,
							Toast.LENGTH_SHORT).show();
				} else if (message != null && message.length() == 0) {
					Toast.makeText(SettingsEmailActivity.this,
							R.string.you_forgot_to_enter_the_message,
							Toast.LENGTH_SHORT).show();
				} else if (to != null && subject != null && message != null) {
					//try to send email
					PostMessageService postMessageService = new PostMessageService();
					postMessageService.setOnTaskFinishedListener(new PostMessageFinishedListener() {
						
						@Override
						public void onTaskFinished(int responseCode) {
							try {
								if (responseCode == PostUtil.RETURN_OK) {
									Toast.makeText(SettingsEmailActivity.this, "Message has been sent", Toast.LENGTH_LONG).show();
									finish();
								} else {
									Toast.makeText(SettingsEmailActivity.this, "Message could not be send", Toast.LENGTH_LONG).show();
								}
							}catch (Exception e){
								e.printStackTrace();
							}
						}
					});
					postMessageService.postMessage(subject, subject, message);
					
		

				}
			}
		});
	}

	boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}
    	
    	

    	
}
