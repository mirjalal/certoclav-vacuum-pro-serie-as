package com.certoclav.app.settings;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.adapters.ConditionAdapter;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.library.application.ApplicationController;
import com.certoclav.library.certocloud.CloudDatabase;
import com.certoclav.library.certocloud.CloudUser;
import com.certoclav.library.certocloud.Condition;
import com.certoclav.library.util.Response;

import java.util.ArrayList;


public class SettingsConditionFragment extends Fragment implements NavigationbarListener {

    private GridView gridViewCondition;
    private ConditionAdapter conditionAdapter = null;
    private ProgressBar progressBar = null;
    private TextView viewText = null;
    private Button buttonSave = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("SterilisationFragment", "oncreate");
        View rootView = inflater.inflate(R.layout.settings_conditions_fragment, container, false);
        Log.e("SterilsationFragment", "hole programGrid aus xml datei");

        buttonSave = (Button) rootView.findViewById(R.id.menu_conditions_button_save);
        buttonSave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Condition con;
                for (int i = 0; i < conditionAdapter.getCount(); i++) {
                    con = conditionAdapter.getItem(i);
                    if ((con.isEnabledEmail() && con.getEmailAddress().isEmpty()) ||
                            (con.isEnabledSms() && con.getSMSNumber().isEmpty())) {
                        Toast.makeText(getActivity(), getString(R.string.please_enter_valid_data), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                new AsyncTask<Void, Void, Response>() {

                    @Override
                    protected void onPreExecute() {
                        buttonSave.setEnabled(false);
                        super.onPreExecute();
                    }

                    @Override
                    protected void onPostExecute(Response response) {
                        buttonSave.setEnabled(true);
                        if (response != null && response.isOK()) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.changes_successfully_saved), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.changes_could_not_be_saved), Toast.LENGTH_LONG).show();
                        }
                        super.onPostExecute(response);
                    }

                    @Override
                    protected Response doInBackground(Void... params) {

                        try {
                            ArrayList<Condition> conditionList = new ArrayList<Condition>();
                            for (int i = 0; i < conditionAdapter.getCount(); i++) {
                                conditionList.add(conditionAdapter.getItem(i));
                            }
                            return CloudDatabase.getInstance().updateConditions(conditionList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                }.execute();

            }
        });
        gridViewCondition = (GridView) rootView.findViewById(R.id.video_gridlayout);
        conditionAdapter = new ConditionAdapter(getActivity(), new ArrayList<Condition>());
        gridViewCondition.setAdapter(conditionAdapter);
        progressBar = (ProgressBar) rootView.findViewById(R.id.menu_conditions_progressbar);
        progressBar.setVisibility(View.GONE);
        viewText = (TextView) rootView.findViewById(R.id.menu_conditions_text);


        return rootView;

    }


    @Override
    public void onResume() {


        if (CloudUser.getInstance().isLoggedIn()) {
            viewText.setVisibility(View.GONE);
            buttonSave.setVisibility(View.VISIBLE);
            for (Condition conditionCloud : CloudDatabase.getInstance().getConditionList()) {
                conditionAdapter.add(conditionCloud);
            }
            conditionAdapter.notifyDataSetChanged();

        } else {
            buttonSave.setVisibility(View.GONE);
            viewText.setText(getActivity().getString(R.string.please_log_in_into_your_certocloud_account_first));
            conditionAdapter.clear();
            conditionAdapter.notifyDataSetChanged();
            viewText.setVisibility(View.VISIBLE);
        }


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                prefs.getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_notifications),
                        ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_notifications))) {
            Toast.makeText(getActivity(), R.string.these_settings_are_locked_by_the_admin, Toast.LENGTH_SHORT).show();
            buttonSave.setVisibility(View.GONE);
            viewText.setText(getActivity().getString(R.string.these_settings_are_locked_by_the_admin));
            conditionAdapter.clear();
            conditionAdapter.notifyDataSetChanged();
            viewText.setVisibility(View.VISIBLE);
        }

        super.onResume();
    }


    @Override
    public void onClickNavigationbarButton(int buttonId) {
    }


}