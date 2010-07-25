package ConnectFourClient;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
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

/**
 * Class represents the main window of the game.
 */
public class MainFrame extends JFrame implements MouseListener , ActionListener , WindowListener{
	
	/**
	 * Defines the message types.
	 */
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

	/**
	 * Constructs the main window.
	 * @param args
	 * @throws IOException
	 */
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
		setSize(500, 300);
		client=new TheClient(args);
		this.addWindowListener(this);
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Opens the signIn window.
	 */
	public void openSignIn()
	{
		LoginWindow signIn=new LoginWindow(this);
		if (!signIn.getSigned())
		{
			this.setVisible(false);
			this.dispose();
		}
	}
	
	/**
	 * Sets the names of the columns.
	 */
	private void setColumnsNames() {
		openGamesColumnsNames= new String[2];
		openGamesColumnsNames[0]= "Player";
		openGamesColumnsNames[1]= "Game Id";
		gamesForWatchColumnsNames= new String [3];
		gamesForWatchColumnsNames[0]= "Player 1";
		gamesForWatchColumnsNames[1]= "Player 2";
		gamesForWatchColumnsNames[2]= "Game Id";
		
	}
	
	/**
	 * Creates the games for join and games for watch panels.
	 */
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
	}
	
	/**
	 * Gets and shows the online game list from the server.
	 */
	@SuppressWarnings("unchecked")
	public void getOnlineGames()
	{
		ArrayList<GameForClient> response= new ArrayList<GameForClient>();
		try {
			response = (ArrayList<GameForClient>)client.sendMessageToServer(ClientServerProtocol.GAMELIST);
		}catch (Exception e) {
			this.showMessageDialog("There was a probem connecting the server: " + e.getMessage(),MsgType.error);
			clearTables();
			return;
		}
		if (response==null)
		{
			this.showMessageDialog("Problem in the server response!",MsgType.error);
			return;
		}
		setUpOnlineGames(response);
		
	}

	/**
	 * Clears the tables of the games.
	 */
	private void clearTables()
	{
		DefaultTableModel model= ((DefaultTableModel)openGames.getModel());
		DefaultTableModel watchModel= ((DefaultTableModel)gamesForWatch.getModel());
	
		while (model.getRowCount()>0)
		{model.removeRow(0);}
		while (watchModel.getRowCount()>0)
		{watchModel.removeRow(0);}
	}
	
	/**
	 * Adds the online games from games arrayList to the grid.
	 * @param games
	 */
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
	
	/**
	 * Creates the main window and opens signIn frame.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new MainFrame(args).openSignIn();
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	
	}
	
	/**
	 * Sends the PLAY command to the server in order to play specific game. 
	 */
	private void joinGameClicked()
	{
		int rowIndex=openGames.getSelectedRow();
		if (rowIndex==-1)
		{
			this.showMessageDialog("Please select game first!", MsgType.error);
			return;
		}
		if (((String)openGames.getValueAt(rowIndex, 0)).equals(client.getClientName()))
		{
			this.showMessageDialog("Unfortunately you cannot play agains yourself!", MsgType.error);
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
				this.showMessageDialog("Cannot connect to this game ,\n Updating game list...", MsgType.error);
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
		} catch (Exception e) {
			this.showMessageDialog("There was a communication problem with the server, please retry...", MsgType.error);
			return;
		}
	}
	
	/**
	 * Send the NEWGAME command to the serve in order to create a new game.
	 */
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
		} catch (Exception e) {
			this.showMessageDialog("There was a communication problem with the server, please retry...", MsgType.error);
			return;
		}
	}
	
	/**
	 * Send the WATCH command to the server in order to watch specific game.
	 */
	private void watchGameClicked()
	{
		int rowIndex=gamesForWatch.getSelectedRow();
		if (rowIndex==-1)
		{
			this.showMessageDialog("Please select game first!", MsgType.error);
			return;
		}
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
			client.HandleEnjoyWatch(client.parseServerResponse(response),(String)gamesForWatch.getValueAt(rowIndex, 0),(String)gamesForWatch.getValueAt(rowIndex, 1),this);
			//after watching is completed , now refresh the gamelist
			getOnlineGames();
		} catch (Exception e) {
			this.showMessageDialog("There was a communication problem with the server, please retry...", MsgType.error);
			return;
		}
	}
	
	/**
	 * Overrides handler of the mouse clicked event.
	 * Calls appropriate function.
	 * @param e
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource()==joinGame)
		{
			joinGameClicked();
		}
		if (e.getSource()==watchGame)
		{
			watchGameClicked();
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
	
	/**
	 * Overrides handler of the actionPerformed event.
	 * Calls appropriate function for a menu item.
	 * @param e
	 */
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
	
	/**
	 * Opens the statistics window and loads the statistics.
	 */
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
	
	/**
	 * Shows a dialog and writes down the message to the log,
	 * corresponding to the message type.
	 * @param msg
	 * @param type
	 */
	public void showMessageDialog(String msg,String type){
		if(type.equals(MsgType.info)){
			client.logger.print_info(msg);
		}
		else{
			client.logger.print_error(msg);
		}
		JOptionPane.showMessageDialog(this,msg);
	}
	
	/**
	 * Overrides handler of the windowActivated event.
	 * @param e
	 */
	@Override
	public void windowActivated(WindowEvent e) {
	}
	
	/**
	 * Overrides handler of the windowClosed event.
	 * @param e
	 */
	@Override
	public void windowClosed(WindowEvent e) {
	}
	
	/**
	 * Overrides handler of the windowClosing event.
	 * @param e
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		client.disconnect();
	}
	
	/**
	 * Overrides handler of the windowDeactivated event.
	 * @param e
	 */
	@Override
	public void windowDeactivated(WindowEvent e) {
	}
	
	/**
	 * Overrides handler of the windowDeiconified event.
	 * @param e
	 */
	@Override
	public void windowDeiconified(WindowEvent e) {
	}
	
	/**
	 * Overrides handler of the windowIconified event.
	 * @param e
	 */
	@Override
	public void windowIconified(WindowEvent e) {
	}
	
	/**
	 * Overrides handler of the windowOpened event.
	 * @param e
	 */
	@Override
	public void windowOpened(WindowEvent e) {
	}

}
