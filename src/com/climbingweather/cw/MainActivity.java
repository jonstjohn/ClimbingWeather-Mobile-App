package com.climbingweather.cw;

import android.app.Dialog;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;

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
    
    private static final String[] CONTENT = new String[] { "Home", "Favorites", "Nearby", "By State", "Map" };
    
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
            if (position == 3) {
                if (stateFragment == null) {
                    stateFragment = new StateListFragment();
                }
                return stateFragment;
            } else if (position == 0) {
                if (searchFragment == null) {
                    searchFragment =  new AreaListFragment();
                    searchFragment.setType(AreaListFragment.TYPE_SEARCH);
                    searchFragment.setSearch("");
                }
                return searchFragment;
            } else if (position == 1) {
                if (favoriteFragment == null) {
                    favoriteFragment =  new AreaListFragment();
                    favoriteFragment.setType(AreaListFragment.TYPE_FAVORITE);
                }
                return favoriteFragment;
            } else if (position == 2) {
                if (nearbyFragment == null) {
                    nearbyFragment =  new AreaListFragment();
                    nearbyFragment.setType(AreaListFragment.TYPE_NEARBY);
                    nearbyFragment.setLocation(latitude, longitude);
                }
                return nearbyFragment;
            } else if (position == 4) {
                if (mapFragment == null) {
                    mapFragment = new AreaMapFragment();
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
            Logger.log(Integer.toString(pager.getCurrentItem()));
            ((DataFragmentInterface) adapter.getItem(pager.getCurrentItem())).refresh();
        }
        return false;

    }
    
}

