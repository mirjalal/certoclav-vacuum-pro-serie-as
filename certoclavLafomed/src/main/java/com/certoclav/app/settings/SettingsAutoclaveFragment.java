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
import android.util.Pair;
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
    //Check is it is first time, get all parameter from autoclave
    private boolean isParameterLoaded;

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

        findPreference("preferences_autoclave_parameter_std_assign")
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {

                        Helper.askConfirmation(getContext(), getString(R.string.warning), getString(R.string.do_you_really_want_to_assign_programs), new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                ReadAndParseSerialService.getInstance().setParameter(2, "1");
                                AuditLogger.addAuditLog(AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_CLICKED,
                                        "preferences_autoclave_parameter_std_assign".hashCode(), "");
                            }
                        }, null);

                        return false;
                    }
                });

        findPreference("preferences_autoclave_parameter_update").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ReadAndParseSerialService.getInstance().requestForFirmwareUpdate();
                AuditLogger.addAuditLog(AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_CLICKED,
                        "preferences_autoclave_parameter_update".hashCode(), "");
                return false;
            }
        });

//        findPreference("preferences_autoclave_parameter_reset_review_hours")
//                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                    @Override
//                    public boolean onPreferenceClick(Preference preference) {
//
//                        Helper.askConfirmation(getContext(), getString(R.string.reset), getString(R.string.do_you_really_want_to_reset_format,
//                                getString(R.string.preferences_autoclave_review_hours)), new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sDialog) {
//                                sDialog.dismissWithAnimation();
//                                ReadAndParseSerialService.getInstance().setParameter(83, "1");
//                                AuditLogger.addAuditLog(AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_CLICKED,
//                                        "preferences_autoclave_parameter_reset_review_hours".hashCode(), "");
//                            }
//                        }, null);
//
//                        return false;
//                    }
//                });
//
//        findPreference("preferences_autoclave_parameter_reset_filter_cycle")
//                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                    @Override
//                    public boolean onPreferenceClick(Preference preference) {
//                        Helper.askConfirmation(getContext(), getString(R.string.reset), getString(R.string.do_you_really_want_to_reset_format,
//                                getString(R.string.preferences_autoclave_filter_cycle)), new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sDialog) {
//                                sDialog.dismissWithAnimation();
//                                ReadAndParseSerialService.getInstance().setParameter(86, "1");
//                                AuditLogger.addAuditLog(AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_CLICKED,
//                                        "preferences_autoclave_parameter_reset_filter_cycle".hashCode(), "");
//                            }
//                        }, null);
//                        return false;
//                    }
//                });
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
            Preference preference = findPreference("preferences_autoclave_parameter_99");
            if (preference != null) {
                PreferenceCategory preferenceRoot = (PreferenceCategory) findPreference("pref_key_calibration_category");
                preferenceRoot.removePreference(preference);
            }
        } else {
            findPreference("preferences_autoclave_parameter_99")
                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            float val = Float.parseFloat(newValue.toString());
                            if ((val >= manager.getWarmingUpTempRange().first) && (val <= manager.getWarmingUpTempRange().second)) {
                                return true;
                            } else {
                                // invalid you can show invalid message
                                Toasty.error(getContext(),
                                        getString(R.string.preferences_autoclave_warm_up_temp_range,
                                                manager.getWarmingUpTempRange().first,
                                                manager.getWarmingUpTempRange().second),
                                        Toast.LENGTH_SHORT, true).show();
                                return false;
                            }
                        }
                    });
        }


        if (!CloudUser.getInstance().isSuperAdmin()) {
            for (Integer parameterId : manager.getAdminParameters()) {
                Preference preference = findPreference("preferences_autoclave_parameter_" + parameterId);

                //Parameter 2 is Std Assign parameter
                if (parameterId == 2)
                    preference = findPreference("preferences_autoclave_parameter_std_assign");

                if (preference == null) continue;
//                PreferenceCategory root = (PreferenceCategory) findPreference("pref_key_device_category");
//                root.removePreference(preference);
                preference.setEnabled(false);
//                if (root.getPreferenceCount() == 0)
//                    root.setShouldDisableView(false);
            }
        }
        //Enable editable of parameter Number review hours
        //Enable editable of parameter Number Filter Cycle
//        findPreference("preferences_autoclave_parameter_96").setEnabled(CloudUser.getInstance().isSuperAdmin());
//        findPreference("preferences_autoclave_parameter_98").setEnabled(CloudUser.getInstance().isSuperAdmin());

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
        if (!isParameterLoaded) return;
        Preference connectionPref = findPreference(key);
        if (!(connectionPref instanceof CheckBoxPreference)) {
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
            if (sharedPreferences.contains(key)) {
                int id = Integer.valueOf(key.replace("preferences_autoclave_parameter_", ""));
                Pair<Float, Float> range = AutoclaveModelManager.getInstance().getParamterRange(id);
                if (range != null) {
                    boolean isInRange = false;
                    try {
                        float value = Float.valueOf(sharedPreferences.getString(key, ""));
                        isInRange = value >= range.first && value <= range.second;
                    } catch (Exception e) {
                        isInRange = false;
                    }
                    if (!isInRange) {
                        Toasty.error(getActivity(), getString(R.string.parameter_range, range.first, range.second),
                                Toast.LENGTH_SHORT, true).show();
                        ReadAndParseSerialService.getInstance().getParameters();
                        return;
                    }

                }
                //It is a temperature, should check the temp unit, StMax only support C
                if (AutoclaveModelManager.getInstance().isTemperatureParameter(id)) {
                    ReadAndParseSerialService.getInstance().setParameter(
                            id,
                            Helper.currentUnitToCelsius(Float.valueOf(sharedPreferences.getString(key, "")
                                    .replace(",", "."))));
                } else
                    ReadAndParseSerialService.getInstance().setParameter(
                            id,
                            sharedPreferences.getString(key, ""));
            }
        } else {
            if (sharedPreferences.contains(key))
                ReadAndParseSerialService.getInstance().setParameter(
                        Integer.valueOf(key.replace("preferences_autoclave_parameter_", "")),
                        sharedPreferences.getBoolean(key, false) ? 1 : 0);
        }

    }

    @Override
    public void onResume() {
        isParameterLoaded = false;
        ReadAndParseSerialService.getInstance().addCallback(this);
        ReadAndParseSerialService.getInstance().getParameters();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if ((!Autoclave.getInstance().getUser().isAdmin() ||
                Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
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
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
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
                        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
                        sharedPreferences.edit().putString("preferences_glp_autoclave_name",
                                "Raypa " + parameter.getValue() + " Autoclave").commit();
                        updatePreferences();
                    }
                    if (parameter.getParameterId() == 3) {
                        AutoclaveModelManager.getInstance().setSerialNumber(parameter);
                        updatePreferences();
                    }

                    if (parameter.getParameterId() == 8) {

                        if (!AutoclaveModelManager.getInstance().getTemperatureUnit().equals(parameter.getValue().toString())) {
                            AutoclaveModelManager.getInstance().setTemperatureSymbol(parameter);
                            Helper.getPrograms(getActivity());
                        } else {
                            AutoclaveModelManager.getInstance().setTemperatureSymbol(parameter);
                        }
                    }

                    key = "preferences_autoclave_parameter_" + parameter.getParameterId();
                    pref = findPreference(key);
                    if (pref != null)
                        if (pref instanceof ListPreference) {
                            pref.getEditor().putString(key, parameter.getValue().toString()).commit();
                        } else if (!(pref instanceof CheckBoxPreference)) {
                            pref.getEditor().putString(key, parameter.getValue().toString()).commit();
                        } else {
                            pref.getEditor().putBoolean(key, parameter.getValue().toString().equals("1")).commit();
                        }
                }

                updatePreferences();

                //Update the summary of the parameters
                for (AutoclaveParameter parameter : parameters) {
                    key = "preferences_autoclave_parameter_" + parameter.getParameterId();
                    pref = findPreference(key);
                    if (pref != null && !(pref instanceof CheckBoxPreference))
                        pref.setSummary(parameter.getValue().toString());
                }
                //Parameters are loaded, then enable the preference to be editable
                isParameterLoaded = true;

            } else if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_SET_PARAMETER) {
                if (!(response instanceof Integer) || Integer.valueOf(response.toString()) == 0) {
                    Toasty.warning(getContext(), getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT, true).show();
                } else {
                    ReadAndParseSerialService.getInstance().getParameters();
                    Toasty.success(getContext(), getString(R.string.changes_successfully_saved), Toast.LENGTH_SHORT, true).show();
                }
            } else if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_GET_PARAMETER) {
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
        } catch (Exception e) {
            e.printStackTrace();
            onError(null, -1);
        } finally {
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
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
        barProgressDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                getActivity().finish();
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