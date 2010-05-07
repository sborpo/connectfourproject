package ConnectFourClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServerListener extends Thread {
	
	private TheClient client;
	public ServerListener(TheClient client)
	{
		this.client=client;
	}
	
	public void run()
	{
		DatagramSocket socket=null;
		try {
			socket = new DatagramSocket(client.listenToServerPort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte [] message = new byte[100000];
		DatagramPacket mes = new DatagramPacket(message, message.length);
		try {
			socket.receive(mes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte [] serverMessage = new byte[mes.getLength()];
		for (int i=0; i<serverMessage.length; i++)
		{
			serverMessage[i]=message[i];
		}
		String str= new String(serverMessage);
		System.out.println(str);
		
		String response = "UDP Client To Server!";
		byte [] buffer=response.getBytes();
		try {
			socket.send(new DatagramPacket(buffer, buffer.length,client.getServerAddress(),client.serverUDPPort()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
