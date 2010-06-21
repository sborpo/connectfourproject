package common;

public class GameReport {
	
	@Override
	public String toString() {
		return "GameReport [clientName=" + clientName + ", gameId=" + gameId
				+ ", status=" + status + ", winner=" + winner + "]";
	}
	public GameReport(String gameId, String clientName, String status,
			String winner) {
		super();
		this.gameId = gameId;
		this.clientName = clientName;
		this.status = status;
		this.winner = winner;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getWinner() {
		return winner;
	}
	public void setWinner(String winner) {
		this.winner = winner;
	}
	
	String gameId;
	String clientName;
	String status;
	String winner;
	
	

}
