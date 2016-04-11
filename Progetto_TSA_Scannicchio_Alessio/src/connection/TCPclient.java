package connection;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

public class TCPclient implements Runnable {


	private Handler mHandler;
	private Message msg;
	private String str;


	public TCPclient(Handler h) {
		this.mHandler = h;
	}


	public void run() {
		try {

			InetAddress serverAddr = InetAddress.getByName("10.0.2.2");//TCPServer.SERVERIP

			str = "Connecting...\n \n";
			msg = mHandler.obtainMessage(0, str);
			mHandler.sendMessage(msg);


			Socket socket = new Socket(serverAddr, 5000);
			String message = "Hello from Client Android";
			try {
				
				str = "Sending: '" + message + "'\n";
				msg = mHandler.obtainMessage(0, str);
				mHandler.sendMessage(msg);
				
				PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);

				out.println(message);
				
				str = "Sent.\n";
				msg = mHandler.obtainMessage(0, str);
				mHandler.sendMessage(msg);
				
				str = "Done.\n \n";
				msg = mHandler.obtainMessage(0, str);
				mHandler.sendMessage(msg);
				
				
				

			} catch(Exception e) {
				str = "Error: " + e.toString();
				msg = mHandler.obtainMessage(0, str);
				mHandler.sendMessage(msg);
			} finally {
				socket.close();
			}
		} catch (Exception e) {
			str = "Error: " + e.toString();
			msg = mHandler.obtainMessage(0, str);
			mHandler.sendMessage(msg);
		}
	}
}
