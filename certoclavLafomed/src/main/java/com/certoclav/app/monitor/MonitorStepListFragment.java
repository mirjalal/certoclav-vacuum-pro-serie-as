package com.certoclav.app.monitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.certoclav.app.R;
import com.certoclav.app.database.Profile;
import com.certoclav.app.listener.AutoclaveStateListener;
import com.certoclav.app.listener.ProfileListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;

public class MonitorStepListFragment extends Fragment implements ProfileListener, AutoclaveStateListener {


    private ListView list;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.monitor_list_fragment, container, false); //je nach mIten könnte man hier anderen Inhalt laden.


        return rootView;
    }


    @Override
    public void onResume() {


        Autoclave.getInstance().setOnProfileListener(this);
        Autoclave.getInstance().setOnAutoclaveStateListener(this);

        super.onResume();


    }


    @Override
    public void onStop() {

        Autoclave.getInstance().removeOnProfileListener(this);
        Autoclave.getInstance().removeOnAutoclaveStateListener(this);
        super.onStop();
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public void onAutoclaveStateChange(AutoclaveState state) {
        switch (state) {
            case PREPARE_TO_RUN:
                break;
            case NOT_RUNNING:
                break;
            case RUNNING:
                break;
            case PROGRAM_FINISHED:
                break;
            case RUN_CANCELED:
                break;
            case WAITING_FOR_CONFIRMATION:
                break;
            case LOCKED:
                break;
            default:
                break;
        }


    }


    @Override
    public void onProfileChange(Profile profile) {
        // TODO Auto-generated method stub

    }


}

