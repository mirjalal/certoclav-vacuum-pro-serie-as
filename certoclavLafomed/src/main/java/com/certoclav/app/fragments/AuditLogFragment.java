package com.certoclav.app.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.adapters.AuditLogsAdapter;
import com.certoclav.app.database.AuditLog;
import com.certoclav.app.database.DatabaseService;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.library.application.ApplicationController;
import com.paging.listview.PagingListView;

import java.util.ArrayList;
import java.util.List;

public class AuditLogFragment extends Fragment {

    private static final int MAX_AUDIT_LOG_ITEM_PER_PAGE = 25;

    private PagingListView listViewAuditLogs;
    private TextView textViewLockout;
    private DatabaseService databaseService;
    private static int pageNumber = 0;
    private AuditLogsAdapter auditLogsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audit_log, container, false); //je nach mIten k�nnte man hier anderen Inhalt laden.

        listViewAuditLogs = rootView.findViewById(R.id.audit_logs_list);
        textViewLockout = rootView.findViewById(R.id.textViewLockout);
        ProgressBar progressBar = rootView.findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);
        listViewAuditLogs.setVisibility(View.GONE);
        textViewLockout.setVisibility(View.GONE);

        databaseService = DatabaseService.getInstance();

        auditLogsAdapter = new AuditLogsAdapter(getActivity(), new ArrayList<AuditLog>());
        listViewAuditLogs.setItemsCanFocus(true);
        listViewAuditLogs.setAdapter(auditLogsAdapter);

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

            pageNumber = 0;
            auditLogsAdapter.clear();
            listViewAuditLogs.setHasMoreItems(true);
            listViewAuditLogs.setPagingableListener(new PagingListView.Pagingable() {
                @Override
                public void onLoadMoreItems() {
                    AuditLogLoader auditLogLoader = new AuditLogLoader();
                    auditLogLoader.execute(pageNumber, MAX_AUDIT_LOG_ITEM_PER_PAGE);
                }
            });
        }
    }

    private class AuditLogLoader extends AsyncTask<Integer, Void, List<AuditLog>> {

        @Override
        protected List<AuditLog> doInBackground(Integer... integers) {
            return databaseService.getPagedAuditLogs(integers[0], integers[1]);
        }

        @Override
        protected void onPostExecute(List<AuditLog> auditLogs) {
            if (AuditLogFragment.this.isVisible()) {
                if (auditLogs != null) {
                    AuditLogFragment.pageNumber += 1;
                    auditLogsAdapter.addAll(auditLogs);

                    auditLogsAdapter.notifyDataSetChanged();
                    listViewAuditLogs.onFinishLoading(auditLogs.size() > 0, auditLogs);
                } else
                    listViewAuditLogs.onFinishLoading(false, null);
            }
            super.onPostExecute(auditLogs);
        }
    }
}
