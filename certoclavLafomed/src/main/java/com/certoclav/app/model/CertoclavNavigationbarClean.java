package com.certoclav.app.model;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.button.QuickActionItem;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.settings.SettingsActivity;

import java.util.ArrayList;

public class CertoclavNavigationbarClean implements SensorDataListener {


    public static final int BUTTON_SETTINGS = 1;
    public static final int BUTTON_REFRESH = 2;
    public static final int BUTTON_SAVE = 3;
    public static final int BUTTON_ADD = 4;
    public static final int BUTTON_EDIT = 5;
    public static final int BUTTON_BACK = 6;


    private Context mContext;
    private Activity mActivity;
    private LinearLayout linActionContainer;
    private QuickActionItem actionItemSettings;
    private QuickActionItem actionItemAdd;
    private QuickActionItem actionItemRefresh;
    private QuickActionItem actionItemSave;
    private QuickActionItem actionItemBack;
    private TextView headText;
    private TextView textDate;


    ArrayList<NavigationbarListener> navigationbarListeners = new ArrayList<NavigationbarListener>();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public CertoclavNavigationbarClean(Activity activity) {
        mActivity = activity;
        mContext = (Context) mActivity;
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        textDate = (TextView) mActivity.findViewById(R.id.navigationbar_clean_datetext);

        Autoclave.getInstance().setOnSensorDataListener(this);
        linActionContainer = (LinearLayout) mActivity.findViewById(R.id.navigationbarclear_container_quickactionitem_settings);

        actionItemRefresh = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemRefresh.setChecked(false);
        actionItemRefresh.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_menu_refresh));
        //actionItemAdd.setText("test");
        linActionContainer.addView(actionItemRefresh);
        actionItemRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(BUTTON_REFRESH);
                }
            }
        });
        actionItemRefresh.setVisibility(View.GONE);


        actionItemSave = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemSave.setChecked(false);
        actionItemSave.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_menu_save));
        //actionItemAdd.setText("test");
        linActionContainer.addView(actionItemSave);
        actionItemSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(BUTTON_SAVE);
                }
            }
        });
        actionItemSave.setVisibility(View.GONE);

        LinearLayout linActionContainerLeft = (LinearLayout) mActivity.findViewById(R.id.navigationbarclear_container_quickactionitem_left);
        actionItemBack = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemBack.setChecked(false);
        actionItemBack.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_back));
        //actionItemAdd.setText("test");
        linActionContainerLeft.addView(actionItemBack);
        actionItemBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                for (NavigationbarListener listener : navigationbarListeners) {
                    listener.onClickNavigationbarButton(BUTTON_BACK);
                }
                //default action
                if (navigationbarListeners.size() == 0) {
                    mActivity.finish();
                }
            }
        });


        actionItemAdd = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemAdd.setChecked(false);
        actionItemAdd.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_action_add_user));
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


        actionItemSettings = (QuickActionItem) mActivity.getLayoutInflater().inflate(R.layout.quickaction_item, linActionContainer, false);
        actionItemSettings.setChecked(false);
        actionItemSettings.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_action_settings));
        //actionItemAdd.setText("test");
        linActionContainer.addView(actionItemSettings);
        actionItemSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, SettingsActivity.class);
                mActivity.startActivity(intent);

            }
        });
        actionItemSettings.setVisibility(View.GONE);


    }

    public void setHeadText(String text) {
        ((TextView) mActivity.findViewById(R.id.navigationbar_clean_headtext)).setText(text);
    }


    public void setSettingsVisible() {
        actionItemSettings.setVisibility(View.VISIBLE);
    }


    public void setRefreshVisible() {
        actionItemRefresh.setVisibility(View.VISIBLE);
    }

    public void setNavigationbarListener(NavigationbarListener listener) {
        this.navigationbarListeners.add(listener);
    }

    public void removeNavigationbarListener(NavigationbarListener listener) {
        this.navigationbarListeners.remove(listener);


    }


    public void setRefreshUnvisible() {
        // TODO Auto-generated method stub
        actionItemRefresh.setVisibility(View.GONE);
    }


    public void setAddVisible(boolean isVisible) {
        if (isVisible) {
            actionItemAdd.setVisibility(View.VISIBLE);
        } else {
            actionItemAdd.setVisibility(View.INVISIBLE);
        }
    }


    public void setSaveVisible() {
        actionItemSave.setVisibility(View.VISIBLE);

    }


    public void hideButtonBack() {
        actionItemBack.setVisibility(View.INVISIBLE);

    }

    public void showButtonBack() {
        actionItemBack.setVisibility(View.VISIBLE);

    }

    @Override
    public void onSensorDataChange(AutoclaveData data) {
        try {
            textDate.setText(Autoclave.getInstance().getTime() + "\n" + Autoclave.getInstance().getDate());
        } catch (Exception e) {

        }

    }


}