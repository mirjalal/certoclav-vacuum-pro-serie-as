package com.certoclav.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
import com.certoclav.app.menu.ScanActivity;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.monitor.MonitorActivity;
import com.certoclav.app.sterilisationassistant.AssistantActivity;
import com.certoclav.library.application.ApplicationController;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 */
public class ProgramAdapter extends ArrayAdapter<Profile> {
    private final Context mContext;
    private final List<Profile> mValues;

    private DatabaseService databaseService;


    /**
     * Constructor
     *
     * @param context context of calling activity
     * @param values  {@link List}<{@link Profile}> containing the data to populate the list
     */
    public ProgramAdapter(Context context, List<Profile> values) {

        super(context, R.layout.menu_fragment_sterilisation_element, values);
        this.mContext = context;
        this.mValues = values;
        databaseService = new DatabaseService(mContext);


    }

    /**
     * Gets a View that displays the data at the specified position in the data
     * set.The View is inflated it from profile_list_row XML layout file
     *
     * @see Adapter#getView(int, View, ViewGroup)
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.menu_fragment_sterilisation_element, parent, false);
            viewHolder = new ViewHolder((TextView) convertView.findViewById(R.id.sterilisation_element_firstline),
                    (TextView) convertView.findViewById(R.id.sterilisation_element_text_description),
                    (TextView) convertView.findViewById(R.id.sterilisation_element_text_start),
                    (CardView) convertView.findViewById(R.id.card_view));
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.firstLine.setText(getItem(position).getName());
        if (AppConstants.IS_CERTOASSISTANT) {
            viewHolder.textButton.setText("Show video assistant");
        }


        if (getItem(position).getIndex() == 7) { //user defined profile
            getItem(position).setDescription("Vacuum times: " + Autoclave.getInstance().getUserDefinedProgram().getVacuumTimes() + "\n" +
                    "Sterilization temp.: " + Autoclave.getInstance().getUserDefinedProgram().getSterilisationTemperature() + " ?C\n" +
                    "Sterilization time: " + Autoclave.getInstance().getUserDefinedProgram().getSterilisationTime() + " min\n" +
                    "Drying time: " + Autoclave.getInstance().getUserDefinedProgram().getDryTime() + " min");
        }
        viewHolder.textDuration.setText(getItem(position).getDescription());

        viewHolder.item = getItem(position);
        viewHolder.cardView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("ProgramAdapter", "onclick");


                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(mContext.getString(R.string.start_program))
                        .setContentText(mContext.getString(R.string.do_you_really_want_to_start) + " " + getItem(position).getName() + "?")
                        .setConfirmText(mContext.getString(R.string.yes))
                        .setCancelText(mContext.getString(R.string.cancel))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();

                                Autoclave.getInstance().setProfile(getItem(position));
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext());
                                Boolean defaultvalue = mContext.getResources().getBoolean(R.bool.switch_step_by_step_default);

                                if (prefs.getBoolean(AppConstants.PREFERENCE_KEY_SCAN_ITEM_ENABLED, false)) {
                                    Intent intent = new Intent(mContext, ScanActivity.class);
                                    mContext.startActivity(intent);
                                } else {

                                    if (prefs.getBoolean(AppConstants.PREFERENCE_KEY_STEP_BY_STEP, defaultvalue)) {
                                        Intent intent = new Intent(ApplicationController.getContext(), AssistantActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        ApplicationController.getContext().startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(ApplicationController.getContext(), MonitorActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        ApplicationController.getContext().startActivity(intent);

                                    }

                                }
                            }
                        }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        }).setCustomImage(R.drawable.ic_network_connection);
                sweetAlertDialog.setCanceledOnTouchOutside(true);
                sweetAlertDialog.setCancelable(true);
                if (AppConstants.IS_CERTOASSISTANT) {
                    sweetAlertDialog.setTitleText(mContext.getString(R.string.start_video_assistant));
                }

                if (AppConstants.IS_CERTOASSISTANT) {
                    sweetAlertDialog.setContentText(mContext.getString(R.string.do_you_really_want_to_start) + " " + mContext.getString(R.string.the_video_assistant) + "?");
                }
                sweetAlertDialog.show();


            }
        });


        return convertView;
    }


    static class ViewHolder {
        final TextView firstLine;
        final TextView textDuration;
        final TextView textButton;
        final CardView cardView;
        Profile item;

        ViewHolder(TextView firstLine, TextView textDuration, TextView textButton, CardView cardView) {
            this.firstLine = firstLine;
            this.textDuration = textDuration;
            this.textButton = textButton;
            this.cardView = cardView;
        }


    }

}