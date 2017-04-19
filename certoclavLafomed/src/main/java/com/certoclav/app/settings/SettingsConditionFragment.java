package com.certoclav.app.settings;




import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.adapters.ConditionAdapter;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.library.certocloud.CloudDatabase;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.Condition;


public class SettingsConditionFragment extends Fragment implements NavigationbarListener {
	
	private GridView gridViewCondition;
	private ConditionAdapter conditionAdapter = null;
	private ProgressBar progressBar = null;
	private TextView viewText = null;
	private Button buttonSave = null;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.e("SterilisationFragment", "oncreate");
		View rootView = inflater.inflate(R.layout.settings_conditions_fragment,container, false); 
	    Log.e("SterilsationFragment", "hole programGrid aus xml datei");

	    buttonSave = (Button) rootView.findViewById(R.id.menu_conditions_button_save);
	    buttonSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AsyncTask<Void, Void, Boolean>(){

					@Override
					protected void onPreExecute() {
						buttonSave.setEnabled(false);
						super.onPreExecute();
					}

					@Override
					protected void onPostExecute(Boolean result) {
						buttonSave.setEnabled(true);
						if(result){
							Toast.makeText(getActivity(), getActivity().getString(R.string.changes_successfully_saved), Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(getActivity(), getActivity().getString(R.string.changes_could_not_be_saved), Toast.LENGTH_LONG).show();
						}
						super.onPostExecute(result);
					}

					@Override
					protected Boolean doInBackground(Void... params) {

						try {
							ArrayList<Condition> conditionList = new ArrayList<Condition>();
							for(int i = 0; i< conditionAdapter.getCount();i++){
								conditionList.add(conditionAdapter.getItem(i));
							}						
							Boolean success  =  CloudDatabase.getInstance().updateConditions(conditionList);
							return success;
						}catch(Exception e){
							
						}
						return false;
					}
					
				}.execute();
				
			}
		});
		gridViewCondition = (GridView) rootView.findViewById(R.id.video_gridlayout);
		conditionAdapter = new ConditionAdapter(getActivity(), new ArrayList<Condition>());
		gridViewCondition.setAdapter(conditionAdapter);
		progressBar = (ProgressBar) rootView.findViewById(R.id.menu_conditions_progressbar);
		progressBar.setVisibility(View.GONE);
		viewText = (TextView) rootView.findViewById(R.id.menu_conditions_text);
		

		
		
			
		

		
		return rootView;

	} 
	
	
	
    
	@Override
	public void onResume() {
		
		
		
	    if(CloudUser.getInstance().isLoggedIn()){
	    	viewText.setVisibility(View.GONE);
	    	buttonSave.setVisibility(View.VISIBLE);
			for(Condition conditionCloud : CloudDatabase.getInstance().getConditionList()){
				conditionAdapter.add(conditionCloud);
			}
			conditionAdapter.notifyDataSetChanged();
			
		}else{
			buttonSave.setVisibility(View.GONE);
			viewText.setText(getActivity().getString(R.string.please_log_in_into_your_certocloud_account_first));
			conditionAdapter.clear();
			conditionAdapter.notifyDataSetChanged();
			viewText.setVisibility(View.VISIBLE);
		}
				

		super.onResume();
	}

	public void onButtonSaveClicked() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClickNavigationbarButton(int buttonId) {
		// TODO Auto-generated method stub
		
	}




  



    
}