package com.climbingweather.cw;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.climbingweather.cw.RESTClient.HTTPMethod;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;

public class RESTLoader extends AsyncTaskLoader<RESTClientResponse> {
    
    private static final String TAG = RESTLoader.class.getName();
    
    // We use this delta to determine if our cached data is 
    // old or not. The value we have here is 10 minutes;
    private static final long STALE_DELTA = 10000; // 600000;
    
    private Context mContext;
    
    public enum HTTPVerb {
        GET,
        POST,
        PUT,
        DELETE
    }
    
    private HTTPVerb     mVerb;
    private Uri          mAction;
    private Bundle       mParams;
    private RESTClientResponse mRestClientResponse;
    
    private long mLastLoad;
    
    public RESTLoader(Context context) {
        super(context);
        
        mContext = context;
    }
    
    public RESTLoader(Context context, HTTPVerb verb, Uri action) {
        super(context);
        
        mVerb   = verb;
        mAction = action;
        
        mContext = context;
    }
    
    public RESTLoader(Context context, HTTPVerb verb, Uri action, Bundle params) {
        super(context);
        
        mVerb   = verb;
        mAction = action;
        mParams = params;
        mContext = context;
    }

    @Override
    public RESTClientResponse loadInBackground() {
        Log.i(TAG, "loadInBackground()");
        RESTClient client = new RESTClient(HTTPMethod.valueOf(mVerb.toString()), mAction, mParams);
        return client.sendRequest();
    }
    
    @Override
    public void deliverResult(RESTClientResponse data) {
        // Here we cache our response.
        mRestClientResponse = data;
        super.deliverResult(data);
        Log.i(TAG, "deliverResult()");
    }
    
    @Override
    protected void onStartLoading() {
        
        Log.i(TAG, "onStartLoading()");
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
    }
    
    private boolean isStale() {
        return System.currentTimeMillis() - mLastLoad >= STALE_DELTA;
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
        mRestClientResponse = null;
        
        // Reset our stale timer.
        mLastLoad = 0;
    }

}
