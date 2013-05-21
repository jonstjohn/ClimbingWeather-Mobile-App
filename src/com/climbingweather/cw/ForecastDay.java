package com.climbingweather.cw;

public class ForecastDay {
    
    // Date YYYY-MM-DD
    private String d;
    
    // High temp
    private String hi;
    
    // Low temp
    private String l;
    
    // Precip day
    private String pd;
    
    // Precip night
    private String pn;
    
    // Symbol
    private String sy;
    
    public String toString()
    {
        return d + " H/l:" + hi + "/" + l;
    }

}
