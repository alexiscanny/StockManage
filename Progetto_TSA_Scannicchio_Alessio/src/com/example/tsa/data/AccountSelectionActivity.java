package com.example.tsa.data;

import android.app.ListActivity; 
import android.widget.*;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.*;
import android.view.*;
import android.content.Context;
import android.content.Intent;

/*ricordiamoci che quando un'attivit� � costituita esclusivamente da una lista android mette a disposizione al posto 
 * di una intera classe sotto la quale si 
 * deve definire un layout basato su un componente ListView ed il relativo Adapter si pu� estendere direttamente 
 * la classe android.app.ListActivity.
 * In questo modo non c'� bisogno di definire layout e l'adattatore pu� essere impostato direttamente 
 * sull'attivit� chiamando il suo metodo setListAdapter()*/

public class AccountSelectionActivity extends ListActivity{	
	
	private DatabaseHelper dbHelper;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		dbHelper=new DatabaseHelper(this);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		//dobbiamo rendere il db in lettura
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		//ora ci serve il cursore per avanzare nella tabella
		Cursor cursor = db.query(TabellaAccount.TABELLA_NOME, /*lista delle colonne che devono ritornare dalla query*/new String[]{
						TabellaAccount.ID_COLONNA, TabellaAccount.TABELLA_MAIL, TabellaAccount.TABELLA_PASSWORD
			}, 
			/*dal result filtro solo le righe con una determinata SELECT Passando null restituir� tutte le righe per la tabella specificata.*/null, 
			/*SELECTIONARGS*/null, 
			/*GROUPBY*/null, 
			/*HAVING*/null, 
			TabellaAccount.ID_COLONNA+"ASC");
		
		/*Nelle applicazione il componente grafico pi� usato � la lista di oggetti.
		 * Infatti lo troviamo nella lista dei messaggi, lista dei contatti, lista delle ultime chiamate, 
		 * sms inviati/ricevuti
		 * Il componente grafico da usare quando si vuole visualizzare una lista � ListView, questo componente 
		 * si occupa della grafica mentre i dati mostrati
		 * e il layout vengono presi da un'interfaccia java che si chiama ListAdapter, per associare una ListView 
		 * e la ListAdapter si usa il metodo setAdapter.
		 * Dell'interfaccia ListAdapter si  usa una delle classi:
		 * 1- ArrayAdapter
		 * 2- SimpleAdapter
		 * 3- CursorAdapter: gestisce un cursor per mostrare i dati presi da un database
		 * usiamo il metodo di listActivity ossia setListAdapter a cui gli si deve passare un adapter
		 * CursorAdapter(Context context, Cursor c, int flags)*/
		
		setListAdapter(new CursorAdapter(this, cursor, true){
			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent){//import content.Context
				TextView textView = new TextView(AccountSelectionActivity.this);
				textView.setText(cursor.getString(1));
				return textView;
			}
			
			@Override
			public void bindView(View view, Context context, Cursor cursor){
				TextView textView = (TextView) view;
				textView.setText(cursor.getString(1));
			}
		});
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		dbHelper.close();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		setResult(1, new Intent(String.valueOf(id)));//import content.Intent
		finish();
	}

}