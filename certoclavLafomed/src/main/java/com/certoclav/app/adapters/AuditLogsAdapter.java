package com.certoclav.app.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.database.AuditLog;
import com.certoclav.app.database.Profile;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.Condition;

import java.util.List;


/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 */
public class AuditLogsAdapter extends ArrayAdapter<AuditLog> {
    private final Context mContext;

    static class ViewHolder {
        protected TextView textViewAuditLog;

    }

    /**
     * Constructor
     *
     * @param context context of calling activity
     * @param values  {@link List}<{@link Profile}> containing the data to populate the list
     */
    public AuditLogsAdapter(Context context, List<AuditLog> values) {

        super(context, R.layout.audit_logs_element, values);
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
        final AuditLog log = getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.settings_condition_element, parent, false);
            holder = new ViewHolder();
            holder.textViewAuditLog = convertView.findViewById(R.id.textViewAuditLog);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String username = log.getUser().getEmail();

        if (log.getUser().getFirstName() != null && log.getUser().getFirstName().length() > 0) {
            username = log.getUser().getFirstName();
            if (log.getUser().getLastName() != null)
                username += " " + log.getUser().getLastName();
        }

        holder.textViewAuditLog.setText(Html.fromHtml(mContext.getString(R.string.textAuditLog,
                username, log.getScreenName(), log.getEventName(), log.getObjectName())));


        return convertView;
    }


}