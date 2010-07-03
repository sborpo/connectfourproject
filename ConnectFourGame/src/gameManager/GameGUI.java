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

import common.UnhandeledReport;
import common.Timer.TimeOutEvent;
import common.Timer.TimerListener;

import theProtocol.ClientServerProtocol;

import ConnectFourClient.MainFrame;
import ConnectFourClient.TheClient;

public class GameGUI extends JDialog implements MouseListener,TimerListener,Runnable,Game,WindowListener {
	

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
	private Board gameBoard = null;
	private Player plays = null;
	private GameState state = null;
	private ArrayList<String> gameHistory = null;
	private String gameReport = null;
	private Pending pending = null;
	private Player clientPlayer = null;
	private Player opponentPlayer = null;
	private boolean iSurrend=false;
	private ServerSocket serverSocket = null;
	private Socket opponentSocket = null;
	private ObjectInputStream opponentIn = null;
	private ObjectOutputStream clientToOpponent = null;
	
	
	//START ONLINE GAME PARAMETERS
	private boolean startedGame = false;
	private final int moveTime = 20;
	private int clientGamePort = TheClient.unDEFport;
	private String opponentHost = null;
	private int opponentPort = TheClient.unDEFport;
	private int opponentTransmitWaiterPort = TheClient.unDEFport;
	private int opponentGamePort = TheClient.unDEFport;	
	
	//GUI COMPONENTS
	private MainFrame mainFrame;
	private TheClient theClient;
	private JButton startGame;
    private JButton surrender;
	private JPanel boardPane;
	private Box upperBox;
	private JButton[][] slots;
	private JLabel consoleArea;
	private JLabel connAs1;
	private JLabel connAs2;
	private int clickedColNum;
	private String clickedByPlayer;
	private UnhandeledReport gameResult;
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
	


	public GameGUI(String name1,String name2,String gameId,MainFrame mainFrame, int clientGamePort, String opponentHost, int opponentGamePort, boolean startedGame, TheClient theClient) {
		this.mainFrame=mainFrame;
		this.clientGamePort=clientGamePort;
		this.opponentHost=opponentHost;
		pending = new Pending();
		this.opponentGamePort=opponentGamePort;
		this.startedGame=startedGame;
		red = new Player(Player.Color.RED,name1);
		if(name2 != null){
			blue = new Player(Player.Color.BLUE,name2);
		}
		else{
			blue = null;
		}
		this.gameId = gameId;
		gameBoard = new BoardGUI(this);
		gameHistory = new ArrayList<String>();
		gameReport = "";
		watchers = new HashMap<String,Player>();
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		 upperBox = Box.createVerticalBox();
		boardPane= new JPanel();
		adjustGrid();
		upperBox.add(boardPane);
		Box lowerBox = Box.createHorizontalBox();
		startGame=new JButton("StartTheGame");
		startGame.addMouseListener(this);
		startGame.setHorizontalAlignment(SwingConstants.LEFT);
		surrender= new JButton("Surrender");
		surrender.setName("SURRENDED");
		surrender.addMouseListener(this);
		surrender.setHorizontalAlignment(SwingConstants.RIGHT);
		lowerBox.add(startGame);
		lowerBox.add(surrender);
		Box lowerBox3 = Box.createHorizontalBox();
		Box TopMost = Box.createHorizontalBox();
		 connAs1= new JLabel();
		 connAs2= new JLabel();
		TopMost.add(connAs1);
		TopMost.add(connAs2);
		lowerBox3.setSize(700, 200);
		consoleArea = new JLabel("Console Printer");
		lowerBox3.setAlignmentX(SwingConstants.LEFT);
		consoleArea.setAlignmentX(LEFT_ALIGNMENT);
		lowerBox3.add(consoleArea);
		this.theClient=theClient;
		gameBoard= new BoardGUI(this);
		this.getContentPane().add(TopMost);
		this.getContentPane().add(upperBox);
		this.getContentPane().add(lowerBox);
		this.getContentPane().add(lowerBox3);
		setSize(700,700);
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
		return gameResult;
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
			SwingUtilities.invokeAndWait(new BoardGUI.MessagePrinter(consoleArea,message));
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
		this.theClient = theClient;
		
		try{
			this.setupConnection();
		} catch (IOException e) {
			// TODO Handle serverSocket initialization problem
			e.printStackTrace();
			return new UnhandeledReport(getId(), theClient.getClientName(), "0", "no-winner");
		}

		plays = red;
		//String playerStr;
		state = GameState.PROCEED;
		//init timers for players
		red.setTimer(moveTime,this).pause().start();
		blue.setTimer(moveTime,this).pause().start();
		plays.getTimer().resume();
		
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		while (state.equals(GameState.PROCEED)) {
			int colnum = -1;
			String inLine = null;
			try {
				if (plays.equals(clientPlayer)) {
					writeToScreen("Please Click Your Move");
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
						if(inLine == null){
							//timeout occured
							break;
						}
						if(inLine.equals("")){
							writeToScreen("Empty move, try again...");
						}
						else{
							if (inLine.equals(Surrended))
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
						if (move.equals(Surrended))
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

		String winner = null;
		winner=decideWinner();
		Integer gameRes = (state.equals(GameState.TIE)) ? 0 : 1;
		this.closeConnection();
		return new UnhandeledReport(this.getId(), theClient.getClientName()	, gameRes.toString(), winner);

	}
	
	

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
	
	private void handleReconnectionProcess(){
		boolean succeeded = false;
		while(!plays.getTimer().isTimedOut() && succeeded == false){
			try {
				InetAddress address = InetAddress.getByName(opponentHost);
				Socket opponentTransmitSocket = new Socket(address, opponentTransmitWaiterPort);
				PrintWriter clientToOpponent = new PrintWriter(opponentTransmitSocket.getOutputStream(),true);
				clientToOpponent.println(ClientServerProtocol.SOCKETREFRESH);
				succeeded = true;
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				theClient.logger.print_error("Problem sending " + ClientServerProtocol.SOCKETREFRESH + "command to the opponent");
			}
		}
		if(succeeded){
			this.resetConnection();
		}
	}

	private String decideWinner() {
		String winner = null;
		switch(state){
			case TIE: 
				winner="noBody";
				break;
			case I_SURRENDED:
				winner= opponentPlayer.getName();
				break;
			case OPPONENT_SURRENDED:
				winner= clientPlayer.getName();
				break;
			case I_TIMED_OUT:
				winner= clientPlayer.getName();
				break;
			case OPP_TIMED_OUT:
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
		if (plays.getColor().equals(Player.Color.RED)) {
			plays = blue;
		} else {
			plays = red;
		}
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
		
		if (e.getComponent()==startGame)
		{
			startGame.removeMouseListener(this);
			startGame.setEnabled(false);
			gameThread= new Thread(this);
			gameThread.start();
		 return;
		}
		synchronized (pending) {
		if (pending.isPending())
		{
			String buttonName=((JButton)e.getComponent()).getName();
			if (buttonName.equals(Surrended))
			{
				this.clickedByPlayer=buttonName;
			}
			else
			{
				int colnum= (new Integer(Integer.parseInt(buttonName.split(" ")[1])));
				this.clickedByPlayer =(new Integer(colnum)).toString();
			}
				pending.setPending(false);
				pending.notify();
			}
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

//	public static void main(String[] args) {
//	
//	GameGUI game = new GameGUI("asf","asf","asf",null, 3455, null, 5325, true, null);
//	game.setVisible(true);
//	}

	@Override
	public void run() {
		System.out.println("STARTING GAME");
		gameResult=startOnlineGame(clientGamePort,(String)opponentHost,opponentGamePort,opponentTransmitWaiterPort,startedGame,theClient);
		this.setVisible(false);
		System.out.println("GUI IS FINISHED: "+gameResult );
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
		if (gameResult!=null)
		{
			//the game finished good
			return;
		}
		if ((red==null) || (blue==null))
		{
			gameResult=null;
			return;
		}
		if (gameThread!=null)
		{
			this.closeConnection();			
		}
		
	}

	public void resetConnection(){
		try {
			//closing old socket data
			this.closeConnection();
			//restarting
			if(startedGame){
				theClient.logger.print_info("Waiting for opponent to connect ...\n");
				opponentSocket = serverSocket.accept();
			}
			else{
				theClient.logger.print_info("Reconnecting to the opponent ...\n");
				InetAddress address = InetAddress.getByName(opponentHost);
				opponentSocket = new Socket(address, opponentPort);
			}
			opponentSocket.setKeepAlive(true);
			clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
			opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
		} catch (IOException e) {
			theClient.logger.print_error("Problem while reseting the connection: " + e.getMessage());
			e.printStackTrace();
		}
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
																		move});
		
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
		boolean timedOut = event.getValue();
		if(timedOut){
			this.timeOutHandler();
			plays.getTimer().stop();
		}
	}
	
	private void timeOutHandler(){
		if(plays.equals(clientPlayer)){
			state = GameState.I_TIMED_OUT;
		}
		else{
			state = GameState.OPP_TIMED_OUT;
		}
		
		theClient.logger.print_info("TIMEOUT");
		synchronized (pending) {
			if (pending.isPending())
			{
				pending.setPending(false);
				pending.notify();
			}
		}
		this.closeConnection();
	}
	
	private void sendMoveToOpponent(int colnum){
		String move = null;
		if(state.equals(GameState.I_SURRENDED)){
			move = Surrended;
		}
		else{
			move = String.valueOf(colnum);
		}
		// write your move
		boolean reconnectOnRead= true;
		while (reconnectOnRead)
		{	
			reconnectOnRead= false;
			try{
				clientToOpponent.writeObject(move);
			}
			//HANDLE CONNECTIONS PROBLEMS
			catch (IOException ex)
			{
				theClient.logger.print_error("While writing object: " + ex.getMessage());
				reconnectOnRead= true;
				handleReconnectionProcess();			
			}
		}
	}
		
	private String getOpponentMove(){
		String move = null;
		if(opponentIn != null){
			boolean reconnectOnRead= true;
			while (reconnectOnRead)
			{	
				reconnectOnRead=false;
				try {
					move = (String)opponentIn.readObject();
				} catch (IOException e) {
					if (iSurrend)
					{
						return null;
					}
					if(plays.getTimer().isTimedOut()){
						state=GameState.OPP_TIMED_OUT;
						theClient.logger.print_error("Time out while waiting for opponents move");
					}
					else{
						//HANDLE CONNECTION PROBLEMS
						theClient.logger.print_error("While reading from socket");
						reconnectOnRead=true;
						handleReconnectionProcess();
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return move;
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
		excahngeData(clientToOpponent,opponentIn);
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
