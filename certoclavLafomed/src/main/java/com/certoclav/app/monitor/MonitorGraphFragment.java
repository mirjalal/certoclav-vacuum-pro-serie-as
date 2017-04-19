package com.certoclav.app.monitor;

import org.achartengine.GraphicalView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.certoclav.app.R;
import com.certoclav.app.graph.GraphService;

public class MonitorGraphFragment extends Fragment {

	private static GraphicalView currentGraph;

		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.monitor_graph_fragment,container, false); //je nach mIten könnte man hier anderen Inhalt laden.
		

			currentGraph = GraphService.getInstance().getCurrentGraph(getActivity());

			if(currentGraph != null){
		
				LinearLayout graphContainer = (LinearLayout) rootView.findViewById(R.id.monitor_graph_container);
				graphContainer.removeAllViews();
				graphContainer.addView(currentGraph);
			}

			Log.e("ControlGraphFragment", "nac add view getcurrentgraph");    

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

