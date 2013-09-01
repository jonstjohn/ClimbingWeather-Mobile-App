package com.climbingweather.cw;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ForecastDay {
    
    // Date YYYY-MM-DD
    private String d;
    
    // Formatted date
    private String dd;
    
    // Formatted day
    private String dy;
    
    // High temp
    private String hi;
    
    // Low temp
    private String l;
    
    // Precip day
    private String pd;
    
    // Precip night
    private String pn;
    
    // Symbol
    private String sy;
    
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
    
    // Weather
    private String w;
    
    // Symbol
    // Conditions
    private String c;
    
    private ArrayList<ForecastHour> hours;
    
    boolean hoursLoaded = false;
    
    public ForecastDay(String date, String high, String low, String precipDay, String precipNight, String symbol)
    {
        d = date;
        hi = high;
        l = low;
        pd = precipDay;
        pn = precipNight;
        sy = symbol;
    }
    
    public String toString()
    {
        return d + " H/l:" + hi + "/" + l;
    }
    
    public ForecastHour getHour(int position)
    {
        return hours.get(position);
    }
    
    public void markHoursLoaded()
    {
        hoursLoaded = true;
    }
    
    public boolean getHoursLoaded()
    {
        return hoursLoaded;
    }
    
    public boolean hasHours()
    {
        return hours != null;
    }
    
    public int getHourCount()
    {
        return hours.size();
    }
    
    public String getDate()
    {
        return d;
    }
    
    public void addHour(ForecastHour hour)
    {
        if (hours == null) {
            hours = new ArrayList<ForecastHour>();
        }
        hours.add(hour);
    }
    
    public String getSymbol()
    {
        return sy;
    }
    
    public String getDayOfWeek()
    {
        return dy;
    }
    
    public String getShortDate()
    {
        return dd;
    }
    
    public String getConditions()
    {
        return c;
    }

    // Get list row view
    public View getListRowView(View view, ViewGroup parent, LayoutInflater inflater, Context context)
    {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_daily, parent,false);
        }
        
        TextView dyTextView = (TextView) view.findViewById(R.id.dy);
        TextView ddTextView = (TextView) view.findViewById(R.id.dd);
        TextView conditionsTextView = (TextView) view.findViewById(R.id.conditions);
        ImageView symbolImageView = (ImageView) view.findViewById(R.id.sy);
        TextView hiTextView = (TextView) view.findViewById(R.id.hi);
        TextView lTextView = (TextView) view.findViewById(R.id.l);
        TextView pdTextView = (TextView) view.findViewById(R.id.pd);
        TextView pnTextView = (TextView) view.findViewById(R.id.pn);
        TextView wsTextView = (TextView) view.findViewById(R.id.ws);
        TextView hTextView = (TextView) view.findViewById(R.id.h);
        
        String symbol = sy.replace(".png", "");
        symbolImageView.setImageResource(context.getResources()
                .getIdentifier(symbol, "drawable", "com.climbingweather.cw"));
        
        dyTextView.setText(dy == null ? "--" : dy);
        ddTextView.setText(dd == null ? "--" : dd);
        
        conditionsTextView.setText(c);
        if (c.length() == 0) {
            conditionsTextView.setVisibility(View.GONE);
        } else {
            conditionsTextView.setVisibility(View.VISIBLE);
        }
        hiTextView.setText(hi == null ? "--" : hi + (char) 0x00B0);
        lTextView.setText(l == null ? "--" : l + (char) 0x00B0);
        pdTextView.setText(pd == null ? "--" : pd + "%");
        pnTextView.setText(pn == null ? "--" : pn + "%");
        wsTextView.setText(ws == null ? "--" : ws + " mph");
        hTextView.setText(h == null ? "--" : h + "%");
 
        return view;
    }
    
    public String getHigh()
    {
        return hi;
    }
    
    public Uri save(ContentResolver contentResolver, String areaId) {
        
        ContentValues values = new ContentValues();
        values.put(DailyContract.Columns.AREA_ID, areaId);
        values.put(DailyContract.Columns.DATE, d);
        values.put(DailyContract.Columns.HIGH, hi);
        values.put(DailyContract.Columns.LOW, l);
        values.put(DailyContract.Columns.PRECIP_DAY, pd);
        values.put(DailyContract.Columns.PRECIP_NIGHT, pn);
        values.put(DailyContract.Columns.RAIN_AMOUNT, r);
        values.put(DailyContract.Columns.RELATIVE_HUMIDITY, h);
        values.put(DailyContract.Columns.SNOW_AMOUNT, s);
        values.put(DailyContract.Columns.WEATHER, w);
        values.put(DailyContract.Columns.WEATHER_SYMBOL, sy);
        values.put(DailyContract.Columns.WIND_GUST, wg);
        values.put(DailyContract.Columns.WIND_SUSTAINED, ws);
        Uri uri = contentResolver.insert(
                Uri.parse(DailyContract.CONTENT_URI.toString() + "/" + areaId), values);
        return uri;
    }
}
