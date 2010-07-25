package ConnectFourClient;

import java.awt.BorderLayout;
import theProtocol.*;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ConnectFourClient.MainFrame.MsgType;
import ConnectFourClient.TheClient.ServerWriteOrReadException;

import common.UnhandledReports.FileChanged;

/**
 * The Login Window class.
 */
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
	private boolean signed;
	
	/**
	 * Gets the value of the flag representing if
	 * the user is signed in.
	 * @return signed
	 */
	public boolean getSigned()
	{
		return signed;
	}
	
	/**
	 * The constructor of login window.
	 * @param MainWindow
	 */
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
			  }
		});
			
		signed=false;
		setSize(300, 150);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * Creates the pane for user inputs.
	 */
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

	/**
	 * Overrides the mouse clicked handler.
	 * Signing in if the "Sign in" button pushed
	 * or opens the sign up  window.
	 * @param e
	 */
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
					father.showMessageDialog("Internal Error: The Server bad response",MsgType.error);
					return;
				}
				if (father.client.parseServerResponse(response)[0].equals(ClientServerProtocol.USERNOTEXISTS))
				{
					father.showMessageDialog("The username not exists or wrong password \n Please, try again or Sign Up",MsgType.error);
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
				signed=true;
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

	/**
	 * Overrides mouseEntered event handler.
	 * @param e
	 */
	@Override
	public void mouseEntered(MouseEvent e) {		
	}

	/**
	 * Overrides mouseExited event handler.
	 * @param e
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Overrides mousePressed event handler.
	 * @param e
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * Overrides mouseReleased event handler.
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}




	
}
