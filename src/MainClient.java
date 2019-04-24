import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;


public class MainClient extends Frame implements java.awt.event.KeyListener  {
	
	public static void main(String[] args) throws IOException {
	String ip = "192.168.43.142";
	Socket sock = new Socket();
	System.err.println("Starting Client...");
	new MainClient("R/C Client", ip);

	}
	
	public HashMap<Integer,Integer> cpressmap= new HashMap<Integer,Integer>();
	public HashMap<Integer,Integer> creleasemap= new HashMap<Integer, Integer>();
	
	public static final int PORT = 7361;
	public static final int CLOSE =0;
	
		
	
	
	Button btnConnect;
	TextField txtIPAddress;
	TextArea messages;
	private Socket socket; 
	private DataOutputStream outStream;
	private TextField txtX;
	private TextField txtY;
	private Button btnDrive;
	public MainClient ( String title, String ip){
		super(title);
		cpressmap.put(87, 1); //W
		cpressmap.put(83, 2); //S
		cpressmap.put(69, 3); //E
		cpressmap.put(65, 4); //A
		cpressmap.put(68, 5);  //D
		cpressmap.put(81, 6); //Q
		creleasemap.put(87, -1); //W
		creleasemap.put(83, -2); //S
		creleasemap.put(69, -3); //E
		creleasemap.put(65, -4); //A
		creleasemap.put(68, -5);  //D
		creleasemap.put(81, -6); //Q
	
		this.setSize(400,300);

		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				System.err.println("Ending Warbird Client");
				disconnect();
				System.exit(0);
			}
		});
		buildGUI(ip);
		this.setVisible(true);
		//btnConnect.addActionListener(new ControlListener());
		
		
	}
	

	public void buildGUI(String ip){
		Panel mainPanel = new Panel (new BorderLayout());
		ControlListener cl = new ControlListener();
		btnConnect = new Button("Connect");
		btnConnect.addKeyListener(this);
		btnConnect.addActionListener(cl);
		txtIPAddress = new TextField(ip, 16);
		
		txtX = new TextField("", 16);
		txtY = new TextField("", 16);
		
		DriveListener d = new DriveListener();
		btnDrive = new Button("Drive");
		btnDrive.addActionListener(d);
		
		messages = new TextArea("status : DISCONNECTED");
		messages.setEditable(false);
		Panel north = new Panel(new FlowLayout (FlowLayout.LEFT));
		north.add(btnConnect);
		north.add(txtIPAddress);
		Panel center = new Panel ( new GridLayout(5,1));
		center.add(new Label("ASD to steer, W-X to move"));
		Panel center4 = new Panel ( new FlowLayout(FlowLayout.LEFT));
		center4.add(messages);
		center.add(center4);
		center.add(txtX);
		center.add(txtY);
		center.add(btnDrive);
		mainPanel.add(north,  "North");
		mainPanel.add(center, "Center");
		this.add(mainPanel);
	}
	
	private void sendCommand(int command){
		//Send coordinates to Server: 
		messages.setText("Status: SENDING command.");
		try{
			outStream.writeInt(command);
		
		}catch(IOException io){
			messages.setText("status: ERROR Problems occurred sending data.");
			
		}
		messages.setText("status: Command SENT. ");
	}
	
	private class DriveListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			int x = Integer.parseInt(txtX.getText());
			int y = Integer.parseInt(txtY.getText());
			
			try {
				outStream.writeInt(7);
				outStream.writeInt(x);
				outStream.writeInt(y);
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
		
		
	}
	
		/**Listener til gui*/
	private class ControlListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			String command = e.getActionCommand();
		if(command.equals("Connect")){
			try {
				socket = new Socket(txtIPAddress.getText(),PORT);
				outStream = new DataOutputStream(socket.getOutputStream());
				messages.setText("status: CONNECTED");
				btnConnect.setLabel("Disconnect");
			}catch(Exception exc){
				messages.setText("status: FAILURE Error establishing connection with server.");
				System.err.println("Error"+ exc);	
				}
			}
		else if (command.equals("Disconnect")){
			disconnect();
		}
	}
	}
	
	public void disconnect(){
		try{
			sendCommand(CLOSE);
			socket.close();
			btnConnect.setLabel("Connect");
			messages.setText("status: DISCONNECTED");
		}catch (Exception exc){
			messages.setText("status: FAILURE ERROR closing connection with server.");
			System.err.println("Error"+ exc);
		}
	}
	
	
	int lastcommand = 0;
	@Override
	public void keyPressed(KeyEvent key) {
		if(key.getKeyCode() == lastcommand){
			return;
		}
		
		lastcommand = key.getKeyCode();
		if(cpressmap.containsKey(key.getKeyCode())){
			System.out.println(key.getKeyCode());
			sendCommand(cpressmap.get(key.getKeyCode()).intValue());
		}
	
		
	}

	@Override
	public void keyReleased(KeyEvent key) {
		lastcommand=0;
		if(creleasemap.containsKey(key.getKeyCode())){
			System.out.println(key.getKeyCode());
			sendCommand(creleasemap.get(key.getKeyCode()).intValue());
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
