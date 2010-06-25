package ConnectFourServer;

import gameManager.Game;
import gameManager.Player;
import gameManager.Player.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import common.OnlineClient;

import ConnectFourClient.TheClient;
import ConnectFourServer.DataBaseManager.GameIdAlreadyExists;
import ConnectFourServer.DataBaseManager.UserAlreadyExists;

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
		ObjectOutputStream out = null;
		BufferedReader in = null;
		String clientHost = clientSock.getInetAddress().getHostName();
		String clientIP = clientSock.getInetAddress().getHostAddress();
		server.printer.print_info("Starting new socket...\n");
		try {
			out = new ObjectOutputStream(clientSock.getOutputStream());
			//out = new PrintWriter(clientSock.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
			
			String inputLine = null;
			//VALERIY
			Object response = ClientServerProtocol.WHAT;
			//String response = ClientServerProtocol.WHAT;
			// reads the input line by line and appends in to the string builder
			while ((inputLine = in.readLine()) != null) {
				if(inputLine.equals("")){
					break;
				}
				
				String logMessage = "From Client with host :" + clientHost + " IP: "
				+ clientIP + " Recieved This Message: \n -------------\n"
				+ inputLine + "\n-------------\n\n\n";
				server.printer.print_info(logMessage);
				//get the response
				//VALERIY
				response = respondToMessage(inputLine);
				if(response == null){
					server.printer.print_error("Can't respond the message");
					continue;
				}
				
				server.printer.print_info("Send to client: " + response);
				//VALERIY
				//out.writeObject(response);
				out.writeObject(response);
			}
			
			if (out != null) {
				out.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					server.printer.print_error("Problem closing socket from Client: "
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
			server.printer.print_error("Problem reading from Client: " + clientHost
					+ " with IP: " + clientIP + " " + ex.getMessage());
		} 
	}
	
	private Object respondToMessage(String message){
		ClientServerProtocol parser = new ClientServerProtocol(msgType.SERVER);
		String[] params = parser.parseCommand(message);
		Object respondMsg = null;
		if(params == null){
			respondMsg =  ClientServerProtocol.WHAT;
		}
		else{
			String command = params[0];
	
			if(command.equals(ClientServerProtocol.MEETME)){
				respondMsg = meetMeTreat(Integer.parseInt(params[1]),params[2],params[3]); 
			}
			else if(command.equals(ClientServerProtocol.NEWGAME)){
				respondMsg = newGameTreat(Integer.parseInt(params[1]),Integer.parseInt(params[2]),params[3]);
			}
			else if(command.equals(ClientServerProtocol.BATCHGAMESREPORT)){
				respondMsg = batchGamesReportTreat(params);
			}
			else if(command.equals(ClientServerProtocol.GAMEREPORT)){
				respondMsg = gamesReportTreat(params[1],params[2],Boolean.parseBoolean(params[3]),params[4]);
			}
			else if(command.equals(ClientServerProtocol.SIGNUP)){
				respondMsg = signupTreat(params[1],params[2]);
			}
			else if(command.equals(ClientServerProtocol.PLAY)){
				respondMsg = playTreat(Integer.parseInt(params[1]),Integer.parseInt(params[2]),params[3],params[4]);
			}
			else if(command.equals(ClientServerProtocol.GAMELIST)){
				respondMsg = getOnlineGamesTreat();
			}
			else if(command.equals(ClientServerProtocol.WATCH)){
				respondMsg = watchTreat(Integer.parseInt(params[1]),params[2],params[3]);
			}
			else{
				respondMsg = ClientServerProtocol.WHAT;
			}
		}
		return respondMsg;
	}
	
	
	private boolean isClientOnline(String clientName){
		boolean res = false;
		OnlineClient theClient = server.clients.getClient(clientName);
		if(theClient != null){
			res = true;
		}
		return res;
	}
	
	private boolean isGameOnline(String gameId){
		boolean res = false;
		Game theGame = server.games.getGame(gameId);
		if(theGame != null){
			res= true;
		}
		return res;
	}
	
	private boolean isClientInTheGame(String clientName,String gameId){
		boolean res= false;
		OnlineClient theClient = server.clients.getClient(clientName);
		Game theGame = server.games.getGame(gameId);
		
		if(theClient != null && theGame != null){
			if(theClient.getGame().equals(theGame.getId()) && theGame.isPlayer(clientName) != null){
				res= true;
			}
		}
		
		return res;
	}
	
	private Object batchGamesReportTreat(String[] params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String gamesReportTreat(String gameId, String clientName, boolean gameRes, String winner){
		String response = ClientServerProtocol.KNOWYA;
		//check if the client is online
		if(isClientOnline(clientName)){

			//check if the game exists
			if(isGameOnline(gameId)){
				//check if the client is in this game
				if(isClientInTheGame(clientName,gameId)){
					//add the report
					try {
						server.printer.print_info("Adding the report to the database.");
						DataBaseManager.makeReport(gameId, clientName, winner);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						//THINK GOOD WHAT TO DO
						e.printStackTrace();
						response = ClientServerProtocol.SERVPROB;
					}
					//return ok message
					response = ClientServerProtocol.OK;
				}
				else{
					server.printer.print_error("The client: " + clientName + " is not in the game: "+gameId);
					response = ClientServerProtocol.DENIED;
				}
			}
			else{
				server.printer.print_error("The game is not online: "+gameId);
				response = ClientServerProtocol.DENIED;
			}
		}
		else{
			server.printer.print_error("The client is not online: "+clientName);
			response = ClientServerProtocol.DENIED;
		}
		return response;
	}

	private String watchTreat(int watcherPort, String gameId,String watcherName) {
		String response = ClientServerProtocol.KNOWYA;
		OnlineClient viewer=server.clients.getClient(watcherName);
		if(viewer != null)
		{
			Random random= new Random();
			int num=random.nextInt(2);
			Color r = (num==0) ? Color.RED : Color.BLUE;
			Game theGame = server.games.getGame(gameId);
			server.printer.print_info("Looking for game: " + gameId);
			if(theGame != null)
			{				
				server.printer.print_info("Game is found!");
				String playerName = theGame.getPlayer(r).getName();
				Player thePlayer = theGame.addWatcher(watcherName, playerName);
				//if the watcher is already watching the game from another player
				if(thePlayer.getName().equals(playerName)){
					playerName = thePlayer.getName();
				}
				
				OnlineClient client=server.clients.getClient(playerName);
				InetAddress clientAddr=client.getAddress();
				int clientPort= client.getTransmitPort();
				
				InetAddress viewerAddr= viewer.getAddress();
				server.printer.print_info("Sending transmit command to: " + client.getName() + "\n");
				SendToClient(clientAddr,clientPort,viewerAddr,watcherPort,watcherName);
				response = ClientServerProtocol.ENJOYWATCH;
			}
			else{
				server.printer.print_error("No game found with id: " + gameId);
				response = ClientServerProtocol.DENIED;
			}
		}
		return response;
	}

	private void SendToClient(InetAddress clientAddr, int clientPort,
			InetAddress viewerAddr, int watcherPort, String watcherName ) {
		try {
			Socket clientTsmtSocket = new Socket(clientAddr,clientPort);
			PrintWriter out = new PrintWriter(clientTsmtSocket.getOutputStream(),true);
			String message = ClientServerProtocol.VIEWERTRANSMIT+" "+ watcherPort +
							" "+viewerAddr.getHostAddress()+" "+watcherName +
							" "+clientSock.getInetAddress().getHostAddress();
			server.printer.print_info("Transmit message: " + message +"\n");
			out.println(message);		
		} catch (IOException e) {
			server.printer.print_error("While sending transmit command: " + e.getMessage());
			e.printStackTrace();
		}
		
		
		
	}

	private String playTreat(int gamePort, int transmitionPort, String gameId,String clientName) {
		String response = ClientServerProtocol.KNOWYA;
		//check if the client is in the list and not in a game
		OnlineClient theClient = server.clients.getClient(clientName);
		if(theClient != null){
			if(theClient.getGame() != null){			
				server.printer.print_error("User is already in game, delete old game...");
				server.games.removeGame(theClient.getGame());
				theClient.resetGame();
			}
			theClient.setTransmitPort(transmitionPort);
			//check if the game exists 
			Game theGame = server.games.getGame(gameId);
			if(theGame != null){
				//and has no second player
				Player thePlayer = theGame.getPlayer(Player.Color.BLUE);
				if(thePlayer == null){
					theGame.addPlayer(clientName);
					OnlineClient enemy = server.clients.getClient(theGame.getPlayer(Player.Color.RED).getName());
					if(enemy != null){
						response = ClientServerProtocol.GOGOGO + " " 
									+ enemy.getTCPPort() + " " 
									+ enemy.getAddress().getHostAddress() + " " 
									+ enemy.getName();
						server.printer.print_info("Player has been added to the game: " + gameId + "\n");
						theClient.setGameForClient(gameId);
						try {
							server.printer.print_info("Adding the game to database: " + gameId + "\n");
							DataBaseManager.createGame(enemy.getName(), clientName, gameId);
						} catch (SQLException e) {
							server.printer.print_error("Cannot insert the game to the database: " + e.getMessage());
							e.printStackTrace();
							response = ClientServerProtocol.SERVPROB;
							//TODO to do something
						} catch (GameIdAlreadyExists e) {
							server.printer.print_error("The game is already in the database: " + e.getMessage());
							e.printStackTrace();
							response = ClientServerProtocol.SERVPROB;
							//TODO something
						}
					}
					else{
						server.printer.print_error("WE HAVE PROBLEM IN SERVER MAN\n");
						response = ClientServerProtocol.SERVPROB;
					}
				}
				else{
					server.printer.print_error("Other man playing..." + thePlayer.getName() +"\n");
					response = ClientServerProtocol.DENIED;
				}
			}
			else{
				server.printer.print_error("No such gameId:" + gameId);
				response = ClientServerProtocol.DENIED;
			}	
		}
		return response;
	}

	private String newGameTreat(int gamePort,int transmitionPort, String playerName) {
		String response = ClientServerProtocol.KNOWYA;
		//generate game id
		String gameId = playerName + Long.toString(System.currentTimeMillis());
		//check if the client is in the list 
		OnlineClient theClient = server.clients.getClient(playerName);
		if(theClient != null){
			//remove old online game of the client
			if(theClient.getGame() != null){
				server.printer.print_info("Removing old online game of the client");
				server.games.removeGame(theClient.getGame());
				theClient.resetGame();
			}
			//create new game
			theClient.setTCPPort(gamePort);
			theClient.setTransmitPort(transmitionPort);
			Game newGame = new Game(playerName, null, gameId);
			server.games.addGame(newGame);
			theClient.setGameForClient(gameId);
			response = ClientServerProtocol.GAME + " " + gameId;
			server.printer.print_info("The game has been created: " + theClient.getGame() + "\n");

		}
		return response;
	}

	
	private String signupTreat(String username , String password)
	{
		String response = ClientServerProtocol.SERVPROB;
		try {
				try {
					DataBaseManager.insertUser(username, password);
				} catch (UserAlreadyExists e) {
					response=ClientServerProtocol.USERALREADYEXISTS;
					return response;
				}
				response= ClientServerProtocol.OK;
				return response;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			server.printer.print_error(e.getMessage());
			return response;
		}
		
	}
	
	private ArrayList<GameForClient> getOnlineGamesTreat()
	{
		return server.games.getOnlineGamesForClient();
	}
	
	private String meetMeTreat(int clientUDPPort,String clientName,String password){
		boolean errFlag = false;
		String response = ClientServerProtocol.SERVPROB;
		try {
			if(!DataBaseManager.authenticateUser(clientName, password)){
				response = ClientServerProtocol.USERNOTEXISTS;
				return response;
			}
			else{
				server.printer.print_info("User entered: " + clientName);	
			}
		} catch (SQLException e) {
			server.printer.print_error(e.getMessage());
			errFlag = true;
		} 
		if(!errFlag){
			server.clients.addClientToUdpList(new OnlineClient(clientSock.getInetAddress(), clientUDPPort,clientName,TheClient.unDEFport,TheClient.unDEFport));
			response = ClientServerProtocol.NICETM + " " + Integer.toString(server.getServerUDPPort());
			server.udpListener.openTimerFor(clientName);
		}
		return response; 
	}

}
