package com.certoclav.app.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.adapters.UserAdapter.OnClickButtonListener;
import com.certoclav.app.button.QuickActionItem;
import com.certoclav.app.database.Profile;


/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 */
public class ScanAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private QuickActionItem quickActionItemDelete;

    public interface OnDeleteListener {
        public void onDelete();
    }

    private OnDeleteListener listener = null;

    /**
     * Constructor
     *
     * @param context context of calling activity
     * @param values  {@link List}<{@link Profile}> containing the data to populate the list
     */
    public ScanAdapter(Context context, List<String> values) {

        super(context, R.layout.menu_scan_element, values);
        this.mContext = context;

    }

    /**
     * Gets a View that displays the data at the specified position in the data
     * set.The View is inflated it from profile_list_row XML layout file
     *
     * @see Adapter#getView(int, View, ViewGroup)
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.menu_scan_element, parent, false);
            convertView.setClickable(false);
            convertView.setFocusable(false);
            convertView.setFocusableInTouchMode(false);
            parent.setClickable(false);
            parent.setFocusable(false);
            parent.setFocusableInTouchMode(false);

        }

        convertView.findViewById(R.id.buttonDelete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(getItem(position));
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onDelete();
                }
            }
        });

        //set preview image
//		GenerateImageTask generateImageTask = new GenerateImageTask();
//		generateImageTask.setImageViewAndPath((ImageView)convertView.findViewById(R.id.information_video_element_image), getItem(position).getPath());
        //generateImageTask.execute();

        //set description text
        TextView textDescription = (TextView) convertView.findViewById(R.id.scan_element_text);
        textDescription.setText(getItem(position));


        return convertView;
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.listener = listener;

    }


}