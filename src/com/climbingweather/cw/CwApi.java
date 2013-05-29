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
    private String mBaseUrl = "http://api.climbingweather.com";
    
    /**
     * Constructor
     * @param context
     */
    public CwApi(Context context)
    {
        mContext = context;
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
            "&device=android&version=1.0";
        
        String cacheFileName = url.replace("/",  "_") + "-" + prefs.getString("tempUnit", "f");
        
        CwCache cache = new CwCache(mContext);
        
        // Cache for 60 seconds
        if (cache.isFresh(cacheFileName)) {
        //if (file.exists() && file.lastModified() / 1000L > (System.currentTimeMillis() / 1000L - cacheSeconds)) {
            
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
