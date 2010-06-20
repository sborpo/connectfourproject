package ConnectFourServer;

import java.io.Serializable;

public class GameForClient implements Serializable{

	private String gameId;
	private String playerOneName;
	private String playerTwoName;
	private int playerOneRank;
	private int playerTwoRank;
	
	public GameForClient(String gameId,String playerOne,String playerTwo)
	{
		this.gameId=gameId;
		playerOneName=playerOne;
		playerTwoName= playerTwo;
	}
	public boolean isOpen()
	{
		return (playerTwoName==null);
	}
	public String getGameId() {
		return gameId;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	public String getPlayerOneName() {
		return playerOneName;
	}
	public void setPlayerOneName(String playerOneName) {
		this.playerOneName = playerOneName;
	}
	public String getPlayerTwoName() {
		return playerTwoName;
	}
	public void setPlayerTwoName(String playerTwoName) {
		this.playerTwoName = playerTwoName;
	}
	public int getPlayerOneRank() {
		return playerOneRank;
	}
	public void setPlayerOneRank(int playerOneRank) {
		this.playerOneRank = playerOneRank;
	}
	public int getPlayerTwoRank() {
		return playerTwoRank;
	}
	public void setPlayerTwoRank(int playerTwoRank) {
		this.playerTwoRank = playerTwoRank;
	}
	
	
}
