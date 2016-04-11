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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StockPriceListAdapter extends BaseAdapter {
	// This variable is used for debug log (LogCat).
	private static final String TAG = "TSA:StockPriceListAdapter";
	
	private LayoutInflater mInflater;
	private Cursor	 mPriceData = null;
	private Bitmap[] mIcon;

	public StockPriceListAdapter(Context context, Cursor priceData) {
		mInflater = LayoutInflater.from(context);
		mPriceData = priceData;
		
		mIcon = new Bitmap[3];
		
		mIcon[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.up);
		mIcon[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.down);
		mIcon[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.same);
		
		// Refresh data.
		mPriceData.requery();
	}
	
	@Override
	public void finalize() {
		freeResources();
	}
	
	public int getCount() {
		return mPriceData.getCount();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder	holder;
		String		szSymbol, szName, szPrice, szChange;
		double		change;
		
		try {
			if(convertView == null) {
				// Uses stocklistitem.xml to display each currency selection.
				convertView = mInflater.inflate(R.layout.stocklistitem, null);
				// Then create a holder for this view for faster access
				holder = new ViewHolder();
				
				holder.icon = (ImageView) convertView.findViewById(R.id.list_change_icon);
				holder.name = (TextView) convertView.findViewById(R.id.list_name_text);
				holder.price = (TextView) convertView.findViewById(R.id.list_price_text);
				holder.change = (TextView) convertView.findViewById(R.id.list_change_text);
				holder.symbol = (TextView) convertView.findViewById(R.id.list_symbol_text);
				
				// Store this holder in the list.
				convertView.setTag(holder);
			} else {
				// Load the holder of this view.
				holder = (ViewHolder) convertView.getTag();
			}
			
			if (mPriceData.moveToPosition(position)) {
				szSymbol = mPriceData.getString(StockDataDB.COL_SD_SYMBOL_IDX);
				szName = mPriceData.getString(StockDataDB.COL_SD_NAME_IDX);
				szPrice = Double.toString(mPriceData.getDouble(StockDataDB.COL_SD_PRICE_IDX));
				change = mPriceData.getDouble(StockDataDB.COL_SD_CHANGE_IDX);
				szChange = Double.toString(change);
			} else {
				szSymbol = "???????";
				szName = "";
				szPrice ="---";
				change = 0;
				szChange = "0";
			}		
			if (change > 0) {
				holder.icon.setImageBitmap(mIcon[0]);
			} else if (change < 0) {
				holder.icon.setImageBitmap(mIcon[1]);
			} else {
				holder.icon.setImageBitmap(mIcon[2]);
			}		
			holder.symbol.setText(szSymbol);
			holder.name.setText(szName);
			holder.price.setText(szPrice);
			holder.change.setText(szChange + "%");
		} catch (Exception e) {
			Log.e(TAG, "getView:" + e.toString());
		}
		return convertView;
	}
	
	public void freeResources() {
		if (mPriceData != null) {
			Log.d(TAG, "Close SQL cursor...");
			mPriceData.close();
			mPriceData = null;
		}
	}
	
	public String getSymbol(int position) {
		String	result = "";
		if (mPriceData.moveToPosition(position)) {
			result = mPriceData.getString(0);
		}
		return result;
	}
	
	public void updateInternalData() {
		// Refresh data
		mPriceData.requery();
	}
	
	/* Class ViewHolder */
	private class ViewHolder {
		TextView	symbol;
		TextView	name;
		TextView	price;
		TextView	change;
		ImageView	icon;
	}
}