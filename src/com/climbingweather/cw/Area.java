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
        ImageView day1ImageView = (ImageView) view.findViewById(R.id.d1);
        ImageView day2ImageView = (ImageView) view.findViewById(R.id.d2);
        ImageView day3ImageView = (ImageView) view.findViewById(R.id.d3);
    
        nameTextView.setText(getName());
        
        String symbol1 = getDay(0).getSymbol().replace(".png", "");
        day1ImageView.setImageResource(context.getResources().getIdentifier(symbol1, "drawable", "com.climbingweather.cw"));
        
        String symbol2 = getDay(1).getSymbol().replace(".png", "");
        day2ImageView.setImageResource(context.getResources().getIdentifier(symbol2, "drawable", "com.climbingweather.cw"));
        
        String symbol3 = getDay(2).getSymbol().replace(".png", "");
        day3ImageView.setImageResource(context.getResources().getIdentifier(symbol3, "drawable", "com.climbingweather.cw"));
        
        areaLinearLayout.setVisibility(View.VISIBLE);
        loadingImageView.setVisibility(View.INVISIBLE);
        
        return view;
        
    }
}
