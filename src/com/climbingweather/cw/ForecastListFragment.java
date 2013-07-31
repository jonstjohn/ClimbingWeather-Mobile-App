package com.climbingweather.cw;

import java.util.Arrays;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class ForecastListFragment  extends ExpandableListFragment
{
    // Forecast day objects
    private ForecastDay[] days;
    
    // Forecast hours
    private ForecastHour[] hours;
    
    // Adapter
    ForecastExpandableListAdapter forecastAdapter;
    
    // Context
    private Context mContext;
    
    // Area id
    private String areaId;
    
    // Area name
    private String name;
    
    private long lastUpdateMillis = 0L;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        super.onCreateView(inflater, container, savedInstanceState);
        
        mContext = getActivity();
        
        setHasOptionsMenu(true);
        
        return inflater.inflate(R.layout.list_expandable, null);
        
    }
    
    
    public void onActivityCreated (Bundle savedInstanceState) {
        
        super.onActivityCreated(savedInstanceState);

        Bundle extras = getActivity().getIntent().getExtras();
        areaId = extras.getString("areaId");
        name = extras.getString("name");
        
        ExpandableListView lv = getExpandableListView();
        
        new GetDaysJsonTask().execute("/api/area/daily/" + areaId);
          
        lv.setTextFilterEnabled(true);
        
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        if (!isFresh()) {
            new GetDaysJsonTask().execute("/api/area/daily/" + areaId);
        }
    }
    
    // Check to see if data is fresh
    private boolean isFresh()
    {
        return lastUpdateMillis > System.currentTimeMillis() - CwCache.cacheMillis; 
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        setListAdapter(null);
    }
    
    
    /**
     * State expandable list adapter
     */
    public class ForecastExpandableListAdapter extends BaseExpandableListAdapter {
        
        private LayoutInflater inflater;
        
        public ForecastExpandableListAdapter(Context context)
        {
            inflater = LayoutInflater.from(context);
        }
        
        public Object getChild(int groupPosition, int childPosition) {
            if (!days[groupPosition].hasHours()) {
                return null;
            }

            return days[groupPosition].getHour(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            if (!days[groupPosition].hasHours()) {
                return 1;
            } else {
                return days[groupPosition].getHourCount();
            }
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 64);

            TextView textView = new TextView(ForecastListFragment.this.getSherlockActivity());
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent)
        {
            if (days[groupPosition].hasHours()) {
                ForecastHour hour = days[groupPosition].getHour(childPosition);
                convertView = hour.getListRowView(convertView, parent, inflater, getActivity());
            } else {
                convertView = inflater.inflate(R.layout.list_item_child, parent,false);
                TextView textView = (TextView) convertView.findViewById(R.id.list_item_text_child);
                textView.setTextSize(12);
                if (days[groupPosition].getHoursLoaded()) {
                    textView.setText("No hourly data available");
                } else {
                    textView.setText("");
                }
            }
     
            //return the entire view
            return convertView;
            
        }

        public Object getGroup(int groupPosition) {
            return days[groupPosition];
        }

        public int getGroupCount() {
            return days.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            
            // Get day
            ForecastDay day = days[groupPosition];
            return day.getListRowView(convertView, parent, inflater, getActivity());
            
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }
        
        // On group expanded
        public void onGroupExpanded (int groupPosition)
        {
            // Check for state areas
            if (!days[groupPosition].getHoursLoaded()) {
                // Load async
                new GetHoursJsonTask().execute("/api/area/hourly/" + areaId);
            }
        }
        
        // Add hour to date
        public void addHourToDay(int dayPosition, ForecastHour hour)
        {
            days[dayPosition].addHour(hour);
        }
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetDaysJsonTask extends AsyncTask<String, Void, String> {
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
            
              CwApi api = new CwApi(mContext, "2.0");
              return api.getJson(args[0]);

        }
        
        protected void onPreExecute() {
            Logger.log(ForecastListFragment.this.getSherlockActivity().toString());
            ForecastListFragment.this.getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.TRUE); 
        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            if (ForecastListFragment.this != null && ForecastListFragment.this.getSherlockActivity() != null) {
                ForecastListFragment.this.getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.FALSE); 
                Log.i("CW", "Finishing JSON task " + result);
                if (days != null) {
                    Arrays.fill(days, null);
                }
                if (hours != null) {
                    Arrays.fill(hours, null);
                }
                processJson(result);
            }
        }
        
        private void processJson(String result)
        {
            try {
                Gson gson = new Gson();
                CwApiDailyResponse apiResponse = gson.fromJson(result,  CwApiDailyResponse.class);

                days = apiResponse.getResult().getForecastDays();
                
                forecastAdapter = new ForecastExpandableListAdapter(mContext);
                setListAdapter(forecastAdapter);
                
                lastUpdateMillis = System.currentTimeMillis();
            } catch (JsonParseException e) {
                Toast.makeText(mContext, "An error occurred while retrieving forecast data", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetHoursJsonTask extends AsyncTask<String, Void, String> {
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
            
              CwApi api = new CwApi(mContext, "2.0");
              return api.getJson(args[0]);

        }
        
        protected void onPreExecute() {
            ForecastListFragment.this.getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.TRUE); 
        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            ForecastListFragment.this.getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.FALSE); 
            
            if (hours != null) {
                Arrays.fill(hours, null);
            }
            processJson(result);
        }
        
        private void processJson(String result)
        {
            try {
                Gson gson = new Gson();
                CwApiHourlyResponse apiResponse = gson.fromJson(result,  CwApiHourlyResponse.class);

                hours = apiResponse.getResult().getForecastHours();
                
                // Assume start with first day
                int dayPosition = 0;
                ForecastDay day = days[dayPosition];
                
                for (int i = 0; i < hours.length; i++) {
                    // Look for day with same dow
                    while (!day.getDayOfWeek().equals(hours[i].getDayOfWeek()) && dayPosition < days.length - 1) {
                        dayPosition++;
                        day = days[dayPosition];
                    }
                    
                    // Only add if we have a match
                    if (day.getDayOfWeek().equals(hours[i].getDayOfWeek())) {
                        forecastAdapter.addHourToDay(dayPosition, hours[i]);
                    }
                }
                
                // Mark all days as loaded
                for (int i = 0; i < days.length; i++) {
                    days[i].markHoursLoaded();
                }
                
                forecastAdapter.notifyDataSetChanged();
                
                //lastUpdateMillis = System.currentTimeMillis();
            } catch (JsonParseException e) {
                Toast.makeText(mContext, "An error occurred while retrieving forecast data", Toast.LENGTH_SHORT).show();
            }
            
        }
        
    }

}
