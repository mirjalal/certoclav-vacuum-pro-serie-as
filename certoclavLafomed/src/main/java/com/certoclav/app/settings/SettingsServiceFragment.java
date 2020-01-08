package com.certoclav.app.settings;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;


/**
 * A fragment representing a single Item detail screen. This fragment is
 * contained in a {@link SettingsActivity} in two-pane mode (on tablets)
 */
public class SettingsServiceFragment extends Fragment implements SensorDataListener {


    private View mRootView;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsServiceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.settings_service, container, false);


        mRootView = rootView;


        return rootView;
    }


    @Override
    public void onSensorDataChange(AutoclaveData data) {

        TextView v = (TextView) mRootView.findViewById(R.id.text_temp);
        v.setText(getString(R.string.temp1_chamber) + data.getTemp1().getValueString() + "\n"
                + getString(R.string.temp2_probe) + data.getTemp2().getValueString() + "\n"
                + getString(R.string.temp3_heater) + data.getTemp3().getValueString() + "\n"
                + getString(R.string.temp4_not_used) + data.getTemp4().getValueString() + "\n"
                + getString(R.string.pressure1_chamber) + data.getPress().getValueString() + "\n"
                + getString(R.string.pressure2_steam_gen) + data.getPress2().getValueString() + "\n"
                + getString(R.string.state) + Autoclave.getInstance().getState().toString() + "\n"
                + getString(R.string.program_step) +Autoclave.getInstance().getProgramStep() + "\n"
                + getString(R.string.is_connected) + Autoclave.getInstance().isMicrocontrollerReachable() + "\n"
                + getString(R.string.index_of_running_program) + Autoclave.getInstance().getIndexOfRunningProgram() + "\n"
                + getString(R.string.date_of_text_view) + Autoclave.getInstance().getDate() + "\n"
                + getString(R.string.time) + Autoclave.getInstance().getTime() + "\n"
                + getString(R.string.error_code) + Autoclave.getInstance().getErrorCode() + "\n"
                + getString(R.string.warning_code) + Autoclave.getInstance().getWarningList() + "\n"
                + getString(R.string.cycle_number) + Autoclave.getInstance().getController().getCycleNumber() + "\n");

        v.setTypeface(Typeface.MONOSPACE);
        CheckBox cb = (CheckBox) mRootView.findViewById(R.id.checkBox_program_finished);
        cb.setChecked(data.isProgramFinishedSucessfully());

        cb = (CheckBox) mRootView.findViewById(R.id.checkBox_door_closed);
        cb.setChecked(data.isDoorClosed());

        cb = (CheckBox) mRootView.findViewById(R.id.checkBox_door_locked);
        cb.setChecked(data.isDoorLocked());

        cb = (CheckBox) mRootView.findViewById(R.id.checkBox_program_running);
        cb.setChecked(data.isProgramRunning());

        cb = (CheckBox) mRootView.findViewById(R.id.checkBox_water_full);
        cb.setChecked(data.isWaterLvlFull());

        cb = (CheckBox) mRootView.findViewById(R.id.checkBox_water_low);
        cb.setChecked(data.isWaterLvlLow());

        cb = (CheckBox) mRootView.findViewById(R.id.checkBox_stopped_by_user);
        cb.setChecked(data.isFailStoppedByUser());


    }


    @Override
    public void onResume() {
        Autoclave.getInstance().setOnSensorDataListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        Autoclave.getInstance().removeOnSensorDataListener(this);
        super.onPause();
    }


}

