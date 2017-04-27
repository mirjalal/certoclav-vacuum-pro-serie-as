package com.certoclav.app.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.button.QuickActionItem;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.User;




/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 * 
*/
public class UserAdapter extends ArrayAdapter<User> {
	
	public interface OnClickButtonListener {
		 void onClickButtonDelete(User user);
		}
	ArrayList<OnClickButtonListener> onClickButtonListeners = new ArrayList<OnClickButtonListener>();
	
	public void setOnClickButtonListener(OnClickButtonListener listener){
		onClickButtonListeners.add(listener);
	}
	public void removeOnClickButtonListener(OnClickButtonListener listener){
		onClickButtonListeners.remove(listener);
	}
	
	private final Context mContext;
	private QuickActionItem actionItemUnlink;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            context of calling activity
	 * @param values
	 * {@link List}<{@link Profile}> containing the data to populate the list
	 */
	public UserAdapter(Context context, List<User> values) {
		super(context, R.layout.user_list_row, values);
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
		
		User user = getItem(position);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.user_list_row, parent, false);
		

		TextView firstLine = (TextView) convertView.findViewById(R.id.first_line);
		firstLine.setText(user.getEmail());

		final TextView secondLine = (TextView) convertView.findViewById(R.id.second_line);
		secondLine.setText(user.getFirstName()+" "+user.getLastName());
	
		convertView.findViewById(R.id.buttonDelete).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				for(OnClickButtonListener listener : onClickButtonListeners){
					listener.onClickButtonDelete(getItem(position));
				}
			}
		});
		

		return convertView;
	}
}