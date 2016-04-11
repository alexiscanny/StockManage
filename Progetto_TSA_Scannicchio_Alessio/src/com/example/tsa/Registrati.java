package com.example.tsa;

import com.example.tsa.data.DatabaseHelper;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tsa.data.DatabaseHelper; //in questo modo mi ha caricato direttamente il database che vede nel progetto
import com.example.tsa.data.DbAdapter;
import com.example.tsa.data.TabellaAccount;
import com.example.tsa.MainActivity;


public class Registrati extends Activity{
	
	public String pwd1;
	public DatabaseHelper dbHelper;
	public Cursor cursor;
	public String email, pwd;
	
	@Override
	public void onCreate(Bundle savedIstanceState){
		super.onCreate(savedIstanceState);
		setContentView(R.layout.registrati);
		dbHelper= new DatabaseHelper(this);
		final Button button=(Button) findViewById(R.id.buttonRegistrati);
		button.setOnClickListener(new View.OnClickListener(){
			
			public void onClick(View v){
				/*inserisce nel database i nomi che inserisco infatti se visualizzo la lista degli utenti 
				 * li vedo tutti però mi prende solo la password e non la mail*/
	
				        final EditText mail = (EditText)findViewById(R.id.editEmailRegistrazione);
			            final EditText password= (EditText)findViewById(R.id.editPassword);
			          //Per il futuro posso pensare di effettuare il match delle password 
			          
			          //creiamo un oggetto Bundle che utilizziamo per salvare i dati inseriti dall’utente:
			            Bundle bundle = new Bundle();
			          //Utilizziamo il metodo putString() dell’oggetto bundle per salvare i dati inseriti, 
			    		//recuperati poi con il metodo getText() del widget EditText  
			            bundle.putString(email, mail.getText().toString());
			            String newemail=bundle.getString(email);
			            
			            bundle.putString(pwd, password.getText().toString());
			            String newpwd=bundle.getString(pwd);
			            
			            SQLiteDatabase db= dbHelper.getWritableDatabase();
						long id =dbHelper.inserisciAccount( db, newemail, newpwd);
						alert("Utente inserito");
						
						//faccio partire il metodo login in modo da far procedere all'autenticazione il cliente
						finish();
						
			}
		});
	}
	public void alert(String message){
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

}				
	
	