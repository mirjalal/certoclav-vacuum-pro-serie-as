package com.certoclav.app.adapters;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Message;
import com.certoclav.app.database.Profile;




/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 * 
*/
public class MessagesAdapter extends ArrayAdapter<Message> {
	private final Context mContext;



public interface DataSetChangedListener {
 void onDataSetChanged();
}



	/**
	 * Constructor
	 * 
	 * @param context
	 *            context of calling activity
	 * @param values
	 * {@link List}<{@link Profile}> containing the data to populate the list
	 */
	public MessagesAdapter(Context context, List<Message> values) {
 
		super(context, R.layout.menu_fragment_information_messages_element, values);
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
		
			final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.menu_fragment_information_messages_element, parent, false);
			
			TextView textHead = (TextView) convertView.findViewById(R.id.information_message_element_text_head);
			textHead.setText(getItem(position).getTextHead());
		
			TextView textBody = (TextView) convertView.findViewById(R.id.information_message_element_text_body);
			textBody.setText(getItem(position).getTextBody());
			
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try 
				    {

				         
				         
							final Dialog dialog = new Dialog(mContext);
							dialog.setContentView(R.layout.dialog_ignore_ok);
							dialog.setTitle(getItem(position).getTextHead());
				 
							// set the custom dialog components - text, image and button
							TextView text = (TextView) dialog.findViewById(R.id.text);
							text.setText(getItem(position).getTextBody());
				 
							Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
							// if button is clicked, close the custom dialog
							dialogButton.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									DatabaseService db = new DatabaseService(mContext);
									db.deleteMessage(getItem(position));
									remove(getItem(position));
									notifyDataSetChanged();
									dialog.dismiss();
								}
							});
				 
							Button ignoreButton =(Button) dialog.findViewById(R.id.dialogButtonIgnore);
							ignoreButton.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									dialog.dismiss();
									
								}
							});
							dialog.show();
							
							
				    } 
				    catch (Exception e) 
				    {
				          e.printStackTrace();
				    }
					
				}
			});
		


        


		return convertView;
	}
}