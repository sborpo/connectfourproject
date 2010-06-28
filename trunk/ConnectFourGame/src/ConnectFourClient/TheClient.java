package ConnectFourClient;

import gameManager.Game;

import java.io.BufferedReader;
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
import java.util.Properties;

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
	
	private void parseArguments(String[] args) {
//		Properties props = new Properties();
//		try {
//			props.load(new FileInputStream("src\\ConnectFourClient\\client.configurations"));
//		} catch (FileNotFoundException e) {
//			//file will be found
//			
//			e.printStackTrace();
//		} catch (IOException e) {
//			//will be read
//			e.printStackTrace();
//		}	
		
		
		//serverHost = props.getProperty("SERVER_HOST");
		serverHost = (args[0]);
		logger.print_info("Server: " + serverHost);
		//serverPort = Integer.parseInt(props.getProperty("SERVER_TCP_PORT"));
		serverPort = Integer.parseInt(args[1]);
		logger.print_info("Server TCP port: "+serverPort);
		//clientUdp = Integer.parseInt(props.getProperty("CLIENT_UDP_LISTEN_PORT"));
		clientUdp = Integer.parseInt(args[2]);
		logger.print_info("Client Udp Listen port: "+clientUdp);
		//clientTransmitPort = Integer.parseInt(props.getProperty("CLIENT_TRANSMIT_PORT"));
		clientTransmitWaiterPort = Integer.parseInt(args[3]);
		logger.print_info("Client TransmitWaiter port: "+clientTransmitWaiterPort);
		clientGamePort = Integer.parseInt(args[4]);
		logger.print_info("Client Game port: "+clientGamePort);
		clientWatchPort = Integer.parseInt(args[4]);
		//clientWatchPort = Integer.parseInt(args[5]);
		//logger.print_info("Client Watch port: "+clientWatchPort);
	}
	
	public Object sendMessageToServer(String message) throws IOException
	{
		Socket serverConnection = null;
		ClientServerProtocol parser = new ClientServerProtocol(msgType.SERVER);
		
		String[] commandPar = parseCommand(message,parser);
		if(commandPar == null){
			this.logger.print_error("Wrong message to server");
			return null;
		}
		
		// send the command to the server
		try{
			serverConnection = new Socket(serverAddress, serverPort);
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
		
		Socket serverConnection = null;
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		try {
			ClientServerProtocol parser = new ClientServerProtocol(msgType.SERVER);
			
			logger.print_info("Getting the public key of server...");
			Key serverKey = (Key)sendMessageToServer(ClientServerProtocol.GETPUBKEY);
			RSAgenerator.setEncKey(serverKey);
			
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
						serverConnection = new Socket(serverAddress, serverPort);
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
	
	private String[] parseCommand(String command,ClientServerProtocol parser){
		String[] params = parser.parseCommand(command);
		
		if(params == null){
			return null;
		}
		else if(params[0].equals(ClientServerProtocol.MEETME)){
			getServerPublicKey();
			clientUdp = Integer.parseInt(params[1]);
			clientName = params[2];
			password = this.hashPassword(params[3]);
			try {
				password =  RSAgenerator.encrypt(password);
				System.out.println("Encrypted: " + password);
				params[3] = password;
			} catch (Exception e) {
				logger.print_error("Cannot encrypt the password: " + e.getMessage());
				e.printStackTrace();
			}
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
			password = this.hashPassword(params[2]);
			try {
				password =  RSAgenerator.encrypt(password);
				params[2] = password;
			} catch (Exception e) {
				logger.print_error("Cannot encrypt the password: " + e.getMessage());
				e.printStackTrace();
			}
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
	
	public void HandleGame(String [] params)
	{
		gameId = params[1];
		logger.print_info("Received game: " + gameId + ", starting waiting on game port...");
		game = new Game(clientName, null,gameId);
		this.startTransmitionWaiter();
		UnhandeledReport gameReportH = game.startOnlineGame(clientGamePort, null,-1, true,this);
		gameId = null;
		this.closeTransmitions();
		//send the report to the server
		makeReportToServer(gameReportH);

	}
	
	private void makeReportToServer(UnhandeledReport gameReportH) {
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		String gameReport = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEREPORT,
				gameReportH.getGameId(),
				gameReportH.getClientName(),
				gameReportH.getGameResult(),
				gameReportH.getWinner()});
		logger.print_info("Send report to server: " + gameReport);
		Object resp = null;
		try {
			resp = this.sendMessageToServer(gameReport);
		if(!this.parseResponse(resp)){
			throw new IOException("Bad server response");
		}
		String [] response = parseServerResponse((String)resp);
		if (response[0].equals(ClientServerProtocol.DBERRORREPSAVED))
		{
			throw new IOException();
		}
		
		}catch (IOException e1) {
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

	public void HandleGoGoGo(String [] params)
	{
		opponentGamePort = Integer.parseInt(params[1]);
		opponentGameHost = params[2];
		opponentName = params[3];
		logger.print_info("Accepted game with: " + opponentName + 
							" host: " + opponentGameHost + 
							" port: " + opponentGamePort +
							" game: " + gameId);
		game = new Game(opponentName, clientName,gameId);
		this.startTransmitionWaiter();
		UnhandeledReport gameReportH = game.startOnlineGame(clientGamePort, opponentGameHost,opponentGamePort, false,this);
		gameId = null;
		this.closeTransmitions();
		//send the report to the server
		makeReportToServer(gameReportH);
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
			return responseRes;
		}
		

		String command = params[0];

		if(command.equals(ClientServerProtocol.NICETM)){
			handleNICETM(params);
		}
		else if(command.equals(ClientServerProtocol.GAME)){
			HandleGame(params);
		}
		else if(command.equals(ClientServerProtocol.GOGOGO)){
			HandleGoGoGo(params);
		}
		else if(command.equals(ClientServerProtocol.ENJOYWATCH)){
			HandleEnjoyWatch(params);
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
		else if(command.equals(ClientServerProtocol.WHAT)){
			responseRes = false;
		}
		else if(command.equals(ClientServerProtocol.OK)){
			responseRes = true;
		}
		
		return responseRes;
	}
	
	public static void main(String[] args) {
		TheClient client;
		try {
			//new MainFrame();
			client = new TheClient(args);
			client.start();
		} catch (IOException e) {
			System.out.println(LogPrinter.error_msg("Client had failed!"));
		}

	}
}
