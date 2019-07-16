package com.certoclav.app.settings;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.menu.ChangeAdminPasswordAccountActivity;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.MyCallback;
import com.certoclav.app.util.Requests;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.util.DownloadUtils;
import com.certoclav.library.util.ExportUtils;
import com.certoclav.library.util.UpdateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;


public class SettingsDeviceFragment extends PreferenceFragment implements SensorDataListener {


    private Calendar dateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_device);

        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

//Device Key
            String deviceKey = "-";
            if (Autoclave.getInstance().getController() != null) {
                if (Autoclave.getInstance().getController().getSavetyKey() != null) {
                    deviceKey = Autoclave.getInstance().getController().getSavetyKey();
                }
            }

            findPreference(AppConstants.PREFERENCE_KEY_DEVICE_KEY).setSummary(deviceKey);

//Check for updates
            findPreference(AppConstants.PREFERENCE_KEY_SOFTWARE_UPDATE).setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {

                    if (ApplicationController.getInstance().isNetworkAvailable()) {
                        List<String> downloadUrls = new ArrayList<String>();
                        downloadUrls.add(AppConstants.DOWNLOAD_LINK);
                        DownloadUtils downloadUtils = new DownloadUtils(getActivity());
                        downloadUtils.Download(downloadUrls);
                    } else {
                        Toasty.warning(getActivity(), getString(R.string.please_connect_to_internet), Toast.LENGTH_SHORT, true).show();
                    }

                    return false;
                }
            });

            findPreference(AppConstants.PREFERENCE_KEY_ADMIN_PASSWORD).setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), ChangeAdminPasswordAccountActivity.class));
                    return false;
                }
            });

            //Install update from USB
            findPreference(AppConstants.PREFERENCE_KEY_SOFTWARE_UPDATE_USB).setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    preference.setEnabled(false);
                    ExportUtils exportUtils = new ExportUtils();
                    if (exportUtils.checkExternalMedia() == false) {
                        Toasty.error(getActivity(), getActivity().getString(R.string.can_not_read_usb_flash_disk), Toast.LENGTH_LONG, true).show();
                    } else {
                        UpdateUtils updateUtils = new UpdateUtils(getActivity());
                        updateUtils.installUpdateZip(UpdateUtils.SOURCE_USB);
                    }
                    preference.setEnabled(true);
                    return false;
                }
            });


            //Install update from SDCARD
//            findPreference(AppConstants.PREFERENCE_KEY_SOFTWARE_UPDATE_SDCARD).setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//                @Override
//                public boolean onPreferenceClick(Preference preference) {
//                    preference.setEnabled(false);
//                    ExportUtils exportUtils = new ExportUtils();
//                    if (exportUtils.checkExternalSDCard() == false) {
//                        Toasty.error(getActivity(), getActivity().getString(R.string.can_not_read_from_sd_card), Toast.LENGTH_LONG, true).show();
//                    } else {
//                        UpdateUtils updateUtils = new UpdateUtils(getActivity());
//                        updateUtils.installUpdateZip(UpdateUtils.SOURCE_SDCARD);
//                    }
//                    preference.setEnabled(true);
//                    return false;
//                }
//            });


//Factory Reset
            findPreference(AppConstants.PREFERENCE_KEY_RESET).setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(getString(R.string.factory_reset))
                                .setContentText(getString(R.string.do_you_really_want_to) + " " + getString(R.string.delete_all_data_))
                                .setConfirmText(getString(R.string.yes))
                                .setCancelText(getString(R.string.cancel))
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        if (!DatabaseService.getInstance().exportDB())
                                            return;
                                        sDialog.dismissWithAnimation();
                                        if (AppConstants.TABLET_HAS_ROOT) {

                                            try {

                                                String command;
                                                command = "pm clear com.certoclav.app";
                                                Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});//, envp);
                                                proc.waitFor();

                                            } catch (Exception ex) {
                                                Log.e("SettingsDeviceFragment", "error clear app data");
                                                Log.e("SettingsDeviceFragment", ex.toString());
                                            }
                                        } else {
                                            // closing Entire Application
                                            Editor editor = getActivity().getSharedPreferences("clear_cache", Context.MODE_PRIVATE).edit();
                                            editor.clear();
                                            editor.commit();
                                            ApplicationController.getInstance().clearApplicationData();
                                            android.os.Process.killProcess(android.os.Process.myPid());
                                        }
                                    }
                                }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                    }
                                });
                        sweetAlertDialog.setCanceledOnTouchOutside(true);
                        sweetAlertDialog.setCancelable(true);
                        sweetAlertDialog.show();


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return false;
                }
            });
        } catch (Exception e) {
            getActivity().finish();
            Toasty.warning(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT,
                    true).show();
        }


    }


    @Override
    public void onResume() {
        Autoclave.getInstance().setOnSensorDataListener(this);
//show date and time

        try {


            findPreference(AppConstants.PREFERENCE_KEY_DATE).setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //Toast.makeText(getActivity(), getActivity().getString(R.string.please_use_secondary_lcd_screen_to_change_the_time), Toast.LENGTH_LONG).show();
//                    setTimeAndDate(true);
                    Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                    intent.putExtra("extra_prefs_show_button_bar", true);
                    startActivityForResult(intent, 0);

                    return false;
                }
            });


            //Storage
            findPreference(AppConstants.PREFERENCE_KEY_STORAGE)
                    .setSummary(getString(R.string.free_memory) + ": "
                            + Long.toString(FreeMemory())
                            + " MB");
            //Software Version
            PackageInfo pInfo;
            try {
                pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                String version = pInfo.versionName + " (" + pInfo.versionCode + ")";
                findPreference(AppConstants.PREFERENCE_KEY_VERSION).setSummary(version);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }


            //serial number
//            try {
//                findPreference(AppConstants.PREFERENCE_KEY_SERIAL_NUMBER).setSummary(Autoclave.getInstance().getController().getSerialnumber());
//            } catch (Exception e) {
//                try {
//                    findPreference(AppConstants.PREFERENCE_KEY_SERIAL_NUMBER).setSummary(getString(R.string.please_connect_to_autoclave_first));
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//            }

            //firmware version
//            try {
//                findPreference(AppConstants.PREFERENCE_KEY_FIRMWARE_VERSION).setSummary(Autoclave.getInstance().getController().getFirmwareVersion());
//            } catch (Exception e) {
//                try {
//                    findPreference(AppConstants.PREFERENCE_KEY_FIRMWARE_VERSION).setSummary(getString(R.string.please_connect_to_autoclave_first));
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//            }

            //cycle number
//            try {
//                findPreference(AppConstants.PREFERENCE_KEY_CYCLE_NUMBER).setSummary(getString(R.string.total_cycles_) + " " + Autoclave.getInstance().getController().getCycleNumber());
//            } catch (Exception e) {
//                try {
//                    findPreference(AppConstants.PREFERENCE_KEY_CYCLE_NUMBER).setSummary(getString(R.string.please_connect_to_autoclave_first));
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
//            }
//
//            Enable FDA
            try {

                final CheckBoxPreference checkBoxPreferenceFDA = (CheckBoxPreference) findPreference(AppConstants.PREFERENCE_KEY_ENABLE_FDA);
                checkBoxPreferenceFDA.setEnabled(CloudUser.getInstance().isSuperAdmin());

                checkBoxPreferenceFDA.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {

                        final SweetAlertDialog dialog = Helper.getInstance().getDialog(getContext(),
                                getString(R.string.saving), null, false, false,
                                null, new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                    }
                                });

                        Requests.getInstance().enableDeviceFDA(new MyCallback() {
                            @Override
                            public void onSuccess(Object response, int requestId) {
                                dialog.setTitleText(getString(R.string.success));
                                dialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                dialog.setConfirmText(getString(R.string.ok));
                            }

                            @Override
                            public void onError(ErrorModel error, int requestId) {
                                checkBoxPreferenceFDA.setChecked(!checkBoxPreferenceFDA.isChecked());
                                dialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                dialog.setTitleText(getString(R.string.error));
                                dialog.setContentText(getString(R.string.something_went_wrong));
                                dialog.setConfirmText(getString(R.string.ok));
                            }

                            @Override
                            public void onStart(int requestId) {
                                dialog.show();
                            }

                            @Override
                            public void onProgress(int current, int max) {

                            }
                        }, !checkBoxPreferenceFDA.isChecked(), 12);
                        return true;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Enable Audit Comment
            try {

                final CheckBoxPreference checkBoxPreferenceAuditComment = (CheckBoxPreference) findPreference(AppConstants.PREFERENCE_KEY_ENABLE_AUDIT_COMMENT);
                checkBoxPreferenceAuditComment.setEnabled(Autoclave.getInstance().getUser().isAdmin());
            } catch (Exception e) {
                e.printStackTrace();
            }


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                    prefs.getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_device),
                            ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_device))) {
                Toasty.warning(getActivity(), getString(R.string.these_settings_are_locked_by_the_admin), Toast.LENGTH_SHORT).show();
                getPreferenceScreen().setEnabled(false);
            } else {
                getPreferenceScreen().setEnabled(true);
            }
        } catch (Exception e) {
            getActivity().finish();
        }

        super.onResume();
    }


    @Override
    public void onPause() {
        Autoclave.getInstance().removeOnSensorDataListener(this);
        super.onPause();
    }


    public long FreeMemory() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long Free = (statFs.getAvailableBlocks() * statFs.getBlockSize()) / 1048576;
        return Free;
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }


    @Override
    public void onSensorDataChange(AutoclaveData data) {
        //update date and time
        findPreference(AppConstants.PREFERENCE_KEY_DATE).setSummary(new StringBuilder()
                .append(Autoclave.getInstance().getDate())                // Month is 0 based so add 1
                .append(" ")
                .append(Autoclave.getInstance().getTime()));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful

            // The user picked a contact.
            // The Intent's data Uri identifies which contact was selected.
            Calendar calendar = Calendar.getInstance();
            // Do something with the contact here (bigger example below)
            ReadAndParseSerialService.getInstance().setParameter(93, new SimpleDateFormat("yyMMddHHmmss").format(calendar.getTime()));

        }
    }

    private void setTimeAndDate(boolean isDate) {


        if (isDate) {
            dateTime = Calendar.getInstance();
            DatePickerDialog datepickerdialog = new DatePickerDialog(
                    getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            dateTime.set(Calendar.YEAR, year);
                            dateTime.set(Calendar.MONTH, month);
                            dateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            setTimeAndDate(false);
                        }
                    },
                    dateTime.get(Calendar.YEAR),
                    dateTime.get(Calendar.MONTH),
                    dateTime.get(Calendar.DAY_OF_MONTH)
            );

            datepickerdialog.setTitle("Please select a date"); //dialog title
            datepickerdialog.show(); //show dialog
        } else {
            TimePickerDialog timepickerdialog = new TimePickerDialog(getContext(),
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            dateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            dateTime.set(Calendar.MINUTE, minute);
                            ReadAndParseSerialService.getInstance().setParameter(93, new SimpleDateFormat("yyMMddhhmmss").format(dateTime.getTime()));
                        }
                    },
                    dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), true);
            timepickerdialog.show(); //show time picker dialog
        }
    }

}