package com.climbingweather.cw;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.climbingweather.cw.MainActivity.CwPagerAdapter;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.viewpagerindicator.TabPageIndicator;

public class AreaFragmentActivity extends SherlockFragmentActivity
{
    private static final String[] CONTENT = new String[] { "Forecast", "Reports", "Averages", "Map" };
    
    private String areaId;
    
    private String name;
    
    private Area area;
    
    public Tracker mGaTracker;
    private GoogleAnalytics mGaInstance;
    
    private ViewPager pager;
    
    private AreaPagerAdapter adapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        Bundle extras = getIntent().getExtras();
        areaId = extras.getString("areaId");
        name = extras.getString("name");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        getSupportActionBar().setTitle(name);
        
        setContentView(R.layout.simple_tabs);
      
        adapter = new AreaPagerAdapter(getSupportFragmentManager());
        
        Log.i("CW", new Boolean(adapter == null).toString());

        pager = (ViewPager)findViewById(R.id.pager);
        
        Log.i("CW", new Boolean(pager == null).toString());
        
        pager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        
        mGaInstance = GoogleAnalytics.getInstance(this);
        mGaTracker = mGaInstance.getTracker("UA-205323-8");
        
        GetDetailJsonTask task = new GetDetailJsonTask();
        task.execute();
    }

    /**
     * Area Pager adapter
     */
    class AreaPagerAdapter extends FragmentPagerAdapter {
        
        private ForecastListFragment forecastFragment;
        private AreaAverageFragment averageFragment;
        private AreaMapFragment mapFragment;
        
        /**
         * Constructor
         * @param fm
         */
        public AreaPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (forecastFragment == null) {
                    forecastFragment = new ForecastListFragment();
                }
                return forecastFragment;
            } else if (position == 2) {
                if (averageFragment == null) {
                    averageFragment = new AreaAverageFragment();
                }
                return averageFragment;
            } else if (position == 3) {
                if (mapFragment == null) {
                    if (area != null) {
                        mapFragment = AreaMapFragment.newInstance(area.getLatitude(), area.getLongitude(), 10.0f);
                    } else {
                        mapFragment = new AreaMapFragment();
                    }
                }
                return mapFragment;
            } else {
                return TestFragment.newInstance(CONTENT[position % CONTENT.length]);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount() {
          return CONTENT.length;
        }
    }
    
    /**
     * Create menu options
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.area_menu, menu);
        MenuItem fav = menu.findItem(R.id.favorite);
        
        if (isFavorite()) {
            Log.i("CW", "Is favorite onCreateOptionsMenu()");
            fav.setTitle("Remove Favorite");
            fav.setIcon(R.drawable.btn_star_big_on);

        } else {
            Log.i("CW", "Is not favorite onCreateOptionsMenu()");
            fav.setTitle("Add Favorite");
            fav.setIcon(R.drawable.btn_star_big_off);

        }
        return true;
    }
    
    /**
     * Handle menu clicks
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Home
            case android.R.id.home:
                finish();
                return true;
            case R.id.favorite:
    
                
                if (isFavorite()) {
                    removeFavorite();
                    item.setTitle("Add Favorite");
                    item.setIcon(R.drawable.btn_star_big_off);
                } else {
                    saveFavorite();
                    item.setTitle("Remove Favorite");
                    item.setIcon(R.drawable.btn_star_big_on);
                }
                return true;
            case R.id.refresh:
                ((DataFragmentInterface) adapter.getItem(pager.getCurrentItem())).refresh();
                return true;
            case R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                return true;
            case R.id.settings:
                Intent i = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(i);
                return true;
        }
        return false;

    }
    
    /**
     * Setup menu dynamically
     * @return 
     */
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem fav = menu.findItem(R.id.favorite);
        
        if (isFavorite()) {
            fav.setTitle("Remove Favorite");
            fav.setIcon(R.drawable.btn_star_big_on);
        } else {
            fav.setTitle("Add Favorite");
            fav.setIcon(R.drawable.btn_star_big_off);
        }
        return true;
    }
    
    /**
     * Save favorite area
     */
    public void saveFavorite()
    {
        ContentValues values = new ContentValues();
        values.put(FavoritesContract.Columns.AREA_ID, Integer.parseInt(areaId));
        values.put(FavoritesContract.Columns.NAME, name);
        getContentResolver().insert(
                FavoritesContract.CONTENT_URI, values);
    }
    
    /**
     * Remove favorite area
     */
    public void removeFavorite() {
        
        Uri uri = Uri.parse(FavoritesContract.CONTENT_URI.toString() + "/" + areaId);
        Logger.log(uri.toString());
        getContentResolver().delete(
                uri, null, null);
    }
    
    /**
     * Check to see if area is already a favorite
     */
    public boolean isFavorite() {
        // Get ids from content provider
        Cursor cursor = getContentResolver().query(
                FavoritesContract.CONTENT_URI, null, null, null, null);
        Logger.log("isFavorite()");
        Logger.log(areaId);
        while (cursor.moveToNext()) {
            Logger.log(cursor.getString(cursor.getColumnIndex(FavoritesContract.Columns.AREA_ID)));
            if (cursor.getString(cursor.getColumnIndex(FavoritesContract.Columns.AREA_ID)).equals(areaId)) {
                return true;
            }
        }
        
        return false;
    }
    
    public String getAreaId()
    {
        return areaId;
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetDetailJsonTask extends AsyncTask<String, Void, String>
    {
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
              
              CwApi api = new CwApi(AreaFragmentActivity.this, "2.0");
              String url = "/area/detail/" + areaId;
              Logger.log(url);
              return api.getJson(url);

        }
        
        protected void onPreExecute() {
            AreaFragmentActivity.this.setProgressBarIndeterminateVisibility(Boolean.TRUE); 
        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            AreaFragmentActivity.this.setProgressBarIndeterminateVisibility(Boolean.FALSE); 
            processJson(result);
        }
        
        private void processJson(String result)
        {
            Logger.log("AreaMapFragment processJson()");
            try {
                Gson gson = new Gson();
                CwApiAreaDetailResponse apiResponse = gson.fromJson(result,  CwApiAreaDetailResponse.class);

                area = apiResponse.getArea();
                
            } catch (JsonParseException e) {
                Toast.makeText(
                        AreaFragmentActivity.this, "An error occurred while retrieving area detail", Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
    
    public Area getArea()
    {
        return area;
    }
}
