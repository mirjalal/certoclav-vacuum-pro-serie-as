package com.certoclav.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.certoclav.library.application.ApplicationController;

public class LockoutManager {


    //Name of preference must be same here, without "preferences_lockout_"
    public enum LOCKS {
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

    //Check the if the all settings has been locked CREATE_USER and ADD_PROGRAM is not in settings
    public boolean isLockedAll() {
        for (LOCKS lock : LOCKS.values()) {
            if (!isLocked(lock)
                    && lock != LOCKS.CREATE_USER
                    && lock != LOCKS.ADD_PROGRAM
                    && lock != LOCKS.SERVICE)
                return false;
        }
        return true;
    }

    public boolean isLocked(LOCKS lock) {
        int resIdName = context.getResources().getIdentifier("preferences_lockout_" + lock.toString().toLowerCase(), "string"
                , context.getPackageName());
        int resIdDefault = context.getResources().getIdentifier("preferences_lockout_" + lock.toString().toLowerCase(), "bool"
                , context.getPackageName());

        return prefs.getBoolean(ApplicationController.getContext().getString(resIdName),
                ApplicationController.getContext().getResources().getBoolean(resIdDefault));
    }

}
