package ConnectFourServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAPublicKeySpec;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import theProtocol.ClientServerProtocol;

import common.LogPrinter;
import common.PasswordHashManager;
import common.RSAgenerator;
import common.PasswordHashManager.SystemUnavailableException;

/**
 * This class represents the server of the game , which
 * every client must connect to in order to search , create ,view and join
 * online games
 *
 */
public class MainServer {
	
	final String[] enabledCipherSuites = { "SSL_DH_anon_WITH_RC4_128_MD5" };
	
	//The unhandeled report file name
	public static String ReportFileName="server";

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
	
	/**
	 * Returns the server TCP port
	 * @return
	 */
	public int getServerTCPPort() {
		return serverTCPPort;
	}
	
	/**
	 * Returns the server UDP port
	 * @return
	 */
	public int getServerUDPPort() {
		return serverUDPPort;
	}

//	public void addConnectedClient(String name, InetAddress ipAddress,
//			int udpPort) {
//		ConnectedClient c = new ConnectedClient(ipAddress, udpPort, this);
//		connectedClients.put(name, c);
//		c.start();
//	}

	/**
	 * The servers constructor , which is initialized with the command line arguemnts (or using a 
	 * properties file). 
	 * It uses a thread pool to manage clients requests.
	 */
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
			printer.print_error("Problem initializing UDP socket: "+ e.getMessage());
		}

	}
	
	/**
	 * This method initiates the database , in case it is not initiated yet.
	 * @throws SQLException
	 */
	private void initDatabase() throws SQLException
	{
		this.printer.print_info("Loading the database...");
		DataBaseManager.createDB("database");
		DataBaseManager.constructTables();
		this.printer.print_info("Done loading the database");
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
			printer.print_error("Problem initializing server socket: " + e.getMessage());
			return;
		}
		
		//start the UDP listener
		udpListener = new UdpListener(this);
		udpListener.start();
		
		
		while (true) {

			try {
				SSLSocket sock= (SSLSocket)serverSocket.accept();
				sock.setSoTimeout(ClientServerProtocol.timeout);
				connectionsPool.execute(new RequestHandler(sock, this));
			} catch (IOException e) {
				printer.print_error("Probem while accepting socket connection: "+ e.getMessage());
			}
		}

	}

	/**
	 * Returns the udp listener , an object that manages the Alive messages protocol
	 * @return
	 */
	public UdpListener getUdpListener(){
		return udpListener;
	}
	
	/**
	 * Here we start the server
	 */
	public static void main(String[] args) {

		MainServer server=null;
		try {
			RSAgenerator.generatePair();
			server = new MainServer(args);
			server.start();
		} catch (Exception e) {
			System.out.println(LogPrinter.error_msg("Server failure: " + e.getMessage()));
		} 
	}

	public DatagramSocket getUdpSocket() {
		return serverUdp;
	}

}
