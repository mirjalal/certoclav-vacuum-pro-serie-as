package com.certoclav.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.button.QuickActionItem;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.User;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;

import java.util.ArrayList;
import java.util.List;


/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 */
public class UserAdapter extends ArrayAdapter<User> {

    public interface OnClickButtonListener {
        void onClickButtonDelete(User user);

        void onClickButtonEdit(User user);
    }

    ArrayList<OnClickButtonListener> onClickButtonListeners = new ArrayList<OnClickButtonListener>();

    public void setOnClickButtonListener(OnClickButtonListener listener) {
        onClickButtonListeners.add(listener);
    }

    public void removeOnClickButtonListener(OnClickButtonListener listener) {
        onClickButtonListeners.remove(listener);
    }

    private final Context mContext;
    private QuickActionItem actionItemDelete;
    private QuickActionItem actionItemEdit;


    /**
     * Constructor
     *
     * @param context context of calling activity
     * @param values  {@link List}<{@link Profile}> containing the data to populate the list
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


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //	if(convertView == null){
        convertView = inflater.inflate(R.layout.user_list_row, parent, false);
        LinearLayout containerItems = (LinearLayout) convertView.findViewById(R.id.user_list_element_container_button);


        actionItemEdit = (QuickActionItem) inflater.inflate(R.layout.quickaction_item, containerItems, false);
        actionItemDelete = (QuickActionItem) inflater.inflate(R.layout.quickaction_item, containerItems, false);

        if (Autoclave.getInstance().getUser() != null && Autoclave.getInstance().getState() != AutoclaveState.LOCKED && getItem(position).getEmail().equals(Autoclave.getInstance().getUser().getEmail())) {
            if (getItem(position).getIsLocal() || Autoclave.getInstance().isOnlineMode(mContext))
                containerItems.addView(actionItemEdit);
            containerItems.addView(actionItemDelete);
        }
            /*if(Autoclave.getInstance().getUser() != null){
                if(Autoclave.getInstance().getUser().getEmail().equals(getItem(position).getEmail()) && Autoclave.getInstance().getState() != AutoclaveState.WAITING_FOR_LOGIN){
					actionItemEdit.setVisibility(View.VISIBLE);
					actionItemDelete.setVisibility(View.VISIBLE);
				}else{
					actionItemEdit.setVisibility(View.INVISIBLE);
					actionItemDelete.setVisibility(View.INVISIBLE);
				}
			}else{
				actionItemEdit.setVisibility(View.INVISIBLE);
				actionItemDelete.setVisibility(View.INVISIBLE);
			}*/


        TextView firstLine = (TextView) convertView.findViewById(R.id.first_line);
        firstLine.setText(getItem(position).getEmail());

        final TextView secondLine = (TextView) convertView.findViewById(R.id.second_line);
        secondLine.setText(getItem(position).getIsLocal() ? "Local Account" : "Online Account");


        actionItemDelete.setChecked(false);
        actionItemDelete.setImageResource(R.drawable.btn_remove);

        //actionItemDelete.setText(getContext().getString(R.string.delete));
        actionItemDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (OnClickButtonListener listener : onClickButtonListeners) {
                    listener.onClickButtonDelete(getItem(position));
                }


            }
        });


        actionItemEdit.setChecked(false);
        actionItemEdit.setImageResource(R.drawable.ic_menu_edit);

        //actionItemEdit.setText(getContext().getString(R.string.edit));
        actionItemEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (OnClickButtonListener listener : onClickButtonListeners) {
                    listener.onClickButtonEdit(getItem(position));
                }


            }
        });

        return convertView;
    }
}