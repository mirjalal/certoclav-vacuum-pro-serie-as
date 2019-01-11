package com.certoclav.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.certoclav.library.application.ApplicationController;

public class LockoutManager {


    //Name of preference must be same here, without "preferences_lockout_"
    public enum LOCKS{
        CREATE_USER,
        NETWORK,
        AUDIT_LOGS,
        USER_ACCOUNT,
        ADD_PROGRAM,
        AUTOCLAVE,
        DEVICE,
        LANGUAGE,
        NOTIFICATIONS,
        STERILIZATION,
        CALIBRATION,
        GLP,
        SERVICE
    }

    private static LockoutManager lockoutManager;
    private SharedPreferences prefs;
    private Context context;

    private LockoutManager() {
        context = AppController.getContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(AppController.getContext());
    }

    public static LockoutManager getInstance() {
        if (lockoutManager == null)
            lockoutManager = new LockoutManager();
        return lockoutManager;
    }

    public boolean isLocked(LOCKS lock){
        int resIdName = context.getResources().getIdentifier("preferences_lockout_"+lock.toString().toLowerCase(),"string"
                , context.getPackageName());
        int resIdDefault = context.getResources().getIdentifier("preferences_lockout_"+lock.toString().toLowerCase(),"bool"
                , context.getPackageName());

        return prefs.getBoolean(ApplicationController.getContext().getString(resIdName),
                ApplicationController.getContext().getResources().getBoolean(resIdDefault));
    }

}
