package gameManager;

import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class Game {

	private String gameId;
	private Player red;
	private Player blue;
	private Board gameBoard;
	private Player plays;

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
	}

	public void startOnlineGame(int clientPort, String opponentHost,int opponentPort, boolean startsGame) {
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
			return;
		}

		plays = red;
		//String playerStr;
		GameState state = GameState.PROCEED;
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
			if (plays.equals(clientPlayer)) {
				// write your move
				clientToOpponent.println(colnum);
			}
			nextPlayer();
		}
		gameBoard.PrintBoard();
		if (state.equals(GameState.TIE)) {
			System.out.println("The game ended with Tie!\n\n");
		}
		String won = state.equals(GameState.RED_WON) ? red.getName() : blue.getName();

		System.out.println(won + " player has won the game!\n\n");
		
		
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
		return;

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
		System.out.println("Adding player: " + player2);
		blue = new Player(Player.Color.BLUE,player2);
	}
	 
	public Player getPlayer(Player.Color pColor){
		return pColor.equals(Player.Color.RED) ? red : blue;
	}

	public String getId(){
		return gameId;
	}

//	public static void main(String[] args) {
//		Game game = new Game();
//		game.startGame();
//	}
}
