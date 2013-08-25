package com.climbingweather.cw;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

public class CwService extends IntentService {
    
    public CwService() {
        super("CW Service");
    }

    public static final String METHOD_EXTRA = "com.climbingweather.cw.service.METHOD_EXTRA";
    
    public static final String METHOD_GET = "GET";
    
    public static final String RESOURCE_TYPE_EXTRA = "com.climbingweather.cw.service.RESOURCE_TYPE_EXTRA";
    
    public static final int RESOURCE_TYPE_FAVORITE = 1;
    
    public static final String SERVICE_CALLBACK = "com.climbingweather.cw.service.SERVICE_CALLBACK";
    
    public static final String ORIGINAL_INTENT_EXTRA = "com.climbingweather.cw.service.ORIGINAL_INTENT_EXTRA";
    
    private ResultReceiver mCallback;
    
    private Intent mOriginalRequestIntent;

    @Override
    protected void onHandleIntent(Intent intent) {
        
        String method = intent.getStringExtra(METHOD_EXTRA);
        
        int resourceType = intent.getIntExtra(RESOURCE_TYPE_EXTRA, -1);
        
        mCallback = intent.getParcelableExtra(SERVICE_CALLBACK);
        
        switch (resourceType) {
        case RESOURCE_TYPE_FAVORITE:
            if (method.equalsIgnoreCase(METHOD_GET)) {
                FavoritesProcessor processor = new FavoritesProcessor(getApplicationContext());
                processor.getFavorites(makeFavoritesProcessorCallback());
            }
        }
        
    }
    
    private FavoritesProcessorCallback makeFavoritesProcessorCallback()
    {
        FavoritesProcessorCallback callback = new FavoritesProcessorCallback() {
            public void send(int resultCode)
            {
                if (mCallback != null) {
                    mCallback.send(resultCode, getOriginalIntentBundle());
                }
            }
        };
        
        return callback;
    }
    
    protected Bundle getOriginalIntentBundle()
    {
        Bundle originalRequest = new Bundle();
        originalRequest.putParcelable(ORIGINAL_INTENT_EXTRA, mOriginalRequestIntent);
        return originalRequest;
    }
}
