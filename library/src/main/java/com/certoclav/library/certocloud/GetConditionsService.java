package com.certoclav.library.certocloud;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class GetConditionsService  extends Service{

	GetConditionsThread getConditionsThread = null;
	

	public Handler mGuiHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			
		};
	}; 
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;//zum Server kann man sich nicht verbinden
	}
	
	@Override
	public void onCreate(){
		getConditionsThread = new GetConditionsThread(mGuiHandler);
		Log.e("Service","onCreate");
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("PostProtocolService", "onstart");
		if(!getConditionsThread.isAlive()){
			getConditionsThread = new GetConditionsThread(mGuiHandler);
			getConditionsThread.start();
		}
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy(){
		Log.e("Service","OnDestroy");
		getConditionsThread.endThread();
	}

}
