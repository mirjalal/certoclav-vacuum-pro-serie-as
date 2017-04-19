package com.certoclav.app.menu;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.certoclav.app.R;

public class InformationFragment extends Fragment {

	



		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.menu_fragment_information,container, false); //je nach mIten könnte man hier anderen Inhalt laden.
		
			Fragment fragment = new VideoFragment();
			FragmentManager     fm = getFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.fragment_video_content, fragment);
			ft.commit(); 
			
			fragment = new ShopFragment();
			ft = fm.beginTransaction();
			ft.replace(R.id.fragment_shop_content, fragment);
		ft.commit(); 		

			fragment = new MessagesFragment();
			ft = fm.beginTransaction();
			ft.replace(R.id.fragment_messages_content, fragment);
			ft.commit(); 
			
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

