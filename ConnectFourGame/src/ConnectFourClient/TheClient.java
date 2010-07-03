package ConnectFourClient;

import gameManager.Game;
import gameManager.GameGUI;
//import gameManager.GameImp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import javax.net.ssl.SSLServerSocketFactory;
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
 * 
 * @author Valeriy Leykin The client class, represents a player which can play
 *         the game with another player P2P
 */
public class TheClient {

	final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
	
	static public int unDEFport = -1; 
	
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
	private AliveSender 	echoServerListener = null;
	
	//this will accept the TRANSMIT command
	private TransmitWaiter transmitWaiter = null;

	private GameWatcher watcher = null;
	
	private SSLSocketFactory  sslsocketfactory;
	
	private InetAddress serverAddress;
	
	public String getPassword(){
		return password;
	}
	
	public String getGameId(){
		return gameId;
	}
	
	public int getGamePort(){
		return clientGamePort;
	}
	
	public int getWatchPort()
	{
		return clientWatchPort;
	}
	
	public ServerSocket getTransmitWaiterSocket(){
		return transmitWaiterSocket;
	}
	
	public ArrayList<String> getGameHistory(){
		return game.getGameHistory();
	}
	

	
	public static class Viewer extends  OnlineClient
	{
		private TheClient transmitter;
		private Socket viewerSocket;
		private PrintWriter viewerWriter;
		private boolean firstMove;

		public Viewer(TheClient transmitter, InetAddress hostAddr, int watcherPort, String name) {
			super(hostAddr, TheClient.unDEFport, name, watcherPort,TheClient.unDEFport);
			this.transmitter = transmitter;
			try {
				viewerSocket = new Socket(this.getAddress(),this.getTCPPort());
				viewerWriter = new PrintWriter(viewerSocket.getOutputStream(),true);
			} catch (IOException e) {
				transmitter.logger.print_error("Cannont initialize connection with watcher");
				e.printStackTrace();
			}
			firstMove = true;
		}
		
		public void endTransmition(){
			if(viewerWriter != null){
				viewerWriter.close();
			}
			if(viewerSocket != null){
				try {
					viewerSocket.close();
				} catch (IOException e) {
					transmitter.logger.print_error("Problem closing watcher - " + this.getName() + " socket: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		public void sendMove(String move){			
			transmitter.logger.print_info("Sending to: " + this.getName() + "on: " + this.getTCPPort() + " move: " + move);
			viewerWriter.println(move);
			viewerWriter.println();
			transmitter.logger.print_info("Move sent");
			firstMove = false;
		}
		
		public boolean isFirstMove(){
			return firstMove;
		}
		
		public void sendPreviousMoves(){
			ArrayList<String> gameHistory= transmitter.getGameHistory();
			StringBuilder strBuilder = new StringBuilder();
			for(String move : gameHistory){
				transmitter.logger.print_info("Sending history move to: " + this.getName() + "on: " + this.getTCPPort() + " move: " + move);
				strBuilder.append(move + "\n");
			}
			viewerWriter.println(strBuilder.toString());
		}
	}
	
	public HashMap<String, Viewer> getViewerList(){
		return viewersList;
	}
	
	public void removeViewerIfExists(String viewerName){
		if (viewersList.containsKey(viewerName))
		{
			this.logger.print_info("Removing the watcher: " + viewerName);
			Viewer watcher = viewersList.get(viewerName);
			watcher.endTransmition();
			viewersList.remove(viewerName);
		}
	}
	
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
	public int getClientAlivePort() {
		return clientUdp;
	}
	
	public int getTransmitWaiterPort(){
		return clientTransmitWaiterPort;
	}

	public TransmitWaiter getTransmitWaiter(){
		return transmitWaiter;
	}
	
	public int serverUDPPort() {
		return serverUdpPort;
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	private void getServerPublicKey(){
		logger.print_info("Getting the public key of server...");
		Key serverKey = null;
		try {
			serverKey = (Key)sendMessageToServer(ClientServerProtocol.GETPUBKEY);
		} catch (IOException e) {
			logger.print_error("Cannot get the public key from server");
			e.printStackTrace();
		}
		RSAgenerator.setEncKey(serverKey);
	}
	
	private Properties getProperties(){
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("client.configurations"));
		} catch (FileNotFoundException e) {
			//file will be found
			logger.print_error("Properties file was not found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			//will be read
			logger.print_error("Problem loading file: " + e.getMessage());
			e.printStackTrace();
		}	
		return prop;
	}
	
	private void parseArguments(String[] args) {
		//From Prroperites
		Properties props = getProperties();
		
		//serverHost = (args[0]);
		serverHost = props.getProperty("SERVER_HOST");
		logger.print_info("Server: " + serverHost);
		//serverPort = Integer.parseInt(args[1]);
		serverPort = Integer.parseInt(props.getProperty("SERVER_TCP_PORT"));
		logger.print_info("Server TCP port: "+serverPort);
		//clientUdp = Integer.parseInt(args[2]);
		clientUdp = Integer.parseInt(props.getProperty("CLIENT_UDP_LISTEN_PORT"));
		logger.print_info("Client Udp Listen port: "+clientUdp);
		//clientTransmitWaiterPort = Integer.parseInt(args[3]);
		clientTransmitWaiterPort = Integer.parseInt(props.getProperty("CLIENT_TRANSMIT_WAITER_PORT"));
		logger.print_info("Client TransmitWaiter port: "+clientTransmitWaiterPort);
		//clientGamePort = Integer.parseInt(args[4]);
		clientGamePort = Integer.parseInt(props.getProperty("CLIENT_GAME_PORT"));
		logger.print_info("Client Game port: "+clientGamePort);
		
		
		//From command line
		
//		serverHost = (args[0]);
//		serverPort = Integer.parseInt(args[1]);
//		clientUdp = Integer.parseInt(args[2]);
//		clientTransmitWaiterPort = Integer.parseInt(args[3]);
//		clientGamePort = Integer.parseInt(args[4]);
	}
	
	public Object sendMessageToServer(String message) throws IOException
	{
		SSLSocket serverConnection = null;
		ClientServerProtocol parser = new ClientServerProtocol(msgType.SERVER);
		
		String[] commandPar = parseCommand(message,parser);
		if(commandPar == null){
			this.logger.print_error("Wrong message to server: " + parser.result);
			return null;
		}
		
		// send the command to the server
		try{
			serverConnection = (SSLSocket)sslsocketfactory.createSocket(serverAddress, serverPort);
			serverConnection.setEnabledCipherSuites(enabledCipherSuites);
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
		ObjectInputStream response = new ObjectInputStream(serverConnection.getInputStream());
		logger.print_info("READING socket...");
		Object resp=null;
		// get server's response
		try {
			if((resp = response.readObject()) != null) {
				logger.print_info("Server Response is:" + resp);
				
			}
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
	
	public void start() {
		//ServerListener echoServerListener = new ServerListener(this);
		
		SSLSocket serverConnection = null;
		InputStreamReader inputStream = new InputStreamReader(System.in);
		BufferedReader stdin = new BufferedReader(inputStream);
		try {
			ClientServerProtocol parser = new ClientServerProtocol(msgType.SERVER);
			
//			logger.print_info("Getting the public key of server...");
//			Key serverKey = (Key)sendMessageToServer(ClientServerProtocol.GETPUBKEY);
//			RSAgenerator.setEncKey(serverKey);
			
			while(true)
			{		
				String inputLine;			
				
				//now ask from the client a message, and send it to the server
				logger.print_info("--------------------------------------------------------------------");
				logger.print_info("Please Enter Your Message (End the message by leaving new empty line):");
				// read the command from the user
				while ((inputLine = stdin.readLine()) != null) {
					
					String[] commandPar = parseCommand(inputLine,parser);
					if(commandPar == null){
						logger.print_error(parser.result + ", please try again");
						break;
					}
					
					// send the command to the server
					try{
						serverConnection = (SSLSocket)sslsocketfactory.createSocket(serverAddress, serverPort);
						serverConnection.setEnabledCipherSuites(enabledCipherSuites);
					}
					catch (IOException ex){
						logger.print_error("Connection problems with server: "+ ex.getMessage());
						break;
					}
					
					PrintWriter out = new PrintWriter(serverConnection.getOutputStream(),true);
					//send the message
					String str  = ClientServerProtocol.buildCommand(commandPar);
					logger.print_info("Sending your message: \n"+ str +" to the server...");
					out.println(str);
					out.println();
					
					ObjectInputStream response = new ObjectInputStream(serverConnection.getInputStream());
					//ObjectInputStream responsedObj= new ObjectInputStream(serverConnection.getInputStream());
					
					logger.print_info("READING socket...");
					Object resp=null;
					// get server's response
					try {
						if((resp = response.readObject()) != null) {
							logger.print_info("Server Response is:" + resp);
							if (!parseResponse(resp)){
								logger.print_error("Bad SERVER reply...");
							}
						}
					} catch (ClassNotFoundException e) {
						// Class Will Always Be found
						e.printStackTrace();
					}
					
					if(out != null){
						out.close();
					}
					if(response!= null){
						response.close();
					}
					
					//now ask from the client a message, and send it to the server
					logger.print_info("--------------------------------------------------------------------");
					logger.print_info("Please Enter Your Message (End the message by leaving new empty line):");
				}
				
				if(serverConnection != null){
					serverConnection.close();
				}
			}
			
		}
		catch (IOException ex) {
			logger.print_error("Problem reading/sending data: " + ex.getMessage());
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

	public void stopWatching(){
		watcher = null;
	}
	
	private void startTransmitionWaiter(){
		try {
			transmitWaiterSocket = new ServerSocket(clientTransmitWaiterPort);
		} catch (IOException e) {
			this.logger.print_error("Problem open transmit waiter socket");
			e.printStackTrace();
		}
		transmitWaiter = new TransmitWaiter(transmitWaiterSocket,this);
		transmitWaiter.start();
	}
	
	private void closeTransmitions(){
		if(transmitWaiterSocket != null){
			this.getTransmitWaiter().endTransmition();
		}
		transmitWaiterSocket = null;
		transmitWaiter = null;
	}
	
	private String preparePassword(String password){
		String preparedPass = this.hashPassword(password);
		try {
			preparedPass =  RSAgenerator.encrypt(preparedPass);
		} catch (Exception e) {
			logger.print_error("Cannot encrypt the password: " + e.getMessage());
			e.printStackTrace();
		}
		return preparedPass;
	}
	
	private String[] parseCommand(String command,ClientServerProtocol parser){
		String[] params = parser.parseCommand(command);
		
		if(params == null){
			return null;
		}
		else if(params[0].equals(ClientServerProtocol.MEETME)){
			getServerPublicKey();
			clientUdp = Integer.parseInt(params[1]);
			clientName = params[2];
			password = preparePassword(params[3]);
			params[3] = password;
		}
		else if(params[0].equals(ClientServerProtocol.NEWGAME)){
			clientGamePort = Integer.parseInt(params[1]);
			clientTransmitWaiterPort = Integer.parseInt(params[2]);
		}
		else if(params[0].equals(ClientServerProtocol.PLAY)){
			clientGamePort = Integer.parseInt(params[1]);
			clientTransmitWaiterPort = Integer.parseInt(params[2]);
			gameId = params[3];
		}
		else if(params[0].equals(ClientServerProtocol.WATCH)){
			clientWatchPort = Integer.parseInt(params[1]);
		}
		else if(params[0].equals(ClientServerProtocol.SIGNUP)){
			getServerPublicKey();
			clientName = params[1];
			password = preparePassword(params[2]);
			params[2] = password;
		}
		else if(params[0].equals(ClientServerProtocol.DISCONNECT)){
			echoServerListener.die();
		}
		
		return params;
	}
	
	private String hashPassword(String pass){
		String hashed = null;
		PasswordHashManager hashManager = PasswordHashManager.getInstance();
		try {
			hashed = hashManager.encrypt(pass);
		} catch (SystemUnavailableException e) {
			this.logger.print_error("Cannot hash the password: "+ e.getMessage());
			e.printStackTrace();
			hashed = null;
		}
		return hashed;
	}
	
	
	public void handleNICETM(String [] params)
	{
		 serverUdpPort = Integer.parseInt(params[1]);
		 echoServerListener = new AliveSender(this);
		 echoServerListener.start();
	}
	
	public void HandleGameGUI(String [] params,MainFrame f)
	{
		gameId = params[1];
		logger.print_info("Received game: " + gameId + ", starting waiting on game port...");
		this.startTransmitionWaiter();
		
		game  = new GameGUI(clientName, null,gameId,f,clientGamePort, null,-1, true,this);
		((GameGUI)game).setVisible(true);
		UnhandeledReport gameReportH = ((GameGUI)game).getReportStatus();
		gameId = null;
		
		if (gameReportH!=null)
		{
			//send the report to the viewers
			makeReportToViewers(gameReportH);
			//send the report to the server
			makeReportToServer(gameReportH);
		}
		else{
			System.out.println("EMPTY REEEEEEEEEEEEEport");
		}
		this.closeTransmitions();
	}
	
//	public void HandleGame(String [] params)
//	{
//		gameId = params[1];
//		logger.print_info("Received game: " + gameId + ", starting waiting on game port...");
//		game = new GameImp(clientName, null,gameId);
//		this.startTransmitionWaiter();
//		UnhandeledReport gameReportH = game.startOnlineGame(clientGamePort, null,unDEFport,unDEFport, true,this);
//		gameId = null;
//		this.closeTransmitions();
//		if (gameReportH!=null)
//		{
//			//send the report to the viewers
//			makeReportToViewers(gameReportH);
//			//send the report to the server
//			makeReportToServer(gameReportH);
//		}
//
//	}
	
	private void makeReportToViewers(UnhandeledReport gameReportH) {
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
	
	private void makeReportToServer(UnhandeledReport gameReportH) {
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.SERVER);
		String gameReport = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEREPORT,
				gameReportH.getGameId(),
				gameReportH.getClientName(),
				gameReportH.getGameResult(),
				gameReportH.getWinner(),
				this.password});
		String[] parsed = prot.parseCommand(gameReport);
		if(parsed == null){
			logger.print_error(prot.result + ". Bad game report: "+ gameReport);
		}
		
		Object resp = null;
		try {
			resp = this.sendMessageToServer(gameReport);
			if(!this.parseResponse(resp)){
				throw new IOException("Bad server response");
			}
			String [] response = parseServerResponse((String)resp);
			if (response[0].equals(ClientServerProtocol.SERVPROB))
			{
				throw new IOException();
			}
		
		}catch (IOException e1) {
			this.logger.print_error(e1.getMessage());
			saveLocalReport(gameReportH);
		}
		
	}

	private void saveLocalReport(UnhandeledReport gameReportH) {
		//server couldn't save the file in his file system/DB , we should save it in ours
		UnhandledReports reports = null;
		try {
			reports = new UnhandledReports(clientName);
		} catch (NoReports e) {
			//igonore
			e.printStackTrace();
		} catch (FileChanged e) {
			//igonore
		}
		try {
			reports.addReport(gameReportH);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

//	public void HandleGoGoGo(String [] params)
//	{
//		opponentGamePort = Integer.parseInt(params[1]);
//		opponentGameHost = params[2];
//		opponentName = params[3];
//		opponentTransmitWaiterPort = Integer.parseInt(params[4]);
//		logger.print_info("Accepted game with: " + opponentName + 
//							" host: " + opponentGameHost + 
//							" port: " + opponentGamePort +
//							" game: " + gameId + 
//							" opponent transmit port: " + opponentTransmitWaiterPort);
//		game = new GameImp(opponentName, clientName,gameId);
//		this.startTransmitionWaiter();
//		UnhandeledReport gameReportH = game.startOnlineGame(clientGamePort, opponentGameHost,opponentGamePort,opponentTransmitWaiterPort, false,this);
//		gameId = null;
//		this.closeTransmitions();
//		if(gameReportH != null){
//			//send the report to the viewers
//			makeReportToViewers(gameReportH);
//			//send the report to the server
//			makeReportToServer(gameReportH);
//		}
//		
//	}
	
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
		game= new  GameGUI(opponentName, clientName,gameId,mainFrame,clientGamePort,opponentGameHost,opponentGamePort, false,this);
		((GameGUI)game).setVisible(true);
		UnhandeledReport gameReportH = ((GameGUI)game).getReportStatus();
		gameId = null;
		if (gameReportH!=null)
		{
			//send the report to the viewers
			makeReportToViewers(gameReportH);
			//send the report to the server
			makeReportToServer(gameReportH);
		}
		else{
			System.out.println("GAME REPORT IS NULL");
		}
		this.closeTransmitions();
	}
	
	public void HandleEnjoyWatch(String [] params)
	{
		watcher = new GameWatcher(this);
		Thread t = new Thread(watcher);
		t.start();
	}
	
	//the one that used
	public String[] parseServerResponse(String message)
	{
		ClientServerProtocol parser = new ClientServerProtocol(msgType.CLIENT);
		String[] params = parser.parseCommand(message);
		return params;
	}
	
	private boolean parseResponse(Object message){
		ClientServerProtocol parser = new ClientServerProtocol(msgType.CLIENT);
		if (!message.getClass().equals("StringClass".getClass()))
		{
			return false;
		}
		String[] params = parser.parseCommand((String)message);
		boolean responseRes = true;
		
		if(params == null){
			responseRes = false;
		}
		return responseRes;
	}
	
//	private boolean parseResponse(Object message){
//		
//		ClientServerProtocol parser = new ClientServerProtocol(msgType.CLIENT);
//		if (!message.getClass().equals("StringClass".getClass()))
//		{
//			return false;
//		}
//		String[] params = parser.parseCommand((String)message);
//		boolean responseRes = true;
//		
//		if(params == null){
//			responseRes = false;
//			return responseRes;
//		}
//		
//
//		String command = params[0];
//
//		if(command.equals(ClientServerProtocol.NICETM)){
//			handleNICETM(params);
//		}
//		else if(command.equals(ClientServerProtocol.GAME)){
//			HandleGame(params);
//		}
//		else if(command.equals(ClientServerProtocol.GOGOGO)){
//			HandleGoGoGo(params);
//		}
//		else if(command.equals(ClientServerProtocol.ENJOYWATCH)){
//			HandleEnjoyWatch(params);
//		}
//		else if(command.equals(ClientServerProtocol.NOCONN)){
//			responseRes = false;
//		}
//		else if(command.equals(ClientServerProtocol.DENIED)){
//			responseRes = false;
//		}
//		else if(command.equals(ClientServerProtocol.KNOWYA)){
//			responseRes = false;
//		}
//		else if(command.equals(ClientServerProtocol.WHAT)){
//			responseRes = false;
//		}
//		else if(command.equals(ClientServerProtocol.OK)){
//			responseRes = true;
//		}
//		
//		return responseRes;
//	}
	
	public static void main(String[] args) {
		TheClient client;
		try {
			new MainFrame(args);
//			client = new TheClient(args);
//			client.start();
		} catch (IOException e) {
			System.out.println(LogPrinter.error_msg("Client had failed!"));
		}

	}

	public void disconnect()
	{
		String disconnectStr = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.DISCONNECT,
				clientName});
		logger.print_info("Send disconnection to server: " + disconnectStr);
		Object resp = null;
		try{
			resp = this.sendMessageToServer(disconnectStr);
			if(!parseResponse(resp)){
				throw new IOException("Bad server response");
			}
		}
		catch (IOException ex)
		{
			logger.print_error(ex.getMessage());
			//never mind , it will remove us because of the udp listener
		}
		while (true)
		{
			try{
				echoServerListener.interrupt();
				break;
			}
			catch (SecurityException ex)
			{
				//currently sending the message
			}
		}
			
	}
	public boolean reportUnhandeledReports() throws FileChanged, IOException {
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
			ArrayList<String> response=(ArrayList<String>)sendMessageToServer(gameReports);
			if (response==null)
			{
				throw new IOException("Bad server response");
			}
			for (String unhandeledReport : response) {
				reports.removeReport(unhandeledReport);
			}
		}
		else{
			return false;
		}
		return true;
		
	}
	
	
}
