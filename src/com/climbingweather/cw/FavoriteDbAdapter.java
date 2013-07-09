package com.climbingweather.cw;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FavoriteDbAdapter {
    
    private CwDbHelper cwDbHelper;
    private SQLiteDatabase mDb;
    
    private final Context ctx;
    
    public static final String KEY_AREAID = "area_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ROWID = "_id";
    public static final String DATABASE_TABLE = "favorite";
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public FavoriteDbAdapter(Context ctx) {
        this.ctx = ctx;
    }
    
    /**
     * Open the cw database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public FavoriteDbAdapter open() throws SQLException {
        
        if (mDb == null || !mDb.isOpen()) {
            cwDbHelper = new CwDbHelper(ctx);
            mDb = cwDbHelper.getWritableDatabase();
        }
        return this;
    }
    
    public void close()
    {
        mDb.close();
    }
    
    /**
     * Create a new favorite. If the favorite is
     * successfully created return the new id for that favorite, otherwise return
     * a -1 to indicate failure.
     * 
     * @param areaId ID of area
     * @param name Name of area
     * @return rowId or -1 if failed
     */
    public long addFavorite(Integer areaId, String name) {
        
        // Try to delete first
        open();
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_AREAID, areaId);
        initialValues.put(KEY_NAME, name);

        long result = mDb.insert(DATABASE_TABLE, null, initialValues);
        close();
        return result;
    }
    
    public long removeFavorite(Integer areaId)
    {
        open();
        mDb.execSQL("DELETE FROM favorite WHERE area_id = " + Integer.toString(areaId));
        close();
        return 1; // mDb.delete(DATABASE_TABLE, KEY_ROWID + " = " + Integer.toString(areaId), null);
    }
    
    /**
     * Return a Cursor over the list of all faves in the database
     * 
     * @return Cursor over all faves
     */
    public Cursor fetchAllFavorites()
    {
        Cursor cursor = mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_AREAID,
                KEY_NAME}, null, null, null, null, null);
        return cursor;
    }
    
    public ArrayList<String> fetchAllFavoriteAreaIds()
    {
        ArrayList<String> ids = new ArrayList<String>();
        Cursor cur = fetchAllFavorites();
        cur.moveToFirst();
        while (cur.isAfterLast() == false) 
        {
            ids.add(cur.getString(1));
            cur.moveToNext();
        }
        return ids;
    }
    
    /**
     * Return a Cursor positioned at the favorite that matches the given rowId
     * 
     * @param rowId id of favorite to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchFavorite(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_AREAID, KEY_NAME}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    /**
     * Check to see if rowId is favorite
     * @param rowId
     * @return
     */
    public boolean isFavorite(Integer areaId)
    {
        open();
        Cursor mCursor = fetchFavoriteByAreaId(areaId);
        Boolean bool = mCursor.getCount() == 1;
        close();
        return bool;
    }
    
    /**
     * Fetch favorite area by area id
     * @param areaId
     * @return boolean
     */
    public Cursor fetchFavoriteByAreaId(Integer areaId)
    {
        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_AREAID, KEY_NAME}, KEY_AREAID + "=" + Integer.toString(areaId), null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

}
