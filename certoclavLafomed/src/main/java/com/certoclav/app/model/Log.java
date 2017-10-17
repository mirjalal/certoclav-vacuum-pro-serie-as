package com.certoclav.app.model;

import com.certoclav.app.AppConstants;

/**
 * Created by musaq on 8/4/2017.
 */

public class Log {
    public static void e(String tag, String s) {
        if(AppConstants.SHOW_LOGS)
            android.util.Log.e(tag,s);
    }
}
