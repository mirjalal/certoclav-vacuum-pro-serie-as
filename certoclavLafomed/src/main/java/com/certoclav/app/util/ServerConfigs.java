package com.certoclav.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.library.certocloud.CertocloudConstants;

/**
 * Created by musaq on 9/30/2017.
 */

public class ServerConfigs {

    private static ServerConfigs serverConfigs;
    private Context context;

    private String name;
    private String url;
    private String port;

    private ServerConfigs() {

    }

    public static ServerConfigs getInstance(Context context) {
        if (serverConfigs == null) {
            serverConfigs = new ServerConfigs();
            serverConfigs.context = context;
        }
        return serverConfigs;
    }


    public void saveServerConfig(int serverType, ServerConfigs configs) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        //AppConstants.PREFERENCE_KEY_SERVER_CERTOCLOUD
        //AppConstants.PREFERENCE_KEY_SERVER_MANUAL
        //AppConstants.PREFERENCE_KEY_SERVER_LOCAL
        pref.edit().putInt(AppConstants.PREFERENCE_KEY_SERVER_TYPE, serverType).commit();
        pref.edit().putString(AppConstants.PREFERENCE_KEY_SERVER_NAME, configs.getName()).commit();
        pref.edit().putString(AppConstants.PREFERENCE_KEY_SERVER_IP, configs.getUrl()).commit();
        pref.edit().putString(AppConstants.PREFERENCE_KEY_SERVER_PORT, configs.getPort()).commit();
    }

    public int getServerType() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getInt(AppConstants.PREFERENCE_KEY_SERVER_TYPE, AppConstants.PREFERENCE_KEY_SERVER_CERTOCLOUD);
    }

    public String getServerUrl() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = pref.getString(AppConstants.PREFERENCE_KEY_SERVER_IP, CertocloudConstants.SERVER_URL);
        String port = pref.getString(AppConstants.PREFERENCE_KEY_SERVER_PORT, null);
        return ip + ((port != null && port.length() > 0) ? (":" + port) : "");
    }

    public String getUrl() {
        return url;
    }

    public String getPort() {
        return port;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void updateServerConfigs() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        url = pref.getString(AppConstants.PREFERENCE_KEY_SERVER_IP, CertocloudConstants.SERVER_URL);
        port = pref.getString(AppConstants.PREFERENCE_KEY_SERVER_PORT, null);
        name = pref.getString(AppConstants.PREFERENCE_KEY_SERVER_NAME, context.getString(R.string.certoclav_server));
    }
}
