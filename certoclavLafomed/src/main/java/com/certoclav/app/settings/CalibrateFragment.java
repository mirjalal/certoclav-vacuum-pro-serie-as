package com.certoclav.app.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.listener.CalibrationListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.service.ReadAndParseSerialService;


/**
 * A fragment representing a single Item detail screen. This fragment is
 * contained in a {@link SettingsActivity} in two-pane mode (on tablets)
 */
public class CalibrateFragment extends Fragment implements CalibrationListener {


    private EditText editOffsetSteamSensor = null;
    private EditText editOffsetHeaterSensor = null;
    private EditText editOffsetSteamGeneratorSensor = null;
    private EditText editOffsetMedia = null;
    private EditText editOffsetPress = null;
    private Button buttonApply = null;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CalibrateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.settings_calibration, container, false);
        editOffsetSteamSensor = (EditText) rootView.findViewById(R.id.parameter_temp1_coeff1);
        editOffsetHeaterSensor = (EditText) rootView.findViewById(R.id.parameter_temp2_coeff1);
        editOffsetSteamGeneratorSensor = (EditText) rootView.findViewById(R.id.parameter_temp3_coeff1);
        editOffsetPress = (EditText) rootView.findViewById(R.id.parameter_press_coeff1);
        editOffsetMedia = (EditText) rootView.findViewById(R.id.parameter_media_coeff1);

        editOffsetSteamSensor.setText("");
        editOffsetHeaterSensor.setText("");
        editOffsetSteamGeneratorSensor.setText("");
        editOffsetPress.setText("");
        editOffsetMedia.setText("");

        buttonApply = (Button) rootView.findViewById(R.id.parameter_button_apply);
        buttonApply.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    Double offsetTemp1 = Double.parseDouble(editOffsetSteamSensor.getText().toString());
                    Double offsetTemp2 = Double.parseDouble(editOffsetHeaterSensor.getText().toString());
                    Double offsetTemp3 = Double.parseDouble(editOffsetSteamGeneratorSensor.getText().toString());
                    Double offsetMedia = Double.parseDouble(editOffsetMedia.getText().toString());
                    Integer offsetPress = Integer.parseInt(editOffsetPress.getText().toString());

                    if (offsetTemp1 > -3 && offsetTemp1 < 3) {
                        if (offsetTemp2 > -3 && offsetTemp2 < 3) {
                            if (offsetTemp3 > -3 && offsetTemp3 < 3) {
                                if (offsetPress > -20 && offsetPress < 20) {
                                    if (offsetMedia > -3 && offsetMedia < 3) {
                                        ReadAndParseSerialService.getInstance().sendPutAdjustParameterCommand(offsetTemp1, offsetTemp2, offsetTemp3, offsetPress, offsetMedia);
                                        ReadAndParseSerialService.getInstance().sendGetAdjustParameterCommand();
                                        Toast.makeText(getActivity(), "Parameters saved", Toast.LENGTH_LONG).show();
                                    } else{
                                        Toast.makeText(getActivity(), "Please enter valid offset for media sensor", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "Please enter valid offset for pressure sensor", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Please enter valid offset for steam generator sensor", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Please enter valid offset for heater sensor", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Please enter a valid offset for steam sensor", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Please enter valid data", Toast.LENGTH_LONG).show();
                }

            }
        });

        return rootView;
    }


    @Override
    public void onResume() {

        Autoclave.getInstance().setOnCalibrationListener(this);
        ReadAndParseSerialService.getInstance().sendGetAdjustParameterCommand();

        super.onResume();
    }


    @Override
    public void onPause() {
        Autoclave.getInstance().removeOnCalibrationListener(this);
        super.onPause();
    }

    @Override
    public void onCalibrationParameterReceived() {
        editOffsetSteamSensor.setText(Autoclave.getInstance().getData().getTemp1().getOffset().toString());
        editOffsetHeaterSensor.setText(Autoclave.getInstance().getData().getTemp2().getOffset().toString());
        editOffsetSteamGeneratorSensor.setText(Autoclave.getInstance().getData().getTemp3().getOffset().toString());
        editOffsetPress.setText(Integer.toString(Autoclave.getInstance().getData().getPress().getOffset().intValue()));
        editOffsetMedia.setText(Autoclave.getInstance().getData().getTemp4().getOffset().toString());

    }


}
