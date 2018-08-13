package com.certoclav.app.menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.adapters.UserDropdownAdapter;
import com.certoclav.app.database.AuditLog;
import com.certoclav.app.database.Controller;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.listener.ControllerInfoListener;
import com.certoclav.app.listener.DatabaseRefreshedListener;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.monitor.MonitorActivity;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.ServerConfigs;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.PostUserLoginService;
import com.certoclav.library.certocloud.PostUserLoginService.PutUserLoginTaskFinishedListener;
import com.certoclav.library.certocloud.PostUtil;
import com.certoclav.library.util.Response;
import com.certoclav.library.util.SettingsDeviceUtils;
import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.ProgressHelper;
import cn.pedant.SweetAlert.SweetAlertDialog;
import io.fabric.sdk.android.Fabric;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class LoginActivity extends CertoclavSuperActivity implements NavigationbarListener, DatabaseRefreshedListener, ControllerInfoListener, PutUserLoginTaskFinishedListener {


    private View buttonLogin;
    private View textViewLogin;
    private EditText editTextPassword;
    private ServerConfigs serverConfigs;
    private Spinner spinner;
    private List<User> listUsers;
    private User currentUser;
    private UserDropdownAdapter adapterUserDropdown;
    private CertoclavNavigationbarClean navigationbar;
    private PostUserLoginService postUserLoginService = null;
    private String loginFailedMessage = "";
    private TextView textViewNotification = null;
    private ProgressBar progressBar = null; // progess bar which shows cloud
    // login process

    // Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();


    final Runnable mShowLoginFailed = new Runnable() {
        public void run() {
            buttonLogin.setEnabled(true);
            textViewLogin.setEnabled(true);
            Toast.makeText(getApplicationContext(),
                    R.string.password_not_correct, Toast.LENGTH_LONG).show();
        }
    };

    final Runnable mShowCloudLoginFailed = new Runnable() {

        public void run() {
            buttonLogin.setEnabled(true);
            textViewLogin.setEnabled(true);

            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(loginFailedMessage)
                    .setContentText(getString(R.string.do_you_want_to_switch_to_offline_mode_))
                    .setConfirmText(getString(R.string.ok))
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            changeApplicationMode(false);
                            onResume();
                        }
                    }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    });
            sweetAlertDialog.setCanceledOnTouchOutside(true);
            sweetAlertDialog.setCancelable(true);
            ProgressHelper progressHelper = sweetAlertDialog.getProgressHelper();
            progressHelper.setBarWidth(1000);
            progressHelper.setRimWidth(1000);
            sweetAlertDialog.show();

        }
    };

    final Runnable mShowLoginSuccessfull = new Runnable() {
        public void run() {

            Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_LONG).show();
            buttonLogin.setEnabled(true);
            textViewLogin.setEnabled(true);
            Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
            Intent intent = new Intent(LoginActivity.this, MenuMain.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        try {
            if (new Date().after(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2018-10-15T09:27:37Z")))
                finish();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        serverConfigs = ServerConfigs.getInstance(this);
        AuditLogger.init();
        Fabric.with(this, new Crashlytics());
        AutoclaveMonitor.getInstance();
        Autoclave.getInstance().setController(new Controller("Touchscreen not connected",
                "Touchscreen not connected",
                "Touchscreen not connected",
                "Touchscreen not connected",
                0,
                "Touchscreen not connected"));
        progressBar = (ProgressBar) findViewById(R.id.login_progressbar);
        SettingsDeviceUtils settingsUtils = new SettingsDeviceUtils();

        if (AppConstants.TABLET_TYPE.equals(AppConstants.TABLET_TYPE_LILLIPUT)) {
            settingsUtils.setvolumeToMaximum(this);
        } else {
            settingsUtils.setvolumeToMedium(this);
        }
        settingsUtils.setScreenBrightnessToMaximum(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        final DatabaseService databaseService = DatabaseService.getInstance();

//        databaseService.fillDatabaseWithProgramIfEmpty();


        // initialize navigationbar
        navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.login));
        navigationbar.setSettingsVisible();
        navigationbar.hideButtonBack();
        navigationbar.setAddVisible(true);
        navigationbar.setNavigationbarListener(this);

        // initialize login form
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewLogin = findViewById(R.id.textViewLogin);
        editTextPassword = (EditText) findViewById(R.id.loginEditTextPassword);
        spinner = (Spinner) findViewById(R.id.login_spinner);

        textViewNotification = (TextView) findViewById(R.id.login_text_notification);
        textViewNotification.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText(getString(R.string.enable_network_communication))
                        .setContentText(getString(R.string.do_you_want_to_enable_network_communication))
                        .setConfirmText(getString(R.string.yes))
                        .setCancelText(getString(R.string.cancel))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                changeApplicationMode(true);
                                textViewNotification.setVisibility(View.GONE);
                            }
                        }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        }).setCustomImage(R.drawable.ic_network_connection);
                sweetAlertDialog.setCanceledOnTouchOutside(true);
                sweetAlertDialog.setCancelable(true);
                sweetAlertDialog.show();

            }
        });

        // read all users from database
        listUsers = databaseService.getUsers();

        // Fill Spinner with Emailaddresses of users

        adapterUserDropdown = new UserDropdownAdapter(this, listUsers);

        // adapterUserDropdown.setDropDownViewResource(R.layout.spinner_dropdown_item_large);
        spinner.setAdapter(adapterUserDropdown);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                try {
                    Autoclave.getInstance().setUser(listUsers.get(position));
                    currentUser = listUsers.get(position);
                    editTextPassword.setText("");
                } catch (IndexOutOfBoundsException e) {
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editTextPassword.setText("");
            }
        });

        buttonLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null)
                    logUser(currentUser.getEmail_user_id(), currentUser.getEmail(), currentUser.getFirstName() + " " + currentUser.getLastName());

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                Editor editor = prefs.edit();
                editor.putInt(AppConstants.PREFERENCE_KEY_ID_OF_LAST_USER,
                        currentUser.getUserId());
                editor.commit();

                if (Autoclave.getInstance().isOnlineMode(LoginActivity.this)) {
                    if (ApplicationController.getInstance()
                            .isNetworkAvailable() || ServerConfigs.getInstance(LoginActivity.this).getUrl() != null) {
                        String password = editTextPassword.getText().toString();
                        buttonLogin.setEnabled(false);
                        progressBar.setVisibility(View.VISIBLE);
                        textViewLogin.setVisibility(View.GONE);

                        if (Helper.checkAdminPassword(getApplicationContext(), password)) {
                            password = AppConstants.DEFAULT_CLOUD_ADMIN_PASSWORD;
                        }

                        postUserLoginService = new PostUserLoginService();
                        postUserLoginService.setOnTaskFinishedListener(LoginActivity.this);
                        postUserLoginService.loginUser(currentUser.getEmail(),
                                password,
                                Autoclave.getInstance().getDevice());
                    } else {
                        showNotificationForNetworkNavigation();
                    }

                } else {
                    new AsyncTask<String, Boolean, Boolean>() {

                        @Override
                        protected void onPreExecute() {
                            buttonLogin.setEnabled(false);
                            progressBar.setVisibility(View.VISIBLE);
                            textViewLogin.setVisibility(View.GONE);

                            super.onPreExecute();
                        }

                        @Override
                        protected Boolean doInBackground(String... params) {
                            if (BCrypt.checkpw(params[0], params[1])
                                    || params[0].toString().equals(AppConstants.DEFAULT_CLOUD_ADMIN_PASSWORD)
                                    || Helper.checkAdminPassword(getApplicationContext(), params[0])) {

                                DatabaseService databaseService = DatabaseService.getInstance();
                                // if the user trys to log in into this
                                // Controller the first time, then save him into
                                // the UserController Table.
                                User selectedUser = listUsers.get(Integer.valueOf(params[2]));


                                return true;

                            }
                            return false;
                        }

                        @Override
                        protected void onPostExecute(Boolean result) {
                            buttonLogin.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            textViewLogin.setVisibility(View.VISIBLE);
                            textViewLogin.setEnabled(true);

                            if (result) {
                                Toast.makeText(LoginActivity.this,
                                        getString(R.string.login_successful),
                                        Toast.LENGTH_LONG).show();
                                CloudUser.getInstance().setLoggedIn(true);
                                Autoclave.getInstance().setState(
                                        AutoclaveState.NOT_RUNNING);
                                Intent intent = new Intent(LoginActivity.this,
                                        MenuMain.class);
                                startActivity(intent);
                                AuditLogger.addAuditLog(currentUser, -1, AuditLogger.ACTION_SUCCESS_LOGIN, AuditLogger.OBJECT_EMPTY, null);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        R.string.password_not_correct,
                                        Toast.LENGTH_LONG).show();

                                AuditLogger.addAuditLog(currentUser, -1, AuditLogger.ACTION_FAILED_LOGIN, AuditLogger.OBJECT_EMPTY, null);

                            }
                            super.onPostExecute(result);
                        }

                    }.execute(editTextPassword.getText().toString(), currentUser.getPassword(), spinner.getSelectedItemPosition() + "");


                }

            }
        });
        // throw new RuntimeException();
        Helper.getPrograms(this);

        //get data from autoclave
//        ReadAndParseSerialService.getInstance().sendGetAdjustParameterCommand();
//        ReadAndParseSerialService.getInstance().sendGetUserProgramCommand();
//        ReadAndParseSerialService.getInstance().sendGetAdjustParameterCommand();
//        ReadAndParseSerialService.getInstance().sendGetUserProgramCommand();
    }


    @Override
    public void onResume() {
        Log.e("LoginActivity", "onresume called");
        super.onResume();
        DatabaseService db = DatabaseService.getInstance();
        db.createAdminAccountIfNotExistantYet();
        progressBar.setVisibility(View.GONE);
        textViewLogin.setVisibility(View.VISIBLE);
        CloudUser.getInstance().setLoggedIn(false);


        AutoclaveMonitor.getInstance();
        Autoclave.getInstance().setOnControllerInfoListener(this);

        refreshUI();

    }

    private void refreshUI() {


        navigationbar.setAddVisible(true);
        buttonLogin.setEnabled(true);
        textViewLogin.setEnabled(true);
        DatabaseService databaseService = DatabaseService.getInstance();

        listUsers = databaseService.getUsers();

        if (getDefaultSharedPreferences(this).getBoolean(
                AppConstants.PREFERENCE_KEY_ONLINE_MODE, false) == true) {
            textViewNotification.setVisibility(View.GONE);
        } else {
            textViewNotification.setVisibility(View.VISIBLE);
        }


        if (listUsers == null) {
            spinner.setEnabled(false);
            editTextPassword.setEnabled(false);
            buttonLogin.setEnabled(false);
            textViewLogin.setEnabled(false);
        } else if (listUsers.size() == 0) {
            spinner.setEnabled(false);
            editTextPassword.setEnabled(false);
            buttonLogin.setEnabled(false);
            textViewLogin.setEnabled(true);
        } else {
            if (Autoclave.getInstance().getController() != null) {
                navigationbar.setHeadText(getString(R.string.login));
            }
            spinner.setEnabled(true);
            editTextPassword.setEnabled(true);
            buttonLogin.setEnabled(true);
            textViewLogin.setEnabled(true);
        }

        // catch event, when user presses home button. Pressing home button
        // should take no effect. That means reopen last activity
        if (Autoclave.getInstance().getState() == AutoclaveState.RUNNING) {
            // buttonLogin.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(LoginActivity.this, MonitorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(intent);
            // finish();

        } else if (Autoclave.getInstance().getState() == AutoclaveState.NOT_RUNNING
                || Autoclave.getInstance().getState() == AutoclaveState.WAITING_FOR_CONFIRMATION) {
            // buttonLogin.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(this, MenuMain.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            this.startActivity(intent);
            // finish();
        }// else Autoclave is locked

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Fill Spinner with Emailaddresses of users
        adapterUserDropdown.clear();
        if (Autoclave.getInstance().getController() != null) {
            for (User user : listUsers) {
                adapterUserDropdown.add(user);
            }
        } else {
            Log.e("LoginActivity", "Autoclave.getcontroller == null");
        }


        int idOfLastUser = PreferenceManager.getDefaultSharedPreferences(this).getInt(AppConstants.PREFERENCE_KEY_ID_OF_LAST_USER, 0);
        if (idOfLastUser != 0) {
            User user = databaseService.getUserById(idOfLastUser);
            if (user != null) {
                if (listUsers.size() > 0) {
                    for (int i = 0; i < listUsers.size(); i++) {
                        if (user.getUserId() == listUsers.get(i).getUserId()) {
                            spinner.setSelection(i);
                            Autoclave.getInstance().setUser(user);
                        }
                    }
                }
            }
        }

        adapterUserDropdown.notifyDataSetChanged();

    }


    @Override
    public void onRefreshedUsers(boolean success) {

        refreshUI();

    }

    @Override
    public void onPause() {
        Autoclave.getInstance().removeOnControllerInfoListener(this);
        super.onPause();
    }

    @Override
    public void onControllerInfoReceived() {
        // refreshUI();
    }

    @Override
    public void onClickNavigationbarButton(int buttonId) {
        switch (buttonId) {
            case CertoclavNavigationbarClean.BUTTON_REFRESH:

                break;
            case CertoclavNavigationbarClean.BUTTON_ADD:

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                        prefs.getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_create_user),
                                ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_create_user))) {
                    Toast.makeText(this, R.string.these_settings_are_locked_by_the_admin, Toast.LENGTH_SHORT).show();
                    askForAdminPassword();
                } else {
                    askForSelecteOption();
                }

                break;
            case CertoclavNavigationbarClean.BUTTON_BACK:


                break;

        }

    }

    @Override
    public void onBackPressed() {
        Log.e("LoginActivity", "Hardware Button Back disabled");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            Log.e("LoginActivity", "Home Button disabled");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onTaskFinished(Response response) {
        if (response == null) {
            Toast.makeText(LoginActivity.this, getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.GONE);
        textViewLogin.setVisibility(View.VISIBLE);


        Log.e("LoginActivity", "onTaskFinished - statusCode: " + response);
        buttonLogin.setEnabled(true);
        textViewLogin.setEnabled(true);
        if (response.getMessage() != null)
            loginFailedMessage = response.getMessage();
        switch (response.getStatus()) {
            case PostUtil.RETURN_OK:
                Toast.makeText(LoginActivity.this,
                        getResources().getString(R.string.login_successful),
                        Toast.LENGTH_LONG).show();
                AuditLogger.addAuditLog(currentUser, -1, AuditLogger.ACTION_SUCCESS_LOGIN, AuditLogger.OBJECT_EMPTY, null);
                Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
                try {
                    if (Autoclave.getInstance().getUser().getIsLocal() == true) {
                        DatabaseService databaseService = DatabaseService.getInstance();
                        databaseService.updateUserIsLocal(Autoclave.getInstance()
                                .getUser().getEmail(), false);
                    }
                } catch (Exception e) {
                }
                Intent intent = new Intent(LoginActivity.this, MenuMain.class);
                startActivity(intent);

                new AsyncTask<Boolean, Boolean, Boolean>() {

                    @Override
                    protected Boolean doInBackground(Boolean... params) {

                        try {
                            PostUtil postUtil = new PostUtil();
                            JSONObject jsonDevice = new JSONObject();
                            jsonDevice.put("devicekey", Autoclave.getInstance()
                                    .getController().getSavetyKey());
                            postUtil.postToCertocloud(
                                    jsonDevice.toString(),
                                    CertocloudConstants.getServerUrl()
                                            + CertocloudConstants.REST_API_POST_DEVICE,
                                    true);

                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;

                        }

                        return true;
                    }
                }.execute();

                return; //break;

            case PostUtil.RETURN_UNKNOWN:
            case PostUtil.RETURN_ERROR:
                loginFailedMessage = getString(R.string.an_error_occured_during_login_please_try_again_later_);
                break;
            case PostUtil.RETURN_ERROR_TIMEOUT:
                loginFailedMessage = getString(R.string.timout_during_login_please_check_internet_availability_);
                break;
            case PostUtil.RETURN_ERROR_UNAUTHORISED_PASSWORD:
                loginFailedMessage = getString(R.string.password_not_correct_);
                break;
            case PostUtil.RETURN_ERROR_UNAUTHORISED_MAIL:
                loginFailedMessage = getString(R.string.email_does_not_exist_please_create_a_certocloud_account_with_this_email_first_);
                break;
            case PostUtil.RETURN_ERROR_UNKNOWN_HOST:
                loginFailedMessage = getString(R.string.not_able_to_connect_to_certocloud_);
                break;
            case PostUtil.RETURN_ERROR_ACCOUNT_NOT_ACTIVATED:
                Intent intent2 = new Intent(LoginActivity.this, ActivateCloudAccountActivity.class);
                startActivity(intent2);
                return;
        }

        AuditLogger.addAuditLog(currentUser, -1, AuditLogger.ACTION_FAILED_LOGIN, AuditLogger.OBJECT_EMPTY, null);
        mHandler.post(mShowCloudLoginFailed);

    }

    private void showCreateAccountDialog() {
        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.dialog_add_create, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.dialog_add_create);
        dialog.setTitle(R.string.register_new_user);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Button buttonCreateLocal = (Button) dialog
                .findViewById(R.id.dialogButtonCreateLocal);
        Button buttonAddExisting = (Button) dialog
                .findViewById(R.id.dialogButtonAddExisting);
        Button buttonCreateCloud = (Button) dialog
                .findViewById(R.id.dialogButtonCreateNew);
        buttonCreateLocal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeApplicationMode(false);
                Intent intent = new Intent(LoginActivity.this,
                        RegisterActivity.class);
                startActivity(intent);
                dialog.dismissWithAnimation();
            }
        });

        buttonAddExisting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ApplicationController.getInstance().isNetworkAvailable() || ServerConfigs.getInstance(LoginActivity.this).getUrl() != null) {
                    changeApplicationMode(true);
                    Intent intent = new Intent(LoginActivity.this,
                            AddCloudAccountActivity.class);
                    startActivity(intent);
                } else {
                    showNotificationForNetworkNavigation();
                }
                dialog.dismissWithAnimation();
            }
        });

        buttonCreateCloud.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ApplicationController.getInstance().isNetworkAvailable() || ServerConfigs.getInstance(LoginActivity.this).getUrl() != null) {

                    Intent i = new Intent(LoginActivity.this,
                            RegisterCloudAccountActivity.class);
                    startActivity(i);
                    dialog.dismiss();
                } else {
                    showNotificationForNetworkNavigation();

                }
                dialog.dismiss();
            }

        });

        dialog.show();

    }

    private void askForSelecteOption() {

        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.register_new_user))
                .setContentText(getString(R.string.do_you_really_want_to) + " "
                        + getString(R.string.create_an_account_))
                .setConfirmText(getString(R.string.yes))
                .setCancelText(getString(R.string.cancel))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        showCreateAccountDialog();
                    }
                }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
        sweetAlertDialog.setCanceledOnTouchOutside(true);
        sweetAlertDialog.setCancelable(true);
        sweetAlertDialog.show();
    }

    private void askForAdminPassword() {
        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.dialog_admin_password, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.dialog_admin_password);
        dialog.setTitle(R.string.register_new_user);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        final EditText editTextPassword = dialog.findViewById(R.id.editTextPassword);
        Button buttonLogin = (Button) dialog
                .findViewById(R.id.dialogButtonLogin);
        Button buttonCancel = (Button) dialog
                .findViewById(R.id.dialogButtonCancel);
        buttonLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helper.checkAdminPassword(LoginActivity.this, editTextPassword.getText().toString())) {
                    showCreateAccountDialog();
                    dialog.dismiss();
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.admin_password_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
            }
        });

        dialog.show();

    }


    private void showNotificationForNetworkNavigation() {
        try {

            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.network_connection_required))
                    .setContentText(getString(R.string.please_connect_to_a_network_via_lan_or_wifi_) + " "
                            + getString(R.string.do_you_want_to_open_wifi_settings_))
                    .setConfirmText(getString(R.string.yes))
                    .setCancelText(getString(R.string.switch_to_offline_mode))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent intent = new Intent(
                                    WifiManager.ACTION_PICK_WIFI_NETWORK);
                            intent.putExtra("only_access_points", true);
                            intent.putExtra("extra_prefs_show_button_bar", true);
                            intent.putExtra("wifi_enable_next_on_connect", true);
                            startActivity(intent);
                        }
                    }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            changeApplicationMode(false);
                            refreshUI();
                        }
                    });
            sweetAlertDialog.setCanceledOnTouchOutside(true);
            sweetAlertDialog.setCancelable(true);
            sweetAlertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void changeApplicationMode(boolean isOnline) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        Editor editor = prefs.edit();
        editor.putBoolean(AppConstants.PREFERENCE_KEY_ONLINE_MODE,
                isOnline);
        editor.commit();
    }

    private void logUser(String userID, String email, String username) {
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(userID);
        Crashlytics.setUserEmail(email);
        Crashlytics.setUserName(username);
    }

}
