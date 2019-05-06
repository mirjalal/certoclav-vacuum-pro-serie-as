package com.certoclav.app.settings;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.listener.CalibrationListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveParameter;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.MyCallback;
import com.certoclav.library.application.ApplicationController;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;


/**
 * A fragment representing a single Item detail screen. This fragment is
 * contained in a {@link SettingsActivity} in two-pane mode (on tablets)
 */
public class CalibrateFragment extends Fragment implements CalibrationListener, MyCallback {


    private EditText editOffsetSteamSensor = null;
    private EditText editOffsetMedia = null;
    private EditText editOffsetMedia2 = null;
    private EditText editOffsetPress = null;
    private EditText editOffsetPress2;

    private TextView textViewSteamTempText;
    private TextView textViewMedia1TempText;
    private TextView textViewMedia2TempText;
    private Button buttonApply = null;
    private int currentOffsetReadParameter = AppConstants.PARAM_OFFSET_STEAM;
    private int failCount = 0;
    private final int MAX_FAIL_COUNT = 5;
    private Double offsetTemp1;
    private Double offsetMedia;
    private Double offsetMedia2;
    private double offsetPress;
    private double offsetPress2;
    private boolean isLocked;
    private SweetAlertDialog barProgressDialog;

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
        editOffsetSteamSensor = rootView.findViewById(R.id.parameter_temp1_coeff1);
        editOffsetPress = rootView.findViewById(R.id.parameter_press_coeff1);
        editOffsetPress2 = rootView.findViewById(R.id.parameter_press_2_coeff1);
        editOffsetMedia = rootView.findViewById(R.id.parameter_media_coeff1);
        editOffsetMedia2 = rootView.findViewById(R.id.parameter_media_2_coeff1);

        textViewSteamTempText = rootView.findViewById(R.id.parameter_temp1_text);
        textViewMedia1TempText = rootView.findViewById(R.id.parameter_media_text);
        textViewMedia2TempText = rootView.findViewById(R.id.parameter_media_2_text);

        editOffsetSteamSensor.setText("");
        editOffsetPress.setText("");
        editOffsetPress2.setText("");
        editOffsetMedia.setText("");
        editOffsetMedia2.setText("");

        buttonApply = rootView.findViewById(R.id.parameter_button_apply);
        buttonApply.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    offsetTemp1 = 0.0;
                    if (editOffsetSteamSensor.getText().length() > 0)
                        offsetTemp1 = Double.parseDouble(editOffsetSteamSensor.getText().toString());

                    offsetMedia = 0.0;
                    if (editOffsetMedia.getText().length() > 0)
                        offsetMedia = Double.parseDouble(editOffsetMedia.getText().toString());

                    offsetMedia2 = 0.0;
                    if (editOffsetMedia2.getText().length() > 0)
                        offsetMedia2 = Double.parseDouble(editOffsetMedia2.getText().toString());

                    offsetPress = 0.0;
                    if (editOffsetPress.getText().length() > 0)
                        offsetPress = Double.parseDouble(editOffsetPress.getText().toString());

                    offsetPress2 = 0.0;
                    if (editOffsetPress.getText().length() > 0)
                        offsetPress2 = Double.parseDouble(editOffsetPress2.getText().toString());

                    Pair<Float, Float> range = AutoclaveModelManager.getInstance().getOffsetTempRange();
                    if (offsetTemp1 < range.first || offsetTemp1 > range.second) {
                        Toasty.warning(getActivity(), getString(R.string.calibration_not_valid_steam_sensor, range.first, range.second), Toast.LENGTH_SHORT, true).show();
                        return;
                    }

                    if (offsetPress < -1 || offsetPress > 1) {
                        Toasty.warning(getActivity(), getString(R.string.calibration_not_valid_pressure_sensor_1, -1f, 1f), Toast.LENGTH_SHORT, true).show();
                        return;
                    }

                    if (offsetPress2 < -1 || offsetPress2 > 1) {
                        Toasty.warning(getActivity(), getString(R.string.calibration_not_valid_pressure_sensor_2, -1f, 1f), Toast.LENGTH_SHORT, true).show();
                        return;
                    }
                    if (offsetMedia < range.first || offsetMedia > range.second) {
                        Toasty.warning(getActivity(), getString(R.string.calibration_not_valid_media_sensor, range.first, range.second), Toast.LENGTH_SHORT, true).show();
                        return;
                    }

                    if (offsetMedia2 < range.first || offsetMedia2 > range.second) {
                        Toasty.warning(getActivity(), getString(R.string.calibration_not_valid_media_2_sensor, range.first, range.second), Toast.LENGTH_SHORT, true).show();
                        return;
                    }

                    barProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
                    barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                    barProgressDialog.setTitleText(getString(R.string.saving));
                    barProgressDialog.setContentText(null);
                    barProgressDialog.showCancelButton(false);
                    barProgressDialog.setCancelable(false);
                    barProgressDialog.setCanceledOnTouchOutside(false);
                    barProgressDialog.show();
                    ReadAndParseSerialService.getInstance().setParameter(currentOffsetReadParameter = AppConstants.PARAM_OFFSET_STEAM,
                            Helper.currentUnitToCelsius(offsetTemp1.floatValue()));

                } catch (Exception e) {
                    e.printStackTrace();
                    Toasty.warning(getActivity(), getString(R.string.calibration_not_valid_data), Toast.LENGTH_SHORT, true).show();
                }

            }
        });

        return rootView;
    }


    @Override
    public void onResume() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                prefs.getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_language),
                        ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_language))) {
            Toast.makeText(getActivity(), R.string.these_settings_are_locked_by_the_admin, Toast.LENGTH_SHORT).show();
            isLocked = true;
        } else {
            isLocked = false;
        }

        textViewSteamTempText.setText(getString(R.string.offset_of_steam_sensor_c_, Helper.getTemperatureUnitText(null)));
        textViewMedia1TempText.setText(getString(R.string.offset_of_media_sensor_c_, Helper.getTemperatureUnitText(null)));
        textViewMedia2TempText.setText(getString(R.string.offset_of_media_2_sensor_c_, Helper.getTemperatureUnitText(null)));


        editOffsetSteamSensor.setEnabled(false);
        editOffsetPress.setEnabled(false);
        editOffsetPress2.setEnabled(false);
        editOffsetMedia.setEnabled(false);
        editOffsetMedia2.setEnabled(false);
        buttonApply.setEnabled(false);

        ReadAndParseSerialService.getInstance().addCallback(this);
        ReadAndParseSerialService.getInstance().getParameter(AppConstants.PARAM_OFFSET_STEAM);

        barProgressDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        barProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        barProgressDialog.setTitleText(getString(R.string.loading));
        barProgressDialog.setContentText(null);
        barProgressDialog.showCancelButton(false);
        barProgressDialog.setCancelable(false);
        barProgressDialog.setCanceledOnTouchOutside(false);
        barProgressDialog.show();

        super.onResume();
    }


    @Override
    public void onPause() {
        Autoclave.getInstance().removeOnCalibrationListener(this);
        ReadAndParseSerialService.getInstance().removeCallback(this);
        if (barProgressDialog != null)
            barProgressDialog.dismiss();
        super.onPause();
    }

    @Override
    public void onCalibrationParameterReceived() {

        if (!isLocked) {
            editOffsetSteamSensor.setEnabled(true);
            editOffsetPress.setEnabled(true);
            editOffsetPress2.setEnabled(true);
            editOffsetMedia.setEnabled(true);
            editOffsetMedia2.setEnabled(true);
            buttonApply.setEnabled(true);
        }
        editOffsetSteamSensor.setText(Autoclave.getInstance().getData().getTemp1().getOffset().toString());
        editOffsetPress.setText(Autoclave.getInstance().getData().getPress().getOffset().toString());
        editOffsetPress2.setText(Autoclave.getInstance().getData().getPress2().getOffset().toString());
        editOffsetMedia.setText(Autoclave.getInstance().getData().getTemp2().getOffset().toString());
        editOffsetMedia2.setText(Autoclave.getInstance().getData().getTemp3().getOffset().toString());
        barProgressDialog.dismissWithAnimation();

    }


    @Override
    public void onSuccess(Object response, int requestId) {
        failCount = 0;
        if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_GET_PARAMETER) {
            AutoclaveParameter parameter = (AutoclaveParameter) response;
            switch (parameter.getParameterId()) {
                case AppConstants.PARAM_OFFSET_STEAM:
                    Autoclave.getInstance().getData().getTemp1().setOffset(Double.valueOf(parameter.getValue().toString()));
                    currentOffsetReadParameter = AppConstants.PARAM_OFFSET_MEDIA;
                    break;
                case AppConstants.PARAM_OFFSET_MEDIA:
                    Autoclave.getInstance().getData().getTemp2().setOffset(Double.valueOf(parameter.getValue().toString()));
                    currentOffsetReadParameter = AppConstants.PARAM_OFFSET_MEDIA_2;
                    break;
                case AppConstants.PARAM_OFFSET_MEDIA_2:
                    Autoclave.getInstance().getData().getTemp3().setOffset(Double.valueOf(parameter.getValue().toString()));
                    currentOffsetReadParameter = AppConstants.PARAM_OFFSET_PRESSURE_1;
                    break;
                case AppConstants.PARAM_OFFSET_PRESSURE_1:
                    Autoclave.getInstance().getData().getPress().setOffset(Double.valueOf(parameter.getValue().toString()));
                    currentOffsetReadParameter = AppConstants.PARAM_OFFSET_PRESSURE_2;
                    break;
                case AppConstants.PARAM_OFFSET_PRESSURE_2:
                    Autoclave.getInstance().getData().getPress2().setOffset(Double.valueOf(parameter.getValue().toString()));
                    currentOffsetReadParameter = -1;
                    break;
            }
            if (currentOffsetReadParameter != -1)
                ReadAndParseSerialService.getInstance().getParameter(currentOffsetReadParameter);
            else
                onCalibrationParameterReceived();
        } else if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_SET_PARAMETER) {

            switch (currentOffsetReadParameter) {
                case AppConstants.PARAM_OFFSET_STEAM:
                    currentOffsetReadParameter = AppConstants.PARAM_OFFSET_MEDIA;
                    ReadAndParseSerialService.getInstance().setParameter(AppConstants.PARAM_OFFSET_MEDIA,
                            Helper.currentUnitToCelsius(offsetMedia.floatValue()));
                    break;
                case AppConstants.PARAM_OFFSET_MEDIA:
                    currentOffsetReadParameter = AppConstants.PARAM_OFFSET_MEDIA_2;
                    ReadAndParseSerialService.getInstance().setParameter(AppConstants.PARAM_OFFSET_MEDIA_2,
                            Helper.currentUnitToCelsius(offsetMedia2.floatValue()));
                    break;
                case AppConstants.PARAM_OFFSET_MEDIA_2:
                    currentOffsetReadParameter = AppConstants.PARAM_OFFSET_PRESSURE_1;
                    ReadAndParseSerialService.getInstance().setParameter(AppConstants.PARAM_OFFSET_PRESSURE_1, offsetPress);
                    break;
                case AppConstants.PARAM_OFFSET_PRESSURE_1:
                    ReadAndParseSerialService.getInstance().setParameter(AppConstants.PARAM_OFFSET_PRESSURE_2, offsetPress2);
                    currentOffsetReadParameter = AppConstants.PARAM_OFFSET_PRESSURE_2;
                    break;
                case AppConstants.PARAM_OFFSET_PRESSURE_2:
                    ReadAndParseSerialService.getInstance().getParameter(AppConstants.PARAM_OFFSET_STEAM);
                    Toasty.success(getActivity(), getString(R.string.calibration_parameters_saved), Toast.LENGTH_SHORT, true).show();
                    barProgressDialog.dismissWithAnimation();
                    break;
            }
        }
    }

    @Override
    public void onError(ErrorModel error, int requestId) {
        if (requestId == ReadAndParseSerialService.HANDLER_MSG_ACK_GET_PARAMETER) {
            if (failCount++ < MAX_FAIL_COUNT)
                ReadAndParseSerialService.getInstance().getParameter(currentOffsetReadParameter);
            else {
                barProgressDialog.dismissWithAnimation();
                Toasty.warning(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT, true).show();
            }
        } else {
            barProgressDialog.dismissWithAnimation();
            Toasty.warning(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT, true).show();
        }
    }

    @Override
    public void onStart(int requestId) {

    }

    @Override
    public void onProgress(int current, int max) {

    }
}
