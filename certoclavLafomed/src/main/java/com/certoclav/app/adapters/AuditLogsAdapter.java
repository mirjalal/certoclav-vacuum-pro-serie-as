package com.certoclav.app.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.database.AuditLog;
import com.certoclav.app.database.Profile;

import java.text.SimpleDateFormat;
import java.util.List;


/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 */
public class AuditLogsAdapter extends ArrayAdapter<AuditLog> {
    private final Context mContext;
    private SimpleDateFormat format;

    static class ViewHolder {
        protected TextView textViewAuditLog;
        protected TextView textViewAuditLogDate;
        protected TextView textViewAuditLogComment;

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
        format = new SimpleDateFormat("MMM dd, yyyy  HH:mm");
    }


    /**
     * Gets a View that displays the data at the specified position in the data
     * set.The View is inflated it from profile_list_row XML layout file
     *
     * @see Adapter#getView(int, View, ViewGroup)
     */
    int objectId;
    int screenId;
    int eventId;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final AuditLog log = getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.audit_logs_element, parent, false);
            holder = new ViewHolder();
            holder.textViewAuditLog = convertView.findViewById(R.id.textViewAuditLog);
            holder.textViewAuditLogDate = convertView.findViewById(R.id.textViewAuditLogDate);
            holder.textViewAuditLogComment = convertView.findViewById(R.id.textViewAuditLogComment);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String username;
        if (log.getUser() != null)
            username = log.getUser().getEmail();
        else
            username = log.getEmail();

        objectId = log.getObjectId();
        screenId = log.getScreenId();
        eventId = log.getEventId();

        holder.textViewAuditLog.setText(Html.fromHtml(mContext.getString(eventId,
                username, objectId != -1 ? mContext.getString(objectId) : "", log.getValue(),
                (screenId != -1 ? mContext.getString(screenId) : -1))));

        holder.textViewAuditLogDate.setText(format.format(log.getDate()));
        holder.textViewAuditLogComment.setText(log.getComment());
        holder.textViewAuditLogComment.setVisibility(log.getComment().isEmpty() ? View.GONE : View.VISIBLE);


        return convertView;
    }


}