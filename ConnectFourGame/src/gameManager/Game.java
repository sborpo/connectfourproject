package gameManager;

import java.io.Serializable;
import java.util.ArrayList;
import ConnectFourClient.TheClient;
import common.UnhandeledReport;

/**
 * This is the game interface. All implementors should implement the given methods
 * The javadoc is in the implementation class
 *
 */
public interface Game {
	/**
	 * Represents the game result , if there was a winner or the game ended without one (problem ,
	 * or TIE)
	 *
	 */
	public static class gameRes { 
		public static final boolean NO_WINNER = false;
		public static final boolean WINNER = true;
	};
	
	/**
	 * Represents the gameWinner options , or there was no winner, or the game not played
	 * @author Boris
	 *
	 */
	public static class gameWinner{
		public static final String NO_WINNER = "0";
		public static final String GAME_NOT_PLAYED = "-1";
	};
	
	/**
	 * Returns true of the game already have two players, otherwise returns false
	 * @return
	 */
	public boolean isGameFull();
	
	/**
	 * Returns true if the given player Name is one of the participants of the game
	 * @param playerName
	 * @return
	 */
	public Player isPlayer(String playerName);
	
	/**
	 * Adds a new watcher into the game.
	 * @param watchName
	 * @param playerName
	 * @return
	 */
	public Player addWatcher(String watchName, String playerName);
	
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
				int opponentTransmitWaiterPort,
				boolean startsGame, TheClient theClient);
 	/**
 	 * Returns  elapsed time of the current  move
 	 */
 	public Integer getCurrMoveTime();
 	
 	/**
 	 * Adds the given player to the game
 	 * @param player
 	 */
 	public void addPlayer(String player);
 	
 	/**
 	 * Removes the second player of the game (Not the host)
 	 */
 	public void removeSecondPlayer();
 	
 	/**
 	 * Returns the player of the given color
 	 * @param pColor
 	 * @return
 	 */
 	public Player getPlayer(Player.Color pColor);
 	
 	/**
 	 * Returns the gameId
 	 * @return
 	 */
 	public String getId();
 	
 	/**
 	 * Returns the moves history of the game
 	 * @return
 	 */
 	public ArrayList<String> getGameHistory();
 	
 	/**
 	 * Resets the connection between the two players
 	 */
 	public void resetConnection();
 	
 	/**
 	 * This method handles the situation that the opponent Surrenders
 	 */
	public void opponentSurrender();
}
