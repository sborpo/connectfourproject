package ConnectFourClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author Valeriy Leykin
 * The client class, represents a player which can play the game
 * with another player P2P
 */
public class TheClient {

	private int serverPort;
	private String serverHost;
	private String clientName;
	
	
	public TheClient(String [] args)
	{
		parseArguments(args);
	}
	
	
	private void parseArguments(String[] args) {
		serverHost= args[0];
		serverPort = Integer.parseInt(args[1]);
		clientName = args[2];
		
		
	}
	
	public void start()
	{
		 InetAddress address=null;
		try {
			address = InetAddress.getByName(serverHost);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
			Socket connection = new Socket(address, serverPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 
	}


	public static void main(String[] args)
	{
		TheClient client = new TheClient(args);
		client.start();
	}
}
