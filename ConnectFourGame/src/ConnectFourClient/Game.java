package ConnectFourClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import ConnectFourClient.Board.GameState;
import ConnectFourClient.Board.IllegalMove;
import ConnectFourServer.MainServer;

public class Game {
	
	private Player red;
	private Player blue;
	private Board gameBoard;
	private Player plays;
	
	public Game() {
		red= new Player(Player.Color.RED);
		blue = new Player(Player.Color.BLUE);
		gameBoard= new Board();
	}
	
	public void startGame()
	{
		System.out.println("Please Choose who will be Red and who will be Blue\n");
		Random randGen = new Random();
		String playerStr;
		int player=randGen.nextInt(2);
		plays = (player==0) ? red : blue;
		GameState state= GameState.PROCEED;
		String playerStarts= (player==0) ? "Red" : "Blue";
		System.out.println(playerStarts+" Player will start the game!\n");
		while (state.equals(GameState.PROCEED))
		{
			gameBoard.PrintBoard();
			playerStr= (plays.getColor().equals(Player.Color.RED)) ? "Red" : "Blue";
			System.out.println("\n"+playerStr+"Player, Please Enter You column number: ");
			int colnum=-1;
			BufferedReader stdin;
			try {
				  stdin= new BufferedReader(new InputStreamReader(System.in));
				  colnum=Integer.parseInt(stdin.readLine());
				state=gameBoard.playColumn(colnum, plays.getColor());
			} catch (IllegalMove e) {
				System.out.println("Illegal Move!!! Please Retry!\n\n");
				continue;
			}
			catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			nextPlayer();
			
			
		}
		gameBoard.PrintBoard();
		if (state.equals(GameState.TIE)) {
			System.out.println("The game ended with Tie!\n\n");
		}
		String won = state.equals(GameState.RED_WON)? "Red" : "Blue";
		
		System.out.println(won+ " player has won the game!\n\n");
		return;
		
	}
	
	private void nextPlayer() {
		if (plays.getColor().equals(Player.Color.RED)) {
			plays= blue;
		}
		else
		{
			plays= red;
		}
		
	}

	public static void main (String [] args)
	{
		Game game= new Game();
		game.startGame();
	}
}
