package com.climbingweather.cw;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;
import android.os.Bundle;

/**
 * Singleton
 */
public class CwServiceHelper {
    
    private Context mContext;
    
    private static CwServiceHelper instance;
    
    private static Object lock = new Object();
    
    private Map<String,Long> pendingRequests = new HashMap<String,Long>();
    
    private static final String REQUEST_ID = "REQUEST_ID";
    public static String ACTION_REQUEST_RESULT = "REQUEST_RESULT";
    public static String EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID";
    public static String EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE";
    private static final String favoritesHashKey = "FAVORITE";
    
    private CwServiceHelper(Context context)
    {
        mContext = context;
    }
    
    public static CwServiceHelper getInstance(Context context)
    {
        synchronized (lock) {
            if (instance == null) {
                instance = new CwServiceHelper(context);
            }
        }
        return instance;
    }
    
    public boolean isRequestPending(long requestId)
    {
        return pendingRequests.containsValue(requestId);
    }
    
    private long generateRequestId()
    {
        long requestId = UUID.randomUUID().getLeastSignificantBits();
        return requestId;
    }
    
    public long getFavorites()
    {
        long requestId = generateRequestId();
        pendingRequests.put(favoritesHashKey, requestId);
        
        ResultReceiver serviceCallback = new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData)
            {
                handleFavoritesResponse(resultCode, resultData);
            }
        };
        
        Intent intent = new Intent(mContext, CwService.class);
        intent.putExtra(CwService.METHOD_EXTRA, CwService.METHOD_GET);
        intent.putExtra(CwService.RESOURCE_TYPE_EXTRA, CwService.RESOURCE_TYPE_FAVORITE);
        intent.putExtra(CwService.SERVICE_CALLBACK, serviceCallback);
        intent.putExtra(REQUEST_ID, requestId);
        
        mContext.startService(intent);
        
        
        return requestId;
    }
    
    private void handleFavoritesResponse(int resultCode, Bundle resultData)
    {
        Intent originalIntent = (Intent) resultData.getParcelable("TODO"); // TODO
        
        if (originalIntent != null) {
            long requestId = originalIntent.getLongExtra(REQUEST_ID, 0);
            
            pendingRequests.remove(requestId);
            
            Intent resultBroadcast = new Intent(ACTION_REQUEST_RESULT);
            resultBroadcast.putExtra(EXTRA_REQUEST_ID, requestId);
            resultBroadcast.putExtra(EXTRA_RESULT_CODE, resultCode);
            
            mContext.sendBroadcast(resultBroadcast);
            
        }
    }

}
