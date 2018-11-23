package com.certoclav.app.settings;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveParameter;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.MyCallback;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.certocloud.CloudUser;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;


public class SettingsAutoclaveFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener, MyCallback {

    private SweetAlertDialog barProgressDialog;
    private static final int EXPORT_TARGET_USB = 1;
    private static final int EXPORT_TARGET_SD = 2;
    private AutoclaveModelManager manager;

    public SettingsAutoclaveFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_autoclave);
        manager = AutoclaveModelManager.getInstance();

    }

    private void updatePreferences() {
        getPreferenceScreen().removeAll();

        addPreferencesFromResource(R.xml.preference_autoclave);

        findPreference("preferences_autoclave_parameter_update").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ReadAndParseSerialService.getInstance().requestForFirmwareUpdate();
                AuditLogger.addAuditLog(AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_CLICKED,
                        "preferences_autoclave_parameter_update".toString().hashCode(), "");
                return false;
            }
        });

        findPreference("preferences_autoclave_parameter_reset_review_hours").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Helper.askConfirmation(getContext(),getString(R.string.reset),getString(R.string.do_you_really_want_to_reset_format,
                        getString(R.string.preferences_autoclave_review_hours)),new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                ReadAndParseSerialService.getInstance().setParameter(83, "1");
                                AuditLogger.addAuditLog(AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_CLICKED,
                                        "preferences_autoclave_parameter_reset_review_hours".toString().hashCode(), "");
                            }
                        },null);

                return false;
            }
        });

        findPreference("preferences_autoclave_parameter_reset_filter_hours").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Helper.askConfirmation(getContext(),getString(R.string.reset),getString(R.string.do_you_really_want_to_reset_format,
                        getString(R.string.preferences_autoclave_filter_hours)),new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        ReadAndParseSerialService.getInstance().setParameter(86, "1");
                        AuditLogger.addAuditLog(AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_CLICKED,
                                "preferences_autoclave_parameter_reset_filter_hours".toString().hashCode(), "");
                    }
                },null);
                return false;
            }
        });
        if (!manager.isMaintaingingTempExistsInParameters()) {
            Preference preference = findPreference("preferences_autoclave_parameter_27");
            if (preference != null) {
                PreferenceCategory preferenceRoot = (PreferenceCategory) findPreference("pref_key_calibration_category");
                preferenceRoot.removePreference(preference);
            }
        }

        if (!manager.isCoolingParameterExists()) {
            Preference preference = findPreference("preferences_autoclave_parameter_42");
            if (preference != null) {
                PreferenceCategory preferenceRoot = (PreferenceCategory) findPreference("pref_key_calibration_category");
                preferenceRoot.removePreference(preference);
            }
        }

        if (!manager.isWarmUpTempExistsInParameters()) {
            Preference preference = findPreference("preferences_autoclave_parameter_97");
            if (preference != null) {
                PreferenceCategory preferenceRoot = (PreferenceCategory) findPreference("pref_key_calibration_category");
                preferenceRoot.removePreference(preference);
            }
        }


        if (!CloudUser.getInstance().isSuperAdmin())
            for (Integer parameterId : manager.getAdminParameters()) {
                Preference preference = findPreference("preferences_autoclave_parameter_" + parameterId);
                if (preference == null) continue;
                PreferenceCategory root = (PreferenceCategory) findPreference("pref_key_device_category");
                root.removePreference(preference);
                if (root.getPreferenceCount() == 0)
                    root.setShouldDisableView(false);
            }
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

        updatePreferences();
        super.onResume();
    }


    @Override
    public void onSuccess(Object response, int requestId) {

        try {
            if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_GET_PARAMETERS) {
                List<AutoclaveParameter> parameters = (List<AutoclaveParameter>) response;
                Preference pref;
                String key;
                updatePreferences();
                for (AutoclaveParameter parameter : parameters) {

                    //Autoclave Model
                    if (parameter.getParameterId() == 1) {
                        AutoclaveModelManager.getInstance().setModel(parameter);
                        updatePreferences();
                    }

                    key = "preferences_autoclave_parameter_" + parameter.getParameterId();
                    pref = findPreference(key);
                    if (pref != null)
                        if (pref instanceof ListPreference) {
                            pref.getEditor().putString(key, parameter.getValue().toString()).commit();
                            pref.setSummary(parameter.getValue().toString());
                        } else if (!(pref instanceof CheckBoxPreference)) {
                            pref.getEditor().putString(key, parameter.getValue().toString()).commit();
                            pref.setSummary(parameter.getValue().toString());
                        } else {
                            pref.getEditor().putBoolean(key, parameter.getValue().toString().equals("1")).commit();
                        }
                }
            } else if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_SET_PARAMETER) {
                if (!(response instanceof Integer) || Integer.valueOf(response.toString()) == 0) {
                    Toasty.warning(getContext(), getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT, true).show();
                } else {
                    ReadAndParseSerialService.getInstance().getParameters();
                    Toasty.success(getContext(), getString(R.string.changes_successfully_saved), Toast.LENGTH_SHORT, true).show();
                }
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
        } catch (Exception e) {
            e.printStackTrace();
            onError(null, -1);
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