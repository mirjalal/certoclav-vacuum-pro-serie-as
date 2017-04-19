/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.certoclav.library.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

@SuppressLint("NewApi")
public class BluetoothService   {
	
	public final Object mLock = new Object();//Object des Threads f�r die syncronized-Bl�cke
    // Debugging
    private final String TAG = "Bluetooth Service";
    
    // Unique UUID for this applicationo
    private final UUID UUID_OTHER_DEVICE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    // Member fields
    private ConnectTask mConnectTask;
 

	private int mState = BluetoothState.STATE_DISCONNECTED;
    private BluetoothSocket mSocket = null;
    private BluetoothDevice mDevice = null;
    private static BluetoothService mBluetoothService = null;
    
    public BluetoothDevice getDevice() {
		return mDevice;
	}

	public static BluetoothService getInstance() {
		if(mBluetoothService == null){
			mBluetoothService = new BluetoothService();
		}
		return mBluetoothService;
	}




	private InputStream mInStream = null;
    private OutputStream mOutStream = null;
    private BluetoothConnectionStateListener mBluetoothConnectionStateListener = null;
   
    
    public interface BluetoothConnectionStateListener {
        public void onChangedBluetoothConnectionState(int state,String address);
    }
        
	//public final Object mLock = new Object();//Object des Threads f�r die syncronized-Bl�cke
	
    // Constructor
    public BluetoothService(){
        setState(BluetoothState.STATE_DISCONNECTED);        
    }
    


	public void setOnBluetoothConnectionStateListener(BluetoothConnectionStateListener listener){
    	mBluetoothConnectionStateListener = listener;
    }

    
    // Set the current state of the chat connection
    // state : An integer defining the current connection state
    private void setState(int state) {
    		
        Log.d(TAG, "setState() " + mState + " -> " + state);
        if(state == BluetoothState.STATE_CONNECTED){
        	Log.e("BluetoothService", "STATE_CONNECTED");
        }else if(state == BluetoothState.STATE_DISCONNECTED){
        	Log.e("BluetoothService", "STATE_DISCONNECTED");
        }else if(state == BluetoothState.STATE_CONNECTING){
        	Log.e("BluetoothService", "STATE_CONNECTING");
  
        }
        
        mState = state;
        if(mBluetoothConnectionStateListener != null){
        	if(mDevice != null){
        		mBluetoothConnectionStateListener.onChangedBluetoothConnectionState(state,mDevice.getAddress());
        	}
        }


    }
 

	// Return the current connection state. 
    public int getState() {
        return mState;
    }



    // Start the ConnectThread to initiate a connection to a remote device
    // device : The BluetoothDevice to connect
    // secure : Socket Security type - Secure (true) , Insecure (false)
    public void connect(BluetoothDevice device) {// Funktioniert bei synchronized, aber dauert sehr lange bis zum ersten erfolgreichen connect

    	mDevice = device; //save device for the autoconnection thread
       
    	if(device != null){
    		// Cancel any thread attempting to make a connection
    		if (mConnectTask != null) {
           		mConnectTask.cancel(true);
           		mConnectTask = null;
        	}
            
        	if (mInStream != null) {
            	try {mInStream.close();} catch (Exception e) {}
            	mInStream = null;
        	}

        	if (mOutStream != null) {
        			try {mOutStream.close();} catch (Exception e) {}
                	mOutStream = null;
        	}

        	if (mSocket != null) {
                	try {mSocket.close();} catch (Exception e) {}
                	mSocket = null;
        	}
        	setState(BluetoothState.STATE_DISCONNECTED);
        

        	mConnectTask = new ConnectTask(device);
        	mConnectTask.execute();
        	setState(BluetoothState.STATE_CONNECTING);
    	
    	}

    }



 

    



    // This thread runs while attempting to make an outgoing connection
    // with a device. It runs straight through
    // the connection either succeeds or fails
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mmSocket = null;
        private BluetoothDevice mmDevice = null; //remember device for the case of recconection

        public ConnectTask(BluetoothDevice device) {
            mmDevice = device;
        }




		@Override
		protected Boolean doInBackground(Void... params) {
			//setState(BluetoothState.STATE_CONNECTING);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			 			Log.e("BluetoothService","ConnectTask execution start");
    			// Get a BluetoothSocket for a connection with the
    			// given BluetoothDevice
    			try {
    				if(mmDevice != null){
    					mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID_OTHER_DEVICE);
    					//mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(UUID_OTHER_DEVICE);
    				}
    			} catch (IOException e) {
    				Log.e("BluetoothService", "Error creating bluetooth socket");
    			}
    			
    			
    			if(mmSocket != null){

    				// Always cancel discovery because it will slow down a connection
    				BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

    				// Make a connection to the BluetoothSocket
    				try {
    					// This is a blocking call and will only return on a
    					// successful connection or an exception
    					mmSocket.connect();
    				} catch (Exception e) {
    						Log.e("ConnectTask", "connection failed");
    						return false;
    				
            		
    				}
    		
    				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
    				if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
    					if(mmSocket.isConnected()){ //available > API 14
    						return true;
    					}
    				} else{
    				    return true;
    				}
    				
    				
    			
    			}else{
    				Log.e("BluetoothService", "mmSocket == null");
    			}
    		//}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Log.e("BluetoothService","ConnectTask OnPostExecute: " + Boolean.toString(result));
			// TODO Auto-generated method stub
			if(result){
				
				//initInAndOutStream(mmSocket, mmDevice);
		    	mSocket = mmSocket;
		    	
		    	
		    	  
		    	if(mSocket != null){
		     
		            InputStream tmpIn = null;
		            OutputStream tmpOut = null;

		            // Get the BluetoothSocket input and output streams
		            try {
		                tmpIn = mSocket.getInputStream();
		                tmpOut = mSocket.getOutputStream();
		            } catch (IOException e) {setState(BluetoothState.STATE_DISCONNECTED); }

		            if(tmpIn != null && tmpOut != null){
		            	mInStream = tmpIn;
		            	mOutStream = tmpOut;
		            	setState(BluetoothState.STATE_CONNECTED);
		            }else{
		            	setState(BluetoothState.STATE_DISCONNECTED);
		            }
		   
		    	}else{
		    		setState(BluetoothState.STATE_DISCONNECTED);
		    	}
			}else{
				setState(BluetoothState.STATE_DISCONNECTED);
					try {
						if(mmSocket != null){
							mmSocket.close();
							mmSocket = null;
						} 
					}catch (Exception e2) { 
					}
			}
			
		}

        
        
		@Override
		protected void onCancelled() {
			super.onCancelled();
			Log.e("ConnectTask", "onCancelled called");
        	if(mmSocket != null){
        		try {
        			mmSocket.close();
        			mmDevice = null;
        			mmSocket = null;
        		} catch (Exception e) {
        			Log.e("ConnectTask", "exception during cancel");
        		}
        	}
			
		}


        
        
        
    }


    

 
    	/**
    	 * Transmit a data to the AndrodpodInterface, autoflushing
    	 * 
    	 * @param onByte
    	 *            One byte to transmit
    	 */
    	public boolean write(int oneByte) {
    		byte buffer[] = { (byte) oneByte };
    		return this.write(buffer, 0, buffer.length);
    	}

    	/**
    	 * Transmit a data to the AndrodpodInterface, autoflushing
    	 * 
    	 * @param buffer
    	 *            Buffer to write to the interface, full length of the array
    	 *            will be transmitted
    	 */
    	public boolean write(byte[] buffer) {
    		return this.write(buffer, 0, buffer.length);
    	}


        
    	/**
    	 * Transmit a data to the AndrodpodInterface, autoflushing
    	 * 
    	 * @param buffer
    	 *            Buffer to write to the interface
    	 * @param offset
    	 *            Offset to start writing
    	 * @param length
    	 *            Length to write
    	 */
    	public boolean write(byte[] buffer, int offset, int length) {
            if (mState != BluetoothState.STATE_CONNECTED) {
            	//Log.e("BluetoothService", "writing failed - because not connected");
            	return false;
            }
            
				
			
    		if(this.mOutStream!=null){
   			synchronized (this.mOutStream) {	
    				try {
    				
            	
    					mOutStream.write(buffer, offset, length);
    					mOutStream.flush();
    					return true;
    				} catch (IOException e) {
    					}
    			}
            }
    	return false;
    	}

    	/**
    	 * Receive data from the Andropod Interface
    	 * 
    	 * @param buffer
    	 *            Buffer to write to (the entire array will be filled)
    	 * @return Received packet
    	 */
    	public int read(byte[] buffer) {
    		return this.read(buffer, 0, buffer.length);
    	}

    	/**
    	 * Receive data from the Andropod Interface
    	 * 
    	 * @param buffer
    	 *            Buffer to write to
    	 * @param offset
    	 *            offset to start reading
    	 * @param length
    	 *            Length to read
    	 * @return Received packet
    	 */
    	public int read(byte[] buffer, int offset, int length) {
    		int read = -1;

    	if(mState != BluetoothState.STATE_CONNECTED){
    	//	Log.e("BluetoothSerice", "read failed - because not connected");
    		return -1;
    	}
    	synchronized (this.mInStream) {	
    			try {
    				int counter = 0;
    			    while (true)
    			    {
    			        int available = mInStream.available();
    			        if (available > 0) { break; }
    			        Thread.sleep(1);
    			        counter++;
    			        if(counter>1000){
    			        	return -1;
    			        }
    			        // here you can optionally check elapsed time, and time out
    			    }
    				//	ReadTimeoutThread readTimeOutThread = new ReadTimeoutThread();
    				//	readTimeOutThread.start();
    					read = mInStream.read(buffer, offset, length);
    				//	readTimeOutThread.resetAndCancelCounterThread();
    					if (read < 0) {
    						Log.e(TAG, " read error " + " bluetoothservice read() no result");
    					}
    				
    			} catch (Exception e) {
    				Log.e("exception during read: !! ", e.toString() + e.getMessage());
    				setState(BluetoothState.STATE_DISCONNECTED);
    				return -1;
    				
                  //  connectionLost();
                    // Start the service over to restart listening mode
                   // BluetoothService.this.start(BluetoothService.this.isAndroid);

    			}	
    	}
    		return read;

    	}
        

 

        
        /*
         * 
         * Call in order to disconnect from all Bluetooth devices
         */
        public void reset() {
    		if (mConnectTask != null) {
           		mConnectTask.cancel(true);
           		mConnectTask = null;
        	}
            
        	if (mInStream != null) {
            	try {mInStream.close();} catch (Exception e) {}
            	mInStream = null;
        	}

        	if (mOutStream != null) {
        			try {mOutStream.close();} catch (Exception e) {}
                	mOutStream = null;
        	}

        	if (mSocket != null) {
                	try {mSocket.close();} catch (Exception e) {}
                	mSocket = null; 
        	}
        	setState(BluetoothState.STATE_DISCONNECTED);
        }

		public void connect(String address) {
			connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
			
		}



		public String getCurrentBluetoothAddress() {
			if(mDevice !=  null){
				return mDevice.getAddress();
			}
			return "";
			
		}

 
 
		/*
		 * returns true, if device has hardware for bluetooth
		 */
		public boolean isBluetoothAvailable(){
			if(BluetoothAdapter.getDefaultAdapter() == null){
				return false;
			}
			return true;
		}


		public boolean setBluetooth(boolean enable) {
			try{
		    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		    boolean isEnabled = bluetoothAdapter.isEnabled();
		    if (enable && !isEnabled) {
		        return bluetoothAdapter.enable(); 
		    }
		    else if(!enable && isEnabled) {
		        return bluetoothAdapter.disable();
		    }
		    // No need to change bluetooth state
		    return true;
			}catch(Exception e){
				return false;
			}
		} 

		public void unpairAllDevices() {
			try {
				reset();
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				for(BluetoothDevice device : pairedDevices){
					   Method m = device.getClass()
						        .getMethod("removeBond", (Class[]) null);
						    m.invoke(device, (Object[]) null);
				}
			} catch (Exception e) {
			    Log.e(TAG, e.getMessage());
			}
			}



    }
