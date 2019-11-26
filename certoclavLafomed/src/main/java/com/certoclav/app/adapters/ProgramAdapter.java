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
import android.widget.ImageView;
import android.widget.TextView;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.database.Profile;
import com.certoclav.app.menu.ScanActivity;
import com.certoclav.app.menu.SterilisationFragment;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.monitor.MonitorActivity;
import com.certoclav.app.sterilisationassistant.AssistantActivity;
import com.certoclav.app.util.AutoclaveModelManager;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.certocloud.CloudUser;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * The ProfileAdapter class provides access to the profile data items. <br>
 * ProfileAdapter is also responsible for making a view for each item in the
 * data set.
 */
public class ProgramAdapter extends ArrayAdapter<Profile> {
    private final Context mContext;
    private final SterilisationFragment fragment;
    private final boolean isLocked;


    private DatabaseService databaseService;


    /**
     * Constructor
     *
     * @param context context of calling activity
     * @param values  {@link List}<{@link Profile}> containing the data to populate the list
     */
    public ProgramAdapter(Context context, List<Profile> values, SterilisationFragment fragment) {

        super(context, R.layout.menu_fragment_sterilisation_element, values);
        this.mContext = context;
        databaseService = DatabaseService.getInstance();
        this.fragment = fragment;

        if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_add_program),
                        ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_add_program))) {
            isLocked = true;
        } else {
            isLocked = false;
        }

    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(Autoclave.getInstance().getProfilesFromAutoclave(), new Comparator<Profile>() {
            @Override
            public int compare(Profile o1, Profile o2) {
                return o1.getRecentUsedDate() > o2.getRecentUsedDate() ? -1 : 1;
            }
        });
        super.notifyDataSetChanged();
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
                    (CardView) convertView.findViewById(R.id.card_view),
                    (ImageView) convertView.findViewById(R.id.buttonMenu),
                    (ImageView) convertView.findViewById(R.id.sterilization_element_image_cloud),
                    fragment,
                    this);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        final Profile profileItem = getItem(position);

        viewHolder.firstLine.setText(profileItem.getName());
        viewHolder.textDuration.setText(profileItem.getDescription(true));

        if (CloudUser.getInstance().isLoggedIn() == false) {
            viewHolder.imageViewCloud.setImageResource(R.drawable.ic_cloud_no_user);
        } else {
            viewHolder.imageViewCloud.setImageResource(profileItem.isLocal() ? R.drawable.ic_cloud_no_user : R.drawable.ic_cloud_user);
        }
        viewHolder.imageViewMenu.setVisibility(isLocked || CloudUser.getInstance().isSuperAdmin() ? View.GONE : View.VISIBLE);

        if (!profileItem.isEditable()) {
            viewHolder.imageViewMenu.setVisibility(View.GONE);
        }

        viewHolder.cardView.setTag(position);
        viewHolder.imageViewMenu.setTag(position);

        viewHolder.item = profileItem;


        return convertView;
    }


    static class ViewHolder {
        final TextView firstLine;
        final TextView textDuration;
        final CardView cardView;
        final ImageView imageViewMenu;
        final ImageView imageViewCloud;
        Profile item;


        ViewHolder(TextView firstLine, TextView textDuration, CardView cardView, ImageView imageViewMenu,
                   ImageView imageViewCloud, final SterilisationFragment fragment, final ProgramAdapter adapter) {
            this.firstLine = firstLine;
            this.textDuration = textDuration;
            this.cardView = cardView;
            this.imageViewCloud = imageViewCloud;
            this.imageViewMenu = imageViewMenu;

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!adapter.isLocked && item.isEditable())
                        fragment.onLongClick(v);
                    return false;
                }
            });

            cardView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onItemClickListener((int) v.getTag());
                }
            });

            imageViewMenu.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.onLongClick(v);
                }
            });
        }

    }

    private void onItemClickListener(final int position) {
        Log.e("ProgramAdapter", "onclick");

        final Profile profileItem = getItem(position);
        if (Autoclave.getInstance().getUser().isAdmin()
                && Autoclave.getInstance().getUser().getEmail().equalsIgnoreCase("admin")) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(mContext.getString(R.string.permission_denied))
                    .setContentText(mContext.getString(R.string.admin_user_cant_start_a_program_and_info))
                    .setConfirmText(mContext.getString(R.string.ok));
            sweetAlertDialog.show();
            return;
        }

        if (Autoclave.getInstance().getProgramStep() == Autoclave.PROGRAM_STEPS.ATMOSPHERIC_PRESSURE) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(mContext.getString(R.string.atmospheric_pressure))
                    .setContentText(mContext.getString(R.string.can_not_start_program_please_open_to_read_atmospheric_pressure))
                    .setConfirmText(mContext.getString(R.string.ok));
            sweetAlertDialog.show();
            return;
        }

        if (Autoclave.getInstance().getProgramStep() == Autoclave.PROGRAM_STEPS.PRE_HEATING
                && AutoclaveModelManager.getInstance().isWarmingUpEnabled()
                && profileItem != null
                && profileItem.getIndex() != AutoclaveModelManager.getInstance().getVacuumProgramIndex()) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(mContext.getString(R.string.can_not_start_program))
                    .setContentText(mContext.getString(R.string.can_not_start_program_please_wait_warming_up_finished))
                    .setConfirmText(mContext.getString(R.string.ok));
            sweetAlertDialog.show();
            return;
        }

        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(mContext.getString(R.string.start_program))
                .setContentText(mContext.getString(R.string.do_you_really_want_to_start) + " " + profileItem.getName() + "?")
                .setConfirmText(mContext.getString(R.string.yes))
                .setCancelText(mContext.getString(R.string.cancel))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        Autoclave.getInstance().setProfile(profileItem);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ApplicationController.getContext());
                        Boolean defaultvalue = mContext.getResources().getBoolean(R.bool.switch_step_by_step_default);
                        profileItem.setRecentUsedDate(new Date().getTime());

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

}