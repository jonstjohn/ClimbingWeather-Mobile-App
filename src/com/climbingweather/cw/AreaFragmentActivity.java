package com.climbingweather.cw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.climbingweather.cw.MainActivity.CwPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;

public class AreaFragmentActivity extends SherlockFragmentActivity
{
    private static final String[] CONTENT = new String[] { "Forecast", "Reports", "Averages", "Map" };
    
    private String areaId;
    
    private String name;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        areaId = extras.getString("areaId");
        name = extras.getString("name");
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.simple_tabs);
      
        FragmentPagerAdapter adapter = new AreaPagerAdapter(getSupportFragmentManager());
        
        Log.i("CW", new Boolean(adapter == null).toString());

        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        
        Log.i("CW", new Boolean(pager == null).toString());
        
        pager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
    }

    /**
     * Area Pager adapter
     */
    class AreaPagerAdapter extends FragmentPagerAdapter {
        
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
                return new ForecastListFragment();
            } else if (position == 11) {
                return new FavoriteListFragment();
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
                // refresh();
                return true;
            case R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                return true;
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
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
