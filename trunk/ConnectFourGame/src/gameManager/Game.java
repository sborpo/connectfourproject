package gameManager;

import java.util.ArrayList;
import ConnectFourClient.TheClient;
import common.UnhandeledReport;

public interface Game {
	public boolean isGameFull();
	public Player isPlayer(String playerName);
	public Player addWatcher(String watchName, String playerName);
 	public UnhandeledReport startOnlineGame(int clientPort, String opponentHost,int opponentPort, 
				int opponentTransmitWaiterPort,
				boolean startsGame, TheClient theClient);
	//public void startGame();
 	public void addPlayer(String player2);
 	public Player getPlayer(Player.Color pColor);
 	public String getId();
 	public ArrayList<String> getGameHistory();
 	public void resetConnection();
	public void opponentSurrender();
}
