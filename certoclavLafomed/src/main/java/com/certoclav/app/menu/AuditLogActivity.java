package com.certoclav.app.menu;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.certoclav.app.R;
import com.certoclav.app.adapters.AuditLogsAdapter;
import com.certoclav.app.database.DatabaseService;

public class AuditLogActivity extends Fragment {

    private ListView listViewAuditLogs;
    private DatabaseService databaseService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_audit_log, container, false); //je nach mIten k�nnte man hier anderen Inhalt laden.
        databaseService = new DatabaseService(getActivity());
        listViewAuditLogs = rootView.findViewById(R.id.listViewAuditLogs);
        listViewAuditLogs.setAdapter(new AuditLogsAdapter(getActivity(),
                databaseService.getAuditLogs(null, null, false)));

        return rootView;
    }
}
