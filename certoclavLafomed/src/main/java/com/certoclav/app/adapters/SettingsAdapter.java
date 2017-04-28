package com.certoclav.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.model.SettingItem;

import java.util.ArrayList;

public class SettingsAdapter extends ArrayAdapter<SettingItem> {
    private int mSelectedPos = 0;

    public SettingsAdapter(Context context, ArrayList<SettingItem> settingItems) {
        super(context, 0, settingItems);
    }


    public void setSelectedPos(int mSelectedPos) {
        this.mSelectedPos = mSelectedPos;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        SettingItem item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_list_item_settings, parent, false);
        }
        // Lookup view for data population
        TextView textViewText = (TextView) convertView.findViewById(R.id.textViewText);
        ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.imageViewIcon);
        ImageView imageViewSelected = (ImageView) convertView.findViewById(R.id.imageViewSelecteed);
        imageViewSelected.setImageResource(mSelectedPos == position ?
                R.drawable.ic_setting_selected_item : R.drawable.ic_setting_unselected_item);

        textViewText.setTextColor(mSelectedPos == position ? Color.parseColor("#1f80ed") : Color.BLACK);

        // Populate the data into the template view using the data object
        textViewText.setText(item.getText());
        imageViewIcon.setImageResource(mSelectedPos == position ? item.getIconSelected() : item.getIcon());
        // Return the completed view to render on screen
        return convertView;
    }
}