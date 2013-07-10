package com.climbingweather.cw;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class AreaMapFragment extends SherlockFragment
{
    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        Log.i("CW", "AreaMapFragment onCreateView()");
        super.onCreate(savedInstanceState);
        
        return inflater.inflate(R.layout.area_map, null);
        
    }
}
