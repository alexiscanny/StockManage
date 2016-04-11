package connection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;


public class TCPServer implements Runnable{

/*per vedere il funzionamento bisogna 
 * 1)avviare l'emulatore del server su 5554 e avviare l'emulatore client su 5556
 * 2)poi far runnare il server
 * 3)digitare su terminale telnet localhost 5554-->uscirˆ la scritta ok
 * 4)redir add tcp:5000:5000 (questa  la porta attraverso la quale comunicano)-->uscirˆ la scritta ok
 * 5)runnare il client*/
	private static final int SERVERPORT = 5000;

	private Handler mHandler;
	private Message msg;
	private String str;



	public TCPServer(Handler h) {
		this.mHandler = h;
	}




	public void run() {
		try {

			str = "Connecting...\n \n";
			msg = mHandler.obtainMessage(0, str);
			mHandler.sendMessage(msg);


			ServerSocket serverSocket = new ServerSocket(SERVERPORT);


			while (true) {

				Socket client = serverSocket.accept();

				str = "Receiving...\n";
				msg = mHandler.obtainMessage(0, str);
				mHandler.sendMessage(msg);

				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
					String txt = in.readLine();
					str = "Received:'" + txt + "'\n";
					msg = mHandler.obtainMessage(0, str);
					mHandler.sendMessage(msg);

				} catch(Exception e) {
					str = "Error: "+e.toString()+"\n \n";
					msg = mHandler.obtainMessage(0, str);
					mHandler.sendMessage(msg);
				} finally {
					str = "Done.\n \n";
					msg = mHandler.obtainMessage(0, str);
					mHandler.sendMessage(msg);
					client.close();
				}

			}

		} catch (Exception e) {
			str = "Error: "+e.toString()+"\n \n";
			msg = mHandler.obtainMessage(0, str);
			mHandler.sendMessage(msg);
		}
	}
}
