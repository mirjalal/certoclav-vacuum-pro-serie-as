package com.certoclav.app.menu;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.adapters.UserDropdownAdapter;
import com.certoclav.app.button.CheckboxItem;
import com.certoclav.app.button.EditTextItem;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.database.UserController;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.responsemodels.UserInfoResponseModel;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.MyCallback;
import com.certoclav.app.util.Requests;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.PostUtil;
import com.certoclav.library.util.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

public class RegisterCloudAccountActivity extends CertoclavSuperActivity implements MyCallback, NavigationbarListener {


    private LinearLayout linEditTextItemContainer;
    private Button buttonRegister;
    private final static int REQUEST_GET_USER = 1;

    private EditTextItem editEmailItem;
    private EditTextItem editPasswordItem;
    private EditTextItem editPasswordItemConfirm;
    private EditTextItem editMobile;
    private EditTextItem editFirstName;
    private EditTextItem editLastName;
    private EditTextItem editCurPasswordItem;
    private CheckboxItem checkBoxIsAdmin;

    private User currentUser;
    private SweetAlertDialog pDialog;
    private boolean isManual = true;
    private CertoclavNavigationbarClean navigationbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("LoginActivity", "onCreate");
        setContentView(R.layout.login_register);
        if (getIntent().hasExtra(AppConstants.INTENT_EXTRA_USER_ID)) {
            DatabaseService db = DatabaseService.getInstance();
            currentUser = db.getUserById(getIntent().getExtras().getInt(AppConstants.INTENT_EXTRA_USER_ID));
            Requests.getInstance().getUserInfo(currentUser.getEmail(), CloudUser.getInstance().getToken(), this, REQUEST_GET_USER);
        }
        navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(currentUser != null ? R.string.edit_user : R.string.register_new_user));


        linEditTextItemContainer = (LinearLayout) findViewById(R.id.register_container_edit_text_items);

        //Checkbox is Admin
        checkBoxIsAdmin = (CheckboxItem) getLayoutInflater().inflate(R.layout.checkbox_item, linEditTextItemContainer, false);
        checkBoxIsAdmin.setText(getString(R.string.admin));
        checkBoxIsAdmin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isManual)
                    askForAdminPassword();
                isManual = true;
            }
        });

        linEditTextItemContainer.addView(checkBoxIsAdmin);


        editEmailItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editEmailItem.setHint(getString(R.string.email));
        editEmailItem.setEnabled(currentUser == null);
        editEmailItem.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches())
                    editEmailItem.setHasValidString(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // 

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        linEditTextItemContainer.addView(editEmailItem);


        editFirstName = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editFirstName.setHint(getString(R.string.first_name));
        editFirstName.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty() == false)
                    editFirstName.setHasValidString(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // 

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        editFirstName.requestFocus();
        linEditTextItemContainer.addView(editFirstName);


        editLastName = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editLastName.setHint(getString(R.string.last_name));
        editLastName.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty() == false)
                    editLastName.setHasValidString(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // 

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        linEditTextItemContainer.addView(editLastName);


        editMobile = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editMobile.setHint(getString(R.string.mobile_number));
        editMobile.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty() == false && (s.toString().startsWith("+") || s.toString().startsWith("00"))) {
                    editMobile.setHasValidString(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // 

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        linEditTextItemContainer.addView(editMobile);

        editCurPasswordItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editCurPasswordItem.setHint(getString(R.string.cur_password));
        editCurPasswordItem.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editCurPasswordItem.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 3)
                    editCurPasswordItem.setHasValidString(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // 

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty())
            linEditTextItemContainer.addView(editCurPasswordItem);

        editPasswordItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editPasswordItem.setHint(getString(R.string.password));
        editPasswordItem.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPasswordItem.setHasValidString(currentUser != null);
        editPasswordItem.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 5 || (s.toString().isEmpty() && currentUser != null))
                    editPasswordItem.setHasValidString(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // 

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        linEditTextItemContainer.addView(editPasswordItem);


        editPasswordItemConfirm = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editPasswordItemConfirm.setHint(getString(R.string.password_confirm));
        editPasswordItemConfirm.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPasswordItemConfirm.setHasValidString(currentUser != null);
        editPasswordItemConfirm.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editPasswordItem.getText().equals(s.toString()) || (editPasswordItem.toString().isEmpty() && currentUser != null)) {
                    editPasswordItemConfirm.setHasValidString(true);
                } else {
                    editPasswordItemConfirm.setHasValidString(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // 

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        linEditTextItemContainer.addView(editPasswordItemConfirm);


        final DatabaseService databaseService = DatabaseService.getInstance();
        buttonRegister = (Button) findViewById(R.id.register_button_ok);
        buttonRegister.setText(currentUser != null ? R.string.save : R.string.register);

        buttonRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (currentUser == null)
                    for (User user : databaseService.getUsers()) {
                        if (editEmailItem.getText().equals(user.getEmail())) {
                            Toasty.error(RegisterCloudAccountActivity.this, getString(R.string.email_already_exists), Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                    }

                if (currentUser != null && (!editCurPasswordItem.hasValidString() || !editCurPasswordItem.hasValidString())) {
                    Toasty.warning(RegisterCloudAccountActivity.this, getString(R.string.please_enter_password), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                Log.e("RegisterActivity", "onclickRegisterButton");

                if (!editPasswordItem.hasValidString() || editPasswordItem.getText().length() < 6) {
                    Toast.makeText(RegisterCloudAccountActivity.this, getString(R.string.passwords_min_length), Toast.LENGTH_LONG).show();
                    return;
                }

                if ((!editPasswordItemConfirm.hasValidString()) ||
                        !editPasswordItem.getText().equals(editPasswordItemConfirm.getText())) {
                    Toast.makeText(RegisterCloudAccountActivity.this, getString(R.string.passwords_do_not_match), Toast.LENGTH_LONG).show();
                    return;
                }
                if (!editEmailItem.hasValidString()) {
                    Toast.makeText(RegisterCloudAccountActivity.this, getString(R.string.please_enter_a_valid_email_address), Toast.LENGTH_LONG).show();
                    return;
                }
                showDialog();
                new AsyncTask<String, Boolean, Response>() {

                    @Override
                    protected Response doInBackground(String... params) {

                        JSONObject jsonObjectRegister;
                        try {
                            jsonObjectRegister = new JSONObject();
                            jsonObjectRegister.put("username", params[0]);
                            jsonObjectRegister.put("password", params[1]);
                            jsonObjectRegister.put("passwordRepeat", params[2]);
                            jsonObjectRegister.put("firstname", params[3]);
                            jsonObjectRegister.put("lastname", params[4]);
                            jsonObjectRegister.put("mobile", params[5]);
                            jsonObjectRegister.put("cur_password", params[6]);
                            jsonObjectRegister.put("is_admin", Boolean.valueOf(params[7]));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return null;
                        }


                        PostUtil postUtil = new PostUtil();
                        Response response = postUtil.postToCertocloud(jsonObjectRegister.toString(),
                                CertocloudConstants.getServerUrl() + (currentUser != null ?
                                        CertocloudConstants.REST_API_POST_EDIT_USER :
                                        CertocloudConstants.REST_API_POST_SIGNUP), false);
                        if (postUtil.getResponseBody() != null) {
                            Log.e("RegisterCloudUser", "response: " + postUtil.getResponseBody());
                        }
                        if (response.getStatus() != PostUtil.RETURN_OK_200) {
                            return response;
                        }
                        if (currentUser != null)
                            databaseService.deleteUser(currentUser);
                        User user = new User(
                                params[3],
                                "",
                                params[4],
                                params[0],
                                params[5],
                                "",
                                "",
                                "",
                                "",
                                "",
                                new Date(),
                                Boolean.valueOf(params[7]),
                                false);
                        if (params[1] == null || params[1].isEmpty()) {
                            user.setPassword(currentUser.getPassword());
                        } else {
                            user.setPassword(BCrypt.hashpw(params[1], BCrypt.gensalt()));
                        }

                        databaseService.insertUser(user);

                        if (currentUser != null && currentUser.getEmail().equalsIgnoreCase(user.getEmail()))
                            currentUser = user;

                        UserController userController = new UserController(user, Autoclave.getInstance().getController());


                        return response;
                    }

                    @Override
                    protected void onPostExecute(Response response) {
                        hideDialog();
                        if (response != null && response.isOK()) {
                            Toasty.success(getApplicationContext(), response.getMessage().isEmpty() ? getString(currentUser != null ? R.string.account_created : R.string.updated_successfully) : response.getMessage(),
                                    Toast.LENGTH_SHORT, true).show();
                            if (currentUser != null) {
                                Autoclave.getInstance().setUser(currentUser);
                            }
                            if (response.getStatus() == 200) {
                                onBackPressed();
                            }
                        } else {
                            Toasty.error(getApplicationContext(), (response == null || response.getMessage().isEmpty()) ? getString(R.string.sign_up_account_failed) : response.getMessage(),
                                    Toast.LENGTH_SHORT, true).show();
                        }
                        super.onPostExecute(response);
                    }


                }.execute(editEmailItem.getText(),
                        editPasswordItem.getText().length() > 0 ?
                                editPasswordItem.getText() :
                                editCurPasswordItem.getText(),
                        editPasswordItemConfirm.getText().length() > 0 ?
                                editPasswordItemConfirm.getText() :
                                editCurPasswordItem.getText(),
                        editFirstName.getText(),
                        editLastName.getText(),
                        editMobile.getText(),
                        editCurPasswordItem.getText(),
                        checkBoxIsAdmin.isChecked() + "");


            }
        });


        if (currentUser != null) {
            editEmailItem.setText(currentUser.getEmail());
            editFirstName.setText(currentUser.getFirstName());
            editLastName.setText(currentUser.getLastName());
            editMobile.setText(currentUser.getMobile());
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            Log.d("Activity", "Touch event " + event.getRawX() + "," + event.getRawY() + " " + x + "," + y + " rect " + w.getLeft() + "," + w.getTop() + "," + w.getRight() + "," + w.getBottom() + " coords " + scrcoords[0] + "," + scrcoords[1]);
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }

    @Override
    public void onSuccess(Object response, int requestId) {
        hideDialog();
        switch (requestId) {
            case REQUEST_GET_USER:
                UserInfoResponseModel responseModel = (UserInfoResponseModel) response;
                if (responseModel.isOk() && responseModel.getUser() != null) {
                    User user = responseModel.getUser();
                    editEmailItem.setText(user.getEmail());
                    editFirstName.setText(user.getFirstName());
                    editLastName.setText(user.getLastName());
                    editMobile.setText(user.getMobile());
                    isManual = false;
                    checkBoxIsAdmin.setChecked(user.isAdmin());
                    isManual = true;
                }
        }
    }

    @Override
    public void onError(ErrorModel error, int requestId) {
        if (error.getStatusCode() == 403)
            finish();
        else
            pDialog
                    .setTitleText(getString(R.string.error))
                    .setContentText(error.getMessage())
                    .setConfirmText(getString(R.string.try_again))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            hideDialog();
                            Requests.getInstance().getUserInfo(currentUser.getEmail(), CloudUser.getInstance().getToken(), RegisterCloudAccountActivity.this, REQUEST_GET_USER);
                        }
                    }).setCancelText(getString(R.string.close)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    RegisterCloudAccountActivity.this.finish();
                }
            })
                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
    }

    @Override
    public void onStart(int requestId) {
        showDialog();
    }

    @Override
    public void onProgress(int current, int max) {

    }

    private void showDialog() {
        try {
            if (pDialog != null && pDialog.isShowing())
                pDialog.dismiss();
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setCancelable(false);
            pDialog.setTitleText(getString(R.string.loading));
            pDialog.show();
        } catch (Exception e) {

        }
    }

    private void hideDialog() {
        try {

            if (pDialog.isShowing())
                pDialog.dismissWithAnimation();
        } catch (Exception e) {

        }
    }

    private void askForAdminPassword() {

        final List<User> adminUsers = new ArrayList<>();
        for (User u : DatabaseService.getInstance().getUsers())
            if (u.isAdmin())
                adminUsers.add(u);
        UserDropdownAdapter adapterUserDropdown = new UserDropdownAdapter(this, adminUsers);

        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.dialog_admin_password, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.dialog_admin_password);
        dialog.setTitle(R.string.register_new_user);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        final EditText editTextPassword = dialog.findViewById(R.id.editTextDesc);
        Button buttonLogin = (Button) dialog
                .findViewById(R.id.dialogButtonLogin);
        Button buttonCancel = (Button) dialog
                .findViewById(R.id.dialogButtonCancel);

        final Spinner spinnerAdmins = dialog.findViewById(R.id.login_spinner);
        spinnerAdmins.setAdapter(adapterUserDropdown);
        buttonLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helper.getInstance().checkUserValidation(
                        RegisterCloudAccountActivity.this,
                        adminUsers.get(spinnerAdmins.getSelectedItemPosition()),
                        editTextPassword.getText().toString())) {
                    dialog.dismiss();
                } else {
                    Toasty.error(RegisterCloudAccountActivity.this, getString(R.string.admin_password_wrong), Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        buttonCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isManual = false;
                checkBoxIsAdmin.setChecked(!checkBoxIsAdmin.isChecked());
                dialog.dismissWithAnimation();
            }
        });

        dialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigationbar != null)
            navigationbar.setNavigationbarListener(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (navigationbar != null)
            navigationbar.removeNavigationbarListener(this);
    }

    @Override
    public void onClickNavigationbarButton(int buttonId) {
        onBackPressed();
    }
}
