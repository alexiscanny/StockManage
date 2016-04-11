/*
	Copyright 2013 Alessio Scannicchio (alessio.scannicchio@gmail.com).

 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at

  	http://www.apache.org/licenses/LICENSE-2.0

 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
*/

package com.example.tsa;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class StockDataDB {
	// This variable is used for debug log (LogCat)
	private static final String TAG = "TSA:StockDataDB";

	// General TimeZone for updates purposes.
	public static final String TIME_ZONE = "US/NYC";
	// Market opening time.
	public static final long MARKET_OPEN_TIME = 9;
	// Market closing time.
	public static final long MARKET_CLOSE_TIME = 18;
	
	// Database setting variables
	private static final String DATABASE_NAME = "db_sd_stockpricelist.db";
	private static final int DATABASE_VERSION = 1;
	
	/**
	 * 	Table: sd_latestprice
	 * 	Columns:
	 * 		sd_symbol	TEXT		// stock symbol
	 * 		sd_name		TEXT		// stock name
	 * 		sd_price	FLOAT		// latest price
	 *    sd_change	FLOAT		// latest change 	
	 *    sd_maximum FLOAT  // maximum of the day
	 *    sd_minimum FLOAT  // minimum of the day
	 */
	private static final String TABLE_SD_LASTPRICE = "sd_latestprice";	
	private static final String COL_SD_SYMBOL = "sd_symbol";
	private static final String COL_SD_NAME = "sd_name";
	private static final String COL_SD_PRICE = "sd_price";
	private static final String COL_SD_CHANGE = "sd_change";
	private static final String COL_SD_MAXIMUM = "sd_maximum";
	private static final String COL_SD_MINIMUM = "sd_minimum";
	
	public static final int COL_SD_SYMBOL_IDX = 0;
	public static final int COL_SD_NAME_IDX = 1;
	public static final int COL_SD_PRICE_IDX = 2;
	public static final int COL_SD_CHANGE_IDX = 3;
	public static final int COL_SD_MAXIMUM_IDX = 4;
	public static final int COL_SD_MINIMUM_IDX = 5;
	
	// DatabaseHelper class.	
	private class SDDB_Helper extends SQLiteOpenHelper {
		
		public SDDB_Helper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				// if database does not exist, create new tables.
				String strSql = "CREATE TABLE " + TABLE_SD_LASTPRICE + " ( "
				          + COL_SD_SYMBOL + " TEXT" + ", "
				          + COL_SD_NAME + " TEXT" +  ", "
				          + COL_SD_PRICE + " FLOAT" +  ", "
				          + COL_SD_CHANGE + " FLOAT" +  ", "
				          + COL_SD_MAXIMUM + " FLOAT" +  ", "
				          + COL_SD_MINIMUM + " FLOAT"
				          + " );";
				Log.d(TAG, "Creating table: SQL=" + strSql);
				db.execSQL(strSql);
			} catch (SQLException sqlException) {
				Log.e(TAG, "onCreate:" + sqlException.toString());				
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Currently, nothing to do for database upgrade
		}
	}
	
	// Database variables
	private SDDB_Helper	helper;
	private SQLiteDatabase	coreDB = null;
	
	public StockDataDB(Context context) {
		helper = new SDDB_Helper(context);		
		if(coreDB == null) {
			Log.d(TAG, "Opening database...");
			coreDB = helper.getWritableDatabase();
		}
	}	
	
	@Override
	public void finalize() {
		closeDB();
	}
	
	public void closeDB() {
		if(coreDB != null) {
			if(coreDB.isOpen()) {
				Log.d(TAG, "Close database...");
				coreDB.close();
				coreDB = null;
			}
		}
	}
	
	public void insertStockData(StockData data) {
		Log.d(TAG, "Insert Symbol=" + data.getSymbol()
				+ " name=" + data.getName()
				+ " price=" + data.getPrice()
				+ " change=" + data.getPercentileChange() + "%"
				+ " maximum=" + data.getMaximum()
				+ " minimum=" + data.getMinimum());	
		try {
			// Insert new stock data.
			String strSql = "INSERT INTO " + TABLE_SD_LASTPRICE + 
	          " ( " + COL_SD_SYMBOL + ", " 
	                + COL_SD_NAME + ", " 
	                + COL_SD_PRICE + ", " 
	                + COL_SD_CHANGE + ", "
	                + COL_SD_MAXIMUM + ", "
	                + COL_SD_MINIMUM
	          + " ) VALUES ('"
	                + data.getSymbol() + "', '"
	                + data.getName() + "', "
	                + String.valueOf(data.getPrice()) + ", "
	                + String.valueOf(data.getPercentileChange()) + ", "
	                + String.valueOf(data.getMaximum()) + ", "
	                + String.valueOf(data.getMinimum()) + ");";			
			Log.d(TAG, "InsertStockData: SQL=" + strSql);
			coreDB.execSQL(strSql);			
		} catch (SQLException sqlException) {
			Log.e(TAG, "InsertStockData:" + sqlException.toString());
		}
	}
	
	public void updateStockData(StockData data) {
		Log.d(TAG, "Update Symbol=" + data.getSymbol()
				+ " name=" + data.getName()
				+ " price=" + data.getPrice()
				+ " change=" + data.getPercentileChange() + "%"
				+ " maximum=" + data.getMaximum()
				+ " minimum=" + data.getMinimum());	
		try {
			// Update stock data.
			String strSql = "UPDATE " + TABLE_SD_LASTPRICE + " SET "
			          + COL_SD_NAME + "='" + data.getName() + "', "
			          + COL_SD_PRICE + "=" + String.valueOf(data.getPrice()) + ", "
			          + COL_SD_CHANGE + "=" + String.valueOf(data.getPercentileChange()) + ", "
			          + COL_SD_MAXIMUM + "=" + String.valueOf(data.getMaximum()) + ", "
			          + COL_SD_MINIMUM + "=" + String.valueOf(data.getMinimum()) + " "
			          + " WHERE " + COL_SD_SYMBOL + "='" + data.getSymbol() + "';";
			Log.d(TAG, "UpdateStockData: SQL=" + strSql);
			coreDB.execSQL(strSql);			
		} catch (SQLException sqlException) {
			Log.e(TAG, "UpdateStockData:" + sqlException.toString());
		}
	}
	
	public void deleteStockData(String symbol) {
		Log.d(TAG, "Delete Symbol=" + symbol);		
		try {
			// Delete stock data.
			String strSql = "DELETE FROM " + TABLE_SD_LASTPRICE
			          + " WHERE " + COL_SD_SYMBOL + "='" + symbol + "';";			
			Log.d(TAG, "DeleteStockData: SQL=" + strSql);
			coreDB.execSQL(strSql);
		} catch (SQLException sqlException) {
			Log.e(TAG, "DeleteStockData:" + sqlException.toString());
		}
	}	
	
	public Cursor getAllData() {
		Cursor cursor = null;
		try {
			String strSql = "SELECT * FROM " + TABLE_SD_LASTPRICE + " ORDER BY " + COL_SD_NAME + ";";
			Log.d(TAG, "GetAllData: SQL=" + strSql);
			cursor = coreDB.rawQuery(strSql, null);
		} catch (SQLException sqlException) {
			Log.e(TAG, "GetAllData:" + sqlException.toString());
		}		
		return cursor;
	}
	
	public StockData getSymbol(String symbol) {
		Cursor cursorResult = null;
		StockData stockData = new StockData();
		String strSql = "SELECT * FROM " + TABLE_SD_LASTPRICE
		                 + " WHERE " + COL_SD_SYMBOL + "='" + symbol + "';";	
		try {
			Log.d(TAG, "getSymbol: SQL=" + strSql);
			
			cursorResult = coreDB.rawQuery(strSql, null);
			Log.d(TAG, "getSymbol Count: " + cursorResult.getCount());
			if(cursorResult.getCount() > 0 && cursorResult.moveToFirst()) {
				stockData.setSymbol(cursorResult.getString(COL_SD_SYMBOL_IDX));
				stockData.setName(cursorResult.getString(COL_SD_NAME_IDX));
				stockData.setPrice(cursorResult.getDouble(COL_SD_PRICE_IDX));
				stockData.setPercentileChange(cursorResult.getDouble(COL_SD_CHANGE_IDX));
				stockData.setMaximum(cursorResult.getDouble(COL_SD_MAXIMUM_IDX));
				stockData.setMinimum(cursorResult.getDouble(COL_SD_MINIMUM_IDX));
			}
			Log.d(TAG, "getSymbol: Stock " + stockData.getName());
			cursorResult.close();
		} catch (SQLException sqlException) {
			Log.e(TAG, "getSymbol:" + sqlException.toString());
		} catch (NullPointerException npe) {
			Log.e(TAG, "getSymbol:" + npe.toString());
		}
		return stockData;
	}
	
	public boolean doesSymbolExist(String symbol) {
		Cursor cursorResult = null;
		boolean result = true;
		symbol = symbol.toUpperCase();
		String strSql = "SELECT * FROM " + TABLE_SD_LASTPRICE
		                 + " WHERE " + COL_SD_SYMBOL + "='" + symbol + "';";	
		try {
			Log.d(TAG, "doesSymbolExist: SQL=" + strSql);
			cursorResult = coreDB.rawQuery(strSql, null);
			if(cursorResult.getCount() == 0) {
				result = false;
			}
			Log.d(TAG, "DoesSymbolExist: " + result);
			cursorResult.close();
		} catch (SQLException sqlException) {
			Log.e(TAG, "DoesSymbolExist:" + sqlException.toString());
		} catch (NullPointerException npe) {
			Log.e(TAG, "DoesSymbolExist:" + npe.toString());
		}
		return result;
	}
}