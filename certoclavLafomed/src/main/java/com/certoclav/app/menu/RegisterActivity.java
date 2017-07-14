package com.certoclav.app.menu;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
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
import com.certoclav.library.bcrypt.BCrypt;

public class RegisterActivity extends CertoclavSuperActivity {


    private LinearLayout linEditTextItemContainer;
    private Button buttonRegister;

    private EditTextItem editEmailItem;
    private EditTextItem editPasswordItem;
    private EditTextItem editPasswordItemConfirm;
    private EditTextItem editMobile;
    private EditTextItem editFirstName;
    private EditTextItem editLastName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("LoginActivity", "onCreate");
        setContentView(R.layout.login_register);
        CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.register_new_user));


        linEditTextItemContainer = (LinearLayout) findViewById(R.id.register_container_edit_text_items);


        editEmailItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editEmailItem.setHint(getString(R.string.email));
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
                if (s.toString().isEmpty() == false)
                    editMobile.setHasValidString(true);
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


        editPasswordItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editPasswordItem.setHint(getString(R.string.new_password));
        editPasswordItem.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPasswordItem.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 3)
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
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        linEditTextItemContainer.addView(editPasswordItemConfirm);


        final DatabaseService databaseService = new DatabaseService(RegisterActivity.this);
        buttonRegister = (Button) findViewById(R.id.register_button_ok);

        buttonRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                boolean isEmailAlreadyExists = false;
                for (User user : databaseService.getUsers()) {
                    if (editEmailItem.getText().equals(user.getEmail())) {
                        isEmailAlreadyExists = true;
                        Toast.makeText(RegisterActivity.this, "Email already exists", Toast.LENGTH_LONG).show();
                    }
                }


                Log.e("RegisterActivity", "onclickRegisterButton");
                if (editPasswordItem.getVisibility() == View.VISIBLE) {
                    if (!editPasswordItem.hasValidString() || !editPasswordItemConfirm.hasValidString()) {
                        Toast.makeText(RegisterActivity.this, getString(R.string.passwords_do_not_match), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!editEmailItem.hasValidString()) {
                        Toast.makeText(RegisterActivity.this, getString(R.string.please_enter_a_valid_email_address), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                if (isEmailAlreadyExists) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
                    return;
                }


                final Boolean isLocal = true;

                if (getIntent().hasExtra(AppConstants.INTENT_EXTRA_USER_ID)) {
                    DatabaseService db = new DatabaseService(RegisterActivity.this);
                    int retval = db.deleteUser(db.getUserById(getIntent().getExtras().getInt(AppConstants.INTENT_EXTRA_USER_ID)));
                    if (retval != 1) {
                        Toast.makeText(RegisterActivity.this, "Failed to apply changes", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                buttonRegister.setEnabled(false);

                new AsyncTask<String, Boolean, Boolean>() {

                    @Override
                    protected Boolean doInBackground(String... params) {
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
                                BCrypt.hashpw(params[4], BCrypt.gensalt()),
                                new Date(),
                                false,
                                isLocal);

                        int retval = databaseService.insertUser(user);
                        if (retval == 1) {
                            return true;
                        } else
                            return false;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        buttonRegister.setEnabled(true);
                        if (result == true) {
                            Toast.makeText(getApplicationContext(), R.string.account_created, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.database_error), Toast.LENGTH_LONG).show();
                        }
                        super.onPostExecute(result);
                    }


                }.execute(editFirstName.getText(), editLastName.getText(), editEmailItem.getText(), editMobile.getText(), editPasswordItem.getText());


            }
        });


    }


    @Override
    protected void onResume() {

        if (getIntent().hasExtra(AppConstants.INTENT_EXTRA_USER_ID)) {
            try {
                DatabaseService db = new DatabaseService(this);
                User user = db.getUserById(getIntent().getExtras().getInt(AppConstants.INTENT_EXTRA_USER_ID));
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

}
