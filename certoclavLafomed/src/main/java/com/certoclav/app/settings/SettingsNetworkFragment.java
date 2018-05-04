package com.certoclav.app.settings;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.listener.BroadcastListener;
import com.certoclav.app.listener.WifiListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.ServerConfigs;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.google.gson.Gson;

import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SettingsNetworkFragment extends PreferenceFragment implements WifiListener, OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private SharedPreferences prefs;
    private ServerConfigs serverConfigs;
    private SweetAlertDialog dialogAskForServer;

    public SettingsNetworkFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_network);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        serverConfigs = ServerConfigs.getInstance(getActivity());
    }


    private void updateWifiUI() {


        WifiManager wifiMgr = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //update Text and Summary
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

        String wifiname = wifiInfo.getSSID();

        Log.e("PrefsFragment", "wifiname: " + wifiname);

        CheckBoxPreference customPref = (CheckBoxPreference) findPreference(AppConstants.PREFERENCE_KEY_WIFI_ENABLED);
        customPref.setChecked(wifiMgr.isWifiEnabled());

        if (customPref.isChecked()) {

            if (wifiname == null) {
                customPref.setSummary(R.string.wifi_not_connected);
            } else {
                if (wifiname.equals("<unknown ssid>") || wifiname.equals("0x")) {
                    customPref.setSummary(R.string.trying_to_connect_click_to_choose_network);
                } else {
                    customPref.setSummary(getActivity().getString(R.string.connected_to) + ": " + wifiname);
                }
            }
        } else {
            customPref.setSummary(R.string.wifi_disabled);
        }

    }

    /*
     *
     * Starts an Activity for Picking and Connecting to Wifi
     */
    private void startWifiPicker() {

        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
        intent.putExtra("only_access_points", true);
        intent.putExtra("extra_prefs_show_button_bar", true);
        intent.putExtra("wifi_enable_next_on_connect", true);
        startActivityForResult(intent, 1);


       // startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
    }

    private void startNetworkSettings(){
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.putExtra("only_access_points", true);
        intent.putExtra("extra_prefs_show_button_bar", true);
        intent.putExtra("wifi_enable_next_on_connect", true);
        startActivityForResult(intent, 1);
    }

/*

	private void startBluetoothManager() {
		startActivity(new Intent(getActivity(), BluetoothManagerActivity.class));
	}
*/

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.hasKey()) {
            if (preference.getKey().equals(AppConstants.PREFERENCE_KEY_WIFI_MANAGER)) {
                startWifiPicker();
            }else if (preference.getKey().equals(AppConstants.PREFERENCE_KEY_LAN_MANAGER)){
                configureLan();
            }

        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void configureLan() {
        startNetworkSettings();
        //PackageManager pm = getActivity().getPackageManager();
        //Intent launchIntent = pm.getLaunchIntentForPackage("com.fsl.ethernet");
        //getActivity().startActivity(launchIntent);
    }

    @Override
    public void onResume() {

        //register receiver for bluetooth changes
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//        getActivity().registerReceiver(bluetoothReceiver, filter);


        findPreference(AppConstants.PREFERENCE_KEY_CHOOSE_SERVER).setOnPreferenceClickListener(this);
        updateServerSummary();
        //register Listeners
        prefs.registerOnSharedPreferenceChangeListener(this);
        Autoclave.getInstance().setOnWifiListener(this);

        //set wifi switch position according to wifimanager
        //udateWifiSwitchPosition
        WifiManager wifiMgr = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr != null) {
            //set initial position of Wifi-Switch
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Editor editor = prefs.edit();
            editor.putBoolean(AppConstants.PREFERENCE_KEY_WIFI_ENABLED, wifiMgr.isWifiEnabled());
            editor.commit();
        }

        //update UI (Text)
        updateWifiUI();
//		 updateBluetoothUI(0);
        super.onResume();
    }


    @Override
    public void onPause() {
        //unregister listeners
//		getActivity().unregisterReceiver(bluetoothReceiver);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        Autoclave.getInstance().removeOnWifiListener(this);
        super.onPause();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case AppConstants.PREFERENCE_KEY_CHOOSE_SERVER:
                askForServerSelection();
                break;
        }
        return false;
    }

    private void askForServerSelection() {
        serverConfigs.updateServerConfigs();
        dialogAskForServer = new SweetAlertDialog(getActivity(), R.layout.choose_server_dialog, SweetAlertDialog.WARNING_TYPE);
        dialogAskForServer.setContentView(R.layout.choose_server_dialog);
        ((RadioButton) dialogAskForServer.findViewById(R.id.dialogCertoCloudServer)).setOnClickListener(this);
        ((RadioButton) dialogAskForServer.findViewById(R.id.dialogAddManually)).setOnClickListener(this);
        ((RadioButton) dialogAskForServer.findViewById(R.id.dialogFromLocalNetwork)).setOnClickListener(this);
        ((RadioButton) dialogAskForServer.findViewById(R.id.dialogCertoCloudServer)).setOnCheckedChangeListener(this);
        ((RadioButton) dialogAskForServer.findViewById(R.id.dialogAddManually)).setOnCheckedChangeListener(this);
        ((RadioButton) dialogAskForServer.findViewById(R.id.dialogFromLocalNetwork)).setOnCheckedChangeListener(this);
        ((RadioButton) dialogAskForServer.findViewById(R.id.dialogCertoCloudServer)).setChecked(
                serverConfigs.getServerType() == AppConstants.PREFERENCE_KEY_SERVER_CERTOCLOUD);
        ((RadioButton) dialogAskForServer.findViewById(R.id.dialogAddManually)).setChecked(
                serverConfigs.getServerType() == AppConstants.PREFERENCE_KEY_SERVER_MANUAL);
        ((RadioButton) dialogAskForServer.findViewById(R.id.dialogFromLocalNetwork)).setChecked(
                serverConfigs.getServerType() == AppConstants.PREFERENCE_KEY_SERVER_LOCAL);
        dialogAskForServer.setTitle(R.string.register_new_user);
        dialogAskForServer.setCancelable(true);
        dialogAskForServer.setCanceledOnTouchOutside(true);
        dialogAskForServer.show();
    }

    private void askForServerInfoManually() {
        final SweetAlertDialog dialog = new SweetAlertDialog(getActivity(), R.layout.dialog_server_info, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.dialog_server_info);
        final EditText editTextHost = (EditText) dialog.findViewById(R.id.editTextHost);
        final EditText editTextPort = (EditText) dialog.findViewById(R.id.editTextPort);
        if (serverConfigs.getServerType() == AppConstants.PREFERENCE_KEY_SERVER_MANUAL) {
            editTextHost.setText(serverConfigs.getUrl());
            editTextPort.setText(serverConfigs.getPort());
        }
        dialog.findViewById(R.id.dialogButtonNO).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
            }
        });
        dialog.findViewById(R.id.dialogButtonOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
                serverConfigs.setName(getString(R.string.server_manual));
                serverConfigs.setUrl(editTextHost.getText().toString());
                serverConfigs.setPort(editTextPort.getText().toString());
                serverConfigs.saveServerConfig(AppConstants.PREFERENCE_KEY_SERVER_MANUAL, serverConfigs);
                updateServerSummary();
            }
        });

        dialog.show();
    }

    int MAX_TRY_COUNT = 5;

    private void searchForServer() {
        MAX_TRY_COUNT = 5;
        final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getString(R.string.searching))
                .setConfirmText(getString(R.string.yes))
                .setCancelText(getString(R.string.cancel));
        sweetAlertDialog.show();

        final Runnable threadRunnable = new Runnable() {
            @Override
            public void run() {
                final Runnable runnable = this;
                BroadcastListener broadcastListener = new BroadcastListener() {
                    BroadcastListener broadcastListener2 = this;

                    @Override
                    public void onReceived(final JSONObject data) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Gson gson = new Gson();
                                final ServerConfigs configs = gson.fromJson(data.toString(), ServerConfigs.class);
                                sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                sweetAlertDialog.setCancelText(null);
                                sweetAlertDialog.setTitleText(getString(R.string.server_found_title));
                                sweetAlertDialog.setContentText(getString(R.string.server_found_content, configs.getName()));
                                sweetAlertDialog.setConfirmText(getString(R.string.save));
                                sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                        serverConfigs.saveServerConfig(AppConstants.PREFERENCE_KEY_SERVER_LOCAL, configs);
                                        updateServerSummary();
                                    }
                                });

                            }
                        });
                    }

                    @Override
                    public void onTimeout() {
                        if (MAX_TRY_COUNT-- > 0) {
                            new Thread(runnable).start();
                            return;
                        }
                        onFailed();
                    }

                    @Override
                    public void onFailed() {

                        final SweetAlertDialog.OnSweetClickListener listener = new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(final SweetAlertDialog sweetAlertDialog) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        sweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                        sweetAlertDialog.setTitleText(getString(R.string.searching));
                                        sweetAlertDialog.showContentText(false);
                                    }
                                });
                                new Thread(runnable).start();
                            }
                        };
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                sweetAlertDialog.setTitleText(getString(R.string.server_not_found_title));
                                sweetAlertDialog.setContentText(getString(R.string.server_not_found_content));
                                sweetAlertDialog.setConfirmText(getString(R.string.try_again));
                                MAX_TRY_COUNT=5;
                            }
                        });
                        sweetAlertDialog.setConfirmClickListener(listener);


                    }
                };
                Helper.sendBroadcast(getActivity(), "Mene gonder gorum", broadcastListener);
            }
        };

        new Thread(threadRunnable).start();
    }

    @Override
    public void onClick(View v) {
        if (dialogAskForServer != null)
            dialogAskForServer.dismissWithAnimation();
        int serverType;
        switch (v.getId()) {
            case R.id.dialogCertoCloudServer:
                serverType = AppConstants.PREFERENCE_KEY_SERVER_CERTOCLOUD;
                serverConfigs.setName(getString(R.string.certoclav_server));
                serverConfigs.setUrl(CertocloudConstants.SERVER_URL);
                serverConfigs.setPort("");
                serverConfigs.saveServerConfig(serverType, serverConfigs);
                updateServerSummary();
                break;
            case R.id.dialogAddManually:
                askForServerInfoManually();
                break;
            case R.id.dialogFromLocalNetwork:
                searchForServer();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        v.setText(getString(isChecked ? R.string.selected : R.string.select));
    }



/*	private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
	    public void onReceive(Context context, Intent intent) {
	        final String action = intent.getAction();

	        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
	            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
            updateBluetoothUI(state);
	        }
	    }
	};*/


    public static class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();

            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI)
                Autoclave.getInstance().setWifiConnected(true);
            else
                Autoclave.getInstance().setWifiConnected(false);
        }
    }


    @Override
    public void onWifiConnectionChange(Boolean wifiConnected) {
        updateWifiUI();

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(AppConstants.PREFERENCE_KEY_WIFI_ENABLED)) {
            WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager != null) {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                CheckBoxPreference customPref = (CheckBoxPreference) findPreference(AppConstants.PREFERENCE_KEY_WIFI_ENABLED);
                wifiManager.setWifiEnabled(sharedPrefs.getBoolean(AppConstants.PREFERENCE_KEY_WIFI_ENABLED, false));
                customPref.setSummary(R.string.trying_to_connect_click_to_choose_network);
            }
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void updateServerSummary() {
        AutoclaveMonitor.getInstance().stopSocketService();
        findPreference(AppConstants.PREFERENCE_KEY_CHOOSE_SERVER).setSummary(
                getPreferenceManager().getSharedPreferences().getString(AppConstants.PREFERENCE_KEY_SERVER_NAME,
                        getString(R.string.certocloud_server)));
    }

}