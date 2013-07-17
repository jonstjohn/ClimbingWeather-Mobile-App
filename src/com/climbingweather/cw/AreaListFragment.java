package com.climbingweather.cw;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AreaListFragment extends SherlockListFragment {

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
    
    private String search;
    
    private View view;
    
    private GetAreasJsonTask async;

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
      super.onCreate(savedInstanceState);
      
      /*
      // Grab passed in info
      Bundle extras = getActivity().getIntent().getExtras();
      
      // Determine JSON request URL
      String url = "";
      if (extras.containsKey("stateCode")) { // state search
          
          url = "/api/state/area/" + extras.getString("stateCode");
          noneText = "No areas for this state";
          
      } else if (extras.containsKey("latitude")) { // lat/long search
          
          url = "/api/area/search/ll=" + extras.getString("latitude") + "," + extras.getString("longitude");
          noneText = "Unable to locate nearby areas";
          
      } else if (extras.containsKey("srch")) { // keyword search
          
          url = "/api/area/search/" + extras.getString("srch");
          noneText = "No areas found for the search";
          
      }
      */
    }
    
    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        Log.i("CW", "AreaListFragment onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);
        
        view = inflater.inflate(R.layout.list, null);
        return view;
        
    }
    
    public void onStart()
    {
        Log.i("CW", "AreaListFragment onStart()");
        super.onStart();
        startLocation();
        loadAreas();
    }
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        removeLocationListener();
        if (async != null) {
            async.cancel(true);
        }
    }
    
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
    
    private void loadAreas()
    {
        mContext = getActivity();
        
        String url = "";
        
        switch (typeId) {
            case TYPE_NEARBY:
                url = "/api/area/search/ll=" + Double.toString(latitude) + "," + Double.toString(longitude) + "?days=3";
                async = new GetAreasJsonTask(this);
                async.execute(url);
                Log.i("CW", url);
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
                    url = "/api/area/list/ids-" + idStr + "?days=3";
                    async = new GetAreasJsonTask(this);
                    async.execute(url);
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
            Location locNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            
            if (locNetwork != null) {
                latitude = locNetwork.getLatitude();
                longitude = locNetwork.getLongitude();
            }
        }
        
        // Add location listener
        addLocationListener();
    }
    
    /**
     * On pause activity
     */
    public void onPause()
    {
        super.onPause();
        removeLocationListener();
        //dialog.dismiss();
    }
    
    public void onStop()
    {
        Log.i("CW", "AreaListFragment onStop()");
        super.onStop();
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
            Gson gson = new Gson();
            areas = gson.fromJson(result, Area[].class);
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
                loadAreas();
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
     * Add location listener
     */
    private void addLocationListener()
    {
        lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                600000,
                2000,
                locationListener);
    }

    /**
     * Remove location listener
     */
    private void removeLocationListener()
    {
        if (lm != null && locationListener != null) {
            lm.removeUpdates(locationListener);
        }
    }
    
}
