package com.certoclav.library.graph;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.certoclav.library.R;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class LineGraph {
 
	private GraphicalView view;
	private TimeSeries timeSeriesSteam = new TimeSeries("Steam temperature"); 
	private TimeSeries timeSeriesMedia = new TimeSeries("Media temperature");
	private TimeSeries timeSeriesPressure = new TimeSeries("Pressure [kPa]");
	private XYMultipleSeriesDataset multiDataset = new XYMultipleSeriesDataset();
	
	private XYSeriesRenderer rendererForSeriesSteam = new XYSeriesRenderer(); // This will be used to customize line 1
	private XYSeriesRenderer rendererForSeriesMedia = new XYSeriesRenderer(); // This will be used to customize line 2
	private XYSeriesRenderer rendererForSeriesPressure = new XYSeriesRenderer(); // This will be used to customize line 2
	
	private XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer(); // Holds a collection of XYSeriesRenderer and customizes the graph
	public static final int TYPE_STEAM = 0;
	public static final int TYPE_MEDIA = 1;
	public static final int TYPE_PRESS = 2;


	
	public LineGraph()
	{

		// Add both time series to the multiple series dataset
		multiDataset.addSeries(timeSeriesSteam);
		multiDataset.addSeries(timeSeriesMedia);
		multiDataset.addSeries(timeSeriesPressure);
		
		
		// Customization time for line 1!
		rendererForSeriesSteam.setColor(Color.BLUE);//Farbe des Graphen (linie)
		rendererForSeriesSteam.setPointStyle(PointStyle.SQUARE);
		rendererForSeriesSteam.setLineWidth(5);
		rendererForSeriesSteam.setDisplayChartValues(true);
		rendererForSeriesSteam.setChartValuesTextSize(12);
		rendererForSeriesSteam.setChartValuesSpacing(3);
	    rendererForSeriesSteam.setFillPoints(true);
		//rendererForSeriesSteam.setGradientEnabled(true);
		//rendererForSeriesSteam.setFillBelowLine(true);
		//rendererForSeriesSteam.setFillBelowLineColor(Color.BLUE);//fl?che unter graph

		// Customization time for line 2!
		rendererForSeriesMedia.setColor(Color.RED);//Farbe des Graphen (linie)
		rendererForSeriesMedia.setPointStyle(PointStyle.SQUARE);
		rendererForSeriesMedia.setLineWidth(5);
		rendererForSeriesMedia.setDisplayChartValues(false);
		rendererForSeriesMedia.setChartValuesTextSize(12);
		rendererForSeriesMedia.setChartValuesSpacing(3);
	    rendererForSeriesMedia.setFillPoints(true);
//		rendererForSeriesMedia.setGradientEnabled(true);
//		rendererForSeriesMedia.setFillBelowLine(true);
	//	rendererForSeriesMedia.setFillBelowLineColor(Color.RED);//fl?che unter graph

		// Customization time for line 3!
		rendererForSeriesPressure.setColor(Color.DKGRAY);//Farbe des Graphen (linie)
		rendererForSeriesPressure.setPointStyle(PointStyle.SQUARE);
		rendererForSeriesPressure.setLineWidth(5);
		rendererForSeriesPressure.setDisplayChartValues(true);
		rendererForSeriesPressure.setChartValuesTextSize(12);
		rendererForSeriesPressure.setChartValuesSpacing(3);
	    rendererForSeriesPressure.setFillPoints(true);
	//	rendererForSeriesPressure.setGradientEnabled(true);
	//	rendererForSeriesPressure.setFillBelowLine(true);
	//	rendererForSeriesPressure.setFillBelowLineColor(Color.DKGRAY);//fl?che unter graph
		

		
		
		multiRenderer.setPointSize(0);
		multiRenderer.setInScroll(true);
		//disable Zoom
		multiRenderer.setZoomButtonsVisible(false);
		multiRenderer.setClickEnabled(false);
		multiRenderer.setExternalZoomEnabled(false);
		multiRenderer.setZoomEnabled(false,false);
		multiRenderer.setFitLegend(false);
		multiRenderer.setPanEnabled(false);
		multiRenderer.setPanEnabled(false, false);
		
		int[] margins = {15,15,15,15};
		multiRenderer.setMargins(margins);
		
		multiRenderer.setXTitle("");
		multiRenderer.setYTitle("");
		multiRenderer.setAxesColor(Color.LTGRAY);//Achsenfarbe
		multiRenderer.setGridColor(Color.LTGRAY);//Gitternetzlinien
		multiRenderer.setLabelsColor(Color.DKGRAY);//Achsenbeschriftungsfarbe
		multiRenderer.setXLabelsColor(Color.DKGRAY);//Zahlen der Achsenbeschriftung farbe
	    multiRenderer.setYLabelsColor(0,Color.DKGRAY);//Farbe der Zahlen der Y-Achse
		multiRenderer.setApplyBackgroundColor(true);
		multiRenderer.setMarginsColor(Color.argb(0x00,0x01,0x01,0x01));
		multiRenderer.setBackgroundColor(Color.argb(0x00,0x01,0x01,0x01));
		multiRenderer.setAntialiasing(true);
		multiRenderer.setShowGridX(true);

	
		
		// Add single renderer to multiple renderer
		multiRenderer.addSeriesRenderer(rendererForSeriesSteam);	
		multiRenderer.addSeriesRenderer(rendererForSeriesMedia);
		multiRenderer.addSeriesRenderer(rendererForSeriesPressure);
	}
	
	public GraphicalView getView(Context context) 
	{
		multiRenderer.setXTitle(context.getString(R.string.time_in_minutes));
		//multiRenderer.setYTitle(context.getString(R.string.temperature_in_c));
		timeSeriesSteam.setTitle(context.getString(R.string.temperature));
		timeSeriesMedia.setTitle(context.getString(R.string.media_temperature));
		timeSeriesPressure.setTitle(context.getString(R.string.pressure)+" [kPa]");
		
		//view =  ChartFactory.getCubeLineChartView(context, mDataset, renderer, 0.3f);
		view = ChartFactory.getLineChartView(context, multiDataset, multiRenderer);
		return view;
	}
	
	public Bitmap getBitmap(Context context) 
	{
		Log.e("GraphService", "Starte bitmap speichern");

		multiRenderer.setApplyBackgroundColor(true);
		multiRenderer.setMarginsColor(Color.WHITE);
		multiRenderer.setBackgroundColor(Color.WHITE);
		
		GraphicalView v =  ChartFactory.getCubeLineChartView(context, multiDataset, multiRenderer, 0.3f);
		
      

       //Enable the cache
        v.setDrawingCacheEnabled(true);

        //Set the layout manually to 800*600
        v.layout(0, 0, 800, 600);

        //Set the quality to high
        v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        //Build the cache, get the bitmap and close the cache
        v.buildDrawingCache(true);
        Bitmap bm = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false); 
        

		return bm;
	}
	public void addNewPoints(Point p, int type)
	{
		if(type == TYPE_STEAM){
		timeSeriesSteam.add(p.getX(), p.getY());
		}
		if(type == TYPE_MEDIA){
			timeSeriesMedia.add(p.getX(), p.getY());	
		}
		if(type == TYPE_PRESS){
			timeSeriesPressure.add(p.getX(), p.getY());	
		}
	}
	
	
	/*
	 * minX, maxX, minY, maxY
	 */
	public void setRange(double[] range){
		multiRenderer.setRange(range);
	}

	public void clearAllPoints() {
		timeSeriesSteam.clear();
		timeSeriesPressure.clear();
		timeSeriesMedia.clear();
		
	}

	public void enableZoom() {
		multiRenderer.setZoomButtonsVisible(true);
		multiRenderer.setClickEnabled(false);
		multiRenderer.setExternalZoomEnabled(true);
		multiRenderer.setZoomEnabled(true);
		multiRenderer.setZoomEnabled(true,true);
		multiRenderer.setFitLegend(true);
		multiRenderer.setPanEnabled(true);
		multiRenderer.setPanEnabled(true,true);

		multiRenderer.setZoomButtonsVisible(true);
		multiRenderer.setClickEnabled(false);
		multiRenderer.setExternalZoomEnabled(true);
		multiRenderer.setZoomEnabled(true);
		multiRenderer.setZoomEnabled(true,true);
		multiRenderer.setFitLegend(true);
		multiRenderer.setPanEnabled(true);
		multiRenderer.setPanEnabled(true,true);
	
		
	}
	
}
