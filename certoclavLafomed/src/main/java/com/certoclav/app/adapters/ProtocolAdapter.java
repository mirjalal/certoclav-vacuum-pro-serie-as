package com.certoclav.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.database.Profile;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.model.AutoclaveMonitor;

import java.util.List;


/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 */
public class ProtocolAdapter extends ArrayAdapter<Protocol> {
    private final Context mContext;
    private int selectedPosition = -1;
    private int currentPage;


    /**
     * Constructor
     *
     * @param context context of calling activity
     * @param values  {@link List}<{@link Profile}> containing the data to populate the list
     */
    public ProtocolAdapter(Context context, List<Protocol> values) {

        super(context, R.layout.menu_fragment_protocols_element, values);
        this.mContext = context;
    }

    /**
     * Gets a View that displays the data at the specified position in the data
     * set.The View is inflated it from profile_list_row XML layout file
     *
     * @see Adapter#getView(int, View, ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.menu_fragment_protocols_element, parent, false);
        }

        if (getSelectedPosition() == position) {
            convertView.setBackgroundResource(R.drawable.background_blue_light);
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        try {
            ImageView imageCloud = (ImageView) convertView.findViewById(R.id.protocols_element_image_cloud);
            imageCloud.setImageResource(getItem(position).isUploaded() ? R.drawable.ic_cloud_sync : R.drawable.ic_cloud_no_sync);


            TextView id = (TextView) convertView.findViewById(R.id.protocols_element_id);
            id.setText(Integer.toString(getItem(position).getZyklusNumber() - 1)); // cycleNumber - 1

            TextView startTime = (TextView) convertView.findViewById(R.id.protocols_element_startTime);
            startTime.setText(getItem(position).getFormatedStartTimeHoursMinutes());

            TextView startDate = (TextView) convertView.findViewById(R.id.protocols_element_startDate);
            startDate.setText(getItem(position).getFormatedStartDate());

            TextView programName = (TextView) convertView.findViewById(R.id.protocols_element_programName);
            programName.setText(getItem(position).getProfileName());


            if (getItem(position).getErrorCode() != 0 && getItem(position).getErrorCode() != AutoclaveMonitor.ERROR_CODE_INDICATOR_SUCCESS) {
                programName.setTextColor(Color.parseColor("#D60000")); //if error
            } else {
                programName.setTextColor(Color.parseColor("#006600"));//if no error
            }


            TextView userName = (TextView) convertView.findViewById(R.id.protocols_element_userName);
            userName.setText(getItem(position).getUserEmail());
        } catch (Exception e) {
            Log.e("ProtocolAdapter", e.toString());
        }


        return convertView;
    }

    public void setSelection(int pos) {
        this.selectedPosition = pos;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void updateProtocol(int aktPosition, Protocol protocol) {
        getItem(aktPosition).setProtocolEntry(protocol.getProtocolEntry());
        notifyDataSetChanged();
    }
}