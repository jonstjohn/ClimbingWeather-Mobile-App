package com.climbingweather.cw;

import java.util.ArrayList;

public class State
{
    private String name;
    
    private String code;
    
    private ArrayList<Area> areas;
    
    public void addArea(Area area)
    {
        if (areas == null) {
            areas = new ArrayList<Area>();
        }
        areas.add(area);
    }
    
    public State(String pName, String pCode)
    {
        name = pName;
        code = pCode;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getCode()
    {
        return code;
    }
    
    public Area getArea(int position)
    {
        return areas.get(position);
    }
    
    public boolean hasAreas()
    {
        return areas != null;
    }
    
    public int getAreaCount()
    {
        return areas.size();
    }
}
