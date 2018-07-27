package com.certoclav.app.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.adapters.AuditLogsAdapter;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.util.AuditLogger;
import com.certoclav.library.application.ApplicationController;

import needle.Needle;
import needle.UiRelatedTask;


public class AuditLogFragment extends Fragment {

    private ListView listViewAuditLogs;
    private TextView textViewLockout;
    private AuditLogger auditLogger;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audit_log, container, false); //je nach mIten k�nnte man hier anderen Inhalt laden.
        auditLogger = AuditLogger.getInstance();
        listViewAuditLogs = rootView.findViewById(R.id.listViewAuditLogs);
        textViewLockout = rootView.findViewById(R.id.textViewLockout);
        progressBar = rootView.findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);
        listViewAuditLogs.setVisibility(View.GONE);
        textViewLockout.setVisibility(View.GONE);

        Needle.onBackgroundThread().execute(new UiRelatedTask<AuditLogsAdapter>() {
            @Override
            protected AuditLogsAdapter doWork() {
                return new AuditLogsAdapter(getActivity(),
                        auditLogger.getAuditLogs(null, null, false));
            }

            @Override
            protected void thenDoUiRelatedWork(AuditLogsAdapter result) {
                listViewAuditLogs.setAdapter(result);
                progressBar.setVisibility(View.GONE);
                listViewAuditLogs.setVisibility(View.VISIBLE);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if ((!Autoclave.getInstance().getUser().isAdmin() || Autoclave.getInstance().getState() == AutoclaveState.LOCKED) &&
                prefs.getBoolean(ApplicationController.getContext().getString(R.string.preferences_lockout_audit_logs),
                        ApplicationController.getContext().getResources().getBoolean(R.bool.preferences_lockout_audit_logs))) {
            Toast.makeText(getActivity(), R.string.these_settings_are_locked_by_the_admin, Toast.LENGTH_SHORT).show();
            listViewAuditLogs.setVisibility(View.GONE);
            textViewLockout.setVisibility(View.VISIBLE);
        } else {
            textViewLockout.setVisibility(View.GONE);
            if (listViewAuditLogs.getAdapter() != null)
                listViewAuditLogs.setVisibility(View.VISIBLE);
        }
    }
}
