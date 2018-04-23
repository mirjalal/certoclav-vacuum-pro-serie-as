package com.certoclav.app.model;


import android.support.v4.app.Fragment;

/**
 * Created by musaq on 4/25/2017.
 */

public class SettingItem {
    private String text;
    private int icon;
    private int iconSelected;
    private Fragment fragment;

    public SettingItem(String text, int icon, int iconSelected, Fragment fragment) {
        this.text = text;
        this.icon = icon;
        this.iconSelected = iconSelected;
        this.fragment = fragment;
    }

    public int getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public int getIconSelected() {
        return iconSelected;
    }

    public Fragment getFragment() {
        return fragment;
    }
}
