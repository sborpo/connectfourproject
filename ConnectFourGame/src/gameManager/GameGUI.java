package gameManager;

import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;
import gameManager.Player.Color;

import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.sun.java.swing.SwingUtilities3;

import common.Timer;
import common.UnhandeledReport;
import common.Timer.TimeOutEvent;
import common.Timer.TimerListener;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

import ConnectFourClient.MainFrame;
import ConnectFourClient.TheClient;

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
	private String gameId = null;
	private Player red = null;
	private Player blue = null;
	private HashMap<String,Player> watchers = null;
	protected Board gameBoard = null;
	private Player plays = null;
	protected GameState state = null;
	private ArrayList<String> gameHistory = null;
	private Pending pending = null;
	private Player clientPlayer = null;
	private Player opponentPlayer = null;
	private ServerSocket serverSocket = null;
	private Socket opponentSocket = null;
	private ObjectInputStream opponentIn = null;
	private ObjectOutputStream clientToOpponent = null;
	private Boolean reconnect = false;
	private boolean blocked = false;
	private boolean closing = false;
	
	//START ONLINE GAME PARAMETERS
	private boolean startedGame = false;
	protected final int moveTime = 70;
	private int clientGamePort = TheClient.unDEFport;
	private String opponentHost = null;
	private int opponentPort = TheClient.unDEFport;
	private int opponentTransmitWaiterPort = TheClient.unDEFport;
	private int opponentGamePort = TheClient.unDEFport;	
	
	//GUI COMPONENTS
	private MainFrame mainFrame;
	protected TheClient theClient;
    private JButton surrender;
	private JPanel boardPane;
	protected JButton [][] slots;
	protected JLabel consoleArea;
	protected JLabel connAs1;
	protected JLabel connAs2;
	private String clickedByPlayer;
	private UnhandeledReport gameReport;
	private Thread gameThread;
	
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
	

	public GameGUI()
	{
		
	}
	public GameGUI(String name1,String name2,String gameId,MainFrame mainFrame,
			int clientGamePort, String opponentHost, int opponentGamePort,
			boolean startedGame, TheClient theClient,int opponentTransmitWaiterPort) {	
		//this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
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
	
	protected Box createSurrenderBox()
	{
		//surrender
		Box surrenderBox = Box.createHorizontalBox();
		surrender= new JButton("Surrender");
		surrender.setName(ClientServerProtocol.ISURRENDER);
		surrender.addMouseListener(this);
		surrender.setHorizontalAlignment(SwingConstants.RIGHT);
		surrenderBox.add(surrender);
		return surrenderBox;
	}
	
	protected Box createConsolseBox()
	{

		//the console box
		Box consoleBox = Box.createHorizontalBox();
		consoleBox.setSize(700, 200);
		consoleArea = new JLabel("Console Printer");
		consoleBox.setAlignmentX(SwingConstants.LEFT);
		consoleArea.setAlignmentX(LEFT_ALIGNMENT);
		consoleBox.add(consoleArea);
		return consoleBox;

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
	
	public void writeToScreen(String message)
	{
		try {
			theClient.logger.print_info(message);
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
			// TODO Handle serverSocket initialization problem
			e.printStackTrace();
			return new UnhandeledReport(getId(), theClient.getClientName(), "0", "-1");
		}

		plays = red;
		//String playerStr;
		state = GameState.PROCEED;
		//init timers for players
		this.initTimers();
		plays.getTimer().resume();
		
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		while (state.equals(GameState.PROCEED)) {
			int colnum = -1;
			String inLine = null;
			try {
				if (plays.equals(clientPlayer)) {
					writeToScreen("Please Click Your Move:");
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
						System.out.println("Move ENTERED");
						if(!state.equals(GameState.PROCEED)){
							break;
						}
						inLine=clickedByPlayer;
						clickedByPlayer = null;
						if(inLine == null){
							//timeout occured
							break;
						}
						if(inLine.equals("")){
							writeToScreen("Empty move, try again...");
						}
						else{
							if (inLine.equals(ClientServerProtocol.ISURRENDER))
							{
								state=GameState.I_SURRENDED;
								break;
							}
							else
							{
								colnum	=	Integer.parseInt(inLine);
							}
						}
					}
					if(state.equals(GameState.PROCEED)){
						//send the move to the opponent
						this.sendMoveToOpponent(colnum);	
					}
					
				} else {
					writeToScreen("Waiting For Opponent Move!:\n");
					try {
						String move= this.getOpponentMove();
						if(move == null){
							theClient.logger.print_error("Bad move or timeout");
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
				writeToScreen("Illegal Move!!! Please Retry!\n\n");
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
		System.out.println("DECIDING WINNER");
		String winner = null;
		winner=decideWinner();
		Integer gameRes = (state.equals(GameState.TIE)) ? 0 : 1;
		this.closeConnection();
		this.stopTimers();
		return new UnhandeledReport(this.getId(), theClient.getClientName()	, gameRes.toString(), winner);

	}
	
	private void initTimers(){
		red.setTimer(moveTime,this).pause().start();
		blue.setTimer(moveTime,this).pause().start();
	}
	
	private void stopTimers(){
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
				theClient.logger.print_error("Bad message to the opponent: " + prot.result);
				succeeded = false;
				return succeeded;
			}
			if(opponentHost == null){
				succeeded = false;
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
				System.out.println("got OK RESPONSE");
			}
			else{
				System.out.println("BAD RESPONSE:" + response);
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
			this.resetConnection();
		}
		this.blocked = false;
	}

	private String decideWinner() {
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
				winner= clientPlayer.getName();
				break;
			case OPP_TIMED_OUT:
				System.out.println("OPP timed OUT");
				winner= opponentPlayer.getName();
				break;
		}
		
		//regular ending of the game
		if(winner == null){
			winner= plays.getName();
		}
		
		writeToScreen("The winner is: " + winner + "\n");
		return winner;
		
	}

	private void nextPlayer() {
		System.out.println("Current player: "+plays.getName());
		plays.getTimer().pause().reset();
		if (plays.getColor().equals(Player.Color.RED)) {
			plays = blue;
		} else {
			plays = red;
		}
		plays.getTimer().resume();
		System.out.println("Changed to player: "+plays.getName());
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
			String buttonName=((JButton)e.getComponent()).getName();
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
					this.AsynchroniousISurrender();
					state = GameState.I_SURRENDED;
					this.closeAndNotify();
				}
			}
		}
		
	}

	private boolean AsynchroniousISurrender() {
		boolean succeeded = false;
		this.blocked = true;
		theClient.logger.print_info("Handling I_surrender message...");
		while(state.equals(GameState.PROCEED) && succeeded == false){
			succeeded = sendMessageGetResponse(ClientServerProtocol.ISURRENDER);
		}
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
		this.setVisible(false);
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

			if(state.equals(GameState.PROCEED)){
				this.state = GameState.I_SURRENDED;
			}
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
		System.out.println("CLOSING AND STOPPING TIMERS");
		this.closeConnection();
		this.stopTimers();
		synchronized (pending) {
			if(pending.isPending()){
				pending.setPending(false);
				pending.notify();
			}
		}
	}
	
	synchronized public void opponentSurrender(){
		state = GameState.OPPONENT_SURRENDED;
		this.closeAndNotify();
	}

	synchronized public void resetConnection(){
		this.reconnect = true;
		this.blocked = true;
		writeToScreen("Handling reconnection, wait...");
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
		System.out.println("END OF RECONNECTION");
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
			System.out.println("MY TIMEOUT");
			state = GameState.I_TIMED_OUT;
		}
		else{
			System.out.println("opp TIMEOUT");
			state = GameState.OPP_TIMED_OUT;
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
		while (reconnectOnRead)
		{	
			if(!state.equals(GameState.PROCEED)){
				break;
			}
			reconnectOnRead= false;
			try{
				clientToOpponent.writeObject(move);
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
			while (reconnectOnRead)
			{	
				if(!state.equals(GameState.PROCEED)){
					break;
				}
				reconnectOnRead=false;
				try {
					move = (String)opponentIn.readObject();
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
						theClient.logger.print_error("While reading from socket: " + e.getMessage());
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
			writeToScreen("Waiting for opponent to connect ...");
			opponentSocket = serverSocket.accept();
			clientPlayer = red;
			writeToScreen("Opponent Was Connected ,You Are The Red Player!");
		
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
			writeToScreen("You Are The Blue Player!");
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
	
}
