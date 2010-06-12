package ConnectFourClient;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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
	public SingUpWindow(JDialog father)
	{
		super(father, "Sign Up Window", true);
		mainPane = new JPanel();
		serversResponsePanel= new JPanel();
		setServersResponsePanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
		command = new JLabel("Please Enter Your Details!");
		setDetailsPane();
		Box upperBox = Box.createHorizontalBox();
		singButton = new JButton("Sign Up");
		upperBox.add(command);
		mainPane.add(upperBox);
		mainPane.add(detailsGrid);
		mainPane.add(singButton);
		mainPane.add(new JSeparator());
		mainPane.add(serversResponsePanel);
		
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPane,BorderLayout.PAGE_START);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
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

	@Override
	public void mouseClicked(MouseEvent e) {
		
		
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
