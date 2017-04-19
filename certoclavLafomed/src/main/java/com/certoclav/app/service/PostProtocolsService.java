package com.certoclav.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PostProtocolsService  extends Service{

	PostProtocolsThread postProtocolsThread = null;
	
	
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;//zum Server kann man sich nicht verbinden
	}
	
	@Override
	public void onCreate(){
		postProtocolsThread = new PostProtocolsThread();
		Log.e("Service","onCreate");
	}
	
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("PostProtocolService", "onstart");
		if(!postProtocolsThread.isAlive()){
			postProtocolsThread = new PostProtocolsThread();
			postProtocolsThread.start();
		}
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy(){
		Log.e("Service","OnDestroy");
		postProtocolsThread.endThread();
	}

}
