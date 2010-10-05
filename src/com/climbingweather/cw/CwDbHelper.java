package com.climbingweather.cw;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CwDbHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "cw";
	private static final int DATABASE_VERSION = 1; 
	
	private static final String TAG = "CW";

    CwDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
	
		String sql = "CREATE TABLE favorite (_id integer primary key autoincrement,"
			+ "area_id integer unique, name text not null)";
		db.execSQL(sql);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS favorite");
        onCreate(db);

	}

}
