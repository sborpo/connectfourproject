package ConnectFourClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import common.Timer;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

/**
 * Listens To UDP Alive Messages
 * 
 */
public class AliveSender extends Thread {

	// the client to which the listener is bind to
	private TheClient client;
	
	private int delayTime = 100;
	//this will wait some time
	private Timer delayTimer;

	public AliveSender(TheClient client) {
		this.client = client;
		delayTimer = new Timer(delayTime);
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
		while (true) {
			if(!delayTimer.isTimedOut()){
				continue;
			}
			delayTimer.reset();
			
			//send to server client Alive message!
			String aliveMsg = ClientServerProtocol.IMALIVE + " " + client.getClientName() + 
			" " + client.getPassword() + " " + client.getTransmitPort()+ " " + client.getGameId() + " " + client.getGamePort();
			client.logger.print_info("I say: " + aliveMsg + " to port: " + client.serverUDPPort());
			byte[] buffer = aliveMsg.getBytes();
			try {
				client.aliveSocket.send(new DatagramPacket(buffer, buffer.length,
						client.getServerAddress(), client.serverUDPPort()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
