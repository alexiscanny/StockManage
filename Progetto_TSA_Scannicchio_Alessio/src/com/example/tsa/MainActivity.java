package com.example.tsa;

import com.example.tsa.data.DatabaseHelper; //in questo modo mi ha caricato direttamente il database che vede nel progetto
import com.example.tsa.data.DbAdapter;
import com.example.tsa.data.TabellaAccount;

import android.app.AlertDialog;
import android.os.Bundle; 
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;


/*Quando si crea una classe secondaria come in questo caso Activity_Login  importante registrare la classe nel AndroidManifest.xml 
 * e per far partire questa 
 * nuova classe bisogna istanziare un Intent che appunto serve per identificare l'intenzione a far qualcosa quindi avremo:
 * Intent secondaAttivitˆ = new Intent(this, ActivityClass2.class)
 * startActivity(secondaAttivitˆ)*/

public class MainActivity extends Activity {

	
	private final int MAIN_LOGIN=1;
	private final int MAIN_CANC_UTENTE=2;
	private final int MAIN_OSPITE=3;
	private final int MAIN_UTENTI=4;
	
	private DbAdapter dbHelper1;
	public DatabaseHelper dbHelper; //mi carico il mio  database

	private static Cursor cursor; //cursore per scorrere nel mio DB
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//creo l'helper per aprire il DB
		dbHelper1=new DbAdapter(this);
		dbHelper= new DatabaseHelper(this); //istanziamo l'helper cos“ lo possiamo utilizzare nel codice quando ci serve
		
		//pulsante del login devo aggiustare l'eccezione sull'errore della password
		Button button_login= (Button) findViewById(R.id.loginbutton);
		//button.setText(R.string.login);
		button_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startLogin();				
			}
		});
		
		//pulsante del registrati, con questo pulsante dovrei accedere al database e creare un account
		Button button_registrati= (Button) findViewById(R.id.registrati);
		//button2.setText(R.string.registrati);
		button_registrati.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//qui creo e collego la pagina di registrazione
				startRegistrati();		
			}
		});
	}
	
	private void startRegistrati(){
		Intent in= new Intent(this, Registrati.class);
		startActivity(in);
		
	}
	
	public void startLogin(){
		Intent in=new Intent(this, Login.class);
		startActivity(in); //attenzione questo  startActivity	
	}
	
	/*Questo metodo mi serve per evitare di creare troppi oggetti toast*/
	public void alert(String message){
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		dbHelper.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(Menu.NONE, MAIN_OSPITE, 0, "Accedi come Ospite");
		menu.add(Menu.NONE, MAIN_UTENTI, 1, "Visualizza Utenti");
		menu.add(Menu.NONE, MAIN_CANC_UTENTE, 2, "Elimina Utente");
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		
		//apro il DB in scrittura
		SQLiteDatabase db= dbHelper.getWritableDatabase();
		
		switch(item.getItemId()){
		case MAIN_OSPITE:
			//in this case the user has some restrictions, for exemple cannot send them an email with the report
			setContentView(R.layout.activity_verifica);
			//qui ci vorrˆ la modifica per far aprire direttamente la classe verifica e fare tutto li dentro
			Button bottone=(Button) findViewById(R.id.Abilita);
			//qui attende che accade l'evento, che sarebbe, spingi il pulsante abilita i tasti
			bottone.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startActivity_verifica();						
				}
			});
			alert("Accedi con le restrizioni");
			return true;
		case MAIN_UTENTI:
			//voglio far visualizzare la lista degli utenti che si trovano del DB
			
			//Assegno al cursore tutti gli account nel DB, con questo cursore posso scansionare il database col metodo c.moveToNext()
			cursor = dbHelper.getAccounts();
			
			/*Abbiamo a disposizione i dati recuperati dal database, e possiamo utilizzarli allÕinterno della nostra applicazione.*/
			
			stampaCursore(cursor);
			
			//Chiudiamo cursore
			alert("Utenti visualizzati nel LogCat");
			return true;
		case MAIN_CANC_UTENTE:
			//attenzione questo caso deve essere scritto meglio perch l'ho usato come utilitˆ per cancellare le righe..devo passare l'id che devo cancellare
			cursor = dbHelper.getAccounts();
			//String id= cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_CONTACTID));

			while (cursor.moveToNext()){
				long id= cursor.getInt(cursor.getColumnIndex(DbAdapter.KEY_CONTACTID));
				DatabaseHelper.cancellaAccount(db, id);
			}
			alert("Tutti gli Utenti eliminati");
			//cursor = dbHelper.getAccounts();
			stampaCursore(cursor);
		}
		cursor.close();
		dbHelper.close();
		return false;
	}
	
	private void startActivity_verifica(){
		Intent intent= new Intent(getBaseContext(), com.example.tsa.Verifica.class);
		startActivity(intent);
	}

	 /*  Con questo metodo possiamo stampare nei log gli identificatori dei contatti ciclando sullÕoggetto Cursor
	 *  Il metodo moveToNext() muove il cursore alla riga successiva, dalla quale possiamo ricavare il valore che 
	 *  ci interessa attraverso 
	 *  il metodo getColumnIndex(), che restituisce lÕindice della colonna richiesta, e il metodo getString(),
	 *   che invece ritorna come stringa 
	 *  il valore della colonna richiesta.*/
	
	public static void stampaCursore(Cursor c){
		while (c.moveToNext()){
			String all="";
			String contactID= cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_CONTACTID));
			String mailLog=cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_MAIL));
			String passwordLog=cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_PASSWORD));
			all="Main "+contactID+"=> mail: "+mailLog+" password: "+passwordLog;
			System.err.println(all);
		}
	}
}
