package ConnectFourServer;


import gameManager.Game;
import gameManager.GameImp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import common.OnlineClient;
import common.Timer;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

public class UdpListener implements Runnable {

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
		timeOut = 140;
		theThread = new Thread(this);
	}

	public void start()
	{
		theThread.start();
	}
	
	@Override
	public void run() {
		//now do it infinitely
		server.printer.print_info("Starting listening to alive messages");
//		Timer waitingTime = new Timer(timeOut);
//		waitingTime.start();
		while (true) {
			ArrayList<String> clientsList=null;
			//get the clients
			
//			if(waitingTime.isTimedOut()){
//				//if there are no clients
//				if (clientsList.size()==0)
//				{
//					server.printLog("No clients yet, waiting...");
//					waitingTime.reset();
//					continue;
//				}
//			}
//			else if (clientsList.size()==0){
//				continue;
//			}
//			server.printLog("UNSET ALL USERS ALIVENESS");
//			server.clients.resetIsAliveMap();
//			resetAllTimers();

			//now wait for answers
			byte[] alv = new byte[100000];
			DatagramPacket aliveMsg = new DatagramPacket(alv, alv.length);
			
			server.printer.print_info("UDP:Waiting for clients alive messages...");
			//int clientNum = clientsList.size();
			//while (clientNum > 0) {
				try {
					socket.setSoTimeout(this.timeOut*1000);
					socket.receive(aliveMsg);
					int dataLen = aliveMsg.getLength();
					byte[] alvFromatted = new byte[dataLen];
					System.arraycopy(alv, 0, alvFromatted, 0, dataLen);
					
					String aliveResponse = new String(alvFromatted);
					
					ClientServerProtocol prot= new ClientServerProtocol(msgType.SERVER);
					String[] theResponse = prot.parseCommand(aliveResponse);
					
					if(theResponse == null){
						server.printer.print_error(prot.result + ". Bad alive message");
						break;
					}
					
					String clientMessage = theResponse[0];
					String clientName = theResponse[1];
					server.printer.print_info("\n----------------------------\nUDP recieved From Client: "+clientName
									+ ", "+aliveMsg.getAddress().toString()
									+ " The message (len: " + dataLen + ") is:\n----------------------------\n"
									+ clientMessage);
					
					//set the client alive
					if(!server.clients.setAlive(clientName)){
						server.printer.print_info("Client with name: "+ clientName + " doesn't exists, checking...");
						addAliveClient(theResponse,aliveMsg);
						continue;
					}
					
					Timer clientTimer = this.clientTimers.get(clientName);
					clientTimer.reset();
					
					checkTimers();
					server.clients.removeIfNotAlive();
					
					//clientNum--;
				} 
				catch (SocketTimeoutException e) {
					server.printer.print_info("Socket timeout...");
					clientsList = server.clients.getClients();
					if (clientsList.size()==0)
					{
						server.printer.print_info("No clients yet, waiting...");
						//waitingTime.reset();
						continue;
					}
					checkTimers();
					server.clients.removeIfNotAlive();
				}
				catch (IOException e) {
					server.printer.print_error(e.getMessage());
					e.printStackTrace();
				}
			//}
			//waitingTime.reset();
		}

	}
	
	private void addAliveClient(String[] message,DatagramPacket packet){
			String clientName = message[1];
			String password = message[2];
			int transmitPort = Integer.parseInt(message[3]);
			String gameId   = message[4];
			int tcpPort = Integer.parseInt(message[5]);
			if(server.authUser(clientName, password)){
				OnlineClient theClient = new OnlineClient(packet.getAddress(), packet.getPort(), clientName, tcpPort, transmitPort);
				server.clients.addClientToUdpList(theClient);
				if(!gameId.equalsIgnoreCase(ClientServerProtocol.noGame)){
					Game theGame = server.games.getGame(gameId);
					if(theGame == null){
						server.printer.print_info("Creating new game: " + gameId);
						theGame = new GameImp(clientName, null, gameId);
						server.games.addGame(theGame);
						theClient.setGameForClient(gameId);
					}
					else{
						theGame.addPlayer(clientName);
					}
					server.printer.print_info("Adding client: " + clientName);
				}
				openTimerFor(clientName);
				server.printer.print_info("Authentication had succeded for: " + clientName);
			}
			else{
				server.printer.print_error("Authentication had failed for: " + clientName);
			}
	}
	
	@SuppressWarnings("unchecked")
	private void checkTimers(){
		Timer timer= null;
		HashMap<String,Timer> clone = (HashMap<String, Timer>) clientTimers.clone();
		for(String name : clone.keySet()){
			timer = clientTimers.get(name);
			if(timer.isTimedOut()){
				server.printer.print_error("Client "+ name+" not responding...");
				server.clients.resetAlive(name);
				timer.delete();
				clientTimers.remove(name);
			}
			else{
				server.clients.setAlive(name);
			}
		}
	}
	
	private void resetAllTimers(){
		Timer timer= null;
		for(String name : clientTimers.keySet()){
			timer = clientTimers.get(name);
			timer.reset();
		}
	}
	
	public void openTimerFor(String clientName){
		Timer timer = null;
		if(clientTimers.containsKey(clientName)){
			timer = clientTimers.get(clientName);
			timer.reset();
		}
		else{
			timer = new Timer(this.timeOut);
			timer.start();
			clientTimers.put(clientName, timer);
		}
		server.clients.setAlive(clientName);
	}

}
