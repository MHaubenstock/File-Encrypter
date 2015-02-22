import java.awt.ComponentOrientation;
import java.awt.EventQueue;

import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class FileEncrypter {

	private JFrame frmFileEncryptionInput;
	private JTextField tf_key1;
	private JButton btn_key1;
	private JLabel lbl_key1;
	private JTextField tf_key2;
	private JLabel lbl_key2;
	private JButton btn_key2;
	public static JTextField tf_plainText;
	private JButton btn_plainText;
	private JButton btn_encrypt;
	private JButton btn_decrypt;
	private JButton btn_random_iv;
	private JButton btn_browse_iv;
	private JLabel lbl_iv;
	private JTextArea tf_iv;
	private StringBuilder sb = new StringBuilder();
	private java.io.File file;
	
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
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
	public FileEncrypter() {
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
					System.out.println("running");
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
					System.out.println("running");
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
		frmFileEncryptionInput.setBounds(100, 200, 450, 300);
		frmFileEncryptionInput.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFileEncryptionInput.getContentPane().setLayout(null);
		
		lbl_key1 = new JLabel("Key 1 (hex)");
		lbl_key1.setBounds(39, 58, 79, 14);
		frmFileEncryptionInput.getContentPane().add(lbl_key1);
		
		tf_key1 = new JTextField();
		tf_key1.setText("Enter key or file location");
		tf_key1.setBounds(128, 59, 164, 20);
		frmFileEncryptionInput.getContentPane().add(tf_key1);
		tf_key1.setColumns(10);
		
		btn_key1 = new JButton("Browse");
		btn_key1.setBounds(302, 58, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_key1);
		
		lbl_key2 = new JLabel("Key 2 (hex)");
		lbl_key2.setBounds(39, 83, 79, 14);
		frmFileEncryptionInput.getContentPane().add(lbl_key2);
		
		tf_key2 = new JTextField();
		tf_key2.setText("Enter key or file location");
		tf_key2.setBounds(128, 84, 164, 20);
		frmFileEncryptionInput.getContentPane().add(tf_key2);
		tf_key2.setColumns(10);
		
		btn_key2 = new JButton("Browse");
		btn_key2.setBounds(302, 83, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_key2);
		
		lbl_iv = new JLabel("IV (hex)");
		lbl_iv.setBounds(39, 135, 79, 14);
		frmFileEncryptionInput.getContentPane().add(lbl_iv);
		
		btn_browse_iv = new JButton("Browse");
		btn_browse_iv.setBounds(302, 131, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_browse_iv);
		
		btn_random_iv = new JButton("Random");
		btn_random_iv.setBounds(302, 156, 89, 23);
		frmFileEncryptionInput.getContentPane().add(btn_random_iv);
		
		btn_encrypt = new JButton("Encrypt");
		btn_encrypt.setBounds(88, 205, 118, 46);
		frmFileEncryptionInput.getContentPane().add(btn_encrypt);
		
		btn_decrypt = new JButton("Decrypt");
		btn_decrypt.setBounds(209, 205, 118, 46);
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
		
		tf_iv = new JTextArea();
		tf_iv.setFont(new Font("Tahoma", Font.PLAIN, 11));
		tf_iv.setText("Enter 64 bit IV\r\nOr browse for file\r\nOr create random");
		tf_iv.setBounds(128, 133, 164, 46);
		frmFileEncryptionInput.getContentPane().add(tf_iv);
	}
}
