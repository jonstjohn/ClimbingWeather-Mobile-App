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
                Intent i = new Intent("your.action");
                sendBroadcast(i);
            }
        }
    }
    
    /**
     * Load favorite areas
     * @param callback
     * @param forceReload
     */
    public void loadFavoriteAreas()
    {
        
        // Get ids from content provider
        ArrayList<String> ids = new ArrayList<String>();
        Cursor cursor = getContentResolver().query(
                FavoritesContract.CONTENT_URI, null, null, null, FavoritesContract.Columns.NAME + " ASC");
        while (cursor.moveToNext()) {
            ids.add(cursor.getString(cursor.getColumnIndex(FavoritesContract.Columns.AREA_ID)));
        }
        
        // Loop over favorite area ids to build URL
        if (ids.size() > 0) {
            String idStr = "";
            for (int i = 0; i < ids.size() - 1; i++) {
                idStr += ids.get(i) + ",";
            }
            idStr += ids.get(ids.size() - 1);
            String url = "/area/list/ids-" + idStr;
            Bundle params = new Bundle();
            params.putString("days", "3");
            
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            
            Uri.Builder uriBuilder = new Uri.Builder()
            .scheme("http")
            .authority("api.climbingweather.com")
            .path("/v2" + url)
            .appendQueryParameter("apiKey", getApiKey())
            .appendQueryParameter("tempUnit", prefs.getString("tempUnit", "f"))
            .appendQueryParameter("version", "2.0");
            
            
            for (String key : params.keySet()) {
                uriBuilder.appendQueryParameter(key, params.getString(key));
            }
            
            Uri uri = uriBuilder.build();
            
            Bundle mParams = new Bundle();
            
            RESTClient client = new RESTClient(HTTPMethod.valueOf("GET"), uri, mParams);
            RESTClientResponse response = client.sendRequest();
            processResponse(response);
            //initLoader(callback, url, params, CwApiLoader.LOADER_AREA_FAVORITE, forceReload);
        }
    }
    
    private void processResponse(RESTClientResponse response)
    {
        int code = response.getCode();
        String json = response.getData();
        
        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !json.equals("")) {
            Log.i(TAG, "onLoadFinished() using service");
            Log.i(TAG, json);
            processJson(json);
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
                //Log.i(TAG, "Loader id: " + Integer.toString(getId()));
                //if (getId() == LOADER_AREA_NEARBY) {
                //    areas[i].setNearby(i);
                //}
                areas[i].save(getContentResolver());
            }
            
            String projection[] = {"area._id AS area_id", "area.name", "d1.high AS d1_high",
                    "d1.wsym AS d1_wsym", "d2.high AS d2_high", "d2.wsym AS d2_wsym", "d3.high AS d3_high", "d3.wsym AS d3_wsym"};
            Cursor cursor = getContentResolver().query(
                    AreasContract.CONTENT_URI,
                    projection, null, null, null
            );
            Log.i(TAG, "Areas Result");
            Log.i(TAG, Integer.toString(cursor.getCount()));
            while (cursor.moveToNext()) {
                Log.i(TAG, formatAreaList(cursor));
                Log.i(TAG, cursor.getString(cursor.getColumnIndex("area_id")));
            }
            
            Log.i(TAG, "Daily rows:");
            Cursor c2 = getContentResolver().query(DailyContract.CONTENT_URI, null, null, null, null);
            while (c2.moveToNext()) {
                Log.i(TAG, c2.getString(c2.getColumnIndex(DailyContract.Columns.ID)));
                Log.i(TAG, c2.getString(c2.getColumnIndex(DailyContract.Columns.DATE)));
                Log.i(TAG, c2.getString(c2.getColumnIndex(DailyContract.Columns.AREA_ID)));
            }
            
        } catch (JsonParseException e) {
            Log.i(TAG, "An error occurred while retrieving area data");
        }
        
    }
    
    /**
     * Get API key
     * @return
     */
    private String getApiKey()
    {
        String apiKey = "android-";
        
        String androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        
        if (androidId == null) {
            androidId = "unknown";
        }
        
        apiKey += androidId;
        return apiKey;
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
