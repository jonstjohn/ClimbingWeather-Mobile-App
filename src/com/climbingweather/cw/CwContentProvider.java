package com.climbingweather.cw;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Climbing Weather Content Provider
 */
public class CwContentProvider extends ContentProvider
{
    /**
     * CW database helper
     */
    private CwDbHelper dbHelper;
    
    /**
     * Authority
     */
    public static final String AUTHORITY = "com.climbingweather.cw.provider";
    
    /**
     * Constants to define request types
     */
    private static final int ALL_FAVORITES = 1;
    private static final int SINGLE_FAVORITE = 2;
    private static final int ALL_STATES = 3;
    private static final int SINGLE_STATE = 4;
    private static final int ALL_AREAS = 5;
    private static final int SINGLE_AREA = 6;
    private static final int AREA_DAILY = 7;
    private static final int AREA_HOURLY = 8;
    private static final int ALL_DAILY = 9;
    private static final int ALL_SEARCH = 10;
    private static final int ALL_SEARCH_AREA = 11;
    
    /**
     * URI matcher
     */
    public static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "/favorites", ALL_FAVORITES);
        uriMatcher.addURI(AUTHORITY, "/favorites/#", SINGLE_FAVORITE);
        uriMatcher.addURI(AUTHORITY, "/states", ALL_STATES);
        uriMatcher.addURI(AUTHORITY, "/states/#", SINGLE_STATE);
        uriMatcher.addURI(AUTHORITY, "/areas", ALL_AREAS);
        uriMatcher.addURI(AUTHORITY, "/areas/#", SINGLE_AREA);
        uriMatcher.addURI(AUTHORITY, "/daily/#", AREA_DAILY);
        uriMatcher.addURI(AUTHORITY, "/hourly/#", AREA_HOURLY);
        uriMatcher.addURI(AUTHORITY, "/daily", ALL_DAILY);
        uriMatcher.addURI(AUTHORITY, "/areas/search", ALL_SEARCH);
        uriMatcher.addURI(AUTHORITY, "/areas/searchArea", ALL_SEARCH_AREA);
    }
    
    /**
     * Tag for logging
     */
    private static final String TAG = CwContentProvider.class.getName();
    
    /**
     * On create, called at provider startup
     */
    @Override
    public boolean onCreate() {
        dbHelper = new CwDbHelper(getContext());
        return false;
    }
    
    /**
     * Return the MIME type corresponding to a content URI
     */
    @Override
    public String getType(Uri uri) {
        return null;
    }
    
    /**
     * Insert new row
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri _uri = null;
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        switch (uriMatcher.match(uri)) {
        // Insert favorite
        case ALL_FAVORITES:
            long favoriteId = db.insert(CwDbHelper.Tables.FAVORITES, null, values);
            _uri = Uri.parse(FavoritesContract.CONTENT_URI + "/" + favoriteId);
            Log.i(TAG, _uri.toString());
            getContext().getContentResolver().notifyChange(_uri, null);
            break;
        // Insert on single favorite does nothing
        case SINGLE_FAVORITE:
            break;
        // Insert state
        case ALL_STATES:
            long stateId = db.replace(CwDbHelper.Tables.STATES, null, values);
            _uri = Uri.parse(FavoritesContract.CONTENT_URI + "/" + stateId);
            getContext().getContentResolver().notifyChange(_uri, null);
            break;
        // Insert on single state does nothing
        case SINGLE_STATE:
            break;
        // Insert area
        case ALL_AREAS:
            long areaId = db.replace(CwDbHelper.Tables.AREAS, null, values);
            _uri = Uri.parse(AreasContract.CONTENT_URI + "/" + areaId);
            getContext().getContentResolver().notifyChange(AreasContract.CONTENT_URI, null);
            Log.i(TAG, "notifyChange()");
            break;
        // Insert on single area does nothing
        case SINGLE_AREA:
            break;
        // Insert daily forecast row
        case AREA_DAILY:
            db.replace(CwDbHelper.Tables.DAILY, null, values);
            _uri = DailyContract.CONTENT_URI;
            getContext().getContentResolver().notifyChange(_uri, null); // TODO
            break;
        // Insert hourly forecast row
        case AREA_HOURLY:
            db.replace(CwDbHelper.Tables.HOURLY, null, values);
            _uri = HourlyContract.CONTENT_URI;
            getContext().getContentResolver().notifyChange(_uri, null); // TODO
            break;
        // Insert search
        case ALL_SEARCH:
            long searchId = db.replace(CwDbHelper.Tables.SEARCH, null, values);
            _uri = Uri.parse(AreasContract.SEARCH_URI + "/" + searchId);
            getContext().getContentResolver().notifyChange(uri, null); // TOOD
            break;
        // Insert search area
        case ALL_SEARCH_AREA:
            long searchAreaId = db.replace(CwDbHelper.Tables.SEARCH_AREA, null, values);
            _uri = Uri.parse(AreasContract.SEARCH_AREA_URI + "/" + searchAreaId);
            getContext().getContentResolver().notifyChange(uri, null); // TOOD
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        
        return _uri;
    }
    
    /**
     * Query
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        
        Log.i("CW", "QUERY");
        switch (uriMatcher.match(uri)) {
        
        // Query all favorites
        case ALL_FAVORITES:
            queryBuilder.setTables(CwDbHelper.Tables.FAVORITES);
            break;
            
        // Query single favorite (uses favorite id)
        case SINGLE_FAVORITE:
            queryBuilder.setTables(CwDbHelper.Tables.FAVORITES);
            String favoriteId = uri.getPathSegments().get(1);
            queryBuilder.appendWhere(FavoritesContract.Columns.ID + "=" + favoriteId);
            break;
            
        // Query all states
        case ALL_STATES:
            queryBuilder.setTables(CwDbHelper.Tables.STATES);
            break;
            
        // Query single state (uses state id)
        case SINGLE_STATE:
            queryBuilder.setTables(CwDbHelper.Tables.STATES);
            String id = uri.getPathSegments().get(1);
            queryBuilder.appendWhere(StatesContract.Columns.ID + "=" + id);
            break;
            
        // Query all areas - does a left join on daily forecasts so row contains 3 days of forecasts
        case ALL_AREAS:
            queryBuilder.setTables("area"
                    + " LEFT JOIN (SELECT area_id, date, high, wsym FROM daily WHERE date = date('now', 'localtime')) AS d1 ON area._id = d1.area_id"
                    + " LEFT JOIN (SELECT area_id, date, high, wsym FROM daily WHERE date = date('now', '+1 day', 'localtime')) AS d2 ON area._id = d2.area_id"
                    + " LEFT JOIN (SELECT area_id, date, high, wsym FROM daily WHERE date = date('now', '+2 day', 'localtime')) AS d3 ON area._id = d3.area_id"
                    + " LEFT JOIN favorite ON area._id = favorite.area_id"
                    + " LEFT JOIN search_area ON area._id = search_area.area_id");
            if (projection == null) {
                projection = AreasContract.PROJECTION_DEFAULT;
            }
            break;
            
        // Query all daily - probably not really used
        case ALL_DAILY:
            queryBuilder.setTables(CwDbHelper.Tables.DAILY);
            break;
            
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    /**
     * Delete
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        int deleteCount = 0;
        
        switch (uriMatcher.match(uri)) {
        
        // Delete all favorites - not implemented
        case ALL_FAVORITES:
            break;
            
        // Delete a single favorite
        case SINGLE_FAVORITE:
            String areaId = uri.getPathSegments().get(1);
            selection = FavoritesContract.Columns.AREA_ID + "=" + areaId
                    + (!TextUtils.isEmpty(selection) ? 
                            " AND (" + selection + ')' : "");
            deleteCount = db.delete(CwDbHelper.Tables.FAVORITES, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
            
        // Delete all states - not implemented
        case ALL_STATES:
            break;
            
        // Delete a single state
        case SINGLE_STATE:
            String stateId = uri.getPathSegments().get(1);
            selection = StatesContract.Columns.ID + "=" + stateId
                    + (!TextUtils.isEmpty(selection) ? 
                            " AND (" + selection + ')' : "");
            deleteCount = db.delete(CwDbHelper.Tables.STATES, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        return deleteCount;
    }
    
    /**
     * Update
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        int updateCount = 0;
        
        switch (uriMatcher.match(uri)) {
        
        // Update all favorites - not implemented
        case ALL_FAVORITES:
            break;
            
        // Update single favorite
        case SINGLE_FAVORITE:
            String favoriteId = uri.getPathSegments().get(1);
            selection = FavoritesContract.Columns.ID + "=" + favoriteId
                + (!TextUtils.isEmpty(selection) ? 
                " AND (" + selection + ')' : "");
            updateCount = db.update(CwDbHelper.Tables.FAVORITES, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
            
        // Update all states - not implemented
        case ALL_STATES:
            break;
            
        // Update single state
        case SINGLE_STATE:
            String stateId = uri.getPathSegments().get(1);
            selection = StatesContract.Columns.ID + "=" + stateId
                + (!TextUtils.isEmpty(selection) ? 
                " AND (" + selection + ')' : "");
            updateCount = db.update(CwDbHelper.Tables.STATES, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
     
        return updateCount;
    }
}
