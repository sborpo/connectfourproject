package gameManager;

import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;
import gameManager.Player.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import common.UnhandeledReport;

import theProtocol.ClientServerProtocol;

import ConnectFourClient.TheClient;

public class Game implements Serializable{
	
	public static class TimeEnded extends Exception{}
	/**
	 * 
	 */
	
	public static final String Surrended="SURRENDED";
	private static final long serialVersionUID = 1L;
	private String gameId;
	private Player red;
	private Player blue;
	private HashMap<String,Player> watchers;
	private Board gameBoard;
	private Player plays;
	private GameState state;
	private ArrayList<String> gameHistory;
	private String gameReport;
	
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

	public Game(String name1,String name2,String gameId) {
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

	public UnhandeledReport startOnlineGame(int clientPort, String opponentHost,int opponentPort, boolean startsGame, TheClient theClient) {
		Player clientPlayer;
		ServerSocket serverSocket = null;
		Socket opponentSocket = null;
		BufferedReader stdin = null;
		ObjectInputStream opponentIn = null;
		ObjectOutputStream clientToOpponent = null;
		try {
			if (startsGame == true) {
				serverSocket = new ServerSocket(clientPort);
				// can be a timeout how much to wait for an opponent
				System.out.println("Waiting for opponent to connect ...\n");
				opponentSocket = serverSocket.accept();
				clientPlayer = red;
				System.out.println("Opponent Was Connected \n");
				System.out.println("You Are The Red Player \n");
			
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
				System.out.println("You Are The Blue Player \n");

			}
			//------This way we will know that the other side disconnected-----
			opponentSocket.setKeepAlive(true);
			//-----------
			stdin = new BufferedReader(new InputStreamReader(System.in));
			clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
			opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
			if(clientPlayer.equals(blue)){
				clientToOpponent.writeObject((clientPlayer.getName()));
			}
			else{
				String name2 = null;
				try {
					name2 = (String)opponentIn.readObject();
				} catch (ClassNotFoundException e) {
					//cannot be
				}
				if( name2 != null){
					addPlayer(name2);
				}
			}
		} catch (IOException e) {
			// TODO Handle serverSocket initialization problem
			e.printStackTrace();
			return new UnhandeledReport(getId(), theClient.getClientName(), "0", "no-winner");
		}

		plays = red;
		//String playerStr;
		state = GameState.PROCEED;
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		while (state.equals(GameState.PROCEED)) {
			gameBoard.PrintBoard();
			int colnum = -1;
			String inLine = null;
			try {
				System.out.println("The client is: "+ clientPlayer.getName());
				System.out.println("Plays: " + plays.getName());
				if (plays.equals(clientPlayer)) {
					System.out.println("Please Enter Your Move:\n");
					while(colnum == -1){
						inLine = stdin.readLine();
						if(inLine.equals("")){
							System.out.println("Empty move, try again...");
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
				} else {
					System.out.println("Waiting For Opponent Move!:\n");
					boolean reconnectOnRead= true;
					while (reconnectOnRead)
					{	
						reconnectOnRead=false;
						try{
						//try to read from the opponent	
							try {
								String move=(String)opponentIn.readObject();
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
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					
						}
						catch (IOException ex)
						{
							reconnectOnRead=true;
							handleReconnectionProcess(opponentSocket, serverSocket, opponentIn, startsGame, clientToOpponent, opponentHost, opponentPort);
						}
					}
				}
				//the clients not surrended
				if (state.equals(GameState.PROCEED))
				{
					state = gameBoard.playColumn(colnum, plays.getColor());
				}
			} catch (IllegalMove e) {
				System.out.println("Illegal Move!!! Please Retry!\n\n");
				continue;
			} 
			catch (TimeEnded e)
			{
				//TODO what to do after retires
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//send the move to the viewers
			//String colorStr=plays.getColor().equals(Color.BLUE) ? "Blue" : "Red";
			String move= (state.equals(GameState.I_SURRENDED) || state.equals(GameState.OPPONENT_SURRENDED)) ? Surrended : String.valueOf(colnum);
			String moveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEMOVE,
																			plays.getName(),
																			move});
			
			String[] parsed = prot.parseCommand(moveMsg);
			if(parsed == null){
				System.out.println(prot.result + ". Bad move report: "+ moveMsg);
			}
			theClient.getTransmitWaiter().sendMoveToViewers(moveMsg);
			
			//add the move to the game history
			gameHistory.add(moveMsg);
			
			if (plays.equals(clientPlayer)) {
				// write your move
				boolean reconnectOnRead= true;
				while (reconnectOnRead)
				{	
					reconnectOnRead= false;
					try{
						clientToOpponent.writeObject(move);
					}
					catch (IOException ex)
					{
						reconnectOnRead= true;
						try {
							handleReconnectionProcess(opponentSocket, serverSocket, opponentIn, startsGame, clientToOpponent, opponentHost, opponentPort);
						} catch (TimeEnded e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					}
				}
				
			}
			nextPlayer();
		}
		gameBoard.PrintBoard();
		String winner = null;
		winner=decideWinner();
		Integer gameRes = (state.equals(GameState.TIE)) ? 0 : 1;
		gameReport = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEREPORT,
																	 this.getId(),
																	 theClient.getClientName(),
																	 gameRes.toString(),
																	 winner,
																	 "dummy"});
		String[] parsed = prot.parseCommand(gameReport);
		if(parsed == null){
			System.out.println(prot.result + ". Bad game report: "+ gameReport);
		}
		theClient.getTransmitWaiter().sendMoveToViewers(gameReport);
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
			System.out.println("The game ended with Tie!\n");
			return "tie";
		}
		if (state.equals(GameState.I_SURRENDED))
		{
			System.out.println("You have surrended!\n");
			winner = (plays.getColor().equals(Color.BLUE)) ? red.getName() : blue.getName();
			System.out.println(winner + " player has won the game!\n");
			return winner;
		}
	    if (state.equals(GameState.OPPONENT_SURRENDED))
		{
			System.out.println("The opponent has surrended!\n");
			winner = (plays.getColor().equals(Color.RED)) ? red.getName() : blue.getName();
			System.out.println(winner + " player has won the game!\n");
			return winner;
		}
	    winner=state.equals(GameState.RED_WON) ? red.getName() : blue.getName();
		System.out.println(winner + " player has won the game!\n");
		return winner;
		
	}

	private void handleReconnectionProcess(Socket opponentSocket,ServerSocket serverSocket,ObjectInputStream opponentIn,boolean startGame, ObjectOutputStream clientToOpponent, String opponentHost, int opponentPort) throws  TimeEnded {
		try {
			opponentIn.close();
			clientToOpponent.close();
			opponentSocket.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if (startGame)
		{
			try {
				serverSocket.setSoTimeout(20000);
			} catch (SocketException e1) {
				// There is a problem with the server socket
				e1.printStackTrace();
			}
			int retries=0;
			while (retries<3)
			{
				try {
					
					opponentSocket = serverSocket.accept();
					opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
					clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
					return;
				} 
				catch (SocketTimeoutException e) {
					//There was a timeout , so the game must be closed and reoported as a winning
					throw new TimeEnded();
				}
				catch (IOException e) {
					// The client cannot start the server socket and reconnect
					e.printStackTrace();
					retries++;
					//sleep for five second
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
					}
				}
			}
		
		}
		//the client was not the host of the game
		else
		{
			int retries=0;
			while (retries<3)
			{
					InetAddress address = null;
					try {
						address = InetAddress.getByName(opponentHost);
						opponentSocket = new Socket(address, opponentPort);
						opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
						clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
						return;
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 catch (IOException e) {
						try {
							Thread.sleep(20000);
						} catch (InterruptedException e1) {
						}
						e.printStackTrace();
					}
					 retries++;
			}
		}
		throw new TimeEnded();
}


	public void startGame() {
		System.out
				.println("Please Choose who will be Red and who will be Blue\n");
		Random randGen = new Random();
		String playerStr;
		int player = randGen.nextInt(2);
		plays = (player == 0) ? red : blue;
		GameState state = GameState.PROCEED;
		String playerStarts = (player == 0) ? "Red" : "Blue";
		System.out.println(playerStarts + " Player will start the game!\n");
		while (state.equals(GameState.PROCEED)) {
			gameBoard.PrintBoard();
			playerStr = (plays.getColor().equals(Player.Color.RED)) ? "Red"
					: "Blue";
			System.out.println("\n" + playerStr
					+ "Player, Please Enter You column number: ");
			int colnum = -1;
			BufferedReader stdin;
			try {
				stdin = new BufferedReader(new InputStreamReader(System.in));
				colnum = Integer.parseInt(stdin.readLine());
				state = gameBoard.playColumn(colnum, plays.getColor());
			} catch (IllegalMove e) {
				System.out.println("Illegal Move!!! Please Retry!\n\n");
				continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nextPlayer();

		}
		gameBoard.PrintBoard();
		if (state.equals(GameState.TIE)) {
			System.out.println("The game ended with Tie!\n\n");
		}
		String won = state.equals(GameState.RED_WON) ? "Red" : "Blue";

		System.out.println(won + " player has won the game!\n\n");
		
		return;

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

//	public static void main(String[] args) {
//		Game game = new Game();
//		game.startGame();
//	}
}
