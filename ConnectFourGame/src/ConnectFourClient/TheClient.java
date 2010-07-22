package ConnectFourClient;

import gameManager.Game;
import gameManager.GameGUI;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import common.LogPrinter;
import common.PasswordHashManager;
import common.RSAgenerator;
import common.UnhandeledReport;
import common.UnhandledReports;

import common.OnlineClient;
import common.PasswordHashManager.SystemUnavailableException;
import common.UnhandledReports.FileChanged;
import common.UnhandledReports.NoReports;
import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

/**
 * The client class, represents a player which can play
 * the game with another player P2P or watch someone playing
 */
public class TheClient {

	final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
	
	static public int unDEFport = -1; 
	public static class ServerWriteOrReadException extends Exception{}
	public LogPrinter logger = null;
	private int serverUdpPort = unDEFport;
	private int serverPort = unDEFport;
	private String serverHost = "";
	private String clientName = "";
	private String password = "";
	private int clientUdp = unDEFport;

	private int clientGamePort = unDEFport;
	private String opponentGameHost = "";
	private int opponentGamePort = unDEFport;
	private String opponentName = "";
	private int clientWatchPort = unDEFport;
	private int clientTransmitWaiterPort = unDEFport;
	private int opponentTransmitWaiterPort = unDEFport;
	private String gameId = ClientServerProtocol.noGame;
	private Game game = null;
	
	private HashMap<String, Viewer> viewersList;
	//UDP socket for sending alive messages
	public DatagramSocket aliveSocket = null;
	
	//UDP socket for receiving transmit requests
	private ServerSocket transmitWaiterSocket = null;
	
	//this will send the alive messages to the server
	private AliveSender 	aliveSender = null;
	
	//this will accept the TRANSMIT command
	private TransmitWaiter transmitWaiter = null;

	private GameWatcher watcher = null;
	
	private SSLSocketFactory  sslsocketfactory;
	
	private InetAddress serverAddress;
	
	
	/**
	 * The client class constructor.
	 * @param args
	 * @throws IOException
	 */
	public TheClient(String[] args) throws IOException {
		try {
			sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			logger = new LogPrinter("Client");
		} catch (IOException e) {
			System.out.println(LogPrinter.error_msg("Cannot open LOG printer: " + e.getMessage()));
			throw e;
		}
		viewersList= new HashMap<String, Viewer>();
		parseArguments(args);
		try {
			serverAddress = InetAddress.getByName(serverHost);
		} catch (UnknownHostException e) {
			logger.print_error("Cannot initialize connection with the server: " + e.getMessage());
		}
	}	
	
	
	/**
	 * Gets the user password string.
	 * @return password
	 */
	public String getPassword(){
		return password;
	}
	
	/**
	 * Gets the game ID.
	 * @return gameId
	 */
	public String getGameId(){
		return gameId;
	}
	
	/**
	 * Gets the game port of the user.
	 * @return clientGamePort
	 */
	public int getGamePort(){
		return clientGamePort;
	}
	
	/**
	 * Gets the watch port of the user.
	 * @return clientWatchPort
	 */
	public int getWatchPort()
	{
		return clientWatchPort;
	}
	
	/**
	 * Gets the transmit waiter socket.
	 * @return transmitWaiterSocket
	 */
	public ServerSocket getTransmitWaiterSocket(){
		return transmitWaiterSocket;
	}
	
	/**
	 * Gets the game history for a game.
	 * @return gameHistory
	 */
	public ArrayList<String> getGameHistory(){
		ArrayList<String> gameHistory = null;
		if(game != null){
			gameHistory = game.getGameHistory();
		}
		return gameHistory;
	}
	
	/**
	 * Gets the alive sender instance.
	 * @return aliveSender
	 */
	public AliveSender getAliveSender(){
		return aliveSender;
	}
	
	/**
	 * Inner class representing the Viewer (watcher) for this
	 * specific user.
	 */
	public static class Viewer extends  OnlineClient
	{
		public static class SendingToWatcherProblem extends Exception{}
		private TheClient transmitter;
		private Socket viewerSocket;
		private PrintWriter viewerWriter;
		private boolean firstMove;

		/**
		 * Constructor for the Viewer class. Creates the socket for transmission.
		 * @param transmitter
		 * @param hostAddr
		 * @param watcherPort
		 * @param name
		 */
		public Viewer(TheClient transmitter, InetAddress hostAddr, int watcherPort, String name) {
			super(hostAddr, TheClient.unDEFport, name, watcherPort,TheClient.unDEFport);
			this.transmitter = transmitter;
			try {
				viewerSocket = new Socket(this.getAddress(),this.getTCPPort());
				viewerWriter = new PrintWriter(viewerSocket.getOutputStream(),true);
			} catch (IOException e) {
				transmitter.logger.print_error("Cannont initialize connection with watcher: " + e.getMessage());

			}
			firstMove = true;
		}
		
		/**
		 * Ends the transmission. Closes the sockets.
		 */
		public void endTransmition(){
			if(viewerWriter != null){
				viewerWriter.close();
			}
			if(viewerSocket != null){
				try {
					viewerSocket.close();
				} catch (IOException e) {
					transmitter.logger.print_error("Problem closing watcher - " + this.getName() + " socket: " + e.getMessage());
				}
			}
		}
		
		/**
		 * Sends a move to the viewer.
		 * @param move
		 * @throws SendingToWatcherProblem
		 */
		public void sendMove(String move) throws SendingToWatcherProblem{	
			if(viewerWriter == null){
				return;
			}
			transmitter.logger.print_info("Sending to: " + this.getName() + ", on: " + this.getTCPPort() + " move: " + move);
			viewerWriter.println(move);
			viewerWriter.println();
			if (viewerWriter.checkError())
			{
				transmitter.logger.print_error("Problem sending move to watcher: " + this.getName() + ", no more moves will be sent to this player");
				endTransmition();
				throw new SendingToWatcherProblem();
			}
			transmitter.logger.print_info("Move sent!");
			firstMove = false;
		}
		
		/**
		 * Returns true if no move were sent yet, else - false.
		 * @return firstMove
		 */
		public boolean isFirstMove(){
			return firstMove;
		}
		
		/**
		 * Sends the game history till now, all moves.
		 */
		public void sendPreviousMoves(){
			ArrayList<String> gameHistory= transmitter.getGameHistory();
			StringBuilder strBuilder = new StringBuilder();
			for(String move : gameHistory){
				transmitter.logger.print_info("Sending history move to: " + this.getName() + "on: " + this.getTCPPort() + " move: " + move);
				strBuilder.append(move + "\n");
			}
			String moveTimer = null;
			if(transmitter.game.getCurrMoveTime() != null){
				moveTimer = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.MOVE_TIME,
						Integer.toString(transmitter.game.getCurrMoveTime())});
			}
			else{
				moveTimer = ClientServerProtocol.DENIED;
			}
			transmitter.logger.print_info("Sending the timer to : "+ this.getName() + " " + moveTimer);
			strBuilder.append(moveTimer);
			viewerWriter.println(strBuilder.toString());
			viewerWriter.println();
			transmitter.logger.print_info("Hystory has been sent!");
		}
	}
	
	/**
	 * Gets the list of all connected viewers.
	 * @return viewersList
	 */
	public HashMap<String, Viewer> getViewerList(){
		return viewersList;
	}
	
	/**
	 * Removes a viewer by name if it is exists.
	 * @param viewerName
	 */
	synchronized public void removeViewerIfExists(String viewerName){
		if (viewersList.containsKey(viewerName))
		{
			this.logger.print_info("Removing the watcher: " + viewerName);
			Viewer watcher = viewersList.get(viewerName);
			watcher.endTransmition();
			viewersList.remove(viewerName);
		}
	}
	
	/**
	 * Adds a viewer to the viewer list.
	 * @param viewer
	 */
	public void addToViewerList(Viewer viewer)
	{
		String name = viewer.getName();
		if (viewersList.containsKey(name))
		{
			return;
		}
		logger.print_info("Adding viewer: "+ viewer.getName() + "with port:" + viewer.getTCPPort());
		viewersList.put(name, viewer);
	}
	
	/**
	 * returns the server InetAddress
	 * @return serverAddress
	 */
	public InetAddress getServerAddress() {
		return serverAddress;
	}
	
	/**
	 * Gets the clients name.
	 * @return clientName
	 */
	public String getClientName()
	{
		return clientName;
	}

	/**
	 * Gets the clients alive port.
	 * @return clientUdp
	 */
	public int getClientAlivePort() {
		return clientUdp;
	}
	
	/**
	 * Gets the transmit waiter port
	 * @return clientTransmitWaiterPort
	 */
	public int getTransmitWaiterPort(){
		return clientTransmitWaiterPort;
	}

	/**
	 * gets the transmit waiter instance.
	 * @return transmitWaiter
	 */
	public TransmitWaiter getTransmitWaiter(){
		return transmitWaiter;
	}
	
	/**
	 * Gets server UDP port.
	 * @return serverUdpPort
	 */
	public int serverUDPPort() {
		return serverUdpPort;
	}
	
	/**
	 * Actually send a message to server and gets its response.
	 * @param message
	 * @return resp
	 * @throws IOException
	 * @throws ServerWriteOrReadException
	 */
	public Object innerSendMessageToServer(String message) throws IOException, ServerWriteOrReadException{
		Object resp=null;
		SSLSocket serverConnection = null;
		ClientServerProtocol parser = new ClientServerProtocol(msgType.SERVER);
		
		String[] commandPar = parseCommand(message,parser);
		if(commandPar == null){
			this.logger.print_error("Wrong message to server: " + parser.result);
			throw new IOException("Wrong message to the server");
		}
		
		// send the command to the server
		try{
			serverConnection = (SSLSocket)sslsocketfactory.createSocket(serverAddress, serverPort);
			serverConnection.setEnabledCipherSuites(enabledCipherSuites);
			serverConnection.setSoTimeout(ClientServerProtocol.timeout);
		}
		catch (IOException ex){
			logger.print_error("Connection problems with server: "+ ex.getMessage());
			throw ex;
		}
		
		PrintWriter out = new PrintWriter(serverConnection.getOutputStream(),true);
		
		//send the message
		String str  = ClientServerProtocol.buildCommand(commandPar);
		logger.print_info("Sending your message: "+ str+" to the server...");
		out.println(str);
		out.println();
		//checking that there were no errors on sending
		if (out.checkError())
		{
			//there was an error
			throw new ServerWriteOrReadException();
		}
		ObjectInputStream response = new ObjectInputStream(serverConnection.getInputStream());
		logger.print_info("READING response from socket...");
		
		// get server's response
		try {
			if((resp = response.readObject()) != null) {
				logger.print_info("Server Response is:" + resp);
			}
		}
		catch (SocketTimeoutException e)
		{
			throw new ServerWriteOrReadException();
		} catch (ClassNotFoundException e) {
			//class will always be found
		}
		if(out != null){
			out.close();
		}
		if(response!= null){
			response.close();
		}
		if(serverConnection != null){
			serverConnection.close();
		}
		return resp;

	}
	
	/**
	 * Gets the server public key.
	 * @return serverKey
	 * @throws IOException
	 * @throws ServerWriteOrReadException
	 */
	public Key getServerPublicKey() throws IOException, ServerWriteOrReadException{
		logger.print_info("Getting the public key of server...");
		Key serverKey = null;
		serverKey = (Key)innerSendMessageToServer(ClientServerProtocol.GETPUBKEY);
		if(serverKey != null){
			RSAgenerator.setEncKey(serverKey);
			return serverKey;
		}
		else{
			return null;
		}
	}
	
	/**
	 * Loads the properties file and returns it.
	 * @return prop
	 */
	private Properties getProperties(){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("client.configurations"));
		} catch (FileNotFoundException e) {
			//file will be found
			logger.print_error("Properties file was not found: " + e.getMessage());
		} catch (IOException e) {
			//will be read
			logger.print_error("Problem loading file: " + e.getMessage());
		}	
		return prop;
	}
	
	/**
	 * Loads the arguments from the properties file.
	 * @param args
	 */
	private void parseArguments(String[] args) {
		//From Properites
		Properties props = getProperties();
		
		serverHost = props.getProperty("SERVER_HOST");
		logger.print_info("Server: " + serverHost);
		serverPort = Integer.parseInt(props.getProperty("SERVER_TCP_PORT"));
		logger.print_info("Server TCP port: "+serverPort);
		clientUdp = Integer.parseInt(props.getProperty("CLIENT_UDP_LISTEN_PORT"));
		logger.print_info("Client Udp Listen port: "+clientUdp);
		clientTransmitWaiterPort = Integer.parseInt(props.getProperty("CLIENT_TRANSMIT_WAITER_PORT"));
		logger.print_info("Client TransmitWaiter port: "+clientTransmitWaiterPort);
		clientGamePort = Integer.parseInt(props.getProperty("CLIENT_GAME_PORT"));
		logger.print_info("Client Game port: "+clientGamePort);
		
		
//		serverHost = args[0];
//		logger.print_info("Server: " + serverHost);
//		serverPort = Integer.parseInt( args[1]);
//		logger.print_info("Server TCP port: "+serverPort);
//		clientUdp = Integer.parseInt (args[2]);
//		logger.print_info("Client Udp Listen port: "+clientUdp);
//		clientTransmitWaiterPort = Integer.parseInt( args[3]);
//		logger.print_info("Client TransmitWaiter port: "+clientTransmitWaiterPort);
//		clientGamePort = Integer.parseInt( args[4]);
//		logger.print_info("Client Game port: "+clientGamePort);
		
		clientWatchPort= clientGamePort;
	}
	
	/**
	 * Gets a server public key and then sends the message to the server.
	 * @param message
	 * @return response from the inner function
	 * @throws IOException
	 * @throws ServerWriteOrReadException
	 */
	public Object sendMessageToServer(String message) throws IOException, ServerWriteOrReadException
	{
		Key srvPK= getServerPublicKey();
		if(srvPK == null){
			throw new IOException("Problems while getting server PK");
		}
		return innerSendMessageToServer(message);
	}
	
	/**
	 * Reset the watcher to null.
	 */
	public void stopWatching(){
		watcher = null;
	}
	
	/**
	 * Starts the transmission waiter waiting for transmission
	 * command on the created transmission socket.
	 */
	private void startTransmitionWaiter(){
		try {
			transmitWaiterSocket = new ServerSocket(clientTransmitWaiterPort);
			System.out.println("Transmitter server socket was opened");
		} catch (IOException e) {
			this.logger.print_error("Problem open transmit waiter socket: " + e.getMessage());
		}
		transmitWaiter = new TransmitWaiter(transmitWaiterSocket,this);
		transmitWaiter.start();
	}
	
	/**
	 * Stops the transmission and reset the transmission data.
	 */
	private void closeTransmitions(){
		if(transmitWaiterSocket != null){
			this.getTransmitWaiter().endTransmition();
		}
		transmitWaiterSocket = null;
		transmitWaiter = null;
	}
	
	/**
	 * Prepare the password for transmission. First hashes it
	 * then encrypts with public key of the server.
	 * @param password
	 * @return preparedPass
	 */
	public String preparePassword(String password){
		String preparedPass = this.hashPassword(password);
		try {
			preparedPass =  RSAgenerator.encrypt(preparedPass);
		} catch (Exception e) {
			logger.print_error("Cannot encrypt the password: " + e.getMessage());
		}
		return preparedPass;
	}
	
	/**
	 * Parses a command and initializes data.
	 * return null if command is wrong.
	 * @param command
	 * @param parser
	 * @return params
	 */
	private String[] parseCommand(String command,ClientServerProtocol parser){
		String[] params = parser.parseCommand(command);
		
		if(params == null){
			return null;
		}
		else if(params[0].equals(ClientServerProtocol.MEETME)){
			clientUdp = Integer.parseInt(params[1]);
			clientName = params[2];
			password = params[3];
			params[3] = preparePassword(params[3]);
		}
		else if(params[0].equals(ClientServerProtocol.NEWGAME)){
			clientGamePort = Integer.parseInt(params[1]);
			clientTransmitWaiterPort = Integer.parseInt(params[2]);
			params[4] = preparePassword(params[4]);
		}
		else if(params[0].equals(ClientServerProtocol.PLAY)){
			clientGamePort = Integer.parseInt(params[1]);
			clientTransmitWaiterPort = Integer.parseInt(params[2]);
			gameId = params[3];
			params[5] = preparePassword(params[5]);
		}
		else if(params[0].equals(ClientServerProtocol.WATCH)){
			clientWatchPort = Integer.parseInt(params[1]);
			params[4] = preparePassword(params[4]);
		}
		else if(params[0].equals(ClientServerProtocol.SIGNUP)){
			clientName = params[1];
			password = params[2];
			params[2] = preparePassword(params[2]);
		}
		
		return params;
	}
	
	/**
	 * Hashes a password.
	 * @param pass
	 * @return hashed
	 */
	private String hashPassword(String pass){
		String hashed = null;
		PasswordHashManager hashManager = PasswordHashManager.getInstance();
		try {
			hashed = hashManager.encrypt(pass);
		} catch (SystemUnavailableException e) {
			this.logger.print_error("Cannot hash the password: "+ e.getMessage());
			hashed = null;
		}
		return hashed;
	}
	
	/**
	 * The nice to meet you handler function.
	 * Creates an AliveSender instance and starts it.
	 * @param params
	 */
	public void handleNICETM(String [] params)
	{
		 serverUdpPort = Integer.parseInt(params[1]);
		 aliveSender = new AliveSender(this);
		 aliveSender.start();
	}
	
	/**
	 * Game handler. Creates and starts a game GUI.
	 * Also starts the transmission waiter.
	 * @param params
	 * @param f
	 */
	public void HandleGameGUI(String [] params,MainFrame f)
	{
		gameId = params[1];
		logger.print_info("Received game: " + gameId + ", starting waiting on game port...");
		this.startTransmitionWaiter();
		
		game  = new GameGUI(clientName, null,gameId,f,clientGamePort, null,-1, true,this,unDEFport);

		((GameGUI)game).setVisible(true);
		this.closeTransmitions();
		gameId = ClientServerProtocol.noGame;
	}
		
	/**
	 * Creates an empty report for game.
	 * @return gameReport
	 */
	public UnhandeledReport getEmptyReport() {
		UnhandeledReport gameReport = new UnhandeledReport(this.gameId,this.clientName,
											Boolean.toString(Game.gameRes.NO_WINNER),
											Game.gameWinner.GAME_NOT_PLAYED);
		return gameReport;
	}

	/**
	 * Sends a game report to all the viewers.
	 * @param gameReportH
	 */
	public void makeReportToViewers(UnhandeledReport gameReportH) {
		this.logger.print_info("Trying to send report to viewers...");
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		String gameReport = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEREPORT,
				gameReportH.getGameId(),
				gameReportH.getClientName(),
				gameReportH.getGameResult(),
				gameReportH.getWinner(),
				"dummy"});
		String[] parsed = prot.parseCommand(gameReport);
		if(parsed == null){
			logger.print_error(prot.result + ". Bad game report: "+ gameReport);
		}
		getTransmitWaiter().sendMoveToViewers(gameReport);
	}
	
	/**
	 * Sends the report to the server. If sending is failed
	 * saves the report to the local file.
	 * @param gameReportH
	 */
	public void makeReportToServer(UnhandeledReport gameReportH) {
		this.logger.print_info("Trying to send report to server...");
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.SERVER);
		String preparedPass = preparePassword(password);
		String gameReport = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEREPORT,
				gameReportH.getGameId(),
				gameReportH.getClientName(),
				gameReportH.getGameResult(),
				gameReportH.getWinner(),
				preparedPass});
		String[] parsed = prot.parseCommand(gameReport);
		if(parsed == null){
			logger.print_error(prot.result + ". Bad game report: "+ gameReport);
		}
		
		Object resp = null;
		try {
			resp = this.sendMessageToServer(gameReport);
			if(this.parseServerResponse((String)resp) == null){
				throw new IOException("Bad server response");
			}
			String [] response = parseServerResponse((String)resp);
			if (response[0].equals(ClientServerProtocol.SERVPROB))
			{
				throw new IOException();
			}
		
		}catch (IOException e1) {
			this.logger.print_error(e1.getMessage() + "Saving the report locally..." );
			saveLocalReport(gameReportH);
		} catch (ServerWriteOrReadException e) {
			this.logger.print_error(e.getMessage() + "Saving the report locally..." );
			saveLocalReport(gameReportH);
		}
		
	}

	/**
	 * Saves a report to a local file.
	 * @param gameReportH
	 */
	private void saveLocalReport(UnhandeledReport gameReportH) {
		//if the report is empty
		if(gameReportH.getWinner().equals(Game.gameWinner.GAME_NOT_PLAYED)){
			return;
		}
		//server couldn't save the file in his file system/DB , we should save it in ours
		UnhandledReports reports = null;
		try {
			reports = new UnhandledReports(clientName);
		} catch (NoReports e) {
			//igonore
		} catch (FileChanged e) {
			//igonore
		}
		try {
			reports.addReport(gameReportH);
		} catch (IOException e) {
			logger.print_error("Problem while adding report to local file: " + e.getMessage());
		}
		
	}
	
	/**
	 * GOGOGO handler function. Creates and starts a game GUI.
	 * Also starts the transmission waiter.
	 * @param params
	 * @param mainFrame
	 */
	public void HandleGoGoGoGUI(String [] params, MainFrame mainFrame)
	{
		opponentGamePort = Integer.parseInt(params[1]);
		opponentGamePort = Integer.parseInt(params[1]);
		opponentGameHost = params[2];
		opponentName = params[3];
		opponentTransmitWaiterPort = Integer.parseInt(params[4]);
		logger.print_info("Accepted game with: " + opponentName + 
							" host: " + opponentGameHost + 
							" port: " + opponentGamePort +
							" game: " + gameId + 
							" opponent transmit port: " + opponentTransmitWaiterPort);
		this.startTransmitionWaiter();
		game= new  GameGUI(opponentName, clientName,gameId,mainFrame,clientGamePort,opponentGameHost,opponentGamePort, false,this,opponentTransmitWaiterPort);
		((GameGUI)game).setVisible(true);
		this.closeTransmitions();
		gameId = ClientServerProtocol.noGame;
	}
	
	/**
	 * EnjoyWatch handler function. Creates and starts the game watcher GUI.
	 * @param params
	 * @param redPlayer
	 * @param bluePlayer
	 * @param mainFrame
	 */
	public void HandleEnjoyWatch(String [] params,String redPlayer,String bluePlayer, MainFrame mainFrame)
	{
		watcher = new GameWatcher(this,redPlayer,bluePlayer,mainFrame);
		watcher.setVisible(true);
	}
	
	/**
	 * Checks whether the server response is legal.
	 * Breakes it down to elements and returns them
	 * @param message
	 * @return params
	 */
	public String[] parseServerResponse(String message)
	{
		ClientServerProtocol parser = new ClientServerProtocol(msgType.CLIENT);
		String[] params = parser.parseCommand(message);
		return params;
	}

	/**
	 * Sends the disconnect command to the server.
	 */
	public void disconnect()
	{
		String disconnectStr = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.DISCONNECT,
																				clientName});
		logger.print_info("Send disconnection to server: " + disconnectStr);
		Object resp = null;
		try{
			resp = this.sendMessageToServer(disconnectStr);
			if(parseServerResponse((String)resp) == null){
				throw new IOException("Bad server response");
			}
		}
		catch (IOException ex)
		{
			logger.print_error(ex.getMessage());
			//never mind , it will remove us because of the udp listener
		} catch (ServerWriteOrReadException e) {
			logger.print_error(e.getMessage());
			//never mind , it will remove us because of the udp listener
		}			
	}
	
	/**
	 * Report to the server the batch of unhandeled reports.
	 * @return true - if succeeded, else - false.
	 * @throws FileChanged
	 * @throws IOException
	 * @throws ServerWriteOrReadException
	 */
	public boolean reportUnhandeledReports() throws FileChanged, IOException, ServerWriteOrReadException {
		UnhandledReports reports=null;
		try {
			reports = new UnhandledReports(clientName);
		} catch (NoReports e) {
			return false;
		} catch (FileChanged e) {
			// Someone tried to change the file ,remove it!
			if(reports != null){
				reports.removeReportsFile();
			}	
			throw new FileChanged();
		}
		String gameReports = reports.createGamesReportString();
		if(gameReports != null){
			String preparedPass = preparePassword(password);
			gameReports = gameReports + ClientServerProtocol.paramSeparator + preparedPass;
			ArrayList<UnhandeledReport> response = (ArrayList<UnhandeledReport>)sendMessageToServer(gameReports);
			if(response != null){
				for (UnhandeledReport unhandeledReport : response) {
					reports.removeReport(unhandeledReport.getGameId(),unhandeledReport.getClientName());
				}
				if(reports.getReportNumber() == 0){
					System.out.println("REMOVE REPORT FILE");
					reports.removeReportsFile();
				}
			}
			else{
				this.logger.print_error("Received bad response from server, while reporting");
			}
		}
		else{
			return false;
		}
		return true;
		
	}

	/**
	 * Calls the opponentSurrender method of the game.
	 */
	public void opponentSurrender() {
		if(game != null){
			game.opponentSurrender();
		}		
	}
	
	/**
	 * Calls the resetConnection method of the game.
	 */
	public void refreshGameConnection(){
		System.out.println("The game is Null? : "+(game==null));
		if(game != null){
			game.resetConnection();
		}
	}
	
}
