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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.button.EditTextItem;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.database.UserController;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.responsemodels.UserInfoResponseModel;
import com.certoclav.app.util.MyCallback;
import com.certoclav.app.util.Requests;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.CertocloudConstants;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.PostUtil;
import com.certoclav.library.util.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegisterCloudAccountActivity extends CertoclavSuperActivity implements MyCallback {


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

    private User currentUser;
    private SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("LoginActivity", "onCreate");
        setContentView(R.layout.login_register);
        if (getIntent().hasExtra(AppConstants.INTENT_EXTRA_USER_ID)) {
            DatabaseService db = new DatabaseService(RegisterCloudAccountActivity.this);
            currentUser = db.getUserById(getIntent().getExtras().getInt(AppConstants.INTENT_EXTRA_USER_ID));
            Requests.getInstance().getUserInfo(currentUser.getEmail(), CloudUser.getInstance().getToken(), this, REQUEST_GET_USER);
        }
        CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(currentUser != null ? R.string.edit_user : R.string.register_new_user));


        linEditTextItemContainer = (LinearLayout) findViewById(R.id.register_container_edit_text_items);


        editEmailItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editEmailItem.setHint(getString(R.string.email));
        editEmailItem.setEnabled(currentUser == null);
        editEmailItem.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("@") && s.toString().contains("."))
                    editEmailItem.setHasValidString(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

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
                // TODO Auto-generated method stub

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
                // TODO Auto-generated method stub

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
                // TODO Auto-generated method stub

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
                // TODO Auto-generated method stub

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
                if (s.toString().length() > 3 || (s.toString().isEmpty() && currentUser != null))
                    editPasswordItem.setHasValidString(true);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

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
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        linEditTextItemContainer.addView(editPasswordItemConfirm);


        final DatabaseService databaseService = new DatabaseService(RegisterCloudAccountActivity.this);
        buttonRegister = (Button) findViewById(R.id.register_button_ok);
        buttonRegister.setText(currentUser != null ? R.string.save : R.string.register);

        buttonRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (currentUser == null)
                    for (User user : databaseService.getUsers()) {
                        if (editEmailItem.getText().equals(user.getEmail())) {
                            Toast.makeText(RegisterCloudAccountActivity.this, getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                if (currentUser != null && (!editCurPasswordItem.hasValidString() || !editCurPasswordItem.hasValidString())) {
                    Toast.makeText(RegisterCloudAccountActivity.this, getString(R.string.please_enter_password), Toast.LENGTH_LONG).show();
                    return;
                }
                Log.e("RegisterActivity", "onclickRegisterButton");
                if ((!editPasswordItem.hasValidString() || !editPasswordItemConfirm.hasValidString()) ||
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
                                false,
                                false);
                        if (params[1] == null || params[1].isEmpty()) {
                            user.setPassword(currentUser.getPassword());
                        } else {
                            user.setPassword(BCrypt.hashpw(params[1], BCrypt.gensalt()));
                        }

                        databaseService.insertUser(user);

                        UserController userController = new UserController(user, Autoclave.getInstance().getController());


                        return response;
                    }

                    @Override
                    protected void onPostExecute(Response response) {
                        hideDialog();
                        if (response != null && response.isOK()) {
                            Toast.makeText(getApplicationContext(), response.getMessage().isEmpty() ? getString(currentUser != null ? R.string.account_created : R.string.updated_successfully) : response.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            if (response.getStatus() == 200)
                                finish();
                        } else {
                            Toast.makeText(getApplicationContext(), (response == null || response.getMessage().isEmpty()) ? getString(R.string.sign_up_account_failed) : response.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        super.onPostExecute(response);
                    }


                }.execute(editEmailItem.getText().toString(), editPasswordItem.getText().toString(),
                        editPasswordItemConfirm.getText().toString(), editFirstName.getText(),
                        editLastName.getText(), editMobile.getText(), editCurPasswordItem.getText());


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
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setCancelable(false);
        pDialog.setTitleText(getString(R.string.loading));
        pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismissWithAnimation();
    }
}
