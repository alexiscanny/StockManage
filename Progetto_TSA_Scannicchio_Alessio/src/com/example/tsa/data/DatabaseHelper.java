package com.example.tsa.data;
/*Scriviamo quindi la classe che si occuperà di creare il nostro database, 
la classe helper è anche il luogo ideale in cui memorizzare le costanti statiche come il nome della 
tabella e delle colonne. Estendiamo la classe SQLiteOpenHelper(che offre già il metodo onCreate() e 
onUpgrade() e scriviamo il costruttore:*/

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String NAME_DB="Database_Alessio";
	private static final int VERSIONE_DB=1;
	
	/*La classe SQLiteOpenHelper si occupa di gestire il database, in particolare crea il db la prima volta che viene usato, 
	 * permette di gestire gli aggiornamenti e gestisce le connessioni. 
	 * Ora nel costruttore che vediamo qui di seguito gli passiamo il nome del database e non il nome della tabella e la versione attuale 
	 * (passiamo 1 in quanto è la nostra prima versione).*/
	
	public DatabaseHelper(Context context) {
		
		super(context, NAME_DB, null, VERSIONE_DB);
	}
	
/*Per gestire la creazione del database riscriviamo il metodo onCreate(il quale serve proprio alla creazione del database appena avviamo l'app) 
 * il parametro passato a questo metodo è il database appena creato 
 * (possiamo notare che non abbiamo dovuto fare niente per creare il database, ha fatto tutto il framework di Android per noi!). Scriviamo il codice 
 * di creazione della tabella  (i nomi delle colonne sono prese dall’interfaccia ) e di inserimento dei dati:*/
	/*private static final String CREA_TAB_ACCOUNTS = "CREATE TABLE"+NAME_DB+"("
			+TabellaAccount.ID_COLONNA+"integer primary key autoincrement, "
			+ TabellaAccount.TABELLA_MAIL+" TEXT NOT NULL, "
			+ TabellaAccount.TABELLA_PASSWORD+" TEXT NOT NULL,"+");";*/
	
	@Override
	public void onCreate(SQLiteDatabase db){
		//ora creamo una tabella 
		String sql="";
		sql += "CREATE TABLE accounts (";
		sql += " _id INTEGER PRIMARY KEY,";
		sql += " mail TEXT NOT NULL, ";
		sql += " password TEXT NOT NULL";
		sql += ")";
		db.execSQL(sql);
		//cursor = getAccounts();
		//db.execSQL(CREA_TAB_ACCOUNTS);
		/*Potevo anche scrivere direttamente lo statement SQL:
		*/
	//	inserisciAccount(db, "alessio@sanbros.it", "alessio");
		//inserisciAccount(db, "gigi@yahoo.it", "gigi");
		
	
	}
	
	 public static boolean cancellaAccount(SQLiteDatabase db, long id){
		//al return si mette >0 perchè se restituisce -1 significa che non è andato a buon fine
		 return db.delete(TabellaAccount.TABELLA_NOME, TabellaAccount.ID_COLONNA+"="+id, null)>0; 
		 
	  }
	
	
	
	/*Il metodo inserisciAccount esegue una insert sulla tabella degli accounts, invece di scrivere codice sql utilizziamo il metodo insert 
	 * dell’oggetto SQLiteDatabase. Questo metodo accetta tre parametri: la tabella in cui inserire la riga, una stringa per poter inserire righe 
	 * vuote (di solito valorizzato a null) e i valori da inserire. Per passare i valori è necessario usare un oggetto ContentValues che è 
	 * una mappa che associa al nome di una colonna il rispettivo valore.*/
	//gli ho implementato un return long per fare la verifica sull'inserimento=>se l'id cresce nel debug vuol dire che inserisce la riga
	public static long inserisciAccount(SQLiteDatabase db, String mail, String password)
	{
		ContentValues v = new ContentValues();
		v.put(TabellaAccount.TABELLA_MAIL, mail);
		v.put(TabellaAccount.TABELLA_PASSWORD, password);
		long id=db.insert(TabellaAccount.TABELLA_NOME, null, v);
		return id;
	
	}
	
	/*Vediamo adesso come effettuare una query per leggere i dati dalla tabella che abbiamo creato e popolato. Per avere una connessione al database
	 *  è necessario chiamare getReadableDatabase() o getWritableDatabase() (a seconda se vogliamo un db in sola lettura o in lettura e scrittura) 
	 *  dentro la nostra classe che estende SQLiteOpenHelper. 
	 *  Sull’oggetto ritornato chiamiamo il metodo query passando i parametri:
	 *  1 nome della tabella
	 *  2 array dei nomi delle colonne da ritornare
	 *  3 filtro da applicare ai dati
	 *  4 argomenti su cui filtrare i dati (nel caso in cui nel filtro siano presenti parametri)
	 *  5 group by da eseguire
	 *  6 clausola having da usare
	 *  7 ordinamento da applicare ai dati
	 *  
	 *  Il metodo query ritorna un oggetto Cursor, questa classe è fondamentale per accedere ai dati di un database: permette di scorrere i dati ritornati 
	 *  da una query e di ottenere i valori delle varie colonne.
	 *  Il nostro metodo che ritorna i nomi degli account con il nome più lungo di 10 caratteri :*/
	
	public Cursor getAccounts()
	{
		return (getReadableDatabase().query(
			TabellaAccount.TABELLA_NOME, 
			TabellaAccount.COLUMNS, 
			null/*qui siccome è un where posso anche pensare di mettere una condizione->"length(" + TabellaAccount.TABELLA_NOME + ") > 10"*/, 
			null,
			null, 
			null, 
			null));
	}
	
	/*Serve per apportare modifiche al database nel caso in cui l'app è stata precedentemente aperta e il database è stato già creato, quindi verifica il numero di 
	 * versione del vecchio database il quale se è minore di quello attuale bisogna fare delle modifiche(ad ogni modifica incrementa la version)*/
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		//non previsto nulla per il momento
	}
}
