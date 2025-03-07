package com.certoclav.app.menu;

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.button.EditTextItem;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.PostUserLoginService;
import com.certoclav.library.certocloud.PostUserLoginService.PutUserLoginTaskFinishedListener;
import com.certoclav.library.certocloud.PostUtil;
import com.certoclav.library.util.Response;

import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddCloudAccountActivity extends Activity {


    private LinearLayout linEditTextItemContainer;
    private Button buttonRegister;

    private EditTextItem editEmailItem;
    private EditTextItem editPasswordItem;
    private ProgressBar progressBar;
    private SweetAlertDialog pDialog;
    private DatabaseService databaseService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("LoginActivity", "onCreate");
        setContentView(R.layout.login_register);
        CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.add_existing_certocloud_account));
        progressBar = (ProgressBar) findViewById(R.id.register_progressbar);


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
                // 

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        linEditTextItemContainer.addView(editEmailItem);


        editPasswordItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editPasswordItem.setHint(getString(R.string.password));
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
                // 

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


        linEditTextItemContainer.addView(editPasswordItem);


        databaseService = DatabaseService.getInstance();
        buttonRegister = (Button) findViewById(R.id.register_button_ok);
        buttonRegister.setText(getString(R.string.add_account));

        buttonRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                for (User user : databaseService.getUsers()) {
                    if (editEmailItem.getText().toLowerCase().equals(user.getEmail().toLowerCase())) {
                        Toast.makeText(AddCloudAccountActivity.this, getString(R.string.email_already_exists), Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (!editEmailItem.hasValidString()) {
                    Toast.makeText(AddCloudAccountActivity.this, getString(R.string.please_enter_a_valid_email_address), Toast.LENGTH_LONG).show();
                    return;
                }

                PostUserLoginService userLoginSerice = new PostUserLoginService();
                userLoginSerice.setOnTaskFinishedListener(new PutUserLoginTaskFinishedListener() {

                    @Override
                    public void onTaskFinished(Response response) {
                        new AddAccountTask().execute((response != null ?
                                response.getStatus() :
                                PostUtil.RETURN_ERROR) + "", editEmailItem.getText(), editPasswordItem.getText());
                    }
                });

                userLoginSerice.loginUser(editEmailItem.getText(), editPasswordItem.getText(), Autoclave.getInstance().getDevice());
                showDialog();

            }
        });


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

    private class AddAccountTask extends AsyncTask<String, Void, Boolean> {

        private User newUser;

        @Override
        protected void onPreExecute() {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.setTitleText(getString(R.string.adding));
            }
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String[] params) {

            if (Integer.valueOf(params[0]) == PostUtil.RETURN_OK) {
                Boolean isLocal = false;
                newUser = new User(
                        "",
                        "",
                        "",
                        params[1]
                        ,
                        "",
                        "",
                        "",
                        "",
                        "",
                        BCrypt.hashpw(params[2], BCrypt.gensalt()),
                        new Date(),
                        false,
                        isLocal);
                return true;
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean result) {

            if (result && newUser != null)
                databaseService.insertUser(newUser);
            if (pDialog == null) return;
            pDialog.setTitleText(getString(result ? R.string.account_added : R.string.username_or_password_is_wrong));
            pDialog.changeAlertType(result ? SweetAlertDialog.SUCCESS_TYPE : SweetAlertDialog.ERROR_TYPE);
            pDialog.setConfirmText(getString(android.R.string.ok));
            pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    hideDialog();
                    if (result)
                        AddCloudAccountActivity.this.finish();
                }
            });

            super.onPostExecute(result);
        }
    }

    private void showDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setCancelable(false);
        pDialog.setTitleText(getString(R.string.checking));
        pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismissWithAnimation();
    }


}
