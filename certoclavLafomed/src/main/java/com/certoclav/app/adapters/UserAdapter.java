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

    private ArrayList<OnClickButtonListener> onClickButtonListeners = new ArrayList<OnClickButtonListener>();

    public void setOnClickButtonListener(OnClickButtonListener listener) {
        onClickButtonListeners.add(listener);
    }

    public void removeOnClickButtonListener(OnClickButtonListener listener) {
        onClickButtonListeners.remove(listener);
    }

    private final Context mContext;
    private boolean isLocked = false;

    private final User loggedInUser = Autoclave.getInstance().getUser();
    
    /**
     * Constructor
     *
     * @param context context of calling activity
     * @param values  {@link List}<{@link Profile}> containing the data to populate the list
     */
    public UserAdapter(Context context, List<User> values) {
        super(context, R.layout.user_list_row, values);
        this.mContext = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if ((!loggedInUser.isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
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
        LinearLayout containerItems = convertView.findViewById(R.id.user_list_element_container_button);

        QuickActionItem actionItemEdit = (QuickActionItem) inflater.inflate(R.layout.quickaction_item, containerItems, false);
        QuickActionItem actionItemDelete = (QuickActionItem) inflater.inflate(R.layout.quickaction_item, containerItems, false);

        final User currentUserItem = getItem(position);

        if (loggedInUser != null && Autoclave.getInstance().getState() != AutoclaveState.LOCKED) {
            containerItems.addView(actionItemEdit);
            containerItems.addView(actionItemDelete);
        }

        TextView firstLine = convertView.findViewById(R.id.first_line);
        firstLine.setText(currentUserItem.getEmail());

        final TextView secondLine = convertView.findViewById(R.id.second_line);
        secondLine.setText(currentUserItem.getIsLocal() ? mContext.getString(R.string.local_account) : mContext.getString(R.string.online_account));

        // according to USERS.xlsx file
        boolean deleteButtonVisibility = false;
        if (!Autoclave.getInstance().isOnlineMode(getContext())) { // offline mode
            if (loggedInUser.getIsDefaultAdmin())
                deleteButtonVisibility = !currentUserItem.getIsDefaultAdmin();

            if (loggedInUser.getIsUserAdmin())
                deleteButtonVisibility = !currentUserItem.getIsDefaultAdmin();
        } else { // online mode
            if (loggedInUser.getIsDefaultAdmin() ||
                loggedInUser.getIsUserAdmin())
                deleteButtonVisibility = true;
        }

        actionItemDelete.setChecked(false);
        actionItemDelete.setImageResource(R.drawable.btn_remove);
        actionItemDelete.setVisibility(deleteButtonVisibility ? View.VISIBLE : View.GONE);
        actionItemDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (OnClickButtonListener listener : onClickButtonListeners) {
                    listener.onClickButtonDelete(currentUserItem);
                }
            }
        });

        boolean editButtonVisibility = false;
        // according to USERS.xlsx file
        if (!Autoclave.getInstance().isOnlineMode(getContext())) { // offline mode
            if (loggedInUser.getIsDefaultAdmin())
                editButtonVisibility = !currentUserItem.getIsDefaultAdmin();

            if (loggedInUser.getIsUserAdmin())
                editButtonVisibility = !currentUserItem.getIsDefaultAdmin();

            if (loggedInUser.getIsNormalLocalUser())
                editButtonVisibility = loggedInUser.getUserId() == currentUserItem.getUserId();
        } else { // online mode
            // nothing to do here, edit button has already been disabled
        }

        actionItemEdit.setChecked(false);
        actionItemEdit.setImageResource(R.drawable.ic_menu_edit);
        actionItemEdit.setVisibility(editButtonVisibility ? View.VISIBLE : View.GONE);
        actionItemEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (OnClickButtonListener listener : onClickButtonListeners)
                    listener.onClickButtonEdit(currentUserItem);
            }
        });

        return convertView;
    }
}