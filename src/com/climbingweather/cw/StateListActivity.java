package com.climbingweather.cw;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import com.climbingweather.cw.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Display list of states
 */
public class StateListActivity extends ListActivity {
    
    /**
     * State array list
     */
    ArrayList<HashMap<String,String>> states = new ArrayList<HashMap<String,String>>();
    
    /**
     * States JSON
     */
    String stateJson = "[['AL','Alabama'], ['AK','Alaska'], ['AZ','Arizona'], ['AR','Arkansas'], ['CA','California'], ['CO','Colorado'], ['CT','Connecticut'], ['DE','Delaware'], ['FL','Florida'], ['GA','Georgia'], ['HI','Hawaii'], ['ID','Idaho'], ['IL','Illinois'], ['IN','Indiana'], ['IA','Iowa'], ['KS','Kansas'], ['KY','Kentucky'], ['LA','Louisiana'], ['ME','Maine'], ['MD','Maryland'], ['MA','Massachusetts'], ['MI','Michigan'], ['MN','Minnesota'], ['MS','Mississippi'], ['MO','Missouri'], ['MT','Montana'], ['NE','Nebraska'], ['NV','Nevada'], ['NH','New Hampshire'], ['NJ','New Jersey'], ['NM','New Mexico'], ['NY','New York'], ['NC','North Carolina'], ['ND','North Dakota'], ['OH','Ohio'], ['OK','Oklahoma'], ['OR','Oregon'], ['PA','Pennsylvania'], ['RI','Rhode Island'], ['SC','South Carolina'], ['SD','South Dakota'], ['TN','Tennessee'], ['TX','Texas'], ['UT','Utah'], ['VT','Vermont'], ['VA','Virginia'], ['WA','Washington'], ['WV','West Virginia'], ['WI','Wisconsin'], ['WY','Wyoming']]";

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.list);
        
        ListView lv = getListView();
        
        // Set header row text
        LayoutInflater inflater = getLayoutInflater();
        TextView headerView = (TextView) inflater.inflate(R.layout.header_row, null);
        headerView.setText("US States");
        lv.addHeaderView(headerView);
        
        // Parse JSON
        try {
            
            JSONArray json = new JSONArray(stateJson);
            states.clear();
            for (int i = 0; i < json.length(); i++)
            {
                // String values = valArray.getString(i);
                JSONArray stateArray = new JSONArray();
                stateArray = json.getJSONArray(i);
                
                String code = stateArray.getString(0);
                String name = stateArray.getString(1);
                
                HashMap<String, String> map = new HashMap<String,String>();
                map.put("code", code);
                map.put("name", name);
                Log.i("CW", map.toString());
                states.add(map);
            }
        
        } catch (JSONException e) {
            
            e.printStackTrace();
            
        }
        
        // Use simple adapter to display states
        SimpleAdapter stateAdapter = new SimpleAdapter(
                this,
                states,
                R.layout.list_row,
                new String[] { "name"},
                new int[] { R.id.name }
        );
          
        setListAdapter(stateAdapter);

        lv.setTextFilterEnabled(true);
        
        // Set on item click listener
        lv.setOnItemClickListener(new OnItemClickListener() {
            
            /**
             * On item click, start state list activity
             */
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                Object item = parent.getItemAtPosition(position);
                HashMap<String, String> hashMap = (HashMap<String, String>) item;
                String stateCode = hashMap.get("code");
                
                Intent i = new Intent(getApplicationContext(), AreaListActivity.class);
                i.putExtra("stateCode", stateCode);
                startActivity(i);
            }
        });
        
    }
}
