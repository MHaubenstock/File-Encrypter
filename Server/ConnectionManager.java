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
import java.util.concurrent.LinkedBlockingQueue;
import java.security.SecureRandom;
import java.math.BigInteger;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

//Manages the connection between clients
public class ConnectionManager
{
	//For event listener
	protected List _listeners;
    ExecutorService executorService;
    Map<String, ClientConnection> clientDictionary;

    //Network stuff
    private ServerSocket serverSocket;
    private LinkedBlockingQueue<Object> messages;

	public ConnectionManager()
	{
		_listeners = new ArrayList();
		executorService = Executors.newFixedThreadPool(100);
		clientDictionary = new HashMap<String, ClientConnection>();
		messages = new LinkedBlockingQueue<Object>();
	}

	public void startListeningForConnections()
	{
		new Thread()
		{
			public void run()
			{
				listenForConnections();
			}
		}.start();
	}

	private void listenForConnections()
	{
		ClientConnection connection;

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
                connection = new ClientConnection(serverSocket.accept(), sessionID = new BigInteger(130, sessionIDGenerator).toString(32));
                System.out.println("Received connection");

                //Save client in the dictonary
				clientDictionary.put(sessionID, connection);

				//Send opened connection event with name of connection (session ID for now)
        		openedConnection(sessionID);

        		//Build json payload for initializing this client
        		JSONObject obj = new JSONObject();
        		obj.put("command", "initialize");
        		obj.put("sessionID", sessionID);

				//Tell client its sessionID to let it know it's connected
				sendToOne(connection, obj);
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
            
            //executorService.shutdownNow();
        }
	}

	public void startListeningForMessages()
	{
		new Thread()
		{
			public void run()
			{
				listenForMessages();
			}
		}.start();
	}

	private void listenForMessages()
	{
		while(true)
		{
			try
			{
				Object message = messages.take();
				
				//Handle the message
				handleMessage(message);

				System.out.println("Message Received: " + message);
			}
			catch(InterruptedException e){ e.printStackTrace(); }
		}
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

    protected void closedConnection(String connectionName)
    {
    	Iterator i = _listeners.iterator();

        while(i.hasNext())
        {
            ((ConnectionManagerEventListener) i.next()).closedConnection(connectionName);
        }
    }

    protected void receivedRequestForPrivateConnection(String requestor, String requestee)
    {
    	Iterator i = _listeners.iterator();

        while(i.hasNext())
        {
            ((ConnectionManagerEventListener) i.next()).receivedRequestForPrivateConnection(requestor, requestee);
        }
    }

    public void sendToOne(ClientConnection clientConnection, Object message)
    {
    	clientConnection.sendMessage(message);
    }

    public void sendToAll(Object message)
    {
    	for(ClientConnection connection : clientDictionary.values())            
            connection.sendMessage(message);
    }

    private void handleMessage(Object messageObject)
    {
    	JSONParser parser = new JSONParser();
    	JSONObject message = new JSONObject();
    	ClientConnection client = null;
    	JSONObject response;

    	try
    	{
    		message = (JSONObject)parser.parse(messageObject.toString());
    		client = clientDictionary.get(message.get("sessionID"));
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}

    	//If no client was found then return
    	if(client == null)
    	{
    		System.out.println("No client found!");
    		return;
    	}

    	switch((String)message.get("command"))
    	{
    		case "ping":
    			//Respond with a pong
    			System.out.println("Server was pinged, responding....");

    			response = new JSONObject();
    			response.put("command", "pong");
    			response.put("sessionID", message.get("sessionID"));

    			sendToOne(client, response);


    			break;

    		case "initialized":
    			//Set this client to initialized
    			client.initialized = true;

    			//Send the client a peer list
    			//sendPeerList(client);
    			sendPeerListToAll();

    			break;

    		case "fetchpeerlist":

    			sendPeerList(client);

    			break;

    		case "disconnect":
    			//Send response telling client it is being disconnected
    			response = new JSONObject();
    			response.put("command", "disconnecting");
    			response.put("sessionID", message.get("sessionID"));

    			sendToOne(client, response);

    			client.disconnect();
    			clientDictionary.remove(client.sessionID);
    			closedConnection(message.get("sessionID").toString());

    			//Send updated peer list
    			sendPeerListToAll();

    			break;
    	}
    }

    private void sendPeerListToAll()
    {
    	JSONObject response = new JSONObject();
		response.put("command", "peerlist");

		ArrayList peerList;

		for(ClientConnection peerToSendTo : clientDictionary.values())
		{
			response.put("sessionID", peerToSendTo.sessionID);

			//Create peer list minus the client asking for the list
			peerList = new ArrayList();

			for(ClientConnection peer : clientDictionary.values())
			{ 		
	            //If this isn't the client the peer list is being sent to
	            if(!peerToSendTo.sessionID.equals(peer.sessionID))
	            	//Add this client to he peer list
	            	peerList.add(peer.sessionID);
	        }

			response.put("peers", peerList);
			sendToOne(peerToSendTo, response);
		}
    }

    private void sendPeerList(ClientConnection client)
    {
    	JSONObject response = new JSONObject();
		response.put("command", "peerlist");
		response.put("sessionID", client.sessionID);

		//Create peer list minus the client asking for the list
		ArrayList peerList = new ArrayList();

		for(ClientConnection peer : clientDictionary.values())
		{            
            //If this isn't the client the peer list is being sent to
            if(!client.sessionID.equals(peer.sessionID))
            	//Add this client to he peer list
            	peerList.add(peer.sessionID);
        }

		response.put("peers", peerList);

		sendToOne(client, response);
    }

    private class ClientConnection
	{
		Boolean initialized = false;
		String sessionID;
		Thread readMessage;

		Socket socket;

		BufferedReader socketReader;
	    PrintWriter socketWriter;

		public ClientConnection(Socket socket, String sessionID) throws IOException
		{
			this.socket = socket;
			this.sessionID = sessionID;

			socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			socketWriter = new PrintWriter(socket.getOutputStream(), true);

			readMessage = new Thread()
			{
	            public void run()
	            {
	                while(!Thread.interrupted())
	                {
	                	try
	                    {
	                        Object message = socketReader.readLine();
	                        
	                        if(!(message == null))
	                        	messages.put(message);
	                    }
	                    catch(Exception e)
	                    {
	                    	e.printStackTrace();
	                    }
	                }

	                //This is ran when the client is disconnected after the disconnect() method is called
	                try
					{
						socket.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
	            }
	        };

	        readMessage.setDaemon(true);	//Terminate when main thread ends
	        readMessage.start();
		}

		private void sendMessage(Object message)
		{
			try
			{
				socketWriter.println(message);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		private void disconnect()
		{
			readMessage.interrupt();
		}
	}

	//Facilitates a connection between two clients
	public class DataServer
	{
		ClientConnection connection1;
		ClientConnection connection2;

		public DataServer(ClientConnection connection1, ClientConnection connection2)
		{
			//Open socket on new port
			this.connection1 = connection1;
			this.connection2 = connection2;
		}
	}
}