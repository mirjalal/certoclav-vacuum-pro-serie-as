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
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.util.Helper;
import com.certoclav.library.bcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

public class RegisterActivity extends CertoclavSuperActivity {
    private LinearLayout linEditTextItemContainer;
    private Button buttonRegister;

    private CheckboxItem checkBoxIsAdmin;
    private EditTextItem editEmailItem;
    private EditTextItem editCurPasswordItem;
    private EditTextItem editPasswordItem;
    private EditTextItem editPasswordItemConfirm;
    private EditTextItem editMobile;
    private EditTextItem editFirstName;
    private EditTextItem editLastName;
    private SweetAlertDialog pDialog;
    private boolean isEdit;
    private User userToBeEdited;

    private boolean isManual = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("LoginActivity", "onCreate");
        setContentView(R.layout.login_register);
        CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
        isEdit = getIntent().hasExtra(AppConstants.INTENT_EXTRA_USER_ID);
        if (isEdit) {
            DatabaseService db = DatabaseService.getInstance();
            userToBeEdited = db.getUserById(getIntent().getExtras().getInt(AppConstants.INTENT_EXTRA_USER_ID));
        }
        navigationbar.setHeadText(getString(isEdit ? R.string.edit_user : R.string.register_new_user));

        linEditTextItemContainer = findViewById(R.id.register_container_edit_text_items);

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
                if (!s.toString().isEmpty())
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
        linEditTextItemContainer.addView(editFirstName);


        editLastName = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editLastName.setHint(getString(R.string.last_name));
        editLastName.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty())
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
                if (!s.toString().isEmpty())
                    editMobile.setHasValidString(true);
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
        if (userToBeEdited != null && userToBeEdited.getEmail() != null && !userToBeEdited.getEmail().isEmpty())
            linEditTextItemContainer.addView(editCurPasswordItem);


        editPasswordItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editPasswordItem.setHint(getString(R.string.new_password));
        editPasswordItem.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPasswordItem.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 5)
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
        editPasswordItemConfirm.setHint(getString(R.string.confirm_password));
        editPasswordItemConfirm.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPasswordItemConfirm.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editPasswordItem.getText().equals(s.toString())) {
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
        buttonRegister = findViewById(R.id.register_button_ok);

        buttonRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!isEdit)
                    for (User user : databaseService.getUsers()) {
                        if (editEmailItem.getText().toLowerCase().equals(user.getEmail().toLowerCase())) {
                            Toasty.error(RegisterActivity.this, "Email already exists", Toast.LENGTH_SHORT, true).show();
                            return;
                        }
                    }

                Log.e("RegisterActivity", "onclickRegisterButton");
                if (editPasswordItem.getVisibility() == View.VISIBLE) {

                    if (!editPasswordItem.hasValidString() || editPasswordItem.getText().length() < 6) {
                        Toasty.warning(RegisterActivity.this, getString(R.string.passwords_min_length), Toast.LENGTH_SHORT, true).show();
                        return;
                    }

                    if (!editPasswordItem.hasValidString() || !editPasswordItemConfirm.hasValidString()) {
                        Toasty.warning(RegisterActivity.this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT, true).show();
                        return;
                    }

                    if (!editEmailItem.hasValidString()) {
                        Toasty.warning(RegisterActivity.this, getString(R.string.please_enter_a_valid_email_address), Toast.LENGTH_SHORT, true).show();
                        return;
                    }
                }

                final Boolean isLocal = true;

//                if (getIntent().hasExtra(AppConstants.INTENT_EXTRA_USER_ID)) {
//                    DatabaseService db = DatabaseService.getInstance();
//                    int retval = db.deleteUser(db.getUserById(getIntent().getExtras().getInt(AppConstants.INTENT_EXTRA_USER_ID)));
//                    if (retval != 1) {
//                        Toasty.error(RegisterActivity.this, "Failed to apply changes", Toast.LENGTH_SHORT, true).show();
//                        return;
//                    }
//                }
                buttonRegister.setEnabled(false);

                new AsyncTask<String, Boolean, Integer>() {
                    @Override
                    protected void onPreExecute() {
                        showDialog();
                        super.onPreExecute();
                    }

                    @Override
                    protected Integer doInBackground(String... params) {
                        User user = new User(
                                params[0],
                                "",
                                params[1],
                                params[2],
                                params[3],
                                "",
                                "",
                                "",
                                "",
                                BCrypt.hashpw(params[5], BCrypt.gensalt()),
                                new Date(),
                                Boolean.valueOf(params[6]),
                                isLocal);

                        if (isEdit) {
                            if (BCrypt.checkpw(params[4], userToBeEdited.getPassword())) // current password is correct
                                return databaseService.updateUserProfile(user, userToBeEdited.getUserId());
                            else
                                return -1;
                        } else
                            return databaseService.insertUser(user);
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        hideDialog();
                        buttonRegister.setEnabled(true);
                        if (result == -1) {
                            Toasty.error(getApplicationContext(),
                                    getString(R.string.current_password_wrong),
                                    Toast.LENGTH_LONG,
                                    true).show();
                        } else if (result == 1) {
                            Toasty.success(getApplicationContext(),
                                    getString(isEdit ? R.string.edit_success : R.string.account_created),
                                    Toast.LENGTH_SHORT,
                                    true).show();
                            finish();
                        } else {
                            Toasty.error(getApplicationContext(),
                                    getString(R.string.database_error),
                                    Toast.LENGTH_LONG,
                                    true).show();
                        }
                        super.onPostExecute(result);
                    }
                }.execute(editFirstName.getText(), editLastName.getText(), editEmailItem.getText(), editMobile.getText(), editCurPasswordItem.getText(), editPasswordItem.getText(), checkBoxIsAdmin.isChecked() + "");
            }
        });
    }

    @Override
    protected void onResume() {

        if (getIntent().hasExtra(AppConstants.INTENT_EXTRA_USER_ID)) {
            try {
                DatabaseService db = DatabaseService.getInstance();
                User user = db.getUserById(getIntent().getExtras().getInt(AppConstants.INTENT_EXTRA_USER_ID));
                checkBoxIsAdmin.setChecked(user.isAdmin());
                editEmailItem.setText(user.getEmail());
                editEmailItem.setEnabled(false);
                editMobile.setText(user.getMobile());
                editFirstName.setText(user.getFirstName());
                editLastName.setText(user.getLastName());
                buttonRegister.setText(R.string.apply_changes);
            } catch (Exception e) {

            }
        } else {
            buttonRegister.setText(R.string.register);
        }
        super.onResume();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int[] scrcoords = new int[2];
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

    private void showDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
            pDialog = null;
        }
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setCancelable(false);
        pDialog.setTitleText(isEdit ? getString(R.string.editing) : getString(R.string.adding));
        pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismissWithAnimation();
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
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        final EditText editTextPassword = dialog.findViewById(R.id.editTextDesc);
        Button buttonLogin = dialog
                .findViewById(R.id.dialogButtonLogin);
        Button buttonCancel = dialog
                .findViewById(R.id.dialogButtonCancel);

        final Spinner spinnerAdmins = dialog.findViewById(R.id.login_spinner);
        spinnerAdmins.setAdapter(adapterUserDropdown);
        buttonLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helper.getInstance().checkUserValidation(
                        RegisterActivity.this,
                        adminUsers.get(spinnerAdmins.getSelectedItemPosition()),
                        editTextPassword.getText().toString())) {
                    dialog.dismiss();
                } else {
                    Toasty.error(RegisterActivity.this, getString(R.string.admin_password_wrong), Toast.LENGTH_SHORT, true).show();
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
}
