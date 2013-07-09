package com.climbingweather.cw;

public class CwApiDailyResult {

    // Name
    private String n;
    
    // Forceast
    private ForecastDay[] f;
    
    public ForecastDay[] getForecastDays()
    {
        return f;
    }
    
    public String getName()
    {
        return n;
    }
    
}
