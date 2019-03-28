package Client;
import java.awt.EventQueue;
import java.io.*;
import java.net.Socket;
import java.util.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Panel;
import java.awt.List;

public class Client {

	private JFrame frame;
	private JTextField inputField;
	private JTextField addressField;
	private JTextField usernameField;
	private JTextField portField;
	private JTextArea outputArea;
	private List userList;
	
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
				while((stream = reader.readLine()) != null) {
					data = stream.split("\\|");
					if(data[2].equals("message")) {
						outputArea.append(data[0] + ": " + data[1] + "\n");
						outputArea.setCaretPosition(outputArea.getDocument().getLength());
					}
					else if(data[2].equals("connect")) {
						addUser(data[0]);
					}
					else if(data[2].equals("disconnect")) {
						removeUser(data[0]);
					}
					else if(data[2].equals("listreset")) {
						removeAllUsers();
					}
					else if(data[2].equals("quietRepop")) {
						addUser(data[0]);
					}
					else if(data[2].equals("die")) {
						frame.dispose();
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
		frame = new JFrame();
		frame.setBounds(100, 100, 500, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		//when a client closes, DON'T panic, still die
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				writer.println(username + "|X|disconnect");
				writer.flush();
				outputArea.append("Disconnected.\n");
				isConnected = false;
			}
		});
		
		
		JLabel usernameLab = new JLabel("Username");
		usernameLab.setBounds(167, 11, 77, 14);
		frame.getContentPane().add(usernameLab);
		
		outputArea = new JTextArea();
		outputArea.setLineWrap(true);
		outputArea.setBounds(10, 61, 318, 406);
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
					writer.println(username + "|" + inputField.getText() + "|message");
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
		usernameField.setBounds(228, 8, 122, 20);
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
					outputArea.append("You all already connected to server.\n");
				}else {
					try {
						s = new Socket(address, port);
						InputStreamReader sr = new InputStreamReader(s.getInputStream());
						reader = new BufferedReader(sr);
						writer = new PrintWriter(s.getOutputStream());
						writer.println(username + "|has connected.|connect");
						writer.flush();
						isConnected = true;
					}catch(Exception e) {
						outputArea.append("Connection to server failed.\n");
					}
					ClientThread();
				}
			}
		});
		connectBtn.setBounds(163, 32, 81, 23);
		frame.getContentPane().add(connectBtn);
		
		JButton disconnectBtn = new JButton("DISCONNECT");
		disconnectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					writer.println(username + "|X|disconnect");
					writer.flush();
					outputArea.append("Disconnected.\n");
					s.close();
					isConnected = false;
					usernameField.setEditable(true);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		disconnectBtn.setBounds(250, 32, 100, 23);
		frame.getContentPane().add(disconnectBtn);
		
		Panel panel = new Panel();
		panel.setBounds(340, 61, 134, 406);
		frame.getContentPane().add(panel);
		
		userList = new List();
		userList.setMultipleSelections(false);
		panel.add(userList);
	}
}
