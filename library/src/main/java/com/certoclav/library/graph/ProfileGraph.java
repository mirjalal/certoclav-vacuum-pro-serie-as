package com.certoclav.library.graph;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;

import com.certoclav.library.R;

public class ProfileGraph {

	private GraphicalView view;
	
	private TimeSeries dataset = new TimeSeries("Temperature"); 
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	
	private XYSeriesRenderer graph1 = new XYSeriesRenderer(); // This will be used to customize line 1
	private XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(); // Holds a collection of XYSeriesRenderer and customizes the graph


	
	@SuppressWarnings("deprecation")
	public ProfileGraph()
	{

		// Add single dataset to multiple dataset
		mDataset.addSeries(dataset);
		
		// Customization time for line 1!
		graph1.setColor(Color.DKGRAY);//Farbe des Graphen (linie)

		graph1.setPointStyle(PointStyle.SQUARE);
		graph1.setDisplayChartValues(true);
		graph1.setChartValuesTextSize(24);
		graph1.setChartValuesSpacing(3);
	  //  graph1.setFillPoints(true);
		//graph1.setGradientEnabled(true);
		graph1.setFillBelowLine(true);
		graph1.setFillBelowLineColor(Color.LTGRAY);//fl?che unter graph
		
		//graph1.setGradientStart(10, Color.LTGRAY);
		//graph1.setGradientStop(100, Color.BLUE);
		
	

		
		
		renderer.setPointSize(0);
		//disable Zoom
		renderer.setZoomButtonsVisible(false);
		renderer.setClickEnabled(false);
		renderer.setExternalZoomEnabled(false);
		renderer.setZoomEnabled(false,false);
		renderer.setFitLegend(false);
		renderer.setPanEnabled(false);
		renderer.setPanEnabled(false, false);
		
		
		
		//int[] margins = {30,30,30,30};
		//renderer.setMargins(margins);
		

		
	
	 
		
		renderer.setChartTitleTextSize(24);
		renderer.setChartValuesTextSize(24);
		renderer.setDisplayChartValues(true);
		renderer.setLabelsTextSize(24);
		renderer.setLegendTextSize(0);//----Temperature
	    renderer.setAxisTitleTextSize(24); //temperature in ?C, Time in minutes
		
	    
		renderer.setXTitle("");
		renderer.setYTitle("");//"Temperature in ?C\n.\n.\n.\n.\n.\n.\n.\n.");
		renderer.setAxesColor(Color.LTGRAY);//Achsenfarbe
		renderer.setGridColor(Color.LTGRAY);//Gitternetzlinien
		renderer.setLabelsColor(Color.DKGRAY);//Achsenbeschriftungsfarbe
		renderer.setXLabelsColor(Color.DKGRAY);//Zahlen der Achsenbeschriftung farbe
	    renderer.setYLabelsColor(0,Color.DKGRAY);//Farbe der Zahlen der Y-Achse
		renderer.setApplyBackgroundColor(true);
		renderer.setMarginsColor(Color.argb(0x00,0x01,0x01,0x01));
		renderer.setBackgroundColor(Color.argb(0x00,0x01,0x01,0x01));
	renderer.setAntialiasing(true);
	renderer.setShowGridX(true);
	
		
		// Add single renderer to multiple renderer
		renderer.addSeriesRenderer(graph1);	
	}
	
	public GraphicalView getView(Context context) 
	{
		renderer.setXTitle("\n" + context.getString(R.string.time_in_minutes));
		dataset.setTitle(context.getString(R.string.temperature));
		//view =  ChartFactory.getCubeLineChartView(context, mDataset, renderer, 0.3f);
		view = ChartFactory.getLineChartView(context, mDataset, renderer);
		return view;
	}
	
	public void addNewPoints(Point p)
	{
		dataset.add(p.getX(), p.getY());
	}
	
	
	/*
	 * minX, maxX, minY, maxY
	 */
	public void setRange(double[] range){
		renderer.setRange(range);
	}

	public void clearAllPoints() {
		dataset.clear();
		
	}
	
}
