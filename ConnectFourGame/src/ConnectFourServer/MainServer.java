package ConnectFourServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;


public class MainServer {

	//the connection pool of the server. each incoming
	//connection is handled by this thread pool
	private ExecutorService connectionsPool;
	
	//the port which the server listens to
	private int serverPort;
	
	//The server's log
	private LogPrinter printer;
	
	
	public MainServer(String [] args)
	{
		parseServerArguments(args);
		connectionsPool= Executors.newCachedThreadPool();
		
	}
	
	public void printLog(String logPrint)
	{
		printer.print(logPrint);
	}
	
	/**
	 * The method parses the given arguments and initializes
	 * the fields of the server
	 * @param args
	 */
	private void parseServerArguments(String[] args) {
		serverPort=Integer.parseInt(args[1]);
		
	}
	
	/**
	 * This method runs when we want to start the server,
	 * listen to incoming connections
	 */
	public void start()
	{
		ServerSocket serverSocket=null;
		try {
		 serverSocket= new ServerSocket(serverPort);
		} catch (IOException e) {
			// TODO Handle serverSocket initialization problem
			e.printStackTrace();
			return;
		}
		while (true)
		{
			
			//Here the server accepts new connections,
			//TODO: we need to build another state when the server checks his open Games data structure
			//and acts according to the algorithms of the game (checking that the client is still connection and ect') 
			try {
				connectionsPool.execute(new RequestHandler(serverSocket.accept(),this));
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
	

}
