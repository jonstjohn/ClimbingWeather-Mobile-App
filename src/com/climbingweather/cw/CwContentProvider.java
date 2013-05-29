package com.climbingweather.cw;

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

// http://www.mysamplecode.com/2012/11/android-database-content-provider.html

public class CwContentProvider extends ContentProvider
{
    private CwDbHelper dbHelper;
    
    private static final int ALL_FAVORITES = 1;
    private static final int SINGLE_FAVORITE = 2;
    
    private static final String AUTHORITY = "com.climbingweather.cw";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites");
    
    public static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "favorites", ALL_FAVORITES);
        uriMatcher.addURI(AUTHORITY, "favorites/#", SINGLE_FAVORITE);
    }
    
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/favorites";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/favorites";
    
    // system calls onCreate() when it starts up the provider.
    @Override
    public boolean onCreate() {
     // get access to the database helper
     dbHelper = new CwDbHelper(getContext());
     return false;
    }
    
    //Return the MIME type corresponding to a content URI
    @Override
    public String getType(Uri uri) {
      
     switch (uriMatcher.match(uri)) {
     case ALL_FAVORITES: 
      return "vnd.android.cursor.dir/vnd.com.climbingweather.cw.favorites";
     case SINGLE_FAVORITE: 
      return "vnd.android.cursor.item/vnd.com.climbingweather.cw.favorites";
     default: 
      throw new IllegalArgumentException("Unsupported URI: " + uri);
     }
    }
    
    // The insert() method adds a new row to the appropriate table, using the values 
    // in the ContentValues argument. If a column name is not in the ContentValues argument, 
    // you may want to provide a default value for it either in your provider code or in 
    // your database schema. 
    @Override
    public Uri insert(Uri uri, ContentValues values) {
      
     SQLiteDatabase db = dbHelper.getWritableDatabase();
     switch (uriMatcher.match(uri)) {
     case ALL_FAVORITES:
      //do nothing
      break;
     default:
      throw new IllegalArgumentException("Unsupported URI: " + uri);
     }
     long id = db.insert(FavoriteDbAdapter.DATABASE_TABLE, null, values);
     getContext().getContentResolver().notifyChange(uri, null);
     return Uri.parse(CONTENT_URI + "/" + id);
    }
    
 // The query() method must return a Cursor object, or if it fails, 
    // throw an Exception. If you are using an SQLite database as your data storage, 
    // you can simply return the Cursor returned by one of the query() methods of the 
    // SQLiteDatabase class. If the query does not match any rows, you should return a 
    // Cursor instance whose getCount() method returns 0. You should return null only 
    // if an internal error occurred during the query process. 
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
     SQLiteDatabase db = dbHelper.getWritableDatabase();
     SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
     queryBuilder.setTables(FavoriteDbAdapter.DATABASE_TABLE);
     Log.i("CW", "QUERY");
     switch (uriMatcher.match(uri)) {
     case ALL_FAVORITES:
      //do nothing 
      break;
     case SINGLE_FAVORITE:
      String id = uri.getPathSegments().get(1);
      queryBuilder.appendWhere(FavoriteDbAdapter.KEY_ROWID + "=" + id);
      break;
     default:
      throw new IllegalArgumentException("Unsupported URI: " + uri);
     }
    
     Cursor cursor = queryBuilder.query(db, projection, selection,
       selectionArgs, null, null, sortOrder);
     return cursor;
    
    }

    // The delete() method deletes rows based on the seletion or if an id is 
    // provided then it deleted a single row. The methods returns the numbers
    // of records delete from the database. If you choose not to delete the data
    // physically then just update a flag here.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    
     SQLiteDatabase db = dbHelper.getWritableDatabase();
     switch (uriMatcher.match(uri)) {
     case ALL_FAVORITES:
      //do nothing 
      break;
     case SINGLE_FAVORITE:
      String id = uri.getPathSegments().get(1);
      selection = FavoriteDbAdapter.KEY_ROWID + "=" + id
      + (!TextUtils.isEmpty(selection) ? 
        " AND (" + selection + ')' : "");
      break;
     default:
      throw new IllegalArgumentException("Unsupported URI: " + uri);
     }
     int deleteCount = db.delete(FavoriteDbAdapter.DATABASE_TABLE, selection, selectionArgs);
     getContext().getContentResolver().notifyChange(uri, null);
     return deleteCount;
    }
    
    // The update method() is same as delete() which updates multiple rows
    // based on the selection or a single row if the row id is provided. The
    // update method returns the number of updated rows.
    @Override
    public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
     SQLiteDatabase db = dbHelper.getWritableDatabase();
     switch (uriMatcher.match(uri)) {
     case ALL_FAVORITES:
      //do nothing 
      break;
     case SINGLE_FAVORITE:
      String id = uri.getPathSegments().get(1);
      selection = FavoriteDbAdapter.KEY_ROWID + "=" + id
      + (!TextUtils.isEmpty(selection) ? 
        " AND (" + selection + ')' : "");
      break;
     default:
      throw new IllegalArgumentException("Unsupported URI: " + uri);
     }
     int updateCount = db.update(FavoriteDbAdapter.DATABASE_TABLE, values, selection, selectionArgs);
     getContext().getContentResolver().notifyChange(uri, null);
     return updateCount;
    }
}
