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
        v.setText("Temperature 1 (Chamber):  " + data.getTemp1().getValueString() + "\n"
                + "Temperature 2 (Probe):    " + data.getTemp2().getValueString() + "\n"
                + "Temperature 3 (Heater):   " + data.getTemp3().getValueString() + "\n"
                + "Temperature 4 (Not Used): " + data.getTemp4().getValueString() + "\n"
                + "Pressure 1 (Chamber):     " + data.getPress().getValueString() + "\n"
                + "Pressure 2 (Steam Gen.):  " + data.getPress2().getValueString() + "\n"
                + "State:                    " + Autoclave.getInstance().getState().toString() + "\n"
                + "Program Step:             " +Autoclave.getInstance().getProgramStep() + "\n"
                + "Is Connected:             " + Autoclave.getInstance().isMicrocontrollerReachable() + "\n"
                + "Index of running program: " + Autoclave.getInstance().getIndexOfRunningProgram() + "\n"
                + "Date:                     " + Autoclave.getInstance().getDate() + "\n"
                + "Time:                     " + Autoclave.getInstance().getTime() + "\n"
                + "ErrorCode:                " + Autoclave.getInstance().getErrorCode() + "\n"
                + "CycleNumber:              " + Autoclave.getInstance().getController().getCycleNumber() + "\n");

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

