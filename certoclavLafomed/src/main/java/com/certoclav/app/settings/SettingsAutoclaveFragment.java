package com.certoclav.app.settings;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.listener.UserProgramListener;
import com.certoclav.app.menu.MenuLabelPrinterActivity;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveParameter;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.MyCallback;
import com.certoclav.app.util.ServerConfigs;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.util.ExportUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SettingsAutoclaveFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, MyCallback {

    private SweetAlertDialog barProgressDialog;
    private static final int EXPORT_TARGET_USB = 1;
    private static final int EXPORT_TARGET_SD = 2;


    public SettingsAutoclaveFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_autoclave);

    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        ReadAndParseSerialService.getInstance().removeCallback(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference connectionPref = findPreference(key);
        if (!(connectionPref instanceof CheckBoxPreference))
            connectionPref.setSummary(sharedPreferences.getString(key, ""));

    }

    @Override
    public void onResume() {
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        ReadAndParseSerialService.getInstance().addCallback(this);
        ReadAndParseSerialService.getInstance().getParameters();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                prefs.getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_sterilization),
                        ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_sterilization))) {
            Toast.makeText(getActivity(), R.string.these_settings_are_locked_by_the_admin, Toast.LENGTH_SHORT).show();
            getPreferenceScreen().setEnabled(false);
        } else {
            getPreferenceScreen().setEnabled(true);
        }
        super.onResume();
    }


    @Override
    public void onSuccess(Object response, int requestId) {
        if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_GET_PARAMETERS) {
            List<AutoclaveParameter> parameters = (List<AutoclaveParameter>) response;
            Preference pref;
            String key;
            for (AutoclaveParameter parameter : parameters) {
                pref = findPreference(key = "preferences_autoclave_parameter_" + parameter.getParameterId());
                if (!(pref instanceof CheckBoxPreference)) {
                    pref.getEditor().putString(key, parameter.getValue().toString());
                    pref.setSummary(parameter.getValue().toString());
                } else {
                    pref.getEditor().putBoolean(key, parameter.getValue().toString().equals("1"));
                }
            }
        }
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
}