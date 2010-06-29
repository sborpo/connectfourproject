package ConnectFourServer;

import java.util.ArrayList;
import java.util.HashMap;

import gameManager.Game;
import gameManager.Player;
import gameManager.Player.Color;
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
		server.printer.print_info("Game has been added : " + game.getId() + "\n");
	}
	
	public synchronized ArrayList<GameForClient> getOnlineGamesForClient()
	{
		ArrayList<GameForClient> clientGames= new ArrayList<GameForClient>();
		for(String gameId : playingGames.keySet()){
			Game game=playingGames.get(gameId);
			String id = game.getId();
			String player1= (game.getPlayer(Color.RED)!=null)? game.getPlayer(Color.RED).getName() : null;
			String player2= (game.getPlayer(Color.BLUE)!=null) ? game.getPlayer(Color.BLUE).getName() : null;
			clientGames.add(new GameForClient(id, player1, player2));
		}
		return clientGames;
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
			
			if(server.clients.getClient(player1) == null && server.clients.getClient(player2) == null)
				downGamesIds.add(currGame.getId());
		}
		
		return downGamesIds;
	}
	
	public synchronized Game getGame(String gameId){
		if(playingGames.containsKey(gameId)){
			return playingGames.get(gameId);
		}
		for(String id : playingGames.keySet()){
			server.printer.print_info("GAME: " + id);
		}
		return null;
	}

	public synchronized void removeGame(String gameId){
		if(gameId != null){
			if(playingGames.containsKey(gameId)){
				playingGames.remove(gameId);
				server.printer.print_info("Game removed: "+ gameId);
			}
			else{
				server.printer.print_error("No such online game: " + gameId);
			}
		}
	}
}
