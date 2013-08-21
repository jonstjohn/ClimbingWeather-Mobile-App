package com.climbingweather.cw;

import com.climbingweather.cw.RESTClient.HTTPMethod;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Async Loader for ClimbingWeather.com API
 */
public class CwApiLoader extends AsyncTaskLoader<CwApiLoaderResult> {
    
    private HTTPMethod mMethod;
    
    private Context mContext;
    
    private Uri mAction;
    
    private Bundle mParams;
    
    /**
     * Loader for NEARBY areas
     */
    public static final int LOADER_AREA_NEARBY = 1;
    
    /**
     * Loader for FAVORITE areas
     */
    public static final int LOADER_AREA_FAVORITE = 2;
    
    /**
     * Loader for SEARCH areas
     */
    public static final int LOADER_AREA_SEARCH = 3;
    
    /**
     * Class for logging
     */
    private static final String TAG = CwApiLoader.class.getName();
    
    public CwApiLoader(Context context)
    {
        super(context);
    }
    
    public CwApiLoader(Context context, HTTPMethod method, Uri action, Bundle params) {
        super(context);
        
        mMethod = method;
        mAction = action;
        mParams = params;
        mContext = context;
    }
    
    @Override
    public CwApiLoaderResult loadInBackground() {
        
        // Check for data freshness TODO
        
        // If not fresh, reload using REST client TODO
        Log.i(TAG, "loadInBackground()");
        
        RESTClient client = new RESTClient(HTTPMethod.valueOf(mMethod.toString()), mAction, mParams);
        RESTClientResponse response = client.sendRequest();
        
        processResponse(response);
        
        return new CwApiLoaderResult(response); // TODO
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
        forceLoad();
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
    
    private void processResponse(RESTClientResponse response)
    {
        int code = response.getCode();
        String json = response.getData();
        
        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !json.equals("")) {
            Log.i(TAG, "onLoadFinished() using new loader");
            Log.i(TAG, json);
            processJson(json);
        }
        else {
            Toast.makeText(mContext, "Failed to load data. Check your internet settings.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void processJson(String result)
    {
        try {
            Log.i(TAG, "processJson");
            Log.i(TAG, result);
            
            // Convert JSON into areas using GSON
            Gson gson = new Gson();
            Area[] areas = gson.fromJson(result, CwApiAreaListResponse.class).getAreas();
            
            // Save areas
            for (int i = 0; i < areas.length; i++) {
                areas[i].save(mContext);
            }
            
            String projection[] = {"area._id AS area_id", "area.name", "d1.high AS d1_high",
                    "d1.wsym AS d1_wsym", "d2.high AS d2_high", "d2.wsym AS d2_wsym", "d3.high AS d3_high", "d3.wsym AS d3_wsym"};
            Cursor cursor = mContext.getContentResolver().query(
                    AreasContract.CONTENT_URI,
                    projection, null, null, null
            );
            Logger.log("Areas Result");
            Logger.log(Integer.toString(cursor.getCount()));
            while (cursor.moveToNext()) {
                Logger.log(formatAreaList(cursor));
                Logger.log(cursor.getString(cursor.getColumnIndex("area_id")));
            }
            
            Logger.log("Daily rows:");
            Cursor c2 = mContext.getContentResolver().query(DailyContract.CONTENT_URI, null, null, null, null);
            while (c2.moveToNext()) {
                Logger.log(c2.getString(c2.getColumnIndex(DailyContract.Columns.ID)));
                Logger.log(c2.getString(c2.getColumnIndex(DailyContract.Columns.DATE)));
                Logger.log(c2.getString(c2.getColumnIndex(DailyContract.Columns.AREA_ID)));
            }
            
        } catch (JsonParseException e) {
            Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
        }
        
    }
    
    private String formatAreaList(Cursor cursor)
    {
        String str = cursor.getString(cursor.getColumnIndex("name"));
        str += " " + cursor.getString(cursor.getColumnIndex("d1_high"));
        str += " " + cursor.getString(cursor.getColumnIndex("d1_wsym"));
        str += " " + cursor.getString(cursor.getColumnIndex("d2_high"));
        str += " " + cursor.getString(cursor.getColumnIndex("d2_wsym"));
        str += " " + cursor.getString(cursor.getColumnIndex("d3_high"));
        str += " " + cursor.getString(cursor.getColumnIndex("d3_wsym"));
        
        return str;
        
    }
}
