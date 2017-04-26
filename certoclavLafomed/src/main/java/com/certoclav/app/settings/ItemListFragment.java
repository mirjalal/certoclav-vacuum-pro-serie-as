package com.certoclav.app.settings;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.adapters.SettingsAdapter;
import com.certoclav.app.model.SettingItem;


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
		public void onItemSelected(long id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(long id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
	//	setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
		//		android.R.layout.simple_list_item_activated_1,
		//		android.R.id.text1, DummyContent.ITEMS));

	
	

	
	
	


	
		// TODO: replace with a real list adapter.

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
	//	getListView().setDivider(ContextCompat.getDrawable(getActivity(),R.drawable.list_divider));
		// Restore the previously serialized activated item position.
		
		adapter = new SettingsAdapter(getActivity(),list);//,android.R.id.text1);
		setListAdapter(adapter);
        
		AddItem(getListView(), getActivity().getString(R.string.settings_user_account), R.drawable.ic_account_settings);
		AddItem(getListView(), getActivity().getString(R.string.settings_network), R.drawable.ic_network_settings);
		AddItem(getListView(), getActivity().getString(R.string.settings_sterilization), R.drawable.ic_sterilization_settings);
		AddItem(getListView(), getActivity().getString(R.string.settings_device), R.drawable.ic_device_settings);
		AddItem(getListView(), getActivity().getString(R.string.settings_language), R.drawable.ic_language_settings);
		AddItem(getListView(), getActivity().getString(R.string.notifications), R.drawable.ic_notification_settings);
		AddItem(getListView(), getActivity().getString(R.string.calibration), R.drawable.ic_calibartion_settings);
		if(AppConstants.APPLICATION_DEBUGGING_MODE){
			AddItem(getListView(), "Service", R.drawable.ic_service_setttings);
		}
		
		
		
		
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}else {
			mCallbacks.onItemSelected(0);
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
		if(adapter!=null)
			adapter.setSelectedPos(position);
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(id);
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

	public void AddItem(View v, String str, int icon){
		list.add(new SettingItem(str,icon));
		adapter.notifyDataSetChanged();
}
}
