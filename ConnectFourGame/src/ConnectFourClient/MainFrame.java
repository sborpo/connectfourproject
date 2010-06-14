package ConnectFourClient;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainFrame extends JFrame {
	private JMenuBar menuBar;
	private JMenu menu1, menu2;
	private JMenuItem menu1Item1;
	private JMenuItem menu1Item2;
	public TheClient client;
	
	public MainFrame(String [] args)
	{
		super("The Main Client Window");
		menuBar = new JMenuBar();
		//Build the first menu.
		menu1 = new JMenu("FirstMenu");
		menu2= new JMenu("Second Menu");
		menuBar.add(menu1);
		menuBar.add(menu2);
		menu1Item1= new JMenuItem("firstItem");
		menu1Item2= new JMenuItem("firstItem");
		menu1.add(menu1Item1);
		menu1.add(menu1Item2);
		setJMenuBar(menuBar);
		setSize(500, 500);
		client=new TheClient(args);
		setVisible(true);
		new LoginWindow(this);
		

	}
	public static void main(String[] args) {
		new MainFrame(args);
	
	}

}
