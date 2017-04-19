package com.certoclav.app.settings;




import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.util.DownloadUtils;
import com.certoclav.library.util.ExportUtils;
import com.certoclav.library.util.UpdateUtils;


public class SettingsDeviceFragment extends PreferenceFragment implements SensorDataListener  {
	


	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_device);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());



//Device Key
        String deviceKey = "-";
        if(Autoclave.getInstance().getController() !=null){
	        if(Autoclave.getInstance().getController().getSavetyKey()!= null){
	           deviceKey = Autoclave.getInstance().getController().getSavetyKey();	
	        }
        }
        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_DEVICE_KEY)).setSummary(deviceKey); 

//Check for updates
       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_SOFTWARE_UPDATE)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {

			if (ApplicationController.getInstance().isNetworkAvailable()) {
				List<String> downloadUrls = new ArrayList<String>(); 
				downloadUrls.add(AppConstants.DOWNLOAD_LINK);
				DownloadUtils downloadUtils= new DownloadUtils(getActivity());
				downloadUtils.Download(downloadUrls);
			}else{
				Toast.makeText(getActivity(), "Please connect to internet", Toast.LENGTH_LONG).show();
			}
			
			return false;
		}
	});   
       
       //Install update from USB
       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_SOFTWARE_UPDATE_USB)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			preference.setEnabled(false);
			ExportUtils exportUtils = new ExportUtils();
			if(exportUtils.checkExternalMedia() == false){
				Toast.makeText(getActivity(), getActivity().getString(R.string.can_not_read_usb_flash_disk), Toast.LENGTH_LONG).show();
			}else{
				boolean success = false;
				try{
					UpdateUtils updateUtils = new UpdateUtils(getActivity());
					success = updateUtils.installUpdateZip(UpdateUtils.SOURCE_USB);
				}catch(Exception e){
					success = false;
				}
				if(success){
					Toast.makeText(getActivity(), "Update successfull", Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_LONG).show();
				}
			}
			preference.setEnabled(true);
			return false;
		}
	}); 
       
       
       
       //Install update from SDCARD
       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_SOFTWARE_UPDATE_SDCARD)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			preference.setEnabled(false);
			ExportUtils exportUtils = new ExportUtils();
			if(exportUtils.checkExternalSDCard() == false){
				Toast.makeText(getActivity(), getActivity().getString(R.string.can_not_read_from_sd_card), Toast.LENGTH_LONG).show();
			}else{
				boolean success = false;
				try{
					UpdateUtils updateUtils = new UpdateUtils(getActivity());
					success = updateUtils.installUpdateZip(UpdateUtils.SOURCE_SDCARD);
				}catch(Exception e){
					success = false;
				}
				if(success){
					Toast.makeText(getActivity(), "Update successfull", Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_LONG).show();
				}
			}
			preference.setEnabled(true);
			return false;
		}
	}); 
       
       
       
//Factory Reset
       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_RESET)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			try 
		    {

		         
		         
					final Dialog dialog = new Dialog(getActivity());
					dialog.setContentView(R.layout.dialog_yes_no);
					dialog.setTitle(R.string.factory_reset);
		 
					// set the custom dialog components - text, image and button
					TextView text = (TextView) dialog.findViewById(R.id.text);
					text.setText("WARNING:" + " "+ getString(R.string.do_you_really_want_to) +" "+ getString(R.string.delete_all_data_));
					ImageView image = (ImageView) dialog.findViewById(R.id.dialog_image);
					image.setVisibility(View.GONE);
		            Button dialogButtonNo = (Button) dialog.findViewById(R.id.dialogButtonNO);
		            dialogButtonNo.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
					// if button is clicked, close the custom dialog
					dialogButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							
							dialog.dismiss();
							
							if(AppConstants.TABLET_HAS_ROOT){
							
				    	    try
				    	    {

				    	        String command;
				    	       command = "pm clear com.certoclav.app";
				    	       Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command });//, envp);
				    	       proc.waitFor();
				    	       
				    	    }
				    	    catch(Exception ex){
				    	    	Log.e("SettingsDeviceFragment", "error clear app data");
				    	         Log.e("SettingsDeviceFragment", ex.toString());
				    	    }
							}else{
								// closing Entire Application
							    Editor editor = getActivity().getSharedPreferences("clear_cache", Context.MODE_PRIVATE).edit();
							    editor.clear();
							    editor.commit();
							    ApplicationController.getInstance().clearApplicationData();
							    android.os.Process.killProcess(android.os.Process.myPid());
							}

						}
					});
		 
					dialog.show();

		    } 
		    catch (Exception e) 
		    {
		          e.printStackTrace();
		    }
			

			return false;
		}
	});


  
       
    }

    

    
    






	@Override
	public void onResume() {
		Autoclave.getInstance().setOnSensorDataListener(this);
//show date and time
		

	       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_DATE)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Toast.makeText(getActivity(), getActivity().getString(R.string.please_use_secondary_lcd_screen_to_change_the_time), Toast.LENGTH_LONG).show();
				return false;
			}
		});
	       
	       
		//Storage
	       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_STORAGE))
	       						.setSummary(getString(R.string.free_memory)+": " 
	       										+ Long.toString(FreeMemory())
	       										+ " MB");
	//Software Version
	       PackageInfo pInfo;
		try {
			pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		    String version = pInfo.versionName + " (" + pInfo.versionCode + ")";   
		    ((Preference) findPreference(AppConstants.PREFERENCE_KEY_VERSION)).setSummary(version); 
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		
		
		
		//serial number
		try {
		    ((Preference) findPreference(AppConstants.PREFERENCE_KEY_SERIAL_NUMBER)).setSummary(Autoclave.getInstance().getController().getSerialnumber()); 
		} catch (Exception e) {
			try {
				((Preference) findPreference(AppConstants.PREFERENCE_KEY_SERIAL_NUMBER)).setSummary(getString(R.string.please_connect_to_autoclave_first));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}	

		//firmware version
		try {
		    ((Preference) findPreference(AppConstants.PREFERENCE_KEY_FIRMWARE_VERSION)).setSummary(Autoclave.getInstance().getController().getFirmwareVersion()); 
		} catch (Exception e) {
			try {
				((Preference) findPreference(AppConstants.PREFERENCE_KEY_FIRMWARE_VERSION)).setSummary(getString(R.string.please_connect_to_autoclave_first));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		//cycle number
		try {
		    ((Preference) findPreference(AppConstants.PREFERENCE_KEY_CYCLE_NUMBER)).setSummary(getString(R.string.total_cycles_)+" " + Autoclave.getInstance().getController().getCycleNumber()); 
		} catch (Exception e) {
			try {
				((Preference) findPreference(AppConstants.PREFERENCE_KEY_CYCLE_NUMBER)).setSummary(getString(R.string.please_connect_to_autoclave_first));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
		
		super.onResume();
	}


	@Override
	public void onPause() {
Autoclave.getInstance().removeOnSensorDataListener(this);
		super.onPause();
	}


    public long FreeMemory()
    {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long   Free   = (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
        return Free;
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }











	@Override
	public void onSensorDataChange(AutoclaveData data) {
		//update date and time
		

	       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_DATE)).setSummary(  new StringBuilder()
	                .append(Autoclave.getInstance().getDate())                // Month is 0 based so add 1
	                .append(" ")
	                .append(Autoclave.getInstance().getTime()));
		
	}
    

    
    
}