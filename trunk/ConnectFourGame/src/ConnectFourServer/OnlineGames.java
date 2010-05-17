package ConnectFourServer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Datastructure of the current online games.
 * @author Boris
 *
 */
public class OnlineGames {
	
	//The current online client (their udp addresses)
	private HashMap<String,Client> udpClients;
	
	//This way we can know if the client was alive
	private HashMap<String, Boolean> isAlive;
	
	public static class Client
	{
		private InetAddress address;
		private String name;
		private int UDPport;
		public Client(InetAddress host, int UDPport,String name)
		{
			address=host;
			this.UDPport=UDPport;
			this.name = name;
		}
		public int getUDPPort()
		{
			return UDPport;
		}
		public InetAddress getAddress()
		{
			return address;
		}
		public String getName(){
			return name;
		}
	}
	
	public OnlineGames()
	{
		udpClients= new HashMap<String,Client>();
		isAlive= new HashMap<String, Boolean>();
		
	}
	
	public synchronized void resetIsAliveMap(){
		for(String key : isAlive.keySet()){
			isAlive.put(key, false);
		}
	}
	
	 public synchronized void addClientToUdpList(Client client)
	 {
		 String clientName = client.getName();
		 if(!udpClients.containsKey(clientName)){
			 udpClients.put(clientName,client);	
			 isAlive.put(client.getName(),new Boolean( true));
		 }
	 }
	 
	 public synchronized ArrayList<Client> getUdpList()
	 {
		 ArrayList<Client> clients = new ArrayList<Client>();
		 for(String key : udpClients.keySet()){
			 clients.add(udpClients.get(key));
			}
		 return clients;
	 }
	 
	 public synchronized boolean setAliveIfExists(String clientName){
		 if(isAlive.containsKey(clientName)){
			 isAlive.put(clientName,true);
			 return true;
		 }
		 return false;
	 }
	
	public synchronized void removeIfNotAlive(){
		for(String key : isAlive.keySet()){
			boolean alive = isAlive.get(key);
			if(!alive){
				System.out.println("Removing: "+ key +"...");
				udpClients.remove(key);
			}
		}
	}
	

}
