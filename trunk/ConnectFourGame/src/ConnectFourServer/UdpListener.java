package ConnectFourServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

import ConnectFourClient.TheClient;

import common.OnlineClient;
import common.Timer;
import common.Timer.TimeOutEvent;
import common.Timer.TimerListener;

import gameManager.Game;
import gameManager.GameGUI;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

/**
 * This class handles the UDP listening for alive messages from the current
 * online users
 *
 */
public class UdpListener implements Runnable,TimerListener {

	//The thread that should run the udpListener
	private Thread theThread;
	//time out for alive message, during this time , everyone should send alive message
	private int timeOut;
	//the main server
	private MainServer server;
	//the socket through which we listening to the alive messages
	private DatagramSocket socket;
	//a hash map of clients timers
	private HashMap<String,Timer> clientTimers;

	public UdpListener(MainServer server) {
		this.server = server;
		socket = server.getUdpSocket();
		clientTimers = new HashMap<String,Timer>();
		timeOut = 60;
		theThread = new Thread(this);
	}

	/**
	 * starts a new UDP listener in a new thread
	 */
	public void start()
	{
		theThread.start();
	}
	
	/**
	 * Removes the given client name from the udp list , now we will not
	 * wait for his Alive message
	 * @param clientName
	 */
	public void removeClient(String clientName){
		server.clients.resetAlive(clientName);
		Timer timer = null;
		if(clientTimers.containsKey(clientName)){
			timer = clientTimers.get(clientName);
			timer.stop();
		}
		clientTimers.remove(clientName);
	}
	
	/**
	 * This is the UDP listener method , which runs in a thread infinitely
	 *  until it closed. It resets the timers of the clients from which it
	 * recieved alive messages.  
	 */
	@Override
	public void run() {
		//now do it infinitely
		server.printer.print_info("Starting listening to alive messages");
		while (true) {
			byte[] alv = new byte[100000];
			DatagramPacket aliveMsg = new DatagramPacket(alv, alv.length);
			
			server.printer.print_info("UDP:Waiting for clients alive messages...");

				try {
					socket.receive(aliveMsg);
					int dataLen = aliveMsg.getLength();
					byte[] alvFromatted = new byte[dataLen];
					System.arraycopy(alv, 0, alvFromatted, 0, dataLen);
					
					String aliveMessage = new String(alvFromatted);
					
					ClientServerProtocol prot= new ClientServerProtocol(msgType.SERVER);
					String[] aliveMessageArr = prot.parseCommand(aliveMessage);
					
					if(aliveMessageArr == null){
						server.printer.print_error(prot.result + ". Bad alive message");
						break;
					}
					
					String clientName = aliveMessageArr[1];
					server.printer.print_info("\n----------------------------\nUDP recieved From Client: "+clientName
									+ ", "+aliveMsg.getAddress().toString()
									+ " The message (len: " + dataLen + ") is:\n----------------------------\n"
									+ aliveMessage);
					
					//set the client alive
					if(!server.clients.setAlive(clientName)){
						server.printer.print_info("Client with name: "+ clientName + " doesn't exists, checking...");
						addAliveClient(aliveMessageArr,aliveMsg);
						continue;
					}
					
					Timer clientTimer = this.clientTimers.get(clientName);
					clientTimer.reset();
					
					server.printer.print_info("Checking the players...");
					checkTimers();
					
				} 

				catch (IOException e) {
					server.printer.print_error(e.getMessage());
				}
		}
	}
	
	/**
	 * This method adds a new client to the udp clients and initializes it's timer
	 * @param message
	 * @param packet
	 */
	private void addAliveClient(String[] message,DatagramPacket packet){
			String clientName = message[1];
			int transmitPort = Integer.parseInt(message[2]);
			String gameId   = message[3];
			int tcpPort = Integer.parseInt(message[4]);
			OnlineClient theClient = new OnlineClient(packet.getAddress(), packet.getPort(), clientName, tcpPort, transmitPort);
			server.clients.addClientToUdpList(theClient);
			if(gameId != null && !gameId.equalsIgnoreCase(ClientServerProtocol.noGame)){
				Game theGame = server.games.getGame(gameId);
				if(theGame == null){
					server.printer.print_info("Creating new game: " + gameId);
					theGame = new GameGUI(clientName, null, gameId, null, tcpPort, null, TheClient.unDEFport, false, null,TheClient.unDEFport);
					server.games.addGame(theGame);
					theClient.setGameForClient(gameId);
				}
				else{
					theGame.addPlayer(clientName);
				}
				server.printer.print_info("Adding client: " + clientName);
			}
			openTimerFor(clientName);
			server.printer.print_info("User has been successfully added: " + clientName);
	}
	
	/**
	 * This method checks the current timers of all online clients , if sime of the timers
	 * timed - out so it will remove the client from the online clients .
	 */
	@SuppressWarnings("unchecked")
	synchronized private void checkTimers(){
		Timer timer= null;
		HashMap<String,Timer> clone = (HashMap<String, Timer>) clientTimers.clone();
		for(String name : clone.keySet()){
			timer = clientTimers.get(name);
			if(timer.isTimedOut()){
				server.printer.print_error("Client "+ name+" not responding...");
				server.clients.resetAlive(name);
				timer.stop();
				clientTimers.remove(name);
			}
			else{
				server.clients.setAlive(name);
			}
		}
		server.clients.removeIfNotAlive();
	}
	
	
	/**
	 * This method opens a new timer of the given client
	 * @param clientName
	 */
	public void openTimerFor(String clientName){
		Timer timer = null;
		if(clientTimers.containsKey(clientName)){
			timer = clientTimers.get(clientName);
			timer.reset();
		}
		else{
			timer = new Timer(this.timeOut,this);
			timer.start();
			clientTimers.put(clientName, timer);
		}
		server.clients.setAlive(clientName);
	}

	/**
	 * This is a method that is run when a timeout is recieved
	 */
	@Override
	public void timeOutReceived(TimeOutEvent event) {
		server.printer.print_error("Client timeout received");
		checkTimers();
	}

}
