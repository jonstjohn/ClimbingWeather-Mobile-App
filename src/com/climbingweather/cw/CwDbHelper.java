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
    private static final int DATABASE_VERSION = 3; 
    
    public interface Tables {
        public static final String FAVORITES = "favorite";
        public static final String STATES = "state";
    }
    
    CwDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    
        createFavoriteTable(db);
        createStateTable(db);
        createAreaTable(db);
        createDailyTable(db);
        createHourlyTable(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
        // Backup favorites
        
        db.execSQL("DROP TABLE IF EXISTS favorite");
        db.execSQL("DROP TABLE IF EXISTS state");
        db.execSQL("DROP TABLE IF EXISTS area");
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
        db.execSQL("CREATE UNIQUE INDEX stateCodeIndex ON state(state_code)");
        db.execSQL("CREATE INDEX updatedIndex ON state(updated)");
    }
    
    /**
     * Create area table
     * @param db
     */
    private void createAreaTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE area (" +
                "_id INTEGER PRIMARY KEY," +
                "area_id INTEGER," +
                "name TEXT," +
                "latitude NUMERIC," +
                "longitude NUMERIC," +
                "state_code TEXT," +
                "elevation TEXT," +
                "detail_updated INTEGER," +
                "updated INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX areaIdIndex ON area(area_id)");
        db.execSQL("CREATE INDEX updatedIndex ON area(updated)");
        db.execSQL("CREATE INDEX detailUpdatedIndex ON area(detail_updated)");
    }
    
    /**
     * Create daily table
     * @param db
     */
    private void createDailyTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE daily (" +
                "_id INTEGER PRIMARY KEY," +
                "area_id INTEGER," +
                "high INTEGER," +
                "low INTEGER," +
                "precip_day INTEGER," +
                "date TEXT," +
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
        db.execSQL("CREATE INDEX areaIdIndex ON daily(area_id)");
        db.execSQL("CREATE INDEX dateIndex ON daily(date)");
        db.execSQL("CREATE INDEX updatedIndex ON daily(updated)");
        db.execSQL("CREATE INDEX detailUpdatedIndex ON daily(detail_updated)");
    }
    
    /**
     * Create hourly table
     * @param db
     */
    private void createHourlyTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE hourly (" +
                "_id INTEGER PRIMARY KEY," +
                "area_id INTEGER," +
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
        db.execSQL("CREATE INDEX areaIdIndex ON hourly(area_id)");
        db.execSQL("CREATE INDEX dateIndex ON hourly(date)");
        db.execSQL("CREATE INDEX updatedIndex ON hourly(updated)");
        db.execSQL("CREATE INDEX detailUpdatedIndex ON hourly(detail_updated)");
    }

}
