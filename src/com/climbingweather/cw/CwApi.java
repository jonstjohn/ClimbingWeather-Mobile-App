package com.climbingweather.cw;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.util.Log;
import android.widget.Toast;

/**
 * Handle ClimbingWeather.com api requests
 */
public class CwApi {
    
    /**
     * Android activity context
     */
    private Context mContext;
    
    /**
     * Base URL
     */
    private String mBaseUrl = "http://api.climbingweather.com/v2";
    
    // Version
    private String version;
    
    private static final String ARGS_URI    = "com.climbingweather.cw.ARGS_URI";
    private static final String ARGS_PARAMS = "com.climbingweather.cw.ARGS_PARAMS";
    
    private static final String TAG = CwApi.class.getName();
    
    /**
     * Loader for NEARBY areas
     */
    private static final int LOADER_AREA_NEARBY = 1;
    
    /**
     * Loader for FAVORITE areas
     */
    private static final int LOADER_AREA_FAVORITE = 2;
    
    /**
     * Loader for SEARCH areas
     */
    private static final int LOADER_AREA_SEARCH = 3;
    
    /**
     * Constructor
     * @param context
     */
    public CwApi(Context context)
    {
        mContext = context;
        version = "1.0";
    }
    
    /**
     * Constructor
     * @param context
     */
    public CwApi(Context context, String version)
    {
        mContext = context;
        this.version = version;
    }
    
    /**
     * Get JSON from API
     * @param url
     * @return
     */
    public String getJson(String url)
    {
        String apiKey = getApiKey();
        
        String divider = url.contains("?") ? "&" : "?";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String absoluteUrl = mBaseUrl + url + divider + "apiKey=" + apiKey + "&tempUnit=" + prefs.getString("tempUnit", "f") +
            "&device=android&version=" + version;
        
        Log.i("CW", absoluteUrl);
        String cacheFileName = url.replace("/",  "_") + "-" + prefs.getString("tempUnit", "f");
        
        CwCache cache = new CwCache(mContext);
        
        // Check to see if cache is enabled and content is fresh
        if (cache.isEnabled() && cache.isFresh(cacheFileName)) {
            
            Log.i("CW", "Using cached file " + cacheFileName);
            try {
                return cache.read(cacheFileName);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "";
            }
            
        } else {
        
            HttpToJson toJson = new HttpToJson();
            String json = toJson.getJsonFromUrl(absoluteUrl);
            
            cache.write(cacheFileName, json);
            
            return json;
            
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
    
    ///  LoaderCallbacks<D> callback
    public void initLoader(LoaderCallbacks<CwApiLoaderResult> callback, String url, Bundle params, int loaderId, boolean forceReload)
    {
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
        
        Uri uri = uriBuilder.build();
        
        Log.i(TAG, uri.toString());
        
        String cacheFileName = uri.toString().replace("/",  "_").replace(":", "_");
        
        Log.i(TAG, cacheFileName);
        
        Bundle args = new Bundle();
        args.putParcelable(ARGS_URI, uri);
        args.putParcelable(ARGS_PARAMS, new Bundle());
        
        CwCache cache = new CwCache(mContext);
        
        // Check to see if cache is enabled and content is fresh
        if (cache.isEnabled() && cache.isFresh(cacheFileName) && !forceReload) {
            
            Log.i(TAG, "Using cached file " + cacheFileName);
            ((SherlockFragmentActivity) mContext).getSupportLoaderManager().initLoader(loaderId, args, callback);
            
        } else {
        
            Log.i(TAG, "Cache not fresh, reloading");
            ((SherlockFragmentActivity) mContext).getSupportLoaderManager().restartLoader(loaderId, args, callback);
            cache.write(cacheFileName, "1");
            
        }
        
    }
    
    /**
     * Load nearby areas
     * @param callback
     * @param latitude
     * @param longitude
     * @param forceReload
     */
    public void loadNearbyAreas(LoaderCallbacks<CwApiLoaderResult> callback, Double latitude, Double longitude, boolean forceReload)
    {
        String url = "/area/list/" + Double.toString(latitude) + "," + Double.toString(longitude);
        Bundle params = new Bundle();
        params.putString("days", "3");
        initLoader(callback, url, params, LOADER_AREA_NEARBY, forceReload);
    }
    
    /**
     * Load favorite areas
     * @param callback
     * @param forceReload
     */
    public void loadFavoriteAreas(LoaderCallbacks<CwApiLoaderResult> callback, boolean forceReload)
    {
        
        // Get ids from content provider
        ArrayList<String> ids = new ArrayList<String>();
        Cursor cursor = mContext.getContentResolver().query(
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
            initLoader(callback, url, params, LOADER_AREA_FAVORITE, forceReload);
        }
    }
    
    /**
     * Load search areas
     * @param callback
     * @param search
     * @param forceReload
     */
    public void loadSearchAreas(LoaderCallbacks<CwApiLoaderResult> callback, String search, boolean forceReload)
    {
        try {
            String encodedSearch = URLEncoder.encode(search, "UTF-8");
            String url = "/area/list/" + encodedSearch;
            Bundle params = new Bundle();
            params.putString("days", "3");
            initLoader(callback, url, params, LOADER_AREA_SEARCH, true);
            Log.i("CW", url);
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(mContext, "An error occurred while performing search", Toast.LENGTH_SHORT).show();
        }
    }

}
