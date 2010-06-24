package gameManager;

import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;
import gameManager.Player.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import theProtocol.ClientServerProtocol;

import ConnectFourClient.TheClient;

public class Game implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String gameId;
	private Player red;
	private Player blue;
	private Board gameBoard;
	private Player plays;
	private GameState state;
	private ArrayList<String> gameHistory;
	private String gameReport;
	
	public boolean isGameFull()
	{
		return (blue != null);
	}
	
	public boolean isPlayer(String playerName){
		boolean res = false;
		if((red != null && red.getName().equals(playerName)) ||
		   (blue != null && blue.getName().equals(playerName))){
			res = true;
		}
		
		return res;
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
							colnum	=	Integer.parseInt(inLine);
						}
					}
				} else {
					System.out.println("Waiting For Opponent Move!:\n");
					colnum = Integer.parseInt(opponentIn.readLine());
				}
				state = gameBoard.playColumn(colnum, plays.getColor());
			} catch (IllegalMove e) {
				System.out.println("Illegal Move!!! Please Retry!\n\n");
				continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//send the move to the viewers
			String colorStr=plays.getColor().equals(Color.BLUE) ? "Blue" : "Red";
			String moveMsg = "GAME_MOVE" + " " + plays.getName() + " " + String.valueOf(colnum);
			String[] parsed = prot.parseCommand(moveMsg);
			if(parsed == null){
				System.out.println(prot.result + ". Bad move report: "+ moveMsg);
			}
			theClient.getTransmitWaiter().sendMoveToViewers(moveMsg);
			
			//add the move to the game history
			gameHistory.add(moveMsg);
			
			if (plays.equals(clientPlayer)) {
				// write your move
				clientToOpponent.println(colnum);
			}
			nextPlayer();
		}
		gameBoard.PrintBoard();
		String winner = null;
		if (state.equals(GameState.TIE)) {
			System.out.println("The game ended with Tie!\n");
		}
		else{
			winner = state.equals(GameState.RED_WON) ? red.getName() : blue.getName();
			System.out.println(winner + " player has won the game!\n");
		}
		Integer gameRes = (state.equals(GameState.TIE)) ? 0 : 1;
		gameReport = "GAME_REPORT" + " " + this.getId() + " " + theClient.getClientName() + " " + gameRes.toString() + " " + winner;
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
	
	public void addPlayer(String player2){
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
