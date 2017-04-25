package com.certoclav.app.model;

/**
 * Created by musaq on 4/25/2017.
 */

public class SettingItem {
    private String text;
    private int icon;
    public SettingItem(String text, int icon){
        this.text = text;
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }
}
