package com.certoclav.app.graph;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.certoclav.app.database.Protocol;
import com.certoclav.app.database.ProtocolEntry;
import com.certoclav.app.listener.SensorDataListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveData;
import com.certoclav.library.graph.LineGraph;
import com.certoclav.library.graph.Point;
import com.j256.ormlite.dao.ForeignCollection;

import org.achartengine.GraphicalView;

public class GraphService implements SensorDataListener {

    private GraphicalView view;
    private LineGraph runningGraph = new LineGraph();


    private LineGraph graphTemp1 = new LineGraph();
    private LineGraph graphTemp2 = new LineGraph();
    private LineGraph graphTemp3 = new LineGraph();
    private LineGraph graphPressure = new LineGraph();
    private GraphicalView viewTemp1;
    private GraphicalView viewTemp2;
    private GraphicalView viewTemp3;
    private GraphicalView viewPressure;


    private long timeStampOfLastPoint = 0; //for current graph generation

    private long secondsSinceStart = 0;

    private static GraphService instance = new GraphService();

    public static synchronized GraphService getInstance() {
        return instance;

    }

    private Handler mGuiHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            try {
                view.invalidate();
                view.repaint();
                view.invalidate();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        ;
    };

    private GraphService() {
        Autoclave.getInstance().setOnSensorDataListener(this);


        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {

                    try {
                        switch (Autoclave.getInstance().getState()) {
                            case RUNNING:
                                secondsSinceStart = Autoclave.getInstance().getSecondsSinceStart();


                                Log.e("GraphService", "secSS: " + secondsSinceStart);
                                if (secondsSinceStart != timeStampOfLastPoint) {
                                    if (secondsSinceStart % (20) == 0) {
                                        timeStampOfLastPoint = secondsSinceStart;

                                        Point p = new Point(roundFloat((float) (secondsSinceStart / 60.0)), roundFloat((float) (Autoclave.getInstance().getData().getTemp1().getCurrentValue())));
                                        runningGraph.addNewPoints(p, LineGraph.TYPE_STEAM);
                                    /*Point p2 = new Point(roundFloat((float) (secondsSinceStart/60.0)),  roundFloat((float) (Autoclave.getInstance().getData().getTemp2().getCurrentValue())));
									runningGraph.addNewPoints(p2, LineGraph.TYPE_MEDIA);*/
                                        Point p3 = new Point(roundFloat((float) (secondsSinceStart / 60.0)), roundFloat((float) (Autoclave.getInstance().getData().getPress().getCurrentValueInKPa())));
                                        runningGraph.addNewPoints(p3, LineGraph.TYPE_PRESS);
                                        double[] range = new double[4];
                                        range[0] = 0;
                                        range[1] = (secondsSinceStart / 60.0);
                                        range[2] = 0;
                                        range[3] = 300;
                                        runningGraph.setRange(range);

                                        mGuiHandler.sendEmptyMessage(0);
                                    }
                                }


                                break;
                            case NOT_RUNNING:
                                runningGraph.clearAllPoints();
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }


    public GraphicalView getGraphTemp1(Context context) {
        viewTemp1 = graphTemp1.getView(context);
        return viewTemp1;
    }

    public GraphicalView getGraphTemp2(Context context) {
        viewTemp2 = graphTemp2.getView(context);
        return viewTemp2;
    }

    public GraphicalView getGraphTemp3(Context context) {
        viewTemp3 = graphTemp3.getView(context);
        return viewTemp3;
    }

    public GraphicalView getGraphPressure(Context context) {
        viewPressure = graphPressure.getView(context);
        return viewPressure;
    }


    public GraphicalView getCurrentGraph(Context context) {

        view = runningGraph.getView(context);
        return view;
    }


    private LineGraph getProtocolGraph(Protocol protocol) {

        LineGraph protocolGraph = new LineGraph();

        try {

            ForeignCollection<ProtocolEntry> entrys = protocol.getProtocolEntry();

            long startTime = 0;
            long pastSeconds = 0;
            long timeLastPoint = 0;

            for (ProtocolEntry entry : entrys) {
                if (startTime == 0) {
                    startTime = entry.getTimestamp().getTime();
                }

                pastSeconds = (entry.getTimestamp().getTime() / 1000) - (startTime / 1000);

                if (pastSeconds - timeLastPoint > 30) {

                    Point p = new Point(roundFloat((float) (pastSeconds / 60.0)), roundFloat(entry.getTemperature()));
                    Log.e("seconds gettemperature", roundFloat((float) (pastSeconds / 60.0)) + " " + roundFloat(entry.getTemperature()));
                    protocolGraph.addNewPoints(p, LineGraph.TYPE_STEAM);
                    Point p2 = new Point(roundFloat((float) (pastSeconds / 60.0)), roundFloat((float) (entry.getPressure())));
                    protocolGraph.addNewPoints(p2, LineGraph.TYPE_PRESS);
					/*if(protocol.getDryTime()==0){ //=> liquid program
						Point p3 = new Point(roundFloat((float) (pastSeconds/60.0)),roundFloat(entry.getMediaTemperature()));
						protocolGraph.addNewPoints(p3, LineGraph.TYPE_MEDIA);
					}*/

                    timeLastPoint = pastSeconds;
                }
            }

            double[] range = new double[4];
            range[0] = 0;
            range[1] = (pastSeconds / 60.0) * 1.05;
            range[2] = 0;
            range[3] = 300;
            protocolGraph.setRange(range);

        } catch (Exception e) {
            protocolGraph = new LineGraph();

        }

        return protocolGraph;
    }


    public LineGraph getProtocolGraphView(Protocol protocol) {

        return getProtocolGraph(protocol);

    }

    public GraphicalView getZoomableProtocolGraphView(Context context, Protocol protocol) {

        LineGraph protocolGraph = getProtocolGraph(protocol);
        protocolGraph.enableZoom();
        return protocolGraph.getView(context);

    }


    @Override
    public void onSensorDataChange(AutoclaveData data) {

        switch (Autoclave.getInstance().getState()) {

        }

    }


    private Double roundFloat(float f) {
        return Double.parseDouble(String.format("%.2f", f));
    }


}
