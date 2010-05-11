package ConnectFourServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ConnectedClient extends Thread {

	private InetAddress clientAddress;
	private int clientUdpPort;
	private int serverPort;
	private MainServer server;
	private DatagramSocket socket;

	public ConnectedClient(InetAddress ipAddress, int udpPort, MainServer server) {
		clientAddress = ipAddress;
		clientUdpPort = udpPort;
		this.server = server;
		serverPort = server.getServerPort();
		socket = server.getUdpSocket();

	}

	@Override
	public void run() {

		byte[] ans = new byte[100000];
		DatagramPacket answer = new DatagramPacket(ans, ans.length);
		try {
			socket.receive(answer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] ans2 = new byte[answer.getLength()];
		for (int i = 0; i < ans2.length; i++) {
			ans2[i] = ans[i];
		}
		String str = new String(ans2);
		server.printLog("UDP recieved : Client: " + clientAddress.toString()
				+ " The message is:\n----------------------------\n" + str);

		String message = "Udp To Client!";
		byte[] buffer = message.getBytes();
		try {
			socket.send(new DatagramPacket(buffer, buffer.length, answer
					.getAddress(), answer.getPort()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
