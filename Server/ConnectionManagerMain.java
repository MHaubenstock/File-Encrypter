import java.awt.ComponentOrientation;
import java.awt.EventQueue;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ConnectionManagerMain implements ConnectionManagerEventListener
{
	private JFrame connectionManagerFrame;
	DefaultListModel connectionsModel = new DefaultListModel();
	private JList<String> connectionList;

	ConnectionManager manager = new ConnectionManager();

	public void openedConnection(String connectionName)
	{
		System.out.println("Connected with: " + connectionName);

		//Add connection to list
		connectionsModel.addElement(connectionName);

		//We have at least one connection so start listening for requests
		manager.startListeningForMessages();
	}

	public void closedConnection(String connectionName)
	{
		//remove client from list
		connectionsModel.removeElement(connectionName);
	}

	public void receivedRequestForPrivateConnection(String requestor, String requestee)
	{

	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					ConnectionManagerMain window = new ConnectionManagerMain();
					window.connectionManagerFrame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	* Create the application.
	*/
	public ConnectionManagerMain()
	{
		//Add this instance to connection manager listeners
		manager.addEventListener(this);

		initialize();

		//Start connection listener
		manager.startListeningForConnections();
	}

	/**
	* Initialize the contents of the frame.
	*/
	public void initialize()
	{
		connectionManagerFrame = new JFrame();
		connectionManagerFrame.setTitle("File Encryption/Decryption Input");
		connectionManagerFrame.setBounds(700, 200, 400, 400);
		connectionManagerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		connectionManagerFrame.getContentPane().setLayout(null);
		
		connectionList = new JList<String>(connectionsModel);
		connectionList.setBounds(20, 20, 360, 340);
		connectionManagerFrame.getContentPane().add(connectionList);
	}
}