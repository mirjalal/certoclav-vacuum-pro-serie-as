package com.certoclav.app.menu;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;

public class ShopFragment extends Fragment {

	

private ListView shopList;

		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.menu_fragment_information_shop,container, false); 
		
			
            rootView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(AppConstants.isIoSimulated == false){
						String url = getActivity().getString(R.string.url_shop);
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
					
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

