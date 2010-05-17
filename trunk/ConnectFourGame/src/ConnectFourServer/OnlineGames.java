package ConnectFourServer;

import java.util.ArrayList;
import java.util.HashMap;
import gameManager.Game;
import gameManager.Player;
import ConnectFourServer.OnlineClients.Client;;
/**
 * Datastructure of the current online games.
 * @author Boris
 *
 */
public class OnlineGames {
	
	//The current online games (game ID to game hash)
	private HashMap<String,Game> playingGames;
	
	public OnlineGames()
	{
		playingGames = new HashMap<String,Game>();		
	}	
	
	public synchronized ArrayList<Game> getOnlineGames(){
		ArrayList<Game> onlineGames = new ArrayList<Game>();
		
		for(String gameId : playingGames.keySet()){
			onlineGames.add(playingGames.get(gameId));
		}
		
		return onlineGames;
	}
	
	public synchronized ArrayList<String> getDownGames(ArrayList<Client> onlineClients){
		ArrayList<String> downGamesIds = new ArrayList<String>();
		
		for(String gameId : playingGames.keySet()){
			Game currGame = playingGames.get(gameId); 
			Client player1 = currGame.getPlayer(Player.Color.RED).getClient();
			Client player2 = currGame.getPlayer(Player.Color.BLUE).getClient();
			
			if(!onlineClients.contains(player1) && !onlineClients.contains(player2))
				downGamesIds.add(currGame.getId());
		}
		
		return downGamesIds;
	}
	
	public synchronized Game getGame(String gameId){
		return playingGames.get(gameId);
	}

}
