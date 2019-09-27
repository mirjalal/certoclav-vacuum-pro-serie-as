package com.certoclav.app.menu;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
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
import com.certoclav.app.util.AuditLogger;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.InputFilterMinMax;
import com.certoclav.app.util.MyCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;


public class EditProgramActivity extends CertoclavSuperActivity implements NavigationbarListener, View.OnClickListener {

    private ProgramDefinitionGraphFragment graphFragment;
    private int programIndex;
    private AutoclaveModelManager manager;

    private Profile newProfile;
    private Profile oldProfile;
    private CertoclavNavigationbarClean navigationbar;
    private EditText editTextProgramName;
    private CheckBox checkboxIdLiquidProgram;
    private CheckBox checkboxIsF0FunctionProgram;
    private CheckBox checkboxMaintainFinalTemp;
    private CheckBox checkboxIsContByFlexProbe1;
    private CheckBox checkboxIsContByFlexProbe2;
    private TextView programStepSterilisationDescription;
    private TextView programStepDryDescription;
    private TextView programStepF0FunctionDescription;
    private TextView programStepVacuumDescription;
    private TextView programStepFinalTempDescription;
    private View linearLayoutF0Function;
    private View linearLayoutFinalTemp;
    private View linearLayoutDry;

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

        manager = AutoclaveModelManager.getInstance();
        editTextProgramName = findViewById(R.id.editTextProgramName);
        checkboxIdLiquidProgram = findViewById(R.id.checkboxIdLiquidProgram);
        checkboxIsF0FunctionProgram = findViewById(R.id.checkboxIsF0FunctionEnabled);
        checkboxIsContByFlexProbe1 = findViewById(R.id.checkboxIsContByFlexProbe1);
        checkboxIsContByFlexProbe2 = findViewById(R.id.checkboxIsContByFlexProbe2);
        checkboxMaintainFinalTemp = findViewById(R.id.checkboxMaintainFinalTemp);
        programStepSterilisationDescription = findViewById(R.id.program_step_sterilisation_description);
        programStepDryDescription = findViewById(R.id.program_step_dry_description);
        programStepF0FunctionDescription = findViewById(R.id.program_step_f0_function_description);
        programStepVacuumDescription = findViewById(R.id.program_step_vacuum_description);
        programStepFinalTempDescription = findViewById(R.id.program_step_final_temp_description);

        linearLayoutDry = findViewById(R.id.linearLayoutDry);

        checkboxIsContByFlexProbe1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkboxIsContByFlexProbe2.setEnabled(b && checkboxIdLiquidProgram.isChecked());
            }
        });
        checkboxIdLiquidProgram.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkboxIsContByFlexProbe1.setEnabled(b);
                checkboxIsContByFlexProbe2.setEnabled(b && checkboxIsContByFlexProbe1.isChecked());
                if (manager.isDryTimeExists())
                    linearLayoutDry.setVisibility(b ? View.GONE : View.VISIBLE);
            }
        });

        findViewById(R.id.linearLayoutSterilisation).setOnClickListener(this);
        linearLayoutF0Function = findViewById(R.id.linearLayoutF0Function);
        linearLayoutF0Function.setOnClickListener(this);
        linearLayoutF0Function.setVisibility(checkboxIsF0FunctionProgram.isChecked() ? View.VISIBLE : View.GONE);

        findViewById(R.id.linearLayoutDry).setOnClickListener(this);
        findViewById(R.id.linearLayoutVacuum).setOnClickListener(this);
        linearLayoutFinalTemp = findViewById(R.id.linearLayoutFinalTemp);
        linearLayoutFinalTemp.setOnClickListener(this);


        navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.title_define_a_new_program));
        navigationbar.setSaveVisible();

        graphFragment = (ProgramDefinitionGraphFragment) getSupportFragmentManager().findFragmentById(R.id.program_defintion_fragment_graph);

        if (getIntent().hasExtra(AppConstants.INTENT_EXTRA_PROFILE_ID)) {
            programIndex = getIntent().getIntExtra(AppConstants.INTENT_EXTRA_PROFILE_ID, -1);
            navigationbar.setHeadText(getString(R.string.title_edit_a_program));
        } else {

        }

        //Change Program parameters according to the current Autoclave model
        if (!manager.isMaintaingingTempExistsInProgram()) {
            checkboxMaintainFinalTemp.setChecked(false);
            checkboxMaintainFinalTemp.setVisibility(View.GONE);
        }

        if (!manager.isF0Exists()) {
            checkboxIsF0FunctionProgram.setChecked(false);
            checkboxIsF0FunctionProgram.setVisibility(View.GONE);
        }

        findViewById(R.id.linearLayoutDry).setVisibility(manager.isDryTimeExists() ? View.VISIBLE : View.GONE);

//        newProfile.setName(s.toString());
//        refreshGraphAndList();
        if (manager.isDryTimeExists())
            linearLayoutDry.setVisibility(checkboxIdLiquidProgram.isChecked() ? View.GONE : View.VISIBLE);

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
        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, 0, protocol, "", ""));
        calendar.add(Calendar.MINUTE, 1);
        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, 0, protocol, "", ""));
        for (int i = 0; i < vacuumTime - 1; i++) {
            calendar.add(Calendar.MINUTE, 5);
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, -0.84f, protocol, "", ""));
            calendar.add(Calendar.MINUTE, 5);
            temp = 100;
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, 0.50f, protocol, "", ""));
            temp = 60;
        }

        calendar.add(Calendar.MINUTE, 5);
        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, -0.84f, protocol, "", ""));
        calendar.add(Calendar.MINUTE, 7);

        temp = (int) newProfile.getSterilisationTemperature(false);
        float pressure;
        //converts temperature [�C] to pressure [bar relative], relative means, atmoshperic pressure is 0 bar

        if (temp >= 100) {
            pressure = (float) (0.006112 * Math.exp((17.62 * Helper.getInstance().currentUnitToCelsius(temp)) / (243.12 + Helper.getInstance().currentUnitToCelsius(temp))) - 1);
        } else {
            pressure = (float) 0;
        }
        pressure = (float) Math.round(pressure * 100) / 100;

        //converts pressure [bar relative] to pressure [kpa relative], relative means, atmoshperic pressure is 0 kPa

        int pressurekpa = (int) (pressure);

        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, pressurekpa, protocol, "", ""));
        calendar.add(Calendar.MINUTE, newProfile.getSterilisationTime());
        entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, pressurekpa, protocol, "", ""));

        if (newProfile.getDryTime() > 0) {
            calendar.add(Calendar.MINUTE, 5);
            temp = (int) newProfile.getFinalTemp(false);
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, -0.84f, protocol, "", ""));
            calendar.add(Calendar.MINUTE, newProfile.getDryTime());
            temp = (int) newProfile.getFinalTemp(false);
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, -0.84f, protocol, "", ""));
            calendar.add(Calendar.MINUTE, 1);
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, 0, protocol, "", ""));
        } else {
            calendar.add(Calendar.MINUTE, 5);
            temp = (int) newProfile.getFinalTemp(false);
            entries.add(new ProtocolEntry(calendar.getTime(), temp, temp, temp, 0, protocol, "", ""));
        }

        graphFragment.setProtocol(protocol);
        graphFragment.notifyDataChanged();

        if (!isEditted) {
            editTextProgramName.setText(newProfile.getName());
            checkboxIdLiquidProgram.setChecked(newProfile.isLiquidProgram());
            checkboxIsF0FunctionProgram.setChecked(newProfile.isF0Enabled());
            checkboxIsContByFlexProbe1.setChecked(newProfile.isContByFlexProbe1Enabled());
            checkboxIsContByFlexProbe2.setChecked(newProfile.isContByFlexProbe2Enabled());
            checkboxMaintainFinalTemp.setChecked(newProfile.isMaintainEnabled());
        }

        linearLayoutF0Function.setVisibility(newProfile.isF0Enabled() ? View.VISIBLE : View.GONE);

        if (!checkboxIsF0FunctionProgram.isChecked())
            programStepSterilisationDescription.setText(getString(R.string.program_step_sterlisation_desc,
                    newProfile.getSterilisationTime(),
                    newProfile.getSterilisationTemperature(false),
                    Helper.getInstance().getTemperatureUnitText(null)));
        else
            programStepSterilisationDescription.setText(getString(R.string.program_step_sterlisation_desc_f0,
                    newProfile.getSterilisationTemperature(false),
                    Helper.getInstance().getTemperatureUnitText(null)));
        programStepF0FunctionDescription.setText(getString(R.string.program_step_f0_function_desc,
                newProfile.getF0Value(), newProfile.getzValue(false),
                Helper.getInstance().getTemperatureUnitText(null)));
        programStepDryDescription.setText(getString(R.string.program_step_dry_desc, newProfile.getDryTime()));
        programStepVacuumDescription.setText(getString(R.string.program_step_vacuum_desc, newProfile.getVacuumTimes()));
        programStepFinalTempDescription.setText(getString(R.string.program_step_final_temp_desc,
                newProfile.getFinalTemp(false),
                Helper.getInstance().getTemperatureUnitText(null)));

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
                if (!(name.length() > 0 && !name.contains(",") && !name.contains(";")
                        && !name.contains("\n")
                        && !name.contains("@")
                        && !name.equals(AppConstants.DELETED_PROFILE_NAME))) {
                    Toasty.error(this, getString(R.string.please_enter_a_valid_name), Toast.LENGTH_SHORT, true).show();
                    break;
                }
                newProfile.setName(name);
                newProfile.setLiquidProgram(checkboxIdLiquidProgram.isChecked());
                newProfile.setMaintainEnabled(checkboxMaintainFinalTemp.isChecked());
                newProfile.setF0Enabled(checkboxIsF0FunctionProgram.isChecked());
                newProfile.setContByFlexProbe1(checkboxIsContByFlexProbe1.isChecked());
                newProfile.setContByFlexProbe2(checkboxIsContByFlexProbe2.isChecked());
                Helper.getInstance().setProgram(this, newProfile, new MyCallback() {
                    @Override
                    public void onSuccess(Object response, int requestId) {
                        Toasty.success(getApplicationContext(), getString(R.string.program_saved), Toast.LENGTH_LONG, true).show();
                        AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(),
                                AuditLogger.SCEEN_EMPTY, AuditLogger.ACTION_PROGRAM_EDITED,
                                AuditLogger.OBJECT_EMPTY, getChangedParameters(), true);
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
        oldProfile = newProfile.clone();

        if (linearLayoutFinalTemp != null)
            linearLayoutFinalTemp.setVisibility(AutoclaveModelManager.getInstance().isFinalTempExistsInProgramEdit() ?
                    View.VISIBLE :
                    View.GONE);

//        newProfile.setLocal(true);
        checkboxIsF0FunctionProgram.setOnCheckedChangeListener(null);
        refreshGraphAndList(false);

        //It is here because, else onReseum F0Function dialog will be shown
        checkboxIsF0FunctionProgram.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                linearLayoutF0Function.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                if (!checkboxIsF0FunctionProgram.isChecked())
                    programStepSterilisationDescription.setText(getString(R.string.program_step_sterlisation_desc,
                            newProfile.getSterilisationTime(), newProfile.getSterilisationTemperature(false), Helper.getInstance().getTemperatureUnitText(null)));
                else
                    programStepSterilisationDescription.setText(getString(R.string.program_step_sterlisation_desc_f0,
                            newProfile.getSterilisationTemperature(false),
                            Helper.getInstance().getTemperatureUnitText(null)));
                if (isChecked)
                    showEditF0FunctionnDialog();
            }
        });
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
            case R.id.linearLayoutFinalTemp:
                showEditFinalTemp();
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
        final TextView editTempUnit = dialog.findViewById(R.id.program_definition_editstep_edit_temp_unit);
        final EditText editTime = dialog.findViewById(R.id.program_definition_editstep_edit_time);

        editTime.setEnabled(!checkboxIsF0FunctionProgram.isChecked());
        editTempUnit.setText(Helper.getInstance().getTemperatureUnitText(null));

        //insert default values
        editTime.setText(Integer.toString(newProfile.getSterilisationTime()));
        editTemp.setText(Float.toString(newProfile.getSterilisationTemperature(false)));

        dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
            }
        });

        dialog.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Float, Float> tempRange = manager.getSterilizationTempRange();
                Pair<Integer, Integer> timeRange = manager.getSterilizationTimeRange();

                if (editTemp.getText().toString().isEmpty()) {
                    Toasty.error(getApplicationContext(), getString(R.string.sterilization_temp_range,
                            tempRange.first, tempRange.second, Helper.getInstance().getTemperatureUnitText(null)),
                            Toast.LENGTH_SHORT, true).show();
                    return;
                }

                if (editTime.getText().toString().isEmpty()) {
                    Toasty.error(getApplicationContext(), getString(R.string.sterilization_time_range,
                            timeRange.first, timeRange.second), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                float valueTemp = Math.max(AppConstants.TEMP_MIN_INT, Float.valueOf(editTemp.getText().toString().replace(",", ".")));
                int sterilisationTime = Integer.valueOf(editTime.getText().toString());

                if (valueTemp < tempRange.first || valueTemp > tempRange.second) {
                    Toasty.error(getApplicationContext(), getString(R.string.sterilization_temp_range,
                            tempRange.first, tempRange.second, Helper.getInstance().getTemperatureUnitText(null)), Toast.LENGTH_SHORT, true).show();
                    return;
                }

                if (sterilisationTime < timeRange.first || sterilisationTime > timeRange.second) {
                    Toasty.error(getApplicationContext(), getString(R.string.sterilization_time_range, timeRange.first, timeRange.second), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                newProfile.setSterilisationTime(sterilisationTime);
                newProfile.setSterilisationTemperature(valueTemp, false);
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
        final EditText editLethalTemp = dialog.findViewById(R.id.program_definition_edit_f0_value);
        final EditText editZValue = dialog.findViewById(R.id.program_definition_edit_z_value);
        final TextView editZValueUnit = dialog.findViewById(R.id.program_definition_edit_z_value_unit);
        editZValueUnit.setText(Helper.getInstance().getTemperatureUnitText(null));
        editLethalTemp.setFilters(new InputFilterMinMax[]{new InputFilterMinMax(0, 100)});

        //insert default values
        editZValue.setText(Float.toString(newProfile.getzValue(false)));
        editLethalTemp.setText(Float.toString(newProfile.getF0Value()));

        dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
            }
        });

        dialog.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Pair<Float, Float> range = AutoclaveModelManager.getInstance().getZValueTempRange();
                if (editZValue.getText().toString().isEmpty()
                        || Float.valueOf(editZValue.getText().toString().replace(",", ".")) < range.first
                        || Float.valueOf(editZValue.getText().toString().replace(",", ".")) > range.second) {
                    Toasty.error(getApplicationContext(), getString(R.string.z_value_range, range.first, range.second,
                            Helper.getInstance().getTemperatureUnitText(null)),
                            Toast.LENGTH_SHORT, true).show();
                    return;
                }

                if (editLethalTemp.getText().toString().isEmpty()
                        || Float.valueOf(editLethalTemp.getText().toString().replace(",", ".")) < 0.1
                        || Float.valueOf(editLethalTemp.getText().toString().replace(",", ".")) > 100) {
                    Toasty.error(getApplicationContext(), getString(R.string.f0_value_range, 0.1f, 100f),
                            Toast.LENGTH_SHORT, true).show();
                    return;
                }

                newProfile.setzValue(Float.valueOf(editZValue.getText().toString().replace(",", ".")), false);
                newProfile.setF0Value(Float.valueOf(editLethalTemp.getText().toString().replace(",", ".")));
                newProfile.setF0Enabled(true);
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

                Pair<Integer, Integer> vacuumPulseRange = manager.getVacuumPulseRange();
                int value = 0;
                try {
                    value = Integer.valueOf(editParameter.getText().toString().replace(",", "."));
                } catch (Exception e) {
                    Toasty.error(getApplicationContext(), getString(R.string.vacuum_pulse_range, vacuumPulseRange.first, vacuumPulseRange.second), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                if (isVacuum && (value < vacuumPulseRange.first || value > vacuumPulseRange.second)) {
                    Toasty.error(getApplicationContext(), getString(R.string.vacuum_pulse_range, vacuumPulseRange.first, vacuumPulseRange.second), Toast.LENGTH_SHORT, true).show();
                    return;
                }

                if (!isVacuum && (value < 0 || value > 180)) {
                    Toasty.error(getApplicationContext(), getString(R.string.dry_time_range, 0, 19), Toast.LENGTH_SHORT, true).show();
                    return;
                }

                if (isVacuum) {
                    newProfile.setVacuumTimes(Integer.valueOf(value));
                } else
                    newProfile.setDryTime(Integer.valueOf(value));
                refreshGraphAndList(true);
                dialog.dismissWithAnimation();
            }
        });

        dialog.show();
    }

    private void showEditFinalTemp() {

        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.program_definition_vacuum_dry_dialog, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.program_definition_vacuum_dry_dialog);
        dialog.setTitle(R.string.register_new_user);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        //get views
        final TextView textViewParameterName = dialog.findViewById(R.id.textViewParameterName);
        final TextView textViewParameterType = dialog.findViewById(R.id.textViewParameterType);
        final EditText editParameter = dialog.findViewById(R.id.program_definition_parameter);
        editParameter.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        //insert default values
        editParameter.setText(String.format("%.1f", newProfile.getFinalTemp(false)));

        textViewParameterName.setText(R.string.final_temp);
        textViewParameterType.setText(Helper.getInstance().getTemperatureUnitText(null));

        dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
            }
        });

        dialog.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Float, Float> range = AutoclaveModelManager.getInstance().getFinalTempRange();
                if (editParameter.getText().toString().isEmpty()) {
                    Toasty.error(getApplicationContext(), getString(R.string.final_temp_range, range.first, range.second,
                            Helper.getInstance().getTemperatureUnitText(null)), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                float value = Float.valueOf(editParameter.getText().toString().replace(",", "."));
                if (value < range.first || value > range.second) {
                    Toasty.error(getApplicationContext(), getString(R.string.final_temp_range, range.first, range.second,
                            Helper.getInstance().getTemperatureUnitText(null)), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                newProfile.setFinalTemp(value, false);
                refreshGraphAndList(true);
                dialog.dismissWithAnimation();
            }
        });

        dialog.show();
    }


    private String getChangedParameters() {
        StringBuilder paramters = new StringBuilder(newProfile.getName() + ": [ ");

        paramters.append(getChangingString(R.string.program_name, oldProfile.getName(), newProfile.getName()))
                .append(getChangingString(R.string.is_liquid_program, oldProfile.isLiquidProgram(), newProfile.isLiquidProgram()))
                .append(getChangingString(R.string.is_cont_by_flex_probe_1, oldProfile.isContByFlexProbe1Enabled(), newProfile.isContByFlexProbe1Enabled()))
                .append(getChangingString(R.string.is_cont_by_flex_probe_2, oldProfile.isContByFlexProbe2Enabled(), newProfile.isContByFlexProbe2Enabled()))
                .append(getChangingString(R.string.is_maintain_final_temp, oldProfile.isMaintainEnabled(), newProfile.isMaintainEnabled()))
                .append(getChangingString(R.string.is_f0_function_enabled, oldProfile.isF0Enabled(), newProfile.isF0Enabled()))
                .append(getChangingString(R.string.final_temp, oldProfile.getFinalTemp(false), newProfile.getFinalTemp(false)))
                .append(getChangingString(R.string.f0_value_title, oldProfile.getF0Value(), newProfile.getF0Value()))
                .append(getChangingString(R.string.z_value_title, oldProfile.getzValue(false), newProfile.getzValue(false)))
                .append(getChangingString(R.string.temperature, oldProfile.getSterilisationTemperature(false), newProfile.getSterilisationTemperature(false)))
                .append(getChangingString(R.string.holding_time, oldProfile.getSterilisationTime(), newProfile.getSterilisationTime()))
                .append(getChangingString(R.string.vacuum_pulse, oldProfile.getVacuumTimes(), newProfile.getVacuumTimes()));

        return paramters.append(" ]").toString();
    }

    private String getChangingString(int resId, Object oldValue, Object newValue) {
        return !newValue.equals(oldValue) ?
                (getString(resId) + ": " + oldValue + " \u00BB " + newValue + ", ") : "";

    }


}
