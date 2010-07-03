package gameManager;

import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;
import gameManager.Player.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import common.UnhandeledReport;
import common.Timer.TimeOutEvent;
import common.Timer.TimerListener;

import theProtocol.ClientServerProtocol;

import ConnectFourClient.TheClient;

public class GameImp implements Game,TimerListener, Serializable{
	/**
	 * 
	 */
	private final int moveTime = 20;
	public static final String Surrended="SURRENDED";
	protected static final long serialVersionUID = 1L;
	
	protected String gameId;
	protected Player red;
	protected Player blue;
	protected HashMap<String,Player> watchers;
	protected Board gameBoard;
	protected Player plays;
	protected GameState state;
	protected ArrayList<String> gameHistory;
	protected String gameReport;
	protected TheClient theClient = null;
	protected Player clientPlayer = null;
	
	//the connection
	protected boolean startedGame = false;
	protected int gamePort = TheClient.unDEFport;
	protected String opponentHost = null;
	protected int opponentPort = TheClient.unDEFport;
	protected int opponentTransmitWaiterPort = TheClient.unDEFport;
	protected ServerSocket serverSocket = null;
	protected Socket opponentSocket = null;
	protected BufferedReader stdin = null;
	protected ObjectInputStream opponentIn = null;
	protected ObjectOutputStream clientToOpponent = null;
	
	
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

	public GameImp(String name1,String name2,String gameId) {
		red = new Player(Player.Color.RED,name1);
		if(name2 != null){
			blue = new Player(Player.Color.BLUE,name2);
		}
		else{
			blue = null;
		}
		this.gameId = gameId;
		gameBoard = new Board();
		gameHistory = new ArrayList<String>();
		gameReport = "";
		watchers = new HashMap<String,Player>();
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
			stdin = new BufferedReader(new InputStreamReader(System.in));
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
			if(opponentSocket != null){
				opponentSocket.close();
			}
//			if(stdin != null){
//				stdin.close();
//			}
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
	
	private void setupConnection() throws IOException{
		if (startedGame == true) {
			serverSocket = new ServerSocket(gamePort);
			// can be a timeout how much to wait for an opponent
			theClient.logger.print_info("Waiting for opponent to connect ...");
			opponentSocket = serverSocket.accept();
			clientPlayer = red;
			theClient.logger.print_info("Opponent Was Connected");
			theClient.logger.print_info("You Are The Red Player");
		
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
			theClient.logger.print_info("You Are The Blue Player");

		}
		//------This way we will know that the other side disconnected-----
		opponentSocket.setKeepAlive(true);
		//-----------
		stdin = new BufferedReader(new InputStreamReader(System.in));
		clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
		opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
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
			}
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
		this.closeConnection();
		System.out.println("TIMEOUT RECEIVEDDDDDDDDDDDDDD");
		Thread.currentThread().interrupt();
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
	
	private String getClientMove(){
		String move = null;
		if(stdin != null){
			try {
				move = stdin.readLine();
			} catch (IOException e) {
				if(plays.getTimer().isTimedOut()){
					theClient.logger.print_error("Time out while waiting for input");
				}
				else{
					theClient.logger.print_error("Problem getting the move from user: " + e.getMessage());
					e.printStackTrace();
				}
			} 
		}
		return move;
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
	
	public UnhandeledReport startOnlineGame(int clientPort, String opponentHost,int opponentPort, 
												int opponentTransmitWaiterPort,
												boolean startsGame, TheClient theClient) {
		startedGame = startsGame;
		serverSocket = null;
		opponentSocket = null;
		stdin = null;
		opponentIn = null;
		clientToOpponent = null;
		this.gamePort = clientPort;
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
		//how starts the game
		plays = red;
		//Game started
		state = GameState.PROCEED;
		red.setTimer(moveTime,this).pause().start();
		blue.setTimer(moveTime,this).pause().start();
		plays.getTimer().resume();
		while (state.equals(GameState.PROCEED)) {
			gameBoard.PrintBoard();
			int colnum = -1;
			String inLine = null;
			try {
				//setting and starting the move timer
				theClient.logger.print_info("The client is: "+ clientPlayer.getName());
				theClient.logger.print_info("Plays: " + plays.getName());
				if (plays.equals(clientPlayer)) {
					theClient.logger.print_info("Please Enter Your Move:");
					while(colnum == -1){
						inLine = this.getClientMove();
						if(inLine == null){
							theClient.logger.print_error("Bad move or timeout");
							break;
						}
						if(inLine.equals("")){
							theClient.logger.print_error("Empty move, try again...");
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
					//send the move to the opponent
					if (plays.equals(clientPlayer)) {
						this.sendMoveToOpponent(colnum);				
					}
					
				} else {
					theClient.logger.print_info("Waiting For Opponent Move!:\n");
					//try to read from the opponent	
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
						//cannot be
					} 					
				}
				//the clients not surrender
				if (state.equals(GameState.PROCEED))
				{
					state = gameBoard.playColumn(colnum, plays.getColor());
				}
			} catch (IllegalMove e) {
				theClient.logger.print_error("Illegal Move!!! Please Retry!\n\n");
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
		gameBoard.PrintBoard();
		String winner = null;
		winner=decideWinner();
		Integer gameRes = (state.equals(GameState.TIE)) ? 0 : 1;
		
		try {
			if( opponentSocket != null){
				opponentSocket.close();
			}
			
			if(serverSocket != null){
				serverSocket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new UnhandeledReport(this.getId(), theClient.getClientName()	, gameRes.toString(), winner);

	}
	
	

	private String decideWinner() {
		String winner;
		if (state.equals(GameState.TIE)) {
			theClient.logger.print_info("The game ended with Tie!\n");
			return "tie";
		}
		if (state.equals(GameState.I_SURRENDED))
		{
			theClient.logger.print_info("You have surrended!\n");
			winner = (plays.getColor().equals(Color.BLUE)) ? red.getName() : blue.getName();
			return winner;
		}
	    if (state.equals(GameState.OPPONENT_SURRENDED))
		{
	    	theClient.logger.print_info("The opponent has surrended!\n");
			winner = (plays.getColor().equals(Color.RED)) ? red.getName() : blue.getName();
			return winner;
		}
	    winner=state.equals(GameState.RED_WON) ? red.getName() : blue.getName();
	    theClient.logger.print_info(winner + " player has won the game!\n");
		return winner;
		
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
	

	private void nextPlayer() {
		theClient.logger.print_info("Current player: "+plays.getName());
		plays.getTimer().pause();
		if (plays.getColor().equals(Player.Color.RED)) {
			plays = blue;
		} else {
			plays = red;
		}
		plays.getTimer().reset().resume();
		theClient.logger.print_info("Changed to player: "+plays.getName());
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

//	@Override
//	public void startGame() {
//		// TODO Auto-generated method stub
//		
//	}

}
