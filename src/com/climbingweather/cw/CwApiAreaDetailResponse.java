package com.climbingweather.cw;

public class CwApiAreaDetailResponse {
    // Status
    private String status;
    
    // Forecast hours
    private Area results;
    
    public Area getArea()
    {
        return results;
    }
    
}
