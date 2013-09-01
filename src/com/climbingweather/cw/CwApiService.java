package com.climbingweather.cw;

import java.util.ArrayList;

import com.climbingweather.cw.RESTClient.HTTPMethod;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.util.Log;
import android.widget.Toast;

public class CwApiService extends IntentService {
    
    public static final String TAG = CwApiService.class.getName();
    
    public static final int FAVORITE_AREAS = 1;
    
    public CwApiService() {
        super("CwApiService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent()");
        
        Uri uri = intent.getData();
        
        if (uri != null) {
            // Favorites
            if (uri.equals(AreasContract.FAVORITES_URI)) {
                Log.i(TAG, intent.getDataString());
                loadFavoriteAreas();
                Intent i = new Intent(AreaListFragment.INTENT_FILTER_FAVORITE);
                sendBroadcast(i);
            } else if (uri.equals(AreasContract.NEARBY_URI)) {
                Log.i(TAG, intent.getDataString());
                Double latitude = intent.getDoubleExtra("latitude", 0.0);
                Double longitude = intent.getDoubleExtra("longitude", 0.0);
                loadNearbyAreas(latitude, longitude);
                Intent i = new Intent(AreaListFragment.INTENT_FILTER_NEARBY);
                sendBroadcast(i);
            }
        }
    }
    
    /**
     * Load favorite areas
     */
    public void loadFavoriteAreas()
    {
        CwApiProcessor processor = new CwApiProcessor(getApplicationContext());
        processor.startFavoriteAreas();
    }
    
    public void loadNearbyAreas(Double latitude, Double longitude) {
        
        CwApiProcessor processor = new CwApiProcessor(getApplicationContext());
        processor.startNearbyAreas(latitude, longitude);
    }
    
}
