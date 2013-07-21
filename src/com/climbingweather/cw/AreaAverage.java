package com.climbingweather.cw;

public class AreaAverage
{
    // clim81 station name
    private String name;
    
    // clim81 elevation
    private int elevation;
    
    // clim81 elevation difference
    private int elevation_diff;
    
    // clim81 elevation dir
    private String elevation_dir;
    
    // average data
    private AreaAverageData data;
    
    public AreaAverageData getAreaAverageData()
    {
        return data;
    }
    
    public String toString()
    {
        return name + " " + Integer.toString(elevation) + " ";
    }
}
