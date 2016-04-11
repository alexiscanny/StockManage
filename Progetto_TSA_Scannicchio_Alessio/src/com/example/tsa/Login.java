package com.example.tsa;


import com.example.tsa.MainActivity;
import com.example.tsa.data.DatabaseHelper;
import com.example.tsa.data.DbAdapter;

import java.util.regex.*;//serve per avere i metodi esatti per validare la mail 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

import android.content.*; 
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;//per la finestra di dialogo riga 128
import android.annotation.SuppressLint;
import android.app.*;
import android.os.*;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

public class Login extends Activity {
	
	private DbAdapter dbHelper;
	private Cursor cursor;
	
	//l'oggetto TentAuth mi dovrà verificare se è gia in atto o meno il login, infatti fà parte della classe LoginUtente
	public LoginUtente TentAuth = null;
	//parametri da usare come stringa cioè quelli che verranno passati
	private String mailstringa;
	
	//parametri da usare come vista cioè da usare come EditText per gli spazi=GUI
	private EditText mailvista;
	private EditText passwordvista;
	
	//valori di email e di password inseriti dall'utente
	private String mail;
	private String password;
	
	private DatabaseHelper dbHelperLog;//mi carico il database
	private Cursor cursorLog;
	
	/*Queste costanti ci servono per avvisare alla riga 128 quale è l'errore, se mail sbagliata o password*/
	private final int errore=1; //errata o la mail o la password
	private final int loginok=2;//è andato tutto a buon fine mail e passwd esatti
	/*Il Context è una classe astratta che permette di raggiungere tutte le risorese dell'applicazione nonchè tutte le classi, l'implementazione di tale classe
	 * è fornita dal sistema*/	
	Context context;

	protected void onCreate (Bundle savedIstanceState){
		super.onCreate(savedIstanceState);
		context = getApplicationContext();
		
		dbHelperLog=new DatabaseHelper(this);//istanzio l'helper per usarlo nel codice
		//apro il database in lettura
		SQLiteDatabase db = dbHelperLog.getReadableDatabase();
		
		//settiamo il layout che deve avere la pagina
		setContentView(R.layout.activity_login);
		
		//Gli oggetti della Graphical User Interface cioè i pulsanti che andremo ad inserire nella pagina e li richiamiamo con il loro id
		mailvista= (EditText) findViewById(R.id.email);
		passwordvista= (EditText) findViewById(R.id.password);
		
		
		
		/*andiamo a provare questa password che l'utente vuole inserire 
		 Se l'utente da il return subito dopo aver inserito la password con il metodo onEditorAction (tale metodo viene chiamato quando un'azione 
		si sta realizzanda)proviamo ad accedere con login
		vedremo che quando sceglieremo il metodo OnEditorActionListener() mi autogenererà un pezzo codice*/
		passwordvista.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId==R.id.password || actionId==EditorInfo.IME_NULL)//EditorInfo è una classe che descrive vari attributi
				{
					tentativologin();
					return true;
				}
				return false;
			}
		});
		/*Nel caso in cui l'utente dovesse spingere il pulsante accedi invece di pigiare return*/
		//metodo pulsante ENTRA
		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				tentativologin();
			}
		});		
	}
	
	public void alert(String message){
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	//metodo per il tentativo di autentificazione, fà il login solo se non si sta già provando, se TentAuth è null il logind deve essere ancora fatto
	@SuppressLint("NewApi")
	public void tentativologin(){
		if (TentAuth != null){
			//diverso da null significa che hai già provato a fare il login quindi esce dall'if
			return;
		}
		//Essendo TentAuth=null dobbiamo iniziare il processo di autentificazione
		//resetto i valori
		mailvista.setError(null);
		passwordvista.setError(null);
		
		//----------------------------
		
		//assegno nuovi valori che inserisco da tastiera
		mail=mailvista.getText().toString();
		password=passwordvista.getText().toString();
		
		//mi serve per vedere se qualcosa è andato storto nella mail o password
		boolean cancel_passwd=false, cancel_mail=false, cancel_verifica=false;
		View focusVista=null;
		
		
		
		//facciamo una verifica sulla password in caso è vuota o minore di 4 caratteri
		if (password.isEmpty()||password.length()<4){
			//errore nella password
			passwordvista.setError(password);
			
			//Toast toast = Toast.makeText(this/*getBaseContext()*/, "campo pwd vuoto o pwd<4 chart ", Toast.LENGTH_SHORT);
			focusVista=passwordvista;
			//toast.show();
			cancel_passwd=true;
		}
		
		/*Si poteva anche scrivere il codice più corretto in questo modo:
		 * if (TextUtils.isEmpty(password)||password.length()<4){
		 * setta errore */
		//la stessa cosa per la password la facciamo per la mail
		//controlliamo se è un indirizzo mail corretto o se campo vuoto
		
		//settiamo il pattern per il confronto
		Pattern p =Pattern.compile(".+@.+\\.[a-z]+");
		//Eseguiamo il match della stringa data con il pattern che abbiamo
		Matcher m=p.matcher(mail);
		boolean matchOk =m.matches();
		//ora vediamo se sono 
		if (mail.isEmpty()|| matchOk==false){
			mailvista.setError(mail);
			Toast toast = Toast.makeText(this/*getBaseContext()*/, "Mail vuota o simboli mancanti", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP, 40, 40);
			focusVista=mailvista;
			alert("Mail errata");
			toast.show();
			cancel_mail=true;			
		}
		//gli passo i valori ricevuti da tastiera al metodo verifica, dovrà ritornare cancel_verifica=false se esiste l'account
		cancel_verifica=verificaAccount(mail, password);
		//ora dobbiamo verificare il cancel perchè se è vero lo rimandiamo al login se è falso vuol dire che è andato tutto ok
		if (cancel_mail==true || cancel_passwd==true || cancel_verifica==true){
			
			//deve rifare il login
			//focusVista.requestFocus(); //questo metodo return false di defoult
			/*Devo inserire un Toast per avvisarlo dell'errore...perchè se ha inserito una password vuota o <4 o una mail 
			 * errata lo devo avvisare altrimenti si ritrova nella pagina del login senza sapere il motivo e può pensare che 
			 * è crashata l'applicazione e ritorna al metodo tentativo login per rieffettuare il login*/
			alert("Attenzione!!");
			alert("...Errore nella composizione della mail o della password");
		//	tentativologin();
		}else{			
			/*ok è andato tutto bene, però ora devo effettuare il collegamento al database e solo se realmente l'account è presente nel database allora può
			 * iniziare l'autenticazione*/
			
			TentAuth = new LoginUtente();
			TentAuth.execute((Void)null);
		}
		
		
	}
	
	private boolean verificaAccount(String mail, String password){//riga128
		boolean flag=true;
		//Apro il cursore e ottengo tutto il db
		cursorLog = dbHelperLog.getAccounts();
		while (cursorLog.moveToNext()){
			//prelevo il dato dalla tabella
			String tablemail=cursorLog.getString(cursorLog.getColumnIndex(DbAdapter.KEY_MAIL));
			String tablepassword=cursorLog.getString(cursorLog.getColumnIndex(DbAdapter.KEY_PASSWORD));
			//se i parametri passati sono diversi ritorna true
			if(tablemail.equals(mail) && tablepassword.equals(password)){
				flag=false;//tutto ok ha trovato l'utente
				break;
			}else{
				flag=true;//il problema è che quando trova l'account deve uscire dal while dando come return false, 
			}
		}
		if(flag==false){
			return false;
		}return true;
	}
	
	/*public void alert(String message){
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}*/
	
	/*protected Dialog onCreateDialog(int d){
		Dialog dialog;
		switch(d){
		
		}
	}*/
	/*Login Asincrono, questa classe serve per evitare che un processo sovrasti completamete l'app bloccandola con tutti i suoi calcoli, quindi succede che
	 * attraverso questo Task Asincrono si creano thread facili da gestire in modo da avere un task principale che gira sempre e uno secondario 
	 * che va in background e che magari restituisce il risultato solo quando finisce avvisando il thread principale.
	 * Il metodo AsyncTask accetta un parametro di tipo Params(il primo generic) e ritorna un oggetto di tipo Result.
	 * Infatti il metodo è AsyncTask<Params, Progress, Result>
	 * Inoltre una cosa interessante di tale metodo è che  possiede al suo interno 3 metodi attraveso i quali si possono gestire le varie fasi e i 
	 * parametri..vediamo i metodi principali di AsyncTask:
	 * 1)onPreExecute: eseguito sul thread principale, contiene il codice di inizializzazione dell’interfaccia grafica (per esempio la disabilitazione di un button)
	 * 2)doInBackground: eseguito in un thread in background si occupa di eseguire il task vero e proprio. Accetta un parametro di tipo Params (il primo generic 
	 * 		definito) e ritorna un oggetto di tipo Result
	 * 3)onPostExecute: eseguito nel thread principale e si occupa di aggiornare l’interfaccia dopo l’esecuzione per mostrare i dati scaricati o calcolati nel 
	 * 		task che vengono passati come parametro*/
	public class LoginUtente extends AsyncTask <Void, Void, Boolean>{
		//Iniziamo col primo metodo
		@Override
		public Boolean doInBackground(Void... params){
			
			String email=mail;
			String pass=password;
			String textSource;
			/*textSource sarà un l'indirizzo dove andremo ad autenticarci infatti inseriamo proprio un indirizzo come se
			stiamo interrogando un DB, e gli mandiamo i dati in chiaro: http://server/fil.php?u=utente&p=password*/
			textSource= context.getString(R.string.php)+"?"+context.getString(R.string.uFlag)+"="+email+"&"+context.getString(R.string.pFlag)+"="+pass;
			//Per usare la classe URL devo importare la libreria java.net
			URL textUrl;
			//proviamo a far connettere al server la pagina
			try{
				textUrl= new URL(textSource);
				//apriamo un inputStream e dobbiamo importare la java.io
				BufferedReader bufferReader=new BufferedReader(new InputStreamReader(textUrl.openStream()));
				/*ora bisogna leggere dal bufferReader e memorizzarlo in una stringa per fare ciò ho bisogno di un ciclo while perchè fin quando non si svuota
				 * completamente il buffer devo continuare a leggerlo*/
				String stringbuffer, stringText = null;
				//ho bisogno di un altro string per mettere insieme tutte le scritte che mi arrivano altrimenti sovrascriverei sempre in stringbuffer
				while((stringbuffer=bufferReader.readLine()) != null){
					stringText+=stringbuffer;
				}
				//abbiamo terminato il riempimento dello stringText quindi possiamo chiudere lo stream
				bufferReader.close();
				System.err.print(stringText);
				/*dobbiamo fare in modo che tutta la pagina sia stata letta, quindi accade che la pagina php contiene R.string.success cioè una stringa definita!
				 * SE contiene tale stringa è tutto ok*/
				if (stringText.contains(context.getString(R.string.success))){
					return true;
				}
			}catch(MalformedURLException e){
				e.printStackTrace();
				
			}catch(IOException e){
				e.printStackTrace();
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(final Boolean success){
			TentAuth=null;
			//Se il task doInBackground ha dato come return true allora mostra il main
			if (success==false){
				/*subito dopo aver pigiato login entra qui e mostra il layout Verifica*/
				setContentView(R.layout.activity_verifica);
				//qui ci vorrà la modifica per far aprire direttamente la classe verifica e fare tutto li dentro
				Button bottone=(Button) findViewById(R.id.Abilita);
				//qui attende che accade l'evento, che sarebbe, spingi il pulsante abilita i tasti
				bottone.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						startActivity_verifica();						
					}
				});
				//finish();
			}else{
				passwordvista.setError(getString(R.string.errPassword));
				passwordvista.requestFocus();
			}
		}
		
		private void startActivity_verifica(){
			Intent intent= new Intent(getBaseContext(), com.example.tsa.Verifica.class);
			startActivity(intent);
		}
		
		@Override
		protected void onCancelled(){
			TentAuth=null;
	}
	}
	
}		