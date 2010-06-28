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
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import common.OnlineClient;
import common.PasswordHashManager;
import common.RSAgenerator;
import common.UnhandeledReport;
import common.UnhandledReports;
import common.PasswordHashManager.SystemUnavailableException;
import common.UnhandledReports.FileChanged;
import common.UnhandledReports.NoReports;

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
			StringBuilder strBuild = new StringBuilder();
			// reads the input line by line and appends in to the string builder
			while ((inputLine = in.readLine()) != null) {
				if(inputLine.equals("")){
					break;
				}
				if(strBuild.length() > 0){
					strBuild.append("\n");
				}
				strBuild.append(inputLine);
			}
				
			String message = strBuild.toString();
			String logMessage = "From Client with host :" + clientHost + " IP: "
			+ clientIP + " Recieved This Message: \n -------------\n"
			+ message + "\n-------------";
			server.printer.print_info(logMessage);
			//get the response
			//VALERIY
			response = respondToMessage(message);
			if(response == null){
				server.printer.print_error("Can't respond the message");
			}
			else{
				server.printer.print_info("Send to client: " + response);
				//VALERIY
				//out.writeObject(response);
				out.writeObject(response);
			}
		} catch (IOException ex) {
			server.printer.print_error("Problem reading from Client: " + clientHost
					+ " with IP: " + clientIP + " " + ex.getMessage());
		} 
		
		try {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
			if(clientSock != null){
				clientSock.close();
			}
		} catch (IOException e) {
			server.printer.print_error("Problem closing socket from Client: "
					+ clientHost + " with IP: " + clientIP);
			e.printStackTrace();
		}
	}
	
	private Object respondToMessage(String message){
		ClientServerProtocol parser = new ClientServerProtocol(msgType.SERVER);
		String[] params = parser.parseCommand(message);
		Object respondMsg = null;
		if(params == null){
			server.printer.print_error("Server can't respod: " + parser.result);
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
			else if(command.equals(ClientServerProtocol.GETPUBKEY)){
				respondMsg = pubKeyTreat();
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
		ArrayList<UnhandeledReport> reports= UnhandledReports.gameReportsFromReportString(params);
		ArrayList<String> correctGameIds= new ArrayList<String>();
		for (UnhandeledReport unhandeledReport : reports) {
			try {
				DataBaseManager.makeReport(unhandeledReport.getGameId(), unhandeledReport.getClientName(), unhandeledReport.getWinner());
				correctGameIds.add(unhandeledReport.getGameId());
			} catch (SQLException e) {
				//There was a problem to add this game report so dont add it to the reported games
				//Try to move the game to the server's unhandeled reports file
				try {
					UnhandledReports localReports = new UnhandledReports(server.ReportFileName);
					try {
						localReports.addReport(unhandeledReport);
						//the server added it , so we can tell the client that it was reported
						correctGameIds.add(unhandeledReport.getGameId());
					} catch (IOException e1) {
						
					}
				} catch (NoReports e1) {
					//Ignore
				} catch (FileChanged e1) {
					//Ignore
				}
				
			}
		}
		return correctGameIds;
		
	}
	
	private Object pubKeyTreat(){
		return RSAgenerator.getPubKey();
	}
	
	private String gamesReportTreat(String gameId, String clientName, boolean gameRes, String winner){
		String response = ClientServerProtocol.KNOWYA ;
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
						try {
							UnhandledReports reports = new UnhandledReports(server.ReportFileName);
							try {
								reports.addReport(new UnhandeledReport(gameId, clientName, String.valueOf(gameRes), winner));
								server.printer.print_error("The report was added correctly");
							} catch (IOException e1) {
								
								//the server couldn't save the report, so return to the user the responsibilityy
								server.printer.print_error("The server couln't save to report file: "+gameId);
								response = ClientServerProtocol.DBERRORREPSAVED;
								return response;
							}
						} catch (NoReports e1) {
							//Ignore
						} catch (FileChanged e1) {
							//Ignore
						}
						server.printer.print_error("The server couldn't save to DB and to report file:  "+gameId);
						response = ClientServerProtocol.DBERRORREPSAVED;
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
		else{
			System.out.println("No such user: '" + watcherName + "'");
		}
		return response;
	}

	private void SendToClient(InetAddress clientAddr, int clientPort,
			InetAddress viewerAddr, int watcherPort, String watcherName ) {
		try {
			Socket clientTsmtSocket = new Socket(clientAddr,clientPort);
			PrintWriter out = new PrintWriter(clientTsmtSocket.getOutputStream(),true);
			String message = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.VIEWERTRANSMIT,
																			Integer.toString(watcherPort),
																			viewerAddr.getHostAddress(),
																			watcherName,
																			clientSock.getInetAddress().getHostAddress()});
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
						response = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GOGOGO, 
																					Integer.toString(enemy.getTCPPort()), 
																					enemy.getAddress().getHostAddress(),
																					enemy.getName()});
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
			response = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAME,gameId});
			server.printer.print_info("The game has been created: " + theClient.getGame() + "\n");

		}
		return response;
	}

	
	private String hashPassword(String pass){
		String hashed = null;
		PasswordHashManager hashManager = PasswordHashManager.getInstance();
		try {
			hashed = hashManager.encrypt(pass);
		} catch (SystemUnavailableException e) {
			this.server.printer.print_error("Cannot hash the password: "+ e.getMessage());
			e.printStackTrace();
			hashed = null;
		}
		return hashed;
	}
	
	private String signupTreat(String username , String password)
	{
		String response = ClientServerProtocol.SERVPROB;
		try {
			String decrypted = RSAgenerator.decrypt(password);
			String hashedPswd = hashPassword(decrypted);
			DataBaseManager.insertUser(username, hashedPswd);
		} catch (UserAlreadyExists e) {
			response=ClientServerProtocol.USERALREADYEXISTS;
			return response;
		} catch (Exception e) {
			server.printer.print_error("Cannot decrypt password: " + e.getMessage());
			e.printStackTrace();
		}
		response= ClientServerProtocol.OK;
		return response;
	}
	
	private ArrayList<GameForClient> getOnlineGamesTreat()
	{
		return server.games.getOnlineGamesForClient();
	}
	
	private String meetMeTreat(int clientUDPPort,String clientName,String password){
		boolean errFlag = false;
		String response = ClientServerProtocol.SERVPROB;
		try {
			String decrypted = RSAgenerator.decrypt(password);
			String hashedPswd = hashPassword(decrypted);
			
			if(!DataBaseManager.authenticateUser(clientName, hashedPswd)){
				response = ClientServerProtocol.USERNOTEXISTS;
				return response;
			}
			else{
				server.printer.print_info("User entered: " + clientName);	
			}
		} catch (SQLException e) {
			server.printer.print_error(e.getMessage());
			errFlag = true;
		} catch (Exception e){
			server.printer.print_error("Cannot decrypt password: " + e.getMessage());
			e.printStackTrace();
		}
		
		if(!errFlag){
			server.clients.addClientToUdpList(new OnlineClient(clientSock.getInetAddress(), clientUDPPort,clientName,TheClient.unDEFport,TheClient.unDEFport));
			response = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.NICETM
																		,Integer.toString(server.getServerUDPPort())});
			server.udpListener.openTimerFor(clientName);
		}
		return response; 
	}

}
