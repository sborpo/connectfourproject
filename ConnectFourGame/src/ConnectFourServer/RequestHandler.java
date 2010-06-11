package ConnectFourServer;

import gameManager.Game;
import gameManager.Player;
import gameManager.Player.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import ConnectFourServer.DataBaseManager.UserAlreadyExists;
import ConnectFourServer.OnlineClients.Client;

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
				respondMsg = meetMeTreat(Integer.parseInt(params[1]),params[2],params[3]); 
			}
			else if(command.equals(ClientServerProtocol.NEWGAME)){
				respondMsg = newGameTreat(Integer.parseInt(params[1]),params[2]);
			}
			else if(command.equals(ClientServerProtocol.PLAY)){
				respondMsg = playTreat(Integer.parseInt(params[1]),params[2],params[3]);
			}
			else if(command.equals(ClientServerProtocol.WATCH)){
				respondMsg = watchTreat(Integer.parseInt(params[1]),params[2],params[3]);
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
	private String watchTreat(int watcherPort, String gameId,String watcherName) {
		String response = ClientServerProtocol.KNOWYA;
		Client viewer=server.clients.getClient(watcherName);
		if(viewer != null)
		{
			Random random= new Random();
			int num=random.nextInt(2);
			Color r = (num==0) ? Color.RED : Color.BLUE;
			Game theGame = server.games.getGame(gameId);
			if(theGame != null)
			{
				String playerName=theGame.getPlayer(r).getName();
			
				Client client=server.clients.getClient(playerName);
				InetAddress clientAddr=client.getAddress();
				int clientPort= client.getUDPPort();
				
				InetAddress viewerAddr= viewer.getAddress();
				server.printLog("Sending transmit command to: " + client.getName() + "\n");
				SendToClient(clientAddr,clientPort,viewerAddr,watcherPort,watcherName);
				response = ClientServerProtocol.OK;
			}
			else{
				response = ClientServerProtocol.DENIED;
			}
		}
		return response;
	}

	private void SendToClient(InetAddress clientAddr, int clientPort,
			InetAddress viewerAddr, int watcherPort, String watcherName ) {
		byte[] buffer = (ClientServerProtocol.VIEWERTRANSMIT+" "+String.valueOf(watcherPort)+" "+viewerAddr.getHostAddress()+" "+watcherName).getBytes();
		try {
			server.getUdpSocket().send(new DatagramPacket(buffer, buffer.length,
					clientAddr, clientPort));
		} catch (IOException e) {
			// TODO Client Didn't receive
			e.printStackTrace();
		}
		
		
		
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

	private String meetMeTreat(int clientUDPPort,String clientName,String password){
		boolean errFlag = false;
		String response = ClientServerProtocol.SERVPROB;
		//find or create user in the users database
		try {
			if(!DataBaseManager.authenticateUser(clientName, password)){
				//user is not exists - create new user
				server.printLog("Creating new user: " + clientName);
				DataBaseManager.insertUser(clientName, password);
			}
			else{
				server.printLog("User already exists: " + clientName);
			}
		} catch (SQLException e) {
			server.printError(e.getMessage());
			errFlag = true;
		} catch (UserAlreadyExists e) {
			errFlag = true;
			server.printError(e.getMessage());
		}
		if(!errFlag){
			//now the user definitely exists --> start sending him alive messages 
			server.clients.addClientToUdpList(new OnlineClients.Client(clientSock.getInetAddress(), clientUDPPort,clientName,-1));
			response = ClientServerProtocol.NICETM + " " + Integer.toString(server.getServerUDPPort());
		}
		return response; 
	}

}
