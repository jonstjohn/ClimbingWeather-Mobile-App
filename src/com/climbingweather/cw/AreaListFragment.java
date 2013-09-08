package com.climbingweather.cw;

import com.actionbarsherlock.app.SherlockListFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.AdapterView.OnItemClickListener;

public class AreaListFragment extends SherlockListFragment implements DataFragmentInterface {

    /**
     * Location manager for location updates
     */
    private LocationManager lm;
    
    /**
     * Location listener to receive location updates
     */
    private LocationListener locationListener;
    
    /**
     * Areas
     */
    //private Area[] areas;
    
    /**
     * Context
     */
    private Context mContext;
    
    /**
     * Area list type (see constants)
     */
    private int typeId;
    
    /**
     * Type NEARBY areas
     */
    public static final int TYPE_NEARBY = 1;
    
    /**
     * Type SEARCH areas
     */
    public static final int TYPE_SEARCH = 2;
    
    /**
     * Type FAVORITE areas
     */
    public static final int TYPE_FAVORITE = 3;
    
    public static final String INTENT_FILTER_NEARBY = "areas.nearby";
    public static final String INTENT_FILTER_FAVORITE = "areas.favorite";
    public static final String INTENT_FILTER_SEARCH = "areas.search";
    
    /**
     * Latitude used for nearby
     */
    private double latitude;
    
    /**
     * Longitude used for nearby
     */
    private double longitude;
    
    /**
     * Search string used for search
     */
    private String search = "";
    
    /**
     * List fragment view, depends on type
     */
    private View view;
    
    /**
     * Cursor
     */
    private Cursor mCursor;
    
    private AreaCursorAdapter mAdapter;
    
    /**
     * Tag for logging
     */
    private static final String TAG = AreaListFragment.class.getName();
    
    /**
     * Get instance
     * @return AreaListFragment
     */
    public static AreaListFragment newInstance(int typeId)
    {
        AreaListFragment myFragment = new AreaListFragment();
        
        Bundle args = new Bundle();
        args.putInt("typeId", typeId);
        myFragment.setArguments(args);
        return myFragment;
    }
    
    /**
     * Get instance
     * @return AreaListFragment
     */
    public static AreaListFragment newInstance(int typeId, double latitude, double longitude) 
    {
        AreaListFragment myFragment = new AreaListFragment();
        Bundle args = new Bundle();
        args.putInt("typeId", typeId);
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        myFragment.setArguments(args);
        return myFragment;
    }
    
    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
      super.onCreate(savedInstanceState);
      Logger.log("AreaListFragment " + getScreenName() + " onCreate()");
      
      // Get typeId, latitude and longitude from arguments set in newInstance()
      typeId = getArguments().getInt("typeId");
      latitude = getArguments().getDouble("latitude");
      longitude = getArguments().getDouble("longitude");
      setRetainInstance(true);
      
    }
    
    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        Log.i("CW", "AreaListFragment " + getScreenName() + " onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);
        mContext = getActivity();
        
        // Type is SEARCH
        if (typeId == TYPE_SEARCH) {
            view = inflater.inflate(R.layout.list_search, null);
            
            // Search edit text
            final EditText searchEdit = (EditText) view.findViewById(R.id.search);
            
            // Add listener so when 'Done' is clicked, a search is performed
            searchEdit.setOnEditorActionListener(new OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // TODO for now, just delete all existing search data and search again
                        CwDbHelper dbHelper = new CwDbHelper(getActivity());
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete(CwDbHelper.Tables.SEARCH, null, null);
                        db.delete(CwDbHelper.Tables.SEARCH_AREA, null, null);
                        loadAreas(false);
                    }
                    return false;
                }
            });

            // When text is changed in search, write to instance variable
            searchEdit.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s)
                {
                    // do nothing
                }
                
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                    // do nothing
                }
                
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    search = s.toString();
                }
            });
        // FAVORITE
        } else if (typeId == TYPE_FAVORITE) {
            view = inflater.inflate(R.layout.list_favorites, null);
        // Other, such as NEARBY
        } else {
            view = inflater.inflate(R.layout.list, null);
        }
        
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCursor = getCursor();
        mCursor.setNotificationUri(getActivity().getContentResolver(), AreasContract.CONTENT_URI);
        mAdapter = new AreaCursorAdapter(mContext, mCursor, 0);
        setListAdapter(mAdapter);
        
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        
        // Set click listener for areas
        lv.setOnItemClickListener(new OnItemClickListener() {
            
            /**
             * On item click action, open area activity
             */
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                mCursor.moveToPosition(position);
                String areaId = mCursor.getString(mCursor.getColumnIndex(AreasContract.Columns.AREA_ID));
                String name = mCursor.getString(mCursor.getColumnIndex(AreasContract.Columns.NAME));
                Intent i = new Intent(getActivity(), AreaFragmentActivity.class);
                i.putExtra("areaId", areaId);
                i.putExtra("name", name);
                startActivity(i);
            }
            
        });
    }
    
    /**
     * Get cursor
     * @return
     */
    private Cursor getCursor()
    {
        Cursor cursor;
        if (typeId == TYPE_NEARBY) {
            cursor = getActivity().getContentResolver().query(AreasContract.CONTENT_URI, null, "nearby IS NOT NULL", null, "nearby ASC");
        } else if (typeId == TYPE_FAVORITE) {
            CwDbHelper.testQuery(getActivity(), "SELECT area_id FROM favorite");
            cursor = getActivity().getContentResolver().query(AreasContract.CONTENT_URI, null, "favorite.area_id IS NOT NULL", null, "area.name ASC");
        } else if (typeId == TYPE_SEARCH) {
            // TODO
            cursor = getActivity().getContentResolver().query(AreasContract.CONTENT_URI, null, "search_area.area_id IS NOT NULL", null, "search_area._id ASC");
        } else {
            cursor = getActivity().getContentResolver().query(AreasContract.CONTENT_URI, null, null, null, null);
        }
        
        return cursor;
    }
    
    /**
     * On fragment start
     */
    @Override
    public void onStart()
    {
        Log.i("CW", "AreaListFragment " + getScreenName() + " onStart()");
        super.onStart();
        
        // Record to google analytics
        ((CwApplication) this.getActivity().getApplication()).getGaTracker().sendView(getScreenName());
        
        // Set empty view for list
        getListView().setEmptyView(view.findViewById(R.id.emptyView));
        
        Intent intent = new Intent(getActivity(), CwApiService.class);
        getActivity().startService(intent);
    }
    
    /**
     * Get screen name for GA
     * @return String
     */
    private String getScreenName()
    {
        String name = "";
        switch (typeId)
        {
        case TYPE_NEARBY:
            name = "/nearby";
            break;
        case TYPE_SEARCH:
            name = "/search";
            break;
        case TYPE_FAVORITE:
            name = "/favorite";
            break;
        default:
            name ="/areaList";
            break;
        }
        return name;
    }
    
    /**
     * On Resume, start location and load areas
     */
    public void onResume()
    {
        super.onResume();
        Log.i(TAG, "AreaListFragment " + getScreenName() + " onResume()");
        if (typeId == TYPE_NEARBY) {
            startLocation();
        }
        getActivity().registerReceiver(myReceiver, new IntentFilter(getIntentFilter()));
        loadAreas(false);
        CwDbHelper.dumpFavorites(getActivity());
    }
    
    /**
     * Load areas
     * @param forceReload Force reload of areas, ignore cache
     */
    private void loadAreas(boolean forceReload)
    {
        getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
        //CwApi api = new CwApi(getActivity(), "2.0");
        switch (typeId) {
            // NEARBY using latitude and longitude
            case TYPE_NEARBY:
                CwApiServiceHelper.getInstance().startNearby(getActivity(), latitude, longitude);
                break;
            // FAVORITE using db adapter to fetch stored favorites
            case TYPE_FAVORITE:
                CwApiServiceHelper.getInstance().startFavorites(getActivity());
                break;
            // SEARCH build URL using search string
            case TYPE_SEARCH:
                //CwApiServiceHelper.getInstance().startSearch(getActivity(), search);
                CwApiServiceHelper.getInstance().startSearch(getActivity(), search);
                //api.loadSearchAreas(this, search, forceReload);
                break;
        }
        
    }
    
    /**
     * Start location tracking
     */
    private void startLocation()
    {
        // Start location manager
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new AreaLocationListener();
        
        // Get last known location
        Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        
        // If location is found, use for latitude and longitude
        if (loc != null) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        }
        
        // Add location listener
        lm.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                600000,
                2000,
                locationListener);
    }
    
    /**
     * On pause activity
     */
    @Override
    public void onPause()
    {
        Log.i("CW", "AreaListFragment " + getScreenName() + " onPause()");
        super.onPause();
        if (typeId == TYPE_NEARBY) {
            removeLocationListener();
        }
        getActivity().unregisterReceiver(myReceiver);
        CwDbHelper.dumpFavorites(getActivity());
    }
    
    /**
     * On stop
     */
    @Override
    public void onStop()
    {
        Log.i("CW", "AreaListFragment " + getScreenName() + " onStop()");
        super.onStop();
    }
    
    public class AreaCursorAdapter extends CursorAdapter
    {

        public AreaCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Area.bindViewCursor(view, context, cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.list_item_area, null, false);
            return view;
        }

    }
    
    /**
     * Location listener
     */
    private class AreaLocationListener implements LocationListener 
    {
        /**
         * On location change, update lat/long
         */
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
                loadAreas(false);
            }
        }

        public void onProviderDisabled(String provider) {}

        public void onProviderEnabled(String provider) {}

        public void onStatusChanged(String provider, int status, 
            Bundle extras) {}
    }
    
    /**
     * Remove location listener
     */
    private void removeLocationListener()
    {
        if (lm != null && locationListener != null) {
            Logger.log("Removing location listener from nearby areas");
            lm.removeUpdates(locationListener);
        }
    }
    
    /**
     * Refresh data
     */
    public void refresh()
    {
        loadAreas(true);
    }
    
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {        
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Received intent in broadcast receiver");
            Log.i(TAG, intent.getAction());
            getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);
            
            mCursor = getCursor();
            mCursor.setNotificationUri(getActivity().getContentResolver(), AreasContract.CONTENT_URI);
            mAdapter.swapCursor(mCursor);
            //mCursor.requery();
            
            mAdapter.notifyDataSetChanged();
        }
    };
    
    private String getIntentFilter() {
        switch (typeId) {
            case TYPE_FAVORITE:
                return INTENT_FILTER_FAVORITE;
            case TYPE_NEARBY:
                return INTENT_FILTER_NEARBY;
            case TYPE_SEARCH:
                return INTENT_FILTER_SEARCH;
        }
        
        return "UNKNOWN";
            
    }
    
}
