package com.example.tsa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.*; 
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class InserimentoIp extends Activity {
	EditText edit;
	TextView text;
	
	protected void onCreate (Bundle savedIstanceState){
		super.onCreate(savedIstanceState);
		setContentView(R.layout.inserimento_ip);
		/*queste due righe con StrictMode le ho inserite perch altrimenti il SO android bloccava l'app in quanto il ping 
		 * viene fatto nel thread principale e per effettuare il ping ci si impiega pi di 5 secondi, il SO blocca l'app appunto
		 * perch assegna per ogni evento max 5secondi proprio in virt del fatto che uno smartphone deve essere necessariamente 
		 * veloce e non bloccarsi mai.
		 * Questi Strict erano per le API 9 e non per 8, per farli funzionare ho dovuto ignorare il problema
		 * Infatti come eccezione mi dava:
		 * ---> .StrictMode$AndroidBlockGuardPolicy.onNetwork(StrictMode.java:1117)<---
		 * Questa eccezione  l“ per un motivo. Le Attivitˆ di rete possono richiedere tempo, eseguendo 
		 * networking sul thread principale, che  lo stesso thread responsabile per l'aggiornamento dell'interfaccia utente, 
		 * significherebbe congelare il thread fino a quando la rete ha terminato la sua esecuzione (in questo caso accade 
		 * per pochi secondi, ma avviene in ogni thread, ma quando  eseguita su un apposito thread,tutto funziona correttamente).
		 *  In Android, se l'interfaccia utente-thread non  attiva per 5 secondi, verrˆ mostrata la finestra di dialogo 
		 *  l'applicazione non risponde, vuoi per chiuderla?*/
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);

		edit = (EditText)findViewById(R.id.ind_pc_remot);
		//edit.setText("127.0.0.1");
		edit.setText("localhost");//anche usando localhost lui passa direttamente ad indirizzo
		//questo text  lo spazio che serve a ricevere la risposta dal server
		text = (TextView)findViewById(R.id.response_pc_remot);
		Button button = (Button)findViewById(R.id.ping_button);
		//ora attende che venga pigiato il tasto ping
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			// TODO Auto-generated method stub
				Editable host = edit.getText();
				InetAddress addr = null;
				try {
						addr = InetAddress.getByName(host.toString());
					} catch (UnknownHostException e) {
						e.printStackTrace();			
					}
				try {
						if(addr.isReachable(5000)) {
							text.append("\n" + host + " - Respond OK");
						} else {
							text.append("\n" + host);
						}
				} catch (IOException e) {			
					text.append("\n" + e.toString());
				}
				try {
					String pingCmd = "ping -c 5 " + host;
					String pingResult = "";
					Runtime r = Runtime.getRuntime();
					Process p = r.exec(pingCmd);
					BufferedReader in = new BufferedReader(new
					InputStreamReader(p.getInputStream()));
					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						System.out.println(inputLine);
						text.setText(inputLine + "\n\n");
						pingResult += inputLine;
						text.setText(pingResult);
					}
					in.close();
				}//try
				catch (IOException e) {
				System.out.println(e);
				}
			}
		});
	}
}