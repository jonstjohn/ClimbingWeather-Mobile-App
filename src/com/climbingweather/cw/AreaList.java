package com.climbingweather.cw;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.climbingweather.cw.R;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Displays a clickable list of areas
 */
public class AreaList extends ListActivity {
    
    /**
     * Area list
     */
    ArrayList<HashMap<String,String>> areaList = new ArrayList<HashMap<String,String>>();
    
    /**
     * Progress dialog for loading
     */
    private ProgressDialog dialog;
    
    /**
     * Text to display if no areas are found
     */
    private String noneText = "No areas found";
    
    /**
     * The empty area view
     */
    private TextView emptyView;

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
      super.onCreate(savedInstanceState);
      
      // Grab passed in info
      Bundle extras = getIntent().getExtras();
      
      // Determine JSON request URL
      String url = "";
      if (extras.containsKey("stateCode")) { // state search
          
          url = "/api/area/" + extras.getString("stateCode");
          noneText = "No areas for this state";
          
      } else if (extras.containsKey("latitude")) { // lat/long search
          
          url = "/api/area/search/ll=" + extras.getString("latitude") + "," + extras.getString("longitude");
          noneText = "Unable to locate nearby areas";
          
      } else if (extras.containsKey("srch")) { // keyword search
          
          url = "/api/area/search/" + extras.getString("srch");
          noneText = "No areas found for the search";
          
      }

      setContentView(R.layout.list);
      
      ListView lv = getListView();
      
      // Inflate header row so we can set some custom text
      LayoutInflater inflater = getLayoutInflater();
      TextView headerView = (TextView) inflater.inflate(R.layout.header_row, null);
      headerView.setText("Climbing Areas");
      lv.addHeaderView(headerView);
      
      // Clear out empty row
      emptyView = ((TextView) lv.getEmptyView());
      emptyView.setText("");
      
      // Show loading dialog
      dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
      
      // async task
      new GetJsonTask().execute(url);
      
    }
    
    /**
     * Asynchronous get JSON task
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
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            loadAreas(result);
        }
    }
    
    /**
     * Load areas from JSON string result
     */
    public void loadAreas(String result) {
    
        try {
          
            // Convert result into JSONArray
            JSONArray json = new JSONArray(result);
          
            // Loop over JSONarray
            for (int i = 0; i < json.length(); i++) {
            
                // Get JSONObject from current array element
                JSONObject areaObject = json.getJSONObject(i);
                
                String areaId = areaObject.getString("id");
                String name = areaObject.getString("name");
                
                // Add state to name, if present
                if (areaObject.has("state")) {
                    name += " (" + areaObject.getString("state") +")";
                }
                
                // Add state to hashmap
                HashMap<String, String> map = new HashMap<String,String>();
                map.put("areaId", areaId);
                map.put("name", name);
                areaList.add(map);
            }
          
        } catch (JSONException e) {
          
            e.printStackTrace();
          
        }
    
        // Create simple adapter using hashmap
        SimpleAdapter areas = new SimpleAdapter(
            this,
            areaList,
            R.layout.list_row,
            new String[] { "name"},
            new int[] { R.id.name }
        );
      
      setListAdapter(areas);
    
      ListView lv = getListView();
      lv.setTextFilterEnabled(true);
      
      // Populate empty row in case we didn't find any areas
      emptyView.setText(noneText);
      
      // Set on item click listener for states
      lv.setOnItemClickListener(new OnItemClickListener() {
          
          /**
           * On item click action, open area activity
           */
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              
              // Get object from item position
              Object item = parent.getItemAtPosition(position);
              HashMap<String, String> hashMap = (HashMap<String, String>) item;
              String str = hashMap.get("areaId"); // id
        
              Intent i = new Intent(getApplicationContext(), Area.class);
              i.putExtra("areaId", str);
              startActivity(i);
          }
          
      });
      
      dialog.hide();
      
    }
    
}
