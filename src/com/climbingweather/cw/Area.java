package com.climbingweather.cw;

public class Area
{
    // id
    private int id;
    
    // name
    private String name;
    
    // Forecast days
    private ForecastDay[] f;
    
    public String toString()
    {
        String s = id + " " + name + " ";
        for (int i = 0; i < f.length; i++) {
            s = s + " " + f[i].toString();
        }
        return s;
    }
    
    public int getId()
    {
        return id;
    }
    
    public String getName()
    {
        return name;
    }
}
