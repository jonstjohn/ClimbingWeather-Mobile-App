package com.climbingweather.cw;

public class CwApiHourlyResult {

    // Name
    private String n;
    
    // Forecast hours
    private ForecastHour[] f;
    
    public ForecastHour[] getForecastHours()
    {
        return f;
    }
    
}
