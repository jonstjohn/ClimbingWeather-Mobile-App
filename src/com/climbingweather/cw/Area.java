package com.climbingweather.cw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Area
{
    // id
    private int id;
    
    // name
    private String name;
    
    // state
    private String state;
    
    // lat
    private String lat;
    
    // lon
    private String lon;
    
    // weather symbol
    private String wsym;
    
    // map icon
    private String icon;
    
    // url
    private String url;
    
    // Forecast days
    private ForecastDay[] f;
    
    public String toString()
    {
        String s = id + " " + name + " ";
        if (f != null) {
            for (int i = 0; i < f.length; i++) {
                s = s + " " + f[i].toString();
            }
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
    
    public String getState()
    {
        return state;
    }
    
    public ForecastDay getDay(int i)
    {
        return f[i];
    }
    
    // Get list row view
    public View getListRowView(View view, ViewGroup parent, Context context)
    {            
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.list_item_area, parent,false);
        }

        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        LinearLayout areaLinearLayout = (LinearLayout) view.findViewById(R.id.area);
        ImageView loadingImageView = (ImageView) view.findViewById(R.id.loading);
        TextView day1TextView = (TextView) view.findViewById(R.id.d1);
        TextView day2TextView = (TextView) view.findViewById(R.id.d2);
        TextView day3TextView = (TextView) view.findViewById(R.id.d3);
    
        nameTextView.setText(getName());
        
        String symbol1 = getDay(0).getSymbol().replace(".png", "");
        day1TextView.setCompoundDrawablesWithIntrinsicBounds(0, context.getResources().getIdentifier(symbol1, "drawable", "com.climbingweather.cw"), 0, 0);
        String high1 = getDay(0).getHigh();
        day1TextView.setText(high1 == null ? "--" : high1 + (char) 0x00B0);
        
        String symbol2 = getDay(1).getSymbol().replace(".png", "");
        day2TextView.setCompoundDrawablesWithIntrinsicBounds(0, context.getResources().getIdentifier(symbol2, "drawable", "com.climbingweather.cw"), 0, 0);
        String high2 = getDay(1).getHigh();
        day2TextView.setText(high2 == null ? "--" : high2 + (char) 0x00B0);
        
        String symbol3 = getDay(2).getSymbol().replace(".png", "");
        day3TextView.setCompoundDrawablesWithIntrinsicBounds(0, context.getResources().getIdentifier(symbol3, "drawable", "com.climbingweather.cw"), 0, 0);
        String high3 = getDay(2).getHigh();
        day3TextView.setText(high3 == null ? "--" : high3 + (char) 0x00B0);
        
        areaLinearLayout.setVisibility(View.VISIBLE);
        loadingImageView.setVisibility(View.INVISIBLE);
        
        return view;
        
    }
}
