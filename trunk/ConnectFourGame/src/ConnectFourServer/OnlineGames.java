package ConnectFourServer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class OnlineGames {
	
	ArrayList<Client> udpClients;
	HashMap<String, Boolean> isAlive;
	
	public static class Client
	{
		private InetAddress address;
		private int port;
		public Client(InetAddress host, int port)
		{
			address=host;
			this.port=port;
		}
		public int getPort()
		{
			return port;
		}
		public InetAddress getAddress()
		{
			return address;
		}
		
	}
	
	public OnlineGames()
	{
		udpClients= new ArrayList<Client>();
		isAlive= new HashMap<String, Boolean>();
		
	}
	
	 public synchronized void addClientToUdpList(Client client)
	 {
		 udpClients.add(client);
		 isAlive.put(client.getAddress().toString(),new Boolean( true));
	 }
	 
	 public synchronized ArrayList<Client> getUdpList()
	 {
		 return (ArrayList<Client>)udpClients.clone();
	 }
	 
	
	
	
	

}
