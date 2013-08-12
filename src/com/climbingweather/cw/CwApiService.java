package com.climbingweather.cw;

import android.app.IntentService;
import android.content.Intent;

public class CwApiService extends IntentService {
    
    public CwApiService() {
        super("CwApiService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.log("Handling intent");
    }

}
