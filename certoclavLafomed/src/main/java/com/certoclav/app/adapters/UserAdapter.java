package com.certoclav.app.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.button.QuickActionItem;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.User;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.library.application.ApplicationController;

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
    private SharedPreferences prefs;
    private boolean isLocked = false;

    /**
     * Constructor
     *
     * @param context context of calling activity
     * @param values  {@link List}<{@link Profile}> containing the data to populate the list
     */
    public UserAdapter(Context context, List<User> values) {
        super(context, R.layout.user_list_row, values);
        this.mContext = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                prefs.getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_user_account),
                        ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_user_account))) {
            Toast.makeText(mContext, R.string.these_settings_are_locked_by_the_admin, Toast.LENGTH_SHORT).show();
            isLocked = true;
        } else {
            isLocked = false;
        }
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

        if (!isLocked)
            if (Autoclave.getInstance().getUser() != null && Autoclave.getInstance().getState() != AutoclaveState.LOCKED
                    && (getItem(position).getEmail().equals(Autoclave.getInstance().getUser().getEmail())
                    || Autoclave.getInstance().getUser().isAdmin())) {
                if ((getItem(position).getIsLocal() || Autoclave.getInstance().isOnlineMode(mContext)))
                    if (!(getItem(position).getEmail().equalsIgnoreCase("admin") &&
                            !Autoclave.getInstance().getUser().getEmail().equalsIgnoreCase("admin")))
                        containerItems.addView(actionItemEdit);

                if (!getItem(position).getEmail().equalsIgnoreCase("admin"))
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
        secondLine.setText(getItem(position).getIsLocal() ? mContext.getString(R.string.local_account) : mContext.getString(R.string.online_account));

        actionItemDelete.setChecked(false);
        actionItemDelete.setImageResource(R.drawable.btn_remove);
        actionItemDelete.setVisibility(
            Autoclave.getInstance().getUser().isAdmin() ? View.VISIBLE : View.GONE
        );
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