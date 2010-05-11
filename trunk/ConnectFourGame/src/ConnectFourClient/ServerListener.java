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

	// the client to wich the listenet is bing to
	private TheClient client;

	public ServerListener(TheClient client) {
		this.client = client;
	}

	public void run() {
		DatagramSocket socket = null;
		// open a UDP socket , from which we will do the communications
		// with the server
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
			//print the recieved message from the server
			String str = new String(serverMessage);
			System.out.println(str);

			//respond to server , that the client is Alive!
			String response = client.getClientName() + ": I'm Alive!";
			byte[] buffer = response.getBytes();
			try {
				socket.send(new DatagramPacket(buffer, buffer.length, client
						.getServerAddress(), client.serverUDPPort()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
