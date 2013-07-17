package com.climbingweather.cw;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;

public class AreaAverageGraph {

    public Intent getIntent(Context context)
    {
        int[] y = { 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91 };
        
        CategorySeries series = new CategorySeries("Demo Bar Graph");
        for (int i = 0; i < y.length; i++) {
            series.add("Bar " + (i+1), y[i]);
        }
        
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series.toXYSeries());
        
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        
        Intent intent = ChartFactory.getBarChartIntent(context, dataset, mRenderer, Type.DEFAULT);
        
        return intent;
    }
}
