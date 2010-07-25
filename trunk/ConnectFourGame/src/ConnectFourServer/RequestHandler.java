package ConnectFourServer;

import gameManager.Game;
import gameManager.GameGUI;
import gameManager.Player;
import gameManager.Player.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import javax.net.ssl.SSLSocket;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;
import ConnectFourClient.TheClient;
import ConnectFourServer.DataBaseManager.GameIdAlreadyExists;
import ConnectFourServer.DataBaseManager.GameIdNotExists;
import ConnectFourServer.DataBaseManager.UserAlreadyExists;

import common.OnlineClient;
import common.RSAgenerator;
import common.StatsReport;
import common.UnhandeledReport;
import common.UnhandledReports;
import common.UnhandledReports.FileChanged;
import common.UnhandledReports.NoReports;

/**
 * This class handles the clients request from the server.
 * it is instansiated when the servers accept a connection in the thread pool
 * and runs the RequestHandler in a new thread 
 *
 */
public class RequestHandler implements Runnable {

	// The client's socket from where we should read the
	// request and act according to it.
	private SSLSocket clientSock;

	// the server which the Request was sent to
	private MainServer server;

	/**
	 * Initialized with the client socket , where we should read the command from,
	 * and the main server.
	 * @param clientSocket
	 * @param server
	 */
	public RequestHandler(SSLSocket clientSocket, MainServer server) {
		clientSock = clientSocket;
		this.server = server;
	}

	/**
	 * This is the run method of the RequestHandler thread. It parses
	 * the client's request, processing it , and returns response.
	 */
	@Override
	public void run() {
		ObjectOutputStream out = null;
		BufferedReader in = null;
		String clientHost = clientSock.getInetAddress().getHostName();
		String clientIP = clientSock.getInetAddress().getHostAddress();
		server.printer.print_info("Starting new socket...");
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
					+ clientHost + " with IP: " + clientIP + " :" + e.getMessage());
		}
	}
	
	/**
	 * This method parsees the given client's message , treat's the message (manipulating 
	 * data structures inside the system according to the request) and returns a response
	 * Object which is sent to the client back.
	 * @param message
	 * @return
	 */
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
			//After parsing the command , we will invoke the appropriate method
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
	
	

	/**
	 * this method checks whenever the client is online or not , it 
	 * uses the server's OnlineClients data structure.
	 * @param clientName
	 * @return
	 */
	private boolean isClientOnline(String clientName){
		boolean res = false;
		OnlineClient theClient = server.clients.getClient(clientName);
		if(theClient != null){
			res = true;
		}
		return res;
	}
	
	/**
	 * Returns true of the game is active , otherwise returns false.
	 * It uses the server's OnlineGames data structure.
	 * @param gameId
	 * @return
	 */
	private boolean isGameOnline(String gameId){
		boolean res = false;
		Game theGame = server.games.getGame(gameId);
		if(theGame != null){
			res= true;
		}
		return res;
	}
	
	/**
	 * Returns true if the given client was participating in the given
	 * game. Otherwise returns false.
	 * @param clientName
	 * @param gameId
	 * @return
	 * @throws SQLException
	 */
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
			throw e;
		}
		
		return res;
	}
	
	/**
	 * Given the new watcher details , they are sent to the client , in order to connect
	 * between them
	 * @param clientAddr
	 * @param clientPort
	 * @param viewerAddr
	 * @param watcherPort
	 * @param watcherName
	 * @throws IOException
	 */
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
			server.printer.print_info("Transmit message: " + message);
			out.println(message);		
		} catch (IOException e) {
			server.printer.print_error("Problem sending transmit command: " + e.getMessage());
			throw e;
		}
		
		
		
	}
	

	
	
	
	//-------------------------------------------------------------------------------------------------
	//****************************** Treat Methods*****************************************************
	//-------------------------------------------------------------------------------------------------
	
	
	/**
	 * This method treats a batch reports request, where client want to report
	 * game results that he couldn't report before (the server couldn't save the results before)
	 * The server must update the database accordingly
	 */
	private Object batchGamesReportTreat(String[] params) {
		String[] reportsArr = new String[params.length - 2];
		System.arraycopy(params, 1, reportsArr, 0, params.length - 2);
		ArrayList<UnhandeledReport> reports= UnhandledReports.gameReportsFromReportsArray(reportsArr);
		ArrayList<UnhandeledReport> correctGameIds= new ArrayList<UnhandeledReport>();
		for (UnhandeledReport unhandeledReport : reports) {
			server.printer.print_info("Trying to add report: " + unhandeledReport);
			String result =this.gamesReportTreat(unhandeledReport.getGameId(), unhandeledReport.getClientName(), 
					Boolean.getBoolean(unhandeledReport.getGameResult()), unhandeledReport.getWinner(), params[params.length-1]);
			if(result.equals(ClientServerProtocol.OK)){
				correctGameIds.add(unhandeledReport);
			}
		}
		return correctGameIds;	
	}
	
	/**
	 * This method treats server's public key request (the public key for
	 * RSA encryption)
	 * @return
	 */
	private Object pubKeyTreat(){
		return RSAgenerator.getPubKey();
	}
	
	/**
	 * This method treats a recieved report about certain game result. If the database is not 
	 * available now , the report will be saved in a special Unahndeled Reports file for futher
	 * fetching (when the db will return to work)
	 * @param gameId
	 * @param clientName
	 * @param gameRes
	 * @param winner
	 * @param password
	 * @return
	 */
	private synchronized String gamesReportTreat(String gameId, String clientName, boolean gameRes, String winner,String password){
		String response = ClientServerProtocol.DENIED ;
		//check if the client is online
		try {
			if(!authenticateUser(clientName,password)){
				return response;
			}
		} catch (Exception e1) {
			response = ClientServerProtocol.SERVPROB;
			return response;
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

	

	
	/**
	 * This methods should handle join game requests. It recieves the second game participant of gameId , and his
	 * detials (game port, transmition Port for watchers and ect..). If everything is o.k , it adds the new player
	 * to the game , afther this operation , watchers can watch the game.
	 * @param gamePort
	 * @param transmitionPort
	 * @param gameId
	 * @param clientName
	 * @param password
	 * @return
	 */
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
						server.printer.print_info("Player has been added to the game: " + gameId );
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
								server.printer.print_info("Adding the game to database: " + gameId);
								DataBaseManager.createGame(enemy.getName(), clientName, gameId);
							}
						} catch (SQLException e) {
							server.printer.print_error("Cannot insert the game to the database: " + e.getMessage());
							response = ClientServerProtocol.SERVPROB;
						} catch (GameIdAlreadyExists e) {
							server.printer.print_error("The game is already in the database: " + e.getMessage());
							response = ClientServerProtocol.SERVPROB;
						}
					}
					else{
						server.printer.print_error("WE HAVE PROBLEM IN SERVER MAN");
						response = ClientServerProtocol.SERVPROB;
					}
			}
			else{
				server.printer.print_error("No such gameId:" + gameId);
				response = ClientServerProtocol.DENIED;
			}	
		}
		return response;
	}

	
	/**
	 * This method authenticates the given player with the DB. Returns true if he is
	 * authenticated, false otherwise.
	 * @param playerName
	 * @param password
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * This method treats a user request to watch the given gameId. The user sends its watchPort 
	 * , (his address is already exists in the server, becuase he is connected) .
	 * Now the server chooses randonly the player that will send to the watcher the moves and sends
	 * him watcher's details.
	 * @param watcherPort
	 * @param gameId
	 * @param watcherName
	 * @param password
	 * @return
	 */
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
				server.printer.print_info("Sending transmit command to: " + client.getName());
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
			server.printer.print_error("No such user: '" + watcherName + "'");
		}
		return response;
	}
	
	/**
	 * This method treats a sign-up request, when new user want to register to
	 * the system (have his statistics and ect..). If the username already exists , It returns
	 * an error
	 * @param username
	 * @param password
	 * @return
	 */
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
		}
		response= ClientServerProtocol.OK;
		return response;
	}
	
	/**
	 * This method handles online games request, it sends to the client the current
	 * online games, using the Online games data structure
	 * @return
	 */
	private ArrayList<GameForClient> getOnlineGamesTreat()
	{
		return server.games.getOnlineGamesForClient();
	}
	
	/**
	 * This method handles the login-in process , when the user wants to log into
	 * the system , he send a meet-me request , and this method adds the user to the current
	 * online players.
	 * @param clientUDPPort
	 * @param clientName
	 * @param password
	 * @return
	 */
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
	
	/**
	 * This method handles a new game request. if the client is participating in a game , it removes it 
	 * from the online games and creates a new game where the given client is the host
	 * @param gamePort
	 * @param transmitionPort
	 * @param playerName
	 * @param password
	 * @return
	 */
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
			server.printer.print_info("The game has been created: " + theClient.getGame());

		}
		return response;
	}	
	
	/**
	 * This method treats a statistics request. It sends to the client his statistics
	 * and the statistics of the top 10 players of the system.
	 * @param username
	 * @return
	 */
	private Object getStatisticsTreat(String username) {
		StatsReport users=null;	
		try {
			 users=DataBaseManager.getTopTenUsers(username);
		} catch (SQLException e) {
			return null;
		}
		return users;
	}

	/**
	 * This method treats a player disconnection. it removes him from the online players
	 * and stops the udp listener from waiting for alive message from this player
	 * @param clientName
	 * @return
	 */
	private Object playerDisconnectTreat(String clientName) {
		server.clients.removeClient(clientName);
		server.getUdpListener().removeClient(clientName);
		return ClientServerProtocol.OK;
	}

}
