package ConnectFourClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;

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
	
	private boolean noInternetConnection;
	
	static final private int delayTime = 20;
	//this will wait some time
	static private Timer delayTimer;
	
	static private Boolean isAlive = true;

	public AliveSender(TheClient client) {
		this.client = client;
		delayTimer = new Timer(delayTime,this);
		noInternetConnection = false;
	}

	synchronized public boolean noInternetConnection(){
		client.logger.print_info("Internet no connection: " + noInternetConnection);
		return noInternetConnection;
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
		//send to server client Alive message!
		try {
			noInternetConnection = false;
			String aliveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.IMALIVE,
																			client.getClientName(), 
																			Integer.toString(client.getTransmitWaiterPort()),
																			client.getGameId(),
																			Integer.toString(client.getGamePort())});
		
			byte[] buffer = aliveMsg.getBytes();
			
			client.logger.print_info("I say: " + aliveMsg + " to port: " + client.serverUDPPort());
			client.aliveSocket.send(new DatagramPacket(buffer, buffer.length,
					client.getServerAddress(), client.serverUDPPort()));
		} catch (Exception e) {
			client.logger.print_error("Problems sening alive message to the server: " + e.getMessage());
		}
		//now check if there any connection to internet
		try{
			HttpURLConnection c = (HttpURLConnection)(new  URL("http://www.google.com")).openConnection();
			int resCode = c.getResponseCode(); 
			if(resCode != 200){
				noInternetConnection = true;
			}
		}
		catch(IOException e){
			client.logger.print_error("Problem echoing GOOGLE: " + e.getMessage());
			noInternetConnection = true;
		}
		finally{
			client.logger.print_info("google is reachable: "+!noInternetConnection);
			delayTimer.restart();
		}
		//isAlive.notify();
	}

}
