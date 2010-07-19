package ConnectFourServer;

import java.io.Serializable;

/**
 * This is a wrapper class for an information that is sent
 * to the clients about the current online games
 *
 */
public class GameForClient implements Serializable{

	//Games properties
	private String gameId;
	private String playerOneName;
	private String playerTwoName;
	
	public GameForClient(String gameId,String playerOne,String playerTwo)
	{
		this.gameId=gameId;
		playerOneName=playerOne;
		playerTwoName= playerTwo;
	}
	
	/**
	 * Returns true if the game is open , false otherwise. Open in means of empty place for
	 * another user to join the game
	 * @return
	 */
	public boolean isOpen()
	{
		return (playerTwoName==null);
	}
	
	/**
	 * Returns the game Id
	 * @return
	 */
	public String getGameId() {
		return gameId;
	}
	
	/**
	 * Sets the gameId
	 * @param gameId
	 */
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	/**
	 * Returns the first player (the player which started the game )
	 * @return
	 */
	public String getPlayerOneName() {
		return playerOneName;
	}
	
	/**
	 * Sets the first player name (the player which started the game )
	 * @param playerOneName
	 */
	public void setPlayerOneName(String playerOneName) {
		this.playerOneName = playerOneName;
	}
	
	/**
	 * Returns the second player (the player which joined the game )
	 * @return
	 */
	public String getPlayerTwoName() {
		return playerTwoName;
	}
	
	/**
	 * Sets the second player name (the player which joined the game )
	 * @param playerOneName
	 */
	public void setPlayerTwoName(String playerTwoName) {
		this.playerTwoName = playerTwoName;
	}
	
	
	
	
}
