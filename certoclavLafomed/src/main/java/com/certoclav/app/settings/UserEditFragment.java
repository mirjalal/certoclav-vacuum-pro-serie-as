package com.certoclav.app.settings;


import java.util.List;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.adapters.UserAdapter;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.listener.DatabaseRefreshedListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;



/**
 * A fragment representing a single Item detail screen. This fragment is
 * contained in a {@link SettingsActivity} in two-pane mode (on tablets) 
 */
public class UserEditFragment extends Fragment implements  DatabaseRefreshedListener , UserAdapter.OnClickButtonListener {
	  private UserAdapter adapter;
	  private ListView listview;
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public UserEditFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	

	@Override
	public void onResume() {
		adapter.setOnClickButtonListener(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		adapter.removeOnClickButtonListener(this);
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.settings_user_edit,container, false); //je nach mIten könnte man hier anderen Inhalt laden.
	
		listview = (ListView) rootView.findViewById(R.id.settings_user_edit_list);
		DatabaseService db = new DatabaseService(getActivity());
		List<User> usersVisible = db.getUsers();
		if(usersVisible != null){
		    adapter = new UserAdapter(getActivity(),usersVisible);
		    listview.setAdapter(adapter);
		}
	    





		return rootView;
	}





	@Override
	public void onRefreshedUsers(boolean success) {//
		Log.e("UserEditFragment", "onrefreshedusers called");
		DatabaseService db = new DatabaseService(getActivity());
		adapter.clear();
		for(User user : db.getUsers()){
			adapter.add(user);
		}
		adapter.notifyDataSetChanged();
		
	}



	@Override
	public void onClickButtonDelete(final User user) {
		if(Autoclave.getInstance().getState() == AutoclaveState.LOCKED){
			try 
		    {

		         
		         
					final Dialog dialog = new Dialog(getActivity());
					dialog.setContentView(R.layout.dialog_yes_no);
					dialog.setTitle(getActivity().getString(R.string.delete_user));
		 
					// set the custom dialog components - text, image and button
					TextView text = (TextView) dialog.findViewById(R.id.text);
					text.setText(getActivity().getString(R.string.do_you_really_want_to_delete_) + user.getEmail());
		            Button dialogButtonNo = (Button) dialog.findViewById(R.id.dialogButtonNO);
		            dialogButtonNo.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
					// if button is clicked, close the custom dialog
					dialogButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							DatabaseService db = new DatabaseService(getActivity());
							db.deleteUser(user);
							adapter.remove(user);
							adapter.notifyDataSetChanged();
							dialog.dismiss();
		
						
						}
					});
		 
					dialog.show();

					
		    } 
		    catch (Exception e) 
		    {
		          e.printStackTrace();
		    }

		}else{
			Toast.makeText(getActivity(), getString(R.string.please_log_out_first), Toast.LENGTH_LONG).show();
		}
		
	}
		
	}

