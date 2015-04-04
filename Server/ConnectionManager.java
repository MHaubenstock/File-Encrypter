import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.EventQueue;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.security.SecureRandom;
import java.math.BigInteger;

//Manages the connection between clients
public class ConnectionManager
{
	//For event listener
	protected List _listeners = new ArrayList();

    ExecutorService executorService = Executors.newFixedThreadPool(100);
    
    Map<String, Socket> clientDictionary = new HashMap<String, Socket>();

	public static void main(String[] args)
	{
		ConnectionManager manager = new ConnectionManager();
		manager.listenForConnections();
	}

	public ConnectionManager()
	{
		
	}

	public void listenForConnections()
	{
		ServerSocket serverSocket = null;
		Socket socket;

		Boolean acceptMoreConnections = true;
    	String sessionID;
    	SecureRandom sessionIDGenerator = new SecureRandom();

    	BufferedReader socketReader;
    	PrintWriter socketWriter;
    	String request;

		try
        {
        	//Open socket 8080 for listening to incoming connections
            serverSocket = new ServerSocket(8080, 100);

            while(acceptMoreConnections)
            {
            	//Listen for a new connection
                socket = serverSocket.accept();
                System.out.println("Received connection");

                //Get the new connection's reader and writer streams
                socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                socketWriter = new PrintWriter(socket.getOutputStream(), true);

                //Create and send session id
                sessionID = new BigInteger(130, sessionIDGenerator).toString(32);;
        		sendMessage(sessionID, socketWriter);
        		System.out.println("Sent session ID: " + sessionID);

        		//Send opened connection event with name of connection (session ID for now)
        		openedConnection(sessionID);

        		//Wait for acknowledgment
        		System.out.println("Waiting for acknowledgment");
        		request = readMessage(socketReader);

				if(request.equals("Ack"))
					System.out.println("Session ID Acknowledged");

                //Save client in the dictonary
				clientDictionary.put(sessionID, socket);

				//Tell client it's "good to go"
				sendMessage("You're good to go!", socketWriter);
        		System.out.println("Told client they are good to go");

        		//Close the socket reader and writer
        		socketReader.close();
        		socketWriter.close();

                //Execute the Data Server service
                //executorService.submit(new DataServer(socket));
            }
        }
        catch (IOException exp)
        {
            exp.printStackTrace();
        }
        finally
        {
            try
            {
                serverSocket.close();
            }
            catch (Exception e)
            {
            }
            
            executorService.shutdownNow();
        }
	}

	private String readMessage(BufferedReader socketReader) throws IOException
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

	private void sendMessage(String message, PrintWriter socketWriter) throws IOException
	{
		socketWriter.println(message);
	}

	public synchronized void addEventListener(ConnectionManagerEventListener listener) 
    {
        _listeners.add(listener);
    }

    public synchronized void removeEventListener(ConnectionManagerEventListener listener)  
    {
       _listeners.remove(listener);
    }

    protected synchronized void openedConnection(String connectionName)
    {
    	Iterator i = _listeners.iterator();

        while(i.hasNext())
        {
            ((ConnectionManagerEventListener) i.next()).openedConnection(connectionName);
        }
    }
}