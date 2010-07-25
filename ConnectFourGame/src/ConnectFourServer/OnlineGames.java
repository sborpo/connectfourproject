package ConnectFourServer;

import java.util.ArrayList;
import java.util.HashMap;

import gameManager.Game;
import gameManager.Player.Color;
/**
 * Datastructure of the current online games.
 *
 */
public class OnlineGames {
	private MainServer server;
	//The current online games (game ID to game hash)
	private HashMap<String,Game> playingGames;
	
	
	/**
	 * Initialize the online games of the given MainServer
	 * @param server
	 */
	public OnlineGames(MainServer server)
	{
		this.server = server;
		playingGames = new HashMap<String,Game>();		
	}		
	
	/**
	 * Adds the given game into the online games
	 * @param game
	 */
	public synchronized void addGame(Game game){
		playingGames.put(game.getId(), game);
		server.printer.print_info("Game has been added : " + game.getId());
	}
	
	/**
	 * Returns an array list of GameForClient object which represent online games
	 * that should be sent to the client.
	 * @return
	 */
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
	
	/**
	 * Returns the current online games
	 * @return
	 */
	public synchronized ArrayList<Game> getOnlineGames(){
		ArrayList<Game> onlineGames = new ArrayList<Game>();
		
		for(String gameId : playingGames.keySet()){
			onlineGames.add(playingGames.get(gameId));
		}
		
		return onlineGames;
	}
	
	
	/**
	 * Returns a Game object of the given gameId , if the gameId wasn't found in the 
	 * online games , then it reutns null
	 * @param gameId
	 * @return
	 */
	public synchronized Game getGame(String gameId){
		if(playingGames.containsKey(gameId)){
			return playingGames.get(gameId);
		}
		for(String id : playingGames.keySet()){
			server.printer.print_info("GAME: " + id);
		}
		return null;
	}

	/**
	 * Removes the given gameId from the online games , if the
	 * game doesn't exists, does nothing
	 * @param gameId
	 */
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
