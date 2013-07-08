package com.climbingweather.cw;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

public class FavoriteListFragment extends SherlockListFragment
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    // Adapter used to display the list's data
    private SimpleCursorAdapter cursorAdapter;
    
    // Header view
    private TextView headerView;
    
    /**
     * On create
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i("CW", "Favorite onCreateView()");
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.list, null);
        
    }
    
    public void onActivityCreated (Bundle savedInstanceState)
    {
        Log.i("CW", "Favorite onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        
        //if (headerView == null) {
            //headerView = (TextView) inflater.inflate(R.layout.header_row, null);
            //headerView.setText("Favorite Areas");
            //getListView().addHeaderView(headerView);
        //}
        
        fillData();
    }
    
    /**
     * On destroy activity
     */
    public void onDestroy()
    {
        Log.i("CW", "Favorite onDestroy()");
        super.onDestroy();
    }
    
    /**
     * On start activity
     */
    public void onStart()
    {
        Log.i("CW", "Favorite onStart()");
        super.onStart();
    }
    
    /**
     * On stop activity
     */
    public void onStop()
    {
        Log.i("CW", "Favorite onStop()");
        super.onStop();
    }
    
    /**
     * On pause activity
     */
    public void onPause()
    {
        Log.i("CW", "Favorite onPause()");
        super.onPause();
    }
    
    /**
     * On resume activity
     */
    public void onResume()
    {
        Log.i("CW", "Favorite onResume()");
        getLoaderManager().getLoader(0).forceLoad();
        super.onResume();
    }
    
    
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Log.i("CW", "Favorite onDestroyView()");
        setListAdapter(null);
    }
    
    private void fillData()
    {
        getLoaderManager().initLoader(0, null, this);
        cursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_row,
                null,
                new String[] { FavoriteDbAdapter.KEY_NAME },
                new int[] { R.id.name },
                0
            );
        setListAdapter(cursorAdapter);
      }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        
        /* Removed b/c no longer using header row
        if (position == 0) {
            
            return;
            
        }
        */
        
        Log.i("CW", Integer.toString(position));
        Log.i("CW", Long.toString(id));
        
        Intent i = new Intent(getActivity(), AreaFragmentActivity.class);
        Uri favoriteUri = Uri.parse(CwContentProvider.CONTENT_URI + "/" + id);
        
        Cursor cursor = getActivity().getContentResolver().query(favoriteUri, null, null, null, null);
        cursor.moveToFirst();
        
        String name = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteDbAdapter.KEY_NAME));
        String areaId = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteDbAdapter.KEY_AREAID));
            
        i.putExtra("areaId", areaId);
        i.putExtra("name", name);
        
        cursor.close();
        
        startActivity(i);
        
    }
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        String[] dataColumns = { FavoriteDbAdapter.KEY_NAME, FavoriteDbAdapter.KEY_AREAID };
        return new CursorLoader(
            getActivity(),
            CwContentProvider.CONTENT_URI,
            null,
            null,
            null,
            FavoriteDbAdapter.KEY_NAME
        );
        
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        Log.i("CW", "load finished");
        cursorAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        cursorAdapter.swapCursor(null);
    }
    
}
