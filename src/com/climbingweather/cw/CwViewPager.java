package com.climbingweather.cw;

import com.google.android.gms.maps.MapView;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class CwViewPager extends ViewPager {
    public CwViewPager(Context context) {
        super(context);
    }

    public CwViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        
        if(v instanceof MapView){
            return true;
        }
        
        // This one actually works with google maps v2
        if (v.getClass().getPackage().getName().startsWith("maps.")) {
            return true;
        }
        
        return super.canScroll(v, checkV, dx, x, y);
    }
}
