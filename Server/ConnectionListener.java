import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionListener extends Thread
{
	private int port;
	protected List _listeners = new ArrayList();

	public ConnectionListener(int port)
	{
		this.port = port;
	}

	public void run()
	{
		while (true)
	    {
			//Open port for listening to requests for a private connection
			try
			{
				listen();
			}
			catch(Exception e)
			{

			}
	    }
	}

	private void listen() throws IOException
	{
		ServerSocket listenerServerSocket = new ServerSocket(port);
		Socket listenerSocket = listenerServerSocket.accept();
		
		System.out.println("Connected");

		recievedRequestForPrivateConnection();

		listenerServerSocket.close();

		System.out.println("Disconnected");

		/*
		BufferedInputStream inStream = new BufferedInputStream(listenerSocket.getInputStream());

		while(true)
		{
	    	//if(inStream.available() > 0)
	        System.out.println(inStream.read());
		}
		*/
	}

	public synchronized void addEventListener(ConnectionListenerEventListener listener) 
    {
        _listeners.add(listener);
    }

    public synchronized void removeEventListener(ConnectionListenerEventListener listener)  
    {
       _listeners.remove(listener);
    }

    protected synchronized void recievedRequestForPrivateConnection()
    {
        Iterator i = _listeners.iterator();

        while(i.hasNext())
        {
            ((ConnectionListenerEventListener) i.next()).recievedRequestForPrivateConnection();
        }
    }
}