package ConnectFourClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

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
	
	static final private int delayTime = 100;
	//this will wait some time
	static private Timer delayTimer;
	
	static private Boolean isAlive = true;

	public AliveSender(TheClient client) {
		this.client = client;
		delayTimer = new Timer(delayTime,this);
	}

//	public synchronized void die(){
//		isAlive = false;
//	}
	
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
//		while (isAlive) {
//			try {
//				synchronized(isAlive){
//					System.out.println("WAITING.....");
//					isAlive.wait();
//					System.out.println("NOTIFIED....");
//				}
//			} catch (InterruptedException e) {
//				//asked to close this thread
//				return;
//			}
			
		//}

	}

	@Override
	synchronized public void timeOutReceived(TimeOutEvent event) {
		//send to server client Alive message!
		String aliveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.IMALIVE,
																			client.getClientName(), 
																			client.getPassword(),
																			Integer.toString(client.getTransmitWaiterPort()),
																			client.getGameId(),
																			Integer.toString(client.getGamePort())});
		client.logger.print_info("I say: " + aliveMsg + " to port: " + client.serverUDPPort());
		byte[] buffer = aliveMsg.getBytes();
		try {
			client.aliveSocket.send(new DatagramPacket(buffer, buffer.length,
					client.getServerAddress(), client.serverUDPPort()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		delayTimer.reset();
		//isAlive.notify();
	}

}
