package ConnectFourClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class GameWatcher implements Runnable{

	TheClient client=null;
	public GameWatcher(TheClient client)
	{
		this.client=client;
	}
	
	@Override
	public void run() {
		System.out.println("Game Watcher Is Running! ");
		DatagramSocket socket=null;
		try {
			socket = new DatagramSocket(client.getWatchPort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] message = new byte[100000];
		while (true) {
			//wait to an echo message from the player
			DatagramPacket mes = new DatagramPacket(message, message.length);
			try {
				socket.receive(mes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//copy the message to a buffer , in order to print later
			byte[] playerMessage = new byte[mes.getLength()];
			for (int i = 0; i < playerMessage.length; i++) {
				playerMessage[i] = message[i];
			}
			String str = new String(playerMessage);
			System.out.println("Got This Move: "+str);
		
		}
	}

}
