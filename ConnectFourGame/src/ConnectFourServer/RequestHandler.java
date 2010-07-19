package ConnectFourServer;

import gameManager.Game;
import gameManager.GameGUI;
//import gameManager.GameImp;
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
import javax.net.ssl.SSLSocket;

import common.OnlineClient;
import common.PasswordHashManager;
import common.RSAgenerator;
import common.StatsReport;
import common.UnhandeledReport;
import common.UnhandledReports;
import common.PasswordHashManager.SystemUnavailableException;
import common.UnhandledReports.FileChanged;
import common.UnhandledReports.NoReports;

import ConnectFourClient.TheClient;
import ConnectFourServer.DataBaseManager.GameIdAlreadyExists;
import ConnectFourServer.DataBaseManager.GameIdNotExists;
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
	private SSLSocket clientSock;

	// the server which the Request was sent to
	private MainServer server;

	public RequestHandler(SSLSocket clientSocket, MainServer server) {
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
				respondMsg = newGameTreat(Integer.parseInt(params[1]),Integer.parseInt(params[2]),params[3],params[4]);
			}
			else if(command.equals(ClientServerProtocol.DISCONNECT)){
				respondMsg = playerDisconnectTreat(params[1]);
			}
			else if(command.equals(ClientServerProtocol.BATCHGAMESREPORT)){
				respondMsg = batchGamesReportTreat(params);
			}
			else if(command.equals(ClientServerProtocol.GAMEREPORT)){
				respondMsg = gamesReportTreat(params[1],params[2],Boolean.parseBoolean(params[3]),params[4],params[5]);
			}
			else if(command.equals(ClientServerProtocol.SIGNUP)){
				respondMsg = signupTreat(params[1],params[2]);
			}
			else if(command.equals(ClientServerProtocol.PLAY)){
				respondMsg = playTreat(Integer.parseInt(params[1]),Integer.parseInt(params[2]),params[3],params[4],params[5]);
			}
			else if(command.equals(ClientServerProtocol.GAMELIST)){
				respondMsg = getOnlineGamesTreat();
			}
			else if(command.equals(ClientServerProtocol.STATS_REQUEST)){
				respondMsg = getStatisticsTreat(params[1]);
			}
			else if(command.equals(ClientServerProtocol.WATCH)){
				respondMsg = watchTreat(Integer.parseInt(params[1]),params[2],params[3],params[4]);
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
	
	
	private Object getStatisticsTreat(String username) {
		StatsReport users=null;	
		try {
			 users=DataBaseManager.getTopTenUsers(username);
		} catch (SQLException e) {
			return null;
		}
		return users;
	}

	private Object playerDisconnectTreat(String clientName) {
		server.clients.removeClient(clientName);
		server.getUdpListener().removeClient(clientName);
		return ClientServerProtocol.OK;
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
	
	private boolean wasClientInTheGame(String clientName,String gameId) throws SQLException{
		boolean res= false;
		OnlineClient theClient = server.clients.getClient(clientName);
		Game theGame = server.games.getGame(gameId);
		try {
			if(theClient != null){
				//game is not online
				if(theGame == null){
					boolean wasGamePlayed;
					wasGamePlayed = DataBaseManager.isGameIdExists(gameId);
					//if the game exists in database
					if(wasGamePlayed){
						//if the player was one of the players of the game
						if(DataBaseManager.isClientPlayedGame(clientName, gameId)){
							res = true;
						}
					}
				}
				//the game is online
				else if(theClient.getGame().equals(theGame.getId()) && theGame.isPlayer(clientName) != null){
					res= true;
				}
			}
		} catch (SQLException e) {
			server.printer.print_error("Server database problems");
			e.printStackTrace();
			throw e;
		}
		
		return res;
	}
	
	
	private Object batchGamesReportTreat(String[] params) {
		String[] reportsArr = new String[params.length - 2];
		System.arraycopy(params, 1, reportsArr, 0, params.length - 2);
		ArrayList<UnhandeledReport> reports= UnhandledReports.gameReportsFromReportsArray(reportsArr);
		ArrayList<UnhandeledReport> correctGameIds= new ArrayList<UnhandeledReport>();
		for (UnhandeledReport unhandeledReport : reports) {
			server.printer.print_info("Trying to add report: " + unhandeledReport);
			String result =this.gamesReportTreat(unhandeledReport.getGameId(), unhandeledReport.getClientName(), 
					Boolean.getBoolean(unhandeledReport.getGameResult()), unhandeledReport.getWinner(), params[params.length-1]);
			System.out.println("RESUT: " +result);
			if(result.equals(ClientServerProtocol.OK)){
				correctGameIds.add(unhandeledReport);
			}
		}
		return correctGameIds;	
	}
	
	private Object pubKeyTreat(){
		return RSAgenerator.getPubKey();
	}
	
	private synchronized String gamesReportTreat(String gameId, String clientName, boolean gameRes, String winner,String password){
		String response = ClientServerProtocol.KNOWYA ;
		//check if the client is online
		if(!isClientOnline(clientName)){
			server.printer.print_error("The client is not online: "+clientName);
			return ClientServerProtocol.DENIED;
		}
		try {
			//get the game from the online games
			Game theGame = server.games.getGame(gameId);
			//remove it from online games
			if(theGame != null){
					server.printer.print_info("Remove the game from online list...");
					server.games.removeGame(gameId);
			}
			//try to authenticate user
			try{
				if(!authenticateUser(clientName,password)){
						server.printer.print_error("Cannot authenticate user: " + clientName + " PASS: '" + password + "'");
						if(theGame != null){
							server.games.addGame(theGame);
						}
						response = ClientServerProtocol.DENIED;
						return response;
				}
			}catch(Exception e)
			{
				throw new SQLException();
			}
	
			//check if the game exists in the database
			boolean isExists;
			try{
				isExists=DataBaseManager.isGameIdExists(gameId);
			}
			catch(SQLException ex)
			{
				throw ex;
			}
			if(!(gameId != null && isExists ))
			//the game not in database
			{
						//if the game wasn't played- does not matter
						if(winner.equals(Game.gameWinner.GAME_NOT_PLAYED)){
							response = ClientServerProtocol.OK;
						}
						//else - error
						else
						{
							server.printer.print_error("The game is not exists in database: "+gameId);
							response = ClientServerProtocol.DENIED;
						}
						return response;
			}
			//check if the client is/was in this game
			if(wasClientInTheGame(clientName,gameId)){
					OnlineClient theClient = server.clients.getClient(clientName);
					//reset game for client
					theClient.resetGame();
					//if the game was not played
					if(winner.equals(Game.gameWinner.GAME_NOT_PLAYED)){
					            //and the reporting player is second player
								if(theGame.getPlayer(Player.Color.BLUE).getName().equals(clientName)){
									//return the game into the online games list
									server.printer.print_info("The game wasn't played, return game to online games...");
									theGame.removeSecondPlayer();
									server.games.addGame(theGame);
								}
								DataBaseManager.removeGame(gameId);
								response = ClientServerProtocol.OK;
								return response;
					}
					//else - the game was played
					//add the report
					try {
						//check if the game IS ONLINE
						if(isGameOnline(gameId)){
								server.games.removeGame(gameId);
						}
						server.printer.print_info("Adding the report to the database: winner = " + winner);
						//throw new SQLException("MY EXCEPTION");
						DataBaseManager.makeReport(gameId, clientName, winner);
						//TODO: TREAT STATISTICS FOR THIS GAME PLAYERS
					}  catch (GameIdNotExists e) {
								server.printer.print_error("Problem while adding report to the database: " + e.getMessage());
								response = ClientServerProtocol.SERVPROB;
								return response;
					}
							//return ok message
					response = ClientServerProtocol.OK;
					return response;
				}
				server.printer.print_error("The client: " + clientName + " is not in the game: "+gameId);
				response = ClientServerProtocol.DENIED;
			} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						try {
							UnhandledReports reports = new UnhandledReports(MainServer.ReportFileName);
							try {
								reports.addReport(new UnhandeledReport(gameId, clientName, String.valueOf(gameRes), winner));
								server.printer.print_info("The report for game: " + gameId + " was added locally correctly");
							} catch (IOException e1) {
								//the server couldn't save the report, so return to the user the responsibility
								server.printer.print_error("The server couln't save to report file: "+gameId);
								response = ClientServerProtocol.SERVPROB;
								return response;
							}
						} catch (NoReports e1) {
							//Ignore
						} catch (FileChanged e1) {
							//Ignore
						}
						server.printer.print_error("The server couldn't save report to DB, but saved to local file");
						return ClientServerProtocol.OK;
			} catch (GameIdNotExists e) {
				//DO NOTHING
			}
		return response;
	}

	private String watchTreat(int watcherPort, String gameId,String watcherName, String password) {
		String response = ClientServerProtocol.DENIED;
		
		try {
			if(!authenticateUser(watcherName,password)){
				return response;
			}
		} catch (Exception e1) {
			response = ClientServerProtocol.SERVPROB;
			return response;
		}
		
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
				try {
					SendToClient(clientAddr,clientPort,viewerAddr,watcherPort,watcherName);
				} catch (IOException e) {
					response = ClientServerProtocol.DENIED;
					return response;
				}
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
			InetAddress viewerAddr, int watcherPort, String watcherName ) throws IOException {
		try {
			server.printer.print_info("Trying to send transmit command to: " + clientAddr.getHostAddress() + " on port: " + clientPort);
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
			server.printer.print_error("Problem sending transmit command: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		
		
		
	}

	private String playTreat(int gamePort, int transmitionPort, String gameId,String clientName,String password) {
		String response = ClientServerProtocol.DENIED;
		
		try {
			if(!authenticateUser(clientName,password)){
				return response;
			}
		} catch (Exception e1) {
			response = ClientServerProtocol.SERVPROB;
			return response;
		}
		
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
				//Player thePlayer = theGame.getPlayer(Player.Color.BLUE);
				//if(thePlayer == null){
					theGame.addPlayer(clientName);
					OnlineClient enemy = server.clients.getClient(theGame.getPlayer(Player.Color.RED).getName());
					if(enemy != null){
						response = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GOGOGO, 
																					Integer.toString(enemy.getTCPPort()), 
																					enemy.getAddress().getHostAddress(),
																					enemy.getName(),
																					Integer.toString(enemy.getTransmitPort())});
						server.printer.print_info("Player has been added to the game: " + gameId + "\n");
						theClient.setGameForClient(gameId);
						try {
							if(DataBaseManager.isGameIdExists(gameId)){
								server.printer.print_info("Game exists in database - updating the second player...");
								try {
									DataBaseManager.updateGamePlayer(gameId,clientName);
								} catch (GameIdNotExists e) {
									//CANNOT HAPPEN
								}
							}
							else{
								server.printer.print_info("Adding the game to database: " + gameId + "\n");
								DataBaseManager.createGame(enemy.getName(), clientName, gameId);
							}
						} catch (SQLException e) {
							server.printer.print_error("Cannot insert the game to the database: " + e.getMessage());
							response = ClientServerProtocol.SERVPROB;
							//TODO to do something
						} catch (GameIdAlreadyExists e) {
							server.printer.print_error("The game is already in the database: " + e.getMessage());
							response = ClientServerProtocol.SERVPROB;
							//TODO something
						}
					}
					else{
						server.printer.print_error("WE HAVE PROBLEM IN SERVER MAN\n");
						response = ClientServerProtocol.SERVPROB;
					}
//				}
//				else{
//					server.printer.print_error("Other man playing..." + thePlayer.getName() +"\n");
//					response = ClientServerProtocol.DENIED;
//				}
			}
			else{
				server.printer.print_error("No such gameId:" + gameId);
				response = ClientServerProtocol.DENIED;
			}	
		}
		return response;
	}

	private String newGameTreat(int gamePort,int transmitionPort, String playerName, String password) {
		String response = ClientServerProtocol.DENIED;
		
		try {
			if(!authenticateUser(playerName,password)){
				return response;
			}
		} catch (Exception e) {
			response = ClientServerProtocol.SERVPROB;
			return response;
		}
		
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
			Game newGame = new GameGUI(playerName, null, gameId, null, gamePort, null, TheClient.unDEFport, true, null,TheClient.unDEFport);
			server.games.addGame(newGame);
			theClient.setGameForClient(gameId);
			response = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAME,gameId});
			server.printer.print_info("The game has been created: " + theClient.getGame() + "\n");

		}
		return response;
	}	
	
	private boolean authenticateUser(String playerName, String password) throws Exception {
		boolean result = false;
		try {
			if(DataBaseManager.authenticateUser(playerName, password)){
				result = true;
			}
		} catch (Exception e) {
			server.printer.print_error("Failed to athenticate user: " + playerName);
			throw e;
		}
		return result;
	}

	private String signupTreat(String username , String password)
	{
		String response = ClientServerProtocol.SERVPROB;
		try {
			DataBaseManager.insertUser(username, password);
		} catch (UserAlreadyExists e) {
			response=ClientServerProtocol.USERALREADYEXISTS;
			return response;
		} catch (Exception e) {
			server.printer.print_error("Cannot decrypt and save password: " + e.getMessage());
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
		String response = ClientServerProtocol.DENIED;
		
		try {
			if(!authenticateUser(clientName,password)){
				response = ClientServerProtocol.USERNOTEXISTS;
				return response;
			}
			else{
				server.printer.print_info("User entered: " + clientName);	
			}
		} catch (Exception e) {
			response = ClientServerProtocol.SERVPROB;
			return response;
		}
		
		server.clients.addClientToUdpList(new OnlineClient(clientSock.getInetAddress(), clientUDPPort,clientName,TheClient.unDEFport,TheClient.unDEFport));
		response = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.NICETM
																	,Integer.toString(server.getServerUDPPort())});
		server.udpListener.openTimerFor(clientName);
		return response; 
	}

}
