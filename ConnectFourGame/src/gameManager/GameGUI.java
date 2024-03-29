package gameManager;

import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;
import gameManager.Player.Color;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import common.Timer;
import common.UnhandeledReport;
import common.Timer.TimeOutEvent;
import common.Timer.TimerListener;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

import ConnectFourClient.MainFrame;
import ConnectFourClient.TheClient;
import ConnectFourClient.MainFrame.MsgType;

/**
 * This class implements the Game interface with GUI components
 * @author Boris
 *
 */
public class GameGUI extends JDialog implements MouseListener,TimerListener,Runnable,Game,WindowListener{
	

	/**
	 * This class represents whatever the application waits for user mouse input
	 *
	 */
	public static class Pending{
		public Pending()
		{
			pending=false;
		}
		public boolean isPending() {
			return pending;
		}

		public void setPending(boolean pending) {
			this.pending = pending;
		}

		private boolean pending;
		
	}

	//LOGIC PARAMETERS
	protected String gameId = null;
	protected Player red = null;
	protected Player blue = null;
	protected HashMap<String,Player> watchers = null;
	protected Board gameBoard = null;
	protected Player plays = null;
	protected GameState state = null;
	protected ArrayList<String> gameHistory = null;
	protected Pending pending = null;
	protected Player clientPlayer = null;
	protected Player opponentPlayer = null;
	protected ServerSocket serverSocket = null;
	protected Socket opponentSocket = null;
	protected ObjectInputStream opponentIn = null;
	protected ObjectOutputStream clientToOpponent = null;
	protected Boolean reconnect = false;
	protected boolean blocked = false;
	protected boolean closing = false;
	protected String errorMessage = null;
	protected String infoMessage = null;
	
	//START ONLINE GAME PARAMETERS
	protected boolean startedGame = false;
	protected final int moveTime = 60;
	protected int clientGamePort = TheClient.unDEFport;
	protected String opponentHost = null;
	protected int opponentPort = TheClient.unDEFport;
	protected int opponentTransmitWaiterPort = TheClient.unDEFport;
	protected int opponentGamePort = TheClient.unDEFport;	
	
	//GUI COMPONENTS
	protected MainFrame mainFrame;
	protected TheClient theClient;
    protected JButton surrender;
	protected JPanel boardPane;
	protected JButton [][] slots;
	protected JLabel consoleArea;
	protected JLabel connAs1;
	protected JLabel connAs2;
	protected Box timerBoxContainer;
	//protected JTextField timerText;
	protected String clickedByPlayer;
	protected UnhandeledReport gameReport;
	protected Thread gameThread;
	
	protected Integer lock ;
	
	/**
	 * Returns true of the game already have two players, otherwise returns false
	 * @return
	 */
	public boolean isGameFull()
	{
		return (blue != null);
	}
	
	/**
	 * Returns true if the given player Name is one of the participants of the game
	 * @param playerName
	 * @return
	 */
	public Player isPlayer(String playerName){
		Player player = null;
		if((red != null && red.getName().equals(playerName))){
			player = red;
		}
		else if((blue != null && blue.getName().equals(playerName))){
			player= blue;
		}
		
		return player;
	}
	
	/**
	 * Adds a new watcher into the game.
	 * @param watchName
	 * @param playerName
	 * @return
	 */
	 public Player addWatcher(String watchName, String playerName){
		Player player = isPlayer(playerName);
		
		if(player != null){
			if(watchers.containsKey(watchName)){
				player = watchers.get(watchName);
			}
			else{
				watchers.put(watchName, player);
			}
		}
		
		return player;
	}
	
	 /**
	 	 * Returns  elapsed time of the current  move
	 	 */
	 public Integer getCurrMoveTime(){
		if(plays != null){
			return plays.getTimer().getElapsed();
		}
		else{
			return null;
		}
	}
	
	public GameGUI()
	{
		
	}
	/**
	 * The c'tor of the GameGUI which initializes the game parameters and the GUI components
	 * @param name1
	 * @param name2
	 * @param gameId
	 * @param mainFrame
	 * @param clientGamePort
	 * @param opponentHost
	 * @param opponentGamePort
	 * @param startedGame
	 * @param theClient
	 * @param opponentTransmitWaiterPort
	 */
	public GameGUI(String name1,String name2,String gameId,MainFrame mainFrame,
			int clientGamePort, String opponentHost, int opponentGamePort,
			boolean startedGame, TheClient theClient,int opponentTransmitWaiterPort) {	
		//this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		super(mainFrame, true);
		this.mainFrame=mainFrame;
		this.clientGamePort=clientGamePort;
		this.opponentHost=opponentHost;
		pending = new Pending();
		this.opponentGamePort=opponentGamePort;
		this.startedGame=startedGame;
		this.opponentTransmitWaiterPort = opponentTransmitWaiterPort;
		red = new Player(Player.Color.RED,name1);
		if(name2 != null){
			blue = new Player(Player.Color.BLUE,name2);
		}
		else{
			blue = null;
		}
		lock= new Integer(0);
		this.theClient=theClient;
		this.gameId = gameId;
		timerBoxContainer = Box.createHorizontalBox();
		gameBoard = new BoardGUI(this);
		gameHistory = new ArrayList<String>();
		gameReport = null;
		watchers = new HashMap<String,Player>();
		Box [] arr= new Box[4];
		arr[0]=createUserNamesBox();
		arr[1]= createGridsBox();
		arr[2]= createSurrenderBox();
		arr[3]=createConsolseBox();
		this.addWindowListener(this);
		AdjustGUIView(arr);	
		this.setModal(true);
		setLocationRelativeTo(null);
	}
	
	/**
	 * Creates the usernames box
	 * @return
	 */
	protected Box createUserNamesBox()
	{
		//upper box (connected players names)
		Box TopMost = Box.createHorizontalBox();
		 connAs1= new JLabel();
		 connAs2= new JLabel();
		TopMost.add(connAs1);
		TopMost.add(connAs2);
		return TopMost;
		
	}
	
	/**
	 * Creates the grid box , where the game will be played
	 * @return
	 */
	protected Box createGridsBox()
	{
		Box gridBox = Box.createVerticalBox();
		boardPane= new JPanel();
		adjustGrid();
		gridBox.add(boardPane);
		return gridBox;
		
	}
	
	/**
	 * Updates the GUI components which shows the time that left to move
	 * @param timer
	 * @param compNum
	 */
	protected void updatePlayerTimer(Timer timer, int compNum){
		Box timerBox = timer.createTimerBox();
		Box consoleContainerBox = (Box)this.getContentPane().getComponent(compNum);
		timerBoxContainer.removeAll();
		timerBoxContainer.add(timerBox);
		if(!consoleContainerBox.isAncestorOf(timerBoxContainer)){
			consoleContainerBox.add(timerBoxContainer);
		}
	}
	
	/**
	 * Creates the box for the surrender button
	 * @return
	 */
	protected Box createSurrenderBox()
	{
		//surrender
		Box surrenderBox = Box.createHorizontalBox();
		surrender= new JButton("Surrender");
		surrender.setName(ClientServerProtocol.ISURRENDER);
		surrender.setHorizontalAlignment(SwingConstants.RIGHT);
		surrenderBox.add(surrender);
		surrender.setEnabled(false);
		return surrenderBox;
	}
	
	/**
	 * Enables the game
	 */
	protected void setGameEnabled(){
		surrender.addMouseListener(this);
		surrender.setEnabled(true);
	}
	
	/**
	 * Disables the game
	 */
	protected void setGameDisabled(){
		surrender.removeMouseListener(this);
		surrender.setEnabled(false);
	}
	
	/**
	 * Creates the message console box
	 * @return
	 */
	protected Box createConsolseBox()
	{

		//the console box
		Box containerBox = Box.createHorizontalBox();
		Box consoleBox = Box.createHorizontalBox();
		consoleArea = new JLabel("Console Printer ");
		consoleBox.setAlignmentX(SwingConstants.LEFT);
		consoleArea.setAlignmentX(LEFT_ALIGNMENT);
		consoleBox.add(consoleArea);
		containerBox.add(consoleBox);
		return containerBox;

	}
	
	/**
	 * Adjusting the grid where the game will played
	 * @param boxesArr
	 */
	protected void AdjustGUIView(Box [] boxesArr)
	{

		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		//gameBoard= new BoardGUI(this);
		for (Box box : boxesArr) {
			this.getContentPane().add(box);
		}
		setSize(300,300);
		setModal(true);	
	}
	
	/**
	 * Auxilary method which adjust the grid
	 */
	private void adjustGrid()
	{
		boardPane.setLayout(new GridLayout(6,7));
		boardPane.setBorder(new LineBorder(java.awt.Color.black));
		slots = new JButton[6][7];
		for (int row=5; row>=0; row--) {
		for (int column=0; column<7; column++) {
			slots[row][column] = new JButton();
			slots[row][column].setBorder(new LineBorder(java.awt.Color.black));
			slots[row][column].setName(String.valueOf(row)+" "+String.valueOf(column));
			slots[row][column].setHorizontalAlignment(SwingConstants.CENTER);
			slots[row][column].addMouseListener(this);
			boardPane.add(slots[row][column]);
		}
		}
		boardPane.setSize(300,300);
	}
	
	/**
	 * Returns the report status
	 * @return
	 */
	public UnhandeledReport getReportStatus() {
		return gameReport;
	}
	
	/**
	 * Writes the clients which are connected now in the top of the screen
	 * @param playerName1
	 * @param player1Col
	 * @param playerName2
	 * @param player2Col
	 */
	public void writeClients(String playerName1,Color player1Col,String playerName2,Color player2Col)
	{
		try {
			SwingUtilities.invokeAndWait(new BoardGUI.ConnectionBoxPrinter(connAs1, connAs2, playerName1, player1Col, playerName2, player2Col));
		} catch (Exception e){
			theClient.logger.print_error("Problem writing the clients: " + e.getMessage());
		}
	}
	
	/**
	 * Writes the approporiate message in the console box
	 * @param message
	 * @param msgType
	 */
	public void writeToScreen(String message,String msgType)
	{
		try {
			if(msgType.equals(MsgType.error)){
				theClient.logger.print_error(message);
			}
			else{
				theClient.logger.print_info(message);
			}
			if(!closing){
				SwingUtilities.invokeAndWait(new BoardGUI.MessagePrinter(consoleArea,message));
			}
		} catch (Exception e) {
			theClient.logger.print_error("Problem writing to screen: " + e.getMessage());
		}
	}
	
	/**
	 * Pops up a dialog window for the user
	 * @param message
	 * @param type
	 */
	public void popupDialog(String message,String type){
		mainFrame.showMessageDialog(message, type);
	}
	
	/**
	 * This method should handle the online game and return an Unhandeled Report object as a result of the game
	 * @param clientPort
	 * @param opponentHost
	 * @param opponentPort
	 * @param opponentTransmitWaiterPort
	 * @param startsGame
	 * @param theClient
	 * @return
	 */
	public UnhandeledReport startOnlineGame(int clientPort, String opponentHost,int opponentPort,
											int opponentTransmitWaiterPort,boolean startsGame, TheClient theClient) {
		startedGame = startsGame;
		serverSocket = null;
		opponentSocket = null;
		opponentIn = null;
		clientToOpponent = null;
		this.clientGamePort = clientPort;
		this.opponentHost = opponentHost;
		this.opponentPort = opponentPort;
		this.opponentTransmitWaiterPort = opponentTransmitWaiterPort;
		this.theClient = theClient;
		
		try{
			this.setupConnection();
		} catch (IOException e) {
			if(!closing){
				errorMessage = "Problem initializing the game connection: " + e.getMessage();
			}
			return null;
		}
		
		plays = red;
		//String playerStr;
		state = GameState.PROCEED;
		//init timers for players
		this.initTimers();
		plays.getTimer().resume();
		setGameEnabled();
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		while (state.equals(GameState.PROCEED)) {
			this.updatePlayerTimer(plays.getTimer(),3);
			int colnum = -1;
			String inLine = null;
			try {
				if (plays.equals(clientPlayer)) {
					writeToScreen("Please Click Your Move: ",MsgType.info);
					while(colnum == -1){
						synchronized (pending) {
							try {
								pending.setPending(true);
								//wait for a user input from the mouse
								pending.wait();
							} catch (InterruptedException e) {
								//
							}
							pending.setPending(false);
						}
						inLine=clickedByPlayer;
						clickedByPlayer = null;
						if (inLine != null && inLine.equals(ClientServerProtocol.ISURRENDER))
						{
							state=GameState.I_SURRENDED;
							break;
						}
						if(!state.equals(GameState.PROCEED)){
							break;
						}
						
						if(inLine == null){
							//timeout occured
							break;
						}
						if(inLine.equals("")){
							writeToScreen("Empty move, try again...",MsgType.error);
						}
						else{
							colnum	=	Integer.parseInt(inLine);
						}
					}
					if(state.equals(GameState.PROCEED)){
						//send the move to the opponent
						this.sendMoveToOpponent(colnum);	
					}
					
				} else {
					writeToScreen("Waiting For Opponent Move: ",MsgType.info);
					try {
						String move= this.getOpponentMove();
						if(move == null){
							writeToScreen("No opponent move",MsgType.error);
							break;
						}
						if (move.equals(ClientServerProtocol.ISURRENDER))
						{
							state=GameState.OPPONENT_SURRENDED;
						}
						else
						{
							colnum = Integer.parseInt(move);
						}
					} catch (NumberFormatException e) {
						//cannot happen
					} 	
				}
				
				//the clients not surrended
				if (state.equals(GameState.PROCEED))
				{
					state = gameBoard.playColumn(colnum, plays.getColor());
					try {
						SwingUtilities.invokeAndWait(new BoardGUI.Painter(((BoardGUI)gameBoard).getColumnsFil(), colnum, plays.getColor(), slots));
					} catch (Exception e){
						theClient.logger.print_error("Problem processing the move: " + e.getMessage());
					}
				}
			} catch (IllegalMove e) {
				writeToScreen("Illegal Move!!! Please Retry!",MsgType.error);
				continue;
			} 

			if(state.equals(GameState.PROCEED)){
				String move = String.valueOf(colnum);
				//sent the move to viewers
				this.sendMoveToViewers(move);
				//go to the next player
				nextPlayer();
			}
		}
		
		if(state.equals(GameState.I_SURRENDED) && !closing){
			boolean res = AsynchroniousISurrender();
			if(!res){
				errorMessage = "Problem sending surrender message to opponent";
			}
		}
		theClient.logger.print_info("DECIDING WINNER");
		String winner = null;
		winner=decideWinner();
		Boolean gameRes = (state.equals(GameState.TIE)) ? Game.gameRes.NO_WINNER : Game.gameRes.WINNER;
		this.closeConnection();
		this.stopTimers();
		setGameDisabled();
		return new UnhandeledReport(this.getId(), theClient.getClientName()	, gameRes.toString(), winner);

	}
	
	/**
	 * initialize timers
	 */
	protected void initTimers(){
		red.setTimer(moveTime,this).pause().start();
		blue.setTimer(moveTime,this).pause().start();
	}
	
	/**
	 * stops the timers
	 */
	protected void stopTimers(){
		Timer timer = null;
		timer = red.getTimer();
		if(timer != null){
			timer.stop();
			red.unsetTimer();
		}
		timer = blue.getTimer();
		if(timer != null){
			timer.stop();
			blue.unsetTimer();
		}
	}

	/**
	 * Excahnges init data between users , their names , address and ect..
	 * @param clientToOpponent
	 * @param opponentIn
	 * @throws IOException
	 */
	private void excahngeData(ObjectOutputStream clientToOpponent, ObjectInputStream opponentIn) throws IOException {
		if(clientPlayer.equals(blue)){
			clientToOpponent.writeObject((clientPlayer.getName()));
			clientToOpponent.writeObject(theClient.getTransmitWaiterPort());
		}
		else{
			String name2 = null;
			try {
				name2 = (String)opponentIn.readObject();
				opponentTransmitWaiterPort =  (Integer)opponentIn.readObject();
			} catch (ClassNotFoundException e) {
				//cannot be
			}
			if( name2 != null){
				addPlayer(name2);
				opponentPlayer=blue;
				writeClients(red.getName(),red.getColor(),blue.getName(),blue.getColor());
			}
		}
		
	}
	
	/**
	 * Sends a message on the transmit waiter port (Surrender request , or recconection)
	 * @param message
	 * @return
	 */
	private boolean sendMessageGetResponse(String message){
		boolean succeeded = false;
		try {
			theClient.logger.print_info("Trying to send your message to opponent: " + message);
			ClientServerProtocol prot= new ClientServerProtocol(msgType.CLIENT);
			String [] messageCommand=prot.parseCommand(message);
			if(messageCommand == null){
				writeToScreen("Bad message to the opponent: " + prot.result,MsgType.error);
				succeeded = false;
				return succeeded;
			}
			if(opponentHost == null){
				succeeded = false;
				theClient.logger.print_error("Opponent host is null");
				return succeeded;
			}
			InetAddress address = InetAddress.getByName(opponentHost);
			Socket opponentTransmitSocket = new Socket(address, opponentTransmitWaiterPort);
			PrintWriter clientToOpponent = new PrintWriter(opponentTransmitSocket.getOutputStream(),true);
			clientToOpponent.println(message);
			opponentTransmitSocket.setSoTimeout(ClientServerProtocol.timeout);
			BufferedReader oppIn = new BufferedReader(new InputStreamReader((opponentTransmitSocket.getInputStream())));
			String response = (String)oppIn.readLine();
			if(response.equals(ClientServerProtocol.OK)){
				succeeded = true;
			}
			else{
				theClient.logger.print_error("BAD RESPONSE:" + response);
			}
		} catch (UnknownHostException e) {
			theClient.logger.print_error("Problem getting opponent address: " + e.getMessage());
		} catch (IOException e) {
			theClient.logger.print_error("Problem sending: " + message + " command to the opponent: "+e.getMessage());
		}
		return succeeded;
	}
	
	/**
	 * Handles the reconnection process
	 */
	private void handleReconnectionProcess(){
		boolean succeeded = false;
		this.blocked = true;
		writeToScreen("Handling connection problems...",MsgType.error);
		while(state.equals(GameState.PROCEED) && succeeded == false){
			succeeded = sendMessageGetResponse(ClientServerProtocol.SOCKETREFRESH);
			if(succeeded){
				break;
			}
			this.sleepAWhile(1000);
		}
		if(succeeded){
			this.blocked = false;
			this.resetConnection();
		}
		
	}

	/**
	 * Decides who was the winner in the game
	 * @return
	 */
	 private String decideWinner() {
		String winner = null;
		switch(state){
			case TIE: 
				theClient.logger.print_info("TIE");
				winner="0";
				break;
			case I_SURRENDED:
				theClient.logger.print_info("I surrended");
				winner= opponentPlayer.getName();
				break;
			case OPPONENT_SURRENDED:
				theClient.logger.print_info("OPP surrended");
				winner= clientPlayer.getName();
				break;
			case I_TIMED_OUT:
				theClient.logger.print_info("I timed OUT");
				winner= opponentPlayer.getName();
				break;
			case OPP_TIMED_OUT:
				theClient.logger.print_info("OPP timed OUT");
				winner= clientPlayer.getName();
				break;
			case NO_CONN:
				theClient.logger.print_info("NO connection");
				winner= gameWinner.I_DONT_KNOW;
				break;
		}
		
		//regular ending of the game
		if(winner == null){
			winner= plays.getName();
		}
		if(!winner.equals(gameWinner.I_DONT_KNOW)){
			writeToScreen("The winner is: " + winner + "!",MsgType.info);
		}
		return winner;
		
	}

	 /**
	  * moves to the next player
	  */
	protected void nextPlayer() {
		plays.getTimer().pause().reset();
		if (plays.getColor().equals(Player.Color.RED)) {
			plays = blue;
		} else {
			plays = red;
		}
		plays.getTimer().resume();
	}
	
	/**
 	 * Adds the given player to the game
 	 * @param player
 	 */
	 public void addPlayer(String player2){
		blue = new Player(Player.Color.BLUE,player2);
	}
	 
	 /**
	 	 * Returns the player of the given color
	 	 * @param pColor
	 	 * @return
	 	 */ 
	public Player getPlayer(Player.Color pColor){
		return pColor.equals(Player.Color.RED) ? red : blue;
	}

	/**
 	 * Returns the gameId
 	 * @return
 	 */
	public String getId(){
		return gameId;
	}
	
	/**
 	 * Returns the moves history of the game
 	 * @return
 	 */
	public ArrayList<String> getGameHistory(){
		return gameHistory;
	}

	/**
	 * Overrides the mouseClicked handler. 
	 * Handles the clicked button and calls appropriate functions.
	 * @param e
	 */
	@Override
	public  void mouseClicked(MouseEvent e) {
		if(this.blocked && this.reconnect){
			return;
		}
		synchronized (pending) {
			JButton theButton = ((JButton)e.getComponent());
			String buttonName=theButton.getName();
			//The client is surrender
			if (buttonName.equals(ClientServerProtocol.ISURRENDER))
			{
				this.clickedByPlayer=buttonName;
			}
			
			if (pending.isPending())
			{
				if(!buttonName.equals(ClientServerProtocol.ISURRENDER)){
					int colnum= (new Integer(Integer.parseInt(buttonName.split(" ")[1])));
					this.clickedByPlayer =(new Integer(colnum)).toString();	
				}
				pending.setPending(false);
				pending.notify();
			}
			else{
				if (buttonName.equals(ClientServerProtocol.ISURRENDER))
				{
					state = GameState.I_SURRENDED;
					this.closeConnection();
				}
			}
		}
		
	}

	/**
	 * handles the case that we want to surrender and it is not our turn
	 * @return
	 */
	private boolean AsynchroniousISurrender() {
		boolean succeeded = false;
		this.blocked = true;
		writeToScreen("Surrender handling...",MsgType.info);
		while(succeeded == false && plays.getTimer() != null && !plays.getTimer().isTimedOut()){
			succeeded = sendMessageGetResponse(ClientServerProtocol.ISURRENDER);
			if(!succeeded){
				this.sleepAWhile(1000);
			}
		}
		if(plays.getTimer() == null){
			theClient.logger.print_error("The timer is null!");
		}
		this.blocked = false;
		return succeeded;
	}

	/**
	 * Overrides the mouseEntered handler.
	 * @param e
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Overrides the mouseExited handler.
	 * @param e
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Overrides the mousePressed handler.
	 * @param e
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * Overrides the mouseReleased handler.
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * Runs the online game in new thread
	 */
	@Override
	public void run() {
		gameReport=startOnlineGame(clientGamePort,(String)opponentHost,opponentGamePort,opponentTransmitWaiterPort,startedGame,theClient);
		if(gameReport == null){
			gameReport = theClient.getEmptyReport();
		}
		if(!gameReport.getWinner().equals(gameWinner.I_DONT_KNOW)){
			theClient.makeReportToViewers(gameReport);
			//send the report to the server
			theClient.makeReportToServer(gameReport);
		}
		else{
			errorMessage = "No internet connection!";
		}
		if(errorMessage != null){
			popupDialog(errorMessage,MsgType.error);
		}
		if(infoMessage != null){
			popupDialog(infoMessage,MsgType.info);
		}
		if(Boolean.parseBoolean(gameReport.getGameResult()) == Game.gameRes.WINNER){
			if(gameReport.getWinner().equals(clientPlayer.getName())){
				popupDialog("You are the winner!",MsgType.info);
			}
			else if(gameReport.getWinner().equals(opponentPlayer.getName())){
				popupDialog("You are the loser!",MsgType.info);
			}
			else{
				popupDialog("Some problems durring the game,\n the second player will report!", MsgType.info);
			}
		}
		else{
			popupDialog("Noboody had won!", MsgType.info);
		}
		this.setVisible(false);
	}

	/**
	 * Overrides the windowActivated handler.
	 * @param e 
	 */
	@Override
	public void windowActivated(WindowEvent e) {
	}

	/**
	 * Overrides the windowClosed handler.
	 * @param e 
	 */
	@Override
	public void windowClosed(WindowEvent e) {
	}

	/**
	 * Overrides the windowClosing handler.
	 * @param e 
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		closing = true;
		setGameDisabled();
		this.closeConnection();
		if (gameReport!=null)
		{
			//the game finished good
			return;
		}
		if ((red==null) || (blue==null))
		{
			gameReport=null;
			return;
		}
		if (gameThread!=null)
		{
			state = GameState.I_SURRENDED;
			this.AsynchroniousISurrender();
			this.closeAndNotify();
		}
		while(this.gameReport == null || this.gameReport.equals("")){
			this.sleepAWhile(1000);
		}
		this.removeWindowListener(this);
	}
	
	/**
	 * Stops the timers and notifies if needed.
	 */
	private void closeAndNotify(){
		this.stopTimers();
		synchronized (pending) {
			if(pending.isPending()){
				pending.setPending(false);
				pending.notify();
			}
		}
	}
	
	/**
 	 * This method handles the situation that the opponent Surrenders
 	 */
	public void opponentSurrender(){
		state = GameState.OPPONENT_SURRENDED;
		this.closeAndNotify();
		infoMessage = "Opponent has surrended!";
	}

	
	/**
 	 * Resets the connection between the two players
 	 */
	 public void resetConnection(){
		 
		 synchronized(lock)
		 {
		 if (reconnect)
		 {
			 return;
		 }
		this.reconnect = true;
		 }
		this.blocked = true;
		writeToScreen("Refreshing the connection, wait...",MsgType.info);
		while(this.reconnect){
			if(!state.equals(GameState.PROCEED)){
				this.reconnect = false;
				return;
			}
			try {
				//closing old socket data
				this.closeConnection();
				//restarting
				this.setupConnection();
				this.reconnect = false;
			} catch (IOException e) {
				theClient.logger.print_error("Problem while reseting the connection: " + e.getMessage());
				this.sleepAWhile(1000);
			}
		}
		this.blocked = false;
		popupDialog("Recconection is Done!",MsgType.info);
		if(clientPlayer.equals(plays)){
			writeToScreen("Enter your Move: ",MsgType.info);
		}
		else{
			writeToScreen("Waiting For Opponent Move: ",MsgType.info);
		}
	}
	
	/**
	 * Closing the connection sockets. 
	 */
	private void closeConnection(){
		try {
			theClient.logger.print_info("Closing connections...");
			if(serverSocket != null){
				serverSocket.close();
				serverSocket = null;
			}
			if(opponentSocket != null){
				opponentSocket.close();
				opponentSocket = null;
			}
//			if(clientToOpponent!=null){
//				clientToOpponent.close();
//				clientToOpponent = null;
//			}
//			if(opponentIn != null){
//				opponentIn.close();
//				opponentIn = null;
//			}
		} catch (IOException e) {
			theClient.logger.print_error("Problem while closing the connection: " + e.getMessage());
		}
	}
	
	/**
	 * Sends the current move to the game viewers
	 * @param move
	 */
	private void sendMoveToViewers(String move){
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		String moveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEMOVE,
																		plays.getName(),
																		move,plays.getColor().getColorStr()});
		
		String[] parsed = prot.parseCommand(moveMsg);
		if(parsed == null){
			theClient.logger.print_error(prot.result + ". Bad move report: "+ moveMsg);
		}
		
		if(theClient.getTransmitWaiter() != null){
			theClient.getTransmitWaiter().sendMoveToViewers(moveMsg);
		}
		
		//save the move in the game history
		gameHistory.add(moveMsg);
	}

	/**
	 * handles the case that a timeout was recieved
	 */
	@Override
	public void timeOutReceived(TimeOutEvent event) {
		if(!state.equals(GameState.PROCEED)){
			return;
		}
		boolean timedOut = event.getValue();
		if(timedOut){
			this.timeOutHandler();
		}
	}
	
	/**
	 * The timeout handler
	 */
	private void timeOutHandler(){
		this.stopTimers();
		//no server connection
		if(theClient.getAliveSender().noInternetConnection()){
			state = GameState.NO_CONN;
		}
		else if(plays.equals(clientPlayer)){
			theClient.logger.print_info("My timer is timed out!");
			//there is server connection but cannot send move - opp lose
			if(this.blocked){
				state = GameState.OPP_TIMED_OUT;
			}
			//else- there are all connection - I lose
			else{
				state = GameState.I_TIMED_OUT;
			}
		}
		else{
			theClient.logger.print_info("Opponents timer is timed out!");
			if(theClient.getAliveSender().noInternetConnection()){
				theClient.logger.print_info("I had no server connection");
				state = GameState.I_TIMED_OUT;
			}
			else{
				state = GameState.OPP_TIMED_OUT;
			}
		}
		
		synchronized (pending) {
			if (pending.isPending())
			{
				pending.setPending(false);
				pending.notify();
			}
		}
		this.closeConnection();
	}
	
	/**
	 * Sends the played move to the opponenet
	 * @param colnum
	 */
	private void sendMoveToOpponent(int colnum){
		String move = null;
		if(state.equals(GameState.I_SURRENDED)){
			move = ClientServerProtocol.ISURRENDER;
		}
		else{
			move = String.valueOf(colnum);
		}
		// write your move
		boolean reconnectOnRead= true;
		while (reconnectOnRead && state.equals(GameState.PROCEED))
		{	
			if(!state.equals(GameState.PROCEED)){
				break;
			}
			reconnectOnRead= false;
			if (this.reconnect)
			{
				this.sleepAWhile(1000);
				reconnectOnRead = true;
				continue;
			}
			
			try{
				String moveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEMOVE,
																	plays.getName(),
																	move,plays.getColor().getColorStr()});
				
				this.innerSendMoveToOpp(moveMsg);				
			}
			//HANDLE CONNECTIONS PROBLEMS
			catch (IOException ex)
			{
				if(this.reconnect){
					//this.reconnect = false;
					reconnectOnRead= true;
					continue;
				}
				theClient.logger.print_error("While writing object: " + ex.getMessage());
				reconnectOnRead= true;
				handleReconnectionProcess();	
			} catch (ClassNotFoundException e) {
				reconnectOnRead= true;
				theClient.logger.print_error("Problem sending the move to the opponent: " + e.getMessage());
			}
		}
	}
		
	/**
	 * Auxillary send to opponent move
	 * @param moveMsg
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	 private void innerSendMoveToOpp(String moveMsg) throws IOException, ClassNotFoundException {
		 theClient.logger.print_info("Sending move to opponent: " + moveMsg);
		 opponentSocket.setSoTimeout(ClientServerProtocol.timeout);
		 clientToOpponent.writeObject(moveMsg);
		 clientToOpponent.flush();
		 //now get the response to be sure the opponent is connected
		 String response = (String)opponentIn.readObject();
		 opponentSocket.setSoTimeout(moveTime*1000);
	}

	/**
	 * waits to opponent move and returns it
	 * @return
	 */
	private String getOpponentMove(){
		String move = null;
		if(opponentIn != null){
			boolean reconnectOnRead= true;
			while (reconnectOnRead && state.equals(GameState.PROCEED))
			{	
				reconnectOnRead=false;
				try {
					String moveMsg = (String)opponentIn.readObject();
					ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
					String[] moveArr = prot.parseCommand(moveMsg);
					if(moveArr == null){
						popupDialog("Wrong move command, trying to get move again",MsgType.error);
						reconnectOnRead= true;
						continue;
					}
					move = moveArr[2];

					//send response ok to the opponent
					clientToOpponent.writeObject(ClientServerProtocol.OK);
					clientToOpponent.flush();
				} catch (IOException e) {
					
					if(this.reconnect){
						this.sleepAWhile(1000);
						//this.reconnect = false;
						reconnectOnRead = true;
						continue;
					}
					
					if(!state.equals(GameState.PROCEED)){
						move = null;
						break;
					}
					else{
						//HANDLE CONNECTION PROBLEMS
						theClient.logger.print_error("While reading from opponent socket: " + e.getMessage());
						reconnectOnRead=true;
						handleReconnectionProcess();
					}
				} catch (ClassNotFoundException e) {
					//DOES NOT HAPPEN
				}
			}
		}
		return move;
	}
	
	/**
	 * sleeps for a certain amount of time
	 * @param sleepTime
	 */
	protected void sleepAWhile(int sleepTime){
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			theClient.logger.print_error("Problem while sleeping: " + e.getMessage());
		}
	}
	
	/**
	 * Initialize a new connection with the opponent
	 * @throws IOException
	 */
	private void setupConnection() throws IOException{
		if (startedGame == true) {
			serverSocket = new ServerSocket(clientGamePort);
			// can be a timeout how much to wait for an opponent
			writeToScreen("Waiting for opponent to connect ...",MsgType.info);
			opponentSocket = serverSocket.accept();
			opponentHost = opponentSocket.getInetAddress().getHostName();
			opponentPlayer = blue;
			clientPlayer = red;
			theClient.logger.print_info("Opponent Was Connected ,You Are The Red Player!");
		
		} else {
			writeToScreen("Connecting to the opponent...",MsgType.info);
			InetAddress address = null;
			try {
				address = InetAddress.getByName(opponentHost);
			} catch (UnknownHostException e) {
				theClient.logger.print_error("Wrong host exception: " + e.getMessage());
			}
			// the opponent starts the game
			opponentSocket = new Socket(address, opponentPort);
			clientPlayer = blue;
			opponentPlayer=red;
			theClient.logger.print_info("You Are The Blue Player!");
			writeClients(blue.getName(),blue.getColor(),red.getName(),red.getColor());

		}
		opponentSocket.setSoTimeout(this.moveTime*1000);
		clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
		opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
	
		if(!this.reconnect){
			writeToScreen("Exchanging data...",MsgType.info);
			excahngeData(clientToOpponent,opponentIn);
		}
	}

	/**
	 * Overrides the windowDeactivated handler.
	 * @param e
	 */
	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	/**
	 * Overrides the windowDeiconified handler.
	 * @param e
	 */
	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	/**
	 * Overrides the windowIconified handler.
	 * @param e
	 */
	@Override
	public void windowIconified(WindowEvent e) {
	}

	/**
	 * Overrides the windowOpened handler.
	 * @param e
	 */
	@Override
	public void windowOpened(WindowEvent e) {

		gameThread= new Thread(this);
		gameThread.start();
		
	}

	/**
 	 * Removes the second player of the game (Not the host)
 	 */
	@Override
	public void removeSecondPlayer() {
		if(blue != null){
			blue = null;
		}
	}
	
}
