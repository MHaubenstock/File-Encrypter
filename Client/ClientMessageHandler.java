import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class ClientMessageHandler extends Thread
{
	//For event listener
	protected List _listeners = new ArrayList();;

	private Socket connectedSocket;
	private LinkedBlockingQueue<Object> messages;
	//private OutputStream socketWriter;
	private BufferedReader socketReader;
	private PrintWriter socketWriter;

	private String sessionID;
	private Boolean initialized = false;
	private Boolean connected = false;

	//Threads
	Thread readMessage;
	Thread messageHandling;

	public ClientMessageHandler()
	{

	}

	public ClientMessageHandler(String ip, int port)
	{
		//Connect to server
		try
		{
			connectedSocket = new Socket(ip, port);
			socketWriter = new PrintWriter(connectedSocket.getOutputStream(), true);
			socketReader = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
			messages = new LinkedBlockingQueue<Object>();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}

	public void connect(String ip, int port)
	{
		//Connect to server
		try
		{
			connectedSocket = new Socket(ip, port);
			socketWriter = new PrintWriter(connectedSocket.getOutputStream(), true);
			socketReader = new BufferedReader(new InputStreamReader(connectedSocket.getInputStream()));
			messages = new LinkedBlockingQueue<Object>();

			connected = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void run()
	{
		//Start listening for messages
		readMessage = new Thread()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted())
                {
                    try
                    {
                        Object message = socketReader.readLine();

                        if(!(message == null))
                        	messages.put(message);
                    }
                    catch(InterruptedException e)
                    {
                    	Thread.currentThread().interrupt();
                    }
                    catch(Exception e) { e.printStackTrace(); }
                }

                //Close the socket, severing the connection
				try
				{
					connectedSocket.close();
				}
				catch(Exception e) { e.printStackTrace(); }
            }
        };

        readMessage.setDaemon(true);
        readMessage.start();


		//Start message handler
		messageHandling = new Thread()
		{
            public void run()
            {
                while(!Thread.currentThread().interrupted())
                {
                    try
                    {
                        Object message = messages.take();
						
						try
						{
                        	handleMessage(message);
                        }
                        catch(Exception e)
                        {
                        	e.printStackTrace();
                        }

                        System.out.println("Message Received: " + message);
                    }
                    catch(InterruptedException e)
                    {
                    	Thread.currentThread().interrupt();
                    }
                    catch(Exception e) { e.printStackTrace(); }
                }
            }
        };

        messageHandling.setDaemon(true);
        messageHandling.start();
	}

	public void pingServer()
	{
		System.out.println("TEST  " + sessionID);

		JSONObject obj = new JSONObject();
		obj.put("command", "ping");

		sendMessageToServer(obj);
	}

	public void sendFileToPeer(String peer)
	{
		
	}

	public void makeConnectionToClient(String peerSessionID)
	{
		JSONObject obj = new JSONObject();
		obj.put("command", "makeconnection");
		obj.put("peer", peerSessionID);

		sendMessageToServer(obj);
	}

	public void sendMessageToServer(JSONObject message)
	{
		//If the client hasn't identified itself then add identification
		if(!message.containsKey("sessionID"))
			message.put("sessionID", sessionID);

		try
		{
			socketWriter.println(message);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void disconnectFromServer()
	{
		//Build disconnecting message
		JSONObject obj = new JSONObject();
		obj.put("command", "disconnect");
		obj.put("sessionID", sessionID);

		//Send disconnecting message
		sendMessageToServer(obj);

		//Stop message reading threads
		readMessage.interrupt();
		messageHandling.interrupt();

		connected = false;
	}

	private void handleMessage(Object messageObject) throws Exception
    {
    	JSONParser parser = new JSONParser();
    	JSONObject message = new JSONObject();
    	JSONObject response;

   		message = (JSONObject)parser.parse(messageObject.toString());

    	switch((String)message.get("command"))
    	{
    		case "ping":
    			//Respond with a pong
    			System.out.println("Pinged, responding....");
    			
    			

    			break;

    		case "initialize":
    			//Store sessionID
    			sessionID = (String)message.get("sessionID");

    			//Respond with confirmation of initialization
    			response = new JSONObject();
    			response.put("command", "initialized");

    			//Set client to initialized
    			initialized = true;

    			sendMessageToServer(response);


    			break;

    		case "connectionrequest":

    			//Respond with confirmation or denial
    			response = new JSONObject();
    			response.put("command", "connectionconfirmation");
    			//(String)message.get("sessionID")

    			break;


    		case "peerlist":

    			Object[] peerList = ((JSONArray)parser.parse(message.get("peers").toString())).toArray();

    			receivedPeerList(peerList);

    			break;


    		case "incomingfile":



    			break;

    		//This is the server telling the client that the server is about to stop running
    		//and that the client should perform necessary disconnecting procedures
    		case "disconnect":

    			connected = false;

    			break;

    		//Called from the server as confirmation of the client disconnecting
    		//The client should be disconnected at this point so this code should never be ran
    		case "disconnecting":

    			connected = false;

    			break;
    	}
    }

    private void acceptIncomingFile()
    {

    }

    public Boolean isConnected()
    {
    	return connected;
    }

    public synchronized void addEventListener(MessageEventListener listener) 
    {
        _listeners.add(listener);
    }

    public synchronized void removeEventListener(MessageEventListener listener)  
    {
       _listeners.remove(listener);
    }

    protected void connectedToServer()
    {
    	Iterator i = _listeners.iterator();

        while(i.hasNext())
        {
            ((MessageEventListener) i.next()).connectedToServer();
        }
    }

    protected void receivedPeerList(Object[] peerList)
    {
    	Iterator i = _listeners.iterator();

        while(i.hasNext())
        {
            ((MessageEventListener) i.next()).receivedPeerList(peerList);
        }
    }
}





