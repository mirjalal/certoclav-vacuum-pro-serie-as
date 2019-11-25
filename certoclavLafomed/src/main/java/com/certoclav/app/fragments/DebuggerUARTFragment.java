package com.certoclav.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.certoclav.app.AppConstants;
import com.certoclav.app.R;
import com.certoclav.app.databinding.FragmentDebuggerUartBinding;
import com.certoclav.app.service.ReadAndParseSerialService;

import java.util.ArrayList;
import java.util.List;

public class DebuggerUARTFragment extends Fragment implements ReadAndParseSerialService.SerialReadWriteListener {

    private FragmentDebuggerUartBinding binding;

    public DebuggerUARTFragment() {
        // Required empty public constructor
    }

    public static DebuggerUARTFragment newInstance() {
        DebuggerUARTFragment fragment = new DebuggerUARTFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (logs == null)
            logs = new ArrayList<>();
    }

    @Override
    public void onResume() {
        ReadAndParseSerialService.getInstance().addSerialReadWriteListener(this);
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_debugger_uart, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ReadAndParseSerialService.getInstance().addSerialReadWriteListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ReadAndParseSerialService.getInstance().removeSerialReadWriteListener(this);
    }

    private static List<String> logs;
    private final static int MAX_LOG_COUNT = 15;

    @Override
    public void onRead(final String message) {
        logs.add("<< " + message + "");
        updateLogs();
    }

    @Override
    public void onWrote(final String message) {
        logs.add(">> " + message + "");
        updateLogs();
    }

    private void updateLogs() {
        if (logs.size() > MAX_LOG_COUNT)
            logs.remove(0);
        if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(AppConstants.PREFERENCE_KEY_SHOW_UART_LOGS, true)) {
            if (binding.textViewLogs.getText().length() > 0)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.textViewLogs.setText("");
                    }
                });
            return;
        }
        final StringBuilder log = new StringBuilder();
        for (String l : logs)
            log.append(l);

        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.textViewLogs.setText(log.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
