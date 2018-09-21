package com.certoclav.app.menu;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.fragments.ProgramDefinitionGraphFragment;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.InputFilterMinMax;
import com.certoclav.app.util.MyCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class EditProgramActivity extends CertoclavSuperActivity implements NavigationbarListener, View.OnClickListener {

    private ProgramDefinitionGraphFragment graphFragment;
    private int programIndex;
    private Profile newProfile;

    private CertoclavNavigationbarClean navigationbar;
    private EditText editTextProgramName;
    private CheckBox checkboxIdLiquidProgram;
    private CheckBox checkboxIsF0FunctionProgram;
    private TextView programStepSterilisationDescription;
    private TextView programStepDryDescription;
    private TextView programStepF0FunctionDescription;
    private TextView programStepVacuumDescription;
    private View linearLayoutF0Function;


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //now getIntent() should always return the last received intent
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_program);

        editTextProgramName = findViewById(R.id.editTextProgramName);
        checkboxIdLiquidProgram = findViewById(R.id.checkboxIdLiquidProgram);
        checkboxIsF0FunctionProgram = findViewById(R.id.checkboxIsF0FunctionEnabled);
        programStepSterilisationDescription = findViewById(R.id.program_step_sterilisation_description);
        programStepDryDescription = findViewById(R.id.program_step_dry_description);
        programStepF0FunctionDescription = findViewById(R.id.program_step_f0_function_description);
        programStepVacuumDescription = findViewById(R.id.program_step_vacuum_description);

        findViewById(R.id.linearLayoutSterilisation).setOnClickListener(this);
        linearLayoutF0Function = findViewById(R.id.linearLayoutF0Function);
        linearLayoutF0Function.setOnClickListener(this);
        linearLayoutF0Function.setVisibility(checkboxIsF0FunctionProgram.isChecked() ? View.VISIBLE : View.GONE);
        checkboxIsF0FunctionProgram.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                linearLayoutF0Function.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
        findViewById(R.id.linearLayoutDry).setOnClickListener(this);
        findViewById(R.id.linearLayoutVacuum).setOnClickListener(this);

        navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.title_define_a_new_program));
        navigationbar.setSaveVisible();

        graphFragment = (ProgramDefinitionGraphFragment) getSupportFragmentManager().findFragmentById(R.id.program_defintion_fragment_graph);

        if (getIntent().hasExtra(AppConstants.INTENT_EXTRA_PROFILE_ID)) {
            programIndex = getIntent().getIntExtra(AppConstants.INTENT_EXTRA_PROFILE_ID, -1);
        } else {

        }
//        newProfile.setName(s.toString());
//        refreshGraphAndList();


    }


    private void refreshGraphAndList(boolean isEditted) {

        Protocol protocol = new Protocol(newProfile.getCloudId(), newProfile.getVersion(),
                null, null, 0, null,
                null, newProfile, 0, false);
        List<ProtocolEntry> entries = new ArrayList<>();
        protocol.setProtocolEntries(entries);
        Date date = Autoclave.getInstance().getDateObject();
        Calendar calendar = Calendar.getInstance();
        int vacuumTime = newProfile.getVacuumTimes();
        int temp = 20;
        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, 0, protocol, "", ""));
        calendar.add(Calendar.MINUTE, 1);
        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, 0, protocol, "", ""));
        for (int i = 0; i < vacuumTime - 1; i++) {
            calendar.add(Calendar.MINUTE, 5);
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, -84, protocol, "", ""));
            calendar.add(Calendar.MINUTE, 5);
            temp = 100;
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, 50, protocol, "", ""));
            temp = 60;
        }

        calendar.add(Calendar.MINUTE, 5);
        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, -84, protocol, "", ""));
        calendar.add(Calendar.MINUTE, 7);

        temp = (int) newProfile.getSterilisationTemperature();
        float pressure;
        //converts temperature [�C] to pressure [bar relative], relative means, atmoshperic pressure is 0 bar

        if (temp >= 100) {
            pressure = (float) (0.006112 * Math.exp((17.62 * temp) / (243.12 + temp)) - 1);
        } else {
            pressure = (float) 0;
        }
        pressure = (float) Math.round(pressure * 100) / 100;

        //converts pressure [bar relative] to pressure [kpa relative], relative means, atmoshperic pressure is 0 kPa

        int pressurekpa = (int) (pressure * 100);

        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, pressurekpa, protocol, "", ""));
        calendar.add(Calendar.MINUTE, newProfile.getSterilisationTime());
        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, pressurekpa, protocol, "", ""));

        if (newProfile.getDryTime() > 0) {
            calendar.add(Calendar.MINUTE, 5);
            temp = 67;
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, -84, protocol, "", ""));
            calendar.add(Calendar.MINUTE, newProfile.getDryTime());
            temp = 68;
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, -84, protocol, "", ""));
            calendar.add(Calendar.MINUTE, 1);
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, 0, protocol, "", ""));
        } else {
            calendar.add(Calendar.MINUTE, 5);
            temp = 67;
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, 0, protocol, "", ""));
        }

        graphFragment.setProtocol(protocol);
        graphFragment.notifyDataChanged();

        if (!isEditted) {
            editTextProgramName.setText(newProfile.getName());
            checkboxIdLiquidProgram.setChecked(newProfile.isLiquidProgram());
            checkboxIsF0FunctionProgram.setChecked(newProfile.isF0Enabled());
        }
        programStepSterilisationDescription.setText(getString(R.string.program_step_sterlisation_desc, newProfile.getSterilisationTime(), newProfile.getSterilisationTemperature()));
        programStepF0FunctionDescription.setText(getString(R.string.program_step_f0_function_desc, newProfile.getLethalTemp(), newProfile.getzValue()));
        programStepDryDescription.setText(getString(R.string.program_step_dry_desc, newProfile.getDryTime()));
        programStepVacuumDescription.setText(getString(R.string.program_step_vacuum_desc, newProfile.getVacuumTimes()));

    }


    @Override
    public void onClickNavigationbarButton(int buttonId) {
        switch (buttonId) {
            case CertoclavNavigationbarClean.BUTTON_BACK:
                try {
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(getString(R.string.cancel_program_definition))
                            .setContentText(getString(R.string.do_you_really_want_to_return_without_saving_the_program_))
                            .setConfirmText(getString(R.string.yes))
                            .setCancelText(getString(R.string.cancel))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    finish();
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
                break;
            case CertoclavNavigationbarClean.BUTTON_SAVE:
                String name = editTextProgramName.getText().toString();
                name = name.replaceAll(" ", "_");
                if (!(name.length() > 0 && !name.contains(",") && !name.contains(";") && !name.contains("\n") && !name.equals(AppConstants.DELETED_PROFILE_NAME))) {
                    Toast.makeText(this, getString(R.string.please_enter_a_valid_name), Toast.LENGTH_SHORT).show();
                    break;
                }
                newProfile.setName(name);
                newProfile.setLiquidProgram(checkboxIdLiquidProgram.isChecked());
                newProfile.setF0Enabled(checkboxIsF0FunctionProgram.isChecked());
                Helper.setProgram(this, newProfile, new MyCallback() {
                    @Override
                    public void onSuccess(Object response, int requestId) {
                        Toast.makeText(getApplicationContext(), R.string.program_saved, Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
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
                });

                break;
        }

    }


    @Override
    protected void onPause() {
        navigationbar.removeNavigationbarListener(this);
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();

        navigationbar.setNavigationbarListener(this);
        newProfile = Autoclave.getInstance().getProfileByIndex(programIndex);
//        newProfile.setLocal(true);

        refreshGraphAndList(false);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            Log.d("Activity", "Touch event " + event.getRawX() + "," + event.getRawY() + " " + x + "," + y + " rect " + w.getLeft() + "," + w.getTop() + "," + w.getRight() + "," + w.getBottom() + " coords " + scrcoords[0] + "," + scrcoords[1]);
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linearLayoutSterilisation:
                showEditSterilisationDialog();
                break;
            case R.id.linearLayoutF0Function:
                showEditF0FunctionnDialog();
                break;
            case R.id.linearLayoutDry:
                showEditDryOrVacuum(false);
                break;
            case R.id.linearLayoutVacuum:
                showEditDryOrVacuum(true);
                break;
        }
    }

    private void showEditSterilisationDialog() {

        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.program_definition_fragment_editstep, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.program_definition_fragment_editstep);
        dialog.setTitle(R.string.register_new_user);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        //get views
        final EditText editTemp = dialog.findViewById(R.id.program_definition_editstep_edit_temp);
        final EditText editTime = dialog.findViewById(R.id.program_definition_editstep_edit_time);
        editTemp.setFilters(new InputFilterMinMax[]{new InputFilterMinMax(0, AppConstants.TEMP_MAX_INT)});

        //insert default values
        editTime.setText(Integer.toString(newProfile.getSterilisationTime()));
        editTemp.setText(Float.toString(newProfile.getSterilisationTemperature()));

        dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
            }
        });

        dialog.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newProfile.setSterilisationTime(Integer.valueOf(editTime.getText().toString()));
                newProfile.setSterilisationTemperature(Math.max(AppConstants.TEMP_MIN_INT, Float.valueOf(editTemp.getText().toString())));
                refreshGraphAndList(true);
                dialog.dismissWithAnimation();
            }
        });

        dialog.show();
    }

    private void showEditF0FunctionnDialog() {

        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.program_definition_fragment_edit_f0_function, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.program_definition_fragment_edit_f0_function);
        dialog.setTitle(R.string.register_new_user);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        //get views
        final EditText editLethalTemp = dialog.findViewById(R.id.program_definition_edit_lethal);
        final EditText editZValue = dialog.findViewById(R.id.program_definition_edit_z_value);
        editLethalTemp.setFilters(new InputFilterMinMax[]{new InputFilterMinMax(0, AppConstants.TEMP_MAX_INT)});

        //insert default values
        editZValue.setText(Float.toString(newProfile.getzValue()));
        editLethalTemp.setText(Float.toString(newProfile.getLethalTemp()));

        dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
            }
        });

        dialog.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newProfile.setzValue(Float.valueOf(editZValue.getText().toString()));
                newProfile.setLethalTemp(Float.valueOf(editLethalTemp.getText().toString()));
                refreshGraphAndList(true);
                dialog.dismissWithAnimation();
            }
        });

        dialog.show();
    }

    private void showEditDryOrVacuum(final boolean isVacuum) {

        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.program_definition_vacuum_dry_dialog, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.program_definition_vacuum_dry_dialog);
        dialog.setTitle(R.string.register_new_user);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        //get views
        final TextView textViewParameterName = dialog.findViewById(R.id.textViewParameterName);
        final TextView textViewParameterType = dialog.findViewById(R.id.textViewParameterType);
        final EditText editParameter = dialog.findViewById(R.id.program_definition_parameter);
        editParameter.setFilters(new InputFilterMinMax[]{new InputFilterMinMax(isVacuum ? 1 : 0, isVacuum ? 3 : 19)});

        //insert default values
        editParameter.setText(Integer.toString(isVacuum ? newProfile.getVacuumTimes() : newProfile.getDryTime()));

        textViewParameterName.setText(isVacuum ? R.string.vacuum_pulse : R.string.drying_time_title);
        textViewParameterType.setText(isVacuum ? R.string.vacuum_pulse_times : R.string.drying_time_minutes);

        dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
            }
        });

        dialog.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVacuum) {
                    newProfile.setVacuumTimes(Integer.valueOf(editParameter.getText().toString()));
                } else
                    newProfile.setDryTime(Integer.valueOf(editParameter.getText().toString()));
                refreshGraphAndList(true);
                dialog.dismissWithAnimation();
            }
        });

        dialog.show();
    }

}
