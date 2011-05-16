package com.climbingweather.cw;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;

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
        
        HttpToJson toJson = new HttpToJson();
        return toJson.getJsonFromUrl(absoluteUrl);
        
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
