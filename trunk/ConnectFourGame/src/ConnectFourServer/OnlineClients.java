package ConnectFourServer;

import gameManager.Game;
import gameManager.Player;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class OnlineClients {
	private MainServer server;
	//The current online client (their udp addresses)
	private HashMap<String,Client> udpClients;
	
	//This way we can know if the client was alive
	private HashMap<String, Boolean> isAlive;
	
	
	
	
	
	
	public static class Client
	{
		private InetAddress address;
		private String name;
		private int UDPport;
		private int TCPport;
		private int UDPtransmitPort;
		private String currentGame;
		
		public Client(InetAddress host, int UDPport,String name,int TCPPort,int UDPtransmitPort)
		{
			address=host;
			this.UDPport = UDPport;
			this.TCPport = TCPPort;
			this.UDPtransmitPort = UDPtransmitPort;
			this.name = name;
			currentGame = null;
		}
		
		public int getUDPPort()
		{
			return UDPport;
		}
		public int getUDPtransmitPort()
		{
			return UDPtransmitPort;
		}
		public int getTCPPort()
		{
			return TCPport;
		}
		public InetAddress getAddress()
		{
			return address;
		}
		public String getName(){
			return name;
		}
		
		public synchronized void setTCPPort(int port){
			TCPport = port;
		}
		
		public synchronized void resetGame(){
			currentGame = "";
		}
		
		public synchronized void setGameForClient(String gameId){
			currentGame = gameId;
		}
		
		public synchronized String getGame(){
			return currentGame;
		}
		 
	}
	
	public OnlineClients(MainServer server)
	{
		this.server = server;
		udpClients= new HashMap<String,Client>();
		isAlive= new HashMap<String, Boolean>();
		
	}
	
	public synchronized void resetIsAliveMap(){
		for(String key : isAlive.keySet()){
			resetAlive(key);
		}
	}
	
	 public synchronized void addClientToUdpList(Client client)
	 {
		 String clientName = client.getName();
		 if(!udpClients.containsKey(clientName)){
			 udpClients.put(clientName,client);	
			 isAlive.put(clientName,true);
		 }
	 }
	 
	 public synchronized Client getClient(String clientName){
		 if(udpClients.containsKey(clientName)){
			 return udpClients.get(clientName);
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
				System.out.println("unset for "+clientName);
				isAlive.put(clientName, false);
			}
		}
	 
	 
	public synchronized void removeIfNotAlive(){
		ArrayList<String> toRemove = new ArrayList<String>();
		for(String key : isAlive.keySet()){
			boolean alive = isAlive.get(key);
			if(!alive){
				server.printer.print_info("Removing: "+ key +"...");
				Client theClient = server.clients.getClient(key);
				if(theClient == null){
					server.printer.print_info("SOME PROBLEM WHILE REMOVING: " + key);
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
				System.out.println(key + "is alive");
			}
		}

		for(String key : toRemove){
			isAlive.remove(key);
		}
	}
		
		

	


}
