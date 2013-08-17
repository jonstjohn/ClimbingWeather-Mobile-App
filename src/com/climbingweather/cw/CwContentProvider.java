package com.climbingweather.cw;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * CW Content Provider
 */
public class CwContentProvider extends ContentProvider
{
    private CwDbHelper dbHelper;
    
    private static final int ALL_FAVORITES = 1;
    private static final int SINGLE_FAVORITE = 2;
    private static final int ALL_STATES = 3;
    private static final int SINGLE_STATE = 4;
    private static final int ALL_AREAS = 5;
    private static final int SINGLE_AREA = 6;
    private static final int AREA_DAILY = 7;
    private static final int AREA_HOURLY = 8;
    private static final int ALL_DAILY = 8;
    
    /**
     * URI matcher
     */
    public static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FavoritesContract.AUTHORITY, null, ALL_FAVORITES);
        uriMatcher.addURI(FavoritesContract.AUTHORITY, "#", SINGLE_FAVORITE);
        uriMatcher.addURI(StatesContract.AUTHORITY, null, ALL_STATES);
        uriMatcher.addURI(StatesContract.AUTHORITY, "#", SINGLE_STATE);
        uriMatcher.addURI(AreasContract.AUTHORITY, null, ALL_AREAS);
        uriMatcher.addURI(AreasContract.AUTHORITY, "#", SINGLE_AREA);
        uriMatcher.addURI(DailyContract.AUTHORITY, "#", AREA_DAILY);
        uriMatcher.addURI(HourlyContract.AUTHORITY, "#", AREA_HOURLY);
        uriMatcher.addURI(DailyContract.AUTHORITY, null, ALL_DAILY);
    }
    
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/favorites";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/favorites";
    
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
      
        switch (uriMatcher.match(uri)) {
        case ALL_FAVORITES: 
            return "vnd.android.cursor.dir/vnd.com.climbingweather.cw.provider.favorites";
        case SINGLE_FAVORITE: 
            return "vnd.android.cursor.item/vnd.com.climbingweather.cw.provider.favorites";
        case ALL_STATES:
            return "vnd.android.cursor.dir/vnd.com.climbingweather.cw.provider.states";
        case SINGLE_STATE:
            return "vnd.android.cursor.item/vnd.com.climbingweather.cw.provider.states";
        default: 
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
    
    /**
     * Insert new row
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri _uri = null;
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        switch (uriMatcher.match(uri)) {
        case ALL_FAVORITES:
            long favoriteId = db.insert(CwDbHelper.Tables.FAVORITES, null, values);
            _uri = Uri.parse(FavoritesContract.CONTENT_URI + "/" + favoriteId);
            getContext().getContentResolver().notifyChange(_uri, null);
            break;
        case SINGLE_FAVORITE:
            break;
        case ALL_STATES:
            long stateId = db.replace(CwDbHelper.Tables.STATES, null, values);
            _uri = Uri.parse(FavoritesContract.CONTENT_URI + "/" + stateId);
            getContext().getContentResolver().notifyChange(_uri, null);
            break;
        case SINGLE_STATE:
            break;
        case ALL_AREAS:
            long areaId = db.replace(CwDbHelper.Tables.AREAS, null, values);
            _uri = Uri.parse(AreasContract.CONTENT_URI + "/" + areaId);
            getContext().getContentResolver().notifyChange(_uri, null);
            break;
        case SINGLE_AREA:
            break;
        case AREA_DAILY:
            Logger.log("Saving daily inside content provider");
            Long dailyId = db.replace(CwDbHelper.Tables.DAILY, null, values);
            Logger.log("Daily id: " + Long.toString(dailyId));
            _uri = DailyContract.CONTENT_URI;
            getContext().getContentResolver().notifyChange(_uri, null); // TODO
            break;
        case AREA_HOURLY:
            db.replace(CwDbHelper.Tables.HOURLY, null, values);
            _uri = HourlyContract.CONTENT_URI;
            getContext().getContentResolver().notifyChange(_uri, null); // TODO
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
        case ALL_FAVORITES:
            queryBuilder.setTables(CwDbHelper.Tables.FAVORITES);
            break;
        case SINGLE_FAVORITE:
            queryBuilder.setTables(CwDbHelper.Tables.FAVORITES);
            String favoriteId = uri.getPathSegments().get(1);
            queryBuilder.appendWhere(FavoritesContract.Columns.ID + "=" + favoriteId);
            break;
        case ALL_STATES:
            queryBuilder.setTables(CwDbHelper.Tables.STATES);
            break;
        case SINGLE_STATE:
            queryBuilder.setTables(CwDbHelper.Tables.STATES);
            String id = uri.getPathSegments().get(1);
            queryBuilder.appendWhere(StatesContract.Columns.ID + "=" + id);
            break;
        case ALL_AREAS:
            //queryBuilder.setTables(CwDbHelper.Tables.AREAS);
            queryBuilder.setTables("area"
                    + " LEFT JOIN (SELECT area_id, date, high, wsym FROM daily WHERE date = date('now')) AS d1 ON area._id = d1.area_id"
                    + " LEFT JOIN (SELECT area_id, date, high, wsym FROM daily WHERE date = date('now', '+1 day')) AS d2 ON area._id = d2.area_id"
                    + " LEFT JOIN (SELECT area_id, date, high, wsym FROM daily WHERE date = date('now', '+2 day')) AS d3 ON area._id = d3.area_id");
            
            //Cursor cursor = db.rawQuery("SELECT area_id, date, high, low FROM daily WHERE date = date('now')", selectionArgs);
            //Logger.log("Daily count: " + Integer.toString(cursor.getCount()));
            //return db.rawQuery("select area._id, area.name," 
//                    + "d1.date as d1_date, d1.high as d1_high, d1.low as d1_low,"
//                    + "d2.date as d2_date, d2.high as d2_high, d2.low as d2_low,"
//                    + "d3.date as d3_date, d3.high as d3_high, d3.low as d3_low"
//                    + " FROM area"
//                    + " LEFT JOIN (SELECT area_id, date, high, low FROM daily WHERE date = date('now')) AS d1 ON area._id = d1.area_id"
//                    + " LEFT JOIN (SELECT area_id, date, high, low FROM daily WHERE date = date('now', '+ 1 day')) AS d2 ON area._id = d2.area_id"
//                    + " LEFT JOIN (SELECT area_id, date, high, low FROM daily WHERE date = date('now', '+ 2 day')) AS d3 ON area._id = d3.area_id"
//                    + " ORDER BY area.name ASC", selectionArgs);
            break;
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
        case ALL_FAVORITES:
            //do nothing 
            break;
        case SINGLE_FAVORITE:
            List<String> segs = uri.getPathSegments();
            for (String s: segs) {
                Logger.log(s);
            }
            String areaId = uri.getPathSegments().get(0);
            selection = FavoritesContract.Columns.AREA_ID + "=" + areaId
                    + (!TextUtils.isEmpty(selection) ? 
                            " AND (" + selection + ')' : "");
            deleteCount = db.delete(CwDbHelper.Tables.FAVORITES, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
        case ALL_STATES:
            //do nothing 
            break;
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
        case ALL_FAVORITES:
            //do nothing 
            break;
        case SINGLE_FAVORITE:
            String favoriteId = uri.getPathSegments().get(1);
            selection = FavoritesContract.Columns.ID + "=" + favoriteId
                + (!TextUtils.isEmpty(selection) ? 
                " AND (" + selection + ')' : "");
            updateCount = db.update(CwDbHelper.Tables.FAVORITES, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            break;
        case ALL_STATES:
            // do nothing
            break;
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
