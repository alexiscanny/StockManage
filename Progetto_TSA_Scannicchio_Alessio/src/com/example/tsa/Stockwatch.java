package com.example.tsa;

import java.text.DateFormat;   
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Stockwatch extends Activity {
	// Debug tag for LogCat.
	private static final String TAG = "TSA:Stockwatch";
	
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
	private static final int STOCKDATA_ALREADY_EXIST = 6;
		
	private static final int REFRESH = Menu.FIRST + 2;
	private static final int MENU_ABOUT = Menu.FIRST;
	private static final int MENU_PREFERENCE = Menu.FIRST + 1;
	
	private static final int DIALOG_DELETE_SYMBOL = 1;
	private static final int DIALOG_ABOUT = 2;
	
	// Preference keys
	private static final String KEY_ROAMING_OPT = "roaming_option";
	private static final String KEY_UPDATE_INTERVAL = "update_interval";
	private static final String KEY_LASTUPDATETIME = "last_update_time";
	private static final String KEY_BKUPDATE = "background_update";
	
	// Backup keys
	private static final String KEY_BK_EDITTEXT = "backup_edittext";
	
	// Intent String for broadcasting. http://www.anddev.it/index.php?topic=10114.0
	private static final String ACTIVITY_TO_SERVICE_BROADCAST = "com.alessio.action.SPV_A_TO_S_BROADCAST"; 
	private static final String SERVICE_TO_ACTIVITY_BROADCAST = "com.alessio.action.SPV_S_TO_A_BROADCAST";
	
	// Message ID
	private static final int GUI_UPDATE_LISTVIEW = 0x100;
	private static final int GUI_UPDATE_LISTVIEW_FAIL = 0x101;
	
	// Controls
	private EditText editTextSymbol;
	private TextView textViewUpdateTime;
	private Button buttonAdd;
	private ListView listViewStocks;
	private ProgressDialog progressDialog = null;
	
	// StockPriceList
	private StockPriceListAdapter stockPriceListAdapter;
	
	// Preferences
	private long lastUpdateTime;
	private boolean enableRoaming;
	private long updateInterval;
	private boolean	enableBkUpdate;
	
	// Broadcast receiver
	private BroadcastReceiver myIntentReceiver;
	
	private StockDataDB	db;	
	private SharedPreferences	sharedPreferences;
	private String selectedSymbol = "";
	private String savedInstanceText = "";
	private static Context thisContext;
		
  /** 
   * Called when the activity is created. 
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate method.");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.stockwatch);
    thisContext = this;
    try {       	
    	// Get controls.
    	editTextSymbol = (EditText) findViewById(R.id.EditTextSymbol);
    	textViewUpdateTime = (TextView) findViewById(R.id.TextViewLastUpdate);
    	buttonAdd = (Button) findViewById(R.id.ButtonAdd);
    	listViewStocks = (ListView) findViewById(R.id.ListViewStock);
    	
    	// Get Database.
    	db = new StockDataDB(this);
    	
    	// Get preferences.
      sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	if(savedInstanceState != null) {
      	// Just restore edit text box firstly.
      	String textSymbol = savedInstanceState.getString(KEY_BK_EDITTEXT);	
      	Log.d(TAG, "text_symbol = " + textSymbol); 	
      	savedInstanceText = textSymbol;
      	editTextSymbol.setText(textSymbol);	        	
      }
    	
    	// Set listeners.
    	editTextSymbol.setSingleLine();
    	editTextSymbol.setOnFocusChangeListener(focusListenerSymbol);
    	buttonAdd.setOnClickListener(clickListenerBtnAdd);	
    	listViewStocks.setOnItemClickListener(clickListenerListView);
    	listViewStocks.setOnItemLongClickListener(longClickListenerListView);
    	stockPriceListAdapter = new StockPriceListAdapter(this, db.getAllData());
    	listViewStocks.setAdapter(stockPriceListAdapter);
    	
    	// Register broadcast receiver.
    	IntentFilter filter = new IntentFilter(SERVICE_TO_ACTIVITY_BROADCAST);
    	myIntentReceiver = new StockBroadcastReceiver();
    	registerReceiver(myIntentReceiver, filter);        	
    } catch (Exception e) {
    	Log.e(TAG, "onCreate: " + e.toString());
    }
  }
    
  @Override
  public void onStart() {
  	Log.d(TAG, "onStart."); 	
  	super.onStart(); 	
  	try {
  		// Check service options.
		  Intent intent = new Intent(this, StockDataService.class);		
		  lastUpdateTime = sharedPreferences.getLong(KEY_LASTUPDATETIME, 0);
  		enableRoaming = sharedPreferences.getBoolean(KEY_ROAMING_OPT, false);    		
  		String szUpdateTime = sharedPreferences.getString(KEY_UPDATE_INTERVAL, "15");
  		updateInterval = Long.parseLong(szUpdateTime);
  		enableBkUpdate = sharedPreferences.getBoolean(KEY_BKUPDATE, true);
  		
  		// Update last update time.
  		String szTime = DateFormat.getDateTimeInstance(DateFormat.LONG,
  				DateFormat.SHORT).format(new Date(lastUpdateTime));
  		textViewUpdateTime.setText(szTime);
  		    		
  		intent.putExtra(BROADCAST_KEY_ROAMING_OPT, enableRoaming);
  		intent.putExtra(BROADCAST_KEY_LASTUPDATETIME, lastUpdateTime);
  		intent.putExtra(BROADCAST_KEY_UPDATETIME, updateInterval);
  		
  		if(enableBkUpdate) {
  			this.startService(intent);
  		} else {
  			this.stopService(intent);
  		}
  	} catch (Exception e) {
  		Log.e(TAG, "Error:" + e.toString());
  	}
  }
  
  @Override
  public void onRestart() {
  	Log.d(TAG, "onRestart");    	
  	super.onRestart();
  }
  
  @Override
  public void onResume() {
  	Log.d(TAG, "onResume");	
  	super.onResume();
  }
  
  @Override
  public void onPause() {
  	Log.d(TAG, "onPause");
  	super.onPause();
  }
  
  @Override
  public void onStop() {
  	Log.d(TAG, "onStop");    	
  	super.onStop();
  }
  
  @Override
  public void onDestroy() {
  	Log.d(TAG, "onDestroy"); 	
  	super.onDestroy();	
  	try {
  		stockPriceListAdapter.freeResources();
  		stockPriceListAdapter = null;
  		
    	// Remove broadcast receiver.
		  unregisterReceiver(myIntentReceiver);
		
		  // Save preferences.
		  savePreferences();
    	
    	if(db != null) {
    		db.closeDB();
    		db = null;
    	}
  	} catch (Exception e) {
  		Log.e(TAG, "onDestroy" + e.toString());
  	}
  }
  
  @Override
  public Object onRetainNonConfigurationInstance() {
  	Log.d(TAG, "onRetainNonConfigurationInstance.");   	
	  return null;        
  }
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
  	Log.d(TAG, "onSaveInstanceState."); 	
  	// Backup current TextView.
  	String txt_symbol = editTextSymbol.getText().toString(); 	
  	Log.d(TAG, "Backup txt_symbol=" + txt_symbol); 	
  	outState.putString(KEY_BK_EDITTEXT, txt_symbol);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
  	MenuItem menuItem;
  	super.onCreateOptionsMenu(menu);
  	// Create menu.
  	menuItem = menu.add(0, REFRESH, 0, R.string.refreshMenu);
  	menuItem.setIcon(R.drawable.refresh);
  	menuItem = menu.add(0, MENU_PREFERENCE, 1, R.string.szMenu_Preference);
  	menuItem.setIcon(android.R.drawable.ic_menu_preferences);
  	menuItem = menu.add(0, MENU_ABOUT, 2, R.string.szMenu_About);
  	menuItem.setIcon(android.R.drawable.ic_menu_info_details);    	
  	return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
  	switch(item.getItemId()) {
  	  case REFRESH:
  	  	// Refresh stocks values.
  	  	refreshStockValues();
  	  	break;
    	case MENU_ABOUT:
    		// Show about dialog box.
    		showDialog(DIALOG_ABOUT);
    		break;    		
    	case MENU_PREFERENCE:
    		// Launch CurrencyPreference activity.
    		Intent intent = new Intent(this, StockPricePreferences.class);
    		startActivity(intent);
    		break;    		
    	default:
    		break;
  	}	
  	return super.onOptionsItemSelected(item);
  }
  
  @Override
  protected Dialog onCreateDialog(int id) {
  	Dialog dialog;  	
  	switch(id) {
  		case DIALOG_DELETE_SYMBOL:
  			dialog = createDeleteSymbolDialog(this);
  			String title = "Symbol: " + selectedSymbol;
  			dialog.setTitle(title);
  			return dialog;  			
  		case DIALOG_ABOUT:
  			return new AboutDialog(this);  		
  		default:
  			break;
  	}
  	return null;
  }
  
  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
  	switch(id) {    		
  		case DIALOG_DELETE_SYMBOL:
  			String title = "Symbol: " + selectedSymbol;
  			dialog.setTitle(title);
  			break;  			
  		default:
  			break;
  	}
  }
  
  // EditText control.
  private OnFocusChangeListener focusListenerSymbol = new OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			String mCurrentInputValue = editTextSymbol.getText().toString();	
			if (hasFocus) {
				Log.d(TAG, "editTextSymbol EditText on focus. text=" + mCurrentInputValue);
				int textLength = mCurrentInputValue.length();
				if (textLength > 0) {
					if (mCurrentInputValue.equals(savedInstanceText)) {
						savedInstanceText = "";
						// Move the cursor to the end of the text.
						editTextSymbol.setSelection(textLength);
					} else {
						mCurrentInputValue = "";
						editTextSymbol.setText(mCurrentInputValue);
					}
				}				
			} else {
				Log.d(TAG, "editTextSymbol EditText loss focus. text=" + mCurrentInputValue);
				if (mCurrentInputValue.length() == 0) {					
					editTextSymbol.setText("");
				}
			}
		}    	
  };
  
  // ListView control for OnItemClickListener.
  private OnItemClickListener clickListenerListView = new OnItemClickListener() {
  	public void onItemClick(AdapterView<?> arg0, View view,int position, long arg3) {
  		Bundle bundle = new Bundle();
			selectedSymbol = stockPriceListAdapter.getSymbol(position);
			StockData stockData = db.getSymbol(selectedSymbol);
			bundle.putString("SymbolName", stockData.getName());
			bundle.putDouble("Price", stockData.getPrice());
			bundle.putDouble("Change", stockData.getPercentileChange());
			bundle.putDouble("High", stockData.getMaximum());
			bundle.putDouble("Low", stockData.getMinimum());			
  		bundle.putString("Symbol", selectedSymbol);
  		Log.d(TAG, "Launching StockViewActivity!");
			Intent intent = new Intent(thisContext, StockViewActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
		}
  };
  //Cosa accade se si tiene premuto il titolo
  // ListView control for OnItemLongClickListener 
  private OnItemLongClickListener longClickListenerListView = new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			selectedSymbol = stockPriceListAdapter.getSymbol(position);
			showDialog(DIALOG_DELETE_SYMBOL);
			return false;
		}    	
  };
  
  // Button control
  private OnClickListener clickListenerBtnAdd = new OnClickListener() {
		public void onClick(View view) {
			String inputSymbol = editTextSymbol.getText().toString();
			if(!inputSymbol.contentEquals("")  && !db.doesSymbolExist(inputSymbol)) {
				Log.d(TAG, "Input symbol=" + inputSymbol);
				sendSettingToService(STOCKDATA_ADD_NEW, inputSymbol);					
				final CharSequence title = getString(R.string.Dialog_Progress_Title);
				final CharSequence message = getString(R.string.Dialog_Progress_Message);	
				progressDialog = ProgressDialog.show(Stockwatch.this, title, message, true);
			}
			else if (db.doesSymbolExist(inputSymbol)) {
				Log.d(TAG, "Input symbol=" + inputSymbol + " already exists.");
				sendSettingToService(STOCKDATA_ALREADY_EXIST, inputSymbol);			
			}
		}
  };
  
  private void refreshStockValues() {
  	sendSettingToService(STOCKDATA_ADD_NEW, "");					
		final CharSequence title = getString(R.string.Dialog_Progress_Title);
		final CharSequence message = getString(R.string.Dialog_Progress_Message);	
		progressDialog = ProgressDialog.show(Stockwatch.this, title, message, true);
  }
  
  // Create delete symbol dialog box.
  private Dialog createDeleteSymbolDialog(Context context) {
  	Log.d(TAG, "----- createDeleteSymbolDialog -----"); 	
  	AlertDialog.Builder builder = new AlertDialog.Builder(context); 	
  	builder.setMessage(R.string.Dialog_Delete_Symbol_Message);
  	
  	// OK button.
  	builder.setPositiveButton(context.getText(R.string.Dialog_Delete_Symbol_OK), 
  	new DialogInterface.OnClickListener() { 		
  		// handle OK button click.
			public void onClick(DialogInterface dialog, int which) {
				db.deleteStockData(selectedSymbol);
				selectedSymbol = "";
				stockPriceListAdapter.updateInternalData();
				stockPriceListAdapter.notifyDataSetChanged();
			}    		
  	});
  	
  	// Cancel button.
  	builder.setNegativeButton(context.getText(R.string.Dialog_Delete_Symbol_Cancel), 
  	new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// nothing to do for cancel button.
			}    		
  	});
  	return builder.create();
  }
  //connessione al server per cercare il simbolo dell'azione inserita
  // Send data to service.
  void sendSettingToService(int type, String symbol) { 	
  	if(enableBkUpdate == false) {
  		// Has to start service to get data.
  		Intent intent = new Intent(this, StockDataService.class);		    		
  		intent.putExtra(BROADCAST_KEY_ROAMING_OPT, enableRoaming);
  		intent.putExtra(BROADCAST_KEY_LASTUPDATETIME, lastUpdateTime);
  		intent.putExtra(BROADCAST_KEY_UPDATETIME, updateInterval);
  		Log.d(TAG, "Starting intent StockDataService.");
 			this.startService(intent);
  	}	
	  Intent intent = new Intent(ACTIVITY_TO_SERVICE_BROADCAST);
	  intent.putExtra(BROADCAST_KEY_TYPE, type);
	  
	  switch(type) {
	    case STOCKDATA_ADD_NEW:
		    intent.putExtra(BROADCAST_KEY_SYMBOL, symbol);
		    Log.d(TAG, "Sending intent to add: " + symbol);
			  break;		
			case STOCKDATA_CONF_UPDATED:
				intent.putExtra(BROADCAST_KEY_ROAMING_OPT, enableRoaming);
				intent.putExtra(BROADCAST_KEY_LASTUPDATETIME, lastUpdateTime);
				break;	
			case STOCKDATA_ALREADY_EXIST:
				intent = new Intent(SERVICE_TO_ACTIVITY_BROADCAST);
				intent.putExtra(BROADCAST_KEY_TYPE, type);	
			}				
		Log.d(TAG, "send data to service.");
		sendBroadcast(intent);
  }
  //create the broadcast receiver class
	// Receive data from service.
	public class StockBroadcastReceiver extends BroadcastReceiver {	
	  @Override
	  public void onReceive(Context context, Intent intent) {
		  // Receive intent from service.
		  Log.d(TAG, "received data from service.");	
		  try {
		  	int type = intent.getExtras().getInt(BROADCAST_KEY_TYPE);		
			  switch(type) {
				  case STOCKDATA_NEWDATA_UPD:
				  	lastUpdateTime = intent.getExtras().getLong(BROADCAST_KEY_LASTUPDATETIME, 0);					
				  	// Send message to activity.
				  	Stockwatch.this.objHandler.sendEmptyMessage(GUI_UPDATE_LISTVIEW);
					  break;
				  case STOCKDATA_NODATA_UPD:
					  // Send message to activity.
					  Stockwatch.this.objHandler.sendEmptyMessage(STOCKDATA_NODATA_UPD);
					  break;
				  case STOCKDATA_ADD_NEW:
				    // Send message to activity.
					  Stockwatch.this.objHandler.sendEmptyMessage(STOCKDATA_ADD_NEW);
					  break;
				  case STOCKDATA_ADD_FAIL:
				    // Send message to activity.
					  Stockwatch.this.objHandler.sendEmptyMessage(STOCKDATA_ADD_FAIL);
					  break; 
				  case STOCKDATA_ALREADY_EXIST:
				    // Send message to activity.
					  Stockwatch.this.objHandler.sendEmptyMessage(STOCKDATA_ALREADY_EXIST);
					  break; 
			  }
		  } catch (Exception e) {
			  Log.e(TAG, "Broadcast_Receiver:" + e.toString());
		  }
		  if(enableBkUpdate == false) {
			  // Stop the service.
			  Intent i = new Intent(Stockwatch.this, StockDataService.class);
			  Stockwatch.this.stopService(i);
		  }
	  }
	}
      
  // Receive message from other threads.
  private Handler objHandler = new Handler() {
  	@Override
  	public void handleMessage(Message msg) {
  		switch (msg.what) {
  			case GUI_UPDATE_LISTVIEW:
  				// Refresh list view
  				Log.d(TAG, "----- refresh listview -----");
  				// Update stock price list
  				stockPriceListAdapter.updateInternalData();
  				stockPriceListAdapter.notifyDataSetChanged();
  				
			    // Last update time.
			    String szTime = DateFormat.getDateTimeInstance(DateFormat.LONG, 
			    		DateFormat.SHORT).format(new Date(lastUpdateTime));
			    textViewUpdateTime.setText(szTime);
				
			    if (progressDialog != null) {
    		    Toast.makeText(Stockwatch.this, R.string.Toast_Stocks_Updated, Toast.LENGTH_LONG).show();
    		    editTextSymbol.setText("");	
			    }
  		    break;	
  			case STOCKDATA_NODATA_UPD:
  				Toast.makeText(Stockwatch.this, R.string.Toast_No_Update_Needed,
  						Toast.LENGTH_LONG).show();
  				break;
  		  case GUI_UPDATE_LISTVIEW_FAIL:
  			  if(progressDialog != null) {
    			  Toast.makeText(Stockwatch.this, R.string.Toast_Add_Symbol_Fail,
    					  Toast.LENGTH_LONG).show();
    			  editTextSymbol.setText("");
				  }
  		    break;
  		  case STOCKDATA_ADD_FAIL:
  		  	if(progressDialog != null) {
    			  Toast.makeText(Stockwatch.this, R.string.Toast_Add_Symbol_Fail,
    					  Toast.LENGTH_LONG).show();
    			  editTextSymbol.setText("");
				  }
  		  	break;
  		  case STOCKDATA_ADD_NEW:
  		    // Refresh list view
  				Log.d(TAG, "----- refresh listview -----");
  				// Update stock price list
  				stockPriceListAdapter.updateInternalData();
  				stockPriceListAdapter.notifyDataSetChanged();
  				
			    // Last update time.
			    String time = DateFormat.getDateTimeInstance(DateFormat.LONG, 
			    		DateFormat.SHORT).format(new Date(lastUpdateTime));
			    textViewUpdateTime.setText(time);
				
  		  	if(progressDialog != null) {
  			    Toast.makeText(Stockwatch.this, R.string.Toast_Add_Symbol_Ok,
  					  Toast.LENGTH_LONG).show();
  			    editTextSymbol.setText("");
			    }
  		  	break;
  		  case STOCKDATA_ALREADY_EXIST:
  			  Toast.makeText(Stockwatch.this, R.string.Toast_Already_Exist,
  					Toast.LENGTH_LONG).show();
  			  editTextSymbol.setText("");
  		  	break;
  		}
  		//facciamo il confrontohykwok
  		if (progressDialog != null) {
			  progressDialog.dismiss();
			  progressDialog = null;
		  }	
  		super.handleMessage(msg);
  	}    	
  };
  
  void savePreferences() {
  	try {
    	SharedPreferences.Editor editor = sharedPreferences.edit();   	
    	editor.putLong(KEY_LASTUPDATETIME, lastUpdateTime);
    	editor.putBoolean(KEY_BKUPDATE, enableBkUpdate);	
    	editor.commit();
  	} catch (Exception e) {
  		Log.e(TAG, "SavePreferences: " + e.toString());
  	}
  }
}