package com.climbingweather.cw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.climbingweather.cw.RESTClient.HTTPMethod;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

/**
 * Climbing Weather API processor
 * The API processor receives start requests from the CwApiService
 * and notifies the CwContentProvider that a request is in progress.
 * It then starts a REST requests using the RESTClient and sends the
 * updated results to the CwContentProvider.  Finally, it tells the
 * CwApiService that the process is completed.
 */
public class CwApiProcessor {
    
    /**
     * Tag for logging
     */
    private static final String TAG = CwApiProcessor.class.getName();
    
    /**
     * Context
     */
    private Context mContext;
    
    /**
     * Constructor
     * @param context
     */
    public CwApiProcessor(Context context) {
        mContext = context;
    }
    
    /**
     * Get favorite ids from content provider
     * @return ArrayList<String>
     */
    private ArrayList<String> getFavoriteIds() {
        
        // Get ids from content provider
        ArrayList<String> ids = new ArrayList<String>();
        Cursor cursor = mContext.getContentResolver().query(
                FavoritesContract.CONTENT_URI, null, null, null, FavoritesContract.Columns.NAME + " ASC");
        while (cursor.moveToNext()) {
            ids.add(cursor.getString(cursor.getColumnIndex(FavoritesContract.Columns.AREA_ID)));
        }
        return ids;
        
    }
    
    /**
     * Build Climbing Weather API Uri
     * @param url
     * @return Uri
     */
    private Uri buildCwApiUri(String url) {
        Bundle params = new Bundle();
        params.putString("days", "3");
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        
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
        
        return uriBuilder.build();
    }
    
    /**
     * Start processing favorite areas
     */
    public void startFavoriteAreas() {
        // Get ids from content provider
        ArrayList<String> ids = getFavoriteIds();
        
        // Loop over favorite area ids to build URL
        if (ids.size() > 0) {
            String idStr = "";
            for (int i = 0; i < ids.size() - 1; i++) {
                idStr += ids.get(i) + ",";
            }
            idStr += ids.get(ids.size() - 1);
            String url = "/area/list/ids-" + idStr;
            
            Uri uri = buildCwApiUri(url);
            
            Bundle mParams = new Bundle();
            
            RESTClient client = new RESTClient(HTTPMethod.valueOf("GET"), uri, mParams);
            RESTClientResponse response = client.sendRequest();
            
            Bundle processParams = new Bundle();
            processParams.putParcelable("uri", AreasContract.FAVORITES_URI);
            processAreasResponse(response, processParams);
        }
    }
    
    /**
     * Start processing nearby areas
     * @param Double latitude
     * @param Double longitude
     */
    public void startNearbyAreas(Double latitude, Double longitude) {
        String url = "/area/list/" + Double.toString(latitude) + "," + Double.toString(longitude);
        
        Uri uri = buildCwApiUri(url);
        
        Bundle mParams = new Bundle();
        RESTClient client = new RESTClient(HTTPMethod.valueOf("GET"), uri, mParams);
        RESTClientResponse response = client.sendRequest();
        
        Bundle processParams = new Bundle();
        processParams.putParcelable("uri", AreasContract.NEARBY_URI);
        processAreasResponse(response, processParams);
    }
    
    /**
     * Start processing search
     * @param String search
     */
    public void startSearch(String search) {
        try {
            String encodedSearch = URLEncoder.encode(search, "UTF-8");
            String url = "/area/list/" + encodedSearch;
            Uri uri = buildCwApiUri(url);
            
            Bundle params = new Bundle();
            RESTClient client = new RESTClient(HTTPMethod.valueOf("GET"), uri, params);
            RESTClientResponse response = client.sendRequest();
            
            Bundle processParams = new Bundle();
            processParams.putParcelable("uri", AreasContract.SEARCH_URI);
            processParams.putString("search", search);
            processAreasResponse(response, processParams);
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "Unsupported encoding");
        }
        
        
    }
    
    /**
     * Start processing state areas
     * @param String stateCode
     */
    public void startStateAreas(String stateCode) {
        try {
            String encodedSearch = URLEncoder.encode(stateCode, "UTF-8");
            String url = "/area/list/" + encodedSearch;
            Uri uri = buildCwApiUri(url);
            
            Bundle params = new Bundle();
            RESTClient client = new RESTClient(HTTPMethod.valueOf("GET"), uri, params);
            RESTClientResponse response = client.sendRequest();
            
            Bundle processParams = new Bundle();
            processParams.putParcelable("uri", AreasContract.CONTENT_URI);
            processParams.putString("stateCode", stateCode);
            processAreasResponse(response, processParams);
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "Unsupported encoding");
        }
    }
    
    /**
     * Start processing states
     */
    public void startStates() {
        String url = "/state/list";
        Uri uri = buildCwApiUri(url);
        
        Bundle params = new Bundle();
        RESTClient client = new RESTClient(HTTPMethod.valueOf("GET"), uri, params);
        RESTClientResponse clientResponse = client.sendRequest();
        
        int code = clientResponse.getCode();
        String json = clientResponse.getData();
        
        if (code == 200 && !json.equals("")) {
            Gson gson = new Gson();
            CwApiStateListResponse response = gson.fromJson(json, CwApiStateListResponse.class);
            State[] states = response.getStates();
            
            Long timestamp = System.currentTimeMillis()/1000;
            
            // Save states to content provider
            for (int i = 0; i < states.length; i++) {
                ContentValues values = new ContentValues();
                values.put(StatesContract.Columns.STATE_CODE, states[i].getCode());
                values.put(StatesContract.Columns.NAME, states[i].getName());
                values.put(StatesContract.Columns.AREAS, states[i].getAreaCount());
                values.put(StatesContract.Columns.UPDATED, timestamp);
                mContext.getContentResolver().insert(
                        StatesContract.CONTENT_URI, values);
            }
        }
        
    }
    
    /**
     * Process areas response
     * @param RESTClientResponse response
     * @param Bundle processParams
     */
    private void processAreasResponse(RESTClientResponse response, Bundle processParams)
    {
        int code = response.getCode();
        String json = response.getData();
        
        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !json.equals("")) {
            processAreasJson(json, processParams);
        }
    }
    
    /**
     * Process areas JSON
     * @param String result
     * @param Bundle processParams
     * @return
     */
    private Area[] processAreasJson(String result, Bundle processParams)
    {
        Uri contentUri = processParams.getParcelable("uri");
        
        try {
            // Convert JSON into areas using GSON
            Gson gson = new Gson();
            Area[] areas = gson.fromJson(result, CwApiAreaListResponse.class).getAreas();
            
            String searchId = null;
            
            // Save search term
            if (contentUri.equals(AreasContract.SEARCH_URI)) {
                ContentValues values = new ContentValues();
                values.put("search", processParams.getString("search"));;
                Uri searchUri = mContext.getContentResolver().insert(AreasContract.SEARCH_URI, values);
                searchId = searchUri.getPathSegments().get(2);
            }
            
            // Save areas
            for (int i = 0; i < areas.length; i++) {
                if (contentUri.equals(AreasContract.NEARBY_URI)) {
                    areas[i].setNearby(i);
                }
                
                areas[i].save(mContext.getContentResolver());
                
                if (contentUri.equals(AreasContract.SEARCH_URI)) {
                    ContentValues sValues = new ContentValues();
                    sValues.put("search_id", searchId);
                    sValues.put("area_id", areas[i].getId());
                    mContext.getContentResolver().insert(AreasContract.SEARCH_AREA_URI, sValues);
                }
            }
            
            return areas;
            
        } catch (JsonParseException e) {
            Log.i(TAG, "An error occurred while retrieving area data");
            return new Area[0];
        }
        
    }
    
    /**
     * Get API key
     * @return String
     */
    private String getApiKey()
    {
        String apiKey = "android-";
        
        String androidId = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
        
        if (androidId == null) {
            androidId = "unknown";
        }
        
        apiKey += androidId;
        return apiKey;
    }
    
    /**
     * Format area list
     * @param Cursor cursor
     * @return String
     */
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
