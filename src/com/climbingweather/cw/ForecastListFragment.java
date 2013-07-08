package com.climbingweather.cw;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.climbingweather.cw.StateListFragment.StateExpandableListAdapter;
import com.google.gson.Gson;

public class ForecastListFragment  extends ExpandableListFragment
{
    // Forecast day objects
    private ArrayList<ForecastDay> days = new ArrayList<ForecastDay>();
    
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
        
        super.onCreate(savedInstanceState);
        
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
        
        // Set header row text
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        TextView headerView = (TextView) inflater.inflate(R.layout.header_row, null);
        headerView.setText(name);
        lv.addHeaderView(headerView);
        
        new GetDaysJsonTask().execute("/api/area/daily/" + areaId);
          
        lv.setTextFilterEnabled(true);
        
        // Set on item click listener
        //lv.setOnChildClickListener(this);
    }
    /*
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        // use groupPosition and childPosition to locate the current item in the adapter
        ForecastHour hour = days.get(groupPosition).getHour(childPosition);
        Intent i = new Intent(getActivity(), AreaFragmentActivity.class);
        i.putExtra("areaId", Integer.valueOf(area.getId()).toString());
        i.putExtra("name", area.getName());
        startActivity(i);
        return true;
    }
    *
    */
    
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
        
        public void addDay(ForecastDay day)
        {
            days.add(day);
        }
        
        public Object getChild(int groupPosition, int childPosition) {
            if (!days.get(groupPosition).hasHours()) {
                return null;
            }

            return days.get(groupPosition).getHour(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            if (!days.get(groupPosition).hasHours()) {
                return 1;
            } else {
                return days.get(groupPosition).getHourCount();
            }
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 64);

            TextView textView = new TextView(ForecastListFragment.this.getActivity());
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
            
            // Get day

            
            /*
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_child, parent,false);
            }
     
            TextView textView = (TextView) convertView.findViewById(R.id.list_item_text_child);
            
            */
            
            if (days.get(groupPosition).hasHours()) {
                ForecastHour hour = days.get(groupPosition).getHour(childPosition);
                convertView = hour.getListRowView(convertView, parent, inflater, getActivity());
            } else {
                convertView = inflater.inflate(R.layout.list_item_child, parent,false);
                TextView textView = (TextView) convertView.findViewById(R.id.list_item_text_child);
                textView.setText("Loading areas ...");
            }
     
            //return the entire view
            return convertView;
            
        }

        public Object getGroup(int groupPosition) {
            return days.get(groupPosition);
        }

        public int getGroupCount() {
            return days.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            
            // Get day
            ForecastDay day = days.get(groupPosition);
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
            if (!days.get(groupPosition).getHoursLoaded()) {
                // Load async
                new GetHoursJsonTask(groupPosition).execute("/api/area/hourly/" + areaId);
            }
        }
        
        // Add hour to date
        public void addHourToDay(int dayPosition, ForecastHour hour)
        {
            days.get(dayPosition).addHour(hour);
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
            
              CwApi api = new CwApi(mContext);
              return api.getJson(args[0]);

        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            Log.i("CW", "Finishing JSON task " + result);
            try {
                
                // Convert result into JSONArray
                JSONArray json = new JSONObject(result).getJSONArray("f");
              
                // Setup adapter
                forecastAdapter = new ForecastExpandableListAdapter(mContext);
                
                Gson gson = new Gson();
                
                // Loop over JSONarray
                for (int i = 0; i < json.length(); i++) {
                    ForecastDay day = gson.fromJson(json.getJSONObject(i).toString(), ForecastDay.class);
                    forecastAdapter.addDay(day);
                }
                
                setListAdapter(forecastAdapter);
                lastUpdateMillis = System.currentTimeMillis();
              
            } catch (JSONException e) {
              
                Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
              
            }
        }
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetHoursJsonTask extends AsyncTask<String, Void, String> {
        
        private int dayPosition;
        
        public GetHoursJsonTask(int mDayPosition)
        {
            dayPosition = mDayPosition;
        }
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
            
              CwApi api = new CwApi(mContext);
              return api.getJson(args[0]);

        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            try {
                
                //Log.i("CW", result);
                
                Gson gson = new Gson();
                
                // Convert result into JSONArray
                JSONArray json = new JSONObject(result).getJSONArray("f");
                
                // Assume start with first day
                int dayPosition = 0;
                ForecastDay day = days.get(dayPosition);
              
                // Loop over JSONarray
                for (int i = 0; i < json.length(); i++) {
                    
                    ForecastHour hour = gson.fromJson(json.getJSONObject(i).toString(), ForecastHour.class);
                    Log.i("CW", hour.toString());
                    
                    // Look for day with same dow
                    while (!day.getDayOfWeek().equals(hour.getDayOfWeek()) && dayPosition < days.size() - 1) {
                        dayPosition++;
                        day = days.get(dayPosition);
                    }
                    
                    // Only add if we have a match
                    if (day.getDayOfWeek().equals(hour.getDayOfWeek())) {
                        forecastAdapter.addHourToDay(dayPosition, hour);
                    }
                    
                }
                
                // Mark all days as loaded
                for (int i = 0; i < days.size(); i++) {
                    days.get(i).markHoursLoaded();
                }
                
                forecastAdapter.notifyDataSetChanged();
                
                
              
            } catch (JSONException e) {
              
                Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
              
            }
        }
        
    }

}
