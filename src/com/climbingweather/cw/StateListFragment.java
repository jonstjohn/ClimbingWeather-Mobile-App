package com.climbingweather.cw;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class StateListFragment extends ExpandableListFragment implements DataFragmentInterface {
	
    // State objects
    private ArrayList<State> states = new ArrayList<State>();
    
    private long lastUpdateMillis = 0L;
    
    private View view;
    
    private static final String TAG = StateListFragment.class.getName();
    
    /**
     * Adapter
     */
    StateExpandableListAdapter stateAdapter;
    
    private Context mContext;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
      
      Cursor cursor = getActivity().getContentResolver().query(StatesContract.CONTENT_URI, null, null, null, null);
      while (cursor.moveToNext()) {
          Logger.log(cursor.getString(cursor.getColumnIndex(StatesContract.Columns.NAME)));
      }
      
    }
    
    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        Log.i("CW", "StateListFragment onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);
        
        mContext = getActivity();
        
        view = inflater.inflate(R.layout.list_expandable, null);
        
        return view;
        
    }
    
    public void onActivityCreated (Bundle savedInstanceState) {
    	
        Log.i("CW", "StateListFragment onActivityCreated()");
    	super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        Logger.log("StateListFragment userVisibleHint " + Boolean.toString(visible));
        if (visible && !isFresh()) {
            Log.i("CW", "Visible and stale");
            loadStates();
        }
    }
    
    public void loadStates()
    {
        CwApiServiceHelper.getInstance().startStates(getActivity());
        Logger.log("loadStates()");
        if (view != null) {
            Logger.log("View is not null");
            ExpandableListView lv = getExpandableListView();
            new GetStatesJsonTask(this).execute("/state/list");
            
            // Set on item click listener
            lv.setOnChildClickListener(this);
        } else {
            Logger.log("View is null");
        }
    }
    
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        // use groupPosition and childPosition to locate the current item in the adapter
        Log.i(TAG, "Child clicked");
        Area area = states.get(groupPosition).getArea(childPosition);
        Intent i = new Intent(getActivity(), AreaFragmentActivity.class);
        i.putExtra("areaId", Integer.valueOf(area.getId()).toString());
        i.putExtra("name", area.getName());
        startActivity(i);
        return true;
    }
    
    /**
     * On destroy activity
     */
    public void onDestroy()
    {
        Log.i("CW", "StateListFragment onDestroy()");
        super.onDestroy();
        view = null;
    }
    
    /**
     * On start activity
     */
    public void onStart()
    {
        Log.i("CW", "StateListFragment onStart()");
        loadStates();
        super.onStart();
        ((CwApplication) this.getActivity().getApplication()).getGaTracker().sendView("/byState");
        
    }
    
    /**
     * On stop activity
     */
    public void onStop()
    {
        Log.i("CW", "StateListFragment onStop()");
        super.onStop();
    }
    
    /**
     * On pause activity
     */
    public void onPause()
    {
        Log.i("CW", "StateListFragment onPause()");
        super.onPause();
    }
    
    /**
     * On resume activity
     */
    public void onResume()
    {
        Log.i("CW", "StateListFragment onResume()");
        super.onResume();
    }
  
    
    @Override
    public void onDestroyView()
    {
    	super.onDestroyView();
    	Log.i("CW", "StateListFragment onDestroyView()");
    	setListAdapter(null);
    	lastUpdateMillis = 0L;
    	view = null;
    }
    
    /**
     * State expandable list adapter
     */
    public class StateExpandableListAdapter extends BaseExpandableListAdapter {
        
        private LayoutInflater inflater;
        
        private Context context;
        
        private Cursor mCursor;
        
        public StateExpandableListAdapter(Context context)
        {
            inflater = LayoutInflater.from(context);
            this.context = context;
            mCursor = getActivity().getContentResolver().query(
                    StatesContract.CONTENT_URI, null, null, null, "NAME DESC");
        }
        
        public void addState(State state)
        {
            states.add(state);
        }
        
        public void addStates(State[] addStates)
        {
            for (int i = 0; i < addStates.length; i++) {
                states.add(addStates[i]);
            }
        }
        
        public void removeAllStates()
        {
            Log.i("CW", "Remove all states");
            states.clear();
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
            
            // TODO
            //mCursor.moveToPosition(groupPosition);
            //State state = State.getInstanceFromCode(getActivity(), mCursor.getString(mCursor.getColumnIndex(StatesContract.Columns.STATE_CODE)));
            
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
            State state = states.get(groupPosition);
            
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_area, parent,false);
            }
     
            TextView nameTextView = (TextView) convertView.findViewById(R.id.name);
            LinearLayout areaLinearLayout = (LinearLayout) convertView.findViewById(R.id.area);
            ImageView loadingImageView = (ImageView) convertView.findViewById(R.id.loading);
            
            if (state.hasAreas()) {
                
                Area area = state.getArea(childPosition);
                convertView = area.getListRowView(convertView, parent, this.context);
                TextView stateTextView = (TextView) convertView.findViewById(R.id.state);
                stateTextView.setText(states.get(groupPosition).getName());
                
                areaLinearLayout.setVisibility(View.VISIBLE);
                loadingImageView.setVisibility(View.INVISIBLE);
                
            } else {
                nameTextView.setText("Loading areas ...");
                areaLinearLayout.setVisibility(View.INVISIBLE);
                loadingImageView.setVisibility(View.VISIBLE);
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
                convertView = inflater.inflate(R.layout.list_item_state, parent,false);
            }
     
            mCursor.moveToPosition(groupPosition);
            
            TextView nameTextView = (TextView) convertView.findViewById(R.id.name);
            //String stateStr = states.get(groupPosition).getName();
            String stateStr = mCursor.getString(mCursor.getColumnIndex(StatesContract.Columns.NAME));
            nameTextView.setText(stateStr);
            
            TextView areaCountTextView = (TextView) convertView.findViewById(R.id.areaCount);
            //String areaCount = Integer.toString(states.get(groupPosition).getAreaCount());
            String areaCount = mCursor.getString(mCursor.getColumnIndex(StatesContract.Columns.AREAS));
            areaCountTextView.setText(areaCount + " areas");
     
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
            mCursor.moveToPosition(groupPosition);
            String stateCode = mCursor.getString(mCursor.getColumnIndex(StatesContract.Columns.STATE_CODE));
            if (!states.get(groupPosition).hasAreas()) {
                // Load async
                new GetAreasJsonTask(groupPosition, context).execute("/state/area/" + stateCode + "?days=3");
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
        
        /**
         * Add areas to state
         * @param String stateCode
         * @param String areaId
         * @param HashMap<String, String> areaData
         */
        public void addAreasToState(int statePosition, Area[] areas)
        {
            states.get(statePosition).addAreas(areas);
        }

    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetStatesJsonTask extends AsyncTask<String, Void, String> {
        
        private ExpandableListFragment listFragment;
        
        public GetStatesJsonTask(ExpandableListFragment listFragment) {
            this.listFragment = listFragment;
        }
        
        protected void onPreExecute() {
            listFragment.getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE); 
        }
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
            
              CwApi api = new CwApi(mContext, "2.0");
              String result = api.getJson(args[0]);
              
              
              Gson gson = new Gson();
              CwApiStateListResponse response = gson.fromJson(result, CwApiStateListResponse.class);
              State[] states = response.getStates();
              
              Long timestamp = System.currentTimeMillis()/1000;
              
              // Save states to content provider
              for (int i = 0; i < states.length; i++) {
                  ContentValues values = new ContentValues();
                  values.put(StatesContract.Columns.STATE_CODE, states[i].getCode());
                  values.put(StatesContract.Columns.NAME, states[i].getName());
                  values.put(StatesContract.Columns.AREAS, states[i].getAreaCount());
                  values.put(StatesContract.Columns.UPDATED, timestamp);
                  getActivity().getContentResolver().insert(
                          StatesContract.CONTENT_URI, values);
              }
              
              return result;

        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            Log.i("CW", "Finishing StateListFragment JSON task");
            
            lastUpdateMillis = System.currentTimeMillis();

            // Setup adapter
            stateAdapter = new StateExpandableListAdapter(mContext);
            stateAdapter.removeAllStates();
            
            try {
                Gson gson = new Gson();
                CwApiStateListResponse response = gson.fromJson(result, CwApiStateListResponse.class);
                State[] states = response.getStates();
                
                /*
                Long timestamp = System.currentTimeMillis()/1000;
                
                // Save states to content provider
                for (int i = 0; i < states.length; i++) {
                    ContentValues values = new ContentValues();
                    values.put(StatesContract.Columns.STATE_CODE, states[i].getCode());
                    values.put(StatesContract.Columns.NAME, states[i].getName());
                    values.put(StatesContract.Columns.AREAS, states[i].getAreaCount());
                    values.put(StatesContract.Columns.UPDATED, timestamp);
                    getActivity().getContentResolver().insert(
                            StatesContract.CONTENT_URI, values);
                }
                */
                
                stateAdapter.addStates(states);
                setListAdapter(stateAdapter);
                stateAdapter.notifyDataSetChanged();
                
                StateListFragment.this.getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE); 
              
            } catch (JsonParseException e) {
              
                Toast.makeText(mContext, "An error occurred while retrieving state data", Toast.LENGTH_SHORT).show();
              
            }
            
        }
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetAreasJsonTask extends AsyncTask<String, Void, String> {
        
        private int statePosition;
        
        private Context context;
        
        public GetAreasJsonTask(int mStatePosition, Context context)
        {
            this.context = context;
            statePosition = mStatePosition;
        }
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
            
              CwApi api = new CwApi(mContext, "2.0");
              return api.getJson(args[0]);

        }
        
        protected void onPreExecute() {
            ((Activity) context).setProgressBarIndeterminateVisibility(Boolean.TRUE); 
        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            ((Activity) context).setProgressBarIndeterminateVisibility(Boolean.FALSE); 
            
            try {
                Gson gson = new Gson();
                CwApiAreaListResponse response = gson.fromJson(result, CwApiAreaListResponse.class);
                Area[] areas = response.getAreas();
                stateAdapter.addAreasToState(statePosition, areas);
                stateAdapter.notifyDataSetChanged();
              
            } catch (JsonParseException e) {
              
                Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
              
            }
        }
    }
    
    // Check to see if data is fresh
    private boolean isFresh()
    {
        return lastUpdateMillis > System.currentTimeMillis() - CwCache.cacheMillis; 
    }
    
    public void refresh()
    {
        loadStates();
    }

}
