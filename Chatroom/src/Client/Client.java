package Client;
import java.awt.EventQueue;
import java.io.*;
import java.net.Socket;
import java.util.*;

import javax.swing.*;
import java.awt.event.*;

public class Client {

	private JFrame frame;
	private JTextField inputField;
	private JTextField addressField;
	private JTextField usernameField;
	private JTextField portField;
	private JTextArea outputArea;
	
	private Socket s;
	private BufferedReader reader;
	private PrintWriter writer;
	private ArrayList<String> users = new ArrayList();
	private boolean isConnected = false;
	private String username;
	private String address;
	private int port;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void ClientThread() {
		Thread ClientReader = new Thread(new ClientReader());
		ClientReader.start();
	}
	
	public void addUser(String user) {
		users.add(user);
	}
	
	public void removeUser(String user) {
		users.remove(user);
	}
	
	public class ClientReader implements Runnable{
		@Override
		public void run() {
			String stream;
			String[] data;
			try {
				while((stream = reader.readLine()) != null) { //receives sent message
					data = stream.split("|");
					outputArea.append("\n" + data[0] + ": " + data[1]);
					outputArea.setCaretPosition(outputArea.getDocument().getLength());
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create the application.
	 */
	public Client() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel usernameLab = new JLabel("Username");
		usernameLab.setBounds(167, 11, 55, 14);
		frame.getContentPane().add(usernameLab);
		
		outputArea = new JTextArea();
		outputArea.setLineWrap(true);
		outputArea.setBounds(10, 61, 417, 406);
		frame.getContentPane().add(outputArea);
		
		inputField = new JTextField();
		inputField.setBounds(10, 478, 318, 20);
		frame.getContentPane().add(inputField);
		inputField.setColumns(10);
		
		JButton sendBtn = new JButton("SEND");
		sendBtn.setBounds(338, 477, 89, 23);
		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String message = inputField.getText();
					writer.println(username + "|" + message);
					writer.flush();
				}catch(Exception e) {
					e.printStackTrace();
				}
				inputField.setText("");
				inputField.requestFocus();
			}
		});
		frame.getContentPane().add(sendBtn);
		
		JLabel addressLab = new JLabel("Address");
		addressLab.setBounds(10, 11, 46, 14);
		frame.getContentPane().add(addressLab);
		
		addressField = new JTextField();
		addressField.setText("127.0.0.1");
		addressField.setEnabled(false);
		addressField.setEditable(false);
		addressField.setBounds(66, 8, 86, 20);
		frame.getContentPane().add(addressField);
		addressField.setColumns(10);
		
		JLabel portLab = new JLabel("Port");
		portLab.setBounds(10, 36, 46, 14);
		frame.getContentPane().add(portLab);
		
		usernameField = new JTextField();
		usernameField.setBounds(232, 8, 86, 20);
		frame.getContentPane().add(usernameField);
		usernameField.setColumns(10);
		
		portField = new JTextField();
		portField.setBounds(66, 33, 86, 20);
		frame.getContentPane().add(portField);
		portField.setColumns(10);
		
		JButton connectBtn = new JButton("CONNECT");
		connectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				address = addressField.getText();
				port = Integer.parseInt(portField.getText());
				username = usernameField.getText();
				usernameField.setEditable(false);
				
				if(isConnected) {
					outputArea.append("\nYou all already connected to server.");
				}else {
					try {
						s = new Socket(address, port);
						InputStreamReader sr = new InputStreamReader(s.getInputStream());
						reader = new BufferedReader(sr);
						writer = new PrintWriter(s.getOutputStream());
						writer.println("\nServer|" + username + " has connected.");
						writer.flush();
						isConnected = true;
					}catch(Exception e) {
						outputArea.append("\nConnection to server failed.");
					}
					ClientThread();
				}
			}
		});
		connectBtn.setBounds(206, 32, 100, 23);
		frame.getContentPane().add(connectBtn);
		
		JButton disconnectBtn = new JButton("DISCONNECT");
		disconnectBtn.setBounds(316, 32, 111, 23);
		frame.getContentPane().add(disconnectBtn);
	}
}
