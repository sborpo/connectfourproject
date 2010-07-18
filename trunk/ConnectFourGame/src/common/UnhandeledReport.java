package common;

import java.io.Serializable;

public  class UnhandeledReport implements Serializable {
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clientName == null) ? 0 : clientName.hashCode());
		result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
		return result;
	}
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
	public UnhandeledReport(String gameId, String clientName,
			String gameResult, String winner) {
		super();
		this.gameId = gameId;
		this.clientName = clientName;
		this.gameResult = gameResult;
		this.winner = winner;
	}
	@Override
	public String toString() {
		return "UnhandeledReport [clientName=" + clientName + ", gameId="
				+ gameId + ", gameResult=" + gameResult + ", winner=" + winner
				+ "]";
	}
	public String getGameId() {
		return gameId;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getGameResult() {
		return gameResult;
	}
	public void setGameResult(String gameResult) {
		this.gameResult = gameResult;
	}
	public String getWinner() {
		return winner;
	}
	public void setWinner(String winner) {
		this.winner = winner;
	}
	private String gameId;
	private String clientName;
	private String gameResult;
	private String winner;
}
