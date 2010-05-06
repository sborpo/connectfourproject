package ConnectFourClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
		Socket connection =null;
		 try {
		 connection = new Socket(address, serverPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter out =null;
		BufferedReader stdin= null;
		try{
		 out = new PrintWriter(connection.getOutputStream());
	     stdin = new BufferedReader(new InputStreamReader(System.in));
	    String inputLine;
		StringBuilder sb = new StringBuilder();
		System.out.println("Please Enter Your Message (End the message by leaving new empty line):");
		//read the command from the user
		  while ((inputLine = stdin.readLine()) != null) 
		  {
			  sb.append(inputLine+"\n");
			  if (inputLine.equals(""))
			  {
				  break;
			  }
		  } 
		
		System.out.println("\nSending your message to the server...");  
		//send the command to the server
		out.print(sb.toString());
		out.flush();
		//get server's response
		stdin= new BufferedReader(new InputStreamReader(connection.getInputStream()));
		sb =new StringBuilder();
		while ((inputLine = stdin.readLine()) != null) 
		  {
			  sb.append(inputLine);
		  } 
		System.out.println("\n\n Server Response is:"+sb.toString());
		
		stdin.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}


	public static void main(String[] args)
	{
		TheClient client = new TheClient(args);
		client.start();
	}
}
