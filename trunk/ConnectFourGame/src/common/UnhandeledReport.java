package common;

import java.io.Serializable;

public  class UnhandeledReport implements Serializable {
	
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
