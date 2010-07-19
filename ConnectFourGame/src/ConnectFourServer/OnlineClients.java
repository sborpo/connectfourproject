package ConnectFourServer;

import gameManager.Game;
import gameManager.Player;
import common.OnlineClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class manages the online clients which are now
 * connected to the server.
 *
 */
public class OnlineClients {
	private MainServer server;
	//The current online client (their udp addresses)
	private HashMap<String,OnlineClient> udpClients;
	
	//This way we can know if the client was alive
	private HashMap<String, Boolean> isAlive;
	
	public OnlineClients(MainServer server)
	{
		this.server = server;
		udpClients= new HashMap<String,OnlineClient>();
		isAlive= new HashMap<String, Boolean>();
		
	}
	
	/**
	 * 
	 */
	public synchronized void resetIsAliveMap(){
		for(String key : isAlive.keySet()){
			resetAlive(key);
		}
	}
	
	 public synchronized void addClientToUdpList(OnlineClient client)
	 {
		 String clientName = client.getName();
		 if(!udpClients.containsKey(clientName)){
			 udpClients.put(clientName,client);	
			 isAlive.put(clientName,true);
		 }
	 }
	 
	 public synchronized OnlineClient getClient(String clientName){
		 if(udpClients.containsKey(clientName)){
			 return udpClients.get(clientName);
		 }
		 for(String name : udpClients.keySet()){
			 System.out.println("Client: '" + name + "'");
		 }
		 return null;
	 }
	 
	 public synchronized ArrayList<String> getClients()
	 {
		 ArrayList<String> clients = new ArrayList<String>();
		 for(String key : udpClients.keySet()){
			 clients.add(key);
			}
		 return clients;
	 }
	 
	 public synchronized boolean setAlive(String clientName){
		 if(isAlive.containsKey(clientName)){
			 isAlive.put(clientName,true);
			 return true;
		 }
		 return false;
	 }
	
	 
	 public synchronized void resetAlive(String clientName){
			if(udpClients.containsKey(clientName)){
				isAlive.put(clientName, false);
			}
		}
	 
	 
	public synchronized void removeClient(String clientName)
	{
		server.printer.print_info("Removing Client: "+ clientName +"...");
		OnlineClient theClient = server.clients.getClient(clientName);
		if(theClient == null){
			server.printer.print_info("SOME PROBLEM WHILE REMOVING: " + clientName);
			return;
		}
		Game game = server.games.getGame(theClient.getGame());
		if(game != null){
			server.printer.print_info("Removing game : "+ game.getId() +"...");
			server.games.removeGame(game.getId());
		}
		udpClients.remove(clientName);
		isAlive.remove(clientName);
	}
	
	public synchronized void removeIfNotAlive(){
		ArrayList<String> toRemove = new ArrayList<String>();
		for(String key : isAlive.keySet()){
			boolean alive = isAlive.get(key);
			if(!alive){
				server.printer.print_info("Removing: "+ key +"...");
				OnlineClient theClient = server.clients.getClient(key);
				if(theClient == null){
					server.printer.print_info("Problem getting the client for removal: " + key);
					return;
				}
				Game game = server.games.getGame(theClient.getGame());
				if(game != null){
					server.printer.print_info("Removing game : "+ game.getId() +"...");
					server.games.removeGame(game.getId());
				}
				udpClients.remove(key);
				toRemove.add(key);
			}
			else{
				server.printer.print_info(key + " is alive");
			}
		}

		for(String key : toRemove){
			isAlive.remove(key);
		}
	}
		
		

	


}
