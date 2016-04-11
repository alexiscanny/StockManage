package com.example.tsa.data;
/*la classe adapter per semplificare le interazioni con il database ed introdurre così un livello di astrazione che fornisca metodi 
 * intuitivi, flessibili e robusti per inserire, eliminare e modificare i record del database.
 * la classe adapter dovrebbe fornire query ed esporre metodi per la creazione del database e la gestione delle connessioni (apertura e chiusura) ad esso.*/

/*La classe DbAdapter fornisce l’interfaccia diretta per manipolare il database. L’adapter è, nella pratica, un livello di astrazione: fornisce tutta una serie
 *  di metodi che, una volta testati opportunamente, permettono al programmatore di concentrare le proprio energie sugli aspetti importanti dell’applicativo 
 *  e non sulla creazione o sulla modifica di un record del database.*/

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;
 
public class DbAdapter {
  @SuppressWarnings("unused")
  //private static final String LOG_TAG = DbAdapter.class.getSimpleName();
         
  private Context context;
  private SQLiteDatabase database;
  private DatabaseHelper dbHelper;
 
  // Database fields i campi della tabella

  public static final String KEY_NAME		= TabellaAccount.TABELLA_NOME;
  public static final String KEY_CONTACTID  = TabellaAccount.ID_COLONNA;
  public static final String KEY_MAIL       = TabellaAccount.TABELLA_MAIL;
  public static final String KEY_PASSWORD   = TabellaAccount.TABELLA_PASSWORD;
  public static final String[] COLUMNS		= TabellaAccount.COLUMNS;
 
  /*la definizione del costruttore: l’unico elemento che sicuramente dobbiamo configurare all’interno del costruttore è il context, 
   * che passeremo di volta in volta quando dovremo istanziare un nuovo adapter per eseguire delle query.  In ambiente Android il context è un elemento 
   * fondamentale che utilizzeremo spesso nelle nostre applicazioni: è una classe astratta di cui l’implementazione è fornita dal sistema. 
   * Il context permette di accedere alle risorse specifiche dell’applicazione, come ad esempio le activity, gli intent, ecc.*/
  public DbAdapter(Context context) {
    this.context = context;
  }
 /*i metodi open() e close() sono metodi che useremo ogni volta che dovremmo comunicare con il database: sarà sufficiente 
  * chiamare questi metodi per lavorare con il database, nascondendo tutto quanto abbiamo visto finora.*/
  /*Nel metodo open() istanziamo un oggetto di tipo DatabaseHelper che fornisce l’interfaccia di creazione/aggiornamento/gestione del database. 
   * A questo punto non ci rimane che richiamare il metodo getWritetableDatabase() definito in SQLiteOpenHelper,
   *  il quale restituisce un oggetto database in modalità lettura/scrittura attivo fino a quando non viene richiamato 
   * il metodo close(). La prima volta che viene richiamato getWritableDatabase() il database viene aperto e vengono automaticamente richiamati i metodi
   *  onCreate(), onUpgrade() e se necessario anche il metodo onOpen().*/
  public DbAdapter open() throws SQLException {
    dbHelper = new DatabaseHelper(context);
    database = dbHelper.getWritableDatabase();
    return this;
  }
 /*Ogni volta che utilizziamo il database è buona norma chiudere le comunicazioni subito dopo aver finito di lavorare con la
  *  base di dati: questa viene messa nella cache, per cui non è un problema chiudere ed eventualmente riaprire il database ogni qualvolta sia necessario.
  *   Nello sviluppo mobile questo atteggiamento è importante: le risorse dei dispositivi non sono molte e bisogna far molta attenzione a come le utilizziamo.*/
  public void close() {
    dbHelper.close();
  }
 
  /*Questo metodo [createContentValues()] ha un compito molto semplice: memorizzare un insieme di valori che il ContentResolver può processare per fornire 
   * l’accesso applicativo al modello del contenuto.
   *Quando abbiamo la necessità di accedere ai dati di un Content provider utilizziamo l’oggetto ContentResolver nel context della nostra applicazione 
   *per comunicare con il provider. Il ContentResolver comunica con un’istanza di una classe che implementa ContentProvider: questo oggetto riceve richieste
   *dai client, gestisce la richiesta e restituisce i risultati.
   *I Content provider gestiscono l’accesso ad un insieme di dati strutturati: essi sono interfacce standard che connettono dati di un processo 
   *con il codice che sta “girando” in un altro. Sono componenti di estrema importanza che possiamo utilizzare quando abbiamo bisogno di un insieme
   *di dati composto da informazioni dei nostri contatti personali, ma anche video, immagini, audio.*/
  
  private ContentValues createContentValues(String mail, String password) {
    ContentValues values = new ContentValues();
    values.put( KEY_MAIL, toString() );
    values.put( KEY_PASSWORD, toString() );
   return values;
  }  
  
  /*Funzione statica usata per inserire un account nel DB*/
  public static void inserisciAccount(SQLiteDatabase db, String mail, String password){
	  ContentValues v =new ContentValues();
	  v.put(KEY_MAIL, mail);
	  v.put(KEY_PASSWORD, password);
	  db.insert(KEY_NAME, null, v);
  }
  
  /*Ritorna un cursore che punta a tutti gli account inseriti*/
  public static Cursor ottieniAccount(SQLiteDatabase db){
	  return db.query(KEY_NAME, COLUMNS, null, null, null, null, null);
  }
  /*Cancella l'account che ha l'id passato come parametro*/
  public static boolean cancellaAccount(SQLiteDatabase db, long id){
	  return db.delete(KEY_NAME, KEY_CONTACTID+"="+id, null)>0; //al return si mette >0 perchè se restituisce -1 significa che non è andato a buon fine
  }
  /*Ritorna un cursore che punta all'account che ha l'id passato come parametro*/
  public static Cursor ottieniAccount(SQLiteDatabase db, long id) throws SQLException{
	  Cursor c= db.query(true, KEY_NAME, COLUMNS, KEY_CONTACTID+"="+id, null, null, null, null, null);
	  if(c != null){
		  c.moveToFirst();
	  }
	  return c;
  }
  
  /*Modifica i valori degli account*/
  public static boolean aggiornaAccount(SQLiteDatabase db, long id, String mail, String password){
	  ContentValues v=new ContentValues();
	  v.put(KEY_MAIL, mail);
	  v.put(KEY_PASSWORD, password);
	  return db.update(KEY_NAME, v, KEY_CONTACTID+"="+id, null)>0;
  }
   
  /*Da qui in poi vedremo i metodi che forniscono l’interfaccia diretta per la manipolazione della base di dati. 
   * Queste funzioni ci semplificano notevolemente la vita in fase di programmazione: quando avremo la necessità di creare, modificare o eliminare 
   * un account nella nostra applicazione, l’unica preoccupazione sarà scegliere il metodo più appropriato per soddisfare la nostra richiesta.

*/
  //create a contact----il metodo insertOrThrow restituisce l'id
  public long createContact(String mail, String password) {
    ContentValues initialValues = createContentValues(mail, password);
    return database.insertOrThrow(KEY_NAME, null, initialValues);//null=indica come comportarsi nel caso in cui i valori iniziali siano vuoti
  }
 
  //update a contact-----il parametro contactID è l'identificatore da aggiornare
  public boolean updateContact( long contactID, String mail, String password) {
    ContentValues updateValues = createContentValues(mail, password);
    return database.update(KEY_NAME, updateValues, KEY_CONTACTID + "=" + contactID, null) > 0;
    /*il metodo update richiede 3 parametri: nome tabella, oggetto ContentValues che contiene i valori del contatto da aggiornare e la clausola WHERE*/
  }
                 
  //delete a contact------RESTITUISCE il numero di righe cancellate o 0 se non sono stati trovati valori
  public boolean deleteContact(long contactID) {
    return database.delete(KEY_NAME, KEY_CONTACTID + "=" + contactID, null) > 0;
  }
 
  //fetch all contacts----metodo che permette di recuperare tutti i contatti presenti nel nostro database
  /*Questa funzione, in base ai parametri passati, genera le query necessarie per interrogare il database e recuperare i dati che ci interessano. 
   * Il numero dei parametri di configurazioni varia in base a quale funzione query() abbiamo la necessità di utilizzare: la piattaforma Android infatti 
   * mette a disposizione 3 funzioni query(), che accettano rispettivamente 7, 8 e 9 parametri, in base appunto al tipo di query di cui abbiamo bisogno.

Nel nostro esempio abbiamo utilizzato la funzione query() che richiede 7 parametri (le altre due disponibili permettono rispettivamente di impostare anche 
un limite e un limite più un flag per l’attivazione del DISTINCT SQL):

1)il nome della tabella in cui deve essere eseguita la query;
2)la lista delle colonne da restituire;
3)un filtro per stabilire quali righe restituire (corrisponde alla clausola SQL WHERE);
4)un array di stringhe per inserire dinamicamente alcuni valori nella SELECT;
5)un filtro che corrisponde alla clausola SQL GROUP BY;
6)un filtro che corrisponde alla clausola HAVING;
7)un filtro che corrisponde alla clausola ORDER BY.
La funzione query() restituisce un oggetto di tipo Cursor: questo oggetto fornisce l’accesso in modalità di lettura-scrittura al result set restituito 
dalla query. Sarà sufficiente ciclare sull’oggetto Cursor per avere accesso ai dati ottenuti.
*/
  public Cursor fetchAllContacts() {
    return database.query(KEY_NAME, new String[] { KEY_CONTACTID, KEY_MAIL, KEY_PASSWORD}, null, null, null, null, null);
  }
   
  //fetch contacts filter by a string
  /*Anche questa funzione, come la precedente, accetta un parametro, ma in questo caso è una stringa che verrà utilizzata come filtro. 
   * Infatti, sempre servendoci nella clausola WHERE, utilizziamo il parametro per generare una stringa di ricerca da applicare al nome del contatto, 
   * in modo che il result set sia composto da tutti i contatti che hanno un nome contenente la stringa filter passata come parametro. */
  public Cursor fetchContactsByFilter(String filter) {
    Cursor mCursor = database.query(true, KEY_NAME, new String[] {
                                    KEY_CONTACTID, KEY_MAIL, KEY_PASSWORD},
                                    KEY_MAIL + " like '%"+ filter + "%'", null, null, null, null, null);
         
    return mCursor;
  }
}
