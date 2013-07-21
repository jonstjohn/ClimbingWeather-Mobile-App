package com.climbingweather.cw;

public class CwApiAverageResponse {
    // Status
    private String status;
    
    // Forecast hours
    private AreaAverage results;
    
    public AreaAverage getAreaAverage()
    {
        return results;
    }
    
    public String toString()
    {
        if (results == null) {
            return status;
        } else {
            return status + " " + results.toString();
        }
    }
}
