package com.certoclav.app.sterilisationassistant;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.Profile;
import com.certoclav.app.listener.AlertListener;
import com.certoclav.app.listener.AutoclaveStateListener;
import com.certoclav.app.listener.ProfileListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.model.Error;
import com.certoclav.app.monitor.MonitorActivity;


public class AssistantActivity extends Activity implements ProfileListener, AlertListener,AutoclaveStateListener  {

	private int currentStep = 1;

	private VideoView videoView;
	private TextView textStepDescription;
	private Button buttonNext;

	CertoclavNavigationbarClean navigationbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sterilisation_assistant_activity);
		







	}
	
	

	@Override
	protected void onResume() {
		currentStep = 1;
		Autoclave.getInstance().setOnProfileListener(this);
		Autoclave.getInstance().setOnAutoclaveStateListener(this);
		AutoclaveMonitor.getInstance().setOnAlertListener(this);
		navigationbar = new CertoclavNavigationbarClean(this);
		if(Autoclave.getInstance().getProfile() != null){
			navigationbar.setHeadText(Autoclave.getInstance().getProfile().getName());
		}
		videoView = (VideoView) findViewById(R.id.sterilisation_assistant_videoview);
		textStepDescription = (TextView) findViewById(R.id.sterilisation_assistant_text_description);
		buttonNext = (Button) findViewById(R.id.sterilisation_assistant_button_next);
		

		

		
		updateUI();
		super.onResume();
	}



	@Override
	protected void onPause() {
		Autoclave.getInstance().removeOnProfileListener(this);
		Autoclave.getInstance().removeOnAutoclaveStateListener(this);
		AutoclaveMonitor.getInstance().removeOnAlertListener(this);
		super.onPause();
	}



	@Override
	public void onProfileChange(Profile profile) {
		
		if(profile != null){
			navigationbar.setHeadText(profile.getName());
		}
		
	}

	@Override
	public void onWarnListChange(ArrayList<Error> errorList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAutoclaveStateChange(AutoclaveState state) {
		if(state == AutoclaveState.RUNNING){
	//		Intent intent = new Intent(AssistantActivity.this,ControlActivity.class );
	//		startActivity(intent);
		}
		
	}

   public void updateUI(){
	   
	  // textStepNumber.setText(Integer.toString(currentStep));
	     
	   switch (currentStep){ 
	   case 1:
		   if(Autoclave.getInstance().getData().isDoorClosed() == false){
			   currentStep++;
			   updateUI();
			   break;
		   }
			Log.e("AssistantActivity", "enter case 1" );
	        videoView.setVideoPath(getString(R.string.path_video_door_open));
	        videoView.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
					
				}
			});
	        videoView.start(); 

			break;
	        
	   case 2:
	
		   		
		   videoView.setVisibility(View.GONE);
           videoView = null;
			videoView = (VideoView) findViewById(R.id.sterilisation_assistant_videoview);
			videoView.setVisibility(View.VISIBLE);
	        videoView.setVideoPath(getString(R.string.path_video_place_item));
	        videoView.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
					
				}
			});
	        
	       
	        videoView.start(); 
		        textStepDescription.setText(R.string.video_place_item_descripton);
		        break;
		        
		        
		    
		        
	   case 3:


			
		   videoView.setVisibility(View.GONE);
           videoView = null;
			videoView = (VideoView) findViewById(R.id.sterilisation_assistant_videoview);
			videoView.setVisibility(View.VISIBLE);
	        videoView.setVideoPath(getString(R.string.path_video_door_close));
	        videoView.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
					
				}
			});
	        
	       
	        videoView.start(); 
	       textStepDescription.setText(R.string.video_lock_description);
	       buttonNext.setText(R.string.start_program);
	       if(AppConstants.IS_CERTOASSISTANT){
	    	   buttonNext.setText("NEXT");
	       }
	       break;
	

	   case 4:


			
		   videoView.setVisibility(View.GONE);

	       textStepDescription.setText("Please choose and start the program"+" " + Autoclave.getInstance().getProfile().getName() + " " + "on the blue front lcd screen");
	       buttonNext.setText(R.string.start_program);
	       if(AppConstants.IS_CERTOASSISTANT){
	    	   buttonNext.setText("NEXT");
	       }
	       break;
	   }
	   
	
	   
   }

public void onClickNextStep(View view){
	
	if(( AppConstants.IS_CERTOASSISTANT == false && currentStep >= 3 )    ||  ( AppConstants.IS_CERTOASSISTANT == true && currentStep >= 4 )){
		currentStep = 1;
		
		Intent intent = new Intent(this, MonitorActivity.class);
		startActivity(intent);
		finish();
	}else{
	
	currentStep = currentStep+ 1;
	Log.e("AssistantActivity", "current step" + currentStep);
	updateUI();
	}
	
}


}
