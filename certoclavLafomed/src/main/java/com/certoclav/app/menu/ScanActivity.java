package com.certoclav.app.menu;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.adapters.ScanAdapter;
import com.certoclav.app.adapters.ScanAdapter.OnDeleteListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.monitor.MonitorActivity;
import com.certoclav.app.sterilisationassistant.AssistantActivity;
import com.certoclav.library.application.ApplicationController;


public class ScanActivity extends Activity {

    private GridView programGrid;
    private EditText editTextId;
    private ScanAdapter scanAdapter;
    private Button buttonNext;
    private CertoclavNavigationbarClean navigationbar;


    @Override
    protected void onResume() {
        Autoclave.getInstance().getListContent().clear(); //dirty
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        super.onResume();
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_scan_activity);
        navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.showButtonBack();
        navigationbar.setHeadText(getString(R.string.scan_item));
        buttonNext = (Button) findViewById(R.id.scan_button_next);
        buttonNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Autoclave.getInstance().getListContent().clear();
                for (int i = 0; i < scanAdapter.getCount(); i++) {
                    Autoclave.getInstance().getListContent().add(scanAdapter.getItem(i));
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext());
                Boolean defaultvalue = getResources().getBoolean(R.bool.switch_step_by_step_default);


                if (prefs.getBoolean(AppConstants.PREFERENCE_KEY_STEP_BY_STEP, defaultvalue)) {
                    Intent intent = new Intent(ApplicationController.getContext(), AssistantActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ApplicationController.getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(ApplicationController.getContext(), MonitorActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ApplicationController.getContext().startActivity(intent);
                }

                finish();

            }
        });
        Log.e("ScanActivity", "Button next done");
        programGrid = (GridView) findViewById(R.id.scan_gridlayout);
        editTextId = (EditText) findViewById(R.id.scan_edittext);
        editTextId.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    editTextId.requestFocus();
                    editTextId.setText("");

                }
            }
        });
        editTextId.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.e("ScanActivity", "EVENT: " + v.getText().toString());
                if (editTextId.getText().toString().length() > 0) {
                    scanAdapter.add(editTextId.getText().toString());
                    programGrid.smoothScrollToPosition(scanAdapter.getCount()-1);
                }
                editTextId.requestFocus();
                editTextId.setText("");
                navigationbar.setHeadText("SCAN ITEMS (" + scanAdapter.getCount() + ")");

                return true;
            }
        });
        scanAdapter = new ScanAdapter(this, new ArrayList<String>());
        scanAdapter.setOnDeleteListener(new OnDeleteListener() {

            @Override
            public void onDelete() {
                navigationbar.setHeadText("SCAN ITEMS (" + scanAdapter.getCount() + ")");
            }
        });
        programGrid.setAdapter(scanAdapter);


    }


}


