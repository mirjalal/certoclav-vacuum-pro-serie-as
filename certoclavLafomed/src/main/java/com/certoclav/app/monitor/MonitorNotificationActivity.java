package com.certoclav.app.monitor;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.listener.AlertListener;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.Error;

import java.util.ArrayList;

public class MonitorNotificationActivity extends CertoclavSuperActivity implements AlertListener {



	private LinearLayout notificationContainer = null;
	
	private LinearLayout notificationHeadContainer = null;
	private TextView textNotificationHead = null;
	private Button buttonOk = null;
	private Button buttonCancel = null;
	private VideoView videoView = null;
	private String lastErrorMessage = "";


	

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monitor_notification_activity);

		AutoclaveMonitor.getInstance().setOnAlertListener(this);
		
		notificationContainer = (LinearLayout) findViewById(R.id.monitor_notification_container);
		notificationHeadContainer = (LinearLayout) findViewById(R.id.monitor_notification_headtext_background);
		textNotificationHead = (TextView) findViewById(R.id.monitor_notification_headtext);
		buttonOk = (Button) findViewById(R.id.monitor_btn_ok);
		buttonCancel = (Button) findViewById(R.id.monitor_btn_cancel);
		textNotificationHead.setText(R.string.warning);
		videoView = (VideoView) findViewById(R.id.monitor_notification_video);
		
		//user can press cancel button to cancel the prepare to run status
		buttonCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AutoclaveMonitor.getInstance().cancelPrepareToRun();
				AutoclaveMonitor.getInstance().ignoreErrorsTemporary();
				finish();
			}
		});
		
		//user can press the ok button in order to confirm that an error happened
		buttonOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					//AutoclaveMonitor.getInstance().codeEnterded();
					//AutoclaveMonitor.getInstance().cancelPrepareToRun();
					finish();
	}
	
		
});


      
		
	}




	@Override
	public void onResume(){
		super.onResume();
	}


	@Override
	public void onWarnListChange(ArrayList<Error> errorList) {

		
		if(errorList != null){
			if(errorList.size()==0){
					finish();
			}else{	
				if(lastErrorMessage.equals("") || !lastErrorMessage.equals(errorList.get(0).getMsg())){
					
					if(errorList.get(0).getType() == Error.TYPE_WARNING){
						buttonOk.setVisibility(View.GONE);
						buttonCancel.setVisibility(View.VISIBLE);
					}else{
						buttonOk.setVisibility(View.VISIBLE);
						buttonCancel.setVisibility(View.VISIBLE);
					}
					
					
					lastErrorMessage = errorList.get(0).getMsg();
					notificationContainer.removeAllViews();
					View view =  getLayoutInflater().inflate(R.layout.alert_view,null);
					TextView tv = (TextView)view.findViewById(R.id.text_message);
					tv.setText(errorList.get(0).getMsg());
					notificationContainer.addView(view);
				

					try{
					  if(errorList.get(0).getPathVideo() != null){
						  if(errorList.get(0).getPathVideo().isEmpty() == false){
						        videoView.setVideoPath(errorList.get(0).getPathVideo());
						        videoView.setOnPreparedListener(new OnPreparedListener() {
									@Override
									public void onPrepared(MediaPlayer mp) {
										mp.setLooping(true);	
									}
								});
						        videoView.start(); 
						  }
					}
				        
					}catch(Exception e){
						
					}
				}
			
			}
		}
		
	}	
	








}

