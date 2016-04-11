package com.example.tsa.data;

import android.provider.BaseColumns;

//creo la tabella del database
/*Questa interfaccia ci serve per non dover stare a scrivere in più punti i nomi delle colonne (rischiando errori di digitazione) e per 
 * organizzare al meglio il nostro codice (per sapere come è fatta la tabella basta guardare la relativa interfaccia). BaseColumns è anch’essa 
 * una interfaccia Java definita nel core di Android e contiene il campo _ID, in molte occasioni Android si aspetta di avere una colonna _ID e quindi 
 * è conveniente includere sempre questa colonna nelle nostre tabelle.
 * */
public interface TabellaAccount extends BaseColumns {
	
	public String TABELLA_NOME="accounts";
	public String TABELLA_MAIL = "mail";
	public String TABELLA_PASSWORD="password";
	public String ID_COLONNA="_id"; //attenzione perchè Android non riconosce il campo id senza l'underscore avanti!!!Incredibile!!!!!
	public String[] COLUMNS = new String[] { ID_COLONNA, TABELLA_MAIL, TABELLA_PASSWORD };

}
