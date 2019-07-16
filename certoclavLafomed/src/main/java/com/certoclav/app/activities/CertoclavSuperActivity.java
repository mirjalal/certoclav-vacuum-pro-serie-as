package com.certoclav.app.activities;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.certoclav.app.R;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.Helper;

public class CertoclavSuperActivity extends FragmentActivity implements SensorDataListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private TextView textSteam = null;
    private TextView textMedia = null;
    private TextView textMedia2 = null;
    private TextView textPressure = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.super_activity);
        textSteam = findViewById(R.id.certoclav_statusbar_text_steam);
        textMedia = findViewById(R.id.certoclav_statusbar_text_media);
        textMedia2 = findViewById(R.id.certoclav_statusbar_text_media_2);
        textPressure = findViewById(R.id.certoclav_statusbar_text_pressure);
//        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preferences_device_show_logs", true))
//            ((ViewGroup) findViewById(R.id.fragment_debugger_uart).getParent()).removeView(findViewById(R.id.fragment_debugger_uart));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

    }

    protected void onResume() {
        Autoclave.getInstance().setOnSensorDataListener(this);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    protected void onPause() {
        Autoclave.getInstance().removeOnSensorDataListener(this);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
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


            textMedia.setVisibility(data.getTemp2().getCurrentValue() < -100 ? View.GONE : View.VISIBLE);
            textMedia2.setVisibility(data.getTemp3().getCurrentValue() < -100 ? View.GONE : View.VISIBLE);
            textMedia2.setVisibility((AutoclaveModelManager.getInstance().hasTwoFlexProbe2()
                    && data.getTemp3().getCurrentValue() > -100) ? View.VISIBLE : View.GONE);

            textPressure.setText(getString(R.string.pressure) + ": " + data.getPress().getValueString() + " " + getString(R.string.bar));
            textSteam.setText(getString(R.string.steam) + ": " + data.getTemp1().getValueString() + " " + Helper.getInstance().getTemperatureUnitText(null));
            textMedia.setText(getString(R.string.media) + ": " + data.getTemp2().getValueString() + " " + Helper.getInstance().getTemperatureUnitText(null));
            textMedia2.setText(getString(R.string.media_2) + ": " + data.getTemp3().getValueString() + " " + Helper.getInstance().getTemperatureUnitText(null));
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (AutoclaveModelManager.getInstance().getParametersSkipForAuditLog().contains(key))
            return;

        if (sharedPreferences.getAll().get(key) instanceof Boolean) {
            AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_PREF_CHANGED,
                    key.hashCode(), sharedPreferences.getBoolean(key, false) + "", true);
        } else if (sharedPreferences.getAll().get(key) instanceof String) {
            AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_PREF_CHANGED,
                    key.hashCode(), sharedPreferences.getString(key, "") + "", true);
        } else if (sharedPreferences.getAll().get(key) instanceof Integer) {
            AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_PREF_CHANGED,
                    key.hashCode(), sharedPreferences.getInt(key, -1) + "",true);
        } else if (sharedPreferences.getAll().get(key) instanceof Float) {
            AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(), AuditLogger.SCEEN_SETTINGS, AuditLogger.ACTION_PREF_CHANGED,
                    key.hashCode(), sharedPreferences.getFloat(key, 0f) + "",true);
        }
    }
}