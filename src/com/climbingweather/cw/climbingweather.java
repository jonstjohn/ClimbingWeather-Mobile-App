package com.climbingweather.cw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.net.URLEncoder;

public class climbingweather extends Activity {
    
    private final static int MENU_ABOUT = 1;
    private final static int MENU_SUGGEST = 2;
    
    /**
     * Location manager for location updates
     */
    private LocationManager lm;
    
    /**
     * Location listener to receive location updates
     */
    private LocationListener locationListener;
    
    /**
     * User latitude
     */
    private double latitude;
    
    /**
     * User longitude
     */
    private double longitude;
    
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        // Favorite text and image
        TextView favoriteText = (TextView)findViewById(R.id.favorite_text);
        favoriteText.setOnClickListener(favoriteListener);
        
        ImageView favoriteImage = (ImageView) findViewById(R.id.favorite_image);
        favoriteImage.setOnClickListener(favoriteListener);
        
        LinearLayout favoriteLayout = (LinearLayout) findViewById(R.id.favorite);
        favoriteLayout.setOnClickListener(favoriteListener);
        
        // States text and image
        /*
        TextView stateButton = (TextView) findViewById(R.id.state_text);
        stateButton.setOnClickListener(stateListener);
        
        ImageView stateImage = (ImageView) findViewById(R.id.state_image);
        stateImage.setOnClickListener(stateListener);
        */
        
        LinearLayout stateLayout = (LinearLayout) findViewById(R.id.state);
        stateLayout.setOnClickListener(stateListener);
        
        // Nearest areas text and image
        /*
        TextView nearestText = (TextView) findViewById(R.id.closest_text);
        nearestText.setOnClickListener(closestListener);
        
        ImageView nearestImage = (ImageView) findViewById(R.id.closest_image);
        nearestImage.setOnClickListener(closestListener);
        */
        
        LinearLayout nearestLayout = (LinearLayout) findViewById(R.id.closest);
        nearestLayout.setOnClickListener(closestListener);
        
        // Search text
        final EditText searchEdit = (EditText) findViewById(R.id.search_edit);

        // Capture key actions
        searchEdit.setOnKeyListener(new OnKeyListener() {
            
            /**
             * Capture key actions
             */
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                // If enter is pressed, do search
                if ((event.getAction() == KeyEvent.ACTION_DOWN) 
                    && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                	doSearch();
                    return true;
                    
                }
                
                return false;
            }
        });
        
        // Search button
        ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        searchButton.setOnClickListener(searchListener);
        
        // Start location manager
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        
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
     * On destory activity
     */
    public void onDestroy()
    {
        super.onDestroy();
        removeLocationListener();
    }
    
    /**
     * On start activity
     */
    public void onStart()
    {
        super.onStart();
        addLocationListener();
    }
    
    /**
     * On stop activity
     */
    public void onStop()
    {
        super.onStop();
        removeLocationListener();
    }
    
    /**
     * On restart activity
     */
    public void onRestart()
    {
        super.onRestart();
        addLocationListener();
    }
    
    /**
     * On pause activity
     */
    public void onPause()
    {
        super.onPause();
        removeLocationListener();
    }
    
    /**
     * On resume activity
     */
    public void onResume()
    {
        super.onResume();
        addLocationListener();
    }
    
    /**
     * On click listener for favorites button
     */
    private OnClickListener favoriteListener = new OnClickListener() {
        
        public void onClick(View v) {
            
            Intent i = new Intent(getApplicationContext(), FavoriteList.class);
            startActivity(i);
        }
        
    };
    
    /**
     * On click listener for favorites button
     */
    private OnClickListener searchListener = new OnClickListener() {
        
        public void onClick(View v) {
            
        	doSearch();

        }
        
    };
    
    /**
     * On click listener for states button
     */
    private OnClickListener stateListener = new OnClickListener() {
        
        public void onClick(View v) {
            launchStates();
        }
    };
    
    /**
     * On click listener for closest areas
     */
    private OnClickListener closestListener = new OnClickListener() {
    
        /**
         * On click
         */
        public void onClick(View v) {

            // If location cannot be determined, do something
            if (latitude == 0.0) {
                
                Toast.makeText(getBaseContext(), "Unable to determine location", Toast.LENGTH_SHORT).show();

            // Location has been determined, start area list activiy with lat/long info
            } else {
            
                Intent i = new Intent(getApplicationContext(), AreaList.class);
                i.putExtra("latitude", Double.toString(latitude));
                i.putExtra("longitude", Double.toString(longitude));
                startActivity(i);
                
            }
            
        }
    };
    
    private boolean doSearch() {

    	EditText searchEdit = (EditText) findViewById(R.id.search_edit);
    	String srch = searchEdit.getText().toString();
    	srch = URLEncoder.encode(srch);
    	
    	// Clean-up search string - URL encode
    	Intent i = new Intent(getApplicationContext(), AreaList.class);
        i.putExtra("srch", srch);
        startActivity(i);
        return true;
        
    }
    
    /**
     * Create options menu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        //menu.add(0, MENU_SUGGEST, 0, "Suggest Area");
        menu.add(0, MENU_ABOUT, 1, "About");
        return true;
    }
    
    /**
     * Handle options menu clicks
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        //case MENU_SUGGEST:
        //	Intent i = new Intent(getApplicationContext(), Tab.class);
        //    startActivity(i);
        //    return true;
        case MENU_ABOUT:
        	
        	Dialog dialog = new Dialog(this);

        	dialog.setContentView(R.layout.about);
        	dialog.setTitle("About");
        	
        	dialog.show();
            return true;
        }
        return false;

    }
    
    /**
     * Launches the Forecast activity to display a forecast
     */
    protected void launchForecast(String id) {
        Intent i = new Intent(getApplicationContext(), Area.class);
        i.putExtra("areaId", id);
        startActivity(i);
    }
    
    /**
     * Launches areas activity
     */
    protected void launchAreas() {
        Intent i = new Intent(this, AreaList.class);
        startActivity(i);
    }
    
    /**
     * Launches states activity
     */
    protected void launchStates() {
        Intent i = new Intent(this, StateList.class);
        startActivity(i);
    }
    
    /**
     * Launches states activity
     */
    protected void launchTest() {
        Intent i = new Intent(this, Area.class);
        i.putExtra("areaId", "3");
        startActivity(i);
    }
    
    /**
     * Location listener
     */
    private class MyLocationListener implements LocationListener 
    {
        /**
         * On location change, update lat/long
         */
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                latitude = loc.getLatitude();
                longitude = loc.getLongitude();
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
        lm.removeUpdates(locationListener);
    }

}

