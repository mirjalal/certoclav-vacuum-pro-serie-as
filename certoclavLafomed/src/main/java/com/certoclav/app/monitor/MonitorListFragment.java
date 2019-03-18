package com.certoclav.app.monitor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.certoclav.app.R;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.util.Helper;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link }
 * interface.
 */
public class MonitorListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private Protocol protocol;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MonitorListFragment() {
    }

    @SuppressWarnings("unused")
    public static MonitorListFragment newInstance(int columnCount, Protocol protocol) {
        MonitorListFragment fragment = new MonitorListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        fragment.protocol = protocol;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_protocolentry_list, container, false);

        // Set the adapter

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        if(protocol!=null) {
            view.findViewById(R.id.mediaTemperature2).setVisibility(protocol.isContByFlexProbe2()?View.VISIBLE:View.GONE);
            ((TextView)view.findViewById(R.id.mediaTemperature2)).setText(getString(R.string.header_media_temp_2,Helper.getTemperatureUnitText(protocol.getTemperatureUnit())));
            ((TextView)view.findViewById(R.id.mediaTemperature)).setText(getString(R.string.header_media_temp,Helper.getTemperatureUnitText(protocol.getTemperatureUnit())));
            ((TextView)view.findViewById(R.id.temperature)).setText(getString(R.string.header_temp,Helper.getTemperatureUnitText(protocol.getTemperatureUnit())));
            view.findViewById(R.id.mediaTemperature).setVisibility(protocol.isContByFlexProbe1()?View.VISIBLE:View.GONE);
            recyclerView.setAdapter(new MyProtocolEntryRecyclerViewAdapter(new ArrayList<>(protocol != null ? protocol.getProtocolEntry() : null),
                    protocol));
        }
        return view;
    }


    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

