package com.certoclav.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;

public class PlugInControlReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();

	    if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
	        // Do something when power connected
	    }
	    else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
	        // Do something when power disconnected
	    	if(AutoclaveMonitor.PowerOffDeviceAutomatically == true){
	    		Log.e("AutoclaveMonitor", "Power off device");
	    		//if(AutoclaveMonitor.SimulatedLockSwitch == true){
	    		if(Autoclave.getInstance().getState() == AutoclaveState.RUNNING){
	    		//	AutoclaveMonitor.getInstance().cancelProgram(99);
	    		}
	    	
	    	
	    	
	     	//shut down device
	    	/*
	    		try {
	    			Process proc = Runtime.getRuntime().exec(new String[]{ "su", "-c", "reboot -p" });
	    			proc.waitFor();
	    		} catch (Exception ex) {
	    			ex.printStackTrace();
	    		}
			*/	
	    	}
	    
	    	//}
	    
	    }	
	
	}

}
