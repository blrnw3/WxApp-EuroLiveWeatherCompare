package co.uk.wxApp.controllers;

import co.uk.wxApp.models.Data;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles connections to the local SQLite database
 *  @author Heavily influenced / mostly copied from Android book: Beginning Android Application Development - WEI-MENG LEE, 2012
 */
public class DBAdapter {
	private static final String TABLE_DATA = "live";
	private static final String TABLE_CITIES = "city";
	private static final int DATABASE_VERSION = 1;

	private static final String[] KEYS = { "City", "Temperature", "Rain", "Wind", "Humidity", "Pressure",
			"Condition", "Updated" };

	private final Context context;

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	/**
	 * Set up connection to the database
	 * @param ctx
	 */
	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, Data.DB_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	/**
	 * Open the database
	 * @return
	 * @throws SQLException
	 */
	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Close the database
	 */
	public void close() {
		DBHelper.close();
	}
	
	/**
	 * Obtain all data for a given table in the db
	 * @param isLiveData which table to use (live or cities)
	 * @return all the data for that table
	 */
	public Cursor getAllData(boolean isLiveData) {
		String table = isLiveData ? TABLE_DATA : TABLE_CITIES;
		return db.query(table, null, null, null, null, null, null);
	}

	/**
	 * Obtain lat/lng data for a particular city
	 * @param city the city name
	 * @return data
	 */
	public Cursor getCityInfo(String city) {
		return db.rawQuery("SELECT Latitude, Longitude FROM " + TABLE_CITIES + " WHERE City = ?",
				new String[] { city });
	}

	/**
	 * Updates the entire live table with new data
	 * @param data the 2D data array for the nine cities
	 */
	public void update(String[] data) {
		ContentValues args = new ContentValues();
		int cnt = 0;
		String key = "UniqueID = ";
		for( String s : data ) {
			args = new ContentValues();
			String[] line = s.split(",");

			for( int i = 0; i < KEYS.length; i++ ) {
				args.put(KEYS[i], line[i]);
			}

			db.update(TABLE_DATA, args, key + cnt, null);
			cnt++;
		}
	}
}