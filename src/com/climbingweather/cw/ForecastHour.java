package com.climbingweather.cw;

public class ForecastHour {

    // Day
    private String dy;
    
    // Time
    private String ti;
    
    // Temperature
    private String t;
    
    // Change of precip
    private String p;
    
    // Humidity
    private String h;
    
    // Rain amount
    private String r;
    
    // Snow amount
    private String s;
    
    // Wind speed
    private String ws;
    
    // Wind gust
    private String wg;
    
    // Weather description
    private String w;
    
    // Symbol
    private String sy;
    
    // Sky cover
    private String sk;
    
    // Conditions
    private String c;
    
    public String toString()
    {
        return dy + " " + ti + " " + t;
    }
    
    public String getDayOfWeek()
    {
        return dy;
    }
}
