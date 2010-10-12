package com.climbingweather.cw;

//import com.google.maps;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class AreaMapActivity extends MapActivity {
	
	private MapController mapController;
	private MapView mapView;
	private GeoPoint areaPoint;
	//private LocationManager locationManager;
	
	private String areaId;
	
	/**
	 * Context
	 */
	private Context mContext;
	
    /**
     * Name
     */
    private String name;
	
    /**
     * Progress dialog
     */
    private ProgressDialog dialog;

	public void onCreate(Bundle bundle) {
		
		super.onCreate(bundle);
		
        // Show loading dialog
        dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        
        Bundle extras = getIntent().getExtras(); 
        areaId = extras.getString("areaId");
        
        mContext = this;
        
        // async task
        String url = "/api/area/detail/" + areaId;
        new GetJsonTask().execute(url);

	}
	
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
    /**
     * Asynchronous task to retrieve JSON
     */
    private class GetJsonTask extends AsyncTask<String, Void, String> {
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
            CwApi api = new CwApi(mContext);
            return api.getJson(args[0]);
        }
        
        /**
         * After execute, handle in UI thread
         */
        protected void onPostExecute(String result) {
            
            loadMap(result);
            
        }
    }
    
    /**
     * Load map from JSON
     */
    private void loadMap(String result) {

    	double latDbl;
    	double lonDbl;
    	int latitude;
    	int longitude;
    	
        try {
            
            JSONObject jsonObj = new JSONObject(result);
            
            name = jsonObj.getString("name");
            latDbl = Double.parseDouble(jsonObj.getString("latitude"));
            lonDbl = Double.parseDouble(jsonObj.getString("longitude"));
            
            latitude = (int) (latDbl * 1000000.00);
            longitude = (int) (lonDbl * 1000000.00);
            
			// create a map view
            setContentView(R.layout.area_map); // bind the layout to the activity
			mapView = (MapView) findViewById(R.id.mapview);
			mapView.setBuiltInZoomControls(true);
			//mapView.setStreetView(true);
			mapController = mapView.getController();
			mapController.setZoom(12); // Zoom 1 is world view
			
			areaPoint = new GeoPoint(latitude, longitude);
			mapController.setCenter(areaPoint);
			
			List<Overlay> mapOverlays = mapView.getOverlays();
			Drawable drawable = this.getResources().getDrawable(R.drawable.climbing);
			AreaMapItemizedOverlay itemizedoverlay = new AreaMapItemizedOverlay(drawable, this);
			
			OverlayItem marker = new OverlayItem(areaPoint, name, "");
			marker.getTitle();
			itemizedoverlay.addOverlay(marker);
			mapOverlays.add(itemizedoverlay);
            
        } catch (JSONException e) {
            
        	Toast.makeText(mContext, "An error occurred while retrieving map data", Toast.LENGTH_SHORT).show();
            
        }
        
		dialog.hide();
        
    }
    
    /**
     * Create menu options
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.area_menu, menu);
        MenuItem fav = menu.findItem(R.id.favorite);
        
        if (isFavorite()) {
            
            fav.setTitle("Remove Favorite");
            fav.setIcon(R.drawable.btn_star_big_off);

        } else {
            
            fav.setTitle("Add Favorite");
            fav.setIcon(R.drawable.btn_star_big_on);

        }
        return true;
    }
    
    /**
     * Handle menu clicks
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.favorite:
            if (isFavorite()) {
                removeFavorite();
            } else {
                saveFavorite();
            }
            return true;
        }
        return false;

    }
    
    /**
     * Setup menu dynamically
     */
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem fav = menu.findItem(R.id.favorite);
        
        if (isFavorite()) {
            fav.setTitle("Remove Favorite");
            fav.setIcon(R.drawable.btn_star_big_off);
        } else {
            fav.setTitle("Add Favorite");
            fav.setIcon(R.drawable.btn_star_big_on);
        }
        return true;
    }
    
    /**
     * Save favorite area
     */
    public void saveFavorite()
    {
        FavoriteDbAdapter dbAdapter = new FavoriteDbAdapter(this);
        dbAdapter.open();
        dbAdapter.addFavorite(Integer.parseInt(areaId), name);
        
    }
    
    /**
     * Remove favorite area
     */
    public void removeFavorite() {
        
        FavoriteDbAdapter dbAdapter = new FavoriteDbAdapter(this);
        dbAdapter.open();
        dbAdapter.removeFavorite(Integer.parseInt(areaId));
        
    }
    
    /**
     * Check to see if area is already a favorite
     */
    public boolean isFavorite() {

        FavoriteDbAdapter dbAdapter = new FavoriteDbAdapter(this);
        dbAdapter.open();
        boolean isFavorite = dbAdapter.isFavorite(Integer.parseInt(areaId));
        return isFavorite;

    }
	
}
