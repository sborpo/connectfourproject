package ConnectFourClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;

import ConnectFourClient.TheClient.Viewer;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

public class TransmitWaiter extends Thread {
	// the client to which the waiter is bind to
	private TheClient client;

	public TransmitWaiter(TheClient client) {
		this.client = client;
	}

	public void run() {
		// open a UDP socket , from which we will do the communications
		// with the server
		try {
			client.transmitSocket = new DatagramSocket(client.getTransmitPort());
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Client starting waiting for transmit message from: "
				+ client.serverUDPPort());

		byte[] message = new byte[100000];
		while (true) {
			//wait to an echo message from the server
			DatagramPacket mes = new DatagramPacket(message, message.length);
			try {
				client.transmitSocket.receive(mes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			byte[] msgFormatted = new byte[mes.getLength()];
			System.arraycopy(message, 0, msgFormatted, 0, mes.getLength());
			String str = new String(msgFormatted);
			//print the received message from the server
			System.out.println("Server say: " + str);
			
			//check if the message received was ok
			if (treatMessage(str)){
				break;
			}

		}

	}
	
	public void sendMoveToViewers(String move)
	{
		byte[] buffer = move.getBytes();
		Collection<Viewer> viewers=client.getViewerList().values();
		for (Viewer viewer : viewers) {
			try {
				System.out.println("Sending to: " + viewer.getName()+ " move: " + move);
				client.getTransmitSocket().send(new DatagramPacket (buffer, buffer.length, viewer.getAddress(), viewer.getUDPPort()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

	private boolean treatMessage(String message){
		boolean result = true;
		ClientServerProtocol prot= new ClientServerProtocol(msgType.CLIENT);
		String [] messageCommand=prot.parseCommand(message);
		if(messageCommand == null){
			System.out.println(prot.result + ". Bad server command.");
			result = false;
		}
		else if (messageCommand[0].equals(ClientServerProtocol.VIEWERTRANSMIT))
		{
			int udpPort= Integer.parseInt(messageCommand[1]);
			InetAddress address=null;
			try {
				address = InetAddress.getByName(messageCommand[2]);
			} catch (UnknownHostException e) {
				System.out.println(e.getMessage());
				result = false;
			}
			String watchName= messageCommand[3];
			TheClient.Viewer viewer = new TheClient.Viewer(address,udpPort,watchName);
			client.addToViewerList(viewer);
		}
		return result;
	}

}
