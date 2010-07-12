package ConnectFourClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import ConnectFourClient.TheClient.ServerWriteOrReadException;

import common.Timer;
import common.Timer.TimeOutEvent;
import common.Timer.TimerListener;

import theProtocol.ClientServerProtocol;

/**
 * Listens To UDP Alive Messages
 * 
 */
public class AliveSender extends Thread implements TimerListener{

	// the client to which the listener is bind to
	private TheClient client;
	
	private boolean noServerConnection;
	
	static final private int delayTime = 20;
	//this will wait some time
	static private Timer delayTimer;
	
	static private Boolean isAlive = true;

	public AliveSender(TheClient client) {
		this.client = client;
		delayTimer = new Timer(delayTime,this);
		noServerConnection = false;
	}

	synchronized public boolean noServerConnection(){
		return noServerConnection;
	}
	
	public void run() {
		
		try {
			client.aliveSocket = new DatagramSocket(client.getClientAlivePort());
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// open a UDP socket , from which we will do the communications
		// with the server
		client.logger.print_info("Client starting sending alive messages from: "
				+client.getClientAlivePort() + " to: "+ client.serverUDPPort());
		delayTimer.start();
	}

	@Override
	synchronized public void timeOutReceived(TimeOutEvent event) {
		noServerConnection = false;
		//send to server client Alive message!
		try {
			client.getServerPublicKey();
			noServerConnection = false;
			String preparedPass = client.preparePassword(client.getPassword());
			String aliveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.IMALIVE,
																			client.getClientName(), 
																			preparedPass,
																			Integer.toString(client.getTransmitWaiterPort()),
																			client.getGameId(),
																			Integer.toString(client.getGamePort())});
		
			byte[] buffer = aliveMsg.getBytes();
			
				client.logger.print_info("I say: " + aliveMsg + " to port: " + client.serverUDPPort());
				client.aliveSocket.send(new DatagramPacket(buffer, buffer.length,
						client.getServerAddress(), client.serverUDPPort()));
		} catch (Exception e) {
			client.logger.print_error("Problems sening alive message to the server: " + e.getMessage());
			noServerConnection = true;
		}
		finally{
			delayTimer.restart();
		}
		//isAlive.notify();
	}

}
