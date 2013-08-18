package com.climbingweather.cw;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

/**
 * Async Loader for ClimbingWeather.com API
 */
public class CwApiLoader extends AsyncTaskLoader<CwApiLoaderResult> {
    
    /**
     * Class for logging
     */
    private static final String TAG = CwApiLoader.class.getName();
    
    public CwApiLoader(Context context)
    {
        super(context);
    }
    
    @Override
    public CwApiLoaderResult loadInBackground() {
        
        // Check for data freshness TODO
        
        // If not fresh, reload using REST client TODO
        Log.i(TAG, "loadInBackground()");
        
        //RESTClient client = new RESTClient(HTTPMethod.valueOf(mVerb.toString()), mAction, mParams);
        //return client.sendRequest();
        
        return new CwApiLoaderResult(); // TODO
    }
    
    @Override
    public void deliverResult(CwApiLoaderResult response) {
        super.deliverResult(response);
        Log.i(TAG, "deliverResult()");
    }
    
    @Override
    protected void onStartLoading() {
        
        Log.i(TAG, "onStartLoading()");
        
        /* TODO
        // Return cached result if it exists and is not statel
        if (mRestClientResponse != null && !isStale()) {
            Log.i(TAG, "Deliverying immediately");
            super.deliverResult(mRestClientResponse);
        }
        
        // If our response is null or we have hung onto it for a long time,
        // then we perform a force load.
        if (mRestClientResponse == null || isStale()) {
            Log.i(TAG, "Forcing reload");
            forceLoad();
        }
        Log.i(TAG, "Update last load");
        mLastLoad = System.currentTimeMillis();
        */
    }
    
    @Override
    protected void onStopLoading() {
        Log.i(TAG, "onStopLoading()");
        // This prevents the AsyncTask backing this
        // loader from completing if it is currently running.
        cancelLoad();
    }
    
    @Override
    protected void onReset() {
        super.onReset();
        
        Log.i(TAG, "onReset()");
        
        // Stop the Loader if it is currently running.
        onStopLoading();
        
        // Get rid of our cache if it exists.
        //mRestClientResponse = null;
        
        // Reset our stale timer.
        //mLastLoad = 0;
    }
}
