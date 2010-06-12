package ConnectFourClient;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginWindow extends JFrame {
	private JPanel mainPane;
	private JLabel command;
	private JTextField username;
	private JButton enterButton;
	private JPanel detailsGrid;
	private JPasswordField password;
	private JLabel usernameLabel;
	private JLabel passwordLabel;
	private JLabel singUp;
	public LoginWindow()
	{
		super("Authentication Window");
		mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		command = new JLabel("Please Enter Your Details!");
		singUp = new JLabel("Sign Up");
		setDetailsPane();
		Box upperBox = Box.createHorizontalBox();
		upperBox.add(command);
		mainPane.add(upperBox);
		mainPane.add(detailsGrid);
		mainPane.add(enterButton);
		
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPane,BorderLayout.PAGE_START);
		getContentPane().add(singUp,BorderLayout.LINE_END);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 150);
		setVisible(true);
	}
	
	private void setDetailsPane()
	{
		usernameLabel= new JLabel("username:");
		passwordLabel = new JLabel("password:");
		username = new JTextField();
		password= new JPasswordField();
		enterButton = new JButton("Sign In");
		detailsGrid = new JPanel(new GridLayout(2, 2));
		detailsGrid.add(usernameLabel);
		detailsGrid.add(username);
		detailsGrid.add(passwordLabel);
		detailsGrid.add(password);
	}

	
}
