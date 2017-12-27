package com.certoclav.app.monitor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;
import com.certoclav.library.application.ApplicationController;

public class MonitorAutoclaveFragment extends Fragment implements SensorDataListener {

private TextView textTemp;
private TextView textPress;
private TextView textMedia;

		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.monitor_autoclave_fragment,container, false); //je nach mIten k�nnte man hier anderen Inhalt laden.
		
			textTemp = (TextView) rootView.findViewById(R.id.monitor_text_temp);
			textPress = (TextView) rootView.findViewById(R.id.monitor_text_press);
			textMedia = (TextView) rootView.findViewById(R.id.monitor_text_media);
			Autoclave.getInstance().setOnSensorDataListener(this);

			return rootView;
		}	
		
		
		
	@Override
		public void onResume() {
			super.onResume();
			if(Autoclave.getInstance().getProfile().getIndex() == 12){ //liquid program
				textMedia.setVisibility(View.VISIBLE);
			}else{
				textMedia.setVisibility(View.GONE);
			}
			
		}



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		



      
		
	}



	@Override
	public void onSensorDataChange(AutoclaveData data) {
		try {
			textTemp.setText(ApplicationController.getContext().getString(R.string.steam) +" " + data.getTemp1().getValueString() + " \u2103");
			textPress.setText(ApplicationController.getContext().getString(R.string.press) + " " + data.getPress().getValueString() + " kPa");
			textMedia.setText(ApplicationController.getContext().getString(R.string.media)+" " + data.getTemp2().getValueString() + " \u2103");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

