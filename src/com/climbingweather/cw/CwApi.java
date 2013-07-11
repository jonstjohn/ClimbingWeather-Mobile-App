package com.climbingweather.cw;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
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
    private String mBaseUrl = "http://dev.climbingweather.com";
    
    // Version
    private String version;
    
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
            
                Log.i("CW", "File contents:");
                try {
                    Log.i("CW", cache.read(cacheFileName));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            
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

}
