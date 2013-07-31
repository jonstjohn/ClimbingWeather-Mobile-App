package com.climbingweather.cw;

public class CwApiHourlyResponse {
    
    // Status
    private String status;
    
    // Forecast hours
    private CwApiHourlyResult results;
    
    public CwApiHourlyResult getResult()
    {
        return results;
    }

}
