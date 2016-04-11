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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class StockDataService extends Service {
	// This variable is used for debug log (LogCat).
	private static final String TAG = "TSA:Service";

	// Intent string for broadcasting.
	public static final String ACTIVITY_TO_SERVICE_BROADCAST = "com.alessio.action.SPV_A_TO_S_BROADCAST";
	public static final String SERVICE_TO_ACTIVITY_BROADCAST = "com.alessio.action.SPV_S_TO_A_BROADCAST";

	// Intent key for broadcasting.
	private static final String BROADCAST_KEY_ROAMING_OPT = "roaming";
	private static final String BROADCAST_KEY_LASTUPDATETIME = "lastupdatetime";
	private static final String BROADCAST_KEY_TYPE = "type";
	private static final String BROADCAST_KEY_SYMBOL = "symbol";
	private static final String BROADCAST_KEY_UPDATETIME = "update_interval";

	private static final int STOCKDATA_ADD_NEW = 1;
	private static final int STOCKDATA_CONF_UPDATED = 2;
	private static final int STOCKDATA_NEWDATA_UPD = 3;
	private static final int STOCKDATA_NODATA_UPD = 4;
	private static final int STOCKDATA_ADD_FAIL = 5;

	// Preference keys.
	private static final String KEY_ROAMING_OPT = "roaming_option";
	private static final String KEY_UPDATE_INTERVAL = "update_interval";
	private static final String KEY_LASTUPDATETIME = "last_update_time";

	private StockDataDB db = null;
	private StockDataProviderYahoo provider = null;

	private String newSymbol = "";
	private Cursor allDataDB = null;

	// Broadcast receiver.
	private StockBroadcastReceiver myIntentReceiver = null;

	// Task delay time (in ms).
	private long taskDelay = 60000; // 1 min
	private long userTaskDelay = 900000; // 15 min
	private final long generalTaskDelay = 900000; // 15 min
	private final long maxTaskDelay = 86400000; // 1 days

	private long refTime = 0;
	private boolean refRoaming = false;

	// creo un oggetto relativo alla connessione
	private StockDataConnection stockDataConnection;

	private Thread parserThread;
	private boolean parserThreadAlive = true;

	private final IBinder binder = new LocalBinder();

	private long startTimes = 0;

	private SharedPreferences sharedPreferences;
	//connessione brodoritornohykwok

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		StockDataService getService() {
			return StockDataService.this;
		}
	}

	@Override
	public IBinder onBind(Intent i) {
		Log.d(TAG, "onBind.");
		return binder;
	}

	@Override
	public boolean onUnbind(Intent i) {
		Log.d(TAG, "onUnbind.");
		return false;
	}

	@Override
	public void onRebind(Intent i) {
		Log.d(TAG, "onRebind.");
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate.");
		super.onCreate();

		// Connection.
		stockDataConnection = new StockDataConnection(this);

		// Create provider.
		provider = new StockDataProviderYahoo(this);

		// Create Database.
		db = new StockDataDB(this);
		allDataDB = db.getAllData();

		// Register broadcast receiver.
		IntentFilter filter = new IntentFilter(ACTIVITY_TO_SERVICE_BROADCAST);
		myIntentReceiver = new StockBroadcastReceiver();
		registerReceiver(myIntentReceiver, filter);

		// Create a new thread to handle database update.
		parserThreadAlive = true;
		parserThread = new Thread(mTask);

		// Get preferences.
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		startTimes++;
		Log.d(TAG, "startTimes=" + startTimes);
	}

	@Override
	public void onStart(Intent i, int startId) {
		Log.d(TAG, "onStart.");
		super.onStart(i, startId);

		// Get data from preferences first.
		refTime = sharedPreferences.getLong(KEY_LASTUPDATETIME, refTime);
		refRoaming = sharedPreferences.getBoolean(KEY_ROAMING_OPT, refRoaming);
		String szUpdateTime = sharedPreferences.getString(KEY_UPDATE_INTERVAL,
				"15");
		userTaskDelay = Long.parseLong(szUpdateTime);

		// Then get data from intent if possible.
		refTime = i.getExtras().getLong(BROADCAST_KEY_LASTUPDATETIME, refTime);
		refRoaming = i.getExtras().getBoolean(BROADCAST_KEY_ROAMING_OPT,
				refRoaming);
		userTaskDelay = i.getExtras().getLong(BROADCAST_KEY_UPDATETIME,
				userTaskDelay) * 60000;

		stockDataConnection.EnableNetworkRoaming(refRoaming);

		// Start a new thread to handle database update.
		if (parserThread.isAlive() == false) {
			parserThread.start();
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy.");
		super.onDestroy();

		parserThreadAlive = false;
		parserThread.interrupt();

		// Remove broadcast receiver.
		unregisterReceiver(myIntentReceiver);
		allDataDB.close();
		if (db != null) {
			db.closeDB();
			db = null;
		}
		startTimes--;
	}

	// Background thread to get data from Internet.
	private Runnable mTask = new Runnable() {
		String symbol[], dbSymbol;
		StockData stockData;
		int total;
		long currentTime;

		private void delay() {
			try {
				Log.d(TAG, "sleep = " + taskDelay);
				Thread.sleep(taskDelay);
			} catch (InterruptedException e) {
				Log.d(TAG, "Parser thread receive interrupt");
			}
		}

		public void run() {
			do {
				// Update.
				currentTime = System.currentTimeMillis();
				try {
					Log.d(TAG, "New Symbol: " + newSymbol);
					if (newSymbol.equalsIgnoreCase("")) {
						// Update all symbols.
						allDataDB.requery();
						if (allDataDB.getCount() > 0) {
							symbol = new String[allDataDB.getCount()];
							allDataDB.moveToFirst();
							int cnt = 0;
							do {
								dbSymbol = allDataDB.getString(StockDataDB.COL_SD_SYMBOL_IDX);
								symbol[cnt++] = dbSymbol;
							} while (allDataDB.moveToNext());

							if (cnt > 0 && provider.startGettingDataFromYahoo(symbol)) {
								total = provider.getStockDataCount();
								Log.d(TAG, "Total data has to be updated is "
										+ total);
								for (int i = 0; i < total; i++) {
									stockData = provider.getStockData(i);
									db.updateStockData(stockData);
								}

								// Update last update time.
								refTime = currentTime;
								taskDelay = userTaskDelay;

								// Send data to activity to update view.
								sendSettingToActivity(STOCKDATA_NEWDATA_UPD);
							} else {
								sendSettingToActivity(STOCKDATA_NODATA_UPD);
							}
							if (cnt == 0) {
								Log.d(TAG, "No symbol has to be checked...");
								taskDelay = generalTaskDelay;
							}
						} else {
							// No symbol
							Log.d(TAG, "No symbol in the database");
							taskDelay = maxTaskDelay;
						}
					} else 
						//add new stock
					{
						symbol = new String[1];
						symbol[0] = newSymbol;
						newSymbol = "";
						if (provider.startGettingDataFromYahoo(symbol)) { //go to StockDataProviderYahoo
							stockData = provider.getStockData(0);
							if (stockData != null) {
								Log.d(TAG, "Inserting new symbol in database: "
										+ stockData);
								db.insertStockData(stockData);
								sendSettingToActivity(STOCKDATA_ADD_NEW);
							} else {
								sendSettingToActivity(STOCKDATA_ADD_FAIL);
							}
						} else {
							sendSettingToActivity(STOCKDATA_NODATA_UPD);
						}
						taskDelay = generalTaskDelay - (currentTime - refTime);
						if (taskDelay < 1) {
							taskDelay = 1;
						}
					}
					symbol = null;
				} catch (Exception e) {
					Log.e(TAG, "mTask: " + e.toString());
					sendSettingToActivity(STOCKDATA_NODATA_UPD);
				}
				// Call this task again.
				delay();
			} while (parserThreadAlive);
		}
	};

	// Receives data from other activities.
	public class StockBroadcastReceiver extends BroadcastReceiver {
		int type;

		@Override
		public void onReceive(Context context, Intent intent) {
			// Receive intent from activity.
			Log.d(TAG, "receive data from activity >>>>>");
			try {
				type = intent.getExtras().getInt(BROADCAST_KEY_TYPE);
				switch (type) {
				case STOCKDATA_ADD_NEW:
					newSymbol = intent.getExtras().getString(
							BROADCAST_KEY_SYMBOL);
					Log.d(TAG, "New Symbol to be fetched: " + newSymbol);
					parserThread.interrupt();
					break;

				case STOCKDATA_CONF_UPDATED:
					refTime = intent.getExtras().getLong(
							BROADCAST_KEY_LASTUPDATETIME);
					refRoaming = intent.getExtras().getBoolean(
							BROADCAST_KEY_ROAMING_OPT, false);
					break;

				default:
					break;
				}
			} catch (Exception e) {
				Log.e(TAG, "Broadcast_Receiver: " + e.toString());
			}
		}
	}

	// Send data to activity.
	void sendSettingToActivity(int type) {
		Intent intent = new Intent(SERVICE_TO_ACTIVITY_BROADCAST);
		intent.putExtra(BROADCAST_KEY_TYPE, type);
		intent.putExtra(BROADCAST_KEY_LASTUPDATETIME, refTime);
		savePreferences();
		Log.d(TAG, "send data to activity >>>>>");
		sendBroadcast(intent);
	}

	void savePreferences() {
		try {
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putLong(KEY_LASTUPDATETIME, refTime);
			editor.commit();
		} catch (Exception e) {
			Log.e(TAG, "SavePreferences: " + e.toString());
		}
	}
}