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
	private ArrayList<Client> udpClients;
	
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
		udpClients= new ArrayList<Client>();
		isAlive= new HashMap<String, Boolean>();
		
	}
	
	 public synchronized void addClientToUdpList(Client client)
	 {
		 if(!udpClients.contains(client)){
			 udpClients.add(client);	
			 isAlive.put(client.getName(),new Boolean( true));
		 }
	 }
	 
	 public synchronized ArrayList<Client> getUdpList()
	 {
		 return (ArrayList<Client>)udpClients.clone();
	 }
	 
	
	
	
	

}
