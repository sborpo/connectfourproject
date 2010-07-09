package ConnectFourServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.security.Key;
import java.security.KeyPair;
import java.security.spec.RSAPublicKeySpec;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.*;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import common.LogPrinter;
import common.PasswordHashManager;
import common.RSAgenerator;
import common.PasswordHashManager.SystemUnavailableException;

public class MainServer {
	
	final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
	
	public String ReportFileName="server";

	// the connection pool of the server. each incoming
	// connection is handled by this thread pool
	private ExecutorService connectionsPool;

	// the port which the server listens to 
	private int serverTCPPort;
	
	// the port that the server sends/receives alive UDP messages to 
	private int serverUDPPort;

	// The server's log
	public LogPrinter printer;

	private DatagramSocket serverUdp;

	//Data structure which manages the OnlineGames
	public OnlineGames games;
	
	//Data structure which manages the OnlineClients
	public OnlineClients clients;

	//The udpListener
	public UdpListener udpListener = null;
	
	public int getServerTCPPort() {
		return serverTCPPort;
	}
	
	public int getServerUDPPort() {
		return serverUDPPort;
	}

//	public void addConnectedClient(String name, InetAddress ipAddress,
//			int udpPort) {
//		ConnectedClient c = new ConnectedClient(ipAddress, udpPort, this);
//		connectedClients.put(name, c);
//		c.start();
//	}

	public MainServer(String[] args) throws SQLException, IOException {
		try {
			printer = new LogPrinter("Server");
		} catch (IOException e) {
			System.out.println(LogPrinter.error_msg("Cannot open LOG printer: " + e.getMessage()));
			throw e;
		}
		parseServerArguments(args);
		this.printer.print_info("TCP: "+serverTCPPort+" UDP: "
								+serverUDPPort+
								" dbName: "+DataBaseManager.getDBname()+
								" PubKey: "+RSAgenerator.getPubKey().toString());
		initDatabase();
		connectionsPool = Executors.newCachedThreadPool();
		games = new OnlineGames(this);
		clients = new OnlineClients(this);
		//connectedClients = new HashMap<String, ConnectedClient>();
		try {
			serverUdp = new DatagramSocket(getServerUDPPort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void initDatabase() throws SQLException
	{
		this.printer.print_info("Loading the database...");
		DataBaseManager.createDB("database");
		DataBaseManager.constructTables();
		this.printer.print_info("Done loading the database");
	}
	
	public boolean authUser(String clientName, String password){
		boolean result = false;
		try{
			if(DataBaseManager.authenticateUser(clientName, password)){
				result = true;
			}
		} catch (SQLException e) {
			printer.print_error("Database error: " + e.getMessage());
		} catch (Exception e){
			printer.print_error("Cannot decrypt password for authentication : " + e.getMessage());
		}
		return result;
	}
	
	/**
	 * The method parses the given arguments and initializes the fields of the
	 * server
	 * 
	 * @param args
	 */
	private void parseServerArguments(String[] args) {
		serverTCPPort = Integer.parseInt(args[0]);
		serverUDPPort = Integer.parseInt(args[1]);
		DataBaseManager.initDBname(args[2]);
	}

	/**
	 * This method runs when we want to start the server, listen to incoming
	 * connections
	 */
	public void start() {
		SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket serverSocket = null;
		try {
			serverSocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(serverTCPPort);
			
			serverSocket.setEnabledCipherSuites(enabledCipherSuites);
		} catch (IOException e) {
			// TODO Handle serverSocket initialization problem
			e.printStackTrace();
			return;
		}
		
		//start the UDP listener
		udpListener = new UdpListener(this);
		udpListener.start();
		
		
		while (true) {

			// Here the server accepts new connections,
			// TODO: we need to build another state when the server checks his
			// open Games data structure
			// and acts according to the algorithms of the game (checking that
			// the client is still connection and etc..)
			try {
				connectionsPool.execute(new RequestHandler((SSLSocket)serverSocket
						.accept(), this));
			} catch (IOException e) {
				// TODO problem inserting client connection
				e.printStackTrace();
			}
		}

	}

	public UdpListener getUdpListener(){
		return udpListener;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		MainServer server=null;
		try {
			RSAgenerator.generatePair();
			server = new MainServer(args);
			server.start();
		} catch (Exception e) {
			System.out.println(LogPrinter.error_msg("Server failure: " + e.getMessage()));
			e.printStackTrace();
		} 
	}

	public DatagramSocket getUdpSocket() {
		return serverUdp;
	}

}
