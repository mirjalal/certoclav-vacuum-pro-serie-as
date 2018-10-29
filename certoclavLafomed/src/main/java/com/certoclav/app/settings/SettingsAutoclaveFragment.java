package com.certoclav.app.settings;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveParameter;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.MyCallback;
import com.certoclav.library.application.ApplicationController;

import java.util.List;

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

        findPreference("preferences_autoclave_parameter_update").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ReadAndParseSerialService.getInstance().requestForFirmwareUpdate();
                return false;
            }
        });
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
        if (!(connectionPref instanceof CheckBoxPreference)) {
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
            if (sharedPreferences.contains(key))
                ReadAndParseSerialService.getInstance().setParameter(Integer.valueOf(key.replace("preferences_autoclave_parameter_", "")),
                        sharedPreferences.getString(key, ""));
        } else {
            if (sharedPreferences.contains(key))
                ReadAndParseSerialService.getInstance().setParameter(Integer.valueOf(key.replace("preferences_autoclave_parameter_", "")),
                        sharedPreferences.getBoolean(key, false) ? 1 : 0);
        }

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

                //Autoclave Model
                if(parameter.getParameterId()==1){
                    AutoclaveModelManager.getInstance().setModel(parameter);
                }

                key = "preferences_autoclave_parameter_" + parameter.getParameterId();
                pref = findPreference(key);
                if (pref != null)
                    if (!(pref instanceof CheckBoxPreference)) {
                        pref.getEditor().putString(key, parameter.getValue().toString()).commit();
                        pref.setSummary(parameter.getValue().toString());
                    } else {
                        pref.getEditor().putBoolean(key, parameter.getValue().toString().equals("1")).commit();
                    }
            }
        } else if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_SET_PARAMETER) {
            if (!(response instanceof Integer) || Integer.valueOf(response.toString()) == 0) {
                Toast.makeText(getContext(), getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show();
            }
            ReadAndParseSerialService.getInstance().getParameters();
        } else if (requestId == ReadAndParseSerialService.HANDLER_MSG_CMD_UTF) {
            boolean isSuccess = response instanceof Integer && Integer.valueOf(response.toString()) == 1;
            final SweetAlertDialog barProgressDialog = new SweetAlertDialog(getContext(),
                    isSuccess ? SweetAlertDialog.SUCCESS_TYPE : SweetAlertDialog.ERROR_TYPE);
            barProgressDialog.setTitleText(getString(isSuccess ? R.string.success : R.string.failed));
            barProgressDialog.setContentText(getString(isSuccess ? R.string.please_reboot_to_start_update : R.string.something_went_wrong));
            barProgressDialog.setConfirmText(getString(R.string.ok));
            barProgressDialog.showCancelButton(false);
            barProgressDialog.setCanceledOnTouchOutside(true);
            barProgressDialog.show();
        }
    }

    @Override
    public void onError(ErrorModel error, int requestId) {
        final SweetAlertDialog barProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
        barProgressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        barProgressDialog.setTitleText(getString(R.string.failed));
        barProgressDialog.setContentText(getString(R.string.something_went_wrong_try_again));
        barProgressDialog.setCancelText(getString(R.string.close));
        barProgressDialog.setConfirmText(getString(R.string.try_again));
        barProgressDialog.showCancelButton(true);
        barProgressDialog.setCanceledOnTouchOutside(true);
        barProgressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                ReadAndParseSerialService.getInstance().getParameters();
            }
        });
        barProgressDialog.show();
    }

    @Override
    public void onStart(int requestId) {

    }

    @Override
    public void onProgress(int current, int max) {

    }
}