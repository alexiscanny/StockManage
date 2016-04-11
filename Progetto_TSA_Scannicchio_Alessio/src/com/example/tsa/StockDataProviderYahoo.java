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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class StockDataProviderYahoo {

	// Debug tag for LogCat.
	private static final String TAG = "TSA:StockDataProviderYahoo";
	
	private List<StockData>	data = new ArrayList<StockData>();
	
	// Format URL path (refer to http://www.gummy-stuff.org/Yahoo-data.htm for more information).
	private static final String URL_PREFIX = "http://finance.yahoo.com/d/quotes.csv?s=";
	// 4 items that we want to get symbol + last trade (price only) + percent change + name + day max + day min.
	private static final String[] YAHOO_FLAGS = { "s", "l1", "p2", "n", "h", "g"};
	
	private Context context;
	
	public StockDataProviderYahoo(Context context) {
		this.context = context;
	}
	
	private String getUrl(String[] symbols) {
		String szURL = URL_PREFIX;
		data.clear();
		szURL += symbols[0];
		for(int i = 1; i < symbols.length; i++) {
			szURL += "+" + symbols[i];
		}		
		szURL += "&f=";
		for(String flag : YAHOO_FLAGS) {
			szURL += flag;
		}
		Log.d(TAG, "URL=" + szURL);
		return szURL;
	}
	
	private void addStockData(String[] decodeString) {
		try {
			if (Double.parseDouble(decodeString[1]) > 0) {
	  		StockData stockData = new StockData();		
	  		// Symbol
	  		stockData.setSymbol(decodeString[0].replace('"', ' ').toUpperCase());
	  		stockData.setSymbol(stockData.getSymbol().trim());
	  		// Price
	  		stockData.setPrice(Double.parseDouble(decodeString[1]));
	  		// Percentile Change
	  		String percentileChangeString = decodeString[2];
	  		percentileChangeString = percentileChangeString.replace("%", "");
	  		percentileChangeString = percentileChangeString.replace("\"", "");
	  		stockData.setPercentileChange(Double.parseDouble(percentileChangeString));
	  		// Name
	  		stockData.setName(decodeString[3].replace('"', ' ').trim());
	  		// Day high
	  		stockData.setMaximum(Double.parseDouble(decodeString[4]));
	  		// Day Low
	  		stockData.setMinimum(Double.parseDouble(decodeString[5]));
	  		Log.d(TAG, "Symbol: " + stockData.getSymbol());
	  		Log.d(TAG, "Name: " + stockData.getName());
	  		Log.d(TAG, "Price: " + stockData.getPrice());
	  		Log.d(TAG, "Change: " + stockData.getPercentileChange());
	  		Log.d(TAG, "Maximum: " + stockData.getMaximum());
	  		Log.d(TAG, "Minimum: " + stockData.getMinimum());		
	  		data.add(stockData);
	  	}
	  } catch (Exception e) {
    	// Just display error and handle next line
    	Log.e(TAG, "decode error: " + e.toString());
    }	
	}
	
	public boolean startGettingDataFromYahoo(String[] symbols) {
		boolean result = false;
		String[] decodeString = null;
		String szURL = getUrl(symbols);
		try {
			StockDataConnection stockDataConnection = new StockDataConnection(context);
			if (stockDataConnection.hasConnectivity()) {
				URL url = new URL(szURL);
				InputStream stream = url.openStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		    String line = null;
		    while ((line = reader.readLine()) != null) {
	        Log.d(TAG, "Received: " + line);   
	        decodeString = line.split(",", YAHOO_FLAGS.length);
	        addStockData(decodeString);
		    }
		    stream.close();
				result = true;
			} else {
				Log.d(TAG, "No connectivity available.");
			}
		} catch (Exception e) {
			Log.e(TAG, "Cannot start parser for Input stream. Err= " + e.toString());
			data.clear();
			result = false;
		}	
		return result;
	}
	
	public int getStockDataCount() {
		return data.size();
	}
	
	public StockData getStockData(int index) {
		StockData stockData = null;
		try {
			stockData = data.get(index);
		} catch (Exception e) {
			Log.e(TAG, "getStockData: " + e.toString());
		}	
		return stockData;
	}
}