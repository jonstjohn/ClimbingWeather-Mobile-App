package com.climbingweather.cw;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.climbingweather.cw.AreaListFragment.AreaAdapter;
import com.climbingweather.cw.ForecastListFragment.ForecastExpandableListAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class AreaMapFragment extends SherlockFragment
{
    private GoogleMap gmap;
    
    private Long lastUpdateMillis = 0L;
    
    private Area[] areas;
    
    private Activity activity;
    
    private ArrayList areaIdsOnMap = new ArrayList();
    
    private HashMap<String, Area> markerAreas = new HashMap<String, Area>();
    
    private GetAreasJsonTask areasJsonTask;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
    }
    
    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        Log.i("CW", "AreaMapFragment onCreateView()");
        super.onCreate(savedInstanceState);
        
        View view = inflater.inflate(R.layout.area_map, null);
        
        gmap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        Logger.log(gmap.toString());
        
        gmap.setMyLocationEnabled(true);
        
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
        return view;
        
    }
    
    public void onAttach(Activity activity)
    {
        Logger.log("Attached");
        this.activity = activity;
        super.onAttach(activity);
    }
    
    public void onDetach()
    {
        Logger.log("Detached");
        super.onDetach();
        
    }
    
    public void onDestroyView() {
        super.onDestroyView(); 
        Fragment fragment = (getFragmentManager().findFragmentById(R.id.map));  
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    @Override
    public void setUserVisibleHint(final boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible) {
            Logger.log("loadAreas()");
            loadAreas();
        }
    }
    
    private void loadAreas()
    {
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
              CharSequence date = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date());
              Logger.log(date.toString());
              String url = "/api/area/map?bounds=" + bounds + "&zoom=" + Integer.toString(zoom) + "&date=" + date.toString();
              Logger.log(url);
              return api.getJson(url);

        }
        
        protected void onPreExecute() {
            Logger.log(activity == null ? "Activity is null" : "Activity exists!");
            activity.setProgressBarIndeterminateVisibility(Boolean.TRUE); 
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
            Logger.log(result);
            Logger.log(result.substring(result.length() - 10));
            try {
                Gson gson = new Gson();
                CwApiAreaMapResponse apiResponse = gson.fromJson(result,  CwApiAreaMapResponse.class);

                areas = apiResponse.getAreas();
                
                for (int i = 0; i < areas.length; i++) {
                    Logger.log(areas[i].toString());
                    
                    if (!areaIdsOnMap.contains(areas[i].getId())) {
                        Marker marker = gmap.addMarker(new MarkerOptions()
                            .position(new LatLng(areas[i].getLatitude(), areas[i].getLongitude()))
                            .title(areas[i].getName())
                        );
                        areaIdsOnMap.add(areas[i].getId());
                        markerAreas.put(marker.getId(), areas[i]);
                    }
                }
                
                lastUpdateMillis = System.currentTimeMillis();
            } catch (JsonParseException e) {
                Toast.makeText(
                        AreaMapFragment.this.getActivity(), "An error occurred while retrieving map areas", Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
    
}
