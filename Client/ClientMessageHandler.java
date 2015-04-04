import java.io.*;
import java.net.*;

public class ClientMessageHandler extends Thread
{
	private Socket connectedSocket;
	//private OutputStream socketWriter;
	private BufferedReader socketReader;
	private PrintWriter socketWriter;

	private String sessionID;

	public ClientMessageHandler(String ip, int port)
	{
		try
		{
			connectedSocket = new Socket(ip, port);
			socketWriter = new PrintWriter(connectedSocket.getOutputStream(), true);
			socketReader = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void run()
	{
		try
		{
			initializeConnection();

			socketReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();				
		}
	}

	private void initializeConnection() throws Exception
	{
		String request;

		//Wait for and read session ID
		sessionID = readMessage();
		
		System.out.println("Session ID:  " + sessionID);

		//Send acknowledgment
		sendMessage("Ack");

		//Wait for good to go message
		request = readMessage();
		System.out.println("Request:  " + request);

		socketReader.close();
		socketWriter.close();
	}

	private String readMessage() throws IOException
	{
		StringBuilder readStringBuilder = new StringBuilder();
		String readString;

		while(!socketReader.ready());

		while((readString = socketReader.readLine()) != null)
		{
	    	readStringBuilder.append(readString);

	    	if(!socketReader.ready())
	    		break;
		}

		return readStringBuilder.toString();
	}

	private void sendMessage(String message) throws IOException
	{
		socketWriter.println(message);
	}
}





