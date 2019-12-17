package com.certoclav.app.menu;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.database.Profile;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveMonitor;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.CertoclavNavigationbar;
import com.certoclav.app.service.ReadAndParseSerialService;
import com.certoclav.app.settings.SettingsActivity;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.app.util.Helper;
import com.certoclav.app.util.MyCallbackAdminAprove;
import com.certoclav.library.certocloud.GetConditionsService;
import com.certoclav.library.certocloud.PostSoftwareUpdateService;
import com.certoclav.library.view.ControlPagerAdapter;
import com.certoclav.library.view.CustomViewPager;
import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.fabric.sdk.android.Fabric;

public class MenuMain extends CertoclavSuperActivity implements NavigationbarListener {

    public static final int REQUEST_PROGRAM_EDIT = 1;
    public static int INDEX_INFORMATION = 0;
    public static int INDEX_STERILISATION = 1;
    public static int INDEX_PROTOCOLS = 2;

    private CertoclavNavigationbar navigationbar;
    private ArrayList<Fragment> fragmentList;
    private AutoclaveModelManager modelManager;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private ControlPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private CustomViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("MenuMain", "onCreate");
        setContentView(R.layout.menu_activity);
        Fabric.with(this, new Crashlytics());
        AutoclaveMonitor.getInstance();
        Autoclave.getInstance().setState(AutoclaveState.NOT_RUNNING);
        modelManager = AutoclaveModelManager.getInstance();
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier("12345");
        Crashlytics.setUserEmail("user@fabric.io");
        Crashlytics.setUserName("Test User");

        fragmentList = new ArrayList<Fragment>(); //liste von Fragmenten f?r den ControlPagerAdapter
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new ControlPagerAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager = (CustomViewPager) findViewById(R.id.pager);

        try {
            navigationbar = new CertoclavNavigationbar(this);
        } catch (Exception e) {
            finish();
        }
        try {
            navigationbar.showNavigationBar();
            navigationbar.setTabSterilisationEnabled();
        } catch (Exception e) {
            e.printStackTrace();
        }


        fragmentList.add(INDEX_INFORMATION, new InformationFragment());
        fragmentList.add(INDEX_STERILISATION, new SterilisationFragment());
        fragmentList.add(INDEX_PROTOCOLS, new ProtocolsFragment());


        // Set up the ViewPager with the sections adapter.

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPagingEnabled(true);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager
                .setOnPageChangeListener(new CustomViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        if (position == INDEX_INFORMATION) navigationbar.setTabInformationEnabled();
                        if (position == INDEX_STERILISATION)
                            navigationbar.setTabSterilisationEnabled();
                        if (position == INDEX_PROTOCOLS) navigationbar.setTabProtocolsEnabled();
                        Log.e("ProgramMenuActivity", "Page changed");
                    }
                });

        //mViewPager.setCurrentItem(tab.getPosition());

        mViewPager.setCurrentItem(INDEX_STERILISATION);
        navigationbar.setTabSterilisationEnabled();

        Helper.getInstance().getPrograms(this);
    }


    public void setCurrentPagerItem(int item) {
        mViewPager.setCurrentItem(item);

    }


    @Override
    protected void onPause() {
        Log.e("MenuMain", "remove navigationbarlistener");
        navigationbar.removeNavigationbarListener(MenuMain.this);
        Autoclave.getInstance().removeOnSensorDataListener(this);
        super.onPause();
    }


    @Override
    protected void onResume() {

        Autoclave.getInstance().setOnSensorDataListener(this);

        Intent intent4 = new Intent(this, GetConditionsService.class);
        startService(intent4);

        Log.e("MenuMain", "set navigationbarlistener");
        navigationbar.setNavigationbarListener(MenuMain.this);
        if(modelManager.getModel() != null)
            ReadAndParseSerialService.getInstance().setParameter(93, new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime()));

        // update software version everytime u load main page
        if (!AppConstants.isIoSimulated && Autoclave.getInstance().getController() != null) {
            if (Autoclave.getInstance().getController().getSavetyKey() != null) {
                String deviceKey = Autoclave.getInstance().getController().getSavetyKey();
                String softVersion = "";
                //Software Version
                PackageInfo pInfo;
                try {
                    pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    softVersion = pInfo.versionName + " (" + pInfo.versionCode + ")";
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                PostSoftwareUpdateService service = new PostSoftwareUpdateService();
                Log.e("SOFT VERSION", softVersion);
                Log.e("DEVICE", deviceKey);
                service.postDeviceData(deviceKey, softVersion);
            }
        }

        super.onResume();
    }


    @Override
    protected void onDestroy() {
        Log.e("MenuMain", "onDestroy");
        Log.e("MenuMain", "fragmentList.clear()");
        fragmentList.clear();
        mSectionsPagerAdapter = null;
        mViewPager = null;
        Log.e("MenuMain", "size of fragmentlist " + fragmentList.size());
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        Log.e("CDA", "onBackPressed Called");
        onClickNavigationbarButton(CertoclavNavigationbar.BUTTON_BACK);
    }


    @Override
    public void onClickNavigationbarButton(int buttonId) {

        switch (buttonId) {
            case CertoclavNavigationbar.BUTTON_BACK:
                try {
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MenuMain.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(getString(R.string.logout))
                            .setContentText(getString(R.string.do_you_really_want_to_logout))
                            .setConfirmText(getString(R.string.yes))
                            .setCancelText(getString(R.string.cancel))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    Autoclave.getInstance().setState(AutoclaveState.LOCKED);
                                    AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getUser(),
                                            AuditLogger.SCEEN_EMPTY, AuditLogger.ACTION_LOGOUT,
                                            AuditLogger.OBJECT_EMPTY, null, false);
                                    Intent intent = new Intent(MenuMain.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
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

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case CertoclavNavigationbar.BUTTON_EDIT:

                break;

            case CertoclavNavigationbar.BUTTON_ADD:
                if (!Autoclave.getInstance().getUser().isAdmin()) {
                    askForAdminPassword();
                } else {
                    askToCreateAProgram();
                }
                break;

            case CertoclavNavigationbar.BUTTON_SETTINGS:
                if (Helper.getInstance().isSettingsLocked(MenuMain.this)) {
                    break;
                }
                Intent intent2 = new Intent(MenuMain.this, SettingsActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent2);
                break;

            case CertoclavNavigationbar.TAB_INFORMATION:
                mViewPager.setCurrentItem(INDEX_INFORMATION);
                navigationbar.setTabInformationEnabled();
                break;
            case CertoclavNavigationbar.TAB_STERILISATION:
                mViewPager.setCurrentItem(INDEX_STERILISATION);
                navigationbar.setTabSterilisationEnabled();
                break;
            case CertoclavNavigationbar.TAB_PROTOCOLS:
                mViewPager.setCurrentItem(INDEX_PROTOCOLS);
                navigationbar.setTabProtocolsEnabled();
                break;

        }


    }

    private void askToCreateAProgram() {
        List<Profile> profiles = Autoclave.getInstance().getProfilesFromAutoclave();
        if (profiles != null) {
            if (profiles.size() >= AppConstants.MAX_PROGRAM_COUNT) {
                Toast.makeText(MenuMain.this, getString(R.string.you_can_add_maximum_programs, AppConstants.MAX_PROGRAM_COUNT), Toast.LENGTH_LONG).show();
                return;
            }
        }
        try {

            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MenuMain.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.new_program))
                    .setContentText(getString(R.string.do_you_really_want_to_add_new_program))
                    .setConfirmText(getString(R.string.yes))
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismissWithAnimation();
                            Intent intent = new Intent(MenuMain.this, EditProgramActivity.class);
                            intent.putExtra(AppConstants.INTENT_EXTRA_PROFILE_ID, Autoclave.getInstance().getUnusedProfileIndex());
                            intent.putExtra(AppConstants.INTENT_EXTRA_NEW_PROFILE, true);
                            startActivityForResult(intent, REQUEST_PROGRAM_EDIT);
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

        } catch (Exception e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PROGRAM_EDIT && resultCode == RESULT_OK) {
            Helper.getInstance().getPrograms(this);
        }
    }

    private void askForAdminPassword() {
        Helper.getInstance().askForAdminPassword(this, REQUEST_PROGRAM_EDIT, new MyCallbackAdminAprove() {
            @Override
            public void onResponse(int requestId, int responseId) {
                if (responseId == APPROVED) {
                    //Hide keyboard when confirm has been clicked
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
                            .getWindowToken(), 0);
                    AuditLogger.getInstance().addAuditLog(Autoclave.getInstance().getSelectedAdminUser(),
                            AuditLogger.SCEEN_EMPTY,
                            AuditLogger.ACTION_ADMIN_APPROVED_EDIT_DELETE_PROGRAM,
                            AuditLogger.OBJECT_EMPTY,
                            Autoclave.getInstance().getUser().getEmail_user_id(),
                            true);
                    askToCreateAProgram();
                }
            }
        });

    }
}
