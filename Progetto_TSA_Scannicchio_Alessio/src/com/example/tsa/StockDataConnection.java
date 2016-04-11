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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class StockDataConnection {
	// This variable is used for debug log (LogCat). 
	private static final String TAG = "TSA:InternetConnection";
	private TelephonyManager	phoneManager;
	private WifiManager			wifiManager;
	
	// Flags
	private boolean ableNetworkRoaming = false;
	
	public StockDataConnection(Context context) {
		/* Get telephony service: Use with getSystemService(String) to retrieve a TelephonyManager 
		for handling management the telephony features of the device.*/
		phoneManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		// Same thing get WIFI service.
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public void EnableNetworkRoaming(boolean flag) {
		ableNetworkRoaming = flag;
	}
	//we can try wifi available
	private boolean isWifiAvailable() {
		boolean wifiConnectionAvailable = false;
		try {
			if (wifiManager.isWifiEnabled()) {
				WifiInfo info = wifiManager.getConnectionInfo();			
				if (info.getNetworkId() != -1) {
					wifiConnectionAvailable = true;
			  } else {
					Log.w(TAG, "Not connected to any network by WIFI");
				}
			} else {
				Log.w(TAG, "WIFI is not enabled");
			}
		} catch (Exception e) {
			Log.e(TAG, "IsWifiAvailable:" + e.toString());
		}	
		return wifiConnectionAvailable;
	}
	//we can try phone connectione available
	private boolean isPhoneConnectionAvailable() {
		boolean phoneConnectivity = false;
		int result = phoneManager.getDataState();
		Log.d(TAG, "Phone data state = " + result);		
		if (result == TelephonyManager.DATA_CONNECTED) {
			Log.w(TAG, "IP traffic available");
			phoneConnectivity = true;
		}	
		if (phoneManager.isNetworkRoaming()) {
			if(ableNetworkRoaming) {
				Log.w(TAG, "The device is roaming but user wants to update data as well.");
				phoneConnectivity = true;
			}
		}
		return phoneConnectivity;
	}
	
	public boolean hasConnectivity() {
		boolean dataConnectionAvailable = false;
		try {
			if (isPhoneConnectionAvailable() || isWifiAvailable()) {
				dataConnectionAvailable = true;			
			}
		} catch (Exception e) {
			Log.e(TAG, "CreateConnection: " + e.toString());
		}
		return dataConnectionAvailable;
	}
}