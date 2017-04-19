package com.certoclav.app.settings;




import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.listener.WifiListener;
import com.certoclav.app.model.Autoclave;


public class SettingsNetworkFragment extends PreferenceFragment implements WifiListener, OnSharedPreferenceChangeListener {
	

	private  	SharedPreferences prefs;
	public SettingsNetworkFragment() {
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_network);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
       
        

    };

   


    

    

    
    



	private void updateWifiUI() {
		
	
	       WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		

				
	       //update Text and Summary
	       WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
	       String wifiname = wifiInfo.getSSID();
	       
	       Log.e("PrefsFragment", "wifiname: " + wifiname);
	       
	       CheckBoxPreference customPref = (CheckBoxPreference) findPreference(AppConstants.PREFERENCE_KEY_WIFI_ENABLED);
	       if(customPref.isChecked()){
	    	   if(wifiname == null){
	    		   customPref.setSummary(R.string.wifi_not_connected);
	    	   }else{
	    		   if(wifiname.equals("<unknown ssid>") || wifiname.equals("0x")){
	    			   customPref.setSummary(R.string.trying_to_connect_click_to_choose_network);
	    		   }else{
	    			   customPref.setSummary(getActivity().getString(R.string.connected_to)+": " + wifiname);
	    		   }
	    	   }
	       }else{
	    	   customPref.setSummary(R.string.wifi_disabled);
	       }
		
	}
	
	

	
	
	
	/*
	 * 
	 * Starts an Activity for Picking and Connecting to Wifi
	 */
	private void startWifiPicker() {
		startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
	}
	


	

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		if(preference.getKey().equals(AppConstants.PREFERENCE_KEY_WIFI_MANAGER)){
			startWifiPicker();
		}

		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onResume() {
		

		
		
				
		
		//register Listeners
		 prefs.registerOnSharedPreferenceChangeListener(this);
	     Autoclave.getInstance().setOnWifiListener(this);
        
	     //set wifi switch position according to wifimanager
         //udateWifiSwitchPosition
	       WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
	       
		if(wifiMgr != null)	{
				//set initial position of Wifi-Switch
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				Editor editor = prefs.edit();
				editor.putBoolean(AppConstants.PREFERENCE_KEY_WIFI_ENABLED, wifiMgr.isWifiEnabled());
				editor.commit();
		}
		

	     
	     //update UI (Text)
		 updateWifiUI();

		super.onResume();
	}


	@Override
	public void onPause() {
		//unregister listeners

	    prefs.unregisterOnSharedPreferenceChangeListener(this);
		Autoclave.getInstance().removeOnWifiListener(this);
		super.onPause();
	}



	
	
	public static class WifiReceiver extends BroadcastReceiver {

	    @Override
	    public void onReceive(Context context, Intent intent) {     
	        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
	        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
	        
	        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) 
	
	        Autoclave.getInstance().setWifiConnected(true);
	        
	        
	        else
	            Autoclave.getInstance().setWifiConnected(false);    
	    }   
	}


	@Override
	public void onWifiConnectionChange(Boolean wifiConnected) {
		updateWifiUI();
		
	}

	
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
    	if(key.equals(AppConstants.PREFERENCE_KEY_WIFI_ENABLED)){
			WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
			
			if(wifiManager !=null){
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				wifiManager.setWifiEnabled(sharedPrefs.getBoolean(AppConstants.PREFERENCE_KEY_WIFI_ENABLED, false));
			}
    	}



updateWifiUI();
		
	};
    
	

	



@Override
public void onDestroy() {
	
	super.onDestroy();
}



}