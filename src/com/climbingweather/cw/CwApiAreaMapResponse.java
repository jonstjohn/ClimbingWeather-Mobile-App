package com.climbingweather.cw;

public class CwApiAreaMapResponse {

    // Status
    private String status;
    
    // Forecast hours
    private Area[] results;
    
    public Area[] getAreas()
    {
        return results;
    }
    
}
