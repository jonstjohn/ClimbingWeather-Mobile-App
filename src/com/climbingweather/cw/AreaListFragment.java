package com.climbingweather.cw;

import com.actionbarsherlock.app.SherlockListFragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AreaListFragment extends SherlockListFragment implements LoaderCallbacks<CwApiLoaderResult>, DataFragmentInterface {

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
    
    private static final String ARGS_URI    = "com.climbingweather.cw.ARGS_URI";
    private static final String ARGS_PARAMS = "com.climbingweather.cw.ARGS_PARAMS";

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
            cursor.setNotificationUri(getActivity().getContentResolver(), AreasContract.CONTENT_URI);
        } else if (typeId == TYPE_FAVORITE) {
            cursor = getActivity().getContentResolver().query(AreasContract.CONTENT_URI, null, "favorite.area_id IS NOT NULL", null, "area.name ASC");
            cursor.setNotificationUri(getActivity().getContentResolver(), AreasContract.CONTENT_URI);
        } else {
            cursor = getActivity().getContentResolver().query(AreasContract.CONTENT_URI, null, null, null, null);
            cursor.setNotificationUri(getActivity().getContentResolver(), AreasContract.CONTENT_URI);
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
        
        // Start location for nearby areas
        if (typeId == TYPE_NEARBY) {
            startLocation();
        }
        
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
        Log.i("CW", "AreaListFragment " + getScreenName() + " onResume()");
        if (typeId == TYPE_NEARBY) {
            startLocation();
        }
        loadAreas(false);
    }
    
    /**
     * Load areas
     * @param forceReload Force reload of areas, ignore cache
     */
    private void loadAreas(boolean forceReload)
    {
        CwApi api = new CwApi(getActivity(), "2.0");
        switch (typeId) {
            // NEARBY using latitude and longitude
            case TYPE_NEARBY:
                api.loadNearbyAreas(this, latitude, longitude, forceReload);
                break;
            // FAVORITE using db adapter to fetch stored favorites
            case TYPE_FAVORITE:
                api.loadFavoriteAreas(this, forceReload);
                break;
            // SEARCH build URL using search string
            case TYPE_SEARCH:
                api.loadSearchAreas(this, search, forceReload);
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
    }
    
    /**
     * On stop
     */
    @Override
    public void onStop()
    {
        Log.i("CW", "AreaListFragment " + getScreenName() + " onStop()");
        super.onStop();
        if (typeId == TYPE_NEARBY) {
            removeLocationListener();
        }
    }
    
    public class AreaCursorAdapter extends CursorAdapter
    {

        public AreaCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            
            /*
            String cols[] = cursor.getColumnNames();
            for (int i = 0; i < cols.length; i++) {
                Log.i(TAG, cols[i]);
            }
            */
            TextView nameTextView = (TextView) view.findViewById(R.id.name);
            LinearLayout areaLinearLayout = (LinearLayout) view.findViewById(R.id.area);
            ImageView loadingImageView = (ImageView) view.findViewById(R.id.loading);
            TextView stateTextView = (TextView) view.findViewById(R.id.state);
            TextView day1TextView = (TextView) view.findViewById(R.id.d1);
            TextView day2TextView = (TextView) view.findViewById(R.id.d2);
            TextView day3TextView = (TextView) view.findViewById(R.id.d3);
        
            nameTextView.setText(cursor.getString(cursor.getColumnIndex(AreasContract.Columns.NAME)));
            stateTextView.setText(CwApplication.getStateNameFromCode(cursor.getString(cursor.getColumnIndex(AreasContract.Columns.STATE_CODE))));
            
            String d1Sym = cursor.getString(cursor.getColumnIndex(AreasContract.Columns.DAY1_SYMBOL));
            if (d1Sym != null) {
                String symbol1 = d1Sym.replace(".png", "");
                day1TextView.setCompoundDrawablesWithIntrinsicBounds(0, context.getResources().getIdentifier(symbol1, "drawable", "com.climbingweather.cw"), 0, 0);
            }
            String high1 = cursor.getString(cursor.getColumnIndex(AreasContract.Columns.DAY1_HIGH));
            day1TextView.setText(high1 == null ? "--" : high1 + (char) 0x00B0);
            
            String d2Sym = cursor.getString(cursor.getColumnIndex(AreasContract.Columns.DAY2_SYMBOL));
            if (d2Sym != null) {
                String symbol2 = d2Sym.replace(".png", "");
                day2TextView.setCompoundDrawablesWithIntrinsicBounds(0, context.getResources().getIdentifier(symbol2, "drawable", "com.climbingweather.cw"), 0, 0);
            }
            String high2 = cursor.getString(cursor.getColumnIndex(AreasContract.Columns.DAY2_HIGH));
            day2TextView.setText(high2 == null ? "--" : high2 + (char) 0x00B0);
            
            String d3Sym = cursor.getString(cursor.getColumnIndex(AreasContract.Columns.DAY3_SYMBOL));
            if (d3Sym != null) {
                String symbol3 = d3Sym.replace(".png", "");
                day3TextView.setCompoundDrawablesWithIntrinsicBounds(0, context.getResources().getIdentifier(symbol3, "drawable", "com.climbingweather.cw"), 0, 0);
            }
            
            String high3 = cursor.getString(cursor.getColumnIndex(AreasContract.Columns.DAY3_HIGH));
            day3TextView.setText(high3 == null ? "--" : high3 + (char) 0x00B0);
            
            areaLinearLayout.setVisibility(View.VISIBLE);
            loadingImageView.setVisibility(View.INVISIBLE);
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
     * Callback for loader
     */
    public Loader<CwApiLoaderResult> onCreateLoader(int id, Bundle args) {
        if (args != null && args.containsKey(ARGS_URI) && args.containsKey(ARGS_PARAMS)) {
            Log.i(TAG, "onCreateLoader()");
            Uri uri = args.getParcelable(ARGS_URI);
            Bundle params = args.getParcelable(ARGS_PARAMS);
            getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
            //return new RESTLoader(this.getActivity(), RESTLoader.HTTPVerb.GET, action, params);
            return new CwApiLoader(this.getActivity(), RESTClient.HTTPMethod.GET, uri, params);
        }
        
        return null;
    }

    /**
     * Callback for loader
     */
    public void onLoadFinished(Loader<CwApiLoaderResult> loader, CwApiLoaderResult data) {
        
        Log.i(TAG, "onLoadFinished()");
        RESTClientResponse response = data.getResponse();
        int code = response.getCode();
        String json = response.getData();
        
        getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);
        
        mCursor = getCursor();
        mAdapter.swapCursor(mCursor);
        //mCursor.requery();
        
        mAdapter.notifyDataSetChanged();
        
        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !json.equals("")) {
            Log.i(TAG, "onLoadFinished() using new loader");
        }
        else {
            Toast.makeText(this.getActivity(), "Failed to load areas. Check your internet settings.", Toast.LENGTH_SHORT).show();
        }
        
    }
    
    /**
     * Callback for loader
     */
    public void onLoaderReset(Loader<CwApiLoaderResult> loader) {
    }
    
    /**
     * Refresh data
     */
    public void refresh()
    {
        loadAreas(true);
    }
    
}
