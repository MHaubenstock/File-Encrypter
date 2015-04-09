import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

private abstract class ClientConnectionAbstract
{
	Boolean initialized = false;
	String sessionID;
	Thread readMessage;

	Socket socket;

	BufferedReader socketReader;
    PrintWriter socketWriter;

	public ClientConnectionAbstract(Socket socket, String sessionID) throws IOException
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