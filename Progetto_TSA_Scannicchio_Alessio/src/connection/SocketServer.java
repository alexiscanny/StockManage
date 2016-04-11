package connection;

import android.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;



public class SocketServer extends Activity {
	
	
	private EditText editText;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_item);
		
		editText = (EditText)findViewById(R.id.edit);

		TCPServer ts = new TCPServer(handler);
		
		Thread sThread = new Thread(ts);
				
		sThread.start();
	}
	
	
	/** Nested class that receive string message */
	 final Handler handler = new Handler() {
	        public void handleMessage(Message msg) {
	           String txt = (String)msg.obj;
	           editText.append(txt);
	            
	        }
	    };

	
	
}