package com.climbingweather.cw;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.list, null);
        
    }
    
    public void onActivityCreated (Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        
        if (headerView == null) {
            headerView = (TextView) inflater.inflate(R.layout.header_row, null);
            headerView.setText("Favorite Areas");
            getListView().addHeaderView(headerView);
        }
        
        cursorAdapter = new SimpleCursorAdapter(
            getActivity(),
            R.layout.list_row,
            null,
            new String[] { FavoriteDbAdapter.KEY_NAME },
            new int[] { R.id.name },
            0
        );
        setListAdapter(cursorAdapter);
        
        getLoaderManager().initLoader(0, null, this);
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
    }
    
    /**
     * On stop activity
     */
    public void onStop()
    {
        super.onStop();
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
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        if (position == 0) {
            
            return;
            
        }
        
        FavoriteDbAdapter dbAdapter = new FavoriteDbAdapter(getActivity());
        dbAdapter.open();
        Cursor fav = dbAdapter.fetchFavorite(id);
        getActivity().startManagingCursor(fav);
        // launchForecast(fav.getString(fav.getColumnIndex(FavoriteDbAdapter.KEY_AREAID)));
        
        Intent i = new Intent(getActivity(), AreaActivity.class);
        i.putExtra("areaId", fav.getString(fav.getColumnIndex(FavoriteDbAdapter.KEY_AREAID)));
        i.putExtra("name", fav.getString(fav.getColumnIndex(FavoriteDbAdapter.KEY_NAME)));
        fav.close();
        dbAdapter.close();
        startActivity(i);
        
    }
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        
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
        cursorAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        cursorAdapter.swapCursor(null);
    }
    
}
