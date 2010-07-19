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

public class UdpListener implements Runnable,TimerListener {

	//The thread that should run the udpListener
	private Thread theThread;
	private int timeOut;
	private MainServer server;
	private DatagramSocket socket;
	private HashMap<String,Timer> clientTimers;

	public UdpListener(MainServer server) {
		this.server = server;
		socket = server.getUdpSocket();
		clientTimers = new HashMap<String,Timer>();
		timeOut = 60;
		theThread = new Thread(this);
	}

	public void start()
	{
		theThread.start();
	}
	
	public void removeClient(String clientName){
		server.clients.resetAlive(clientName);
		Timer timer = null;
		if(clientTimers.containsKey(clientName)){
			timer = clientTimers.get(clientName);
			timer.stop();
		}
		clientTimers.remove(clientName);
	}
	
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
					
					System.out.println("Checking the players...");
					checkTimers();
					
				} 

				catch (IOException e) {
					server.printer.print_error(e.getMessage());
				}
		}
	}
	
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
	
//	private void resetAllTimers(){
//		Timer timer= null;
//		for(String name : clientTimers.keySet()){
//			timer = clientTimers.get(name);
//			timer.reset();
//		}
//	}
	
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

	@Override
	public void timeOutReceived(TimeOutEvent event) {
		server.printer.print_error("Client timeout received");
		checkTimers();
	}

}
