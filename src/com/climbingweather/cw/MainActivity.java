package com.climbingweather.cw;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends SherlockFragmentActivity {
    
    private static final String[] CONTENT = new String[] { "Favorites", "Nearby", "By State", "Search", "Map" };
    
    /**
     * User latitude
     */
    private double latitude;
    
    /**
     * User longitude
     */
    private double longitude;
    
    private CwViewPager pager;
    
    private CwPagerAdapter adapter;
    
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.simple_tabs);
        
        adapter = new CwPagerAdapter(getSupportFragmentManager());

        pager = (CwViewPager)findViewById(R.id.pager);
        
        pager.setAdapter(adapter);
        
        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        
        // Listen for page changes
        indicator.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                // Hide soft keyboard on tab change
                EditText editText = (EditText) findViewById(R.id.search);
                if (editText != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
        });

        indicator.setViewPager(pager);
        
    }
    
    /**
     * CW Pager adapter
     */
    class CwPagerAdapter extends FragmentPagerAdapter {
        
        private AreaListFragment searchFragment;
        private AreaListFragment favoriteFragment;
        private AreaListFragment nearbyFragment;
        private StateListFragment stateFragment;
        private AreaMapFragment mapFragment;
        /**
         * Constructor
         * @param fm
         */
        public CwPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // State
            if (position == 2) {
                if (stateFragment == null) {
                    stateFragment = new StateListFragment();
                }
                return stateFragment;
            // Search
            } else if (position == 3) {
                if (searchFragment == null) {
                    searchFragment =  AreaListFragment.newInstance(AreaListFragment.TYPE_SEARCH);
                }
                return searchFragment;
            // Favorite
            } else if (position == 0) {
                if (favoriteFragment == null) {
                    favoriteFragment =  AreaListFragment.newInstance(AreaListFragment.TYPE_FAVORITE);
                }
                return favoriteFragment;
            // Nearby
            } else if (position == 1) {
                if (nearbyFragment == null) {
                    Logger.log("Put main nearby latitude longitude " + Double.toString(latitude) + " " + Double.toString(longitude));
                    nearbyFragment =  AreaListFragment.newInstance(AreaListFragment.TYPE_NEARBY, latitude, longitude);
                }
                return nearbyFragment;
            // Map
            } else if (position == 4) {
                if (mapFragment == null) {
                    Logger.log("Put main latitude longitude " + Double.toString(latitude) + " " + Double.toString(longitude));
                    mapFragment = AreaMapFragment.newInstance(latitude, longitude, 8.0f);
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
        
        public void finishUpdate(ViewGroup container)
        {
            super.finishUpdate(container);
        }
    }
    
    /**
     * On destroy activity
     */
    public void onDestroy()
    {
        super.onDestroy();
    }
    
    /**
     * On start activity
     */
    public void onStart()
    {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
        
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
        
    }
    
    /**
     * On stop activity
     */
    public void onStop()
    {
        super.onStop();
    }
    
    /**
     * On restart activity
     */
    public void onRestart()
    {
        super.onRestart();
    }
    
    /**
     * On pause activity
     */
    public void onPause()
    {
        super.onPause();
    }
    
    /**
     * On resume activity
     */
    public void onResume()
    {
        super.onResume();
    }
    
    /**
     * Create options menu
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    /**
     * Handle options menu clicks
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
        case R.id.about:
            
            Dialog dialog = new Dialog(this);

            dialog.setContentView(R.layout.about);
            dialog.setTitle("About");
            
            dialog.show();
            return true;
        case R.id.settings:
            Intent i = new Intent(getApplicationContext(), PreferencesActivity.class);
            startActivity(i);
            return true;
        case R.id.refresh:
            ((DataFragmentInterface) adapter.getItem(pager.getCurrentItem())).refresh();
            return true;
        }
        return false;

    }
    
}

