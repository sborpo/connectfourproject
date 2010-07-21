package common;

import java.io.Serializable;

/**
 * Represents the UnhandeledReport class
 */
public  class UnhandeledReport implements Serializable {
	private String gameId;
	private String clientName;
	private String gameResult;
	private String winner;
	
	/**
	 * Overrides the hashCode function for creating the hash code.
	 * @return result
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientName == null) ? 0 : clientName.hashCode());
		result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
		return result;
	}
	
	/**
	 * Overrides the equals function.
	 * @return true - if report is equal to the obj.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnhandeledReport other = (UnhandeledReport) obj;
		if (clientName == null) {
			if (other.clientName != null)
				return false;
		} else if (!clientName.equals(other.clientName))
			return false;
		if (gameId == null) {
			if (other.gameId != null)
				return false;
		} else if (!gameId.equals(other.gameId))
			return false;
		return true;
	}
	
	/**
	 * Constructor of the Unhandeled report
	 * @param gameId
	 * @param clientName
	 * @param gameResult
	 * @param winner
	 */
	public UnhandeledReport(String gameId, String clientName,
			String gameResult, String winner) {
		super();
		this.gameId = gameId;
		this.clientName = clientName;
		this.gameResult = gameResult;
		this.winner = winner;
	}
	
	/**
	 * Overrides the toString function.
	 * @return a string representing the report.
	 */
	@Override
	public String toString() {
		return "UnhandeledReport [clientName=" + clientName + ", gameId="
				+ gameId + ", gameResult=" + gameResult + ", winner=" + winner
				+ "]";
	}
	
	/**
	 * Gets the game ID.
	 * @return gameId
	 */
	public String getGameId() {
		return gameId;
	}
	
	/**
	 * Sets the game ID.
	 * @param gameId
	 */
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	
	/**
	 * Gets the client name.
	 * @return clientName
	 */
	public String getClientName() {
		return clientName;
	}
	
	/**
	 * Sets the client name.
	 * @param clientName
	 */
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
	/**
	 * Gets the game result.
	 * @return gameResult
	 */
	public String getGameResult() {
		return gameResult;
	}
	
	/**
	 * Sets game result.
	 * @param gameResult
	 */
	public void setGameResult(String gameResult) {
		this.gameResult = gameResult;
	}
	
	/**
	 * Gets the winner.
	 * @return winner
	 */
	public String getWinner() {
		return winner;
	}
	
	/**
	 * Sets a winner.
	 * @param winner
	 */
	public void setWinner(String winner) {
		this.winner = winner;
	}
}
