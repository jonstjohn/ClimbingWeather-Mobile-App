package com.climbingweather.cw;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ForecastHour {

    // Day
    private String dy;
    
    // Time
    private String ti;
    
    // Temperature
    private String t;
    
    // Chance of precip
    private String p;
    
    // Humidity
    private String h;
    
    // Rain amount
    private String r;
    
    // Snow amount
    private String s;
    
    // Wind speed
    private String ws;
    
    // Wind gust
    private String wg;
    
    // Weather description
    private String w;
    
    // Symbol
    private String sy;
    
    // Sky cover
    private String sk;
    
    // Conditions
    private String c;
    
    public String toString()
    {
        return dy + " " + ti + " " + t;
    }
    
    public String getDayOfWeek()
    {
        return dy;
    }
    
    // Get list row view
    public View getListRowView(View view, ViewGroup parent, LayoutInflater inflater, Context context)
    {
        if (view == null || view.getId() != R.layout.list_item_hourly) {
            view = inflater.inflate(R.layout.list_item_hourly, parent,false);
        }
        
        TextView tiTextView = (TextView) view.findViewById(R.id.ti);
        TextView conditionsTextView = (TextView) view.findViewById(R.id.conditions);
        ImageView symbolImageView = (ImageView) view.findViewById(R.id.sy);
        TextView tTextView = (TextView) view.findViewById(R.id.t);
        TextView pTextView = (TextView) view.findViewById(R.id.p);
        TextView wsTextView = (TextView) view.findViewById(R.id.ws);
        TextView hTextView = (TextView) view.findViewById(R.id.h);
        
        String symbol = sy.replace(".png", "");
        symbolImageView.setImageResource(context.getResources()
            .getIdentifier(symbol, "drawable", "com.climbingweather.cw"));
        
        tiTextView.setText(ti == null ? "--" : ti);
        
        conditionsTextView.setText(c);
        if (c.length() == 0) {
            conditionsTextView.setVisibility(View.GONE);
        } else {
            conditionsTextView.setVisibility(View.VISIBLE);
        }
        tTextView.setText(t == null ? "--" : t + (char) 0x00B0);
        pTextView.setText(p == null ? "--" : p + "%");
        wsTextView.setText(ws == null ? "--" : ws + " mph");
        hTextView.setText(h == null ? "--" : h + "%");
 
        return view;
    }
}