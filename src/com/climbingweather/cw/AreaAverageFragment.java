package com.climbingweather.cw;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AreaAverageFragment extends SherlockFragment  implements DataFragmentInterface
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
            mChart = ChartFactory.getCubeLineChartView(getActivity(), mDataset, mRenderer, 0.3f);
            layout.addView(mChart);
            loadData();
        } else {
            mChart.repaint();
        }
    }
    
    private void loadData()
    {
        String url = "/area/averages/" + ((AreaFragmentActivity) getActivity()).getAreaId();
        GetAveragesJsonTask async = new GetAveragesJsonTask(this);
        async.execute(url);
    }
    
    private void initChart()
    {
        // Temperature High
        mDataset.addSeries(mTempHighSeries);
        
        mTempHighRenderer = new XYSeriesRenderer();
        mTempHighRenderer.setDisplayChartValues(true);
        mTempHighRenderer.setChartValuesSpacing((float) 0.5);
        mTempHighRenderer.setChartValuesTextSize(30);
        mTempHighRenderer.setColor(Color.RED);
        
        mRenderer.addSeriesRenderer(mTempHighRenderer);
        
        // Temperature mean
        mDataset.addSeries(mTempMeanSeries);
        mTempMeanRenderer = new XYSeriesRenderer();
        mTempMeanRenderer.setDisplayChartValues(true);
        mTempMeanRenderer.setChartValuesSpacing((float) 0.5);
        mTempMeanRenderer.setChartValuesTextSize(30);
        mTempMeanRenderer.setColor(Color.GRAY);
        
        mRenderer.addSeriesRenderer(mTempMeanRenderer);
        
        // Temperature low
        mDataset.addSeries(mTempLowSeries);
        mTempLowRenderer = new XYSeriesRenderer();
        mTempLowRenderer.setDisplayChartValues(true);
        mTempLowRenderer.setChartValuesSpacing((float) 0.5);
        mTempLowRenderer.setChartValuesTextSize(30);
        mTempLowRenderer.setColor(Color.BLUE);
        
        mRenderer.addSeriesRenderer(mTempLowRenderer);
        
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
        
        int color = Color.argb(0, 255, 255, 255); // Transparent color
        mRenderer.setBackgroundColor(color);
        mRenderer.setMarginsColor(color);
        
        mRenderer.setXLabels(0);
        mRenderer.addXTextLabel(1, "Jan");
        mRenderer.addXTextLabel(2, "Feb");
        mRenderer.addXTextLabel(3, "Mar");
        mRenderer.addXTextLabel(4, "Apr");
        mRenderer.addXTextLabel(5, "May");
        mRenderer.addXTextLabel(6, "Jun");
        mRenderer.addXTextLabel(7, "Jul");
        mRenderer.addXTextLabel(8, "Aug");
        mRenderer.addXTextLabel(9, "Sep");
        mRenderer.addXTextLabel(10, "Oct");
        mRenderer.addXTextLabel(11, "Nov");
        mRenderer.addXTextLabel(12, "Dec");
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
    
    public void refresh()
    {
        loadData();
    }
}
