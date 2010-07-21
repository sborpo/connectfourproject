package ConnectFourClient;

import gameManager.BoardGUI;
import gameManager.Game;
import gameManager.GameGUI;
import gameManager.Player;
import gameManager.Board.IllegalMove;
import gameManager.Player.Color;

import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import common.Timer.TimeOutEvent;

import ConnectFourClient.MainFrame.MsgType;

import theProtocol.ClientServerProtocol;

/**
 * Class represents the Watcher entity and his GUI.
 */
public class GameWatcher extends GameGUI implements Runnable{

	static public class GameEndedException extends Exception{};
	private static final String STOP_WATCH = "Stop watching!"; 
	private ServerSocket Isocket=null;
	private Socket watchSocket = null;
	private BufferedReader watcherIn = null;
	private Thread watcher= null;
	private String redPlayer;
	private String bluePlayer;
	private boolean watching;
	private JButton stopWatch;
	private boolean timeUpdated;
	
	/**
	 * Constructs a new game watcher GUI.
	 * @param client
	 * @param redPlayer
	 * @param bluePlayer
	 * @param mainFrame
	 */
	public GameWatcher(TheClient client,String redPlayer,String bluePlayer,MainFrame mainFrame)
	{	
		this.mainFrame = mainFrame;
		this.redPlayer=redPlayer;
		this.bluePlayer=bluePlayer;
		red = new Player(Player.Color.RED,redPlayer);
		blue = new Player(Player.Color.BLUE,bluePlayer);
		gameBoard = new BoardGUI(this);
		theClient=client;
		this.watcherIn = null;
		Box [] arr= new Box[4];
		arr[0]=createUserNamesBox();
		arr[1]= createGridsBox();
		arr[2]=createConsolseBox();
		arr[3]=createStopWatchButton();
		this.addWindowListener(this);
		AdjustGUIView(arr);	
		connAs1.setText(redPlayer+"  ");
		connAs1.setForeground(java.awt.Color.RED);
		connAs2.setText("  "+bluePlayer);
		connAs2.setForeground(java.awt.Color.BLUE);
		timerBoxContainer = Box.createHorizontalBox();
		watching = false;
		timeUpdated = false;
		setLocationRelativeTo(null);
	}
	
	/**
	 * Creates a box containing STOP_WATCH button.
	 * @return stopWatchBox
	 */
	private Box createStopWatchButton(){
		Box stopWatchBox = Box.createHorizontalBox();
		stopWatch= new JButton(GameWatcher.STOP_WATCH);
		stopWatch.setName(GameWatcher.STOP_WATCH);
		stopWatch.setHorizontalAlignment(SwingConstants.RIGHT);
		stopWatchBox.add(stopWatch);
		stopWatch.setEnabled(true);
		stopWatch.addMouseListener(this);
		return stopWatchBox;
	}
	
	/**
	 * Main thread function - running GUI, showing each move.
	 */
	@Override
	public void run() {
		theClient.logger.print_info("Game Watcher Is Running! ");
		try{
			watching = true;
			ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
			Isocket = new ServerSocket(theClient.getWatchPort());
			watchSocket = Isocket.accept();
			watcherIn =  new BufferedReader(new InputStreamReader(watchSocket.getInputStream()));
			//set the timeout to read
			watchSocket.setSoTimeout((int)(moveTime*1000));
			String inputLine = null;
			super.initTimers();
			plays = red;
			writeToScreen("Waiting for a transmit message...",MsgType.info);
			while(watching){
				this.updatePlayerTimer(plays.getTimer(),2);
				while((inputLine = watcherIn.readLine()) != null) {
					if(inputLine.equals("")){
						break;
					}
					if (inputLine==null)
					{
						throw new IOException();
					}
					theClient.logger.print_info("Transmition received: " + inputLine);
					String[] parsed = prot.parseCommand(inputLine);
					if(parsed == null){
						theClient.logger.print_error(prot.result + ". Bad move report received!");
					}
					else if(parsed[0].equals(ClientServerProtocol.GAMEMOVE)){
						Color c =(parsed[3].equals(red.getColor().getColorStr()))? Color.RED : Color.BLUE;
						String playingName =(parsed[1].equals(this.bluePlayer))? this.redPlayer : this.bluePlayer;
						try {
							state = gameBoard.playColumn(Integer.parseInt(parsed[2]), c);
							SwingUtilities.invokeAndWait(new BoardGUI.Painter(((BoardGUI)gameBoard).getColumnsFil(),Integer.parseInt(parsed[2]),c, slots));
							writeToScreen("Waiting for "+ playingName +" turn...",MsgType.info);
						} catch (Exception e) {
							popupDialog("Bad move or other problem: " + e.getMessage(), MsgType.error);
						}
					}
					else if(parsed[0].equals(ClientServerProtocol.GAMEREPORT)){
						String winner = parseReport(parsed);
						if(winner != null){
							if(winner.equals(Game.gameWinner.NO_WINNER)){
								writeToScreen("There is no winner!",MsgType.info);
							}
							else{
								writeToScreen(winner + " is the winner!",MsgType.info);
							}
							throw new GameEndedException();
						}
						else{
							theClient.logger.print_error("Don't understand who is the winner");
							throw new GameEndedException();
						}
					}
					else if(parsed[0].equals(ClientServerProtocol.MOVE_TIME)){
						plays.getTimer().updateTimer(Integer.parseInt(parsed[1]));
						this.updatePlayerTimer(plays.getTimer(),2);
						timeUpdated = true;
						continue;
					}
					else if(parsed[0].equals(ClientServerProtocol.DENIED)){
						popupDialog("Cannot watch this game, sorry", MsgType.error);
						throw new GameEndedException();
					}
					else{
						theClient.logger.print_error("I don't understand what transmitter send");
						throw new GameEndedException();
					}
				}
				if(timeUpdated){
					timeUpdated = false;
					continue;
				}
				super.nextPlayer();
			}
		}
		catch (SocketTimeoutException e)
		{
			theClient.logger.print_info(e.getMessage() + ". Stopping watching...");
			popupDialog("Connection Timeout , something went wrong , please reconnect!",MsgType.error);
		}
		catch (IOException e) {
			theClient.logger.print_info(e.getMessage() + ". Stopping watching...");
			//writeToScreen("The other player closed the connection!");
		}
		catch (GameEndedException myExc){
			theClient.logger.print_info("The game is over!");
		}
		finally{
			this.stopTimers();
			this.stopWatching();
		}
	}
	
	/**
	 * Set the watcher GUI invisible and remove mouse listeners.
	 */
	private void watchOver(){
		stopWatch.removeMouseListener(this);
		this.setVisible(false);
	}
	
	/**
	 * Closes all sockets and stops watching process.
	 */
	private void stopWatching(){
		watching = false;
		try {
			if(watcherIn != null){
				watcherIn.close();
				watcherIn = null;
			}
			if(watchSocket != null){
				watchSocket.close();
				watchSocket = null;
			}
			if(Isocket != null){
				Isocket.close();
				Isocket = null;
			}
		} catch (IOException e) {
			theClient.logger.print_error("Cannot close input stream for watcher");
		}
		theClient.stopWatching();
	}
	
	/**
	 * Parses the report from transmitter.
	 * @param message
	 * @return winner
	 */
	private String parseReport(String[] message){
		String winner = null;
		if(message[0].equalsIgnoreCase(ClientServerProtocol.GAMEREPORT)){
			if(Boolean.parseBoolean(message[3]) == Game.gameRes.WINNER){
				winner = message[4];
			}
			else{
				winner = Game.gameWinner.NO_WINNER;
			}
		}
		return winner;
	}
	
	/**
	 * Overrides handler of the window closing event.
	 * Joins the watcher thread.
	 * @param e
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		this.stopWatching();
		try {
			watcher.join();
		} catch (InterruptedException e1) {
			popupDialog("Problem while joining thread: " + e1.getMessage(), MsgType.error);
		}
	}
	
	/**
	 * Overrides handler of the window opened event.
	 * Starts the whatcher thread.
	 * @param e
	 */
	@Override
	public void windowOpened(WindowEvent e) {
		watcher = new Thread(this);
		watcher.start();
	}

	/**
	 * Overrides handler of the mouse clicked event.
	 * Stops watching is STOP_WATCH button was clicked.
	 * @param e
	 */
	@Override
	public  void mouseClicked(MouseEvent e) {
		String buttonName=((JButton)e.getComponent()).getName();
		//The client is surrender
		if (buttonName.equals(GameWatcher.STOP_WATCH))
		{
			this.stopWatching();
			this.watchOver();
		}		
	}
	
	/**
	 * Overrides handler of the timeout event.
	 * @param event
	 */
	@Override
	public void timeOutReceived(TimeOutEvent event) {
		//DO NOTHING
	}
	
}
