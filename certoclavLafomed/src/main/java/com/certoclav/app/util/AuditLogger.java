package com.certoclav.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.AuditLog;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.model.Autoclave;
import com.certoclav.library.application.ApplicationController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

public class AuditLogger {
    private static Context context;
    private static AuditLogger logger;
    private DatabaseService databaseService;
    private SharedPreferences prefs;

    public static final int SCEEN_LOCKOUT = 1;//R.string.lockout;
    public static final int SCEEN_SETTINGS = 2;//R.string.settings;
    public static final int SCEEN_EMPTY = -1;

    //Actions
    public static final int ACTION_SELECTED = 3;//R.string.audit_action_selected;
    public static final int ACTION_CLICKED = 4;//R.string.audit_action_clicked;
    public static final int ACTION_OPEN_WINDOW = 5;//R.string.audit_action_open_window;
    public static final int ACTION_CANCELED = 6;//R.string.audit_action_canceled;
    public static final int ACTION_CONFIRMED = 7;//R.string.audit_action_confirmed;
    public static final int ACTION_USER_CREATED = 8;//R.string.audit_action_user_created;
    public static final int ACTION_USER_DELETED = 9;//R.string.audit_action_user_deleted;
    public static final int ACTION_PROGRAM_STARTED = 10;//R.string.audit_action_program_started;
    public static final int ACTION_PROGRAM_INDICATOR_CHANGED = 11;//R.string.audit_action_program_indicator_changed;
    public static final int ACTION_PROGRAM_CANCELED = 12;//R.string.audit_action_program_canceled;
    public static final int ACTION_PROGRAM_FINISHED = 13;//R.string.audit_action_program_finished;
    public static final int ACTION_PROGRAM_FAILED = 30;
    public static final int ACTION_FAILED_LOGIN = 14;//R.string.audit_action_failed_to_login;
    public static final int ACTION_SUCCESS_LOGIN = 15;//R.string.audit_action_success_to_login;
    public static final int ACTION_LOGOUT = 16;//R.string.audit_action_logout;
    public static final int ACTION_ACTIVATED = 17;//R.string.audit_action_activated;
    public static final int ACTION_PREF_CHANGED = 18;//R.string.audit_action_pref_changed;
    public static final int ACTION_PROGRAM_FINISHED_MAINTAIN_TEMP = 19;//R.string.audit_action_program_finished_maintain;
    public static final int ACTION_PROGRAM_EDITED = 31;//R.string.audit_action_program_edited;
    public static final int ACTION_PROGRAM_DELETED = 32;//R.string.audit_action_program_edited;
    public static final int ACTION_USER_BLOCKED = 33;//R.string.audit_action_program_edited;
    public static final int ACTION_USER_UNBLOCKED = 34;//R.string.audit_action_program_edited;
    public static final int ACTION_USER_BLOCKED_TEMPORALLY = 35;//R.string.audit_action_program_edited;
    public static final int ACTION_USER_UNBLOCKED_TEMPORALLY = 36;//R.string.audit_action_program_edited;
    public static final int ACTION_USER_PASSWORD_UPDATED = 37;//R.string.audit_action_program_edited;
    public static final int ACTION_ADMIN_APPROVED_EDIT_DELETE_PROGRAM = 38;//R.string.audit_action_program_edited;
    public static final int ACTION_ADMIN_FAILED_LOGIN = 39;//R.string.audit_action_failed_to_login;
    public static final int ACTION_LOGOUT_AUTO = 40;//R.string.audit_action_logout;
    public static final int ACTION_CAL_CHANGED = 41;//R.string.audit_action_pref_changed;
    public static final int ACTION_ADMIN_APPROVED_EDIT_CALIBRARION = 42;//R.string.audit_action_program_edited;
    public static final int ACTION_PROGRAM_CREATED = 43;//R.string.audit_action_program_edited;
    public static final int ACTION_USER_UPDATED = 44; // R.string.audit_action_user_updated

    //Object Names
    public static final int OBJECT_NETWORK_SETTINGS = 19;//R.string.network_settings;
    public static final int OBJECT_EMPTY = 20;//R.string.audit_object_empty;
    public static final int OBJECT_LOGIN_BUTTON = 21;//R.string.audit_object_login_button;
    public static final int OBJECT_LOGIN = 22;//R.string.audit_object_login;
    public static final int OBJECT_ADD_USER_BUTTON = 23;//R.string.audit_object_add_user_button;
    public static final int OBJECT_CANCEL_BUTTON = 24;//R.string.audit_object_cancel_button;
    public static final int OBJECT_YES_BUTTON = 25;//R.string.audit_object_yes_button;
    public static final int OBJECT_SETTINGS_BUTTON = 26;//R.string.audit_object_setting_button;
    public static final int OBJECT_CREATE_LOCAL_BUTTON = 27;//R.string.audit_object_create_local_button;
    public static final int OBJECT_ADD_EXISTS_BUTTON = 28;//R.string.audit_objectadd_exists_button;
    public static final int OBJECT_CREATE_NEW_BUTTON = 29;//R.string.audit_object_create_new_button;


    private static Map<Integer, Integer> map = new HashMap<>();

    private AuditLogger() {
        context = AppController.getInstance().getApplicationContext();
        databaseService = DatabaseService.getInstance();
        prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext());
        //Screens
        map.put(SCEEN_LOCKOUT, R.string.lockout);
        map.put(SCEEN_SETTINGS, R.string.settings);
        map.put(SCEEN_EMPTY, -1);

        //Actions
        map.put(ACTION_SELECTED, R.string.audit_action_selected);
        map.put(ACTION_CLICKED, R.string.audit_action_clicked);
        map.put(ACTION_OPEN_WINDOW, R.string.audit_action_open_window);
        map.put(ACTION_CANCELED, R.string.audit_action_canceled);
        map.put(ACTION_CONFIRMED, R.string.audit_action_confirmed);
        map.put(ACTION_USER_CREATED, R.string.audit_action_user_created);
        map.put(ACTION_USER_DELETED, R.string.audit_action_user_deleted);
        map.put(ACTION_PROGRAM_STARTED, R.string.audit_action_program_started);
        map.put(ACTION_PROGRAM_EDITED, R.string.audit_action_program_edited);
        map.put(ACTION_PROGRAM_CREATED, R.string.audit_action_program_created);
        map.put(ACTION_PROGRAM_DELETED, R.string.audit_action_program_deleted);
        map.put(ACTION_PROGRAM_INDICATOR_CHANGED, R.string.audit_action_program_indicator_changed);
        map.put(ACTION_PROGRAM_CANCELED, R.string.audit_action_program_canceled);
        map.put(ACTION_PROGRAM_FINISHED, R.string.audit_action_program_finished);
        map.put(ACTION_PROGRAM_FINISHED_MAINTAIN_TEMP, R.string.audit_action_program_finished_maintain);
        map.put(ACTION_PROGRAM_FAILED, R.string.audit_action_program_failed);
        map.put(ACTION_FAILED_LOGIN, R.string.audit_action_failed_to_login);
        map.put(ACTION_ADMIN_FAILED_LOGIN, R.string.audit_action_admin_failed_to_login);
        map.put(ACTION_SUCCESS_LOGIN, R.string.audit_action_success_to_login);
        map.put(ACTION_LOGOUT, R.string.audit_action_logout);
        map.put(ACTION_LOGOUT_AUTO, R.string.audit_action_logout_auto);
        map.put(ACTION_ACTIVATED, R.string.audit_action_activated);
        map.put(ACTION_PREF_CHANGED, R.string.audit_action_pref_changed);
        map.put(ACTION_CAL_CHANGED, R.string.audit_action_cal_changed);
        map.put(ACTION_USER_BLOCKED, R.string.audit_action_user_blocked);
        map.put(ACTION_USER_UNBLOCKED, R.string.audit_action_user_unblocked);
        map.put(ACTION_USER_BLOCKED_TEMPORALLY, R.string.audit_action_user_blocked_temp);
        map.put(ACTION_USER_UNBLOCKED_TEMPORALLY, R.string.audit_action_user_unblocked_temp);
        map.put(ACTION_USER_PASSWORD_UPDATED, R.string.audit_action_user_password_udpated);
        map.put(ACTION_ADMIN_APPROVED_EDIT_DELETE_PROGRAM, R.string.audit_action_admin_approved);
        map.put(ACTION_ADMIN_APPROVED_EDIT_CALIBRARION, R.string.audit_action_admin_approved_edit_calibration);
        map.put(ACTION_USER_UPDATED, R.string.audit_action_user_updated);

        //Object Names
        map.put(OBJECT_NETWORK_SETTINGS, R.string.network_settings);
        map.put(OBJECT_EMPTY, R.string.audit_object_empty);
        map.put(OBJECT_LOGIN_BUTTON, R.string.audit_object_login_button);
        map.put(OBJECT_LOGIN, R.string.audit_object_login);
        map.put(OBJECT_ADD_USER_BUTTON, R.string.audit_object_add_user_button);
        map.put(OBJECT_CANCEL_BUTTON, R.string.audit_object_cancel_button);
        map.put(OBJECT_YES_BUTTON, R.string.audit_object_yes_button);
        map.put(OBJECT_SETTINGS_BUTTON, R.string.audit_object_setting_button);
        map.put(OBJECT_CREATE_LOCAL_BUTTON, R.string.audit_object_create_local_button);
        map.put(OBJECT_ADD_EXISTS_BUTTON, R.string.audit_objectadd_exists_button);
        map.put(OBJECT_CREATE_NEW_BUTTON, R.string.audit_object_create_new_button);


        map.putAll(Helper.getInstance().getPreferenceTitles());

    }

    public static AuditLogger getInstance() {
        if (logger == null)
            logger = new AuditLogger();
        return logger;
    }

    public void addAuditLog(AuditLog auditLog) {
        init();
        databaseService.addAuditLog(auditLog);
    }


    public void addAuditLog(User user, @StringRes int screenId, @StringRes int eventId, int objectId, String value, boolean shouldAskForComment) {
        init();
        if (context == null) return;

        if (isCommentEnabled() && shouldAskForComment)
            askForComment(user, screenId, eventId, objectId, value);
        else
            databaseService.addAuditLog(new AuditLog(user, screenId, eventId, objectId, value, null));
    }

    public void addAuditLog(@StringRes int screenId, @StringRes int eventId, int objectId, String value, boolean shouldAskForComment) {
        init();
        if (context == null) return;

        if (isCommentEnabled() && shouldAskForComment)
            askForComment(Autoclave.getInstance().getUser(), screenId, eventId, objectId, value);
        else
            databaseService.addAuditLog(new AuditLog(Autoclave.getInstance().getUser(), screenId, eventId, objectId, value, null));
    }

    public List<AuditLog> getAuditLogs(User user, String orderBy, boolean isAsc) {
        return databaseService.getAuditLogs(user, orderBy, isAsc);
    }

    public static void init() {
        if (logger == null) {
            logger = new AuditLogger();
        }
    }

    public static int getResource(int id) {
        if (map.containsKey(id))
            return map.get(id);
        return id;
    }

    private boolean isCommentEnabled() {
        return prefs.getBoolean(AppConstants.PREFERENCE_KEY_ENABLE_AUDIT_COMMENT, false);
    }

    private void askForComment(final User user, @StringRes final int screenId, @StringRes final int eventId, final int objectId, final String value) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final SweetAlertDialog dialog = new SweetAlertDialog(context, R.layout.dialog_ask_comment_audit, SweetAlertDialog.WARNING_TYPE);
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);

                Window window = dialog.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.gravity = Gravity.TOP;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                window.setAttributes(wlp);

                dialog.setContentView(R.layout.dialog_ask_comment_audit);
                dialog.setTitle(R.string.register_new_user);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                final EditText editTextDesc = dialog.findViewById(R.id.editTextDesc);
                Button buttonSave = dialog
                        .findViewById(R.id.dialogButtonSave);
                buttonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (editTextDesc.getText().toString().isEmpty())
                            Toasty.warning(context, context.getString(R.string.please_fill_comment_field), Toast.LENGTH_SHORT, true).show();
                        else {
                            databaseService.addAuditLog(new AuditLog(user, screenId, eventId, objectId, value, editTextDesc.getText().toString()));
                            dialog.dismissWithAnimation();
                        }
                    }
                });


                dialog.show();
            }
        });
    }


}
