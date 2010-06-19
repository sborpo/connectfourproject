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

import ConnectFourClient.TheClient;
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
		//ObjectOutputStream out = null;
		//VALERIY
		PrintWriter out = null;
		BufferedReader in = null;
		String clientHost = clientSock.getInetAddress().getHostName();
		String clientIP = clientSock.getInetAddress().getHostAddress();
		server.printer.print_info("Starting new socket...\n");
		try {
			//VALERIY
			//out = new ObjectOutputStream(clientSock.getOutputStream());
			out = new PrintWriter(clientSock.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
			
			String inputLine;
			//VALERIY
			//Object response = ClientServerProtocol.WHAT;
			String response = ClientServerProtocol.WHAT;
			// reads the input line by line and appends in to the string builder
			while ((inputLine = in.readLine()) != null) {
				if(inputLine.equals("")){
					break;
				}
				String logMessage = "From Client with name :" + clientHost + " IP: "
				+ clientIP + " Recieved This Message: \n -------------\n"
				+ inputLine + "\n-------------\n\n\n";
				server.printer.print_info(logMessage);
				
				//get the response
				//VALERIY
				response = (String)respondToMessage(inputLine);
				server.printer.print_info("Send to client: " + response);
				//VALERIY
				//out.writeObject(response);
				out.write(response);
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
				respondMsg = meetMeTreat(Integer.parseInt(params[1]),params[2],Integer.parseInt(params[3]),params[4]); 
			}
			else if(command.equals(ClientServerProtocol.NEWGAME)){
				respondMsg = newGameTreat(Integer.parseInt(params[1]),params[2]);
			}
			else if(command.equals(ClientServerProtocol.SIGNUP)){
				respondMsg = signupTreat(params[1],params[2]);
			}
			else if(command.equals(ClientServerProtocol.PLAY)){
				respondMsg = playTreat(Integer.parseInt(params[1]),params[2],params[3]);
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
				int clientPort= client.getUDPtransmitPort();
				
				InetAddress viewerAddr= viewer.getAddress();
				server.printer.print_info("Sending transmit command to: " + client.getName() + "\n");
				SendToClient(clientAddr,clientPort,viewerAddr,watcherPort,watcherName);
				response = ClientServerProtocol.ENJOYWATCH;
			}
			else{
				response = ClientServerProtocol.DENIED;
			}
		}
		return response;
	}

	private void SendToClient(InetAddress clientAddr, int clientPort,
			InetAddress viewerAddr, int watcherPort, String watcherName ) {
		byte[] buffer = (ClientServerProtocol.VIEWERTRANSMIT+" "+ watcherPort +" "+viewerAddr.getHostAddress()+" "+watcherName).getBytes();
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
							server.printer.print_info("Player has been added to the game: " + gameId + "\n");
							theClient.setGameForClient(gameId);
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
			else{
				server.printer.print_error("User is already in game!");
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
				server.printer.print_info("The game has been created: " + theClient.getGame() + "\n");
			}
			else{
				response = ClientServerProtocol.DENIED;
			}
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
	
	private ArrayList<StamClass> getOnlineGamesTreat()
	{
		StamClass c = new StamClass(5, 3);
		ArrayList<StamClass> l = new ArrayList<StamClass>();
		l.add(c);
		return l;
		//return server.games.getOnlineGames();
	}
	
	private String meetMeTreat(int clientUDPPort,String clientName,int clientTransmitPort,String password){
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
			//now the user definitely exists --> start sending him alive messages 
			server.clients.addClientToUdpList(new OnlineClients.Client(clientSock.getInetAddress(), clientUDPPort,clientName,TheClient.unDEFport,clientTransmitPort));
			response = ClientServerProtocol.NICETM + " " + Integer.toString(server.getServerUDPPort());
			server.udpListener.openTimerFor(clientName);
		}
		return response; 
	}

}
