package ConnectFourClient;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;

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
import javax.swing.table.DefaultTableModel;

import common.StatsReport;

import theProtocol.ClientServerProtocol;
import ConnectFourClient.TheClient.ServerWriteOrReadException;
import ConnectFourServer.GameForClient;


public class MainFrame extends JFrame implements MouseListener , ActionListener , WindowListener{
	public static class MsgType{
		public static final String error = "ERROR";
		public static final String info = "INFO";
	}
	
	private JMenuBar menuBar;
	private JMenu menu1;
	private JMenuItem menu1Item1;
	private JMenuItem menu1Item2;
	private JMenuItem menu1Item3;
	private String[] openGamesColumnsNames;
	private String[] gamesForWatchColumnsNames;
	private JPanel gamesPanel;
	private JTable openGames;
	private JTable gamesForWatch;
	private JButton joinGame;
	private JButton watchGame;
	public TheClient client;
	//private JButton addGame;
	
	

	
	public MainFrame(String [] args) throws IOException
	{
		super("The Main Client Window");
		setColumnsNames();
		menuBar = new JMenuBar();
		//Build the first menu.
		menu1 = new JMenu("Options");
		menuBar.add(menu1);
		menu1Item1= new JMenuItem("Refresh Game List");
		menu1Item1.addActionListener(this);
		menu1Item2= new JMenuItem("Game Statistics");
		menu1Item2.addActionListener(this);
		menu1Item3= new JMenuItem("Create New Game");
		menu1Item3.addActionListener(this);
		menu1.add(menu1Item1);
		menu1.add(menu1Item2);
		menu1.add(menu1Item3);
		setJMenuBar(menuBar);
		setGamesPanel();
		this.add(gamesPanel);
		setSize(500, 500);
		client=new TheClient(args);
		this.addWindowListener(this);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void openSignIn()
	{
		LoginWindow signIn=new LoginWindow(this);
		if (!signIn.getSigned())
		{
			this.setVisible(false);
			this.dispose();
		}
	}
	private void setColumnsNames() {
		openGamesColumnsNames= new String[2];
		openGamesColumnsNames[0]= "Player";
		openGamesColumnsNames[1]= "Game Id";
		gamesForWatchColumnsNames= new String [3];
		gamesForWatchColumnsNames[0]= "Player 1";
		gamesForWatchColumnsNames[1]= "Player 2";
		gamesForWatchColumnsNames[2]= "Game Id";
		
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
		joinGame.addMouseListener(this);
		watchGame= new JButton("Watch Game");
		watchGame.addMouseListener(this);
		l= Box.createHorizontalBox();
		r = Box.createHorizontalBox();
		l.add(joinGame);
		r.add(watchGame);
		left.add(l); right.add(r);
		gamesPanel.add(left);
		gamesPanel.add(right);
		
//		addGame= new JButton("Create Game");
//		addGame.addMouseListener(this);
//		Box b = Box.createHorizontalBox();
//		b.add( addGame);
//		gamesPanel.add(b);
	}
	
	@SuppressWarnings("unchecked")
	public void getOnlineGames()
	{
		ArrayList<GameForClient> response= new ArrayList<GameForClient>();
		try {
			response = (ArrayList<GameForClient>)client.sendMessageToServer(ClientServerProtocol.GAMELIST);
		} catch (IOException e) {
			this.showMessageDialog("There was a probem connecting the server",MsgType.error);
			return;
		} catch (ServerWriteOrReadException e) {
			this.showMessageDialog("There was a probem connecting the server",MsgType.error);
			clearTables();
			return;
		}
		if (response==null)
		{
			//TODO problem with syntax , server doesn't understand
			this.showMessageDialog("Internal Error: The Server Didn't understand the sent message",MsgType.error);
			return;
		}
		setUpOnlineGames(response);
		
	}

	private void clearTables()
	{
		DefaultTableModel model= ((DefaultTableModel)openGames.getModel());
		DefaultTableModel watchModel= ((DefaultTableModel)gamesForWatch.getModel());
	
		while (model.getRowCount()>0)
		{model.removeRow(0);}
		while (watchModel.getRowCount()>0)
		{watchModel.removeRow(0);}
	}
	public void setUpOnlineGames(ArrayList<GameForClient> games)
	{
		clearTables();
		DefaultTableModel model= ((DefaultTableModel)openGames.getModel());
		DefaultTableModel watchModel= ((DefaultTableModel)gamesForWatch.getModel());
		for (GameForClient game : games) {
				if (game.isOpen())
				{
					if (game.getPlayerOneName().equals(this.client.getClientName()))
					{
						continue;
					}
					Object [] arr= new Object[2];
					arr[0]=game.getPlayerOneName().toString();
					arr[1]=game.getGameId().toString();
					model.addRow(arr);
				}
				else
				{
					if ((game.getPlayerOneName().equals(this.client.getClientName())) || (game.getPlayerTwoName().equals(this.client.getClientName())))
					{
						continue;
					}
					Object [] arr= new Object[3];
					arr[0]=game.getPlayerOneName().toString();
					arr[1]=game.getPlayerTwoName();
					arr[2]=game.getGameId().toString();
					watchModel.addRow(arr);
				}
				
		}	
	}
	
	public static void main(String[] args) {
		try {
			new MainFrame(args).openSignIn();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: " + e.getMessage());
		}
	
	}
	
	private void joinGameClicked()
	{
		int rowIndex=openGames.getSelectedRow();
		if (((String)openGames.getValueAt(rowIndex, 0)).equals(client.getClientName()))
		{
			JOptionPane.showMessageDialog(null,"Unfortunately you cannot play agains yourself!");
			return;
		}
		try {
			String response =(String)client.sendMessageToServer(ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.PLAY,
																								  String.valueOf(client.getGamePort()),
																								  String.valueOf(client.getTransmitWaiterPort()),
																								  (String)openGames.getValueAt(rowIndex, 1),
																								  client.getClientName(),
																								  client.getPassword()}));
			if(response == null){
				this.showMessageDialog("There was an error in server's response!", MsgType.error);
				return;
			}
			
			if (client.parseServerResponse(response)==null)
			{
				this.showMessageDialog("There was an error in server's response!", MsgType.error);
				return;
			}
			if (client.parseServerResponse(response)[0].equals(ClientServerProtocol.SERVPROB))
			{
				this.showMessageDialog("There was a server internal error", MsgType.error);
				return;
			}
			if (client.parseServerResponse(response)[0].equals(ClientServerProtocol.DENIED))
			{
				this.showMessageDialog("This game doesn't exists or someone else\n is connected ,\n Updating game list...", MsgType.error);
				getOnlineGames();
				return;
			}
			if (client.parseServerResponse(response)[0].equals(ClientServerProtocol.GOGOGO))
			{
				this.showMessageDialog("You have joined a game aggaints : "+openGames.getValueAt(rowIndex, 0)+" ,Good Luck!", MsgType.info);
				client.HandleGoGoGoGUI(client.parseServerResponse(response),this);
				getOnlineGames();
				return;
			}
		} catch (IOException e1) {
			this.showMessageDialog("There was a communication problem with the server, please retry...", MsgType.error);
		} catch (ServerWriteOrReadException e) {
			this.showMessageDialog("There was a communication problem with the server, please retry...", MsgType.error);
			return;
		}
	}
	
	private void newGameClicked()
	{
		try {
			String response =(String)client.sendMessageToServer(ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.NEWGAME,
																												String.valueOf(client.getGamePort()),
																												String.valueOf(client.getTransmitWaiterPort()),
																												client.getClientName(),
																												client.getPassword()}));
			if(response == null){
				this.showMessageDialog("There was an error in server's response!", MsgType.error);
				return;
			}
			
			if (client.parseServerResponse(response)==null)
			{
				this.showMessageDialog("There was an error in server's response!", MsgType.error);
				return;
			}
			if (client.parseServerResponse(response)[0].equals(ClientServerProtocol.SERVPROB))
			{
				this.showMessageDialog("There was a server internal error", MsgType.error);
				return;
			}
			client.HandleGameGUI(client.parseServerResponse(response),this);
		} catch (IOException e1) {
			this.showMessageDialog("There was a communication problem with the server, please retry...", MsgType.error);
		} catch (ServerWriteOrReadException e) {
			this.showMessageDialog("There was a communication problem with the server, please retry...", MsgType.error);
			return;
		}
	}
	
	private void watchGameClicked()
	{
		int rowIndex=gamesForWatch.getSelectedRow();
		try {
			String response =(String)client.sendMessageToServer(ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.WATCH,
																												String.valueOf(client.getWatchPort()),
																												(String)gamesForWatch.getValueAt(rowIndex, 2),
																												client.getClientName(),
																												client.getPassword()}));
			if(response == null){
				this.showMessageDialog("There was an error in server's response!", MsgType.error);
				return;
			}
			
			if (client.parseServerResponse(response)==null)
			{
				this.showMessageDialog("There was an error in server's response!", MsgType.error);
				return;
			}
			if (client.parseServerResponse(response)[0].equals(ClientServerProtocol.SERVPROB))
			{
				this.showMessageDialog("There was a server internal error", MsgType.error);
				return;
			}
			if (client.parseServerResponse(response)[0].equals(ClientServerProtocol.DENIED))
			{
				this.showMessageDialog("This game cannot be watched now, try again or another game...", MsgType.error);
				getOnlineGames();
				return;
			}
			client.HandleEnjoyWatch(client.parseServerResponse(response),(String)gamesForWatch.getValueAt(rowIndex, 0),(String)gamesForWatch.getValueAt(rowIndex, 2),this);
			//after watching is completed , now refresh the gamelist
			getOnlineGames();
		} catch (IOException e1) {
			this.showMessageDialog("There was a communication problem with the server, please retry...", MsgType.error);
		} catch (ServerWriteOrReadException e) {
			this.showMessageDialog("There was a communication problem with the server, please retry...", MsgType.error);
			return;
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource()==joinGame)
		{
			joinGameClicked();
		}
//		if (e.getSource()==addGame)
//		{
//			newGameClicked();
//		}
		if (e.getSource()==watchGame)
		{
			watchGameClicked();
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
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==menu1Item1)
		{
			getOnlineGames();
			return;
		}
		if (e.getSource()==menu1Item2)
		{
			openStatsWindow();
			return;
		}
		if (e.getSource()==menu1Item3)
		{
			newGameClicked();
		}
		
	}
	
	private void openStatsWindow() {

		StatsReport response= new StatsReport();
		try {
			response = (StatsReport)client.sendMessageToServer(ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.STATS_REQUEST,
																												client.getClientName()}));
		} catch (IOException e) {
			this.showMessageDialog("Error while sending message to server", MsgType.error);
			return;
		} catch (ServerWriteOrReadException e) {
			this.showMessageDialog("There was a probem connecting the server", MsgType.error);
			clearTables();
			return;
		}
		if (response==null)
		{
			this.showMessageDialog("There are no statistics available", MsgType.error);
			return;
		}
		new StatsWindow(response);
	}
	
	
	public void showMessageDialog(String msg,String type){
		if(type.equals(MsgType.info)){
			client.logger.print_info(msg);
		}
		else{
			client.logger.print_error(msg);
		}
		JOptionPane.showMessageDialog(this,msg);
	}
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowClosing(WindowEvent e) {
		client.disconnect();
		
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

}
