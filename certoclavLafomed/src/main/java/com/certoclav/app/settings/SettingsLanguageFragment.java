package com.certoclav.app.settings;


import java.util.Locale;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.app.util.Helper;
import com.certoclav.library.application.ApplicationController;


public class SettingsLanguageFragment extends PreferenceFragment {

    private OnSharedPreferenceChangeListener listener;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("SettingsLanguage", "oncreate called");
        addPreferencesFromResource(R.xml.preferences_language);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_LANGUAGE)).setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

//                Intent i = new Intent();
//                i.setAction(android.provider.Settings.ACTION_LOCALE_SETTINGS);
//                i.addCategory(Intent.CATEGORY_DEFAULT);
//                startActivity(i);

                Intent intent = new Intent(getActivity(), SettingsLanguagePickerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().startActivity(intent);
                return false;
            }
        });


        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

            }


        };

        prefs.registerOnSharedPreferenceChangeListener(listener);

        Log.e("SettingsLanguage", "oncreate finished");
    }



    @Override
    public void onResume() {

        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_LANGUAGE)).setSummary(String.format("%s (%s)", toTitleCase(Locale.getDefault().getDisplayLanguage()), toTitleCase(Locale.getDefault().getDisplayCountry())));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                prefs.getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_language),
                        ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_language))) {
            Toast.makeText(getActivity(), R.string.these_settings_are_locked_by_the_admin, Toast.LENGTH_SHORT).show();
            getPreferenceScreen().setEnabled(false);
        } else {
            getPreferenceScreen().setEnabled(true);
        }

        super.onResume();
    }

    private static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

}