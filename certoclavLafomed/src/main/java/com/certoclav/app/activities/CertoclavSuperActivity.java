package com.certoclav.app.activities;


import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;

public class CertoclavSuperActivity extends FragmentActivity implements SensorDataListener {
    private TextView textSteam = null;
    private TextView textMedia = null;
    private TextView textPressure = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.super_activity);
        textSteam = (TextView) findViewById(R.id.certoclav_statusbar_text_steam);
        textMedia = (TextView) findViewById(R.id.certoclav_statusbar_text_media);
        textPressure = (TextView) findViewById(R.id.certoclav_statusbar_text_pressure);
        if(!AppConstants.SHOW_LOGS)
            ((ViewGroup)findViewById(R.id.fragment_debugger_uart).getParent()).removeView(findViewById(R.id.fragment_debugger_uart));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        ((AudioManager) this.getSystemService(Context.AUDIO_SERVICE)).setRingerMode(AudioManager.RINGER_MODE_SILENT);

    }

    protected void onResume() {
        Autoclave.getInstance().setOnSensorDataListener(this);
        super.onResume();
    }

    protected void onPause() {
        Autoclave.getInstance().removeOnSensorDataListener(this);
        super.onPause();
    }

    @Override
    public void onSensorDataChange(AutoclaveData data) {
        try {
            if (data.getTemp2().getCurrentValue() < 200) {
                textMedia.setVisibility(View.VISIBLE);
            } else {
                textMedia.setVisibility(View.GONE);
            }

            textPressure.setText(getString(R.string.pressure) + ": " + data.getPress().getValueString() + " " + getString(R.string.bar));
            textSteam.setText(getString(R.string.steam) + ": " + data.getTemp1().getValueString() + " " + getString(R.string._c));
            textMedia.setText(getString(R.string.media) + ": " + data.getTemp2().getValueString() + " " + getString(R.string._c));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        ((ViewGroup) findViewById(R.id.main_content)).addView(View.inflate(this, layoutResID, null));
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

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w
                    .getBottom())) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
                        .getWindowToken(), 0);
            }
        }
        return ret;
    }
}