package com.wireless.locationtracking;

import java.util.Vector;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBManager extends SQLiteOpenHelper {
	
	private static DBManager instance = null;

	private static final String DB_NAME = "pos_tracking_log.db";
	private static final int DB_VERSION = 1;
	
	
	protected DBManager(Context context){
		super(context, DB_NAME, null, DB_VERSION);
		
	}
	
	public static DBManager getInstance(Context context){
		if(context == null && instance == null){
			System.err.println("CAN'T MAKE DB WITH NULL CONTEXT");
		}
		if (instance == null)
			instance = new DBManager(context);
		
		return instance;
	}
	
	@Override 
	public void onCreate(SQLiteDatabase database){
		createDatabaseTables(database);
	}
		
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

		flushTables(db);
	}	
	
	public void insertEntries(long timestamp, double acc[], double vel[], double pos[])
	{
		SQLiteDatabase db = getWritableDatabase();

		for (int i = 0; i<3; ++i) {
			ContentValues values = new ContentValues();
			
			values.put("timestamp", timestamp);
			values.put("direction", i);
			values.put("value", acc[i]);
			
			db.insert("accInfo", null, values);	
		}

		for (int i = 0; i<3; ++i) {
			ContentValues values = new ContentValues();
			
			values.put("timestamp", timestamp);
			values.put("direction", i);
			values.put("value", vel[i]);
			
			db.insert("velInfo", null, values);	
		}

		for (int i = 0; i<3; ++i) {
			ContentValues values = new ContentValues();
			
			values.put("timestamp", timestamp);
			values.put("direction", i);
			values.put("value", pos[i]);
			
			db.insert("posInfo", null, values);	
		}
	}
	
	
	public void flushTables(SQLiteDatabase db)
	{
		db.execSQL("DROP TABLE IF EXISTS accInfo;");
		db.execSQL("DROP TABLE IF EXISTS velInfo;");
		db.execSQL("DROP TABLE IF EXISTS posInfo;");
		
		createDatabaseTables(db);
	}
	
		
	/**
	 * 
	 * 
	 * @author Nick
	 * @param db - The database object to use when performing the queries 
	 */
	private void createDatabaseTables(SQLiteDatabase db)
	{
		String CREATE_TABLE = "" +
				"CREATE TABLE accInfo " +
				"             ( " +
				"                      id           INTEGER PRIMARY KEY autoincrement, " +
				"                      timestamp    INTEGER NOT NULL                 , " +
				"                      direction    INTEGER NOT NULL                 , " +
				"                      value        REAL NOT NULL " +
				"             )";
		db.execSQL(CREATE_TABLE);
		
		CREATE_TABLE = "" +
				"CREATE TABLE velInfo " +
				"             ( " +
				"                      id           INTEGER PRIMARY KEY autoincrement, " +
				"                      timestamp    INTEGER NOT NULL                 , " +
				"                      direction    INTEGER NOT NULL                 , " +
				"                      value        REAL NOT NULL " +
				"             )";
		db.execSQL(CREATE_TABLE);

		CREATE_TABLE = "" +
				"CREATE TABLE posInfo " +
				"             ( " +
				"                      id           INTEGER PRIMARY KEY autoincrement, " +
				"                      timestamp    INTEGER NOT NULL                 , " +
				"                      direction    INTEGER NOT NULL                 , " +
				"                      value        REAL NOT NULL " +
				"             )";
		db.execSQL(CREATE_TABLE);
		
	}
}