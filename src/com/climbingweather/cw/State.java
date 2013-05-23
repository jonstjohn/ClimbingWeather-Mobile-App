package com.climbingweather.cw;

import java.util.ArrayList;

public class State
{
    private String name;
    
    private String code;
    
    // States areas
    private ArrayList<Area> stateAreas;
    
    private int areas;
    
    public void addArea(Area area)
    {
        if (stateAreas == null) {
            stateAreas = new ArrayList<Area>();
        }
        stateAreas.add(area);
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
        return stateAreas.get(position);
    }
    
    public boolean hasAreas()
    {
        return stateAreas != null;
    }
    
    public int getAreaCount()
    {
        if (stateAreas == null) {
            return areas;
        } else {
            return stateAreas.size();
        }
    }
}
