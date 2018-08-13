package com.certoclav.app.menu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.adapters.ProgramAdapter;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.DeletedProfileModel;
import com.certoclav.app.database.Profile;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.MyCallback;
import com.certoclav.app.util.ProfileSyncedListener;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.DeleteTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class SterilisationFragment extends Fragment implements ProfileSyncedListener {


    private GridView programGrid; //static damit programGrid nach dem Casten von Fragment auf SterilisationFragment nicht == null ist.

    private ProgramAdapter programAdapter;
    private DatabaseService db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("SterilisationFragment", "oncreate");
        View rootView = inflater.inflate(R.layout.menu_fragment_sterilisation, container, false); //je nach mIten k?nnte man hier anderen Inhalt laden.
        Log.e("SterilsationFragment", "hole programGrid aus xml datei");
        programGrid = (GridView) rootView.findViewById(R.id.sterilisation_grid);
        if (programGrid == null) {
            Log.e("SterilsationFragment", "programGrid == null nach dem holen");


        }

        //DatabaseService db = new DatabaseService(getActivity());
        ArrayList<Profile> profiles = Autoclave.getInstance().getProfilesFromAutoclave();
        Log.e("SterilisationFragment", "nach db anfrage");

        if (profiles != null) {
            programAdapter = new ProgramAdapter(getActivity(), profiles, this);
            programGrid.setAdapter(programAdapter);
        }
        return rootView;
    }


    @Override
    public void onResume() {
        Autoclave.getInstance().setOnProfileSyncedListener(this);
        if (AppConstants.isIoSimulated) {
            ArrayList<Profile> profiles = Autoclave.getInstance().getProfilesFromAutoclave();
            if (profiles != null) {
                programAdapter = new ProgramAdapter(getActivity(), profiles, this);
                programGrid.setAdapter(programAdapter);
            }
        }

        ((ProgramAdapter) programGrid.getAdapter()).notifyDataSetChanged();
        super.onResume();


    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DatabaseService.getInstance();
    }

    public void onLongClick(View view) {
        registerForContextMenu(view);
        if (!(view instanceof CardView))
            view.showContextMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        Autoclave.getInstance().removeOnProfileSyncedListener(this);
    }

    final int CONTEXT_MENU_EDIT = 2;
    final int CONTEXT_MENU_DELETE = 3;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View
            v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.removeGroup((Integer) v.getTag());
        menu.add((Integer) v.getTag(), CONTEXT_MENU_EDIT, Menu.NONE, getString(R.string.edit));
        menu.add((Integer) v.getTag(), CONTEXT_MENU_DELETE, Menu.NONE, getString(R.string.delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case CONTEXT_MENU_EDIT: {
                // Edit Action
                Intent intent = new Intent(getActivity(), EditProgramActivity.class);
                intent.putExtra(AppConstants.INTENT_EXTRA_PROFILE_ID, programAdapter.getItem(item.getGroupId()).getIndex());
                getActivity().startActivityForResult(intent, MenuMain.REQUEST_PROGRAM_EDIT);

            }
            break;
            case CONTEXT_MENU_DELETE: {
                Profile profile = programAdapter.getItem(item.getGroupId());

                profile.setName(AppConstants.DELETED_PROFILE_NAME);
                if (profile.getCloudId() != null && profile.getCloudId().length() > 0) {
//                    db.insertDeletedProfile(new DeletedProfileModel(profile.getCloudId()));
//                    new DeleteTask().execute(CertocloudConstants.SERVER_URL + CertocloudConstants.REST_API_DELETE_PROFILE + profile.getCloudId());
                }
                Helper.setProgram(getContext(), profile, new MyCallback() {
                    @Override
                    public void onSuccess(Object response, int requestId) {
//                        programGrid.setVisibility(View.GONE);
                        Helper.getPrograms(getActivity());
//                        programGrid.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(ErrorModel error, int requestId) {

                    }

                    @Override
                    public void onStart(int requestId) {

                    }

                    @Override
                    public void onProgress(int current, int max) {

                    }
                });
//                programGrid.setVisibility(programAdapter.getCount() > 0 ? View.VISIBLE : View.GONE);
            }
            break;
        }

        return false;
    }


    @Override
    public void onProfileSynced() {
        if (programAdapter != null)
            programAdapter.notifyDataSetChanged();
    }
}

