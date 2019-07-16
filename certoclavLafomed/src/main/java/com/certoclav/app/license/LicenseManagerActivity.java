package com.certoclav.app.license;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.User;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.ErrorModel;
import com.certoclav.app.settings.SettingsActivity;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.MyCallback;
import com.certoclav.app.util.Requests;
import com.certoclav.library.certocloud.CloudUser;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

public class LicenseManagerActivity extends Activity implements View.OnClickListener, MyCallback {


    private int licenseCount;
    private static final int REQUEST_GET_COUNT = 1;
    private SweetAlertDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_manager);

        TextView textViewSettings = findViewById(R.id.textViewSettingsButton);
        TextView textViewActivate = findViewById(R.id.textViewActivateButton);
        textViewActivate.setText(Html.fromHtml(getString(R.string.welcome_activate)));
        textViewSettings.setText(Html.fromHtml(getString(R.string.welcome_settings)));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonActivate:
                showActivatePage();
                break;
            case R.id.buttonSettings:
                DatabaseService databaseService = DatabaseService.getInstance();
                User user = databaseService.getUserByUsername("Admin");
                if (user != null) {
                    Autoclave.getInstance().setUser(user);
                    Intent intent = new Intent(this, SettingsActivity.class);
                    intent.putExtra("isAdmin", true);
                    CloudUser.getInstance().setSuperAdmin(true);
                    CloudUser.getInstance().setLoggedIn(true);
                    startActivity(intent);
                } else {
                    Toasty.warning(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT, true).show();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.buttonActivate).setEnabled(
                !(AutoclaveModelManager.getInstance().getModel() == null
                        || AutoclaveModelManager.getInstance().getPCBSerialNumber() == null
                        || AutoclaveModelManager.getInstance().getSerialNumber() == null));

        Requests.getInstance().getLicenseCount(this, REQUEST_GET_COUNT);
    }

    private void askForActivate() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.activate))
                .setContentText(getString(R.string.do_you_really_want_to) + " "
                        + getString(R.string.activate))
                .setConfirmText(getString(R.string.yes))
                .setCancelText(getString(R.string.cancel))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        showActivatePage();
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

    private void showActivatePage() {
        final SweetAlertDialog dialog = new SweetAlertDialog(this, R.layout.dialog_activate_autoclave, SweetAlertDialog.WARNING_TYPE);
        dialog.setContentView(R.layout.dialog_activate_autoclave);
        dialog.setTitle(R.string.register_new_user);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        final EditText editTextPassword = dialog.findViewById(R.id.editTextDesc);

        final Button buttonActivate = dialog
                .findViewById(R.id.dialogButtonActivate);

        TextView textViewModel = dialog.findViewById(R.id.textViewAutoclaveModel);
        TextView textViewSerial = dialog.findViewById(R.id.textViewAutoclaveSerialNumber);
        TextView textViewPcbSerial = dialog.findViewById(R.id.textViewAutoclavePCBSerial);
        TextView textViewLicenseCount = dialog.findViewById(R.id.textViewLicenseCount);

        textViewModel.setText(AutoclaveModelManager.getInstance().getModel());
        textViewSerial.setText(AutoclaveModelManager.getInstance().getSerialNumber());
        textViewPcbSerial.setText(AutoclaveModelManager.getInstance().getPCBSerialNumber());
        textViewLicenseCount.setText(String.valueOf(licenseCount));


        CompoundButton.OnCheckedChangeListener checkBoxListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                buttonActivate.setEnabled(((CheckBox) dialog.findViewById(R.id.checkbox1)).isChecked() &&
                        ((CheckBox) dialog.findViewById(R.id.checkbox2)).isChecked() &&
                        ((CheckBox) dialog.findViewById(R.id.checkbox3)).isChecked());
            }
        };

        ((CheckBox) dialog.findViewById(R.id.checkbox1)).setOnCheckedChangeListener(checkBoxListener);
        ((CheckBox) dialog.findViewById(R.id.checkbox2)).setOnCheckedChangeListener(checkBoxListener);
        ((CheckBox) dialog.findViewById(R.id.checkbox3)).setOnCheckedChangeListener(checkBoxListener);

        Button buttonCancel = dialog
                .findViewById(R.id.dialogButtonCancel);
        buttonCancel.setText(buttonCancel.getText().toString().toUpperCase());
        buttonCancel.setTextSize(20);
        buttonActivate.setTextSize(20);
        buttonActivate.setText(buttonActivate.getText().toString().toUpperCase());
        buttonActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismissWithAnimation();
            }
        });

        dialog.show();
    }

    @Override
    public void onSuccess(Object response, int requestId) {

        switch (requestId) {
            case REQUEST_GET_COUNT:
                LicenseCountModel licenseCountModel = (LicenseCountModel) response;
                licenseCount = licenseCountModel.getCount();

                if (licenseCount <= 0) {
                    licenseCount = -1;
                    onError(null, REQUEST_GET_COUNT);
                } else {
                    if (progressDialog != null)
                        progressDialog.dismissWithAnimation();
                }
                break;
        }
    }

    @Override
    public void onError(ErrorModel error, int requestId) {
        switch (requestId) {
            case REQUEST_GET_COUNT:
                progressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                progressDialog.setTitleText(getString(licenseCount == -1 ? R.string.no_license_key_title : R.string.failed));
                progressDialog.setContentText(getString(licenseCount == -1 ? R.string.no_license_key : R.string.something_went_wrong_try_again));
                progressDialog.setConfirmText(getString(R.string.try_again).toUpperCase());
                progressDialog.setCancelText(getString(R.string.settings));
                progressDialog.showCancelButton(true);

                progressDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        LicenseManagerActivity.this.onClick(LicenseManagerActivity.this.findViewById(R.id.buttonSettings));
                    }

                });

                progressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        Requests.getInstance().getLicenseCount(LicenseManagerActivity.this, REQUEST_GET_COUNT);
                    }
                });
                progressDialog.show();
                break;

            default:
                Toasty.warning(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT, true).show();
        }
    }

    @Override
    public void onStart(int requestId) {
        if (progressDialog != null)
            progressDialog.dismiss();
        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        progressDialog.setTitleText(getString(R.string.loading));
        progressDialog.setContentText(null);
        progressDialog.showCancelButton(false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    public void onProgress(int current, int max) {

    }


    private void activate(){

    }
}
