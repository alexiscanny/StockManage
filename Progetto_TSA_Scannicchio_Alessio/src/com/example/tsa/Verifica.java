package com.example.tsa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.content.Intent;

//questa classe serve per effettuare le verifiche di connessione, infatti deve andare di pari passo con la crazione(giˆ fatta della pagina in xml)
public class Verifica extends Activity {

	protected void onCreate(Bundle savedIstanceState){
		super.onCreate(savedIstanceState);
		setContentView(R.layout.activity_verifica);
		
		//quando si preme il pulsante Inserisci ip aprirˆ la pagina e l'activity dell'inserimento dell ip
		Button bottone = (Button) findViewById(R.id.inserisci_ip_button);
		bottone.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			startInserimentoIp();
			}
		});
		
		Button bottone_php=(Button) findViewById(R.id.riceviphpbutton);
		bottone_php.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startRiceviphp();
				
			}
		});
		Button bottone_stockwatch=(Button) findViewById(R.id.stockwatch_button);
		bottone_stockwatch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startStockwatch();
				
			}
		});
	
	}
	
	private void startInserimentoIp(){
		Intent in=new Intent(getBaseContext(), com.example.tsa.InserimentoIp.class);
		startActivity(in);
	}
	
	
	
	protected void startRiceviphp() {
		Intent intent=new Intent(this, AndroidPhp.class);
		startActivity(intent);
	}

	private void startStockwatch(){
		Intent intent =new Intent(this, Stockwatch.class);
		startActivity(intent);
	}

}
