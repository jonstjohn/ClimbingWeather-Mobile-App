package com.climbingweather.cw;

import java.util.ArrayList;
import java.util.HashMap;

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
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.Gson;

public class StateListFragment extends ExpandableListFragment {
	
    // State objects
    private ArrayList<State> states = new ArrayList<State>();
    
    /**
     * Adapter
     */
    StateExpandableListAdapter stateAdapter;
    
    private Context mContext;
    
    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        mContext = getActivity();
        
        // setContentView(R.layout.list);
        //inflater.inflate(R.layout.list, null);
        
        return inflater.inflate(R.layout.list_expandable, null);
        
    }
    
    public void onActivityCreated (Bundle savedInstanceState) {
    	
    	super.onActivityCreated(savedInstanceState);

    	
    	ExpandableListView lv = getExpandableListView();
        
        // Set header row text
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        TextView headerView = (TextView) inflater.inflate(R.layout.header_row, null);
        headerView.setText("US States");
        lv.addHeaderView(headerView);
        
        if (stateAdapter == null) {
            new GetStatesJsonTask().execute("/api/state/list");
        }
          
        lv.setTextFilterEnabled(true);
        
        // Set on item click listener
        lv.setOnChildClickListener(this);
    }
    
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        // use groupPosition and childPosition to locate the current item in the adapter
        Area area = states.get(groupPosition).getArea(childPosition);
        Intent i = new Intent(getActivity(), AreaActivity.class);
        i.putExtra("areaId", Integer.valueOf(area.getId()).toString());
        i.putExtra("name", area.getName());
        startActivity(i);
        return true;
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
    public class StateExpandableListAdapter extends BaseExpandableListAdapter {
        
        private LayoutInflater inflater;
        
        public StateExpandableListAdapter(Context context)
        {
            inflater = LayoutInflater.from(context);
        }
        
        public void addState(State state)
        {
            states.add(state);
        }
        
        public Object getChild(int groupPosition, int childPosition) {
            if (!states.get(groupPosition).hasAreas()) {
                return null;
            }

            return states.get(groupPosition).getArea(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            if (!states.get(groupPosition).hasAreas()) {
                return 1;
            } else {
                return states.get(groupPosition).getAreaCount();
            }
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 64);

            TextView textView = new TextView(StateListFragment.this.getActivity());
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
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_child, parent,false);
            }
     
            TextView textView = (TextView) convertView.findViewById(R.id.list_item_text_child);
            
            if (states.get(groupPosition).hasAreas()) {
                textView.setText(states.get(groupPosition).getArea(childPosition).toString());
            } else {
                textView.setText("Loading areas ...");
            }
     
            //return the entire view
            return convertView;
        }

        public Object getGroup(int groupPosition) {
            return states.get(groupPosition);
        }

        public int getGroupCount() {
            return states.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_parent, parent,false);
            }
     
            TextView textView = (TextView) convertView.findViewById(R.id.list_item_text_view);
            
            String stateStr = states.get(groupPosition).getName();
            textView.setText(stateStr);
     
            return convertView;
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
            String stateCode = states.get(groupPosition).getCode();
            if (!states.get(groupPosition).hasAreas()) {
                // Load async
                new GetAreasJsonTask(groupPosition).execute("/api/state/area/" + stateCode + "?days=3");
            }
        }
        
        /**
         * Add areas to state
         * @param String stateCode
         * @param String areaId
         * @param HashMap<String, String> areaData
         */
        public void addAreaToState(int statePosition, Area area)
        {
            states.get(statePosition).addArea(area);
        }

    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetStatesJsonTask extends AsyncTask<String, Void, String> {
        
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
            Log.i("CW", "Finishing JSON task");
            try {
                
                // Convert result into JSONArray
                JSONArray json = new JSONArray(result);
              
                // Setup adapter
                stateAdapter = new StateExpandableListAdapter(mContext);
                
                // Loop over JSONarray
                for (int i = 0; i < json.length(); i++) {
                    JSONObject jsonState = json.getJSONObject(i);
                    stateAdapter.addState(new State(jsonState.getString("name"), jsonState.getString("code")));
                }
                
                setListAdapter(stateAdapter);
              
            } catch (JSONException e) {
              
                Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
              
            }
        }
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetAreasJsonTask extends AsyncTask<String, Void, String> {
        
        private int statePosition;
        
        public GetAreasJsonTask(int mStatePosition)
        {
            statePosition = mStatePosition;
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
                JSONArray json = new JSONArray(result);
              
                // Loop over JSONarray
                for (int i = 0; i < json.length(); i++) {
                    
                    Area a = gson.fromJson(json.getJSONObject(i).toString(), Area.class);
                    Log.i("CW", a.toString());
                    
                    stateAdapter.addAreaToState(statePosition, a);
                }
                
                stateAdapter.notifyDataSetChanged();
                
                
              
            } catch (JSONException e) {
              
                Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
              
            }
        }
    }

}
