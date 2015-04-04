import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;

//Serves a single connection between two clients
public class DataServer implements Callable
{
	Socket socket;

	public DataServer(Socket socket)
	{
		//Open socket on new port
		this.socket = socket;
	}

	@Override
    public Object call() throws Exception
    {
    	System.out.println("Connected");

    	BufferedInputStream inStream = new BufferedInputStream(socket.getInputStream());

		while(!socket.isClosed())
		{
	    	//if(inStream.available() > 0)
	        System.out.println(inStream.read());
		}

		System.out.println("Disconnected");

    	return null;
    }
}