package ConnectFourServer;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.*;

public class MainServer {

	// the connection pool of the server. each incoming
	// connection is handled by this thread pool
	private ExecutorService connectionsPool;

	// the port which the server listens to 
	private int serverTCPPort;
	
	// the port that the server sends/receives alive UDP messages to 
	private int serverUDPPort;

	// The server's log
	private LogPrinter printer;

	private DatagramSocket serverUdp;

	//Data structure which manages the OnlineGames
	public OnlineGames games;
	
	//Data structure which manages the OnlineClients
	public OnlineClients clients;

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

	public MainServer(String[] args) {
		parseServerArguments(args);
		connectionsPool = Executors.newCachedThreadPool();
		printer = new LogPrinter();
		this.printLog("TCP: "+serverTCPPort+"\nUDP: "+serverUDPPort);
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

	public void printLog(String logPrint) {
		printer.print(logPrint);
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
	}

	/**
	 * This method runs when we want to start the server, listen to incoming
	 * connections
	 */
	public void start() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(serverTCPPort);
		} catch (IOException e) {
			// TODO Handle serverSocket initialization problem
			e.printStackTrace();
			return;
		}
		
		//start the UDP listener
		Thread t = new Thread(new UdpListener(this));
		t.start();
		
		
		while (true) {

			// Here the server accepts new connections,
			// TODO: we need to build another state when the server checks his
			// open Games data structure
			// and acts according to the algorithms of the game (checking that
			// the client is still connection and etc..)
			try {
				connectionsPool.execute(new RequestHandler(serverSocket
						.accept(), this));
			} catch (IOException e) {
				// TODO problem inserting client connection
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		MainServer server = new MainServer(args);
		server.start();

	}

	public DatagramSocket getUdpSocket() {
		return serverUdp;
	}

}
