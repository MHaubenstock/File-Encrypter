import java.io.*;
import java.net.*;

//Serves a single connection between two clients
private class DataServer
{
	ServerSocket dataSocket;

	public DataServer(int portNum)
	{
		//Open socket on new port
		dataSocket = new ServerSocket(portNumber);
	}
}