package com.certoclav.library.bluetooth.chooser;

import java.util.List;

import android.content.Context;
import android.provider.ContactsContract.Profile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.certoclav.library.R;
import com.certoclav.library.bluetooth.BluetoothState;




/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 * 
*/
public class BluetoothDeviceElementAdapter extends ArrayAdapter<BluetoothDeviceElement> {
	private final Context mContext;


	/**
	 * Constructor
	 * 
	 * @param context
	 *            context of calling activity
	 * @param values
	 * {@link List}<{@link Profile}> containing the data to populate the list
	 */
	public BluetoothDeviceElementAdapter(Context context, List<BluetoothDeviceElement> values) {
 
		super(context, R.layout.bluetooth_device_list_fragment_element, values);
		this.mContext = context;

	 
	}

	/**
	 * Gets a View that displays the data at the specified position in the data
	 * set.The View is inflated it from profile_list_row XML layout file
	 * 
	 * @see Adapter#getView(int, View, ViewGroup)
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
	
		if(convertView == null){
			final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.bluetooth_device_list_fragment_element, parent, false);
		}


		final TextView firstLine = (TextView) convertView.findViewById(R.id.sterilisation_element_text_firstline);
		firstLine.setText(getItem(position).getName());
		//convertView.findViewById(R.id.sterilisation_element_container_start).setVisibility(View.INVISIBLE);	
		//Toast.makeText(convertView.getContext(), "signal: " + getItem(position).getRssi(), Toast.LENGTH_SHORT).show();
		final ImageView imageSignalstrength = (ImageView) convertView.findViewById(R.id.bluetooth_element_image_signal);
		if(  -getItem(position).getRssi() > 60  ){
		imageSignalstrength.setImageResource(R.drawable.sigbar4);
		}else if(-getItem(position).getRssi() > 50){
			imageSignalstrength.setImageResource(R.drawable.sigbar3);	
		}else if(-getItem(position).getRssi() > 10){
			imageSignalstrength.setImageResource(R.drawable.sigbar2);
		}else if(-getItem(position).getRssi() > 0){
			imageSignalstrength.setImageResource(R.drawable.sigbar1);
		}else {
			imageSignalstrength.setImageResource(R.drawable.sigbar0);
		}
	
		final TextView textStatus = (TextView) convertView.findViewById(R.id.bluetooth_element_text_secondline);
	
		
		
		switch(getItem(position).getStatus()){
		case BluetoothState.STATE_CONNECTED:
			textStatus.setText(R.string.connected);
			break;
		case BluetoothState.STATE_DISCONNECTED:
			textStatus.setText(R.string.not_connected);
			break;
		case BluetoothState.STATE_CONNECTING:
			textStatus.setText(R.string.trying_to_connect_);
			break;
		}


		return convertView;
	}






}