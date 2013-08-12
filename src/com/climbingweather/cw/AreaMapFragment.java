package com.climbingweather.cw;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class AreaMapFragment extends SherlockFragment implements DataFragmentInterface
{
    private GoogleMap gmap;
    
    private Area[] areas;
    
    private ArrayList<Integer> areaIdsOnMap = new ArrayList<Integer>();
    
    private HashMap<String, Area> markerAreas = new HashMap<String, Area>();
    
    private GetAreasJsonTask areasJsonTask;
    
    private View view;
    
    private double latitude;
    
    private double longitude;
    
    private float zoomLevel = 10.0f;
    
    /**
     * Get instance
     * @return AreaListFragment
     */
    public static AreaMapFragment newInstance(double latitude, double longitude, float zoomLevel) 
    {
        AreaMapFragment myFragment = new AreaMapFragment();
        Bundle args = new Bundle();
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        args.putFloat("zoomLevel", zoomLevel);
        Logger.log("Put latitude longitude " + Double.toString(latitude) + " " + Double.toString(longitude));
        myFragment.setArguments(args);
        
        return myFragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        latitude = getArguments().getDouble("latitude");
        longitude = getArguments().getDouble("longitude");
        
        zoomLevel = getArguments().getFloat("zoomLevel");
        setRetainInstance(true);
    }
    
    /**
     * On create
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        
        Logger.log("AreaMapFragment onCreateView()");
        
        /*
        view = inflater.inflate(R.layout.area_map, container, false);
        setupGmap();
        
        return view;
        */
        
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.area_map, container, false);
            setupGmap();
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }
        return view;
        
    }
    
    public void onStart()
    {
        Logger.log("AreaMapFragment onStart()");
        super.onStart();
        ((CwApplication) this.getActivity().getApplication()).getGaTracker().sendView("/map");
        //loadAreas();
    }
    // Setup google map 
    public void setupGmap()
    {
        Logger.log("AreaMapFragment setupGmap()");
        gmap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        Logger.log(gmap.toString());
        
        //gmap.setMyLocationEnabled(true);
        
        if (latitude != 0.0 && longitude != 0.0) {
            gmap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude) , zoomLevel) );
        }
        
        gmap.setOnCameraChangeListener(new OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position)
            {
                loadAreas();
            }
        });
        
        gmap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            public void onInfoWindowClick(Marker marker)
            {
                Area area = markerAreas.get(marker.getId());
                Intent i = new Intent(getActivity(), AreaFragmentActivity.class);
                i.putExtra("areaId", Integer.valueOf(area.getId()).toString());
                i.putExtra("name", area.getName());
                startActivity(i);
            }
            
        });
        
        gmap.setInfoWindowAdapter(new InfoWindowAdapter() {
            public View getInfoContents(Marker marker)
            {
                return markerAreas.get(marker.getId()).getMapInfoWindow(AreaMapFragment.this.getActivity());
            }
            
            public View getInfoWindow(Marker marker)
            {
                return null;
            }
            
            
            
        });
    }
    
    public void onAttach(Activity activity)
    {
        Logger.log("AreaMapFragment onAttach()");
        super.onAttach(activity);
    }
    
    public void onStop()
    {
        super.onStop();
        Logger.log("AreaMapFragment onStop()");
    }
    
    public void onDestroyView() {
        super.onDestroyView();
        //areas = null;
        //markerAreas.clear();
        //areaIdsOnMap.clear();
        //gmap.clear();
        Logger.log("AreaMapFragment onDestroyView()");
        
        /*
        Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));
        if (fragment != null) {
            android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }
        */
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        Logger.log("AreaMapFragment setUserVisibleHint() " + Boolean.toString(visible));
        // Load areas if visible and view was already created
        if (visible) {
            loadAreas();
        }
    }
    
    private void loadAreas()
    {
        Logger.log("AreaMapFragment loadAreas()");
        if (gmap != null) {
            LatLngBounds bounds = gmap.getProjection().getVisibleRegion().latLngBounds;
            String boundsStr = Double.toString(bounds.southwest.latitude) + ","
                    + Double.toString(bounds.southwest.longitude) + ","
                    + Double.toString(bounds.northeast.latitude) + ","
                    + Double.toString(bounds.northeast.longitude);
            
            areasJsonTask = new GetAreasJsonTask(boundsStr, (int) gmap.getCameraPosition().zoom);
            areasJsonTask.execute();
        }

    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetAreasJsonTask extends AsyncTask<String, Void, String>
    {
        private String bounds;
        
        private int zoom;
        
        public GetAreasJsonTask(String bounds, int zoom)
        {
            this.bounds = bounds;
            this.zoom = zoom;
        }
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
              
              CwApi api = new CwApi(AreaMapFragment.this.getActivity(), "2.0");
              Date today = new java.util.Date();
              String todayStr = android.text.format.DateFormat.format("yyyy-MM-dd", today).toString();
              
              Calendar cal = Calendar.getInstance();  
              cal.setTime(today);  
              cal.add(Calendar.DATE, 1);
              String tomorrowStr = android.text.format.DateFormat.format("yyyy-MM-dd", cal.getTime()).toString();
              
              cal.add(Calendar.DATE, 1);
              String nextDayStr = android.text.format.DateFormat.format("yyyy-MM-dd", cal.getTime()).toString();
              
              String dateStr = todayStr + "," + tomorrowStr + "," + nextDayStr;
              
              CharSequence date = android.text.format.DateFormat.format("yyyy-MM-dd", today);
              Logger.log(date.toString());
              String url = "/area/map?bounds=" + bounds + "&zoom=" + Integer.toString(zoom) + "&date=" + dateStr;
              Logger.log(url);
              return api.getJson(url);

        }
        
        protected void onPreExecute() {
            AreaMapFragment.this.getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE); 
        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            AreaMapFragment.this.getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE); 
            processJson(result);
        }
        
        private void processJson(String result)
        {
            Logger.log("AreaMapFragment processJson()");
            try {
                Gson gson = new Gson();
                CwApiAreaMapResponse apiResponse = gson.fromJson(result,  CwApiAreaMapResponse.class);

                areas = apiResponse.getAreas();
                Logger.log("Found " + Integer.toString(areas.length) + " areas in AreaMapFragment processJson()"); 
                
                for (int i = 0; i < areas.length; i++) {
                    
                    if (!areaIdsOnMap.contains(areas[i].getId())) {
                        Marker marker = gmap.addMarker(new MarkerOptions()
                            .position(new LatLng(areas[i].getLatitude(), areas[i].getLongitude()))
                            .title(areas[i].getName())
                            .icon(BitmapDescriptorFactory.fromResource(
                                    AreaMapFragment.this.getActivity().getResources()
                                    .getIdentifier(areas[i].getMapIcon(), "drawable", "com.climbingweather.cw")
                             ))
                        );
                        areaIdsOnMap.add(areas[i].getId());
                        markerAreas.put(marker.getId(), areas[i]);
                    }
                }
                
            } catch (JsonParseException e) {
                Toast.makeText(
                        AreaMapFragment.this.getActivity(), "An error occurred while retrieving map areas", Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
    
    public void refresh()
    {
        loadAreas();
    }
    
}
