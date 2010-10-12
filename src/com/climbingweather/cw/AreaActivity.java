package com.climbingweather.cw;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class AreaActivity extends TabActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab);

        //Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
        Bundle extras = getIntent().getExtras();
        String areaId = extras.getString("areaId");

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, DailyActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout dailyTab = (LinearLayout)inflater.inflate(R.layout.tab_content, tabHost, false);
        TextView dailyText = (TextView)dailyTab.findViewById(R.id.name);
        dailyText.setText("Daily");
        dailyText.setSelected(true);
        spec = tabHost.newTabSpec("daily").setIndicator(dailyTab).setContent(intent);
        intent.putExtra("areaId", areaId);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, HourlyActivity.class);
        LinearLayout hourlyTab = (LinearLayout)inflater.inflate(R.layout.tab_content, tabHost, false);
        TextView hourlyText = (TextView)hourlyTab.findViewById(R.id.name);
        hourlyText.setText("Hourly");
        spec = tabHost.newTabSpec("hourly").setIndicator(hourlyTab).setContent(intent);
        intent.putExtra("areaId", areaId);
        intent.putExtra("dayIndex", "0");
        tabHost.addTab(spec);
        
        // Map
        intent = new Intent().setClass(this, AreaMapActivity.class);
        LinearLayout mapTab = (LinearLayout)inflater.inflate(R.layout.tab_content, tabHost, false);
        TextView mapText = (TextView)mapTab.findViewById(R.id.name);
        mapText.setText("Map");
        spec = tabHost.newTabSpec("map").setIndicator(mapTab).setContent(intent);
        intent.putExtra("areaId", areaId);
        //intent.putExtra("dayIndex", "0");
        tabHost.addTab(spec);

        //TextView areaView = (TextView)inflater.inflate(R.layout.tab_content, tabHost, false);
        TextView areaView = (TextView)tabHost.findViewById(R.id.areaName);
        areaView.setText(extras.getString("name"));
        
        /*
        intent = new Intent().setClass(this, SongsActivity.class);
        spec = tabHost.newTabSpec("songs").setIndicator("Songs",
                          res.getDrawable(R.drawable.ic_tab_songs))
                      .setContent(intent);
        tabHost.addTab(spec);
		*/
        tabHost.setCurrentTab(0);
    }
}
