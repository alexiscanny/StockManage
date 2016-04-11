package com.example.tsa; 

import java.io.BufferedReader;  
import java.io.InputStream; 
import java.io.InputStreamReader; 
import java.util.ArrayList;

import org.apache.http.HttpEntity; 
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair; 
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity; 
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity; 
import android.net.ParseException;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*Classe creata per visualizzare il database dei titoli azionari presente su una macchina posta in remoto, 
 * attraverso le query già inserite (possono essere modificate e aggiunte in seguito) potremo ricevere i dati 
 * presenti nel database*/
public class AndroidPhp extends Activity { 
	
	JSONArray jArray=null;
	String result = ""; 
	InputStream is = null; 
	StringBuilder sb=null; 
	TextView text;
	
	@Override
		public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		//devo fare il layout
		setContentView(R.layout.android_php); 
		
		final TextView textviewDariRicevuti = (TextView) findViewById(R.id.datiRicevuti);
		
		Button buttonInviaDati = (Button) findViewById(R.id.buttonInviaDati);
		buttonInviaDati.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//invio richiesta
				textviewDariRicevuti.setText(inviaDati());
			}
		});
	}
	
	public String inviaDati(){
		//digitando il pulsante "invia Dati" sto inviando il nome dell'azione e il suo valore
		String result="";
		String stringaFinale="";
		String stringaIntermedia="";
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		//Sotto scrivo come si chiama il campo che invio(azione1) e il suo valore (1)
		nameValuePairs.add(new BasicNameValuePair("Stock_name","azione1"));
		//mi serve per far attendere l'app per più di 5 secondi
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
		
		try { 
		
			//Per impostare parametri all'HttpPostRequest è possibile utilizzare BasicNameValuePair:
			//questa parte fara la chiamata http post all'url
			HttpClient httpclient = new DefaultHttpClient(); 
			HttpPost httppost = new HttpPost("http://10.0.2.2/localhost/interroga2.php"); 
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			//qui si ferma perchè non riceve nulla
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			result = EntityUtils.toString(entity, HTTP.UTF_8);
		}
		catch(Exception e){ 
			Log.e("TEST", "Errore nella connessione http:  "+e.toString());
		} 
			
		//questa parte effettuerà il convert response to string 

			try{ 
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8); 
				StringBuilder sb = new StringBuilder();
				String line=null; 
 				while ((line = reader.readLine()) != null) { 
					sb.append(line + "\n");
				} 
				is.close();
				//Converto la risposta del server situata nello stream builder all'interno della var result
				result=sb.toString(); 
			}
			catch(Exception e){ 
				Log.e("TEST", "Errore nella conversione del risultato : "+e.toString());
			}
			
			//questa parte effetuerà il parsing dati arrivati in formato JSON da string a array 
 			if(result!=null){
				try{ 
					//la pagina php deve solo far visualizzare la json stringa per poi inserirla nel jArray
 					jArray = new JSONArray(result); 
					JSONObject json_data=null;
					for(int i=0;i<jArray.length();i++){
						json_data = jArray.getJSONObject(i); 
 						Log.i("TEST","id: "+json_data.getInt("Stock_id")+
								", name: "+json_data.getString("Stock_name")+
								", cliente: "+json_data.getString("Stock_Client_id")+
								", industria: "+json_data.getString("Stock_Industry_id")
						);
						stringaFinale = "Stock id: "+json_data.getInt("Stock_id")+ "\nStock name: "+ json_data.getString("Stock_name")+
								"\n Id Cliente: "+ json_data.getString("Stock_Client_id")+
								"\n Id Industria Quotata: "+ json_data.getString("Stock_Industry_id")+"\n\n";
						stringaIntermedia=stringaIntermedia.concat(stringaFinale);
						
					}
				}
				catch(JSONException e1){
					Toast.makeText(getBaseContext(), "Titolo Azionario non trovato" ,Toast.LENGTH_LONG).show(); 
				}
				catch (ParseException e1) {
					e1.printStackTrace();
				}
			}	
		
		else { //is è null e non ho avuto risposta
		}
		return stringaIntermedia;
		}
}