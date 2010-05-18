package ConnectFourServer;

import gameManager.Game;
import gameManager.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

/**
 * Handles the TCP requests
 *
 */
public class RequestHandler implements Runnable {

	// The client's socket from where we should read the
	// request and act according to it.
	private Socket clientSock;

	// the server which the Request was sent to
	private MainServer server;

	public RequestHandler(Socket clientSocket, MainServer server) {
		clientSock = clientSocket;
		this.server = server;
	}

	@Override
	public void run() {
		PrintWriter out = null;
		BufferedReader in = null;
		String clientHost = clientSock.getInetAddress().getHostName();
		String clientIP = clientSock.getInetAddress().getHostAddress();
		server.printLog("Starting new socket...\n");
		try {
			out = new PrintWriter(clientSock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
			
			String inputLine;
			String response = ClientServerProtocol.WHAT;
			// reads the input line by line and appends in to the string builder
			while ((inputLine = in.readLine()) != null) {
				if(inputLine.equals("")){
					break;
				}
				String logMessage = "From Client with name :" + clientHost + " IP: "
				+ clientIP + " Recieved This Message: \n -------------\n"
				+ inputLine + "\n-------------\n\n\n";
				server.printLog(logMessage);
				
				//get the response
				response = respondToMessage(inputLine);
				System.out.println("Send to client: " + response);
				out.println(response);
				out.println();
			}
			System.out.println("------------------FINISHED-----------------");
			if (out != null) {
				out.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					server.printLog("Problem closing socket from Client: "
							+ clientHost + " with IP: " + clientIP);
					e.printStackTrace();
				}
			}
			if(clientSock != null){
				try {
					clientSock.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException ex) {
			server.printLog("Problem reading from Client: " + clientHost
					+ " with IP: " + clientIP + " " + ex.getMessage());
		} 
	}
	
	private String respondToMessage(String message){
		ClientServerProtocol parser = new ClientServerProtocol(msgType.SERVER);
		String[] params = parser.parseCommand(message);
		String respondMsg = null;
		if(params == null){
			respondMsg =  ClientServerProtocol.WHAT;
		}
		else{
			String command = params[0];
	
			if(command.equals(ClientServerProtocol.MEETME)){
				respondMsg = meetMeTreat(Integer.parseInt(params[1]),params[2]); 
			}
			else if(command.equals(ClientServerProtocol.NEWGAME)){
				respondMsg = newGameTreat(Integer.parseInt(params[1]),params[2]);
			}
			else if(command.equals(ClientServerProtocol.PLAY)){
				respondMsg = playTreat(Integer.parseInt(params[1]),params[2],params[3]);
			}
			else if(command.equals(ClientServerProtocol.WATCH)){
				respondMsg = watchTreat(Integer.parseInt(params[1]),params[2]);
			}
			else if(command.equals(ClientServerProtocol.OK)){
				respondMsg = okOnWatchTreat(Integer.parseInt(params[1]),params[2]);
			}
			else{
				respondMsg = ClientServerProtocol.WHAT;
			}
		}
		return respondMsg;
	}
	
	private String okOnWatchTreat(int parseInt, String string) {
		// TODO Auto-generated method stub
		return null;
	}
	private String watchTreat(int parseInt, String string) {
		// TODO Auto-generated method stub
		return null;
	}

	private String playTreat(int gamePort, String gameId,String clientName) {
		String response = ClientServerProtocol.KNOWYA;
		//check if the client is in the list and not in a game
		OnlineClients.Client theClient = server.clients.getClient(clientName);
		if(theClient != null){
			if(theClient.getGame() == null){
				//check if the game exists 
				Game theGame = server.games.getGame(gameId);
				if(theGame != null){
					//and has no second player
					Player thePlayer = theGame.getPlayer(Player.Color.BLUE);
					if(thePlayer == null){
						theGame.addPlayer(clientName);
						OnlineClients.Client enemy = server.clients.getClient(theGame.getPlayer(Player.Color.RED).getName());
						if(enemy != null){
							response = ClientServerProtocol.GOGOGO + " " 
										+ enemy.getTCPPort() + " " 
										+ enemy.getAddress().getHostAddress() + " " 
										+ enemy.getName();
							server.printLog("Player has been added to the game: " + gameId + "\n");
							theClient.setGameForClient(gameId);
						}
						else{
							server.printLog("WE HAVE PROBLEM IN SERVER MAN\n");
							response = ClientServerProtocol.SERVPROB;
						}
					}
					else{
						server.printLog("Other man playing..." + thePlayer.getName() +"\n");
						response = ClientServerProtocol.DENIED;
					}
				}
				else{
					server.printLog("WE HAVE PROBLEM IN SERVER MAN\n");
					response = ClientServerProtocol.SERVPROB;
				}
			}
			else{
				response = ClientServerProtocol.DENIED;
			}
		}
		return response;
	}

	private String newGameTreat(int gamePort,String playerName) {
		String response = ClientServerProtocol.KNOWYA;
		//generate game id
		String gameId = playerName + Long.toString(System.currentTimeMillis());
		//check if the client is in the list 
		OnlineClients.Client theClient = server.clients.getClient(playerName);
		if(theClient != null){
			//and not in a game
			if(theClient.getGame() == null){
				//create new game
				theClient.setTCPPort(gamePort);
				Game newGame = null;
				newGame = new Game(playerName, null, gameId);
				server.games.addGame(newGame);
				theClient.setGameForClient(gameId);
				response = ClientServerProtocol.GAME + " " + gameId;
				server.printLog("The game has been created: " + theClient.getGame() + "\n");
			}
			else{
				response = ClientServerProtocol.DENIED;
			}
		}
		return response;
	}

	private String meetMeTreat(int clientUDPPort,String clientName){
		server.clients.addClientToUdpList(new OnlineClients.Client(clientSock.getInetAddress(), clientUDPPort,clientName,-1));
		String response = ClientServerProtocol.NICETM + " " + Integer.toString(server.getServerUDPPort()); 
		return response; 
	}

}
