package com.certoclav.app.monitor;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;
import com.certoclav.app.util.Helper;
import com.certoclav.library.application.ApplicationController;

public class MonitorAutoclaveFragment extends Fragment implements SensorDataListener {

    private TextView textTemp;
    private TextView textPress;
    private TextView textGenPress;
    private TextView textMedia;
    private TextView textMedia2;
    private TextView textHTemp;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.monitor_autoclave_fragment, container, false); //je nach mIten k�nnte man hier anderen Inhalt laden.

        textTemp = rootView.findViewById(R.id.monitor_text_temp);
        textPress = rootView.findViewById(R.id.monitor_text_press);
        textGenPress = rootView.findViewById(R.id.monitor_text_generator_press);
        textMedia = rootView.findViewById(R.id.monitor_text_media);
        textMedia2 = rootView.findViewById(R.id.monitor_text_media_2);
        textHTemp = rootView.findViewById(R.id.monitor_text_h_temp);
        Autoclave.getInstance().setOnSensorDataListener(this);


        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSensorDataChange(AutoclaveData data) {
        try {
            textTemp.setText(ApplicationController.getContext().getString(R.string.steam) + " " + data.getTemp1().getValueString() + " \u2103");
            textPress.setText(ApplicationController.getContext().getString(R.string.press) + " " + data.getPress().getValueString() + " bar");

            textGenPress.setText(ApplicationController.getContext().getString(R.string.gen_press) + " " + data.getPress2().getValueString() + " bar");


            textMedia.setText(ApplicationController.getContext().getString(R.string.media) + " "
                    + data.getTemp2().getValueString() + Helper.getInstance().getTemperatureUnitText(null));
            textMedia2.setText(ApplicationController.getContext().getString(R.string.media_2) + " "
                    + data.getTemp3().getValueString() + Helper.getInstance().getTemperatureUnitText(null));
            textHTemp.setText(ApplicationController.getContext().getString(R.string.media_h_temp) + " "
                    + data.getTemp4().getValueString() + Helper.getInstance().getTemperatureUnitText(null));


            textMedia.setVisibility(data.getTemp2().getCurrentValue() > -100 ? View.VISIBLE : View.GONE);
            textMedia2.setVisibility(data.getTemp3().getCurrentValue() > -100 ? View.VISIBLE : View.GONE);
            textHTemp.setVisibility(data.getTemp4().getCurrentValue() > -100 ? View.VISIBLE : View.GONE);
            textGenPress.setVisibility(data.getPress2().getCurrentValue() > -100 ? View.VISIBLE : View.GONE);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

