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
import android.view.MotionEvent;
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
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.model.Error;
import com.certoclav.app.service.PostProtocolsService;
import com.certoclav.app.settings.SettingsActivity;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.app.util.Helper;
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
    private Autoclave.PROGRAM_STEPS currentProgramStep = Autoclave.PROGRAM_STEPS.NOT_DEFINED;

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
    private SweetAlertDialog sweetAlertDialogCanNotStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_activity);
        navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.title_monitor));
        if (AppConstants.APPLICATION_DEBUGGING_MODE == true) {
            navigationbar.setSettingsVisible();
        }
        textSteps = findViewById(R.id.monitor_text_steps);
        textCycleCount = findViewById(R.id.monitor_text_cycle_count);
        textProgram = findViewById(R.id.monitor_text_programname);
        textState = findViewById(R.id.monitor_text_state);
        buttonStop = findViewById(R.id.monitor_button_stop);

        if (Autoclave.getInstance().getProfile() != null) {
            textProgram.setText(Autoclave.getInstance().getProfile().getName());
        } else {
            textProgram.setText("-");
        }

        //Force Stop Program
        buttonStop.setOnTouchListener(new View.OnTouchListener() {
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    //Force Stop
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MonitorActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.stop_program_forcibly))
                            .setContentText(getString(R.string.do_you_want_to_stop_program_force))
                            .setConfirmText(getString(R.string.yes))
                            .setCancelText(getString(R.string.cancel))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    AutoclaveMonitor.getInstance().sendStopCommand(true);
                                    buttonStop.setText(R.string.stopping_);
                                }
                            }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            });
                    sweetAlertDialog.setCanceledOnTouchOutside(false);
                    sweetAlertDialog.setCancelable(false);
                    sweetAlertDialog.show();
                }
            };

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.postDelayed(runnable, AppConstants.FORCE_STOP_DELAY);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_HOVER_EXIT:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(runnable);
                        break;
                }
                return false;
            }
        });


        buttonStop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                if (Autoclave.getInstance().getState().equals(AutoclaveState.NOT_RUNNING)) {
                    showStartNowOrLaterProgramDialog();
                } else if (Autoclave.getInstance().getState().equals(AutoclaveState.PROGRAM_FINISHED)
                        && Autoclave.getInstance().getProgramStep() != Autoclave.PROGRAM_STEPS.MAINTAIN_TEMP) {
                    //do nothing
                } else {

                    if (Autoclave.getInstance().getState().equals(AutoclaveState.RUNNING) &&
                            !Autoclave.getInstance().isDoorLocked()) {
                        final SensorDataListener sensorDataListener = new SensorDataListener() {
                            @Override
                            public void onSensorDataChange(AutoclaveData data) {
                                if (sweetAlertDialogCanNotStop != null && sweetAlertDialogCanNotStop.isShowing()) {
                                    int percent = (int) ((data.getPress().getCurrentValue() * 100f) / -0.3);
                                    percent = Math.min(percent, 100);
                                    sweetAlertDialogCanNotStop.setContentText(
                                            getString(R.string.can_not_stop_program_please_wait_door_closing,
                                                    percent) + " % )");
                                    sweetAlertDialogCanNotStop.setConfirmButtonEnable(data.isDoorLocked());
                                }
                            }
                        };
                        sweetAlertDialogCanNotStop = new SweetAlertDialog(MonitorActivity.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(getString(R.string.can_not_stop_program))
                                .setContentText(getString(R.string.can_not_stop_program_please_wait_door_closing, 0))
                                .setConfirmText(getString(R.string.stop_program))
                                .setCancelText(getString(R.string.cancel))
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        v.callOnClick();
                                        sweetAlertDialog.dismissWithAnimation();
                                        Autoclave.getInstance().removeOnSensorDataListener(sensorDataListener);
                                    }
                                })
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        Autoclave.getInstance().removeOnSensorDataListener(sensorDataListener);
                                    }
                                });

                        Autoclave.getInstance().setOnSensorDataListener(sensorDataListener);
                        sweetAlertDialogCanNotStop.show();
                        sweetAlertDialogCanNotStop.setConfirmButtonEnable(false);
                        return;

                    }
                    Log.e("MonitorActivity", "sendStopCommand");
                    if (Autoclave.getInstance().getState().equals(AutoclaveState.RUNNING)
                            || Autoclave.getInstance().getProgramStep() == Autoclave.PROGRAM_STEPS.MAINTAIN_TEMP) {

                        String dialogTitleText = getString(Autoclave.getInstance().getProgramStep() == Autoclave.PROGRAM_STEPS.MAINTAIN_TEMP ?
                                R.string.stop_maintain_temp :
                                R.string.stop_program);
                        String dialogContentText = getString(
                                Autoclave.getInstance().getProgramStep() == Autoclave.PROGRAM_STEPS.MAINTAIN_TEMP ?
                                        R.string.do_you_really_want_to_stop_maintain_temp :
                                        R.string.do_you_really_want_to_stop_the_running_program_);


                        try {
                            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MonitorActivity.this, SweetAlertDialog.WARNING_TYPE)
                                    .setTitleText(dialogTitleText)
                                    .setContentText(dialogContentText)
                                    .setConfirmText(getString(R.string.yes))
                                    .setCancelText(getString(R.string.cancel))
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sDialog) {
                                            sDialog.dismissWithAnimation();
                                            AutoclaveMonitor.getInstance().sendStopCommand(false);
                                            buttonStop.setText(R.string.stopping_);
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
    public void onWarnListChange(ArrayList<Error> errorList, ArrayList<Error> warningList) {
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
                buttonStop.setText(R.string.start);
                buttonStop.setEnabled(true);

                navigationbar.showButtonBack();
                break;
            case PREPARE_TO_RUN:
                if (AppConstants.IS_CERTOASSISTANT) {
                    buttonStop.setVisibility(View.GONE);
                } else {
                    buttonStop.setVisibility(View.VISIBLE);
                }
                buttonStop.setText(R.string.starting_);
                textState.setText(R.string.state_prepare_to_run);
                navigationbar.showButtonBack();
                break;
            case DOOR_UNLOCKED:
            case PROGRAM_FINISHED:
                buttonStop.setEnabled(false);
                buttonStop.setText(getString(Autoclave.getInstance().isDoorLocked() ?
                        R.string.please_wait_door_unlocking :
                        R.string.please_open_door));

                if (currentProgramStep == Autoclave.getInstance().getProgramStep())
                    break;

                currentProgramStep = Autoclave.getInstance().getProgramStep();

                buttonStop.setVisibility(View.VISIBLE);
                if (currentProgramStep == Autoclave.PROGRAM_STEPS.MAINTAIN_TEMP) {
                    textState.setText(getString(R.string.success_sterilization_and_maintain_temp,
                            Autoclave.getInstance().getProfile().getFinalTemp(false)));
                    buttonStop.setText(getString(R.string.stop_maintain_temp));
                    buttonStop.setEnabled(true);
                } else {
                    textState.setText(R.string.state_finished);
                }

                AuditLogger.addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                        currentProgramStep == Autoclave.PROGRAM_STEPS.MAINTAIN_TEMP ?
                                AuditLogger.ACTION_PROGRAM_FINISHED_MAINTAIN_TEMP :
                                AuditLogger.ACTION_PROGRAM_FINISHED,
                        AuditLogger.OBJECT_EMPTY,
                        Autoclave.getInstance().getProfile().getName() + " (" + getString(R.string.cycle) + " "
                                + Autoclave.getInstance().getController().getCycleNumber() + ")");


                if (currentProgramStep == Autoclave.PROGRAM_STEPS.FINISHED) {
                    askForIndicator();
                } else {
                    startProtocolSync();
                }

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
                textState.append(Helper.getStateText());
                textState.append(")");

                navigationbar.hideButtonBack();
                buttonStop.setText(R.string.stop);

                break;
            case RUN_CANCELED:
                buttonStop.setVisibility(View.INVISIBLE);
                if (AppConstants.IS_CERTOASSISTANT) {
                    buttonStop.setVisibility(View.GONE);
                }
                textState.setText(R.string.state_cancelled);
                navigationbar.showButtonBack();

                break;
            case WAITING_FOR_CONFIRMATION:
                buttonStop.setVisibility(View.VISIBLE);

                buttonStop.setText(getString(Autoclave.getInstance().isDoorLocked() ?
                        R.string.please_wait_door_unlocking :
                        R.string.please_open_door));
                buttonStop.setEnabled(false);

                if (Autoclave.getInstance().getProgramStep() == Autoclave.PROGRAM_STEPS.MAINTAIN_TEMP) {
                    buttonStop.setEnabled(true);
                    buttonStop.setText(R.string.stop_maintain_temp);
                }

                if (Autoclave.getInstance().getData().isProgramRunning()) {
                    textState.setText(R.string.state_stopping);
                    textState.append(" (");
                    textState.append(Helper.getStateText());
                    textState.append(")");
                } else {
                    textState.setText(R.string.state_finished);
                }
                navigationbar.hideButtonBack();
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
        textSteps.setText(Autoclave.getInstance().getProfile().getDescription(true));

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

    private void startProtocolSync() {
        if (Autoclave.getInstance().isOnlineMode(this)) {
            Intent intent5 = new Intent(this, PostProtocolsService.class);
            startService(intent5);
        }
    }

    private void askForIndicator() {
        if (!PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext()).getBoolean(AppConstants.PREFERENCE_KEY_INDICATOR_TEST, false))
            return;
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.title_indicator_test))
                .setContentText(getString(R.string.is_indicator_ready))
                .setConfirmText(getString(R.string.later))
                .setCancelText(getString(R.string.yes))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        askForIndicatorStatus();
                        sweetAlertDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        AuditLogger.addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                                AuditLogger.ACTION_PROGRAM_INDICATOR_CHANGED,
                                AuditLogger.OBJECT_EMPTY,
                                getString(R.string.later));
                        DatabaseService.getInstance().updateProtocolErrorCode(Autoclave.getInstance().getProtocol().getProtocol_id(),
                                AutoclaveMonitor.ERROR_CODE_INDICATOR_NOT_COMPLETED);
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
        sweetAlertDialog.setCanceledOnTouchOutside(false);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }

    private void askForIndicatorStatus() {
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
                        DatabaseService.getInstance().updateProtocolErrorCode(Autoclave.getInstance().getProtocol().getProtocol_id(), AutoclaveMonitor.ERROR_CODE_INDICATOR_FAILED);
                        sweetAlertDialog.dismissWithAnimation();
                        startProtocolSync();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        AuditLogger.addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_EMPTY,
                                AuditLogger.ACTION_PROGRAM_INDICATOR_CHANGED,
                                AuditLogger.OBJECT_EMPTY,
                                getString(R.string.success));
                        DatabaseService.getInstance().updateProtocolErrorCode(Autoclave.getInstance().getProtocol().getProtocol_id(), AutoclaveMonitor.ERROR_CODE_INDICATOR_SUCCESS);
                        sweetAlertDialog.dismissWithAnimation();
                        startProtocolSync();
                    }
                })
                .setCustomImage(R.drawable.ic_indicator);
        sweetAlertDialog.setCanceledOnTouchOutside(false);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }
}
