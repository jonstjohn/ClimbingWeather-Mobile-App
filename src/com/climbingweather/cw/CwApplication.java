package com.climbingweather.cw;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

import android.app.Application;

public class CwApplication extends Application
{
    
    public Tracker mGaTracker;
    private GoogleAnalytics mGaInstance;

    private void initGaTracker()
    {
        mGaInstance = GoogleAnalytics.getInstance(this);
        mGaTracker = mGaInstance.getTracker("UA-205323-8");
    }
    
    public Tracker getGaTracker()
    {
        if (mGaTracker == null) {
            initGaTracker();
        }
        return mGaTracker;
        
    }
}
