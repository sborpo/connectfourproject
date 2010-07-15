package gameManager;

import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;
import gameManager.Player.Color;

import java.awt.Button;
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
import javax.swing.JTextField;
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

public class GameGUI extends JDialog implements MouseListener,TimerListener,Runnable,Game,WindowListener{
	

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
	
	public boolean isGameFull()
	{
		return (blue != null);
	}
	
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
	
	synchronized public Player addWatcher(String watchName, String playerName){
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
	
	synchronized public Integer getCurrMoveTime(){
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
	
	protected Box createGridsBox()
	{
		Box gridBox = Box.createVerticalBox();
		boardPane= new JPanel();
		adjustGrid();
		gridBox.add(boardPane);
		return gridBox;
		
	}
	
	protected void updatePlayerTimer(Timer timer, int compNum){
		Box timerBox = timer.createTimerBox();
		Box consoleContainerBox = (Box)this.getContentPane().getComponent(compNum);
		timerBoxContainer.removeAll();
		timerBoxContainer.add(timerBox);
		if(!consoleContainerBox.isAncestorOf(timerBoxContainer)){
			//consoleContainerBox.remove(timerBoxContainer);
			consoleContainerBox.add(timerBoxContainer);
		}
	}
	
	
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
	
	protected void setGameEnabled(){
		surrender.addMouseListener(this);
		surrender.setEnabled(true);
	}
	
	protected void setGameDisabled(){
		surrender.removeMouseListener(this);
		surrender.setEnabled(false);
	}
	
	protected Box createConsolseBox()
	{

		//the console box
		Box containerBox = Box.createHorizontalBox();
		Box consoleBox = Box.createHorizontalBox();
		//consoleBox.setSize(700, 200);
		consoleArea = new JLabel("Console Printer ");
		consoleBox.setAlignmentX(SwingConstants.LEFT);
		consoleArea.setAlignmentX(LEFT_ALIGNMENT);
		consoleBox.add(consoleArea);
		containerBox.add(consoleBox);
		return containerBox;

	}
	
	
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
		boardPane.setSize(700,600);
	}
	
	public UnhandeledReport getReportStatus() {
		return gameReport;
	}
	
	public void writeClients(String playerName1,Color player1Col,String playerName2,Color player2Col)
	{
		try {
			SwingUtilities.invokeAndWait(new BoardGUI.ConnectionBoxPrinter(connAs1, connAs2, playerName1, player1Col, playerName2, player2Col));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void popupDialog(String message,String type){
		theClient.logger.print_error(message);
		mainFrame.showMessageDialog(message, type);
	}
	
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
		System.out.println("Opponent port transmit: "+opponentTransmitWaiterPort);
		this.theClient = theClient;
		
		try{
			this.setupConnection();
		} catch (IOException e) {
			if(!closing){
				errorMessage = "Problem initializing the game connection: " + e.getMessage();
				//popupDialog("Problem initializing the game connection: " + e.getMessage());
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
						System.out.println("Move ENTERED");
						if (inLine != null && inLine.equals(ClientServerProtocol.ISURRENDER))
						{
							writeToScreen("opp HOST: " + this.opponentHost,MsgType.info);
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
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
		
		if(state.equals(GameState.I_SURRENDED)){
			boolean res = AsynchroniousISurrender();
			if(!res){
				//errorMessage = "Problem sending surrender message to opponent";
				errorMessage = "Problem sending surrender message to opponent";
			}
		}
		System.out.println("DECIDING WINNER");
		String winner = null;
		winner=decideWinner();
		Boolean gameRes = (state.equals(GameState.TIE)) ? Game.gameRes.NO_WINNER : Game.gameRes.WINNER;
		this.closeConnection();
		this.stopTimers();
		setGameDisabled();
		return new UnhandeledReport(this.getId(), theClient.getClientName()	, gameRes.toString(), winner);

	}
	
	protected void initTimers(){
		red.setTimer(moveTime,this).pause().start();
		blue.setTimer(moveTime,this).pause().start();
	}
	
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

	private void excahngeData(ObjectOutputStream clientToOpponent, ObjectInputStream opponentIn) throws IOException {
		if(clientPlayer.equals(blue)){
			clientToOpponent.writeObject((clientPlayer.getName()));
			System.out.println("Sending: port: " + theClient.getTransmitWaiterPort());
			clientToOpponent.writeObject(theClient.getTransmitWaiterPort());
		}
		else{
			String name2 = null;
			try {
				name2 = (String)opponentIn.readObject();
				opponentTransmitWaiterPort =  (Integer)opponentIn.readObject();
				System.out.println("RECEIVED: "+ opponentTransmitWaiterPort);
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
	
	private boolean sendMessageGetResponse(String message){
		boolean succeeded = false;
		try {
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
	
	private void handleReconnectionProcess(){
		boolean succeeded = false;
		this.blocked = true;
		theClient.logger.print_error("Handling connection problems...");
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

	synchronized private String decideWinner() {
		String winner = null;
		switch(state){
			case TIE: 
				System.out.println("TIE");
				winner="0";
				break;
			case I_SURRENDED:
				System.out.println("I surrended");
				winner= opponentPlayer.getName();
				break;
			case OPPONENT_SURRENDED:
				System.out.println("OPP surrended");
				winner= clientPlayer.getName();
				break;
			case I_TIMED_OUT:
				System.out.println("I timed OUT");
				winner= opponentPlayer.getName();
				break;
			case OPP_TIMED_OUT:
				System.out.println("OPP timed OUT");
				winner= clientPlayer.getName();
				break;
		}
		
		//regular ending of the game
		if(winner == null){
			winner= plays.getName();
		}
		
		writeToScreen("The winner is: " + winner + "!",MsgType.info);
		return winner;
		
	}

	protected void nextPlayer() {
		plays.getTimer().pause().reset();
		if (plays.getColor().equals(Player.Color.RED)) {
			plays = blue;
		} else {
			plays = red;
		}
		plays.getTimer().resume();
	}
	
	synchronized public void addPlayer(String player2){
		blue = new Player(Player.Color.BLUE,player2);
	}
	 
	public Player getPlayer(Player.Color pColor){
		return pColor.equals(Player.Color.RED) ? red : blue;
	}

	public String getId(){
		return gameId;
	}
	
	public ArrayList<String> getGameHistory(){
		return gameHistory;
	}

	@Override
	public  void mouseClicked(MouseEvent e) {
		if(this.blocked){
			return;
		}
		synchronized (pending) {
			JButton theButton = ((JButton)e.getComponent());
			String buttonName=theButton.getName();
			//The client is surrender
			if (buttonName.equals(ClientServerProtocol.ISURRENDER))
			{
				this.clickedByPlayer=buttonName;
//				theButton.removeMouseListener(this);
//				theButton.setEnabled(false);
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
					//this.closeAndNotify();
					this.closeConnection();
				}
			}
		}
		
	}

	private boolean AsynchroniousISurrender() {
		boolean succeeded = false;
		this.blocked = true;
		theClient.logger.print_info("Handling I_surrender message..." + plays.getTimer());
		while(succeeded == false && !plays.getTimer().isTimedOut()){
			System.out.println("send I surr to opp");
			succeeded = sendMessageGetResponse(ClientServerProtocol.ISURRENDER);
			if(!succeeded){
				this.sleepAWhile(1000);
			}
		}
		this.blocked = false;
		return succeeded;
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
	public void run() {
		System.out.println("STARTING GAME");
		gameReport=startOnlineGame(clientGamePort,(String)opponentHost,opponentGamePort,opponentTransmitWaiterPort,startedGame,theClient);
		//this.setVisible(false);
		if(gameReport == null){
			gameReport = theClient.getEmptyReport();
		}
		theClient.makeReportToViewers(gameReport);
		//send the report to the server
		theClient.makeReportToServer(gameReport);
		if(errorMessage != null){
			popupDialog(errorMessage,MsgType.error);
		}
		else if(infoMessage != null){
			popupDialog(infoMessage,MsgType.info);
		}
		System.out.println("GUI IS FINISHED: "+gameReport );
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
			System.out.println("closing, and ...");
			state = GameState.I_SURRENDED;
			this.AsynchroniousISurrender();
			this.closeAndNotify();
		}
		while(this.gameReport == null || this.gameReport.equals("")){
			System.out.println("waittttt: " + gameReport);
			this.sleepAWhile(1000);
		}
		this.removeWindowListener(this);
		System.out.println("closing ended");
	}
	
	private void closeAndNotify(){
		this.stopTimers();
		synchronized (pending) {
			if(pending.isPending()){
				pending.setPending(false);
				pending.notify();
			}
		}
	}
	
	public void opponentSurrender(){
		state = GameState.OPPONENT_SURRENDED;
		
		//infoMessage = "Opponent has surrended!";
		this.closeAndNotify();
		popupDialog("Opponent has surrended!",MsgType.info);
	}

	synchronized public void resetConnection(){
		this.reconnect = true;
		this.blocked = true;
		writeToScreen("Handling reconnection, wait...",MsgType.info);
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
		writeToScreen("Recconection is Done!",MsgType.info);
	}
	
	private void closeConnection(){
		try {
			System.out.println("Closing connections...");
			if(serverSocket != null){
				serverSocket.close();
			}
			if(opponentSocket != null){
				opponentSocket.close();
			}
			if(clientToOpponent!=null){
				clientToOpponent.close();
			}
			if(opponentIn != null){
				opponentIn.close();
			}
		} catch (IOException e) {
			theClient.logger.print_error("Problem while closing the connection: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void sendMoveToViewers(String move){
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		String moveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEMOVE,
																		plays.getName(),
																		move,plays.getColor().getColorStr()});
		
		String[] parsed = prot.parseCommand(moveMsg);
		if(parsed == null){
			theClient.logger.print_error(prot.result + ". Bad move report: "+ moveMsg);
		}
		theClient.getTransmitWaiter().sendMoveToViewers(moveMsg);
		
		//save the move in the game history
		gameHistory.add(moveMsg);
	}

	@Override
	public void timeOutReceived(TimeOutEvent event) {
		theClient.logger.print_info("TIMEOUT");
		if(!state.equals(GameState.PROCEED)){
			System.out.println("Ignoring timeout...");
			return;
		}
		boolean timedOut = event.getValue();
		if(timedOut){
			this.timeOutHandler();
		}
	}
	
	private void timeOutHandler(){
		this.stopTimers();
		if(plays.equals(clientPlayer)){
			theClient.logger.print_info("My timer is timed out!");
			//no server connection - my timeout - I lose
			if(theClient.getAliveSender().noServerConnection()){
				state = GameState.I_TIMED_OUT;
			}
			//there is server connection but cannot send move - opp lose
			else if(this.blocked){
				state = GameState.OPP_TIMED_OUT;
			}
			//else- there are all connection - I lose
			else{
				state = GameState.I_TIMED_OUT;
			}
		}
		else{
			theClient.logger.print_info("Opponents timer is timed out!");
			if(theClient.getAliveSender().noServerConnection()){
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
				System.out.println("Notify pending");
				pending.notify();
			}
		}
		this.closeConnection();
	}
	
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
			try{
				String moveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEMOVE,
																	plays.getName(),
																	move,plays.getColor().getColorStr()});
				System.out.println("Sending: " + moveMsg);
				clientToOpponent.writeObject(moveMsg);
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
			}
		}
	}
		
	synchronized private String getOpponentMove(){
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
//					int time = Integer.parseInt(moveArr[4]);
//					plays.getTimer().updateTimer(time);
//					int after = plays.getTimer().getElapsed();
				} catch (IOException e) {
					
					if(this.reconnect){
						System.out.println("RECONNECTIng...");
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("OPPP MOVE IS REEAD");
		return move;
	}
	
	private void sleepAWhile(int sleepTime){
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			theClient.logger.print_error("Problem while sleeping: " + e.getMessage());
		}
	}
	
	private void setupConnection() throws IOException{
		if (startedGame == true) {
			serverSocket = new ServerSocket(clientGamePort);
			// can be a timeout how much to wait for an opponent
			writeToScreen("Waiting for opponent to connect ...",MsgType.info);
			opponentSocket = serverSocket.accept();
			opponentHost = opponentSocket.getInetAddress().getHostName();
			opponentPlayer = blue;
			clientPlayer = red;
			writeToScreen("Opponent Was Connected ,You Are The Red Player!",MsgType.info);
		
		} else {
			InetAddress address = null;
			try {
				address = InetAddress.getByName(opponentHost);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// the opponent starts the game
			opponentSocket = new Socket(address, opponentPort);
			clientPlayer = blue;
			opponentPlayer=red;
			writeToScreen("You Are The Blue Player!",MsgType.info);
			writeClients(blue.getName(),blue.getColor(),red.getName(),red.getColor());

		}
		clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
		opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
		if(!this.reconnect){
			excahngeData(clientToOpponent,opponentIn);
		}
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

		gameThread= new Thread(this);
		gameThread.start();
		
	}

	@Override
	public void removeSecondPlayer() {
		if(blue != null){
			blue = null;
		}
	}
	
}
