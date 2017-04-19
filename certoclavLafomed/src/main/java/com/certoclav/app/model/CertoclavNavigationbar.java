package com.certoclav.app.model;


import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.certoclav.app.R;
import com.certoclav.app.button.QuickActionItem;
import com.certoclav.app.listener.NavigationbarListener;

public class CertoclavNavigationbar {



	ArrayList<NavigationbarListener> navigationbarListeners = new ArrayList<NavigationbarListener>();

	private Context mContext;
	private Activity mActivity;
	
	
	private View tabInformation;
	private View tabSterilisation;
	private View tabProtocols;
	
	private LinearLayout linActionContainer;
	private QuickActionItem actionItemSettings;
	private QuickActionItem actionItemEdit;
	private QuickActionItem actionItemAdd;
	private QuickActionItem actionItemLogout;
	private QuickActionItem actionItemPrint;
	private QuickActionItem actionItemScan;
	
	public static final int BUTTON_SETTINGS = 1;
	public static final int BUTTON_REFRESH = 2;
	public static final int BUTTON_SAVE = 3;
	public static final int BUTTON_ADD = 4;
	public static final int BUTTON_EDIT = 5;
	public static final int BUTTON_BACK = 6;
	public static final int TAB_INFORMATION = 7;
	public static final int TAB_STERILISATION = 8;
	public static final int TAB_PROTOCOLS = 9;
	public static final int BUTTON_PRINT = 10;
	public static final int BUTTON_SCAN = 11;
	

	public void setNavigationbarListener (NavigationbarListener listener){
		this.navigationbarListeners.add(listener);
	}

	public void removeNavigationbarListener(NavigationbarListener listener) {
		this.navigationbarListeners.remove(listener);
		
		
	}
	
	
	public CertoclavNavigationbar(Activity activity){
		mActivity = activity;
		mContext = (Context) mActivity;

	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void showNavigationBar(){
		mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		tabInformation = mActivity.findViewById(R.id.navigationbar_text_information);
		tabSterilisation = mActivity.findViewById(R.id.navigationbar_text_sterilisation);		
		tabProtocols = mActivity.findViewById(R.id.navigationbar_text_protocols);
		






		
		tabSterilisation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (NavigationbarListener listener : navigationbarListeners){
					listener.onClickNavigationbarButton(TAB_STERILISATION);
				}
				
			}
		});
		
		
		tabProtocols.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (NavigationbarListener listener : navigationbarListeners){
					listener.onClickNavigationbarButton(TAB_PROTOCOLS);
				}
				
			}
		});
		
		
		tabInformation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for (NavigationbarListener listener : navigationbarListeners){
					listener.onClickNavigationbarButton(TAB_INFORMATION);
				}
				
			}
		});
		
		
		linActionContainer = (LinearLayout) mActivity.findViewById(R.id.navigationbar_container_quickactionitem_rest);
		
		actionItemEdit = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
		actionItemEdit.setChecked(false);
		actionItemEdit.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_menu_launcher_settings));
		//actionItemAdd.setText("test");
		linActionContainer.addView(actionItemEdit);
		actionItemEdit.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				
				for (NavigationbarListener listener : navigationbarListeners){
					listener.onClickNavigationbarButton(BUTTON_EDIT);
				}
			}
		});
		actionItemEdit.setVisibility(View.INVISIBLE);
		
		actionItemAdd = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
		actionItemAdd.setChecked(false);
		actionItemAdd.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_menu_add));
		//actionItemAdd.setText("test");
		linActionContainer.addView(actionItemAdd);
		actionItemAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
						for(NavigationbarListener listener : navigationbarListeners){
					listener.onClickNavigationbarButton(BUTTON_ADD);
				}
				
			}
		});
		actionItemAdd.setVisibility(View.INVISIBLE);
		
		
		

		actionItemPrint = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
		actionItemPrint.setChecked(false);
		actionItemPrint.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_menu_print));
		//actionItemAdd.setText("test");
		linActionContainer.addView(actionItemPrint);
		actionItemPrint.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
			    
				for (NavigationbarListener listener : navigationbarListeners){
					listener.onClickNavigationbarButton(BUTTON_PRINT);
				}
			}
		});
		
		
		
		actionItemScan = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
		actionItemScan.setChecked(false);
		actionItemScan.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_menu_scan));
		linActionContainer.addView(actionItemScan);
		actionItemScan.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
			    
				for (NavigationbarListener listener : navigationbarListeners){
					listener.onClickNavigationbarButton(BUTTON_SCAN);
				}
			}
		});
		
		
		
		
		
		
		
		
		linActionContainer = (LinearLayout) mActivity.findViewById(R.id.navigationbar_container_quickactionitem_settings);
		actionItemSettings = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
		actionItemSettings.setChecked(false);
		actionItemSettings.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_menu_manage));
		//actionItemAdd.setText("test");
		linActionContainer.addView(actionItemSettings);
		actionItemSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				for (NavigationbarListener listener : navigationbarListeners){
					listener.onClickNavigationbarButton(BUTTON_SETTINGS);
				}
		
			}
		});


		linActionContainer = (LinearLayout) mActivity.findViewById(R.id.navigationbar_container_quickaction_left);
		actionItemLogout = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
		actionItemLogout.setChecked(false);
		actionItemLogout.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_menu_exit));
		//actionItemLogout.setText("Logout");
		linActionContainer.addView(actionItemLogout);
		actionItemLogout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				for (NavigationbarListener listener : navigationbarListeners){
					listener.onClickNavigationbarButton(BUTTON_BACK);
				}
		
			}
		});

		


		

	}
	
	public void hideButtonEdit(){
		actionItemEdit.setVisibility(View.GONE);
	}
	
	public void showButtonEdit(){
		actionItemEdit.setVisibility(View.VISIBLE);
	}

	public void hideButtonAdd(){
		actionItemAdd.setVisibility(View.GONE);
	}
	
	public void showButtonAdd(){
		actionItemAdd.setVisibility(View.VISIBLE);
	}
	



	public void setTabInformationEnabled() {
		tabInformation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_white_shadow_top_left));
		tabSterilisation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_shadow_bottom_left));
		tabProtocols.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_shadow_bottom));
		hideButtonAdd();
		hideButtonEdit();
		actionItemPrint.setVisibility(View.INVISIBLE);
		actionItemScan.setVisibility(View.INVISIBLE);
		
	}

	public void setTabProtocolsEnabled() {
		tabInformation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_shadow_bottom));
		tabSterilisation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_shadow_bottom_right));
		tabProtocols.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_white_shadow_top_right));
		hideButtonAdd();
		hideButtonEdit();
		actionItemPrint.setVisibility(View.VISIBLE);
		actionItemScan.setVisibility(View.VISIBLE);
	}
	
	public void setTabSterilisationEnabled() {
		tabInformation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_shadow_bottom_right));
		tabSterilisation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_white_shadow_top));
		tabProtocols.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_shadow_bottom_left));
		//showButtonEdit();
		actionItemPrint.setVisibility(View.INVISIBLE);
		actionItemScan.setVisibility(View.INVISIBLE);
	}
	




	





	
}