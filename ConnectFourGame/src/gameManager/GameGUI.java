package gameManager;

import gameManager.BoardGUI.GameState;
import gameManager.BoardGUI.IllegalMove;
import gameManager.Player.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import javax.swing.*;
import java.awt.event.*;

import theProtocol.ClientServerProtocol;

import ConnectFourClient.TheClient;

public class GameGUI implements Serializable{
	
	public static class TimeEnded extends Exception{}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String gameId;
	private Player red;
	private Player blue;
	private HashMap<String,Player> watchers;
	protected BoardGUI gameBoard;
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

	public GameGUI(String name1,String name2,String gameId) {
		red = new Player(Player.Color.RED,name1);
		if(name2 != null){
			blue = new Player(Player.Color.BLUE,name2);
		}
		else{
			blue = null;
		}
		this.gameId = gameId;
		gameBoard = new BoardGUI();
		gameHistory = new ArrayList<String>();
		gameReport = "";
		watchers = new HashMap<String,Player>();
		
	}

	public String startOnlineGame(int clientPort, String opponentHost,int opponentPort, boolean startsGame, TheClient theClient) {
		
		Player clientPlayer;
		ServerSocket serverSocket = null;
		Socket opponentSocket = null;
		BufferedReader stdin = null;
		BufferedReader opponentIn = null;
		PrintStream clientToOpponent = null;
		
		try {
			if (startsGame == true) {
				serverSocket = new ServerSocket(clientPort);
				// can be a timeout how much to wait for an opponent
				System.out.println("Waiting for opponent to connect ...\n");
				JOptionPane.showMessageDialog(null, "Waiting for opponent to connect ...");
				opponentSocket = serverSocket.accept();
				serverSocket.close();
				serverSocket=null;
				clientPlayer = red;
				//System.out.println("Opponent Was Connected \n");
				//System.out.println("You Are The Red Player \n");
				JOptionPane.showMessageDialog(null, "Opponent Was Connected ");
				JOptionPane.showMessageDialog(null, "You Are The Red Player ");
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
				//System.out.println("You Are The Blue Player \n");
				JOptionPane.showMessageDialog(null, "You Are The Blue Player ");
			}
			//------This way we will know that the other side disconnected-----
			opponentSocket.setKeepAlive(true);
			//-----------
			stdin = new BufferedReader(new InputStreamReader(System.in));
			opponentIn = new BufferedReader(new InputStreamReader(opponentSocket.getInputStream()));
			clientToOpponent = new PrintStream(opponentSocket.getOutputStream());
			if(clientPlayer.equals(blue)){
				clientToOpponent.println(clientPlayer.getName());
			}
			else{
				String name2 = opponentIn.readLine();
				if( name2 != null){
					addPlayer(name2);
				}
			}
		} catch (IOException e) {
			// TODO Handle serverSocket initialization problem
			e.printStackTrace();
			return gameReport;
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
//				System.out.println("The client is: "+ clientPlayer.getName());
//				System.out.println("Plays: " + plays.getName());
				JOptionPane.showMessageDialog(null, "The client is: "+ clientPlayer.getName());
				JOptionPane.showMessageDialog(null, "Plays: " + plays.getName());
				
				if (plays.equals(clientPlayer)) {
//					System.out.println("Please Enter Your Move:\n");
//					JOptionPane.showMessageDialog(null, "Please Enter Your Move ");
					while(colnum == -1){
						inLine = stdin.readLine();
						if(inLine.equals("")){
//							System.out.println("Empty move, try again...");
							JOptionPane.showMessageDialog(null, "Empty move, try again... ");
						}
						else{
							colnum	=	Integer.parseInt(inLine);
						}
					}
				} else {
//					System.out.println("Waiting For Opponent Move!:\n");
					JOptionPane.showMessageDialog(null, "Waiting For Opponent Move! ");
					boolean reconnectOnRead= true;
					while (reconnectOnRead)
					{	
						reconnectOnRead=false;
						try{
						//try to read from the opponent	
							colnum = Integer.parseInt(opponentIn.readLine());
						}
						catch (IOException ex)
						{
							reconnectOnRead=true;
							handleReconnectionProcess(opponentSocket, serverSocket, opponentIn, startsGame, clientToOpponent, opponentHost, opponentPort);
						}
					}
				}
				state = gameBoard.playColumn(colnum, plays.getColor());
			} 
			
			catch (IllegalMove e) {
				System.out.println("Illegal Move!!! Please Retry!\n\n");
				JOptionPane.showMessageDialog(null, "Illegal Move!!! Please Retry! ");
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
			String colorStr=plays.getColor().equals(Color.BLUE) ? "Blue" : "Red";
			String moveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEMOVE,
																			plays.getName(),
																			String.valueOf(colnum)});
			
			String[] parsed = prot.parseCommand(moveMsg);
			if(parsed == null){
				System.out.println(prot.result + ". Bad move report: "+ moveMsg);
			}
			//theClient.getTransmitWaiter().sendMoveToViewers(moveMsg);
			
			//add the move to the game history
			gameHistory.add(moveMsg);
			
			if (plays.equals(clientPlayer)) {
				// write your move
				boolean reconnectOnRead= true;
				while (reconnectOnRead)
				{	
					reconnectOnRead= false;
					clientToOpponent.println(colnum);
					if (clientToOpponent.checkError())
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
		if (state.equals(GameState.TIE)) {
			//System.out.println("The game ended with Tie!\n");
			JOptionPane.showMessageDialog(null, "The game ended with Tie! ");
		}
		else{
			winner = state.equals(GameState.RED_WON) ? red.getName() : blue.getName();
			//System.out.println(winner + " player has won the game!\n");
			JOptionPane.showMessageDialog(null, winner + " player has won the game!");
		}
		Integer gameRes = (state.equals(GameState.TIE)) ? 0 : 1;
		gameReport = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEREPORT,
																	 this.getId(),
																	 theClient.getClientName(),
																	 gameRes.toString(),
																	 winner});
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
		return gameReport;

	}	
	private void handleReconnectionProcess(Socket opponentSocket,ServerSocket serverSocket,BufferedReader opponentIn,boolean startGame, PrintStream clientToOpponent, String opponentHost, int opponentPort) throws  TimeEnded {
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
					opponentIn = new BufferedReader(new InputStreamReader(opponentSocket.getInputStream()));
					clientToOpponent = new PrintStream(opponentSocket.getOutputStream());
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
						opponentIn = new BufferedReader(new InputStreamReader(opponentSocket.getInputStream()));
						clientToOpponent = new PrintStream(opponentSocket.getOutputStream());
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
		//System.out.println("Please Choose who will be Red and who will be Blue\n");
		//JOptionPane.showMessageDialog(null,"Please Choose who will be Red and who will be Blue");

		Random randGen = new Random();
		String playerStr;
		int player = randGen.nextInt(2);
		plays = (player == 0) ? red : blue;
		GameState state = GameState.PROCEED;
		String playerStarts = (player == 0) ? "Red" : "Blue";
		//System.out.println(playerStarts + " Player will start the game!\n");
		JOptionPane.showMessageDialog(null,playerStarts + " Player will start the game!");
		gameBoard.PrintBoard();
		while (state.equals(GameState.PROCEED)) {
			
			gameBoard.PrintBoard();
			playerStr = (plays.getColor().equals(Player.Color.RED)) ? "Red"
					: "Blue";
			//System.out.println("\n" + playerStr+ "Player, Please Enter You column number: ");
			//JOptionPane.showMessageDialog(null,playerStr+ "Player, Please Enter You column number: ");
			
			int colnum = -1;
			//BufferedReader stdin;

			try {
				//stdin = new BufferedReader(new InputStreamReader(System.in));
				//colnum = Integer.parseInt(stdin.readLine());
				colnum = getNextMove();
				JOptionPane.showMessageDialog(null,"The next move is: " + colnum + " played by: " + plays.getColor());
				state = gameBoard.playColumn(colnum, plays.getColor());
			
			} catch (IllegalMove e) {
				//System.out.println("Illegal Move!!! Please Retry!\n\n");
				JOptionPane.showMessageDialog(null,"Illegal Move!!! Please Retry!");
				continue;
			}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			nextPlayer();

		}
		
		gameBoard.PrintBoard();
		
		if (state.equals(GameState.TIE)) {
			//System.out.println("The game ended with Tie!\n\n");
			JOptionPane.showMessageDialog(null,"The game ended with Tie!");
		}
		String won = state.equals(GameState.RED_WON) ? "Red" : "Blue";

		//System.out.println(won + " player has won the game!\n\n");
		JOptionPane.showMessageDialog(null,won + " player has won the game!");
		
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

	private int getNextMove(){
		ConnectFourListener listener = new ConnectFourListener(this);
		int colmun = listener.nextMove;
		return colmun;
	}

	public String getId(){
		return gameId;
	}
	
	public ArrayList<String> getGameHistory(){
		return gameHistory;
	}

	public static void main(String[] args) {
		GameGUI game = new GameGUI("liat","gabby","liat12345");
		game.startGame();
	}
}
