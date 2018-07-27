package com.certoclav.app.model;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.certoclav.app.R;
import com.certoclav.app.button.QuickActionItem;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.util.Helper;
import com.certoclav.library.application.ApplicationController;

import java.util.ArrayList;

public class CertoclavNavigationbar {


    private final boolean isCreateProgramLocked;
    ArrayList<NavigationbarListener> navigationbarListeners = new ArrayList<NavigationbarListener>();

    private Context mContext;
    private Activity mActivity;


    private View tabInformation;
    private View tabSterilisation;
    private View tabProtocols;

    private LinearLayout linActionContainer;
    private QuickActionItem actionItemSettings;
    private QuickActionItem actionItemEdit;
    private QuickActionItem actionItemAdd;
    private QuickActionItem actionItemLogout;
    private QuickActionItem actionItemPrint;
    private QuickActionItem actionItemScan;

    public static final int BUTTON_SETTINGS = 1;
    public static final int BUTTON_REFRESH = 2;
    public static final int BUTTON_SAVE = 3;
    public static final int BUTTON_ADD = 4;
    public static final int BUTTON_EDIT = 5;
    public static final int BUTTON_BACK = 6;
    public static final int TAB_INFORMATION = 7;
    public static final int TAB_STERILISATION = 8;
    public static final int TAB_PROTOCOLS = 9;
    public static final int BUTTON_PRINT = 10;
    public static final int BUTTON_SCAN = 11;


    public void setNavigationbarListener(NavigationbarListener listener) {
        this.navigationbarListeners.add(listener);
    }

    public void removeNavigationbarListener(NavigationbarListener listener) {
        this.navigationbarListeners.remove(listener);


    }


    public CertoclavNavigationbar(Activity activity) {
        mActivity = activity;
        mContext = (Context) mActivity;


        if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_add_program),
                        ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_add_program))) {
            isCreateProgramLocked = true;
        } else {
            isCreateProgramLocked = false;
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showNavigationBar() {
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tabInformation = mActivity.findViewById(R.id.navigationbar_text_information);
        tabSterilisation = mActivity.findViewById(R.id.navigationbar_text_sterilisation);
        tabProtocols = mActivity.findViewById(R.id.navigationbar_text_protocols);

        tabSterilisation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(TAB_STERILISATION);
                }

            }
        });


        tabProtocols.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(TAB_PROTOCOLS);
                }

            }
        });


        tabInformation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(TAB_INFORMATION);
                }

            }
        });


        linActionContainer = (LinearLayout) mActivity.findViewById(R.id.navigationbar_container_quickactionitem_rest);

        actionItemEdit = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemEdit.setChecked(false);
        actionItemEdit.setImageDrawable(Helper.changeColorToWhite(mActivity.getResources().getDrawable(R.drawable.ic_menu_launcher_settings)));
        //actionItemAdd.setText("test");
        linActionContainer.addView(actionItemEdit);
        actionItemEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(BUTTON_EDIT);
                }
            }
        });
        actionItemEdit.setVisibility(View.GONE);

        actionItemAdd = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemAdd.setChecked(false);
        actionItemAdd.setImageDrawable(Helper.changeColorToWhite(mActivity.getResources().getDrawable(R.drawable.ic_menu_add)));
        //actionItemAdd.setText("test");
        linActionContainer.addView(actionItemAdd);
        actionItemAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(BUTTON_ADD);
                }

            }
        });
        actionItemAdd.setVisibility(View.GONE);


        actionItemPrint = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemPrint.setChecked(false);
        actionItemPrint.setImageDrawable(Helper.changeColorToWhite(mActivity.getResources().getDrawable(R.drawable.ic_menu_print)));
        //actionItemAdd.setText("test");
        linActionContainer.addView(actionItemPrint);
        actionItemPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(BUTTON_PRINT);
                }
            }
        });


        actionItemScan = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemScan.setChecked(false);
        actionItemScan.setImageDrawable(Helper.changeColorToWhite(mActivity.getResources().getDrawable(R.drawable.ic_menu_scan)));
        linActionContainer.addView(actionItemScan);
        actionItemScan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(BUTTON_SCAN);
                }
            }
        });


        linActionContainer = (LinearLayout) mActivity.findViewById(R.id.navigationbar_container_quickactionitem_settings);
        actionItemSettings = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemSettings.setChecked(false);
        actionItemSettings.setImageDrawable(Helper.changeColorToWhite(mActivity.getResources().getDrawable(R.drawable.ic_settings)));
        //actionItemAdd.setText("test");
        linActionContainer.addView(actionItemSettings);
        actionItemSettings.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(BUTTON_SETTINGS);
                }

            }
        });


        linActionContainer = (LinearLayout) mActivity.findViewById(R.id.navigationbar_container_quickaction_left);
        actionItemLogout = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemLogout.setChecked(false);
        actionItemLogout.setImageDrawable(Helper.changeColorToWhite(mActivity.getResources().getDrawable(R.drawable.ic_logout)));
        //actionItemLogout.setText("Logout");
        linActionContainer.addView(actionItemLogout);
        actionItemLogout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(BUTTON_BACK);
                }

            }
        });

    }

    public void hideButtonEdit() {
        actionItemEdit.setVisibility(View.GONE);
    }

    public void showButtonEdit() {
        actionItemEdit.setVisibility(View.VISIBLE);
    }

    public void hideButtonAdd() {
        actionItemAdd.setVisibility(View.GONE);
    }

    public void showButtonAdd() {
        actionItemAdd.setVisibility(View.VISIBLE);
    }


    public void setTabInformationEnabled() {
        tabInformation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_white_tab_1));
        tabSterilisation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_tab_2_shadow_left));
        tabProtocols.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_tab_3));
        hideButtonAdd();
        hideButtonEdit();
        actionItemPrint.setVisibility(View.GONE);
        actionItemScan.setVisibility(View.GONE);

    }

    public void setTabProtocolsEnabled() {
        tabInformation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_tab_1));
        tabSterilisation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_tab_2_right_shadow));
        tabProtocols.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_white_tab_3));
        hideButtonAdd();
        hideButtonEdit();
        //	actionItemPrint.setVisibility(View.VISIBLE);
        //	actionItemScan.setVisibility(View.VISIBLE);
    }

    public void setTabSterilisationEnabled() {
        tabInformation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_tab_1_shadow_right));
        tabSterilisation.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_white_tab_2));
        tabProtocols.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.btn_grey_tab_3_shadow_left));
        //showButtonEdit();
        if (!isCreateProgramLocked)
            showButtonAdd();
        else
            hideButtonAdd();
        actionItemPrint.setVisibility(View.GONE);
        actionItemScan.setVisibility(View.GONE);
    }


}