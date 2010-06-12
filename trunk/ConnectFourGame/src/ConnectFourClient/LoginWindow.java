package ConnectFourClient;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginWindow extends JDialog implements MouseListener {
	private JPanel mainPane;
	private JLabel command;
	private JTextField username;
	private JButton enterButton;
	private JPanel detailsGrid;
	private JPasswordField password;
	private JLabel usernameLabel;
	private JLabel passwordLabel;
	private JLabel singUp;
	private JFrame father;
	public LoginWindow(JFrame MainWindow)
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
		new SingUpWindow(this);
		
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