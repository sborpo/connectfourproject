package ConnectFourClient;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ConnectFourClient.MainFrame.MsgType;
import ConnectFourClient.TheClient.ServerWriteOrReadException;

import theProtocol.ClientServerProtocol;

/**
 * SignUp window class.
 */
public class SingUpWindow extends JDialog implements MouseListener{
	private JPanel mainPane;
	private JPanel serversResponsePanel;
	private JLabel command;
	private JTextField username;
	private JButton singButton;
	private JPanel detailsGrid;
	private JPasswordField password;
	private JPasswordField confirmpassword;
	private JLabel usernameLabel;
	private JLabel passwordLabel;
	private JLabel confirmPasswordLabel;
	private JTextArea log;
	private MainFrame main;
	
	/**
	 * The constructor for class.
	 * @param father
	 * @param main
	 */
	public SingUpWindow(JDialog father,MainFrame main)
	{
		super(father, "Sign Up Window", true);
		this.main=main;
		mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		command = new JLabel("Please Enter Your Details!");
		setDetailsPane();
		Box upperBox = Box.createHorizontalBox();
		singButton = new JButton("Sign Up");
		upperBox.add(command);
		mainPane.add(upperBox);
		mainPane.add(detailsGrid);
		mainPane.add(singButton);

		
		singButton.addMouseListener(this);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPane,BorderLayout.PAGE_START);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * Creates the details pane.
	 */
	private void setDetailsPane()
	{
		usernameLabel= new JLabel("username:");
		passwordLabel = new JLabel("password:");
		confirmPasswordLabel = new JLabel("password confirmation:");
		username = new JTextField();
		password= new JPasswordField();
		confirmpassword= new JPasswordField();
		detailsGrid = new JPanel(new GridLayout(0, 2));
		detailsGrid.add(usernameLabel);
		detailsGrid.add(username);
		detailsGrid.add(passwordLabel);
		detailsGrid.add(password);
		detailsGrid.add(confirmPasswordLabel);
		detailsGrid.add(confirmpassword);
	}
	
	/**
	 * Creates a panel for server responses presentation.
	 */
	public void setServersResponsePanel()
	{
		serversResponsePanel.setLayout(new BoxLayout(serversResponsePanel, BoxLayout.PAGE_AXIS));
		Box x = Box.createHorizontalBox();
		JLabel label = new JLabel("Server's Response Log");
		label.setAlignmentX(RIGHT_ALIGNMENT);
		x.add(label);
		log = new JTextArea(5, 0);
		serversResponsePanel.add(x);
		serversResponsePanel.add(log);
	}

	/**
	 * Overrides handler of the mouseClicked event.
	 * Activates signUp process.
	 * @param e
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		
		if (username.getText().equals(""))
		{
			main.showMessageDialog("The username field is empty!",MsgType.error);
			return;
		}
		if (password.getText().equals(""))
		{
			main.showMessageDialog("The password field is empty!",MsgType.error);
			return;
		}
		if (!(password.getText().equals(confirmpassword.getText())))
		{
			main.showMessageDialog("The password and the confirmation password doesn't match!",MsgType.error);
			return;
		}
		try {
			String response=(String)main.client.sendMessageToServer(ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.SIGNUP,
																									                username.getText(),
																									                password.getText().toString()}));
			if (main.client.parseServerResponse(response)==null)
			{
				main.showMessageDialog("Internal Error: The Server Didn't understand the sent message",MsgType.error);
				return;
			}
			if (main.client.parseServerResponse(response)[0].equals(ClientServerProtocol.USERALREADYEXISTS))
			{
				main.showMessageDialog("The username that you have entered already exists in the system \n please choose other username",MsgType.error);
				return;
			}
			if (main.client.parseServerResponse(response)[0].equals(ClientServerProtocol.OK))
			{
				main.showMessageDialog("Your username has been added successfully \n You can sign in right now.",MsgType.info);
				this.setVisible(false);
				return;
			}
			main.showMessageDialog("The server had a problem serving your request , please retry.",MsgType.error);
			return;
		} catch (Exception ex) {
			main.showMessageDialog("A problem with server connection",MsgType.error);
			return;
		}	
	}

	/**
	 * Overrides handler of the mouseEntered event.
	 * @param e
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Overrides handler of the mouseExited event.
	 * @param e
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Overrides handler of the mousePressed event.
	 * @param e
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * Overrides handler of the mouseReleased event.
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	
}
