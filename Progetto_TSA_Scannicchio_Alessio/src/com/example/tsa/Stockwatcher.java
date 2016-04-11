package com.example.tsa;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Stockwatcher extends Activity{
	
	private static final int DIALOG_ERROR_ID=1;
	private static final int DIALOG_CONFIRM_ID=2;

	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.stockwatcher);
		
		
		showDialog(DIALOG_ERROR_ID);
		showDialog(DIALOG_CONFIRM_ID);
		
		ListView listView = (ListView)findViewById(R.id.listViewDemo);
        String [] array = {"Yahoo","Google","Apple","Fiat", "MIB", "NASDAQ"};
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);//R.layout.row, R.id.textViewList
        listView.setAdapter(arrayAdapter);		
	}
}
