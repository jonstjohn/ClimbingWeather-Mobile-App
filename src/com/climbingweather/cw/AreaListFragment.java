package com.climbingweather.cw;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AreaListFragment extends SherlockListFragment {

    private Area[] areas;
    
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
     * Context
     */
    private Context mContext;
    
    /**
     * Area list type (see constants)
     */
    private int typeId;
    
    public static final int TYPE_NEARBY = 1;
    
    public static final int TYPE_SEARCH = 2;
    
    public static final int TYPE_FAVORITE = 3;
    
    private double latitude;
    
    private double longitude;
    
    private String search;

    /**
     * On create
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
      super.onCreate(savedInstanceState);
      
      /*
      // Grab passed in info
      Bundle extras = getActivity().getIntent().getExtras();
      
      // Determine JSON request URL
      String url = "";
      if (extras.containsKey("stateCode")) { // state search
          
          url = "/api/state/area/" + extras.getString("stateCode");
          noneText = "No areas for this state";
          
      } else if (extras.containsKey("latitude")) { // lat/long search
          
          url = "/api/area/search/ll=" + extras.getString("latitude") + "," + extras.getString("longitude");
          noneText = "Unable to locate nearby areas";
          
      } else if (extras.containsKey("srch")) { // keyword search
          
          url = "/api/area/search/" + extras.getString("srch");
          noneText = "No areas found for the search";
          
      }
      */
    }
    
    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        Log.i("CW", "AreaListFragment onCreateView()");
        super.onCreate(savedInstanceState);
        
        mContext = getActivity();
        
        String url = "";
        
        switch (typeId) {
            case TYPE_NEARBY:
                url = "/api/area/search/ll=" + Double.toString(latitude) + "," + Double.toString(longitude) + "?days=3";
                Log.i("CW", url);
                break;
        }

        mContext = getActivity();
        
        // async task
        new GetAreasJsonTask(this).execute(url);
        
        return inflater.inflate(R.layout.list, null);
        
    }
    
    /**
     * On pause activity
     */
    public void onPause()
    {
        super.onPause();
        //dialog.dismiss();
    }
    
    public void onStop()
    {
        super.onStop();
        //dialog.dismiss();
    }
    
    /**
     * Asynchronous get JSON task
     */
    private class GetAreasJsonTask extends AsyncTask<String, Void, String> {
        
        private SherlockListFragment listFragment;
        
        public GetAreasJsonTask(SherlockListFragment listFragment) {
            this.listFragment = listFragment;
            //dialog = new ProgressDialog(listFragment.getActivity());
        }
        
        /**
         * Execute in background
         */
        protected String doInBackground(String... args) {
              
              Log.i("CW", args[0]);
              CwApi api = new CwApi(mContext);
              return api.getJson(args[0]);

        }
        
        protected void onPreExecute() {
            listFragment.getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE); 
        }
        
        /**
         * After execute (in UI thread context)
         */
        protected void onPostExecute(String result)
        {
            listFragment.getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE); 
            loadAreas(result);
        }
    }
    
    /**
     * Load areas from JSON string result
     */
    public void loadAreas(String result) {
    
        try {
            Gson gson = new Gson();
            areas = gson.fromJson(result, Area[].class);
            AreaAdapter adapter = new AreaAdapter(mContext, R.id.list_item_text_view, areas);
            setListAdapter(adapter);
        } catch (JsonParseException e) {
            Toast.makeText(mContext, "An error occurred while retrieving area data", Toast.LENGTH_SHORT).show();
        }
        
      ListView lv = getListView();
      lv.setTextFilterEnabled(true);
      
      // Populate empty row in case we didn't find any areas
      //emptyView.setText(noneText);
      
      // Set on item click listener for states
      lv.setOnItemClickListener(new OnItemClickListener() {
          
          /**
           * On item click action, open area activity
           */
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              
              Area area = areas[position];
              Intent i = new Intent(getActivity(), AreaFragmentActivity.class);
              i.putExtra("areaId", Integer.valueOf(area.getId()).toString());
              i.putExtra("name", area.getName());
              startActivity(i);
          }
          
      });
      
      //dialog.hide();
      
    }
    
    public void setType(int typeId)
    {
        this.typeId = typeId;
    }
    
    public void setLocation(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public class AreaAdapter extends ArrayAdapter<Area>
    {
        public AreaAdapter(Context context, int textViewResourceId,
                Area[] objects) {
            super(context, textViewResourceId, objects);
            // TODO Auto-generated constructor stub
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            return ((Area) getItem(position)).getListRowView(convertView, parent, getContext());
        }
    }
    
}
