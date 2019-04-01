package Server;
import java.awt.EventQueue;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

public class Server {

	private JFrame frmServer;
	private JTextField addressField;
	private JTextField portField;
	private JTextArea outputArea;
	
	private static ServerSocket ss;
	private static Socket s;
	private ArrayList clientOutputStreams;
	private ArrayList<ClientHandler> clients;
	private ArrayList<String> users;
	private String address;
	private int port;
	/**
	 * Launch the application.
	 */
	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server window = new Server();
					window.frmServer.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public class ClientHandler implements Runnable {
		
		BufferedReader reader;
		PrintWriter client;
		Socket s;
		
		public ClientHandler(Socket cs, PrintWriter user) throws Exception{
			client = user;
			try {
				s = cs;
				InputStreamReader isr = new InputStreamReader(s.getInputStream());
				reader = new BufferedReader(isr);
				clients.add(this);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			String stream;
			String[] data;
			boolean dead = false;
			try {
				while(!dead) {
					stream = reader.readLine();
					data = stream.split("\\|");
					for(String a:data) {
						System.out.println(a);
					}
					if(data[2].equals("message")) {
						sendToAll(stream);
					}
					else if(data[2].equals("connect")) {
						sendToAll(data[0] + "|X|connect");
						sendToAll("X|X|listreset");
						outputArea.append(data[0] + " has connected.\n");
						addUser(data[0]);
						for(String s : users) {
							sendToAll(s+"|X|quietRepop");
						}
						dead = false;
					}
					else if(data[2].equals("disconnect")) {
						s.close();
						removeUser(data[0]);
						sendToAll(data[0]+"|X|disconnect");
						sendToAll("X|X|listreset");
						outputArea.append(data[0] + " has disconnected.\n");
						for(String s : users) {
							sendToAll(s+"|X|quietRepop");
						}
						dead = true;
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
				clientOutputStreams.remove(client);
			}
		}
		
	}
	public class ServerStart implements Runnable{
		@Override
		public void run() {
			clientOutputStreams = new ArrayList();
			users = new ArrayList();
			try {
				ss = new ServerSocket(port);
				while(true) {
					s = ss.accept();
					PrintWriter writer = new PrintWriter(s.getOutputStream());
					clientOutputStreams.add(writer);
					Thread listener = new Thread(new ClientHandler(s, writer));
					listener.start();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addUser(String data) {
		users.add(data);
		String[] tempList = new String[(users.size())];
		users.toArray(tempList);
	}
	
	public void removeUser(String data) {
		users.remove(data);
		String[] tempList = new String[(users.size())];
		users.toArray(tempList);
	}
	
	public void sendToAll(String message) {
		Iterator it = clientOutputStreams.iterator();
		
		while(it.hasNext()) {
			try {
				PrintWriter writer = (PrintWriter) it.next();
				writer.println(message);
				writer.flush();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create the application.
	 */
	public Server() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		//Thread starter = new Thread(new ServerStart());
		//starter.start();
		
		frmServer = new JFrame();
		frmServer.setResizable(false);
		frmServer.setTitle("Server");
		frmServer.setBounds(100, 100, 450, 520);
		frmServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmServer.getContentPane().setLayout(null);
		
		//when closing the server, tell all its relatives and die
		frmServer.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				try {
					sendToAll("X|X|close");
					for(ClientHandler cl : clients) {
						cl.s.close();
					}
					ss.close();
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		JLabel addressLabel = new JLabel("Address");
		addressLabel.setBounds(10, 409, 63, 14);
		frmServer.getContentPane().add(addressLabel);
		
		addressField = new JTextField();
		addressField.setEnabled(false);
		addressField.setBounds(65, 406, 97, 20);
		addressField.setEditable(false);
		addressField.setText("127.0.0.1");
		frmServer.getContentPane().add(addressField);
		addressField.setColumns(10);
		
		JLabel portLabel = new JLabel("Port");
		portLabel.setBounds(10, 437, 46, 14);
		frmServer.getContentPane().add(portLabel);
		
		portField = new JTextField();
		portField.setBounds(65, 434, 97, 20);
		frmServer.getContentPane().add(portField);
		portField.setColumns(10);
		
		JButton cleanBtn = new JButton("CLEAR");
		cleanBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outputArea.setText("");
			}
		});
		cleanBtn.setBounds(335, 405, 89, 23);
		frmServer.getContentPane().add(cleanBtn);
		
		outputArea = new JTextArea();
		outputArea.setEditable(false);
		outputArea.setBounds(10, 11, 414, 387);
		frmServer.getContentPane().add(outputArea);
		
		JButton startBtn = new JButton("START");
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				address = addressField.getText();
				port = Integer.parseInt(portField.getText());
				
				if(portField.getText().matches("^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$")) {
					Thread starter = new Thread(new ServerStart());
					starter.start();
					startBtn.setEnabled(false);
					portField.setEditable(false);
					outputArea.append("Server started on port " + port + ".\n");
				}else {
					outputArea.append("Please enter a valid port number.");
				}
			}
		});
		startBtn.setBounds(10, 459, 75, 23);
		frmServer.getContentPane().add(startBtn);
		
		JButton endBtn = new JButton("END");
		endBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sendToAll("X|X|close");
					ss.close();
					Thread.currentThread().interrupt();
					startBtn.setEnabled(true);
					portField.setEditable(true);
					outputArea.append("Server stopped.\n");
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		endBtn.setBounds(87, 459, 75, 23);
		frmServer.getContentPane().add(endBtn);
	}
}
