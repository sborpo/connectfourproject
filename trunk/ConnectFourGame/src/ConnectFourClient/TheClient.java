package ConnectFourClient;

import gameManager.Game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

/**
 * 
 * @author Valeriy Leykin The client class, represents a player which can play
 *         the game with another player P2P
 */
public class TheClient {

	public int unDEFport = -1; 
	private int serverUdpPort = unDEFport;
	private int serverPort;
	private String serverHost;
	private String clientName;
	private int clientUdp = unDEFport;

	private int clientGamePort = unDEFport;
	private String opponentGameHost = "";
	private int opponentGamePort = unDEFport;
	private String opponentName = "";
	private int clientWatchPort = unDEFport;	
	private String gameId = "";
	private Game game;

	private InetAddress serverAddress;

	public InetAddress getServerAddress() {
		return serverAddress;
	}
	
	public String getClientName()
	{
		return clientName;
	}

	/**
	 * 
	 * @return The UDP port that the client listens on to the server
	 */
	public int listenToServerPort() {
		return clientUdp;
	}

	public int serverUDPPort() {
		return serverUdpPort;
	}

	public TheClient(String[] args) {
		parseArguments(args);

		try {
			serverAddress = InetAddress.getByName(serverHost);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void parseArguments(String[] args) {
		serverHost = args[0];
		System.out.println("Server: " + serverHost);
		serverPort = Integer.parseInt(args[1]);
		System.out.println("Server TCP port: "+serverPort);
		clientName = args[2];
		System.out.println("Clent name: "+clientName);
		//clientUdp = Integer.parseInt(args[3]);
		//System.out.println("Clent UDP: " +clientUdp);
		//serverUdpPort = Integer.parseInt(args[4]);
		//System.out.println(serverUdpPort);
		//clientGamePort = Integer.parseInt(args[3]);
		//System.out.println("Game port: " +clientGamePort);
		//VALERIY: That are optional parameters, they will come only after game is created, not here
		//opponentGameHost = args[6];
		//System.out.println(opponentGameHost);
		//opponentGamePort = Integer.parseInt(args[7]);
		//System.out.println(opponentGamePort);
		//clientStartsGame = args[8].equals("TRUE") ? true : false;
		//System.out.println(clientStartsGame);

	}

	public void start() {
		ServerListener echoServerListener = new ServerListener(this);
		echoServerListener.start();
		Socket serverConnection = null;
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		try {
			ClientServerProtocol parser = new ClientServerProtocol(msgType.SERVER);
			while(true)
			{		
				String inputLine;			
				
				//now ask from the client a message, and send it to the server
				System.out.println("--------------------------------------------------------------------");
				System.out.println("Please Enter Your Message (End the message by leaving new empty line):");
				// read the command from the user
				while ((inputLine = stdin.readLine()) != null) {
					
					String[] commandPar = parseCommand(inputLine,parser);
					if(commandPar == null){
						System.out.println("Wrong command, please try again");
						break;
					}
					
					// send the command to the server
					try{
						serverConnection = new Socket(serverAddress, serverPort);
					}
					catch (IOException ex){
						System.out.println("Connection problems with server: "+ ex.getMessage());
						break;
					}
					System.out.println("Sending your message: "+ inputLine +" to the server...");
					PrintWriter out = new PrintWriter(serverConnection.getOutputStream(),true);
					BufferedReader response = new BufferedReader(new InputStreamReader(serverConnection.getInputStream()));
					out.println(inputLine);
					out.println();
					
					System.out.println("READING socket...");
					// get server's response
					while((inputLine = response.readLine()) != null) {
						if(inputLine.equals("")){
							break;
						}
						System.out.println("\n\nServer Response is:" + inputLine);
						parseResponse(inputLine);
					}
					
					if(out != null){
						out.close();
					}
					if(response!= null){
						response.close();
					}
					
					//now ask from the client a message, and send it to the server
					System.out.println("--------------------------------------------------------------------");
					System.out.println("Please Enter Your Message (End the message by leaving new empty line):");
				}
				
				if(serverConnection != null){
					serverConnection.close();
				}
			}
			
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally{
			if(stdin!= null){
				try {
					stdin.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private String[] parseCommand(String command,ClientServerProtocol parser){
		String[] params = parser.parseCommand(command);
		
		if(params == null){
			return null;
		}
		else if(params[0].equals(ClientServerProtocol.MEETME)){
			clientUdp = Integer.parseInt(params[1]);
		}
		else if(params[0].equals(ClientServerProtocol.NEWGAME)){
			clientGamePort = Integer.parseInt(params[1]);
		}
		else if(params[0].equals(ClientServerProtocol.PLAY)){
			clientGamePort = Integer.parseInt(params[1]);
			gameId = params[2];
		}
		else if(params[0].equals(ClientServerProtocol.WATCH)){
			clientWatchPort = Integer.parseInt(params[1]);
		}
		
		return params;
	}
	private boolean parseResponse(String message){
		ClientServerProtocol parser = new ClientServerProtocol(msgType.CLIENT);
		String[] params = parser.parseCommand(message);
		boolean responseRes = true;
		
		if(params == null){
			System.out.println("I don't understand what server say...");
			responseRes = false;
			return responseRes;
		}
		

		String command = params[0];

		if(command.equals(ClientServerProtocol.NICETM)){
			 serverUdpPort = Integer.parseInt(params[1]);
		}
		else if(command.equals(ClientServerProtocol.GAME)){
			gameId = params[1];
			System.out.println("Received game: " + gameId + ", starting waiting on game port...");
			game = new Game(clientName, null,gameId);
			game.startOnlineGame(clientGamePort, null,-1, true);
		}
		else if(command.equals(ClientServerProtocol.GOGOGO)){
			opponentGamePort = Integer.parseInt(params[1]);
			opponentGameHost = params[2];
			opponentName = params[3];
			System.out.println("Accepted game with: " + opponentName + 
								" host: " + opponentGameHost + 
								" port: " + opponentGamePort +
								" game: " + gameId);
			game = new Game(opponentName, clientName,gameId);
			game.startOnlineGame(clientGamePort, opponentGameHost,opponentGamePort, false);			
		}
		else if(command.equals(ClientServerProtocol.NOCONN)){
			responseRes = false;
		}
		else if(command.equals(ClientServerProtocol.DENIED)){
			responseRes = false;
		}
		else if(command.equals(ClientServerProtocol.KNOWYA)){
			responseRes = false;
		}
		else if(command.equals(ClientServerProtocol.TAKE)){
			
		}
		else if(command.equals(ClientServerProtocol.WHAT)){
			responseRes = false;
		}
		
		return responseRes;
	}
	
	public static void main(String[] args) {
		TheClient client = new TheClient(args);
		client.start();
	}
}
