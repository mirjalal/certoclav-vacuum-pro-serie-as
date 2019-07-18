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
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.MyCallback;
import com.certoclav.app.util.ServerConfigs;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.util.ExportUtils;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SettingsSterilisationFragment extends PreferenceFragment {

    private SweetAlertDialog barProgressDialog;
    private static final int EXPORT_TARGET_USB = 1;
    private static final int EXPORT_TARGET_SD = 2;


    private OnSharedPreferenceChangeListener listener;

    public SettingsSterilisationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_sterilization);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //upload protocols to USB
        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_EXPORT_USB)).setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                ExportUtils exportUtils = new ExportUtils();


                if (exportUtils.checkExternalMedia()) { //check if usb flash drive is available
                    uploadAllProtocolsTo(EXPORT_TARGET_USB);
                } else {

                    try {

                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(getString(R.string.mount_usb_stick))
                                .setContentText(getString(R.string.reboot_neccessary))
                                .setConfirmText(getString(R.string.ok))
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                    }
                                });
                        sweetAlertDialog.setCanceledOnTouchOutside(true);
                        sweetAlertDialog.setCancelable(true);
                        sweetAlertDialog.show();


                    } catch (Exception e) {

                    }
                }
                return false;

            }
        });

        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_DOWNLOAD_PROTOCOLS)).setEnabled(CloudUser.getInstance().isLoggedIn() && Autoclave.getInstance().isOnlineMode(getActivity()));
        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_DOWNLOAD_PROTOCOLS)).setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (ApplicationController.getInstance().isNetworkAvailable() || ServerConfigs.getInstance(getActivity()).getUrl() != null) {
                    barProgressDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                    barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    barProgressDialog.setTitleText(getActivity().getString(com.certoclav.library.R.string.downloading));
                    barProgressDialog.setCancelable(false);
                    Helper.getInstance().downloadProtocols(getActivity(), new MyCallback() {
                        @Override
                        public void onSuccess(Object response, int requestId) {
                            if ((Boolean) response)
                                barProgressDialog.setTitleText(getActivity().getString(R.string.adding));
                            else {
                                barProgressDialog.setTitleText(getActivity().getString(R.string.download_success));
                                barProgressDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            }

                        }

                        @Override
                        public void onError(ErrorModel error, int requestId) {
                            barProgressDialog.setTitleText(error.getMessage() != null ? error.getMessage() : getActivity().getString(R.string.download_failed));
                            barProgressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        }

                        @Override
                        public void onStart(int requestId) {
                            barProgressDialog.show();
                        }

                        public void onProgress(int current, int max) {
                            barProgressDialog.setTitleText(getActivity().getString(R.string.adding) + " (" + current + " / " + max + ")");
                        }
                    });
                } else {

                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                            .setTitleText(getString(R.string.enable_network_communication))
                            .setConfirmText(getString(R.string.ok))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            }).setCustomImage(R.drawable.ic_network_connection);
                    sweetAlertDialog.setCanceledOnTouchOutside(true);
                    sweetAlertDialog.setCancelable(true);
                    sweetAlertDialog.show();
                }
                return false;

            }
        });


        //OPEN LABEL PRINTER UTIL
        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_PRINT_LABEL)).setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent(getActivity(), MenuLabelPrinterActivity.class);
                getActivity().startActivity(intent);

                return false;
            }
        });


        //upload protocols to SD
//        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_EXPORT_SD)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//
//
//                ExportUtils exportUtils = new ExportUtils();
//
//
//                if (exportUtils.checkExternalSDCard()) { //check if usb flash drive is available
//                    uploadAllProtocolsTo(EXPORT_TARGET_SD);
//                } else {
//
//                    try {
//
//                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
//                                .setTitleText(getString(R.string.no_sdcard))
//                                .setContentText(getString(R.string.no_sdcard_detected))
//                                .setConfirmText(getString(R.string.ok))
//                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                    @Override
//                                    public void onClick(SweetAlertDialog sDialog) {
//                                        sDialog.dismissWithAnimation();
//                                    }
//                                });
//                        sweetAlertDialog.setCanceledOnTouchOutside(true);
//                        sweetAlertDialog.setCancelable(true);
//                        sweetAlertDialog.show();
//
//
//                    } catch (Exception e) {
//
//                    }
//                }
//                return false;
//
//            }
//        });

        prefs.registerOnSharedPreferenceChangeListener(listener);


    }


    @Override
    public void onResume() {


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_PREHEAT)).setChecked(Autoclave.getInstance().isPreheat());
        // ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_KEEP_TEMP)).setChecked(Autoclave.getInstance().isPreheat());
        ((CheckBoxPreference) findPreference(AppConstants.PREFERENCE_KEY_STEP_BY_STEP)).setChecked(prefs.getBoolean(AppConstants.PREFERENCE_KEY_STEP_BY_STEP, false));


        if (AppConstants.IS_CERTOASSISTANT) {
            ((CheckBoxPreference) findPreference(AppConstants.PREFERENCE_KEY_STEP_BY_STEP)).setEnabled(false);
        }

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

    public void uploadAllProtocolsTo(final int target_id) {

        barProgressDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        barProgressDialog.setTitleText(getString(R.string.import_protocols));
        barProgressDialog.setCancelable(false);
        barProgressDialog.show();
        //  barProgressDialog = new ProgressDialog(getActivity());

        barProgressDialog.setTitle("");
        if (target_id == EXPORT_TARGET_USB) {
            barProgressDialog.setContentText("copy protocols to USB flash drive");
        } else {
            barProgressDialog.setContentText("copy protocols to SD card");
        }
        barProgressDialog.show();


        final DatabaseService databaseServie = DatabaseService.getInstance();

        new AsyncTask<Void, Boolean, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                List<Protocol> protocols = databaseServie.getProtocols();
                try {
                    final int numberOfProtocols = protocols.size();
                    int i = 0;
                    while (i < numberOfProtocols) {

                        StringBuilder sb = new StringBuilder();
                        Protocol protocol = protocols.get(i);

                        String filename = Autoclave.getInstance().getController().getSerialnumber() + "-" + protocol.getZyklusNumber();
                        //	barProgressDialog.setMessage("Copy " + filename + ".txt");

                        sb.append("Protocol CertoClav Vacuum Pro Series").append("\r\n")
                                .append("S/N.: ").append(Autoclave.getInstance().getController().getSerialnumber()).append("\r\n")
                                .append("\r\n")
                                .append("Program: ").append(protocol.getProfileName()).append("\r\n")
                                .append("Program description: ").append(protocol.getProfileDescription()).append("\r\n")
                                .append("Cycle number: ").append(protocol.getZyklusNumber()).append("\r\n")
                                .append("Start time: ").append(protocol.getStartTime()).append("\r\n")
                                .append("End time: ").append(protocol.getEndTime()).append("\r\n")
                                .append("Status: ").append(protocol.getErrorCode()).append("\r\n")
                                .append("\r\n")
                                .append("h:m:s").append("\t").append("temperature").append("\t").append("pressure").append("\r\n");
                        for (ProtocolEntry pE : protocol.getProtocolEntry()) {
                            sb.append(pE.getFormatedTimeStampShort()).append("\t").append(pE.getTemperature()).append("\t").append(pE.getPressure()).append("\r\n");
                        }


                        sb.append("Summary: ").append(AutoclaveMonitor.getInstance().getErrorString(protocol.getErrorCode()));
                        ExportUtils expUtils = new ExportUtils();
                        boolean success = false;
                        if (target_id == EXPORT_TARGET_USB) {
                            success = expUtils.writeToExtUsbFile("Raypa protocols", filename, "txt", sb.toString());
                        } else {
                            success = expUtils.writeToExtSDFile("Raypa protocols", filename, "txt", sb.toString());
                        }
                        if (success == false) {
                            return false;
                        }

                        i++;
                        final int finalI = i;
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                barProgressDialog.setTitleText(getActivity().getString(R.string.coping_protocols) + " (" + ((100 * finalI) / numberOfProtocols) + "%)");
                            }
                        });


                    }//end while
                    //all protocols copied sucessfully
                    return true;


                } catch (Exception e) {
                    Log.e("ExportUtils", "Exception during copying protocols: " + e.toString());
                    e.printStackTrace();
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result == false) {
                    barProgressDialog
                            .setContentText(getString(R.string.export_failed))
                            .setConfirmText("OK")
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                } else {
                    // Toast.makeText(getActivity(), , Toast.LENGTH_LONG).show();
                    barProgressDialog
                            .setConfirmText("OK")
                            .setContentText(getString(R.string.export_success))
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                }
                super.onPostExecute(result);
            }


        }.execute();


    }


    public class EditProgramDialog extends Dialog {

        public EditProgramDialog(Context context) {
            super(context);
        }


        public void setOnUserProgramReceivedListener(UserProgramListener listener) {
            Autoclave.getInstance().setOnUserProgramListener(listener);
        }

        public void removeOnUserProgramReceivedListener(UserProgramListener listener) {
            Autoclave.getInstance().removeOnUserProgramListener(listener);
        }

    }


}