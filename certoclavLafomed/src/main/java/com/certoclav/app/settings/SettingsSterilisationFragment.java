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
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.listener.UserProgramListener;
import com.certoclav.app.menu.MenuLabelPrinterActivity;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.service.ReadAndParseSerialService;
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

        //PRE HEAT ON OFF
        ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_PREHEAT)).setChecked(Autoclave.getInstance().isPreheat());
        ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_PREHEAT)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    Boolean boolVal = (Boolean) newValue;
                    ReadAndParseSerialService.getInstance().sendPreheatCommand(boolVal);
                }

                return true;
            }
        });

        //KEEP TEMP ON OFF
        ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_KEEP_TEMP)).setChecked(Autoclave.getInstance().isPreheat());
        ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_KEEP_TEMP)).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue instanceof Boolean) {
                    Boolean boolVal = (Boolean) newValue;
                    ReadAndParseSerialService.getInstance().sendKeepTemperatureCommand(boolVal);
                }

                return true;
            }
        });

        //Edit user defined program
        ((Preference) findPreference(AppConstants.PREFREENCE_KEY_USER_DEFINED)).setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    final SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), R.layout.dialog_edit_program, SweetAlertDialog.WARNING_TYPE);
                    dialog.setContentView(R.layout.dialog_edit_program);
                    dialog.setTitle(R.string.edit_custom_program);
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                    final UserProgramListener userProgramReceivedListener = new UserProgramListener() {

                        @Override
                        public void onUserProgramReceived() {
                            Profile profile = Autoclave.getInstance().getUserDefinedProgram();
                            ((TextView) dialog.findViewById(R.id.dialog_program_edit_vacuum_times)).setText(Integer.toString(profile.getVacuumTimes()));
                            ((TextView) dialog.findViewById(R.id.dialog_program_edit_sterilizationtemperature)).setText(Integer.toString(profile.getSterilisationTemperature()));
                            ((TextView) dialog.findViewById(R.id.dialog_program_edit_sterilizationtime)).setText(Integer.toString(profile.getSterilisationTime()));
                            ((TextView) dialog.findViewById(R.id.dialog_program_edit_dryingtime)).setText(Integer.toString(profile.getDryTime()));


                        }
                    };

                    Autoclave.getInstance().setOnUserProgramListener(userProgramReceivedListener);

                    ReadAndParseSerialService.getInstance().sendGetUserProgramCommand();

                    Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialog_program_button_cancel);
                    dialogButtonCancel.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Autoclave.getInstance().removeOnUserProgramListener(userProgramReceivedListener);
                            dialog.dismissWithAnimation();
                        }
                    });
                    Button dialogButtonApply = (Button) dialog.findViewById(R.id.dialog_program_button_apply);
                    // if button is clicked, close the custom dialog
                    dialogButtonApply.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Integer vacuumTimes = Integer.parseInt(((TextView) dialog.findViewById(R.id.dialog_program_edit_vacuum_times)).getText().toString());
                                Integer sterilizationTemp = Integer.parseInt(((TextView) dialog.findViewById(R.id.dialog_program_edit_sterilizationtemperature)).getText().toString());
                                Integer sterilizationTime = Integer.parseInt(((TextView) dialog.findViewById(R.id.dialog_program_edit_sterilizationtime)).getText().toString());
                                Integer dryingTime = Integer.parseInt(((TextView) dialog.findViewById(R.id.dialog_program_edit_dryingtime)).getText().toString());
                                if (vacuumTimes >= 1 && vacuumTimes <= 10) {
                                    if (sterilizationTemp >= 105 && sterilizationTemp <= 134) {
                                        if (sterilizationTime >= 4 && sterilizationTime <= 60) {
                                            if (dryingTime >= 1 && dryingTime <= 25) {
                                                ReadAndParseSerialService.getInstance().sendPutUserProgramCommand(vacuumTimes, sterilizationTemp, sterilizationTime, dryingTime);
                                                ReadAndParseSerialService.getInstance().sendGetUserProgramCommand();
                                                Toast.makeText(getActivity(), R.string.program_saved, Toast.LENGTH_LONG).show();
                                                Autoclave.getInstance().removeOnUserProgramListener(userProgramReceivedListener);
                                                dialog.dismissWithAnimation();
                                            } else {
                                                Toast.makeText(getActivity(), getString(R.string.please_enter_a_valid_drying_time), Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(getActivity(), getString(R.string.please_enter_a_valid_sterilization_time), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), getString(R.string.please_enter_a_valid_sterilization_temperature), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), getString(R.string.please_enter_a_valid_number_of_vacuum_times), Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {

                                Toast.makeText(getActivity(), getString(R.string.please_enter_a_valid_data), Toast.LENGTH_LONG).show();

                            }

                        }
                    });

                    dialog.show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

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

                if (ApplicationController.getInstance().isNetworkAvailable()|| ServerConfigs.getInstance(getActivity()).getUrl() != null) {
                    barProgressDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                    barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    barProgressDialog.setTitleText(getActivity().getString(com.certoclav.library.R.string.downloading));
                    barProgressDialog.setCancelable(false);
                    Helper.downloadProtocols(getActivity(), new MyCallback() {
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
        ((Preference) findPreference(AppConstants.PREFERENCE_KEY_EXPORT_SD)).setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                ExportUtils exportUtils = new ExportUtils();


                if (exportUtils.checkExternalSDCard()) { //check if usb flash drive is available
                    uploadAllProtocolsTo(EXPORT_TARGET_SD);
                } else {

                    try {

                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(getString(R.string.no_sdcard))
                                .setContentText(getString(R.string.no_sdcard_detected))
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

        prefs.registerOnSharedPreferenceChangeListener(listener);


    }


    @Override
    public void onResume() {


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_PREHEAT)).setChecked(Autoclave.getInstance().isPreheat());
        ((CheckBoxPreference) findPreference(AppConstants.PREFREENCE_KEY_KEEP_TEMP)).setChecked(Autoclave.getInstance().isPreheat());
        ((CheckBoxPreference) findPreference(AppConstants.PREFERENCE_KEY_STEP_BY_STEP)).setChecked(prefs.getBoolean(AppConstants.PREFERENCE_KEY_STEP_BY_STEP,false));


        if(AppConstants.IS_CERTOASSISTANT){
            ((CheckBoxPreference) findPreference(AppConstants.PREFERENCE_KEY_STEP_BY_STEP)).setEnabled(false);
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


        final DatabaseService databaseServie = new DatabaseService(getActivity());

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
                            success = expUtils.writeToExtUsbFile("Certoclav protocols", filename, "txt", sb.toString());
                        } else {
                            success = expUtils.writeToExtSDFile("Certoclav protocols", filename, "txt", sb.toString());
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


        }.

                execute();


    }


    public class EditProgramDialog extends Dialog {

        public EditProgramDialog(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }


        public void setOnUserProgramReceivedListener(UserProgramListener listener) {
            Autoclave.getInstance().setOnUserProgramListener(listener);
        }

        public void removeOnUserProgramReceivedListener(UserProgramListener listener) {
            Autoclave.getInstance().removeOnUserProgramListener(listener);
        }

    }


}