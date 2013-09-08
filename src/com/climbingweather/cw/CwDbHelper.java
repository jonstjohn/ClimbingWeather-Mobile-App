package com.climbingweather.cw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CwDbHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "cw";
    private static final int DATABASE_VERSION = 11; 
    
    private static final String TAG = CwDbHelper.class.getName();
    
    public interface Tables {
        public static final String FAVORITES = "favorite";
        public static final String STATES = "state";
        public static final String AREAS = "area";
        public static final String DAILY = "daily";
        public static final String HOURLY = "hourly";
        public static final String SEARCH = "search";
        public static final String SEARCH_AREA = "search_area";
    }
    
    CwDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    
        createFavoriteTable(db);
        createStateTable(db);
        createAreaTable(db);
        createDailyTable(db);
        createHourlyTable(db);
        createSearchTables(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
        // Backup favorites
        
        db.execSQL("DROP TABLE IF EXISTS favorite");
        db.execSQL("DROP TABLE IF EXISTS state");
        db.execSQL("DROP TABLE IF EXISTS area");
        db.execSQL("DROP TABLE IF EXISTS daily");
        db.execSQL("DROP TABLE IF EXISTS hourly");
        db.execSQL("DROP TABLE IF EXISTS search");
        db.execSQL("DROP TABLE IF EXISTS search_area");
        onCreate(db);

    }
    
    /**
     * Get columns for table
     * @param db
     * @param tableName
     * @return
     */
    public static List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> ar = null;
        Cursor c = null;
        try {
            c = db.rawQuery("select * from " + tableName + " limit 1", null);
            if (c != null) {
                ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return ar;
    }

    /**
     * Join list of strings into string
     * @param list
     * @param delim
     * @return
     */
    public static String join(List<String> list, String delim) {
        StringBuilder buf = new StringBuilder();
        int num = list.size();
        for (int i = 0; i < num; i++) {
            if (i != 0)
                buf.append(delim);
            buf.append((String) list.get(i));
        }
        return buf.toString();
    }
    
    /**
     * Create favorite table
     * @param db
     */
    private void createFavoriteTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE favorite (" +
                "_id integer primary key autoincrement," +
                "area_id integer unique," +
                "name text not null);");
    }
    /**
     * Create states table
     * @param db
     */
    private void createStateTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE state (" +
                "_id INTEGER PRIMARY KEY," +
                "name TEXT," +
                "state_code TEXT," +
                "areas INTEGER," +
                "updated INTEGER);");
        db.execSQL("CREATE UNIQUE INDEX state_stateCodeIndex ON state(state_code)");
        db.execSQL("CREATE INDEX state_updatedIndex ON state(updated)");
    }
    
    /**
     * Create area table
     * @param db
     */
    private void createAreaTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE area (" +
                "_id INTEGER PRIMARY KEY," +
                "name TEXT," +
                "latitude NUMERIC," +
                "longitude NUMERIC," +
                "state_code TEXT," +
                "elevation TEXT," +
                "daily_updated INTEGER," +
                "averages_updated INTEGER," +
                "detail_updated INTEGER," +
                "list_updated INTEGER," +
                "nearby INTEGER)");
        db.execSQL("CREATE INDEX area_listUpdatedIndex ON area(list_updated)");
        db.execSQL("CREATE INDEX area_detailUpdatedIndex ON area(detail_updated)");
        db.execSQL("CREATE INDEX area_nearbyIndex ON area(nearby)");
    }
    
    /**
     * Create daily table
     * @param db
     */
    private void createDailyTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE daily (" +
                "_id INTEGER PRIMARY KEY," +
                "area_id INTEGER REFERENCES area(_id) ON DELETE CASCADE," +
                "high INTEGER," +
                "low INTEGER," +
                "precip_day INTEGER," +
                "date TEXT," +
                "date_formatted TEXT," +
                "day TEXT," +
                "sky INTEGER," +
                "precip_night INTEGER," +
                "relative_humidity INTEGER," +
                "wsym TEXT," +
                "rain_amount NUMERIC," +
                "wind_sustained INTEGER," +
                "wind_gust INTEGER," +
                "weather TEXT," +
                "snow_amount NUMERIC," +
                "detail_updated INTEGER," +
                "updated INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX daily_AreaIdDateIndex ON daily(area_id, date)");
        db.execSQL("CREATE INDEX daily_updatedIndex ON daily(updated)");
        db.execSQL("CREATE INDEX daily_detailUpdatedIndex ON daily(detail_updated)");
    }
    
    /**
     * Create hourly table
     * @param db
     */
    private void createHourlyTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE hourly (" +
                "_id INTEGER PRIMARY KEY," +
                "area_id INTEGER REFERENCES area(_id) ON DELETE CASCADE," +
                "temp INTEGER," +
                "precip INTEGER," +
                "timestamp INTEGER," +
                "sky INTEGER," +
                "relative_humidity INTEGER," +
                "wsym TEXT," +
                "rain_amount NUMERIC," +
                "wind_sustained INTEGER," +
                "wind_gust INTEGER," +
                "weather TEXT," +
                "snow_amount NUMERIC," +
                "detail_updated INTEGER," +
                "updated INTEGER)");
        db.execSQL("CREATE INDEX hourly_areaIdIndex ON hourly(area_id)");
        db.execSQL("CREATE UNIQUE INDEX hourly_timestampIndex ON hourly(timestamp)");
        db.execSQL("CREATE INDEX hourly_updatedIndex ON hourly(updated)");
        db.execSQL("CREATE INDEX hourly_detailUpdatedIndex ON hourly(detail_updated)");
    }
    
    /**
     * Create search tables
     * @param db
     */
    private void createSearchTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE search (" +
                "_id integer primary key autoincrement," +
                "updated integer," +
                "search TEXT UNIQUE NOT NULL);");
        
        db.execSQL("CREATE INDEX search_searchIndex ON search(search)");
        db.execSQL("CREATE INDEX search_updatedIndex ON search(updated)");
        
        db.execSQL("CREATE TABLE search_area (" +
                "_id integer primary key autoincrement," +
                "search_id INTEGER NOT NULL REFERENCES search(_id) ON DELETE CASCADE," +
                "area_id INTEGER NOT NULL);");
        
        db.execSQL("CREATE UNIQUE INDEX searchAreas_searchAreaIndex ON search_area(search_id, area_id)");
    }
    
    public static void dumpFavorites(Context context) {
        testQuery(context, "SELECT _id, area_id, name FROM favorite");
    }
    
    public static void dumpStates(Context context) {
        testQuery(context, "SELECT _id, name, state_code, areas, updated FROM state");
    }
    
    /**
     * Test a query and output some information
     * @param sql
     */
    public static void testQuery(Context context, String sql)
    {
        Log.i(TAG, "Testing query: " + sql);
        CwDbHelper dbHelper = new CwDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        dumpCursor(cursor);
    }
    
    public static void dumpCursor(Cursor cursor) {
        int cnt = cursor.getColumnCount();
        
        Log.i(TAG, "Found " + Integer.toString(cursor.getCount()) + " rows");
        Log.i(TAG, "Rows");
        while (cursor.moveToNext()) {
            for (int i = 0; i < cnt; i++) {
                Log.i(TAG, cursor.getString(i));
            }
        }
    }
    
    public static void dumpCursorRow(Cursor cursor) {
        int cnt = cursor.getColumnCount();
        
        Log.i(TAG, "Dumping cursor row");
        for (int i = 0; i < cnt; i++) {
            Log.i(TAG, cursor.getColumnName(i) + ": " + cursor.getString(i));
        }
        Log.i(TAG, "Finished dumping cursor row");
    }

}
