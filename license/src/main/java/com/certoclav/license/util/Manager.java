package com.certoclav.license.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.FileReader;

public class Manager {

    private static Manager manager;
    private Context context;


    private String imei;
    private String androidID;
    private String bluetoothMAC;
    private String wifiMAC;
    private String ethMAC;

    @SuppressLint("MissingPermission")
    private Manager(Context context) {
        this.context = context;

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            imei = telephonyManager.getImei();
        } else {
            imei = telephonyManager.getDeviceId();
        }

        androidID = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        BluetoothManager ba=(BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothMAC=ba.getAdapter().getAddress();

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        wifiMAC = wInfo.getMacAddress();

        ethMAC = getEthernetMacAddress();
    }

    private Manager(){    }

    public static Manager getInstance(Context context) {
        if(manager==null)
            manager = new Manager(context);
        return manager;
    }


    private String getEthernetMacAddress(){
        try {

            StringBuffer fileData = new StringBuffer(1000);
            BufferedReader reader = new BufferedReader(new FileReader("/sys/class/net/eth0/address"));
            char[] buf = new char[1024];
            int numRead;
            while((numRead=reader.read(buf)) != -1){
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
            reader.close();
            return fileData.toString().toUpperCase().substring(0, 17);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
