package gameManager;

import java.io.Serializable;
import java.util.ArrayList;
import ConnectFourClient.TheClient;
import common.UnhandeledReport;

public interface Game {
	public static class gameRes { 
		public static final boolean NO_WINNER = false;
		public static final boolean WINNER = true;
	};
	
	public static class gameWinner{
		public static final String NO_WINNER = "0";
		public static final String GAME_NOT_PLAYED = "-1";
	};
	
	public boolean isGameFull();
	public Player isPlayer(String playerName);
	public Player addWatcher(String watchName, String playerName);
 	public UnhandeledReport startOnlineGame(int clientPort, String opponentHost,int opponentPort, 
				int opponentTransmitWaiterPort,
				boolean startsGame, TheClient theClient);
	//public void startGame();
 	public Integer getCurrMoveTime();
 	public void addPlayer(String player);
 	public void removeSecondPlayer();
 	public Player getPlayer(Player.Color pColor);
 	public String getId();
 	public ArrayList<String> getGameHistory();
 	public void resetConnection();
	public void opponentSurrender();
}
