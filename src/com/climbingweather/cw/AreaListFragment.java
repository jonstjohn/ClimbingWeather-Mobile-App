package com.climbingweather.cw;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
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
     * Progress dialog for loading
     */
    private ProgressDialog dialog;
    
    /**
     * Text to display if no areas are found
     */
    private String noneText = "No areas found";
    
    /**
     * The empty area view
     */
    private TextView emptyView;
    
    /**
     * Context
     */
    private Context mContext;
    
    /**
     * Area list type (see constants)
     */
    private int typeId;
    
    public static final int TYPE_NEARBY = 1;
    
    public static final int TYPE_SEARCH = 2;
    
    public static final int TYPE_FAVORITE = 3;
    
    private double latitude;
    
    private double longitude;
    
    private String search = "";
    
    private View view;
    
    private GetAreasJsonTask async;
    
    private static final String TAG = AreaListFragment.class.getName();
    
    private static final int LOADER_AREA_NEARBY = 1;
    
    private static final int LOADER_AREA_FAVORITE = 2;
    
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
        
        //myFragment.setType(typeId);
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
        
        //myFragment.setType(typeId);
        //myFragment.setLocation(latitude,  longitude);
        return myFragment;
    }
    
    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
      super.onCreate(savedInstanceState);
      Logger.log("AreaListFragment " + getScreenName() + " onCreate()");
      
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
        
        if (typeId == TYPE_SEARCH) {
            view = inflater.inflate(R.layout.list_search, null);
            
            // Search text
            final EditText searchEdit = (EditText) view.findViewById(R.id.search);
            
            searchEdit.setOnEditorActionListener(new OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        loadAreas(false);
                    }
                    return false;
                }
            });

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
                    Logger.log(s.toString());
                }
            });
        } else {
            view = inflater.inflate(R.layout.list, null);
        }
        return view;
        
    }
    
    public void onStart()
    {
        Log.i("CW", "AreaListFragment " + getScreenName() + " onStart()");
        super.onStart();
        if (typeId == TYPE_NEARBY) {
            startLocation();
        }
        ((CwApplication) this.getActivity().getApplication()).getGaTracker().sendView(getScreenName());
    }
    
    // Get screen name for GA
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
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Log.i("CW", "AreaListFragment " + getScreenName() + " onDestroyView()");
    }
    
    /*
    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && view != null) {
            if (typeId == TYPE_NEARBY) {
                startLocation();
            }
            loadAreas();
        } else {
            if (typeId == TYPE_NEARBY) {
                removeLocationListener();
            }
        }
    }
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
    
    private void loadAreas(boolean forceReload)
    {
        String url = "";
        
        CwApi api = new CwApi(getActivity(), "2.0");
        Bundle params = new Bundle();
        params.putString("days", "3");
        
        switch (typeId) {
            case TYPE_NEARBY:
                url = "/api/area/list/" + Double.toString(latitude) + "," + Double.toString(longitude);
                Logger.log(url);
                api.initLoader(this, url, params, LOADER_AREA_NEARBY, forceReload);
                break;
            case TYPE_FAVORITE:
                FavoriteDbAdapter favDb = new FavoriteDbAdapter(mContext);
                favDb.open();
                ArrayList<String> ids = favDb.fetchAllFavoriteAreaIds();
                favDb.close();
                if (ids.size() > 0) {
                    String idStr = "";
                    for (int i = 0; i < ids.size() - 1; i++) {
                        idStr += ids.get(i) + ",";
                    }
                    idStr += ids.get(ids.size() - 1);
                    url = "/api/area/list/ids-" + idStr; // + "?days=3";
                    api.initLoader(this, url, params, LOADER_AREA_FAVORITE, forceReload);
                }
                break;
            case TYPE_SEARCH:
                try {
                    String encodedSearch = URLEncoder.encode(search, "UTF-8");
                    url = "/api/area/list/" + encodedSearch; //  + "?days=3";
                    api.initLoader(this, url, params, LOADER_AREA_SEARCH, true);
                    Log.i("CW", url);
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(mContext, "An error occurred while performing search", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
    
    private void startLocation()
    {
        // Start location manager
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new AreaLocationListener();
        
        // Get last known location
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        // If GPS location is found, use that
        if (loc != null) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            
        // If no GPS location is found, try network provider
        } else {
            Logger.log("Trying network provider");
            Location locNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            
            if (locNetwork != null) {
                latitude = locNetwork.getLatitude();
                longitude = locNetwork.getLongitude();
            }
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
    public void onPause()
    {
        Log.i("CW", "AreaListFragment " + getScreenName() + " onPause()");
        super.onPause();
        if (typeId == TYPE_NEARBY) {
            removeLocationListener();
        }
    }
    
    public void onStop()
    {
        Log.i("CW", "AreaListFragment " + getScreenName() + " onStop()");
        super.onStop();
        if (typeId == TYPE_NEARBY) {
            removeLocationListener();
        }
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetAreasJsonTask extends AsyncTask<String, Void, String> {
        
        private AreaListFragment listFragment;
        
        public GetAreasJsonTask(AreaListFragment listFragment) {
            this.listFragment = listFragment;
            //dialog = new ProgressDialog(listFragment.getActivity());
        }
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
              
              Log.i("CW", args[0]);
              CwApi api = new CwApi(mContext);
              return api.getJson(args[0]);

        }
        
        protected void onPreExecute() {
            listFragment.getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE); 
        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            listFragment.getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE); 
            processJson(result);
        }
    }
    
    /**
     * Load areas from JSON string result
     */
    public void processJson(String result) {
    
        try {
            Log.i(TAG, result);
            Gson gson = new Gson();
            areas = gson.fromJson(result, CwApiAreaListResponse.class).getAreas();
            AreaAdapter adapter = new AreaAdapter(mContext, R.id.list_item_text_view, areas);
            setListAdapter(adapter);
        } catch (JsonParseException e) {
            Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
        }
        
        try {
            ListView lv = getListView();
            lv.setTextFilterEnabled(true);
            
            // Populate empty row in case we didn't find any areas
            //emptyView.setText(noneText);
            
            // Set on item click listener for states
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
    
    public void setType(int typeId)
    {
        this.typeId = typeId;
    }
    
    public void setLocation(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public void setSearch(String search)
    {
        this.search = search;
    }
    
    public class AreaAdapter extends ArrayAdapter<Area>
    {
        public AreaAdapter(Context context, int textViewResourceId,
                Area[] objects) {
            super(context, textViewResourceId, objects);
            // TODO Auto-generated constructor stub
        }

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

        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onStatusChanged(String provider, int status, 
            Bundle extras) {
            // TODO Auto-generated method stub
        }
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
    
    public Loader<RESTLoader.RESTResponse> onCreateLoader(int id, Bundle args) {
        if (args != null && args.containsKey(ARGS_URI) && args.containsKey(ARGS_PARAMS)) {
            Uri    action = args.getParcelable(ARGS_URI);
            Bundle params = args.getParcelable(ARGS_PARAMS);
            getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
            return new RESTLoader(this.getActivity(), RESTLoader.HTTPVerb.GET, action, params);
        }
        
        return null;
    }

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
    
    public void onLoaderReset(Loader<RESTLoader.RESTResponse> loader) {
    }
    
    public void refresh()
    {
        loadAreas(true);
    }
}
