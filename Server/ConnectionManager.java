import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.EventQueue;

//Manages the connection between clients
public class ConnectionManager implements ConnectionListenerEventListener
{
	ConnectionListener connectionListener = new ConnectionListener(8080);

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConnectionManager manager = new ConnectionManager();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ConnectionManager()
	{
		connectionListener.addEventListener(this);

		try
		{
			//Start connection listener
			connectionListener.start();
		}
		catch(Exception e)
		{

		}
	}

	public void recievedRequestForPrivateConnection()
	{

	}
}