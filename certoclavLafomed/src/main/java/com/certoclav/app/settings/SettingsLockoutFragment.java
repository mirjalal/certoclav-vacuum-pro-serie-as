package com.certoclav.app.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.model.Autoclave;
import com.certoclav.library.application.ApplicationController;


public class SettingsLockoutFragment extends PreferenceFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_lockout);

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (!Autoclave.getInstance().getUser().isAdmin() &&
                    prefs.getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_glp),
                            ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_glp))) {
                Toast.makeText(getActivity(), R.string.these_settings_are_locked_by_the_admin, Toast.LENGTH_SHORT).show();
                getPreferenceScreen().setEnabled(false);
            } else {
                getPreferenceScreen().setEnabled(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}