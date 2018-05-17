package com.certoclav.app.menu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.adapters.ProgramAdapter;
import com.certoclav.app.database.Profile;
import com.certoclav.app.model.Autoclave;

import java.util.ArrayList;

public class SterilisationFragment extends Fragment {


    private GridView programGrid; //static damit programGrid nach dem Casten von Fragment auf SterilisationFragment nicht == null ist.

    private ProgramAdapter programAdapter;

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
            programAdapter = new ProgramAdapter(getActivity(), profiles);
            programGrid.setAdapter(programAdapter);
        }
        return rootView;
    }


    @Override
    public void onResume() {

        if (AppConstants.isIoSimulated) {
            ArrayList<Profile> profiles = Autoclave.getInstance().getProfilesFromAutoclave();
            if (profiles != null) {
                programAdapter = new ProgramAdapter(getActivity(), profiles);
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

    }


}

