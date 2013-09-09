package com.climbingweather.cw;

import java.util.HashMap;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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

public class StateListFragment extends ExpandableListFragment implements DataFragmentInterface {
	
    // State objects
    //private ArrayList<State> states = new ArrayList<State>();
    
    private long lastUpdateMillis = 0L;
    
    private View view;
    
    private static final String TAG = StateListFragment.class.getName();
    
    private StateExpandableListAdapter mAdapter;
    
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
    	mAdapter = new StateExpandableListAdapter(mContext);
        setListAdapter(mAdapter);
    	
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
        getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
        CwApiServiceHelper.getInstance().startStates(getActivity());
    }
    
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        // use groupPosition and childPosition to locate the current item in the adapter
        Log.i(TAG, "Child clicked");
        Cursor cursor = ((StateExpandableListAdapter) getListAdapter()).getChild(groupPosition, childPosition);
        Intent i = new Intent(getActivity(), AreaFragmentActivity.class);
        i.putExtra("areaId", cursor.getString(cursor.getColumnIndex(AreasContract.Columns.AREA_ID)));
        i.putExtra("name", cursor.getString(cursor.getColumnIndex(AreasContract.Columns.NAME)));
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
        
        getActivity().unregisterReceiver(myReceiver);
    }
    
    /**
     * On resume activity
     */
    public void onResume()
    {
        Log.i("CW", "StateListFragment onResume()");
        super.onResume();
        
        getActivity().registerReceiver(myReceiver, new IntentFilter(CwApiService.INTENT_FILTER_STATE));
        getActivity().registerReceiver(myReceiver, new IntentFilter(CwApiService.INTENT_FILTER_STATE_AREAS));
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
        
        private Cursor mStateCursor;
        
        private HashMap<String, Cursor> mStateAreaCursors = new HashMap<String, Cursor>();
        
        public StateExpandableListAdapter(Context context)
        {
            inflater = LayoutInflater.from(context);
            this.context = context;
            mStateCursor = getActivity().getContentResolver().query(
                    StatesContract.CONTENT_URI, null, null, null, "NAME ASC");
            mStateCursor.setNotificationUri(getActivity().getContentResolver(), StatesContract.CONTENT_URI);
        }
        
        public Cursor getChild(int groupPosition, int childPosition) {
            
            if (!hasAreasLoaded(groupPosition)) {
                return null;
                
            }
            
            return getAreaCursor(groupPosition, childPosition);
        }
        
        private String getStateCode(int groupPosition) {
            mStateCursor.moveToPosition(groupPosition);
            return mStateCursor.getString(mStateCursor.getColumnIndex(StatesContract.Columns.STATE_CODE));
        }
        
        private boolean hasAreasLoaded(String stateCode) {
            return mStateAreaCursors.containsKey(stateCode);
        }
        
        private boolean hasAreasLoaded(int groupPosition) {
            return mStateAreaCursors.containsKey(getStateCode(groupPosition));
        }
        
        private Cursor getAreaCursor(int groupPosition, int childPosition) {
            if (!hasAreasLoaded(groupPosition)) {
                return null;
            }
            
            Cursor cursor = mStateAreaCursors.get(getStateCode(groupPosition));
            Log.i(TAG, "getAreaCursor()");
            Log.i(TAG, "Cursor count: " + Integer.toString(cursor.getCount()));
            Log.i(TAG, "Move to position: " + Integer.toString(childPosition));;
            cursor.moveToPosition(childPosition);
            CwDbHelper.dumpCursorRow(cursor);
            return cursor;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            
            String stateCode = getStateCode(groupPosition);
            
            if (mStateAreaCursors.containsKey(stateCode)) {
                return mStateAreaCursors.get(stateCode).getCount();
            } else {
                return 1;
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
            mStateCursor.moveToPosition(groupPosition);
            String stateCode = mStateCursor.getString(mStateCursor.getColumnIndex(StatesContract.Columns.STATE_CODE));
            
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_area, parent,false);
            }
     
            TextView nameTextView = (TextView) convertView.findViewById(R.id.name);
            LinearLayout areaLinearLayout = (LinearLayout) convertView.findViewById(R.id.area);
            ImageView loadingImageView = (ImageView) convertView.findViewById(R.id.loading);
            
            if (hasAreasLoaded(stateCode) && mStateAreaCursors.get(stateCode).getCount() > 0) {
                
                Cursor areaCursor = getChild(groupPosition, childPosition);
                Area.bindViewCursor(convertView, context, areaCursor);
                
            } else {
                nameTextView.setText("Loading areas ...");
                areaLinearLayout.setVisibility(View.INVISIBLE);
                loadingImageView.setVisibility(View.VISIBLE);
            }
     
            //return the entire view
            return convertView;
        }

        public Object getGroup(int groupPosition) {
            return mStateCursor.moveToPosition(groupPosition);
            //return states.get(groupPosition);
        }

        public int getGroupCount() {
            return mStateCursor.getCount();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_state, parent,false);
            }
     
            mStateCursor.moveToPosition(groupPosition);
            
            TextView nameTextView = (TextView) convertView.findViewById(R.id.name);
            String stateStr = mStateCursor.getString(mStateCursor.getColumnIndex(StatesContract.Columns.NAME));
            nameTextView.setText(stateStr);
            
            TextView areaCountTextView = (TextView) convertView.findViewById(R.id.areaCount);
            String areaCount = mStateCursor.getString(mStateCursor.getColumnIndex(StatesContract.Columns.AREAS));
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
            mStateCursor.moveToPosition(groupPosition);
            String stateCode = mStateCursor.getString(mStateCursor.getColumnIndex(StatesContract.Columns.STATE_CODE));
            if (!hasAreasLoaded(groupPosition)) {
                getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
                CwApiServiceHelper.getInstance().startStateAreas(context, stateCode);
            }
            
            String[] selectionArgs = {stateCode};
            Cursor areaCursor = getActivity().getContentResolver().query(
                    AreasContract.CONTENT_URI, null, "state_code = ?", selectionArgs, "area.name ASC");
            areaCursor.setNotificationUri(getActivity().getContentResolver(), AreasContract.CONTENT_URI);
            mStateAreaCursors.put(stateCode, areaCursor);
        }
        
        public void refreshStates() {
            mStateCursor = getActivity().getContentResolver().query(
                    StatesContract.CONTENT_URI, null, null, null, "NAME ASC");
            notifyDataSetChanged();
        }
        public void refreshStateAreas(String stateCode) {
            String[] selectionArgs = {stateCode};
            Cursor areaCursor = getActivity().getContentResolver().query(
                    AreasContract.CONTENT_URI, null, "state_code = ?", selectionArgs, "area.name ASC");
            areaCursor.setNotificationUri(getActivity().getContentResolver(), AreasContract.CONTENT_URI);
            mStateAreaCursors.put(stateCode, areaCursor);
            notifyDataSetChanged();
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
    
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            Log.i(TAG, "Received intent in broadcast receiver");
            Log.i(TAG, intent.getAction());
            
            if (action == CwApiService.INTENT_FILTER_STATE) {
                mAdapter.refreshStates();
            } else if (action == CwApiService.INTENT_FILTER_STATE_AREAS) {
                String stateCode = intent.getStringExtra("stateCode");
                mAdapter.refreshStateAreas(stateCode);
            }
            
            if (!CwServiceHelper.getInstance(mContext).isServiceRunning()) {
                getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }
            
            
        }
    };
    
}
