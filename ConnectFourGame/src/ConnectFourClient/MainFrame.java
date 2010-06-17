package ConnectFourClient;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import ConnectFourServer.StamClass;

import theProtocol.ClientServerProtocol;
import gameManager.*;
import gameManager.Player.Color;
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
    
    public void removeAllRows()
    {
    	int rows=data.length;
    	data=new Object[rows][columnNames.length];	
    }
	
}

public class MainFrame extends JFrame implements MouseListener{
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
		gamesPanel = new JPanel(new GridLayout(2, 2));
		JPanel left = new JPanel(); JPanel right= new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
		Box l = Box.createHorizontalBox();
		Box r = Box.createHorizontalBox();
		l.add(new JLabel("Open Games"));
		r.add(new JLabel("Games For Watch"));
		left.add(l); right.add(r);
		openGames= new JTable(new DefaultTableModel());
		for (String colName : openGamesColumnsNames) {
			((DefaultTableModel)openGames.getModel()).addColumn(colName);
		}
		gamesForWatch= new JTable(new DefaultTableModel());
		for (String colName : gamesForWatchColumnsNames) {
			((DefaultTableModel)gamesForWatch.getModel()).addColumn(colName);
		}
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
		JButton addGame= new JButton("Create Game");
		addGame.addMouseListener(this);
		gamesPanel.add(addGame);
	}
	
	@SuppressWarnings("unchecked")
	public void getOnlineGames()
	{
		ArrayList<StamClass> response;
		try {
			response = (ArrayList<StamClass>)client.sendMessageToServer(ClientServerProtocol.GAMELIST);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,"Internal Error: The Server Didn't understand the sent message");
			return;
		}
		if (response==null)
		{
			//TODO problem with syntax , server doesn't understand
			JOptionPane.showMessageDialog(null,"Internal Error: The Server Didn't understand the sent message");
			return;
		}
		setUpOnlineGames(response);
		
	}
	
	public void setUpOnlineGames(ArrayList<StamClass> games)
	{
		DefaultTableModel model= ((DefaultTableModel)openGames.getModel());
		while (model.getRowCount()>0)
		{model.removeRow(0);}
		for (StamClass game : games) {
			
				Object [] arr= new Object[3];
				arr[0]=game.getX().toString();
				arr[1]="";
				arr[2]=game.getY().toString();
				model.addRow(arr);
		}	
	}
	
	public static void main(String[] args) {
		new MainFrame(args);
	
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		try {
			String response =(String)client.sendMessageToServer(ClientServerProtocol.NEWGAME+" "+String.valueOf(client.getGamePort())+" "+client.getClientName());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
