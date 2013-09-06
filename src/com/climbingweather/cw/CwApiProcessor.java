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
import android.widget.Toast;

public class CwApiProcessor {
    
    private static final String TAG = CwApiProcessor.class.getName();
    
    private Context mContext;
    
    public CwApiProcessor(Context context) {
        mContext = context;
    }
    
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
    
    private Uri buildUri(String url) {
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
     * Load favorite areas
     */
    public void startFavoriteAreas()
    {
        
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
            
            Uri uri = buildUri(url);
            
            Bundle mParams = new Bundle();
            
            RESTClient client = new RESTClient(HTTPMethod.valueOf("GET"), uri, mParams);
            RESTClientResponse response = client.sendRequest();
            
            Bundle processParams = new Bundle();
            processParams.putParcelable("uri", AreasContract.FAVORITES_URI);
            processAreasResponse(response, processParams);
        }
    }
    
    public void startNearbyAreas(Double latitude, Double longitude) {
        String url = "/area/list/" + Double.toString(latitude) + "," + Double.toString(longitude);
        
        Uri uri = buildUri(url);
        
        Bundle mParams = new Bundle();
        RESTClient client = new RESTClient(HTTPMethod.valueOf("GET"), uri, mParams);
        RESTClientResponse response = client.sendRequest();
        
        Bundle processParams = new Bundle();
        processParams.putParcelable("uri", AreasContract.NEARBY_URI);
        processAreasResponse(response, processParams);
    }
    
    public void startSearch(String search) {
        
        try {
            String encodedSearch = URLEncoder.encode(search, "UTF-8");
            String url = "/area/list/" + encodedSearch;
            Uri uri = buildUri(url);
            Log.i(TAG, uri.toString());
            
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
    
    public void startStates() {
        String url = "/state/list";
        Uri uri = buildUri(url);
        
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
        
        CwDbHelper.dumpStates(mContext);
        
    }
    
    private void processAreasResponse(RESTClientResponse response, Bundle processParams)
    {
        int code = response.getCode();
        String json = response.getData();
        
        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !json.equals("")) {
            Log.i(TAG, "onLoadFinished() using service");
            processAreasJson(json, processParams);
        }
    }
    
    private Area[] processAreasJson(String result, Bundle processParams)
    {
        Uri contentUri = processParams.getParcelable("uri");
        
        try {
            Log.i(TAG, "processJson");
            Log.i(TAG, result);
            
            // Convert JSON into areas using GSON
            Gson gson = new Gson();
            Area[] areas = gson.fromJson(result, CwApiAreaListResponse.class).getAreas();
            
            Log.i(TAG, Integer.toString(areas.length));
            
            String searchId = null;
            
            // Save search term
            if (contentUri.equals(AreasContract.SEARCH_URI)) {
                ContentValues values = new ContentValues();
                values.put("search", processParams.getString("search"));;
                Uri searchUri = mContext.getContentResolver().insert(AreasContract.SEARCH_URI, values);
                searchId = searchUri.getPathSegments().get(2);
                Log.i(TAG, "Search id: " + searchId);
            }
            
            // Save areas
            for (int i = 0; i < areas.length; i++) {
                //Log.i(TAG, "Loader id: " + Integer.toString(getId()));
                if (contentUri.equals(AreasContract.NEARBY_URI)) {
                    areas[i].setNearby(i);
                }
                
                areas[i].save(mContext.getContentResolver());
                
                if (contentUri.equals(AreasContract.SEARCH_URI)) {
                    ContentValues sValues = new ContentValues();
                    sValues.put("search_id", searchId);
                    sValues.put("area_id", areas[i].getId());
                    Uri uri = mContext.getContentResolver().insert(AreasContract.SEARCH_AREA_URI, sValues);
                    Log.i(TAG, uri.toString());
                }
            }
            
            String projection[] = {"area._id AS area_id", "area.name", "d1.high AS d1_high",
                    "d1.wsym AS d1_wsym", "d2.high AS d2_high", "d2.wsym AS d2_wsym", "d3.high AS d3_high", "d3.wsym AS d3_wsym"};
            Cursor cursor = mContext.getContentResolver().query(
                    AreasContract.CONTENT_URI,
                    projection, null, null, null
            );
            Log.i(TAG, "Areas Result");
            Log.i(TAG, Integer.toString(cursor.getCount()));
            while (cursor.moveToNext()) {
                Log.i(TAG, formatAreaList(cursor));
                Log.i(TAG, cursor.getString(cursor.getColumnIndex("area_id")));
            }
             /*
            Log.i(TAG, "Daily rows:");
            Cursor c2 = getContentResolver().query(DailyContract.CONTENT_URI, null, null, null, null);
            while (c2.moveToNext()) {
                Log.i(TAG, c2.getString(c2.getColumnIndex(DailyContract.Columns.ID)));
                Log.i(TAG, c2.getString(c2.getColumnIndex(DailyContract.Columns.DATE)));
                Log.i(TAG, c2.getString(c2.getColumnIndex(DailyContract.Columns.AREA_ID)));
            }
            */
            
            
            return areas;
            
        } catch (JsonParseException e) {
            Log.i(TAG, "An error occurred while retrieving area data");
            return new Area[0];
        }
        
    }
    
    /**
     * Get API key
     * @return
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
