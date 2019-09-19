package com.certoclav.library.certocloud;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.certoclav.library.application.ApplicationController;

public class CertocloudConstants {

    /**
     * CertoCloud REST API
     * Routes that can be accessed only by autheticated users
     */

    /**
     * Routes that can be accessed only by admin users
     */

    public static final String PREFERENCE_KEY_SERVER_IP = "serverurl";
    public static final String PREFERENCE_KEY_SERVER_NAME = "servername";
    public static final String PREFERENCE_KEY_SERVER_PORT = "serverport";

    /**
     * CertoCloud REST API
     * Routes that can be accessed by everyone
     */
    public final static String SERVER_URL = "https://raypacloudapi.herokuapp.com";//www.ng-certocloud.rhcloud.com";
    public final static String SERVER_CERTOCLOUD_URL = "https://api.certocloud.com";//www.ng-certocloud.rhcloud.com";
    public final static String REST_API_POST_LOGIN = "/login";// auth.login);
    public final static String REST_API_POST_SIGNUP = "/signup";// auth.signup);
    public final static String REST_API_POST_SIGNUP_EXIST = "/signup/exist";// auth.userExist);
    public final static String REST_API_POST_EDIT_USER = "/edituser";// auth.signup);
    public final static String REST_API_POST_SIGNUP_ACTIVATE = "/signup/activate";// auth.userExist);
    public final static String REST_API_POST_SIGNUP_RESEND_KEY = "/signup/resend";//
    public final static String REST_API_GET_USER = "/getuser";// auth.login);
    public final static String REST_API_GET_LICENSE_COUNT = "/license/count";// auth.login);
    public final static String REST_API_ACTICATE_AUTOCLAVE = "/license/activate";// auth.login);


    /**
     * CertoCloud REST API
     * Routes that can be accessed only by autheticated users
     */
    public final static String REST_API_POST_PROTOCOLS = "/api/protocols/";//, devices.getAll);
    public final static String REST_API_POST_PROTOCOL_LIVE = "/api/livedebug/";//, devices.getAll);
    public final static String REST_API_GET_DEVICES = "/api/devices/";//, devices.getAll);
    public final static String REST_API_GET_CONDITIONS = "/api/conditions/"; //+safetykey
    public final static String REST_API_GET_IEMS = "/api/balanceitem/"; //+safetykey
    public final static String REST_API_POST_CONDITIONS_CREATE = "/api/conditions/"; //+safetykey //warning: only create condition if there is no
    public final static String REST_API_POST_CONDITIONS_UPDATE = "/api/conditions/update/";
    public final static String REST_API_POST_DEVICE = "/api/devices/";// devices.create);
    public final static String REST_API_PUT_DEVICE_RENAME = "/api/devices/";// devices.rename);
    public final static String REST_API_DELETE_DEVICE = "/api/devices/";// devices.delete);
    public final static String REST_API_GET_PROFILES = "/api/programs/";// programs.getAll);
    public final static String REST_API_GET_DEVICE_PROFILES = "/api/programs/all/";// programs.getAll);
    public final static String REST_API_POST_PROFILE = "/api/programs/";// programs.create);
    public final static String REST_API_DELETE_PROFILE = "/api/programs/";// programs.delete);
    public final static String REST_API_GET_PROTOCOLS = "/api/device-protocols/";
    public final static String REST_API_POST_USER_PASSWORD = "/api/profiles/updatePwd";
    public final static String REST_API_UNBLOCK_USER = "/api/profiles/unblockUser";
    public final static String REST_API_GET_PROTOCOL = "/api/protocols/";
    public final static String REST_API_ENABLE_FDA = "/devices/fda/";// auth.login);
    public final static String REST_API_ENABLE_FDA_PERMISSION = "/api/devices/permission/";// auth.login);

    public final static String REST_POST_SUPPORT = "/api/support";// support.send); send email to certosupport
    public final static String REST_RESET_PASSWORD = "/recovery/sendcode";// support.send); send email to certosupport
    public final static String REST_API_POST_EMAIL = "/api/support/email/"; //send notification email to a specific email address
    public final static String REST_API_POST_SMS = "/api/support/sms/"; // send notification sms to a specific phone

    /**
     * Routes that can be accessed only by admin users
     */
    public final static String REST_POST_CREATE_DEVICE = "/api/admin/devices/";// devices.createAdmin);


    public static String getServerUrl() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext());
        String ip = pref.getString(PREFERENCE_KEY_SERVER_IP, CertocloudConstants.SERVER_URL);
        ip = ip.trim();
        if (!(ip.contains("https://") || ip.contains("http://")))
            ip = "http://" + ip;

        String port = pref.getString(PREFERENCE_KEY_SERVER_PORT, null);
        return ip + ((port != null && port.length() > 0) ? (":" + port) : "");
    }

}