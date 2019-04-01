package Client;
import java.awt.EventQueue;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Panel;
import java.awt.List;

public class Client {

	private JFrame frmClient;
	private JTextField inputField;
	private JTextField addressField;
	private JTextField usernameField;
	private JTextField portField;
	private JTextArea outputArea;
	private List userList;
	private JButton connectBtn;
	
	private Socket s;
	private BufferedReader reader;
	private PrintWriter writer;
	private ArrayList<String> users = new ArrayList();
	private boolean isConnected = false;
	private String username;
	private String address;
	private int port;
	private boolean dead = false;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();
					window.frmClient.setVisible(true);
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
		//users.add(user);
		userList.add(user);
	}
	
	public void removeUser(String user) {
		//users.remove(user);
		userList.remove(user);
	}
	
	public void removeAllUsers() {
		userList.removeAll();
	}
	
	public class ClientReader implements Runnable{
		@Override
		public void run() {
			String stream;
			String[] data;
			try {
				while(!dead) {
					stream = reader.readLine();
					data = stream.split("\\|");
					if(data[2].equals("message")) {
						outputArea.append(data[0] + ": " + data[1] + "\n");
						outputArea.setCaretPosition(outputArea.getDocument().getLength());
					}
					else if(data[2].equals("connect")) {
						outputArea.append(data[0] + " has connected.\n");
						addUser(data[0]);
					}
					else if(data[2].equals("disconnect")) {
						outputArea.append(data[0] + " has disconnected.\n");
						removeUser(data[0]);
					}
					else if(data[2].equals("listreset")) {
						removeAllUsers();
					}
					else if(data[2].equals("quietRepop")) {
						addUser(data[0]);
					}
					else if(data[2].equals("die")) {
						dead=true;
						frmClient.dispose();
					}
					else if(data[2].equals("close")) {
						writer.flush();
						outputArea.append("Server has ended.\n");
						s.close();
						isConnected = false;
						connectBtn.setEnabled(true);
						usernameField.setEditable(true);
						portField.setEditable(true);
						userList.removeAll();
						dead=true;
					}
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
		frmClient = new JFrame();
		frmClient.setResizable(false);
		frmClient.setTitle("Client");
		frmClient.setBounds(100, 100, 485, 536);
		frmClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClient.getContentPane().setLayout(null);
		
		//when a client closes, DON'T panic, still die
		frmClient.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				if(isConnected) {
					try {
						writer.println(username + "|X|disconnect");
						writer.flush();
						s.close();
						isConnected = false;
						outputArea.append("You have been disconnected.\n");
					}catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		
		JLabel usernameLab = new JLabel("Username");
		usernameLab.setBounds(191, 11, 60, 14);
		frmClient.getContentPane().add(usernameLab);
		
		outputArea = new JTextArea();
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);
		outputArea.setBounds(10, 61, 318, 406);
		frmClient.getContentPane().add(outputArea);
		
		inputField = new JTextField();
		inputField.setBounds(10, 478, 318, 20);
		frmClient.getContentPane().add(inputField);
		inputField.setColumns(10);
		
		JButton sendBtn = new JButton("SEND");
		sendBtn.setBounds(338, 477, 136, 23);
		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					writer.println(username + "|" + inputField.getText() + "|message");
					writer.flush();
				}catch(Exception e) {
					e.printStackTrace();
				}
				inputField.setText("");
				inputField.requestFocus();
			}
		});
		frmClient.getContentPane().add(sendBtn);
		
		JLabel addressLab = new JLabel("Address");
		addressLab.setBounds(10, 11, 60, 14);
		frmClient.getContentPane().add(addressLab);
		
		addressField = new JTextField();
		addressField.setText("127.0.0.1");
		addressField.setEnabled(false);
		addressField.setEditable(false);
		addressField.setBounds(66, 8, 86, 20);
		frmClient.getContentPane().add(addressField);
		addressField.setColumns(10);
		
		JLabel portLab = new JLabel("Port");
		portLab.setBounds(10, 36, 46, 14);
		frmClient.getContentPane().add(portLab);
		
		usernameField = new JTextField();
		usernameField.setBounds(257, 8, 107, 20);
		frmClient.getContentPane().add(usernameField);
		usernameField.setColumns(10);
		
		portField = new JTextField();
		portField.setBounds(66, 33, 86, 20);
		frmClient.getContentPane().add(portField);
		portField.setColumns(10);
		
		connectBtn = new JButton("CONNECT");
		connectBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if(usernameField.getText().equals("")) {
					outputArea.append("Please enter a username.\n");
				}
				else if(!portField.getText().matches("\\d\\d\\d\\d")) {
					outputArea.append("Please enter a valid port number.\n");
				}
				else if(isConnected) {
					outputArea.append("You are already connected to a server.");
				}
				else {
					address = addressField.getText();
					port = Integer.parseInt(portField.getText());
					username = usernameField.getText();
					usernameField.setEditable(false);
					portField.setEditable(false);
					try {
						s = new Socket(address, port);
						InputStreamReader sr = new InputStreamReader(s.getInputStream());
						reader = new BufferedReader(sr);
						writer = new PrintWriter(s.getOutputStream());
						writer.println(username + "|X|connect");
						writer.flush();
						isConnected = true;
						connectBtn.setEnabled(false);
						dead = false;
					}catch(Exception e) {
						outputArea.append("Connection to server failed.\n");
						usernameField.setEditable(true);
						portField.setEditable(true);
						connectBtn.setEnabled(true);
						isConnected = false;
					}
					ClientThread();
				}
			}
		});
		connectBtn.setBounds(162, 32, 89, 23);
		frmClient.getContentPane().add(connectBtn);
		
		JButton disconnectBtn = new JButton("DISCONNECT");
		disconnectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(isConnected) {
					try {
						writer.println(username + "|X|disconnect");
						writer.flush();
						outputArea.append("You have been disconnected.\n");
						s.close();
						isConnected = false;
						connectBtn.setEnabled(true);
						usernameField.setEditable(true);
						portField.setEditable(true);
						userList.removeAll();
					}catch(Exception e) {
						outputArea.append("Failed to disconnect from server.");
						connectBtn.setEnabled(false);
						usernameField.setEditable(false);
						portField.setEditable(false);
						isConnected = true;
						e.printStackTrace();
					}
				}else {
					connectBtn.setEnabled(true);
					usernameField.setEditable(true);
					portField.setEditable(true);
					outputArea.append("You are not connected to a server.\n");
					isConnected = false;
				}
			}
		});
		disconnectBtn.setBounds(256, 32, 108, 23);
		frmClient.getContentPane().add(disconnectBtn);
		
		Panel panel = new Panel();
		panel.setBounds(340, 61, 134, 406);
		frmClient.getContentPane().add(panel);
		
		userList = new List();
		userList.setEnabled(false);
		userList.setMultipleSelections(false);
		panel.add(userList);
	}
}
