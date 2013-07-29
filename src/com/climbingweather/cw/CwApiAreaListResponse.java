package com.climbingweather.cw;

public class CwApiAreaListResponse {
    private String status;
    
    private Area[] results;
    
    public Area[] getAreas()
    {
        return results;
    }
}
