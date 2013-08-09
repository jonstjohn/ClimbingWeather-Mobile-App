package com.climbingweather.cw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AreaListFragment extends SherlockListFragment implements LoaderCallbacks<RESTLoader.RESTResponse>, DataFragmentInterface {

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
    private Area[] areas;
    
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
     * Tag for logging
     */
    private static final String TAG = AreaListFragment.class.getName();
    
    /**
     * Loader for NEARBY areas
     */
    private static final int LOADER_AREA_NEARBY = 1;
    
    /**
     * Loader for FAVORITE areas
     */
    private static final int LOADER_AREA_FAVORITE = 2;
    
    /**
     * Loader for SEARCH areas
     */
    private static final int LOADER_AREA_SEARCH = 3;
    
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
        String url = "";
        
        CwApi api = new CwApi(getActivity(), "2.0");
        Bundle params = new Bundle();
        params.putString("days", "3");
        
        switch (typeId) {
            // NEARBY using latitude and longitude
            case TYPE_NEARBY:
                url = "/api/area/list/" + Double.toString(latitude) + "," + Double.toString(longitude);
                api.initLoader(this, url, params, LOADER_AREA_NEARBY, forceReload);
                break;
            // FAVORITE using db adapter to fetch stored favorites
            case TYPE_FAVORITE:
                
                // Get favorites from DB
                FavoriteDbAdapter favDb = new FavoriteDbAdapter(mContext);
                favDb.open();
                ArrayList<String> ids = favDb.fetchAllFavoriteAreaIds();
                favDb.close();
                
                // Loop over favorite area ids to build URL
                if (ids.size() > 0) {
                    String idStr = "";
                    for (int i = 0; i < ids.size() - 1; i++) {
                        idStr += ids.get(i) + ",";
                    }
                    idStr += ids.get(ids.size() - 1);
                    url = "/api/area/list/ids-" + idStr; // + "?days=3";
                    api.initLoader(this, url, params, LOADER_AREA_FAVORITE, forceReload);
                // If no favorites, show empty view
                } else {
                    View tv = (View) view.findViewById(R.id.emptyView);
                    tv.setVisibility(View.VISIBLE);
                }
                break;
            // SEARCH build URL using search string
            case TYPE_SEARCH:
                try {
                    String encodedSearch = URLEncoder.encode(search, "UTF-8");
                    url = "/api/area/list/" + encodedSearch;
                    api.initLoader(this, url, params, LOADER_AREA_SEARCH, true);
                    Log.i("CW", url);
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(mContext, "An error occurred while performing search", Toast.LENGTH_SHORT).show();
                }
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
    
    /**
     * Load areas from JSON string result
     */
    public void processJson(String result) {
    
        try {
            Log.i(TAG, result);
            
            // Convert JSON into areas using GSON
            Gson gson = new Gson();
            areas = gson.fromJson(result, CwApiAreaListResponse.class).getAreas();
            AreaAdapter adapter = new AreaAdapter(mContext, R.id.list_item_text_view, areas);
            setListAdapter(adapter);
            
            View tv = (View) view.findViewById(R.id.emptyView);
            
            if (areas.length > 0) {
                tv.setVisibility(View.GONE);
            } else {
                tv.setVisibility(View.VISIBLE);
            }
            
        } catch (JsonParseException e) {
            Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
        }
        
        try {
            ListView lv = getListView();
            lv.setTextFilterEnabled(true);
            
            // Set click listener for areas
            lv.setOnItemClickListener(new OnItemClickListener() {
                
                /**
                 * On item click action, open area activity
                 */
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    
                    Area area = areas[position];
                    Intent i = new Intent(getActivity(), AreaFragmentActivity.class);
                    i.putExtra("areaId", Integer.valueOf(area.getId()).toString());
                    i.putExtra("name", area.getName());
                    startActivity(i);
                }
                
            });
        } catch (IllegalStateException e) {
            Logger.log("Unable to getListView() in AreaListFragment processJson(), likely view destroyed during async");
            return;
        }
      
    }
    
    /**
     * Area adapter for list
     */
    public class AreaAdapter extends ArrayAdapter<Area>
    {
        public AreaAdapter(Context context, int textViewResourceId,
                Area[] objects) {
            super(context, textViewResourceId, objects);
        }

        /**
         * Get view
         */
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return ((Area) getItem(position)).getListRowView(convertView, parent, getContext());
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
    public Loader<RESTLoader.RESTResponse> onCreateLoader(int id, Bundle args) {
        if (args != null && args.containsKey(ARGS_URI) && args.containsKey(ARGS_PARAMS)) {
            Uri    action = args.getParcelable(ARGS_URI);
            Bundle params = args.getParcelable(ARGS_PARAMS);
            getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
            return new RESTLoader(this.getActivity(), RESTLoader.HTTPVerb.GET, action, params);
        }
        
        return null;
    }

    /**
     * Callback for loader
     */
    public void onLoadFinished(Loader<RESTLoader.RESTResponse> loader, RESTLoader.RESTResponse data) {
        int    code = data.getCode();
        String json = data.getData();
        
        getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);
        
        // Check to see if we got an HTTP 200 code and have some data.
        if (code == 200 && !json.equals("")) {
            Log.i(TAG, "onLoadFinished() using new loader");
            AreaListFragment.this.getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE); 
            processJson(json);
        }
        else {
            Toast.makeText(this.getActivity(), "Failed to load data. Check your internet settings.", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Callback for loader
     */
    public void onLoaderReset(Loader<RESTLoader.RESTResponse> loader) {
    }
    
    /**
     * Refresh data
     */
    public void refresh()
    {
        loadAreas(true);
    }
}
