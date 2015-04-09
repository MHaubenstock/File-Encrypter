import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class ClientMessageHandler extends Thread
{
	private Socket connectedSocket;
	private LinkedBlockingQueue<Object> messages;
	//private OutputStream socketWriter;
	private BufferedReader socketReader;
	private PrintWriter socketWriter;

	private String sessionID;
	private Boolean initialized = false;

	public ClientMessageHandler(String ip, int port)
	{
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

	public void run()
	{
		//Start listening for messages
		Thread readMessage = new Thread()
        {
            public void run()
            {
                while(true)
                {
                    try
                    {
                        Object message = socketReader.readLine();
                        messages.put(message);
                    }
                    catch(Exception e){ e.printStackTrace(); }
                }
            }
        };

        readMessage.setDaemon(true);
        readMessage.start();


		//Start message handler
		Thread messageHandling = new Thread()
		{
            public void run()
            {
                while(true)
                {
                    try
                    {
                        Object message = messages.take();
						
                        handleMessage(message);

                        System.out.println("Message Received: " + message);
                    }
                    catch(Exception e){ e.printStackTrace(); }
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

		sendMessageToServer(obj);
	}

	private void handleMessage(Object messageObject)
    {
    	JSONParser parser = new JSONParser();
    	JSONObject message = new JSONObject();

    	try
    	{
    		message = (JSONObject)parser.parse(messageObject.toString());
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}

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
    			JSONObject response = new JSONObject();
    			response.put("command", "initialized");

    			//Set client to initialized
    			initialized = true;


    			break;

    		case "disconnecting":
    			

    			break;
    	}
    }
}





