package ConnectFourServer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

import gameManager.Board;
import gameManager.Game;
import gameManager.Player;
import ConnectFourServer.OnlineClients.Client;;
/**
 * Datastructure of the current online games.
 * @author Boris
 *
 */
public class OnlineGames {
	private MainServer server;
	//The current online games (game ID to game hash)
	private HashMap<String,Game> playingGames;
	
	public OnlineGames(MainServer server)
	{
		this.server = server;
		playingGames = new HashMap<String,Game>();		
	}		
	
	public synchronized void addGame(Game game){
		playingGames.put(game.getId(), game);
		server.printLog("Game has been added : " + game.getId() + "\n");
		for(String gameId : playingGames.keySet()){
			server.printLog("ONline game: " +gameId + "\n");
		}
	}
	
	public synchronized ArrayList<Game> getOnlineGames(){
		ArrayList<Game> onlineGames = new ArrayList<Game>();
		
		for(String gameId : playingGames.keySet()){
			onlineGames.add(playingGames.get(gameId));
		}
		
		return onlineGames;
	}
	
	public synchronized ArrayList<String> getDownGames(){
		ArrayList<String> downGamesIds = new ArrayList<String>();
		
		for(String gameId : playingGames.keySet()){
			Game currGame = playingGames.get(gameId); 
			String player1 = currGame.getPlayer(Player.Color.RED).getName();
			String player2 = currGame.getPlayer(Player.Color.BLUE).getName();
			
			if(server.clients.getClient(player1) == null && server.clients.getClient(player1) == null)
				downGamesIds.add(currGame.getId());
		}
		
		return downGamesIds;
	}
	
	public synchronized Game getGame(String gameId){
		server.printLog("Looking for: " + gameId + "\n");
		if(playingGames.containsKey(gameId)){
			return playingGames.get(gameId);
		}
		for(String id : playingGames.keySet()){
			server.printLog("GAME: " + id);
		}
		return null;
	}

	public synchronized void removeGame(String gameId){
		if(playingGames.containsKey(gameId)){
			playingGames.remove(gameId);
		}
	}
}
