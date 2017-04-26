package com.certoclav.app.model;

/**
 * Created by musaq on 4/25/2017.
 */

public class SettingItem {
    private String text;
    private int icon;
    private int iconSelected;

    public SettingItem(String text, int icon, int iconSelected) {
        this.text = text;
        this.icon = icon;
        this.iconSelected = iconSelected;
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
}
