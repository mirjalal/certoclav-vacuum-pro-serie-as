package com.certoclav.app.adapters;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
import com.certoclav.app.menu.ScanActivity;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.monitor.MonitorActivity;
import com.certoclav.app.sterilisationassistant.AssistantActivity;
import com.certoclav.library.application.ApplicationController;




/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 * 
*/
public class ProgramAdapter extends ArrayAdapter<Profile> {
	private final Context mContext;
	private final List<Profile> mValues;

	private DatabaseService databaseService;




	/**
	 * Constructor
	 * 
	 * @param context
	 *            context of calling activity
	 * @param values
	 * {@link List}<{@link Profile}> containing the data to populate the list
	 */
	public ProgramAdapter(Context context, List<Profile> values) {
 
		super(context, R.layout.menu_fragment_sterilisation_element, values);
		this.mContext = context;
		this.mValues = values;
		databaseService = new DatabaseService(mContext);


	}

	/**
	 * Gets a View that displays the data at the specified position in the data
	 * set.The View is inflated it from profile_list_row XML layout file
	 * 
	 * @see Adapter#getView(int, View, ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
	
	 if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.menu_fragment_sterilisation_element, parent, false);
	 }
		
	
		final TextView firstLine = (TextView) convertView.findViewById(R.id.sterilisation_element_firstline);
		firstLine.setText(getItem(position).getName());
	

		final TextView textDuration = (TextView) convertView.findViewById(R.id.sterilisation_element_text_description);
	

		TextView textButton = (TextView) convertView.findViewById(R.id.sterilisation_element_text_start);
		if(AppConstants.IS_CERTOASSISTANT){
			textButton.setText("Show video assistant");
		}
		
	
		if(getItem(position).getIndex() == 7){ //user defined profile
			getItem(position).setDescription(	"Vacuum times: "+ Autoclave.getInstance().getUserDefinedProgram().getVacuumTimes()+"\n"+
									"Sterilization temp.: "+ Autoclave.getInstance().getUserDefinedProgram().getSterilisationTemperature()+" °C\n"+
									"Sterilization time: "+ Autoclave.getInstance().getUserDefinedProgram().getSterilisationTime()+" min\n"+
									"Drying time: " + Autoclave.getInstance().getUserDefinedProgram().getDryTime()+" min");
		}
		textDuration.setText(getItem(position).getDescription());
		
	    

			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.e("ProgramAdapter", "onclick");
					
					final Dialog dialog = new Dialog(mContext);
					dialog.setContentView(R.layout.dialog_yes_no);
					dialog.setTitle(R.string.start_program);
					if(AppConstants.IS_CERTOASSISTANT){
						dialog.setTitle("Start video assistant");
					}
					dialog.setCancelable(true);
		 
					// set the custom dialog components - text, image and button
					TextView text = (TextView) dialog.findViewById(R.id.text);
					text.setText(mContext.getString(R.string.do_you_really_want_to_start)+" " + getItem(position).getName() + "?");
					if(AppConstants.IS_CERTOASSISTANT){
					text.setText(mContext.getString(R.string.do_you_really_want_to_start)+" " + "the video assistant" + "?");	
					}
					ImageView image = (ImageView) dialog.findViewById(R.id.dialog_image);
					image.setImageResource(R.drawable.ic_menu_help);
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

				
								Autoclave.getInstance().setProfile(getItem(position));				
								SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext());
								Boolean defaultvalue = mContext.getResources().getBoolean(R.bool.switch_step_by_step_default);
					
									if(prefs.getBoolean(AppConstants.PREFERENCE_KEY_SCAN_ITEM_ENABLED, false)){
										Intent intent = new Intent(mContext, ScanActivity.class);
										mContext.startActivity(intent);
									}else{
								
										if(prefs.getBoolean(AppConstants.PREFERENCE_KEY_STEP_BY_STEP, defaultvalue)){
											Intent intent = new Intent(ApplicationController.getContext(), AssistantActivity.class );
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											ApplicationController.getContext().startActivity(intent);
										}else{
											Intent intent = new Intent(ApplicationController.getContext(), MonitorActivity.class);
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											ApplicationController.getContext().startActivity(intent);
							
										}
									
								}
							dialog.dismiss();
							
						}
					});

					dialog.show();

					
				}
			});


				
			
        


		return convertView;
	}









}