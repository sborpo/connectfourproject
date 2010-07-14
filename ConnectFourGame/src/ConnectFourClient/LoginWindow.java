package ConnectFourClient;

import java.awt.BorderLayout;
import theProtocol.*;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ConnectFourClient.MainFrame.MsgType;
import ConnectFourClient.TheClient.ServerWriteOrReadException;

import common.UnhandledReports.FileChanged;

public class LoginWindow extends JDialog implements MouseListener  {
	private JPanel mainPane;
	private JLabel command;
	private JTextField username;
	private JButton enterButton;
	private JPanel detailsGrid;
	private JPasswordField password;
	private JLabel usernameLabel;
	private JLabel passwordLabel;
	private JLabel singUp;
	private MainFrame father;
	public LoginWindow(MainFrame MainWindow)
	{

		super(MainWindow, "Authentication Window", true);
		father= MainWindow;
		mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		command = new JLabel("Please Enter Your Details!");
		singUp = new JLabel("Sign Up");
		setDetailsPane();
		Box upperBox = Box.createHorizontalBox();
		enterButton = new JButton("Sign In");
		
		upperBox.add(command);
		mainPane.add(upperBox);
		mainPane.add(detailsGrid);
		mainPane.add(enterButton);
		
		singUp.addMouseListener(this);
		enterButton.addMouseListener(this);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPane,BorderLayout.PAGE_START);
		getContentPane().add(singUp,BorderLayout.LINE_END);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			 public void windowClosed(WindowEvent e)
			  {
			   father.setVisible(false);
			  }
		});
			
		setSize(300, 150);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void setDetailsPane()
	{	
		usernameLabel= new JLabel("username:");
		passwordLabel = new JLabel("password:");
		username = new JTextField();
		password= new JPasswordField();
		detailsGrid = new JPanel(new GridLayout(2, 2));
		detailsGrid.add(usernameLabel);
		detailsGrid.add(username);
		detailsGrid.add(passwordLabel);
		detailsGrid.add(password);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource()==enterButton)
		{
			try {
				String response=(String)father.client.sendMessageToServer(ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.MEETME,
																												String.valueOf(father.client.getClientAlivePort()),
																												username.getText(),
																												password.getText()}));
				if (father.client.parseServerResponse(response)==null)
				{
					//TODO problem with syntax , server doesn't understand
					father.showMessageDialog("Internal Error: The Server Didn't understand the sent message",MsgType.error);
					return;
				}
				if (father.client.parseServerResponse(response)[0].equals(ClientServerProtocol.USERNOTEXISTS))
				{
					father.showMessageDialog("The username that you have typed doesn't exists \n Please Sign Up",MsgType.error);
					return;
				}
				//else log the user into the main window
				father.client.handleNICETM(father.client.parseServerResponse(response));
				
				try{
					boolean res=father.client.reportUnhandeledReports();
					if (res)
					{
						father.showMessageDialog("Successfully reported to the server the unreported games.",MsgType.info);
					}
				}
				catch (FileChanged ex) {
					father.showMessageDialog("Someone changed the report file",MsgType.error);
					return;
				}
				catch (IOException ex) {
					father.showMessageDialog("Problem sending the report file",MsgType.error);
					return;
				}
				
				father.getOnlineGames();
				father.setTitle("Connected As: "+username.getText());
				this.setVisible(false);
			} catch (IOException e1) {
				father.showMessageDialog("A problem with server connection",MsgType.error);
				return;
			} catch (ServerWriteOrReadException ex) {
				father.showMessageDialog("A problem with server connection",MsgType.error);
				return;
			} 
		}
		else{
			new SingUpWindow(this,father);
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}




	
}
