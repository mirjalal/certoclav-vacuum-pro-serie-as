package com.certoclav.app.adapters;

import android.content.Context;
import android.text.Editable;
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
import com.certoclav.app.database.Profile;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.Condition;

import java.util.List;


/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 */
public class ConditionAdapter extends ArrayAdapter<Condition> {
    private final Context mContext;

    static class ViewHolder {
        protected EditText editTextEmail;
        protected EditText editTextSms;
        protected CheckBox cbEmail;
        protected CheckBox cbSms;

    }

    /**
     * Constructor
     *
     * @param context context of calling activity
     * @param values  {@link List}<{@link Profile}> containing the data to populate the list
     */
    public ConditionAdapter(Context context, List<Condition> values) {

        super(context, R.layout.settings_condition_element, values);
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
        final Condition wed = getItem(position);
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.settings_condition_element, parent, false);
            final ViewHolder viewHolder = new ViewHolder();

            viewHolder.cbSms = (CheckBox) convertView.findViewById(R.id.condition_element_cb_sms);
            viewHolder.cbSms.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked == false) {
                        viewHolder.editTextSms.setEnabled(false);
                        viewHolder.editTextSms.setText("");
                    } else {
                        viewHolder.editTextSms.setEnabled(true);
                    }
                    wed.setEnabledSms(isChecked);
                }
            });


            viewHolder.cbEmail = (CheckBox) convertView.findViewById(R.id.condition_element_cb_mail);
            viewHolder.cbEmail.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked == false) {
                        viewHolder.editTextEmail.setEnabled(false);
                        viewHolder.editTextEmail.setText("");
                    } else {
                        viewHolder.editTextEmail.setEnabled(true);
                    }
                    wed.setEnabledEmail(isChecked);
                }
            });


            viewHolder.editTextEmail = (EditText) convertView.findViewById(R.id.condition_element_edit_mail);
            viewHolder.editTextEmail.setText(getItem(position).getEmailAddress());
            viewHolder.editTextEmail.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub
                    wed.setEmail(s.toString());
                }
            });

            viewHolder.editTextSms = (EditText) convertView.findViewById(R.id.condition_element_edit_sms);
            viewHolder.editTextSms.setText(getItem(position).getSMSNumber());
            viewHolder.editTextSms.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    getItem(position).setSMS(s.toString());

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub

                }
            });


            convertView.setTag(viewHolder);
            viewHolder.editTextEmail.setTag(wed);
            viewHolder.editTextSms.setTag(wed);
        } else {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.editTextEmail.setTag(wed);
            holder.editTextSms.setTag(wed);
        }


        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (!holder.editTextEmail.getText().toString().isEmpty()) {
            holder.cbEmail.setChecked(true);
        }


        if (!holder.editTextSms.getText().toString().isEmpty()) {
            holder.cbSms.setChecked(true);
        }

        TextView textTitle = (TextView) convertView.findViewById(R.id.condition_element_text_title);
        textTitle.setText(getItem(position).getIfDescription());


        if (CloudUser.getInstance().isPremiumAccount() == false) {
            holder.cbSms.setEnabled(false);
            holder.editTextSms.setEnabled(false);
        }


        return convertView;
    }


}