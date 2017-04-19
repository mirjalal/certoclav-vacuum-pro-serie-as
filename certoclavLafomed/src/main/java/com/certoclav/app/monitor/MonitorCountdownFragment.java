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

public class MonitorCountdownFragment extends Fragment implements SensorDataListener {

private TextView textNumber;
private View loadingBar;
private MonitorUtils monitorService;

		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.monitor_countdown_fragment,container, false); //je nach mIten könnte man hier anderen Inhalt laden.
		
			textNumber = (TextView) rootView.findViewById(R.id.monitor_countdown_number);
			loadingBar = rootView.findViewById(R.id.monitor_countdown_bar);
			 monitorService = new MonitorUtils();
	        Autoclave.getInstance().setOnSensorDataListener(this);

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



	@Override
	public void onSensorDataChange(AutoclaveData data) {
		try{
			
 
			int seconds = (int) (Autoclave.getInstance().getSecondsSinceStart()%60);
			int minutes = (int) ((  Autoclave.getInstance().getSecondsSinceStart()/60  )%60); 
			int hours = (int) ((Autoclave.getInstance().getSecondsSinceStart()/60/60)%24);
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append(String.format("%02d", hours))
					.append(":")
					.append(String.format("%02d", minutes))
					.append(":")
					.append(String.format("%02d", seconds));
		textNumber.setText(sBuilder.toString());
		//loadingBar.getLayoutParams().width = 300 - (3* ((monitorService.getRemainingTime()*100) / monitorService.getAbsoluteTime(Autoclave.getInstance().getProfile())));
		}catch(Exception e){
			//do nothing
		}
		//loadingBar.requestLayout();
		
	}



}

