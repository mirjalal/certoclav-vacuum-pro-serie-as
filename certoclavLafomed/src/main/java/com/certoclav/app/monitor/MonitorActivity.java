package com.certoclav.app.monitor;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.activities.ProgramTimerActivity;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
import com.certoclav.app.fragments.DatePickerFragment;
import com.certoclav.app.fragments.TimePickerFragment;
import com.certoclav.app.listener.AlertListener;
import com.certoclav.app.listener.AutoclaveStateListener;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.listener.ProfileListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.model.Error;
import com.certoclav.app.settings.SettingsActivity;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.view.ControlPagerAdapter;
import com.certoclav.library.view.CustomViewPager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class MonitorActivity extends CertoclavSuperActivity implements NavigationbarListener, ProfileListener, AlertListener, AutoclaveStateListener {

    private static final int INDEX_FRAGMANT_GRAPH = 0;
    private static final int INDEX_FRAGMENT_AUTOCLAVE = 1;
    private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
    private CertoclavNavigationbarClean navigationbar;
    private Calendar dateTime;

    ControlPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    CustomViewPager mViewPager;


    private TextView textProgram;
    private TextView textState;
    private Button buttonStop;
    private TextView textSteps;
    private TextView textCycleCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_activity);
        navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.title_monitor));
        if (AppConstants.APPLICATION_DEBUGGING_MODE == true) {
            navigationbar.setSettingsVisible();
        }


        textSteps = (TextView) findViewById(R.id.monitor_text_steps);


        textCycleCount = (TextView) findViewById(R.id.monitor_text_cycle_count);
        textProgram = (TextView) findViewById(R.id.monitor_text_programname);
        textState = (TextView) findViewById(R.id.monitor_text_state);
        buttonStop = (Button) findViewById(R.id.monitor_button_stop);

        if (Autoclave.getInstance().getProfile() != null) {
            textProgram.setText(Autoclave.getInstance().getProfile().getName());
        } else {
            textProgram.setText("-");
        }

        buttonStop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (Autoclave.getInstance().getState().equals(AutoclaveState.NOT_RUNNING)) {
                    showStartNowOrLaterProgramDialog();
                } else if (Autoclave.getInstance().getState().equals(AutoclaveState.PROGRAM_FINISHED)) {
                    //do nothing
                } else {
                    Log.e("MonitorActivity", "sendStopCommand");
                    if (Autoclave.getInstance().getState().equals(AutoclaveState.RUNNING)) {

                        String dialogTitletext = "";
                        String dialogContentText = "";
                        if (Autoclave.getInstance().getData().getTemp1().getCurrentValue() < 90 && Autoclave.getInstance().getData().getTemp2().getCurrentValue() <= 90 && Autoclave.getInstance().getSecondsSinceStart() > 2400 && Autoclave.getInstance().getIndexOfRunningProgram() == 12) {
                            dialogTitletext = getString(R.string.stop_program_earlier);
                            dialogContentText = getString(R.string.do_you_want_to_stop_the_program_earlier_sterilization_result_will_be_successfull_);
                        } else {
                            dialogTitletext = getString(R.string.stop_program);
                            dialogContentText = getString(R.string.do_you_really_want_to_stop_the_running_program_);
                        }


                        try {
                            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MonitorActivity.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText(dialogTitletext)
                                    .setContentText(dialogContentText)
                                    .setConfirmText(getString(R.string.yes))
                                    .setCancelText(getString(R.string.cancel))
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                            AutoclaveMonitor.getInstance().sendStopCommand();
                                            buttonStop.setText("STOPPING...");
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
                            Log.e("MonitorActiviy", e.toString());
                        }


                    }
                }

            }
        });


        fragmentList.add(INDEX_FRAGMANT_GRAPH, new MonitorGraphFragment());
        fragmentList.add(INDEX_FRAGMENT_AUTOCLAVE, new MonitorAutoclaveFragment());

        mSectionsPagerAdapter = new ControlPagerAdapter(getSupportFragmentManager(), fragmentList);


        mViewPager = (CustomViewPager) findViewById(R.id.monitor_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // a reference to the Tab.
        mViewPager
                .setOnPageChangeListener(new CustomViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {


                    }
                });
    }

    private void startProgram() {
        if (Autoclave.getInstance().getProfile() != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext());

            if (prefs.getBoolean(AppConstants.PREFERENCE_KEY_MATERIAL_TEST, false)) {
                showProgramCounterDialog();
            } else {
                Autoclave.getInstance().setProgramsInRowTotal(1);
                Autoclave.getInstance().setCurrentProgramCounter(0);
            }
        } else {
            Toast.makeText(MonitorActivity.this, getString(R.string.go_to_main_menu_and_choose_program_first), Toast.LENGTH_LONG).show();
        }
    }


    public void setCurrentPagerItem(int item) {
        mViewPager.setCurrentItem(item);
    }


    @Override
    public void onProfileChange(Profile profile) {
        if (Autoclave.getInstance().getProfile() != null) {
            textProgram.setText(Autoclave.getInstance().getProfile().getName());
        } else {
            textProgram.setText("-");
        }

    }


    @Override
    public void onWarnListChange(ArrayList<Error> errorList) {
        if (errorList.size() > 0) {

        }
    }


    @Override
    public void onAutoclaveStateChange(AutoclaveState state) {
        textCycleCount.setText(getString(R.string.cycle) + " " + Autoclave.getInstance().getCurrentProgramCounter() + " " + getString(R.string.of) + " " + Autoclave.getInstance().getProgramsInRowTotal());

        switch (state) {
            case LOCKED:
                textState.setText(R.string.state_locked);
                buttonStop.setVisibility(View.INVISIBLE);
                if (AppConstants.IS_CERTOASSISTANT) {
                    buttonStop.setVisibility(View.GONE);
                }
                navigationbar.showButtonBack();
                break;
            case NOT_RUNNING:
                    textState.setText(R.string.state_not_running);
                    buttonStop.setVisibility(View.VISIBLE);
                    buttonStop.setPadding(0, 0, 0, 0);
                    buttonStop.setText("START");

                navigationbar.showButtonBack();
                break;
            case PREPARE_TO_RUN:
                if (AppConstants.IS_CERTOASSISTANT) {
                    buttonStop.setVisibility(View.GONE);
                } else {
                    buttonStop.setVisibility(View.VISIBLE);
                }
                buttonStop.setText("STARTING...");
                textState.setText(R.string.state_prepare_to_run);
                navigationbar.showButtonBack();
                break;
            case PROGRAM_FINISHED:
                buttonStop.setVisibility(View.VISIBLE);
                buttonStop.setEnabled(false);
                buttonStop.setText("PLEASE OPEN DOOR");
                textState.setText(R.string.state_finished);
                navigationbar.showButtonBack();

                AuditLogger.addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                        AuditLogger.ACTION_PROGRAM_FINISHED,
                        AuditLogger.OBJECT_EMPTY,
                        Autoclave.getInstance().getProfile().getName() + " (" + getString(R.string.cycle) + " " + Autoclave.getInstance().getCurrentProgramCounter() + ")");

                askForIndicator();
                break;
            case RUNNING:
                if (AppConstants.IS_CERTOASSISTANT) {
                    buttonStop.setVisibility(View.GONE);
                } else {
                    buttonStop.setVisibility(View.VISIBLE);
                }
                buttonStop.setText(R.string.stop);
                textState.setText(R.string.state_running);
                textState.append(" (");
                if(Autoclave.getInstance().getProgramStep().contains("SF1")){
                    textState.append("Vacuum Step 1 of " + Autoclave.getInstance().getProfile().getVacuumTimes());
                }else if(Autoclave.getInstance().getProgramStep().contains("SF2")) {
                    textState.append("Vacuum Step 2 of " + Autoclave.getInstance().getProfile().getVacuumTimes());
                }else if(Autoclave.getInstance().getProgramStep().contains("SF3")) {
                    textState.append("Vacuum Step 3 of " + Autoclave.getInstance().getProfile().getVacuumTimes());
                }else if(Autoclave.getInstance().getProgramStep().contains("SF4")) {
                    textState.append("Vacuum Step 4 of " + Autoclave.getInstance().getProfile().getVacuumTimes());
                }else if(Autoclave.getInstance().getProgramStep().contains("SH")) {
                    textState.append("Heating up to " + (int) Autoclave.getInstance().getProfile().getSterilisationTemperature() + "\u00B0C");
                }else if(Autoclave.getInstance().getProgramStep().contains("SS")) {
                    textState.append("Hold temperature for " + (int) Autoclave.getInstance().getProfile().getSterilisationTime() + " minutes");
                }else if(Autoclave.getInstance().getProgramStep().contains("SC")) {
                    textState.append("Cooling down");
                }else if(Autoclave.getInstance().getProgramStep().contains("SD")) {
                    textState.append("Drying for " + Autoclave.getInstance().getProfile().getDryTime() + " minutes");
                }else if(Autoclave.getInstance().getProgramStep().contains("SR")) {
                    textState.append("Pressure compensation");
                }else if(Autoclave.getInstance().getProgramStep().contains("SE")) {
                    textState.append("Program finished");
                }
                textState.append(")");

                navigationbar.hideButtonBack();

                // Insert the log one time
                if (!buttonStop.getText().equals(getString(R.string.stop))) {
                    AuditLogger.addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                            AuditLogger.ACTION_PROGRAM_STARTED,
                            AuditLogger.OBJECT_EMPTY,
                            Autoclave.getInstance().getProfile().getName() + " (" + getString(R.string.cycle) + " " + Autoclave.getInstance().getCurrentProgramCounter() + ")");
                }
                buttonStop.setText(R.string.stop);

                break;
            case RUN_CANCELED:
                buttonStop.setVisibility(View.INVISIBLE);
                if (AppConstants.IS_CERTOASSISTANT) {
                    buttonStop.setVisibility(View.GONE);
                }
                textState.setText(R.string.state_cancelled);
                navigationbar.showButtonBack();
                AuditLogger.addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                        AuditLogger.ACTION_PROGRAM_CANCELED,
                        AuditLogger.OBJECT_EMPTY,
                        Autoclave.getInstance().getProfile().getName() + " (" + getString(R.string.cycle) + " " + Autoclave.getInstance().getCurrentProgramCounter() + ")");
                break;
            case WAITING_FOR_CONFIRMATION:
                buttonStop.setVisibility(View.INVISIBLE);
                if (AppConstants.IS_CERTOASSISTANT) {
                    buttonStop.setVisibility(View.GONE);
                }
                textState.setText(R.string.state_wait_for_code);
                navigationbar.showButtonBack();
                break;
            default:
                buttonStop.setVisibility(View.INVISIBLE);
                if (AppConstants.IS_CERTOASSISTANT) {
                    buttonStop.setVisibility(View.GONE);
                }
                textState.setText(R.string.state_unknown);
                navigationbar.showButtonBack();
                break;

        }
        if (AppConstants.IS_CERTOASSISTANT) {
            buttonStop.setVisibility(View.GONE);
        }

    }


    @Override
    protected void onResume() {

        navigationbar.setNavigationbarListener(this);
        Autoclave.getInstance().setOnProfileListener(this);
        AutoclaveMonitor.getInstance().setOnAlertListener(this);
        Autoclave.getInstance().setOnAutoclaveStateListener(this);


        List<Profile> profilesFromDb = Autoclave.getInstance().getProfilesFromAutoclave();
        if (Autoclave.getInstance().getProfile() == null) {
                for (Profile profile : profilesFromDb) {
                    if (profile.getIndex() == Autoclave.getInstance().getIndexOfRunningProgram()) {
                        Autoclave.getInstance().setProfile(profile);
                        break;
                    }
                }
        }

        StringBuilder sbuilder = new StringBuilder();
        if (Autoclave.getInstance().getProfile().getVacuumTimes() != 0) {
            sbuilder.append(getString(R.string.vacuum_times) + " ")
                    .append(Autoclave.getInstance().getProfile().getVacuumTimes())
                    .append("\n");
        }

        if (Autoclave.getInstance().getProfile().getSterilisationTemperature() != 0) {
            sbuilder.append(getString(R.string.sterilization_temperature) + " ")
                    .append(Autoclave.getInstance().getProfile().getSterilisationTemperature())
                    .append(" " + "\u2103")
                    .append("\n");
        }

        if (Autoclave.getInstance().getProfile().getSterilisationPressure() != 0) {
            sbuilder.append(getString(R.string.sterilization_pressure) + " ")
                    .append(Float.toString(Autoclave.getInstance().getProfile().getSterilisationPressure()))
                    .append(" " + getString(R.string.bar))
                    .append("\n");
        }

        if (Autoclave.getInstance().getProfile().getSterilisationTime() != 0) {
            sbuilder.append(getString(R.string.sterilization_holding_time) + " ")
                    .append(Autoclave.getInstance().getProfile().getSterilisationTime())
                    .append(" " + getString(R.string.min))
                    .append("\n");
        }

        if (Autoclave.getInstance().getProfile().getVacuumPersistTemperature() != 0) {
            sbuilder.append(getString(R.string.vacuum_persist_temperature) + " ")
                    .append(Autoclave.getInstance().getProfile().getVacuumPersistTemperature())
                    .append(" " + "\u2103")
                    .append("\n");
        }
        if (Autoclave.getInstance().getProfile().getVacuumPersistTime() != 0) {
            sbuilder.append(getString(R.string.vacuum_persist_time) + " ")
                    .append(Autoclave.getInstance().getProfile().getVacuumPersistTime())
                    .append(" " + getString(R.string.min))
                    .append("\n");
        }
        if (Autoclave.getInstance().getProfile().getDryTime() != 0) {
            sbuilder.append(getString(R.string.drying_time) + " ")
                    .append(Autoclave.getInstance().getProfile().getDryTime())
                    .append(" " + getString(R.string.min));
        }

        textSteps.setText(sbuilder.toString());

        //update UI
        onAutoclaveStateChange(Autoclave.getInstance().getState());

        if (AppConstants.IS_CERTOASSISTANT) {
            buttonStop.setVisibility(View.GONE);
        }
        super.onResume();

    }


    @Override
    protected void onPause() {
        navigationbar.removeNavigationbarListener(this);
        Autoclave.getInstance().removeOnProfileListener(this);
        AutoclaveMonitor.getInstance().removeOnAlertListener(this);
        Autoclave.getInstance().removeOnAutoclaveStateListener(this);
        //finish();
        super.onPause();
    }


    @Override
    public void onClickNavigationbarButton(int buttonId) {
        switch (buttonId) {
            case CertoclavNavigationbarClean.BUTTON_BACK:
                if (!Autoclave.getInstance().getState().equals(AutoclaveState.RUNNING)) {
                    if (Autoclave.getInstance().getState().equals(AutoclaveState.PREPARE_TO_RUN)) {
                        AutoclaveMonitor.getInstance().cancelPrepareToRun();
                    }
                    finish();

                }
                break;
            case CertoclavNavigationbarClean.BUTTON_SETTINGS:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        Log.e("LoginActivity", "Hardware Button Back disabled");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            Log.e("LoginActivity", "Home Button disabled");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void showProgramCounterDialog() {

        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.dialog_program_counter, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.dialog_program_counter);
        dialog.setTitleText(getString(R.string.material_testing));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        Button dialogButtonNo = (Button) dialog.findViewById(R.id.dialogButtonNO);
        dialogButtonNo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        final EditText editCounterValue = (EditText) dialog.findViewById(R.id.dialog_counter_edit);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                int counterValue = 1;

                try {
                    counterValue = Integer.parseInt(editCounterValue.getText().toString());
                    if (counterValue <= 0) {
                        counterValue = 1;
                    }
                    if (counterValue > 1000) {
                        counterValue = 1000;
                    }
                } catch (Exception e) {

                }


                Autoclave.getInstance().setProgramsInRowTotal(counterValue);
                Autoclave.getInstance().setCurrentProgramCounter(0);

                dialog.dismiss();

            }
        });

        dialog.show();
    }

    private Double roundFloat(float f) {
        int tempnumber = (int) (f * 100);
        Double roundedfloat = (double) ((double) tempnumber / 100.0);
        return roundedfloat;
    }

    private void showStartNowOrLaterProgramDialog() {
        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.dialog_start_now_or_later, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.dialog_start_now_or_later);
        dialog.setTitle(R.string.register_new_user);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Button buttonStartLater = (Button) dialog
                .findViewById(R.id.dialogButtonStartLater);
        Button buttonStartNow = (Button) dialog
                .findViewById(R.id.dialogButtonStartNow);

        final Handler handler = new Handler();
        final TextView textViewTimer = (TextView) dialog.findViewById(R.id.dialog_start_now_timer);
        final int MAX_WAITING_TIME = 5;
        final int[] currentWaitingTimer = {MAX_WAITING_TIME};
        textViewTimer.setText(currentWaitingTimer[0] + "");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                textViewTimer.setText((--currentWaitingTimer[0]) + "");
                if (currentWaitingTimer[0] > 0)
                    handler.postDelayed(this, 1000);
                else {
                    if (dialog.isShowing()) {
                        dialog.dismissWithAnimation();
                        startProgram();
                    }
                }
            }
        };
        handler.postDelayed(runnable, 1000);
        buttonStartLater.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startProgramLater();
                dialog.dismissWithAnimation();
            }
        });

        buttonStartNow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startProgram();
                dialog.dismissWithAnimation();
            }
        });

        dialog.show();

    }


    private void startProgramLater() {
        DatePickerFragment newFragment = new DatePickerFragment();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(Autoclave.getInstance().getDateObject());
        newFragment.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        Intent intent = new Intent(MonitorActivity.this, ProgramTimerActivity.class);
                        intent.putExtra(ProgramTimerActivity.ARG_PROGRAM_NAME, Autoclave.getInstance().getProfile().getName());
                        intent.putExtra(ProgramTimerActivity.ARG_PROGRAM_STARTING_TIME, calendar.getTimeInMillis());
                        startActivityForResult(intent, 0);
                    }
                });
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            startProgram();
        }
    }

    private void askForIndicator() {
        if (!PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext()).getBoolean(AppConstants.PREFERENCE_KEY_INDICATOR_TEST, false))
            return;
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getString(R.string.title_indicator_test))
                .setContentText(getString(R.string.is_indicator_passed))
                .setConfirmText(getString(R.string.passed))
                .setCancelText(getString(R.string.failed))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        AuditLogger.addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                                AuditLogger.ACTION_PROGRAM_INDICATOR_CHANGED,
                                AuditLogger.OBJECT_EMPTY,
                                getString(R.string.failed));
                        new DatabaseService(ApplicationController.getContext()).updateProtocolErrorCode(Autoclave.getInstance().getProtocol().getProtocol_id(), AutoclaveMonitor.ERROR_CODE_INDICATOR_FAILED);
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        AuditLogger.addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                                AuditLogger.ACTION_PROGRAM_INDICATOR_CHANGED,
                                AuditLogger.OBJECT_EMPTY,
                                getString(R.string.success));
                    }
                })
                .setCustomImage(R.drawable.ic_indicator);
        sweetAlertDialog.setCanceledOnTouchOutside(false);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }
}
