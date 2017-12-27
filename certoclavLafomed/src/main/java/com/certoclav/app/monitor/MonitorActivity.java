package com.certoclav.app.monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
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
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.view.ControlPagerAdapter;
import com.certoclav.library.view.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class MonitorActivity extends CertoclavSuperActivity implements NavigationbarListener, ProfileListener, AlertListener, AutoclaveStateListener {

    private static final int INDEX_FRAGMANT_GRAPH = 0;
    private static final int INDEX_FRAGMENT_AUTOCLAVE = 1;
    private ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
    private CertoclavNavigationbarClean navigationbar;

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
                } else if (Autoclave.getInstance().getState().equals(AutoclaveState.PROGRAM_FINISHED)) {
                    //do nothing
                } else {
                    Log.e("MonitorActivity", "sendStopCommand");
                    if (Autoclave.getInstance().getState().equals(AutoclaveState.RUNNING)) {

                        String dialogTitletext = "";
                        String dialogContentText = "";
                        if(Autoclave.getInstance().getData().getTemp1().getCurrentValue() < 90 && Autoclave.getInstance().getData().getTemp2().getCurrentValue() <= 90 && Autoclave.getInstance().getSecondsSinceStart() > 2400 && Autoclave.getInstance().getIndexOfRunningProgram() == 12 ){
                            dialogTitletext = getString(R.string.stop_program_earlier);
                            dialogContentText = getString(R.string.do_you_want_to_stop_the_program_earlier_sterilization_result_will_be_successfull_);
                        }else{
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
                if (Autoclave.getInstance().getData().isDoorLocked() == true) {
                    textState.setText(getString(R.string.wait_until_door_is_unlocked).toUpperCase());
                    buttonStop.setVisibility(View.INVISIBLE);
                } else {
                    textState.setText(R.string.state_not_running);
                    buttonStop.setVisibility(View.VISIBLE);
                    buttonStop.setPadding(0, 0, 0, 0);
                    buttonStop.setText("START");
                }
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
                buttonStop.setText("PLEASE OPEN DOOR");
                textState.setText(R.string.state_finished);
                navigationbar.showButtonBack();
                break;
            case RUNNING:
                if (AppConstants.IS_CERTOASSISTANT) {
                    buttonStop.setVisibility(View.GONE);
                } else {
                    buttonStop.setVisibility(View.VISIBLE);
                }
                buttonStop.setText(R.string.stop);
                textState.setText(R.string.state_running);
                navigationbar.hideButtonBack();
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
        if(AppConstants.IS_CERTOASSISTANT){
            buttonStop.setVisibility(View.GONE);
        }

    }


    @Override
    protected void onResume() {

        navigationbar.setNavigationbarListener(this);
        Autoclave.getInstance().setOnProfileListener(this);
        AutoclaveMonitor.getInstance().setOnAlertListener(this);
        Autoclave.getInstance().setOnAutoclaveStateListener(this);

        DatabaseService db = new DatabaseService(this);
        List<Profile> profilesFromDb = db.getProfiles();
        if(Autoclave.getInstance().getProfile() == null){
            if(Autoclave.getInstance().getIndexOfRunningProgram() == 7){
                Autoclave.getInstance().setProfile(Autoclave.getInstance().getUserDefinedProgram());
            }else{
                for(Profile profile : profilesFromDb){
                    if(profile.getIndex() == Autoclave.getInstance().getIndexOfRunningProgram()){
                        Autoclave.getInstance().setProfile(profile);
                        break;
                    }
                }
            }
        }else{
            if(Autoclave.getInstance().getProfile().getIndex() == 7){
                Autoclave.getInstance().setProfile(Autoclave.getInstance().getUserDefinedProgram());
            }
        }

        StringBuilder sbuilder = new StringBuilder();
        if (Autoclave.getInstance().getProfile().getVacuumTimes() != 0) {
            sbuilder.append(getString(R.string.vacuum_times)+" ")
                    .append(Autoclave.getInstance().getProfile().getVacuumTimes())
                    .append("\n");
        }

        if (Autoclave.getInstance().getProfile().getSterilisationTemperature() != 0) {
            sbuilder.append(getString(R.string.sterilization_temperature)+" ")
                    .append(Autoclave.getInstance().getProfile().getSterilisationTemperature())
                    .append(" "+"\u2103")
                    .append("\n");
        }

        if (Autoclave.getInstance().getProfile().getSterilisationPressure() != 0) {
            sbuilder.append(getString(R.string.sterilization_pressure)+" ")
                    .append( Integer.toString(Autoclave.getInstance().getProfile().getSterilisationPressure()))
                    .append(" "+getString(R.string.kpa))
                    .append("\n");
        }

        if (Autoclave.getInstance().getProfile().getSterilisationTime() != 0) {
            sbuilder.append(getString(R.string.sterilization_holding_time)+" ")
                    .append(Autoclave.getInstance().getProfile().getSterilisationTime())
                    .append(" "+getString(R.string.min))
                    .append("\n");
        }

        if (Autoclave.getInstance().getProfile().getVacuumPersistTemperature() != 0) {
            sbuilder.append(getString(R.string.vacuum_persist_temperature)+" ")
                    .append(Autoclave.getInstance().getProfile().getVacuumPersistTemperature())
                    .append(" "+"\u2103")
                    .append("\n");
        }
        if (Autoclave.getInstance().getProfile().getVacuumPersistTime() != 0) {
            sbuilder.append(getString(R.string.vacuum_persist_time)+" ")
                    .append(Autoclave.getInstance().getProfile().getVacuumPersistTime())
                    .append(" "+getString(R.string.min))
                    .append("\n");
        }
        if (Autoclave.getInstance().getProfile().getDryTime() != 0) {
            sbuilder.append(getString(R.string.drying_time)+" ")
                    .append(Autoclave.getInstance().getProfile().getDryTime())
                    .append(" "+getString(R.string.min));
        }

        textSteps.setText(sbuilder.toString());

        //update UI
        onAutoclaveStateChange(Autoclave.getInstance().getState());

        if(AppConstants.IS_CERTOASSISTANT){
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

    private Double roundFloat(float f){
        int tempnumber = (int) (f*100);
        Double roundedfloat = (double) ((double)tempnumber/100.0);
        return roundedfloat;
    }

}
