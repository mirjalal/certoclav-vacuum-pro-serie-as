package com.certoclav.app.settings;




import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.listener.UserProgramListener;
import com.certoclav.app.menu.MenuLabelPrinterActivity;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.library.util.ExportUtils;


public class SettingsSterilisationFragment extends PreferenceFragment {

	private ProgressDialog barProgressDialog;
	private static final int EXPORT_TARGET_USB = 1;
	private static final int EXPORT_TARGET_SD = 2;

		
	
	
	private OnSharedPreferenceChangeListener listener;
	public SettingsSterilisationFragment() {
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
       addPreferencesFromResource(R.xml.preference_sterilization);
       
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
      
       //PRE HEAT ON OFF
       ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_PREHEAT)).setChecked(Autoclave.getInstance().isPreheat());
       ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_PREHEAT)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
		    if(newValue instanceof Boolean){
	            Boolean boolVal = (Boolean)newValue;
	            ReadAndParseSerialService.getInstance().sendPreheatCommand(boolVal);
	        }
			
			return true;
		}
	});
       
       //KEEP TEMP ON OFF
       ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_KEEP_TEMP)).setChecked(Autoclave.getInstance().isPreheat());
       ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_KEEP_TEMP)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
		    if(newValue instanceof Boolean){
	            Boolean boolVal = (Boolean)newValue;
	            ReadAndParseSerialService.getInstance().sendKeepTemperatureCommand(boolVal);
	        }
			
			return true;
		}
	});
       
       //Edit user defined program
       ((Preference) findPreference(AppConstants.PREFREENCE_KEY_USER_DEFINED)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			try 
		    {
					final EditProgramDialog dialog = new EditProgramDialog(getActivity());
					dialog.setContentView(R.layout.dialog_edit_program);
					dialog.setTitle("Edit custom program");
					final UserProgramListener userProgramReceivedListener = new UserProgramListener() {
						
						@Override
						public void onUserProgramReceived() {
							Profile profile = Autoclave.getInstance().getUserDefinedProgram();
							((TextView) dialog.findViewById(R.id.dialog_program_edit_vacuum_times)).setText(Integer.toString(profile.getVacuumTimes()));
							((TextView) dialog.findViewById(R.id.dialog_program_edit_sterilizationtemperature)).setText(Integer.toString(profile.getSterilisationTemperature()));
							((TextView) dialog.findViewById(R.id.dialog_program_edit_sterilizationtime)).setText(Integer.toString(profile.getSterilisationTime()));
							((TextView) dialog.findViewById(R.id.dialog_program_edit_dryingtime)).setText(Integer.toString(profile.getDryTime()));
							
							
						}
					};

					Autoclave.getInstance().setOnUserProgramListener(userProgramReceivedListener);
					
					ReadAndParseSerialService.getInstance().sendGetUserProgramCommand();
					
		            Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialog_program_button_cancel);
		            dialogButtonCancel.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Autoclave.getInstance().removeOnUserProgramListener(userProgramReceivedListener);
							dialog.dismiss();
						}
					});
					Button dialogButtonApply = (Button) dialog.findViewById(R.id.dialog_program_button_apply);
					// if button is clicked, close the custom dialog
					dialogButtonApply.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							try{
								Integer vacuumTimes = Integer.parseInt(((TextView) dialog.findViewById(R.id.dialog_program_edit_vacuum_times)).getText().toString());
								Integer sterilizationTemp = Integer.parseInt(((TextView) dialog.findViewById(R.id.dialog_program_edit_sterilizationtemperature)).getText().toString());
								Integer sterilizationTime = Integer.parseInt(((TextView) dialog.findViewById(R.id.dialog_program_edit_sterilizationtime)).getText().toString());
								Integer dryingTime = Integer.parseInt(((TextView) dialog.findViewById(R.id.dialog_program_edit_dryingtime)).getText().toString());
								if(vacuumTimes >= 1 && vacuumTimes <=10){
									if(sterilizationTemp>=105 && sterilizationTemp <= 134){
										if(sterilizationTime >= 4 && sterilizationTime <= 60){
											if(dryingTime >=1 && dryingTime <=25){
												ReadAndParseSerialService.getInstance().sendPutUserProgramCommand(vacuumTimes, sterilizationTemp, sterilizationTime, dryingTime);
												ReadAndParseSerialService.getInstance().sendGetUserProgramCommand();
												Toast.makeText(getActivity(), R.string.program_saved, Toast.LENGTH_LONG).show();
												Autoclave.getInstance().removeOnUserProgramListener(userProgramReceivedListener);
												dialog.dismiss();
											}else{
												Toast.makeText(getActivity(), "Please enter a valid drying time", Toast.LENGTH_LONG).show();
											}
										}else{
											Toast.makeText(getActivity(), "Please enter a valid sterilization time", Toast.LENGTH_LONG).show();
										}
									}else{
										Toast.makeText(getActivity(), "Please enter a valid sterilization temperature", Toast.LENGTH_LONG).show();
									}
								}else{
									Toast.makeText(getActivity(), "Please enter a valid number of vacuum times", Toast.LENGTH_LONG).show();
								}
							}catch(Exception e){

								Toast.makeText(getActivity(), "Please enter a valid data", Toast.LENGTH_LONG).show();
							
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

     //upload protocols to USB
       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_EXPORT_USB)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {

			
			ExportUtils exportUtils = new ExportUtils();
			

			if (exportUtils.checkExternalMedia()) { //check if usb flash drive is available
				uploadAllProtocolsTo(EXPORT_TARGET_USB);
			}else{
				
				try 
			    {

			         
			         
						final Dialog dialog = new Dialog(getActivity());
						dialog.setContentView(R.layout.dialog_yes_no);
						dialog.setTitle("Mount USB-Stick");
						
			 
						// set the custom dialog components - text, image and button
						TextView text = (TextView) dialog.findViewById(R.id.text);
						text.setText("A reboot is neccessary in order to write to USB-Stick. Please insert the USB-Stick and reboot this touch terminal.");
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
								
							
							}
						});
			 
						dialog.show();

			}catch(Exception e){
				
			}
			}
				return false;
		
		}
	});   

       
       
       //OPEN LABEL PRINTER UTIL
       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_PRINT_LABEL)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			
			Intent intent = new Intent(getActivity(), MenuLabelPrinterActivity.class);
			getActivity().startActivity(intent);
		
			return false;
		}
	});   

       
       
       
       
       //upload protocols to SD
       ((Preference) findPreference(AppConstants.PREFERENCE_KEY_EXPORT_SD)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {

			
			ExportUtils exportUtils = new ExportUtils();
			

			if (exportUtils.checkExternalSDCard()) { //check if usb flash drive is available
				uploadAllProtocolsTo(EXPORT_TARGET_SD);
			}else{
				
				try 
			    {

			         
			         
						final Dialog dialog = new Dialog(getActivity());
						dialog.setContentView(R.layout.dialog_yes_no);
						dialog.setTitle("No SD-Card");
						
			 
						// set the custom dialog components - text, image and button
						TextView text = (TextView) dialog.findViewById(R.id.text);
						text.setText("No SD-Card detected. Please reboot this touch terminal and try again.");
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
								
							
							}
						});
			 
						dialog.show();

			}catch(Exception e){
				
			}
			}
				return false;
		
		}
	});   
       
    prefs.registerOnSharedPreferenceChangeListener(listener);
       

       
    }

    

    

    @Override
	public void onResume() {
        ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_PREHEAT)).setChecked(Autoclave.getInstance().isPreheat());
        ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_KEEP_TEMP)).setChecked(Autoclave.getInstance().isPreheat());
		super.onResume();
	}

	public void uploadAllProtocolsTo(final int target_id){
		
        barProgressDialog = new ProgressDialog(getActivity());
   	 
        barProgressDialog.setTitle("COPY PROTOTOCOLS");
        if(target_id == EXPORT_TARGET_USB){
        	barProgressDialog.setMessage("copy protocols to USB flash drive");
        }else{
        	barProgressDialog.setMessage("copy protocols to SD card");
        }
        barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setCanceledOnTouchOutside(true);
        barProgressDialog.setMax(100);
        barProgressDialog.setCancelable(true);
        barProgressDialog.setCanceledOnTouchOutside(false);
        barProgressDialog.show();
        
        
        
        DatabaseService databaseServie = new DatabaseService(getActivity());
        final List<Protocol> protocols = databaseServie.getProtocols();
        
        new AsyncTask<Void, Boolean, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {

	                try{
	  				  int numberOfProtocols = protocols.size();
		          	  int i = 0;  
	                while (i<numberOfProtocols) {
	              		
	              	  StringBuilder sb = new StringBuilder();
	              	  Protocol protocol = protocols.get(i);
	              	  
	              		String filename = Autoclave.getInstance().getController().getSerialnumber() + "-" +protocol.getZyklusNumber();
	              	//	barProgressDialog.setMessage("Copy " + filename + ".txt");
	              		 
	              	  sb.append("Protocol CertoClav Vacuum Pro Series").append("\r\n")
	              	  .append("S/N.: ").append(Autoclave.getInstance().getController().getSerialnumber()).append("\r\n")
	              	  .append("\r\n")
	              	  .append("Program: ").append(protocol.getProfileName()).append("\r\n")
	              	  .append("Program description: ").append(protocol.getProfileDescription()).append("\r\n")
	              	  .append("Cycle number: ").append(protocol.getZyklusNumber()).append("\r\n")
	              	  .append("Start time: ").append(protocol.getStartTime()).append("\r\n")
	              	  .append("End time: ").append(protocol.getEndTime()).append("\r\n")
	              	  .append("Status: ").append(protocol.getErrorCode()).append("\r\n")
	              	  .append("\r\n")
	              	  .append("h:m:s").append("\t").append("temperature").append("\t").append("pressure").append("\r\n");
	              	  for(ProtocolEntry pE : protocol.getProtocolEntry()){
	              		  sb.append(pE.getFormatedTimeStampShort()).append("\t").append(pE.getTemperature()).append("\t").append(pE.getPressure()).append("\r\n");
	              	  }
	              	   
	              	  
	              	  sb.append("Summary: ").append(AutoclaveMonitor.getInstance().getErrorString(protocol.getErrorCode()));
	              	  ExportUtils expUtils = new ExportUtils();
	              	  boolean success = false;
	              	  if(target_id == EXPORT_TARGET_USB){
	              		  success = expUtils.writeToExtUsbFile("Certoclav protocols", filename, "txt", sb.toString());
	              	  }else{
	              		  success = expUtils.writeToExtSDFile("Certoclav protocols", filename, "txt", sb.toString());
	              	  }
	              	  if(success == false)  {
	              		  barProgressDialog.dismiss();
	              		  return false;
	              	  }

	              	  i++;
	                    barProgressDialog.setProgress((100*i)/numberOfProtocols);
	                     
	                
	           
	                }//end while
	                //all protocols copied sucessfully
	              	  barProgressDialog.dismiss();
	              	  return true;
	                
	    
	                }catch(Exception e){
	              	  Log.e("ExportUtils", "Exception during copying protocols: " + e.toString());
	              	  e.printStackTrace();
	              	  barProgressDialog.dismiss();
	              	  return false;
	                }

			}

			@Override
			protected void onPostExecute(Boolean result) {
				if(result == false){
					Toast.makeText(getActivity(), "Export failed", Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(getActivity(), "Export successful", Toast.LENGTH_LONG).show();
				}
				super.onPostExecute(result);
			}
			
			
		}.execute();
        
		
		

  		
  	}


    
    public class EditProgramDialog extends Dialog {

		public EditProgramDialog(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

		
		public void setOnUserProgramReceivedListener(UserProgramListener listener){
			Autoclave.getInstance().setOnUserProgramListener(listener);
		}
		
		public void removeOnUserProgramReceivedListener(UserProgramListener listener){
			Autoclave.getInstance().removeOnUserProgramListener(listener);
		}
    	
    }
    

 
 
    
}