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
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.button.EditTextItem;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.util.Helper;
import com.certoclav.library.bcrypt.BCrypt;
import com.certoclav.library.certocloud.PostUtil;

import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChangeAdminPasswordAccountActivity extends Activity {


    private LinearLayout linEditTextItemContainer;
    private Button buttonSave;

    private EditTextItem editPasswordItem;
    private SweetAlertDialog pDialog;
    private DatabaseService databaseService;
    private EditTextItem editCurPasswordItem;
    private EditTextItem editPasswordItemConfirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("LoginActivity", "onCreate");
        setContentView(R.layout.change_admin_password_layout);
        CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.change_admin_password));


        linEditTextItemContainer = (LinearLayout) findViewById(R.id.register_container_edit_text_items);

        editCurPasswordItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editCurPasswordItem.setHint(getString(R.string.cur_password));
        editCurPasswordItem.setHasValidString(false);
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

        linEditTextItemContainer.addView(editCurPasswordItem);

        editPasswordItem = (EditTextItem) getLayoutInflater().inflate(R.layout.edit_text_item, linEditTextItemContainer, false);
        editPasswordItem.setHint(getString(R.string.password));
        editPasswordItem.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPasswordItem.setHasValidString(false);
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
        editPasswordItemConfirm.setHint(getString(R.string.password_confirm));
        editPasswordItemConfirm.setEditTextInputtype(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPasswordItemConfirm.setHasValidString(false);
        editPasswordItemConfirm.addTextChangedListner(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editPasswordItemConfirm.setHasValidString(editPasswordItem.getText().equals(s.toString()));
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


        databaseService = new DatabaseService(ChangeAdminPasswordAccountActivity.this);

        buttonSave = (Button) findViewById(R.id.register_button_ok);
        buttonSave.setText(getString(R.string.save));

        buttonSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!editCurPasswordItem.hasValidString()) {
                    Toast.makeText(ChangeAdminPasswordAccountActivity.this, getString(R.string.please_enter_password), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e("RegisterActivity", "onclickRegisterButton");
                if ((!editPasswordItem.hasValidString() || !editPasswordItemConfirm.hasValidString()) ||
                        !editPasswordItem.getText().equals(editPasswordItemConfirm.getText())) {
                    Toast.makeText(ChangeAdminPasswordAccountActivity.this, getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Helper.checkAdminPassword(ChangeAdminPasswordAccountActivity.this, editCurPasswordItem.getText())) {
                    if (Helper.updateAdminPassword(ChangeAdminPasswordAccountActivity.this, editPasswordItem.getText())) {
                        Toast.makeText(ChangeAdminPasswordAccountActivity.this, getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ChangeAdminPasswordAccountActivity.this, getString(R.string.something_went_wrong_try_again), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangeAdminPasswordAccountActivity.this, getString(R.string.admin_password_wrong), Toast.LENGTH_SHORT).show();
                }

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
                User user = new User(
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
                databaseService.insertUser(user);
                return true;
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (pDialog == null) return;
            pDialog.setTitleText(getString(result ?
                    R.string.account_added :
                    R.string.linking_certocloud_account_failed));
            pDialog.changeAlertType(result ? SweetAlertDialog.SUCCESS_TYPE : SweetAlertDialog.ERROR_TYPE);
            pDialog.setConfirmText(getString(android.R.string.ok));
            pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    hideDialog();
                    if (result)
                        ChangeAdminPasswordAccountActivity.this.finish();
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
