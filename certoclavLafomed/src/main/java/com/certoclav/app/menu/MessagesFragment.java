package com.certoclav.app.menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.certoclav.app.R;
import com.certoclav.app.settings.SettingsEmailActivity;

public class MessagesFragment extends Fragment {

		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.menu_fragment_information_messages,container, false); 
		    
			rootView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
				    Intent intent = new Intent(getActivity(), SettingsEmailActivity.class);
				    getActivity().startActivity(intent);
				}
			});
			

			return rootView;
		}	
		
		
		
	@Override
		public void onResume() {
			super.onResume();
			

		}






	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);      
		
	}
	

	
}

