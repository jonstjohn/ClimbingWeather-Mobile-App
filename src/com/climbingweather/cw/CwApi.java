package com.climbingweather.cw;

import java.io.IOException;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

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
    private String mBaseUrl = "http://api.climbingweather.com";
    
    // Version
    private String version;
    
    private static final String ARGS_URI    = "com.climbingweather.cw.ARGS_URI";
    private static final String ARGS_PARAMS = "com.climbingweather.cw.ARGS_PARAMS";
    
    private static final String TAG = CwApi.class.getName();
    
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
            
                /*
                //Log.i("CW", "File contents:");
                try {
                    Log.i("CW", cache.read(cacheFileName));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                */
            
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
    public void initLoader(AreaListFragment areaListFragment, String url, Bundle params, int loaderId)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        
        Uri.Builder uriBuilder = new Uri.Builder()
        .scheme("http")
        .authority("dev.climbingweather.com")
        .path(url)
        .appendQueryParameter("apiKey", getApiKey())
        .appendQueryParameter("tempUnit", prefs.getString("tempUnit", "f"))
        .appendQueryParameter("version", "2.0");
        
        
        for (String key : params.keySet()) {
            uriBuilder.appendQueryParameter(key, params.getString(key));
        }
        
        Uri uri = uriBuilder.build();
        
        Log.i(TAG, uri.toString());
        Bundle args = new Bundle();
        args.putParcelable(ARGS_URI, uri);
        args.putParcelable(ARGS_PARAMS, new Bundle());
        ((SherlockFragmentActivity) mContext).getSupportLoaderManager().initLoader(loaderId, args, areaListFragment);    
    }
    


}
