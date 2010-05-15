package ConnectFourClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Listens To UDP Alive Messages
 * 
 */
public class ServerListener extends Thread {

	// the client to which the listener is bind to
	private TheClient client;

	public ServerListener(TheClient client) {
		this.client = client;
	}

	public void run() {
		DatagramSocket socket = null;
		//wait till the client gets from server udp port
		while(client.serverUDPPort() <= 0){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// open a UDP socket , from which we will do the communications
		// with the server
		System.out.println("Client starting listening to udp alive messages on: "+client.listenToServerPort());
		try {
			socket = new DatagramSocket(client.listenToServerPort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte[] message = new byte[100000];
		while (true) {
			//wait to an echo message from the server
			DatagramPacket mes = new DatagramPacket(message, message.length);
			try {
				socket.receive(mes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//copy the message to a buffer , in order to print later
			byte[] serverMessage = new byte[mes.getLength()];
			for (int i = 0; i < serverMessage.length; i++) {
				serverMessage[i] = message[i];
			}
			//print the received message from the server
			String str = new String(serverMessage);
			System.out.println("Server say: " + str);

			//respond to server , that the client is Alive!
			String response = client.getClientName() + ": I'm Alive!";
			System.out.println("I answer: " + response + "to port: " + client.serverUDPPort());
			byte[] buffer = response.getBytes();
			try {
				socket.send(new DatagramPacket(buffer, buffer.length, client.getServerAddress(), client.serverUDPPort()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
