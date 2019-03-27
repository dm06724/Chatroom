package Client;
import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Client {

	private JFrame frame;
	private static Socket s;
	private static DataInputStream din;
	private static DataOutputStream dout;
	private JTextField inputField;
	private static JTextArea msgbox = new JTextArea();
	
	
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
		
		String msgin = "";
		
		try {
			s = new Socket("127.0.0.1", 2001);
			din = new DataInputStream(s.getInputStream());
			dout = new DataOutputStream(s.getOutputStream());
			
			while(!msgin.equals("exit")) {
				msgin = din.readUTF();
				msgbox.setText(msgbox.getText().trim() + "\n Server:" + msgin);
			}
		}catch(Exception e) {
			
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
		frame.setBounds(100, 100, 453, 452);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		msgbox.setBounds(10, 11, 417, 360);
		frame.getContentPane().add(msgbox);
		
		inputField = new JTextField();
		inputField.setBounds(10, 382, 318, 20);
		frame.getContentPane().add(inputField);
		inputField.setColumns(10);
		
		JButton msgSend = new JButton("SEND");
		msgSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String msgout = "";
					msgout = inputField.getText().trim();
					dout.writeUTF(msgout);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		msgSend.setBounds(338, 381, 89, 23);
		frame.getContentPane().add(msgSend);
	}

}
