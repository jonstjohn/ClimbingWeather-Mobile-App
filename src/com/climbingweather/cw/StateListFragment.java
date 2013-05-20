package com.climbingweather.cw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
// import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockListFragment;

public class StateListFragment extends ExpandableListFragment {
	
	   /**
     * State array list
     */
    ArrayList<HashMap<String,String>> states = new ArrayList<HashMap<String,String>>();
    
    /**
     * States JSON
     */
    String stateJson = "[['AL','Alabama'], ['AK','Alaska'], ['AZ','Arizona'], ['AR','Arkansas'], ['CA','California'], ['CO','Colorado'], ['CT','Connecticut'], ['DE','Delaware'], ['FL','Florida'], ['GA','Georgia'], ['HI','Hawaii'], ['ID','Idaho'], ['IL','Illinois'], ['IN','Indiana'], ['IA','Iowa'], ['KS','Kansas'], ['KY','Kentucky'], ['LA','Louisiana'], ['ME','Maine'], ['MD','Maryland'], ['MA','Massachusetts'], ['MI','Michigan'], ['MN','Minnesota'], ['MS','Mississippi'], ['MO','Missouri'], ['MT','Montana'], ['NE','Nebraska'], ['NV','Nevada'], ['NH','New Hampshire'], ['NJ','New Jersey'], ['NM','New Mexico'], ['NY','New York'], ['NC','North Carolina'], ['ND','North Dakota'], ['OH','Ohio'], ['OK','Oklahoma'], ['OR','Oregon'], ['PA','Pennsylvania'], ['RI','Rhode Island'], ['SC','South Carolina'], ['SD','South Dakota'], ['TN','Tennessee'], ['TX','Texas'], ['UT','Utah'], ['VT','Vermont'], ['VA','Virginia'], ['WA','Washington'], ['WV','West Virginia'], ['WI','Wisconsin'], ['WY','Wyoming']]";

    /**
     * Adapter
     */
    ExpandableListAdapter stateAdapter;
    
    private Context mContext;
    
    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        mContext = getActivity();
        
        // setContentView(R.layout.list);
        //inflater.inflate(R.layout.list, null);
        
        return inflater.inflate(R.layout.list_expandable, null);
        
    }
    
    public void onActivityCreated (Bundle savedInstanceState) {
    	
    	super.onActivityCreated(savedInstanceState);

    	
    	ListView lv = getListView();
        
        // Set header row text
        LayoutInflater inflater = LayoutInflater.from(getActivity());
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
                states.add(map);
            }
        
        } catch (JSONException e) {
            
        	Toast.makeText(
        		getActivity(), "An error occurred while retrieving state data", Toast.LENGTH_SHORT
        	).show();
            
        }
        
        if (stateAdapter == null) {
            
            new GetStatesJsonTask().execute("/api/state/list");
        
	        // Use simple adapter to display states
            //stateAdapter = new StateExpandableListAdapter(this.getActivity());
            /*
	        stateAdapter = new SimpleExpandableListAdapter(
	                getActivity(),
	                states,
	                R.layout.list_row,
	                new String[] { "name"},
	                new int[] { R.id.name }
	        );
	        */
	        
        }
          
        //setListAdapter(stateAdapter);
        
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
                
                Intent i = new Intent(getActivity().getApplicationContext(), AreaListActivity.class);
                i.putExtra("stateCode", stateCode);
                startActivity(i);
            }
        });
    }
    
    @Override
    public void onDestroyView()
    {
    	super.onDestroyView();
    	setListAdapter(null);
    }
    
    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids. 
     * Each photo is displayed as an image. This adapter supports clearing the
     * list of photos and adding a new photo.
     *
     */
    public class StateExpandableListAdapter extends BaseExpandableListAdapter {
        // Sample data set.  children[i] contains the children (String[]) for groups[i].
        //private String[] groups = { "People Names", "Dog Names", "Cat Names", "Fish Names" };
        /*private String[][] children = {
                { "Arnold", "Barry", "Chuck", "David" },
                { "Ace", "Bandit", "Cha-Cha", "Deuce" },
                { "Fluffy", "Snuggles" },
                { "Goldy", "Bubbles" }
        };*/
        
        // State codes
        private ArrayList<String> stateCodes = new ArrayList<String>();
        
        // Area ids indexed by state code
        private ArrayList<ArrayList<String>> areaIds = new ArrayList<ArrayList<String>>();
        
        // State data
        private HashMap<String, HashMap<String, String>> stateData = new HashMap<String, HashMap<String, String>>();
        
        // State areas
        private HashMap<String, String[]> stateAreas = new HashMap<String, String[]>();;
        
        // Area data
        private HashMap<String, HashMap<String, String>> areaData;
        
        /*
         * stateAreas = { 'AZ': {'name': 'Arizona', 'areaCount': '10', 'areas': { '10': {'name': 'New River' .... } } } }
         * 
         */
        
        private LayoutInflater inflater;
        
        public StateExpandableListAdapter(Context context, ArrayList<String> groups, HashMap<String, HashMap<String, String>> localStateData)
        {
            inflater = LayoutInflater.from(context);
            stateCodes = groups;
            stateData = localStateData;
        }
        
        public Object getChild(int groupPosition, int childPosition) {
            if (areaIds.size() == 0 || areaIds.get(groupPosition).size() == 0) {
                return null;
            }
            
            return areaIds.get(groupPosition).get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return 1;
            //return areaIds[groupPosition].length;
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 64);

            TextView textView = new TextView(StateListFragment.this.getActivity());
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            /*
            TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;
            *
            */
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_child, parent,false);
            }
     
            TextView textView = (TextView) convertView.findViewById(R.id.list_item_text_child);
            //"i" is the position of the parent/group in the list and 
            //"i1" is the position of the child
            
            if (getChild(groupPosition, childPosition) != null) {
                textView.setText(getChild(groupPosition, childPosition).toString());
            }
     
            //return the entire view
            return convertView;
        }

        public Object getGroup(int groupPosition) {
            return stateCodes.get(groupPosition);
        }

        public int getGroupCount() {
            return stateCodes.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_parent, parent,false);
            }
     
            TextView textView = (TextView) convertView.findViewById(R.id.list_item_text_view);
            //"i" is the position of the parent/group in the list
            
            String stateCode = getGroup(groupPosition).toString();
            
            //Log.i("CW", stateCode);
            
            textView.setText(stateData.get(stateCode).get("name").toString());
     
            //return the entire view
            return convertView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }
        
        // On group expanded
        public void onGroupExpanded (int groupPosition)
        {
            // Check for state areas
            String stateCode = getGroup(groupPosition).toString();
            if (!stateAreas.containsKey(stateCode)) {
                // Load async
                new GetAreasJsonTask(stateCode).execute("/api/state/area/" + stateCode);
            }
        }

    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetStatesJsonTask extends AsyncTask<String, Void, String> {
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
            
              CwApi api = new CwApi(mContext);
              return api.getJson(args[0]);

        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            try {
                
                // Convert result into JSONArray
                JSONArray json = new JSONArray(result);
              
                // Loop over JSONarray
                ArrayList<String> stateCodeList = new ArrayList<String>();
                HashMap<String, HashMap<String, String>> stateData = new HashMap<String, HashMap<String, String>>();
                
                for (int i = 0; i < json.length(); i++) {
                //for (int i = 0; i < 4; i++) {
                
                    // Get JSONObject from current array element
                    JSONObject areaObject = json.getJSONObject(i);
                    
                    String stateCode = areaObject.getString("code");
                    String name = areaObject.getString("name");
                    
                    // Add state to name, if present
                    //if (areaObject.has("state")) {
                    //    name += " (" + areaObject.getString("state") +")";
                    //
                    
                    //}
                    // Add state to hashmap
                    HashMap<String, String> thisStateData = new HashMap<String,String>();
                    thisStateData.put("code", stateCode);
                    thisStateData.put("name", name);
                    stateData.put(stateCode, thisStateData);
                    
                    stateCodeList.add(stateCode);
                    //areaList.add(map);
                    
                    String[] namesStrings = new String[stateCodeList.size()];
                    String[] stateCodes = stateCodeList.toArray(namesStrings);
                    
                    stateAdapter = new StateExpandableListAdapter(
                            mContext,
                            stateCodeList,
                            stateData
                        );
                        setListAdapter(stateAdapter);
                }
              
            } catch (JSONException e) {
              
                Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
              
            }
        }
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetAreasJsonTask extends AsyncTask<String, Void, String> {
        
        private String stateCode;
        
        public GetAreasJsonTask(String mStateCode)
        {
            stateCode = mStateCode;
        }
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
            
              CwApi api = new CwApi(mContext);
              return api.getJson(args[0]);

        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            //try {
                
                Log.i("CW", result);
                
                /*
                // Convert result into JSONArray
                JSONArray json = new JSONArray(result);
              
                // Loop over JSONarray
                List<String> stateCodeList = new ArrayList<String>();
                HashMap<String, HashMap<String, String>> stateData = new HashMap<String, HashMap<String, String>>();
                
                for (int i = 0; i < json.length(); i++) {
                //for (int i = 0; i < 4; i++) {
                
                    // Get JSONObject from current array element
                    JSONObject areaObject = json.getJSONObject(i);
                    
                    String stateCode = areaObject.getString("code");
                    String name = areaObject.getString("name");
                    
                    // Add state to name, if present
                    //if (areaObject.has("state")) {
                    //    name += " (" + areaObject.getString("state") +")";
                    //
                    
                    //}
                    // Add state to hashmap
                    HashMap<String, String> thisStateData = new HashMap<String,String>();
                    thisStateData.put("code", stateCode);
                    thisStateData.put("name", name);
                    stateData.put(stateCode, thisStateData);
                    
                    stateCodeList.add(stateCode);
                    //areaList.add(map);
                    
                    String[] namesStrings = new String[stateCodeList.size()];
                    String[] stateCodes = stateCodeList.toArray(namesStrings);
                    
                    stateAdapter = new StateExpandableListAdapter(
                            mContext,
                            stateCodes,
                            stateData
                        );
                        setListAdapter(stateAdapter);
                }
                */
              
            /*} catch (JSONException e) {
              
                Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
              
            }*/
        }
    }

}
