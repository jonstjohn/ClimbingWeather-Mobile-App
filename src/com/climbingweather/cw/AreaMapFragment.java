package com.climbingweather.cw;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class AreaMapFragment extends SherlockFragment
{
    private GoogleMap gmap;
    
    private Long lastUpdateMillis = 0L;
    
    private Area[] areas;
    
    private Activity activity;
    
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
                LatLngBounds bounds = gmap.getProjection().getVisibleRegion().latLngBounds;
                Logger.log(Double.toString(bounds.southwest.latitude)); // -2.249943
                Logger.log(Double.toString(bounds.southwest.longitude)); // -126.741093
                Logger.log(Double.toString(bounds.northeast.latitude)); // 63.0507365
                Logger.log(Double.toString(bounds.northeast.longitude)); // -63.4598489
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
    
    public void onDestroy()
    {
        Logger.log("AreaMap destroy()");
        super.onDestroy();
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
        String url = "";
        
        url = "/api/area/map?bounds=17.642747,-132.1875,57.515105,-47.8125&zoom=4&date=2013-07-11";
        new GetAreasJsonTask().execute(url);

    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetAreasJsonTask extends AsyncTask<String, Void, String> {
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
              
              Log.i("CW", args[0]);
              CwApi api = new CwApi(AreaMapFragment.this.getActivity(), "2.0");
              return api.getJson(args[0]);

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
                    gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(areas[i].getLatitude(), areas[i].getLongitude()))
                    .title(areas[i].getName()));
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
