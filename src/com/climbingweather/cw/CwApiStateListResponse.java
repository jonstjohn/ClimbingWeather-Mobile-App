package com.climbingweather.cw;

public class CwApiStateListResponse {
    private String status;
    
    private State[] results;
    
    public State[] getStates()
    {
        return results;
    }
}