package com.certoclav.app.settings;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.adapters.UserAdapter;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.menu.LoginActivity;
import com.certoclav.app.menu.RegisterActivity;
import com.certoclav.app.menu.RegisterCloudAccountActivity;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A fragment representing a single Item detail screen. This fragment is
 * contained in a {@link SettingsActivity} in two-pane mode (on tablets)
 */
public class UserEditFragment extends Fragment implements UserAdapter.OnClickButtonListener {
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

        DatabaseService db = DatabaseService.getInstance();
        adapter.clear();
        for (User user : db.getUsers()) {
            adapter.add(user);
        }
        adapter.notifyDataSetChanged();


        super.onResume();
    }

    @Override
    public void onPause() {
        adapter.removeOnClickButtonListener(this);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_user_edit, container, false); //je nach mIten k�nnte man hier anderen Inhalt laden.

        listview = (ListView) rootView.findViewById(R.id.settings_user_edit_list);
        DatabaseService db = DatabaseService.getInstance();
        List<User> usersVisible = db.getUsers();
        if (usersVisible != null) {
            adapter = new UserAdapter(getActivity(), usersVisible);
            listview.setAdapter(adapter);
        }


        return rootView;
    }


    @Override
    public void onClickButtonDelete(final User user) {

        try {

            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.delete_user))
                    .setContentText(getActivity().getString(R.string.do_you_really_want_to_delete) + " " + user.getEmail())
                    .setConfirmText(getString(R.string.yes))
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            DatabaseService db = DatabaseService.getInstance();
                            db.deleteUser(user);
                            adapter.remove(user);
                            adapter.notifyDataSetChanged();
                            Autoclave.getInstance().setState(AutoclaveState.LOCKED);
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
            sweetAlertDialog.setCanceledOnTouchOutside(true);
            sweetAlertDialog.setCancelable(true);
            sweetAlertDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onClickButtonEdit(User user) {
        Intent intent = new Intent(getContext(), !user.getIsLocal() ?
                RegisterCloudAccountActivity.class
                : RegisterActivity.class);
        intent.putExtra(AppConstants.INTENT_EXTRA_USER_ID, user.getUserId());
        startActivity(intent);
        getActivity().finish();
    }


}

