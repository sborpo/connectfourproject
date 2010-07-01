package gameManager;

import gameManager.GameImp.TimeEnded;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import ConnectFourClient.TheClient;

import common.UnhandeledReport;

public interface Game {
	public static final String Surrended="SURRENDED";
	public boolean isGameFull();
	public Player isPlayer(String playerName);
	 public Player addWatcher(String watchName, String playerName);
	 public UnhandeledReport startOnlineGame(int clientPort, String opponentHost,int opponentPort, boolean startsGame, TheClient theClient) ;
	 public void startGame();
	 public void addPlayer(String player2);
	 public Player getPlayer(Player.Color pColor);
	 public String getId();
	 public ArrayList<String> getGameHistory();
}
