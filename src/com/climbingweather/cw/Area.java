package com.climbingweather.cw;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.climbingweather.cw.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Area/forecast page activity
 */
public class Area extends Activity {
    
    /**
     * Menu item for favorite
     */
    private static final int MENU_FAVORITE = 0;
    
    /**
     * Menu item to remove favorite
     */
    private static final int MENU_REMOVE_FAVORITE = 1;
    
    /**
     * Area id
     */
    private String areaId;
    
    /**
     * Name
     */
    private String name;
    
    /**
     * Progress dialog
     */
    private ProgressDialog dialog;
    
    /** 
     * On create 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras(); 
        areaId = extras.getString("areaId");
        
        String url = "/api/area/" + areaId;
        
        name = "";
        
        setContentView(R.layout.forecast_table);
        
        // Show loading dialog
        dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        
        // async task
        new GetJsonTask().execute(url);
        
    }
    

    /**
     * Create menu options
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isFavorite()) {
            menu.add(0, MENU_REMOVE_FAVORITE, 0, "Remove Favorite");
        } else {
            menu.add(0, MENU_FAVORITE, 0, "Add to Favorite");
        }
        return true;
    }
    
    /**
     * Handle menu clicks
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_FAVORITE:
            saveFavorite();
            return true;
        case MENU_REMOVE_FAVORITE:
            removeFavorite();
            return true;
        }
        return false;

    }
    
    /**
     * Setup menu dynamically
     */
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(MENU_FAVORITE);
        menu.removeItem(MENU_REMOVE_FAVORITE);
        if (isFavorite()) {
            menu.add(0, MENU_REMOVE_FAVORITE, 0, "Remove Favorite");
        } else {
            menu.add(0, MENU_FAVORITE, 0, "Add to Favorite");
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
    
    /**
     * Asynchronous task to retrieve JSON
     */
    private class GetJsonTask extends AsyncTask<String, Void, String> {
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
              HttpToJson toJson = new HttpToJson();
              String result = toJson.getJsonFromUrl(args[0]);
              return result;
        }
        
        /**
         * After execute, handle in UI thread
         */
        protected void onPostExecute(String result) {
            
            loadForecast(result);
            
        }
    }
    
    /**
     * Load forecast from JSON
     */
    private void loadForecast(String result) {

        TableLayout table = (TableLayout) findViewById(R.id.forecast_table);
        
        LayoutInflater inflater = getLayoutInflater();
        
        try {
            
            JSONObject jsonObj = new JSONObject(result);
            
            name = jsonObj.getString("n");
            //((TextView)findViewById(R.id.name)).setText(name);
            JSONArray forecastData = jsonObj.getJSONArray("f");
            
            // Loop over forecast data
            for (int i = 0; i < forecastData.length(); i++)
            {
                JSONObject dayData = forecastData.getJSONObject(i);
                
                // Set values of table row
                TableRow row = (TableRow)inflater.inflate(R.layout.forecast_row, table, false);
                
                ((TextView)row.findViewById(R.id.day)).setText(dayData.getString("dy"));
                ((TextView)row.findViewById(R.id.high)).setText(dayData.getString("hi") + "\u00b0");
                ((TextView)row.findViewById(R.id.low)).setText(dayData.getString("l") + "\u00b0");
                ((TextView)row.findViewById(R.id.date)).setText(dayData.getString("dd"));
                ((TextView)row.findViewById(R.id.precipDay)).setText(dayData.getString("pd") + "%");
                if (dayData.getString("pn") != null) {
                    ((TextView)row.findViewById(R.id.precipNight)).setText(dayData.getString("pn") + "%");
                }
                ((TextView)row.findViewById(R.id.humidity)).setText(dayData.getString("h") + "%");
                ((TextView)row.findViewById(R.id.wind)).setText(dayData.getString("ws") + " mph");
                
                String symbol = dayData.getString("sy").replace(".png", "");
                ((ImageView)row.findViewById(R.id.symbol)).setImageResource(getResources().getIdentifier(symbol, "drawable", "com.climbingweather.cw"));
                
                /*
                row.setClickable(true);
                row.setId(i);
                if (i % 2 == 1) {
                    row.setBackgroundResource(R.color.silver);
                }
                row.setOnClickListener(new View.OnClickListener() {
                    
                    public void onClick(View view) {
                        
                        Intent intent = new Intent(getApplicationContext(), Hourly.class);
                        intent.putExtra("areaId", areaId);
                        intent.putExtra("dayIndex", new Integer(view.getId()).toString());
                        startActivity(intent);
                        
                    }
                    
                });
                */
                table.addView(row);
                
                String conditions = dayData.getString("c");
                
                if (conditions.length() > 0) {
                
                    TableRow conditionsRow = (TableRow)inflater.inflate(R.layout.conditions_row, table, false);
                    ((TextView)conditionsRow.findViewById(R.id.weather)).setText(conditions);
                
                    if (i % 2 == 1) {
                        conditionsRow.setBackgroundResource(R.color.silver);
                    }
                    
                    table.addView(conditionsRow);
                    
                }
                
            }
            
        } catch (JSONException e) {
            
            e.printStackTrace();
            
        }
        
        dialog.hide();
        
    }
    
}
