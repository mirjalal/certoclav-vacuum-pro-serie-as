package com.certoclav.app.settings;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.adapters.SettingsAdapter;
import com.certoclav.app.fragments.AuditLogFragment;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.SettingItem;
import com.certoclav.app.util.LockoutManager;

import java.util.ArrayList;


/**
 * A list fragment representing a list of Items. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link }.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ItemListFragment extends ListFragment {

    ArrayList<SettingItem> list = new ArrayList<SettingItem>();
    SettingsAdapter adapter;
    private boolean isAdmin;


    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(long id, Fragment fragment);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long id, Fragment fragment) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemListFragment() {
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        isAdmin = ((SettingsActivity) getActivity()).isAdmin();
        //	getListView().setDivider(ContextCompat.getDrawable(getActivity(),R.drawable.list_divider));
        // Restore the previously serialized activated item position.

        adapter = new SettingsAdapter(getActivity(), list);//,android.R.id.text1);
        setListAdapter(adapter);


        if (!LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.USER_ACCOUNT) || isAdmin)
            AddItem(getListView(), getActivity().getString(R.string.settings_user_account), R.drawable.ic_account_settings, R.drawable.ic_account_settings_selected, new UserEditFragment());

        if (!LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.NETWORK) || isAdmin)
            AddItem(getListView(), getActivity().getString(R.string.settings_network), R.drawable.ic_network_settings, R.drawable.ic_network_settings_selected, new SettingsNetworkFragment());
        if (!LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.LANGUAGE) || isAdmin)
            AddItem(getListView(), getActivity().getString(R.string.settings_language), R.drawable.ic_language_settings, R.drawable.ic_language_settings_selected, new SettingsLanguageFragment());


        //following settings are always visible
        if (Autoclave.getInstance().getUser() != null || isAdmin) {
            if (isAdmin || Autoclave.getInstance().getState() != AutoclaveState.LOCKED) {
                //Always if it is logged in.
                if (isAdmin || !LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.AUTOCLAVE)
                        || Autoclave.getInstance().getUser().isAdmin())
                    AddItem(getListView(), getActivity().getString(R.string.settings_autoclave), R.drawable.ic_service_setttings, R.drawable.ic_service_setttings_selected, new SettingsAutoclaveFragment());

                if (isAdmin || !LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.DEVICE)
                        || Autoclave.getInstance().getUser().isAdmin())
                    AddItem(getListView(), getActivity().getString(R.string.settings_device),
                            R.drawable.ic_device_settings, R.drawable.ic_device_settings_selected,
                            new SettingsDeviceFragment());

                if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(AppConstants.PREFERENCE_KEY_ENABLE_FDA, false)) {
                    if (isAdmin || !LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.AUDIT_LOGS)
                            || Autoclave.getInstance().getUser().isAdmin())
                        AddItem(getListView(), getActivity().getString(R.string.settings_audit_log),
                                R.drawable.ic_audit_logs, R.drawable.ic_audit_logs_selected,
                                new AuditLogFragment());
                }

                if (isAdmin || !LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.NOTIFICATIONS)
                        || Autoclave.getInstance().getUser().isAdmin())
                    AddItem(getListView(), getActivity().getString(R.string.notifications),
                            R.drawable.ic_notification_settings, R.drawable.ic_notification_settings_selected,
                            new SettingsConditionFragment());

                if (isAdmin || !LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.STERILIZATION)
                        || Autoclave.getInstance().getUser().isAdmin())
                    AddItem(getListView(), getActivity().getString(R.string.settings_sterilization),
                            R.drawable.ic_sterilization_settings, R.drawable.ic_sterilization_settings_selceted,
                            new SettingsSterilisationFragment());

                if (isAdmin || !LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.CALIBRATION)
                        || Autoclave.getInstance().getUser().isAdmin())
                    AddItem(getListView(), getActivity().getString(R.string.calibration),
                            R.drawable.ic_calibartion_settings, R.drawable.ic_calibartion_settings_selected,
                            new CalibrateFragment());

                if (isAdmin || Autoclave.getInstance().getUser().isAdmin())
                    AddItem(getListView(), getActivity().getString(R.string.lockout),
                            R.drawable.ic_lock, R.drawable.ic_lock_selected,
                            new SettingsLockoutFragment());
//                if (isAdmin ||!LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.SERVICE)
//                        || Autoclave.getInstance().getUser().isAdmin())
//                    AddItem(getListView(), "Service", R.drawable.ic_service_setttings,
//                            R.drawable.ic_service_setttings_selected,
//                            new SettingsServiceFragment());

                if (isAdmin || !LockoutManager.getInstance().isLocked(LockoutManager.LOCKS.GLP)
                        || Autoclave.getInstance().getUser().isAdmin())
                    AddItem(getListView(), getActivity().getString(R.string.glp),
                            R.drawable.ic_glp, R.drawable.ic_glp_selected,
                            new SettingsGlpFragment());
            }
        }


        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState
                    .getInt(STATE_ACTIVATED_POSITION));
        } else {
            mCallbacks.onItemSelected(0, list.get(0).getFragment());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position,
                                long id) {
        super.onListItemClick(listView, view, position, id);
        if (adapter != null)
            adapter.setSelectedPos(position);
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(id, list.get(position).getFragment());

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }


    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    public void AddItem(View v, String str, int icon, int iconSelected, Fragment fragment) {
        list.add(new SettingItem(str, icon, iconSelected, fragment));
        adapter.notifyDataSetChanged();
    }
}
