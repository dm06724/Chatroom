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
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

public class Server {

	private JFrame frame;
	private JTextField addressField;
	private JTextField portField;
	private JTextArea outputArea;
	
	private static ServerSocket ss;
	private static Socket s;
	private ArrayList clientOutputStreams;
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
					window.frame.setVisible(true);
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
		
		public ClientHandler(Socket cs, PrintWriter user) {
			client = user;
			try {
				s = cs;
				InputStreamReader isr = new InputStreamReader(s.getInputStream());
				reader = new BufferedReader(isr);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			String message;
			String[] data;
			try {
				while((message = reader.readLine()) != null) {
					data = message.split("|");
					sendToAll(data[0] + "|" + data[1]);
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
					outputArea.append("\nNew connection.");
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
		Thread starter = new Thread(new ServerStart());
		starter.start();
		
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 532);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel addressLabel = new JLabel("Address");
		addressLabel.setBounds(10, 409, 46, 14);
		frame.getContentPane().add(addressLabel);
		
		addressField = new JTextField();
		addressField.setBounds(65, 406, 97, 20);
		addressField.setEnabled(false);
		addressField.setEditable(false);
		addressField.setText("127.0.0.1");
		frame.getContentPane().add(addressField);
		addressField.setColumns(10);
		
		JLabel portLabel = new JLabel("Port");
		portLabel.setBounds(10, 437, 46, 14);
		frame.getContentPane().add(portLabel);
		
		portField = new JTextField();
		portField.setBounds(65, 434, 97, 20);
		frame.getContentPane().add(portField);
		portField.setColumns(10);
		
		JButton cleanBtn = new JButton("CLEAR");
		cleanBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outputArea.setText("");
			}
		});
		cleanBtn.setBounds(335, 405, 89, 23);
		frame.getContentPane().add(cleanBtn);
		
		outputArea = new JTextArea();
		outputArea.setBounds(10, 11, 414, 387);
		frame.getContentPane().add(outputArea);
		
		JButton startBtn = new JButton("START");
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String temp = portField.getText();
				address = addressField.getText();
				port = Integer.parseInt(portField.getText());
				
				if(!(portField.getText().equals(""))) {
					Thread starter = new Thread(new ServerStart());
					starter.start();
					
					outputArea.append("Server started.");
				}
			}
		});
		startBtn.setBounds(10, 459, 75, 23);
		frame.getContentPane().add(startBtn);
		
		JButton endBtn = new JButton("END");
		endBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread.currentThread().interrupt();
				outputArea.append("Server stopped.");
			}
		});
		endBtn.setBounds(87, 459, 75, 23);
		frame.getContentPane().add(endBtn);
	}
}
