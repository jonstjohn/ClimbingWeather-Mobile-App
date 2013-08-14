package com.climbingweather.cw;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

public class State
{
    private String name;
    
    private String code;
    
    // States areas
    private ArrayList<Area> stateAreas;
    
    private int areas;
    
    /**
     * Get instance from code
     * @param context
     * @param stateCode
     */
    public static State getInstanceFromCode(Context context, String stateCode)
    {
        String[] args = new String[1];
        args[0] = stateCode;
        
        Cursor cursor = context.getContentResolver().query(StatesContract.CONTENT_URI, null, StatesContract.Columns.STATE_CODE + "=?", args, null);
        cursor.moveToFirst();
        
        State state = new State(cursor.getString(cursor.getColumnIndex(StatesContract.Columns.NAME)), stateCode);
        state.areas = cursor.getInt(cursor.getColumnIndex(StatesContract.Columns.AREAS));
        
        return state;
        
    }
    
    public void addArea(Area area)
    {
        if (stateAreas == null) {
            stateAreas = new ArrayList<Area>();
        }
        stateAreas.add(area);
    }
    
    public void addAreas(Area[] areas)
    {
        if (stateAreas == null) {
            stateAreas = new ArrayList<Area>();
        }
        for (int i = 0; i < areas.length; i++) {
            addArea(areas[i]);
        }
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
