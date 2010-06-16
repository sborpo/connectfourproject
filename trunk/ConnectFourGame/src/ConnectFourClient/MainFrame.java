package ConnectFourClient;

import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

class TableModel extends AbstractTableModel
{
	private String [] columnNames;
	private Object [][] data;
	
	public TableModel(int rows,String [] names)
	{
		columnNames=names;
		data= new Object[rows][columnNames.length];
	}
	@Override
	public int getColumnCount() {
		  return columnNames.length;
	}

	@Override
	public int getRowCount() {
		 return data.length;
	}
	
	public String getColumnName(int col) {
		return columnNames[col];
    }

	   public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }

	@Override
	public Object getValueAt(int row, int col) {
		return data[row][col];
	}
	
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
    }
	
}

public class MainFrame extends JFrame {
	private JMenuBar menuBar;
	private JMenu menu1, menu2;
	private JMenuItem menu1Item1;
	private JMenuItem menu1Item2;
	private String [] openGamesColumnsNames;
	private String[] gamesForWatchColumnsNames;
	private JPanel gamesPanel;
	private JTable openGames;
	private JTable gamesForWatch;
	private JButton joinGame;
	private JButton watchGame;
	public TheClient client;
	
	public MainFrame(String [] args)
	{
		super("The Main Client Window");
		setColumnsNames();
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
		setGamesPanel();
		this.add(gamesPanel);
		setSize(500, 500);
		client=new TheClient(args);
		setVisible(true);
		new LoginWindow(this);
		

	}
	private void setColumnsNames() {
		openGamesColumnsNames= new String[3];
		openGamesColumnsNames[0]= "Player";
		openGamesColumnsNames[1]= "Rank";
		openGamesColumnsNames[2]= "Game Id";
		gamesForWatchColumnsNames= new String [5];
		gamesForWatchColumnsNames[0]= "Player 1";
		gamesForWatchColumnsNames[1]= "Rank";
		gamesForWatchColumnsNames[2]= "Player 2";
		gamesForWatchColumnsNames[3] = "Rank";
		gamesForWatchColumnsNames[4]= "Game Id";
		
	}
	private void setGamesPanel() {
		gamesPanel = new JPanel(new GridLayout(1, 2));
		JPanel left = new JPanel(); JPanel right= new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
		Box l = Box.createHorizontalBox();
		Box r = Box.createHorizontalBox();
		l.add(new JLabel("Open Games"));
		r.add(new JLabel("Games For Watch"));
		left.add(l); right.add(r);
		openGames= new JTable(new TableModel(0,openGamesColumnsNames));
		gamesForWatch= new JTable(new TableModel(0,gamesForWatchColumnsNames));
		openGames.setFillsViewportHeight(true);
		gamesForWatch.setFillsViewportHeight(true);
		JScrollPane openTablePane= new JScrollPane(openGames);
		JScrollPane gamesForWatchTablePane= new JScrollPane(gamesForWatch);
		l= Box.createHorizontalBox();
		r = Box.createHorizontalBox();
		l.add(openTablePane);
		r.add(gamesForWatchTablePane);
		left.add(l); right.add(r);
		joinGame= new JButton("Join Game");
		watchGame= new JButton("Watch Game");
		l= Box.createHorizontalBox();
		r = Box.createHorizontalBox();
		l.add(joinGame);
		r.add(watchGame);
		left.add(l); right.add(r);
		gamesPanel.add(left);
		gamesPanel.add(right);
	}
	public static void main(String[] args) {
		new MainFrame(args);
	
	}

}
