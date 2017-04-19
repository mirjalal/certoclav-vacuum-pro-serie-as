package com.certoclav.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CloudSocketService  extends Service{

	CloudSocketThread cloudSocketThread = null;
		
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;//zum Server kann man sich nicht verbinden
	}
	
	@Override
	public void onCreate(){
		cloudSocketThread = new CloudSocketThread();
		Log.e("Service","onCreate");
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if(!cloudSocketThread.isAlive()){
			cloudSocketThread = new CloudSocketThread();
			cloudSocketThread.start();
		}
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy(){
		Log.e("Service","OnDestroy");
		cloudSocketThread.endThread();
	}

}
