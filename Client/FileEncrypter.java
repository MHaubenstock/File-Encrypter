import java.awt.ComponentOrientation;
import java.awt.EventQueue;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class FileEncrypter implements EncryptEventListener, MessageEventListener
{
	private JFrame frmFileEncryptionInput;
	private JTextField tf_key1;
	private JButton btn_key1;
	private JLabel lbl_key1;
	private JTextField tf_key2;
	private JLabel lbl_key2;
	private JButton btn_key2;
	public static JTextField tf_plainText;
	public static JTextField tf_destination;
	private JButton btn_plainText;
	private JButton btn_destination;
	private JButton btn_encrypt;
	private JButton btn_decrypt;
	private JButton btn_random_iv;
	private JButton btn_browse_iv;
	private JLabel lbl_iv;
	private JTextArea tf_iv;
	private JProgressBar progressBar;
	private ButtonGroup modeGroup;
	private JRadioButton ocmRadioButton;
	private JRadioButton icmRadioButton;
	private JButton connectButton;
	private JButton disconnectButton;
	private JButton quitButton;
	private JButton sendToPeerButton;
	private DefaultListModel peerListModel;
	private JList<String> peerList;
	private StringBuilder sb = new StringBuilder();
	private java.io.File file;
	private java.io.File outputFile;

	//Instantiate the encrypter
	private OutsideChainingMode ocmEncrypter = new OutsideChainingMode();
	private InsideChainingMode icmEncrypter = new InsideChainingMode();
	
	//For Communicating with the server
	private ClientMessageHandler messageHandler = new ClientMessageHandler();

	private void pingServer()
	{
		//Ping the server
		if(messageHandler.isConnected())
		{
			System.out.println("Pinging the server");
			messageHandler.pingServer();
		}
	}

	private String hexGenerator()
	{
		Random rnd = new Random();
		StringBuffer stb = new StringBuffer();
		while(stb.length() < 16)
		{
			stb.append(Integer.toHexString(rnd.nextInt()));
		}
		return stb.toString().substring(0,16);
	}
	
	//checks for hex and length
	private boolean isHex(String str)
	{
		str = str.replaceAll("\\s", "");
		
		if (str.length() != 16)
			return false;
		
		for (int i = 0; i < str.length(); i++){
			char current = str.charAt(i);
			if(!(current >= 'a' && current <= 'f' || current >= 'A' && current <= 'F' || 
				Character.isDigit(current))){
				return false;
			}
		}

		return true;
	}

	//Handle encryption/decryption events
	public void beganProcessing()
	{
		System.out.println("Began Processing");
		progressBar.setBorder(BorderFactory.createTitledBorder("Processing..."));


	}
	
	public void processedData(byte[] bytes, long bytesProcessed, long totalBytes)
	{
		//Update progress bar
		//System.out.println("Progress: " + (int)(((float)bytesProcessed / totalBytes) * 100) + "%");
		progressBar.setValue((int)(((float)bytesProcessed / totalBytes) * 100));

		try
		{
			//Send data to server
			if(messageHandler.isConnected() && messageHandler.isSendingToPeer())
			{
				//Convertg bytes to hex
				String dataBlockHex = "";

				for(int x = 0; x < bytes.length; ++x)
					dataBlockHex += String.format("%02x", bytes[x]);

				messageHandler.sendDataBlockToPeer(peerList.getSelectedValue().toString(), dataBlockHex);
			}
		}
		catch(Exception e)
		{

		}
	}

	public void finishedProcessing()
	{
		System.out.println("Finished Processing");
		progressBar.setBorder(BorderFactory.createTitledBorder("Finished"));

		//Tell peer file transfer has ended
		if(messageHandler.isConnected() && messageHandler.isSendingToPeer())
		{
			messageHandler.endFileTransferToPeer(peerList.getSelectedValue().toString());
		}
	}

	public void connectedToServer()
	{
		System.out.println("Connected to server");
	}
	
	public void receivedPeerList(Object[] peerList)
	{
		System.out.println("Peer list: " + peerList);

		//Clear the peer list
		peerListModel.clear();

		for(Object peer : peerList)
			peerListModel.addElement((String)peer);
	}

	//The client has just received a request for another client to send a file with this info
	//use this info to initialize an encrypted file transfer
	public void receivedFileSendRequest(String fileName, String k1, String k2, String iv)
	{
		try
		{
			if(ocmRadioButton.isSelected())
				ocmEncrypter.initializeIncrementalEncodingOrDecoding(fileName, k1, k2, iv);
			else
				icmEncrypter.initializeIncrementalEncodingOrDecoding(fileName, k1, k2, iv);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public void startFileTransfer(String peer)
	{
		new Thread(){
			public void run() {
				try
				{
					if(ocmRadioButton.isSelected())
						ocmEncrypter.encode(file.getPath(), outputFile.getAbsolutePath(), tf_key1.getText(), tf_key2.getText(), tf_iv.getText().replaceAll(" ", ""));
					else
						icmEncrypter.encode(file.getPath(), outputFile.getAbsolutePath(), tf_key1.getText(), tf_key2.getText(), tf_iv.getText().replaceAll(" ", ""));
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void recievedDataBlockFromPeer(String peer, String dataBlock)
	{
		try
		{
			if(ocmRadioButton.isSelected())
				ocmEncrypter.decodeIncrementally(dataBlock);
			else
				icmEncrypter.decodeIncrementally(dataBlock);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public void endedFileTransferWithPeer(String peer)
	{
		try
		{
			if(ocmRadioButton.isSelected())
				ocmEncrypter.endIncrementalEncodingOrDecoding();
			else
				icmEncrypter.endIncrementalEncodingOrDecoding();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	* Launch the application.
	*/
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileEncrypter window = new FileEncrypter();
					window.frmFileEncryptionInput.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FileEncrypter()
	{
		/*
		final JOptionPane optionPane = new JOptionPane(
	    "The only way to close this dialog is by\n"
	    + "pressing one of the following buttons.\n"
	    + "Do you understand?",
	    JOptionPane.QUESTION_MESSAGE,
	    JOptionPane.YES_NO_OPTION);
		*/
		
		//Add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
	        public void run()
	        {
	        	System.out.println("In shutdown hook");

            	//Tell server you're disconnection and close the socket
            	if(!(messageHandler == null))
            		messageHandler.disconnectFromServer();
	        }
    	}, "Shutdown-thread"));

		//Add this instance to the list of encryption/decryption listeners
		ocmEncrypter.addEventListener(this);
		icmEncrypter.addEventListener(this);

		//Add this instance to list of message handling listeners
		messageHandler.addEventListener(this);

		initialize();
		//one click highlight textfield key1
		tf_key1.addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent fEvt)
			{
				JTextField tField = (JTextField)fEvt.getSource();
				tField.selectAll();
			}
		});
		//one click highlight textfield key2
		tf_key2.addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent fEvt)
			{
				JTextField tField = (JTextField)fEvt.getSource();
				tField.selectAll();
			}
		});
		//one click highlight textfield plaintext
		tf_plainText.addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent fEvt)
			{
				JTextField tField = (JTextField)fEvt.getSource();
				tField.selectAll();
			}
		});
		
		//plain text browser
		btn_plainText.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0){
				JFileChooser fileChooser = new JFileChooser();
								
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					file = fileChooser.getSelectedFile();
					tf_plainText.setText(file.getName());
				}
				else{
					tf_plainText.setText("File not found");
				}
			}
		});

		//File destination text field and browser
		//one click highlight textfield destination
		tf_destination.addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent fEvt)
			{
				JTextField tField = (JTextField)fEvt.getSource();
				tField.selectAll();
			}
		});
		
		//plain text browser
		btn_destination.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0){
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					outputFile = fileChooser.getSelectedFile();
					tf_destination.setText(outputFile.getName());
				}
				else{
					tf_destination.setText("File not found");
				}
			}
		});
		
		//key1 browser
		btn_key1.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0){
				JFileChooser fileChooser = new JFileChooser();
								
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					java.io.File file = fileChooser.getSelectedFile();
					String strFile = file.toString();
					try {
						String input = new Scanner(new File(strFile)).useDelimiter("\\A").next();
						if (isHex(input) == true)
						{
							tf_key1.setText(input);
						}
						else{
							tf_key1.setText("Not hex or wrong length");
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					tf_key1.setText("File not found");
				}
			}
		});
		
		//key2 browser
		btn_key2.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0){
				JFileChooser fileChooser = new JFileChooser();
								
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					java.io.File file = fileChooser.getSelectedFile();
					String strFile = file.toString();
					try {
						String input = new Scanner(new File(strFile)).useDelimiter("\\A").next();
						if (isHex(input) == true)
						{
							tf_key2.setText(input);
						}
						else{
							tf_key2.setText("Not hex or wrong length");
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					tf_key2.setText("File not found");
				}
			}
		});
		
		//IV browse
		btn_browse_iv.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0){
				JFileChooser fileChooser = new JFileChooser();
								
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				{
					java.io.File file = fileChooser.getSelectedFile();
					String strFile = file.toString();
					try {
						String input = new Scanner(new File(strFile)).useDelimiter("\\A").next();
						if (isHex(input) == true)
						{
							tf_iv.setText(input);
						}
						else{
							tf_iv.setText("Not hex or wrong length");
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				else{
					tf_iv.setText("File not found");
				}
			}
		});
		
		//random hex generator
		btn_random_iv.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0){
				JFileChooser fileChooser = new JFileChooser();
				
				String hex = hexGenerator();
				String spacedHex = hex.replaceAll("(.{4})(?!$)", "$1 ");
				tf_iv.setText(spacedHex);
			}
		});
		
		//Encrypt
		btn_encrypt.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0){
				/*
				Some error checking before allowing to run - 'run here' commented below
				variables used:
				tf_key1;
				tf_key2;
				tf_iv;
				tf_plainText;
				*/				
				boolean[] canRun = new boolean[5];
				JTextField[] fields = new JTextField[2];
				fields[0] = tf_key1;
				fields[1] = tf_key2;

				////TAKE THIS OUT WHEN YOU'RE DONE TESTING IT!
				pingServer();
								
				//text view checking keys
				for (int i = 0; i < 2; i++){
					if (isHex(fields[i].getText()) == true)
					{
						canRun[i] = true;
					}
					else{
						fields[i].setText("Not hex or wrong length");
						canRun[i] = false;
					}
				}
				//iv textarea
				if (isHex(tf_iv.getText()) == true)
					{
						canRun[2] = true;
					}
					else{
						tf_iv.setText("Not hex or wrong length");
						canRun[2] = false;
					}
				
				//check plain text
				try{
					file.exists();
					canRun[3] = true;
				}catch (Exception e){
					canRun[3] = false;
					tf_plainText.setText("Invalid file");
				}				
				
				canRun[4] = true;
				for (int i = 0; i < 5; i++){
					if (canRun[i] == false){
						canRun[4] = false;
					}
				}
				
				if (canRun[4] == true){
					//run here
					System.out.println("Encrypting");

					new Thread(){
						public void run() {
							try
							{
								if(ocmRadioButton.isSelected())
									ocmEncrypter.encode(file.getPath(), outputFile.getAbsolutePath(), tf_key1.getText(), tf_key2.getText(), tf_iv.getText().replaceAll(" ", ""));
								else
									icmEncrypter.encode(file.getPath(), outputFile.getAbsolutePath(), tf_key1.getText(), tf_key2.getText(), tf_iv.getText().replaceAll(" ", ""));
							}
							catch(IOException e)
							{
								e.printStackTrace();
							}
						}
					}.start();
				}	
			}
		});
		
		//Decrypt
		btn_decrypt.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0){
						/*
				Some error checking before allowing to run - 'run here' commented below
				variables used:
				tf_key1;
				tf_key2;
				tf_iv;
				tf_plainText;
				*/				
				boolean[] canRun = new boolean[5];
				JTextField[] fields = new JTextField[2];
				fields[0] = tf_key1;
				fields[1] = tf_key2;
								
				//text view checking keys
				for (int i = 0; i < 2; i++){
					if (isHex(fields[i].getText()) == true)
					{
						canRun[i] = true;
					}
					else{
						fields[i].setText("Not hex or wrong length");
						canRun[i] = false;
					}
				}
				//iv textarea
				if (isHex(tf_iv.getText()) == true)
					{
						canRun[2] = true;
					}
					else{
						tf_iv.setText("Not hex or wrong length");
						canRun[2] = false;
					}
				
				//check plain text
				try{
					file.exists();
					canRun[3] = true;
				}catch (Exception e){
					canRun[3] = false;
					tf_plainText.setText("Invalid file");
				}				
				
				canRun[4] = true;
				for (int i = 0; i < 5; i++){
					if (canRun[i] == false){
						canRun[4] = false;
					}
				}
				
				if (canRun[4] == true){
					//run here
					System.out.println("Decrypting");
					new Thread(){
						public void run() {
							try
							{
								if(ocmRadioButton.isSelected())
									ocmEncrypter.decode(file.getPath(), outputFile.getAbsolutePath(), tf_key1.getText(), tf_key2.getText(), tf_iv.getText().replaceAll(" ", ""));
								else
									icmEncrypter.decode(file.getPath(), outputFile.getAbsolutePath(), tf_key1.getText(), tf_key2.getText(), tf_iv.getText().replaceAll(" ", ""));
							}
							catch(IOException e)
							{
								e.printStackTrace();
							}
						}
					}.start();
				}
			}
		});

		//Connect button
		connectButton.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0)
			{
				//Connect to the server
				messageHandler.connect("localhost", 8080);
				//Start thread to listen for initialization requests from the server
				messageHandler.start();
			}
		});

		//Disconnect button
		disconnectButton.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0)
			{
				if(messageHandler.isConnected())
				{
					messageHandler.disconnectFromServer();
					messageHandler = new ClientMessageHandler();
					peerListModel.clear();
				}
			}
		});

		//Send file to peer button
		sendToPeerButton.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent arg0)
			{
				if(messageHandler.isConnected())
				{
					boolean[] canRun = new boolean[5];
					JTextField[] fields = new JTextField[2];
					fields[0] = tf_key1;
					fields[1] = tf_key2;
									
					//text view checking keys
					for (int i = 0; i < 2; i++){
						if (isHex(fields[i].getText()) == true)
						{
							canRun[i] = true;
						}
						else{
							fields[i].setText("Not hex or wrong length");
							canRun[i] = false;
						}
					}
					//iv textarea
					if (isHex(tf_iv.getText()) == true)
						{
							canRun[2] = true;
						}
						else{
							tf_iv.setText("Not hex or wrong length");
							canRun[2] = false;
						}
					
					//check plain text
					try{
						file.exists();
						canRun[3] = true;
					}catch (Exception e){
						canRun[3] = false;
						tf_plainText.setText("Invalid file");
					}				
					
					canRun[4] = true;
					for (int i = 0; i < 5; i++){
						if (canRun[i] == false){
							canRun[4] = false;
						}
					}
					
					if (canRun[4] == true){
						messageHandler.sendFileRequestToPeer(peerList.getSelectedValue().toString(), outputFile.getAbsolutePath(), tf_key1.getText(), tf_key2.getText(), tf_iv.getText().replaceAll(" ", ""));
					}
				}
			}
		});
	}



	/**
	* Initialize the contents of the frame.
	*/
	public void initialize() {
		frmFileEncryptionInput = new JFrame();
		frmFileEncryptionInput.setTitle("File Encryption/Decryption Input");
		frmFileEncryptionInput.setBounds(100, 200, 750, 423);
		frmFileEncryptionInput.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFileEncryptionInput.getContentPane().setLayout(null);
		
		lbl_key1 = new JLabel("Key 1 (hex)");
		lbl_key1.setBounds(39, 81, 79, 14);
		frmFileEncryptionInput.getContentPane().add(lbl_key1);
		
		tf_key1 = new JTextField();
		tf_key1.setText("Enter key or file location");
		tf_key1.setBounds(128, 82, 164, 20);
		frmFileEncryptionInput.getContentPane().add(tf_key1);
		tf_key1.setColumns(10);
		
		btn_key1 = new JButton("Browse");
		btn_key1.setBounds(302, 81, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_key1);
		
		lbl_key2 = new JLabel("Key 2 (hex)");
		lbl_key2.setBounds(39, 106, 79, 14);
		frmFileEncryptionInput.getContentPane().add(lbl_key2);
		
		tf_key2 = new JTextField();
		tf_key2.setText("Enter key or file location");
		tf_key2.setBounds(128, 107, 164, 20);
		frmFileEncryptionInput.getContentPane().add(tf_key2);
		tf_key2.setColumns(10);
		
		btn_key2 = new JButton("Browse");
		btn_key2.setBounds(302, 106, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_key2);
		
		lbl_iv = new JLabel("IV (hex)");
		lbl_iv.setBounds(39, 158, 79, 14);
		frmFileEncryptionInput.getContentPane().add(lbl_iv);
		
		btn_browse_iv = new JButton("Browse");
		btn_browse_iv.setBounds(302, 154, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_browse_iv);
		
		btn_random_iv = new JButton("Random");
		btn_random_iv.setBounds(302, 179, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_random_iv);
		
		btn_encrypt = new JButton("Encrypt");
		btn_encrypt.setBounds(88, 328, 118, 46);
		frmFileEncryptionInput.getContentPane().add(btn_encrypt);
		
		btn_decrypt = new JButton("Decrypt");
		btn_decrypt.setBounds(209, 328, 118, 46);
		frmFileEncryptionInput.getContentPane().add(btn_decrypt);
		
		JLabel lbl_plainText = new JLabel("Plain Text");
		lbl_plainText.setBounds(39, 33, 57, 14);
		frmFileEncryptionInput.getContentPane().add(lbl_plainText);
		
		tf_plainText = new JTextField();
		tf_plainText.setText("Browse for file");
		tf_plainText.setBounds(128, 34, 164, 20);
		frmFileEncryptionInput.getContentPane().add(tf_plainText);
		tf_plainText.setColumns(10);
		
		btn_plainText = new JButton("Browse");
		btn_plainText.setBounds(302, 33, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_plainText);

		JLabel lbl_destination = new JLabel("Output Destination");
		lbl_destination.setBounds(39, 56, 57, 14);
		frmFileEncryptionInput.getContentPane().add(lbl_destination);
		
		tf_destination = new JTextField();
		tf_destination.setText("Browse for file");
		tf_destination.setBounds(128, 57, 164, 20);
		frmFileEncryptionInput.getContentPane().add(tf_destination);
		tf_destination.setColumns(10);
		
		btn_destination = new JButton("Browse");
		btn_destination.setBounds(302, 56, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_destination);
		
		tf_iv = new JTextArea();
		tf_iv.setFont(new Font("Tahoma", Font.PLAIN, 11));
		tf_iv.setText("Enter 64 bit IV\r\nOr browse for file\r\nOr create random");
		tf_iv.setBounds(128, 156, 164, 46);
		frmFileEncryptionInput.getContentPane().add(tf_iv);

		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setBorder(BorderFactory.createTitledBorder("Ready"));
		progressBar.setBounds(88, 252, 239, 50);
		frmFileEncryptionInput.getContentPane().add(progressBar);

		ocmRadioButton = new JRadioButton("Outside Chaining");
		ocmRadioButton.setSelected(true);
		ocmRadioButton.setBounds(59, 208, 150, 25);
		frmFileEncryptionInput.getContentPane().add(ocmRadioButton);
		
		icmRadioButton = new JRadioButton("Inside Chaining");
		icmRadioButton.setBounds(241, 208, 150, 25);
		frmFileEncryptionInput.getContentPane().add(icmRadioButton);

		modeGroup = new ButtonGroup();
		modeGroup.add(ocmRadioButton);
		modeGroup.add(icmRadioButton);

		connectButton = new JButton("Connect");
		connectButton.setBounds(471, 34, 118, 46);
		frmFileEncryptionInput.getContentPane().add(connectButton);

		disconnectButton = new JButton("Disconnect");
		disconnectButton.setBounds(611, 34, 118, 46);
		frmFileEncryptionInput.getContentPane().add(disconnectButton);

		sendToPeerButton = new JButton("Send to Peer");
		sendToPeerButton.setBounds(611, 318, 118, 46);
		frmFileEncryptionInput.getContentPane().add(sendToPeerButton);

		peerListModel = new DefaultListModel();
		peerList = new JList<String>(peerListModel);
		peerList.setBounds(471, 90, 258, 200);
		frmFileEncryptionInput.getContentPane().add(peerList);
	}
}