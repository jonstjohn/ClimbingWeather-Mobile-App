package com.climbingweather.cw;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.actionbarsherlock.app.SherlockFragment;
import com.climbingweather.cw.AreaListFragment.AreaAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AreaAverageFragment extends SherlockFragment
{
    private GraphicalView mChart;

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mTempMeanSeries;

    private XYSeriesRenderer mTempMeanRenderer;
    
    private XYSeries mTempLowSeries;

    private XYSeriesRenderer mTempLowRenderer;
    
    private XYSeries mTempHighSeries;

    private XYSeriesRenderer mTempHighRenderer;

    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i("CW", "AreaAverageFragment onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);
        
        return inflater.inflate(R.layout.area_average, container, false);
    }
    
    public void onResume()
    {
        super.onResume();
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.chart);
        if (mChart == null) {
            //initChart();
            //addSampleData();
            //mChart = ChartFactory.getCubeLineChartView(getActivity(), mDataset, mRenderer, 0.3f);
            mChart = ChartFactory.getCubeLineChartView(getActivity(), mDataset, mRenderer, 0.3f);
            layout.addView(mChart);
            
            String url = "/api/area/averages/" + ((AreaFragmentActivity) getActivity()).getAreaId();
            GetAveragesJsonTask async = new GetAveragesJsonTask(this);
            async.execute(url);
        } else {
            mChart.repaint();
        }
    }
    
    private void initChart()
    {
        // Temperature High
        /*
        mTempHighSeries = new XYSeries("High");
        mTempHighSeries.add(1, 38.7);
        mTempHighSeries.add(2, 45.1);
        mTempHighSeries.add(3, 53.5);
        mTempHighSeries.add(4, 61.6);
        mTempHighSeries.add(5, 71.5);
        */
        
        mDataset.addSeries(mTempHighSeries);
        
        mTempHighRenderer = new XYSeriesRenderer();
        mTempHighRenderer.setDisplayChartValues(true);
        mTempHighRenderer.setChartValuesSpacing((float) 0.5);
        mTempHighRenderer.setChartValuesTextSize(30);
        mTempHighRenderer.setColor(Color.RED);
        
        mRenderer.addSeriesRenderer(mTempHighRenderer);
        
        // Temperature mean
        //mTempMeanSeries = new XYSeries("Mean");
        
        mDataset.addSeries(mTempMeanSeries);
        mTempMeanRenderer = new XYSeriesRenderer();
        mTempMeanRenderer.setDisplayChartValues(true);
        mTempMeanRenderer.setChartValuesSpacing((float) 0.5);
        mTempMeanRenderer.setChartValuesTextSize(30);
        mTempMeanRenderer.setColor(Color.GRAY);
        
        mRenderer.addSeriesRenderer(mTempMeanRenderer);
        
        // Temperature low
        /*
        mTempLowSeries = new XYSeries("Low");
        mTempLowSeries.add(1, 22.1);
        mTempLowSeries.add(2, 26.5);
        mTempLowSeries.add(3, 33.7);
        mTempLowSeries.add(4, 40.5);
        mTempLowSeries.add(5, 48.2);
        */
        
        mDataset.addSeries(mTempLowSeries);
        mTempLowRenderer = new XYSeriesRenderer();
        mTempLowRenderer.setDisplayChartValues(true);
        mTempLowRenderer.setChartValuesSpacing((float) 0.5);
        mTempLowRenderer.setChartValuesTextSize(30);
        mTempLowRenderer.setColor(Color.BLUE);
        
        mRenderer.addSeriesRenderer(mTempLowRenderer);
        
        //mRenderer.setChartTitle("Temperature");
        //mRenderer.setXTitle("Months");
        //mRenderer.setYTitle("Temperature");
        //mRenderer.setBackgroundColor(color.white);
        mRenderer.setAxisTitleTextSize(30);
        mRenderer.setLabelsTextSize(30);
        mRenderer.setLegendTextSize(30);
        mRenderer.setPointSize(0); // 10
        mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
        mRenderer.setZoomButtonsVisible(false);
        mRenderer.setShowAxes(true);
        mRenderer.setShowLegend(true);
        mRenderer.setShowGridX(true);
        mRenderer.setShowLabels(true);
        
        int color = Color.argb(0, 255, 255, 255); // Transparent colour
        mRenderer.setBackgroundColor(color);
        mRenderer.setMarginsColor(color);
        
        mRenderer.setXLabels(0);
        mRenderer.addXTextLabel(1, "Jan");
        mRenderer.addXTextLabel(2, "Feb");
        mRenderer.addXTextLabel(3, "Mar");
        mRenderer.addXTextLabel(4, "Apr");
        mRenderer.addXTextLabel(5, "May");
        
        
    }

    private void addSampleData() {
        mTempMeanSeries.add(1, 30.4);
        mTempMeanSeries.add(2, 35.8);
        mTempMeanSeries.add(3, 43.6);
        mTempMeanSeries.add(4, 51.1);
        mTempMeanSeries.add(5, 61.6);
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetAveragesJsonTask extends AsyncTask<String, Void, String> {
        
        private AreaAverageFragment fragment;
        
        public GetAveragesJsonTask(AreaAverageFragment fragment) {
            this.fragment = fragment;
        }
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
              
              Log.i("CW", args[0]);
              CwApi api = new CwApi(getActivity(), "2.0");
              return api.getJson(args[0]);

        }
        
        protected void onPreExecute() {
            fragment.getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE); 
        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            fragment.getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE); 
            processJson(result);
        }
    }
    
    /**
     * Load areas from JSON string result
     */
    public void processJson(String result) {
    
        try {
            Logger.log("CWI API Result:");
            Logger.log(result);
            Gson gson = new Gson();
            CwApiAverageResponse response = gson.fromJson(result, CwApiAverageResponse.class);
            Logger.log("Response string:");
            Logger.log(response.toString());

            AreaAverage average = response.getAreaAverage();
            AreaAverageData data = average.getAreaAverageData();
            
            if (data.getHigh() != null) {
                mTempHighSeries = new XYSeries("High");
                Double[] highs = data.getHigh().getMonthlyData();
                Logger.log("Highs:");
                for (int i = 0; i < highs.length; i++) {
                    Logger.log(Double.toString(highs[i]));
                    mTempHighSeries.add(i + 1, highs[i]);
                }
            }
            
            if (data.getLow() != null) {
                mTempLowSeries = new XYSeries("Low");
                Logger.log("Lows:");
                Double[] lows = data.getLow().getMonthlyData();
                for (int i = 0; i < lows.length; i++) {
                    Logger.log(Double.toString(lows[i]));
                    mTempLowSeries.add(i + 1, lows[i]);
                }
            }
            
            if (data.getMean() != null) {
                mTempMeanSeries = new XYSeries("Mean");
                Logger.log("Means:");
                Double[] means = data.getMean().getMonthlyData();
                for (int i = 0; i < means.length; i++) {
                    Logger.log(Double.toString(means[i]));
                    mTempMeanSeries.add(i + 1, means[i]);
                }
            }
            
            initChart();
            mChart.repaint();
            
        } catch (JsonParseException e) {
            Toast.makeText(getActivity(), "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
        }
        
    }
}
