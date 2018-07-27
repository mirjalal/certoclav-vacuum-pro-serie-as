package com.certoclav.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.certoclav.app.R;
import com.certoclav.app.database.Command;
import com.certoclav.app.database.Protocol;
import com.certoclav.app.graph.GraphService;

import org.achartengine.GraphicalView;

import java.util.ArrayList;

public class ProgramDefinitionGraphFragment extends Fragment {

    private static GraphicalView programGraph;
    private Protocol protocol;
    private LinearLayout graphContainer;

    public ProgramDefinitionGraphFragment() {
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.program_definition_graph_fragment, container, false); //je nach mIten k?nnte man hier anderen Inhalt laden.
        graphContainer = (LinearLayout) rootView.findViewById(R.id.program_definition_container_graph);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (protocol == null) return;
        Log.e("ProgramDefinition", "vor getcurrentgraph");
        programGraph = GraphService.getInstance().getProtocolGraphView(protocol).getView(getActivity());

        if (programGraph != null) {
            graphContainer.addView(programGraph);
        }

        Log.e("ControlGraphFragment", "nac add view getcurrentgraph");


    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public void notifyDataChanged() {
        graphContainer.removeAllViews();
        programGraph = GraphService.getInstance().getProtocolGraphView(protocol).getView(getActivity());
        graphContainer.addView(programGraph);
        graphContainer.requestLayout();
        graphContainer.invalidate();

    }
}

