/*
 * Copyright 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.certoclav.library.bluetooth.chooser;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.certoclav.library.R;
import com.certoclav.library.bluetooth.BluetoothService;
import com.certoclav.library.bluetooth.BluetoothService.BluetoothConnectionStateListener;
import com.certoclav.library.bluetooth.BluetoothState;

@SuppressLint("NewApi")
public class BluetoothDeviceListFragment extends Fragment {

    // Member fields
    private BluetoothAdapter mBtAdapter;

    private BluetoothDeviceElementAdapter mDeviceAdapter;


    
    
  
	
	private TextView textProgress = null;
	private ProgressBar progressBar = null;
    


    public interface BluetoothStateListener{
    	public void onStateChanged(int state);
    }
    private BluetoothStateListener bluetoothStateListener = null;

	private String filter = "";
	
    public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public void setOnBluetoothStateListiner(BluetoothStateListener listener){
    	bluetoothStateListener = listener;
    }
    public void removeOnBluetoothStateListiner(){
    	bluetoothStateListener = null;
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View rootView = inflater.inflate(R.layout.bluetooth_device_list_fragment,container, false); //je nach mIten k?nnte man hier anderen Inhalt laden.
	
		textProgress = (TextView) rootView.findViewById(R.id.bluetooth_fragment_text_progress);
		progressBar = (ProgressBar) rootView.findViewById(R.id.bluetooth_fragment_progressbar);

		 
	
		if(BluetoothService.getInstance().isBluetoothAvailable()){
			
		
				BluetoothService.getInstance().setBluetooth(true);
				BluetoothService.getInstance().setOnBluetoothConnectionStateListener(new BluetoothConnectionStateListener() {
					
					@Override
					public void onChangedBluetoothConnectionState(int state,String address) {
						
		                 
		                Log.e("BluetoothDeviceListFragment", "onChangedBluetoothConnectionState");
		                    if (state == BluetoothState.STATE_CONNECTED) {
		                    	
		                    	//remove all elements from list
		                    	mDeviceAdapter.clear();
		                    	//If a device is currently connected, add it to the list. It will not be found during discovery
		                        mDeviceAdapter.add(new BluetoothDeviceElement(BluetoothService.getInstance().getDevice().getAddress(), BluetoothService.getInstance().getDevice().getName(), -70, BluetoothState.STATE_CONNECTED));
		                    	
		                    	mDeviceAdapter.notifyDataSetChanged();
		                       
		                    	if(bluetoothStateListener != null){
		                    		bluetoothStateListener.onStateChanged(BluetoothState.STATE_CONNECTED);
		                    	}
		                    	//TODO callback via listener	   			 		
			   			 		
		                    } else if(state == BluetoothState.STATE_CONNECTING) {
		    	            	for(int i = 0; i< mDeviceAdapter.getCount(); i++){
		    	            		if(mDeviceAdapter.getItem(i).getAddress().equals(address)){
		    	            			mDeviceAdapter.getItem(i).setStatus(BluetoothState.STATE_CONNECTING);	
		    	            		}
		    	            	}
		    	            	mDeviceAdapter.notifyDataSetChanged();
		    	             	
		    	            	
		                    } else if(state == BluetoothState.STATE_DISCONNECTED){
		
		        	            	for(int i = 0; i< mDeviceAdapter.getCount(); i++){
		        	            		if(mDeviceAdapter.getItem(i).getAddress().equals(address)){
		        	            			mDeviceAdapter.getItem(i).setStatus(BluetoothState.STATE_DISCONNECTED);	
		        	            		}
		        	            	}
		        	            	mDeviceAdapter.notifyDataSetChanged();	            
		                    }	
					}
				});
		
        
	        // Setup the window 
	
	
	        
	        
	
	
	        // Initialize array adapters. One for already paired devices 
	        // and one for newly discovered devices
	    
	        
	        mDeviceAdapter= new BluetoothDeviceElementAdapter(getActivity(), new ArrayList<BluetoothDeviceElement>());
	        //mPairedDevicesArrayAdapter = new ArrayAdapter<String>(getActivity(), layout_text);
	
	        // Find and set up the ListView for paired devices
	        ListView pairedListView = (ListView) rootView.findViewById(R.id.list_devices);
	        pairedListView.setItemsCanFocus(false);
	        pairedListView.setAdapter(mDeviceAdapter);
	        pairedListView.setOnItemClickListener(mDeviceClickListener);
	        
	        // Register for broadcasts when a device is discovered
	        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	        getActivity().registerReceiver(mReceiver, filter);
	
	        // Register for broadcasts when discovery has finished
	        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	        getActivity().registerReceiver(mReceiver, filter);
	        
	        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
	        getActivity().registerReceiver(mReceiver, filter);
	        
	        filter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
	        getActivity().registerReceiver(mReceiver, filter);
	
	        // Get the local Bluetooth adapter
	        mBtAdapter = BluetoothAdapter.getDefaultAdapter();


		}
		return rootView;
		
    }

	
	
    @Override
	public void onResume() {

    if(BluetoothService.getInstance().isBluetoothAvailable()){
    //	doDiscovery(filter);
    //	textProgress.setVisibility(View.INVISIBLE);
    //	progressBar.setVisibility(View.INVISIBLE);
    	
    }
		
		super.onResume();
	}



	@Override
	public void onPause() {
		super.onPause();
	}



	public void onDestroy() {
        super.onDestroy();
        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        if(BluetoothService.getInstance().isBluetoothAvailable()){
        	getActivity().unregisterReceiver(mReceiver);
        }
    }

	
	
    /*
     * Start Discovery for bluetooth devices and list them in this fragment
     * 
     */
	public void doDiscovery(String preFilter) {
		filter  = preFilter;

		try{
			 if(mBtAdapter.isDiscovering()){
				 mBtAdapter.cancelDiscovery();
			 }
        	//clear the  list of found devices, because we will start a new discovery for devices
        	mDeviceAdapter.clear();
        	
        	//If a device is currently connected, list it at first. It will not be found during discovery
        	if(BluetoothService.getInstance().getState() == BluetoothState.STATE_CONNECTED){
        		
        		mDeviceAdapter.add(new BluetoothDeviceElement(BluetoothService.getInstance().getDevice().getAddress(), BluetoothService.getInstance().getDevice().getName(), -70, BluetoothState.STATE_CONNECTED));
        	}
        	
        	// Request discover from BluetoothAdapter
        	mBtAdapter.startDiscovery();
		}catch(Exception e){
			Log.e("BluetoothDeviceListFragment", "Exception during doDiscovery: " + e.toString());
		}
        
	}

	
	
    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	
         if(mDeviceAdapter.getItem(arg2)!=null) {
        	 //if status != Trying to connect
        	 if(mDeviceAdapter.getItem(arg2).getStatus()== BluetoothState.STATE_CONNECTED){
        		 BluetoothService.getInstance().reset();
        		 doDiscovery(filter);
        	 }else if(mDeviceAdapter.getItem(arg2).getStatus() != BluetoothState.STATE_CONNECTING){
            	

      
        		 // Cancel discovery because it's costly and we're about to connect
        		 if(mBtAdapter.isDiscovering()){
        			 mBtAdapter.cancelDiscovery();
        		 }

	        	mDeviceAdapter.getItem(arg2).setStatus(BluetoothState.STATE_CONNECTING);
	        	mDeviceAdapter.notifyDataSetChanged();
	            if(mDeviceAdapter.getItem(arg2).getAddress()!= null){
	            	BluetoothService.getInstance().connect(mDeviceAdapter.getItem(arg2).getAddress());
	            }
            }
        }
      }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
           
            
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
            	if(intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)){
            		BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            		if(device.getName() != null){
            			if(device.getName().startsWith(filter)){
            				int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE); //signal strength
            				mDeviceAdapter.add(new BluetoothDeviceElement(device.getAddress(), device.getName(),rssi, BluetoothState.STATE_DISCONNECTED));
            				mDeviceAdapter.notifyDataSetChanged();
            			}
            		
                }
            	}

             
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	// When discovery is finished, change the Activity title
            	Log.e("BluetoothDeviceListFragment", "ACTION_DISCOVERY_FINISHED");
            	textProgress.setVisibility(View.INVISIBLE);
            	progressBar.setVisibility(View.INVISIBLE);
            	if(BluetoothService.getInstance().getState() == BluetoothState.STATE_DISCONNECTED){

	            
            		//if only one device is in range: connect directly to it
            		if(mDeviceAdapter.getCount()== 1){
            			if(mDeviceAdapter.getItem(0) != null){
            				if(mDeviceAdapter.getItem(0).getAddress() != null){
            					if(mBtAdapter.isDiscovering()){
            						mBtAdapter.cancelDiscovery();
            					}
            					//BluetoothService.getInstance().connect(mDeviceAdapter.getItem(0).getAddress());
            				}
            			}
            		}
            	}
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
            	Log.e("BluetoothDeviceListFragment", "ACTION_DISCOVERY_STARTED");
            	textProgress.setVisibility(View.VISIBLE);
            	progressBar.setVisibility(View.VISIBLE);
            }
        }
    };

















    
    
}