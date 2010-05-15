package ConnectFourServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class UdpListener implements Runnable {

	private MainServer server;
	private DatagramSocket socket;

	public UdpListener(MainServer server) {
		this.server = server;
		socket = server.getUdpSocket();
	}

	@Override
	public void run() {
		//Set the message that will be sent to the clients
		String message = "Are You Alive?\n";
		byte[] buffer = message.getBytes();
		
		//now do it infinitely
		while (true) {
			//get the clients which opened a game
			ArrayList<OnlineGames.Client> clientsList = server.games.getUdpList();
			//if there are no clients , sleep for a 5 seconds and retry
			if (clientsList.size()==0)
			{
				server.printLog("No clients yet...\n");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			System.out.println("SEND ALIVE MESSAGES...");
			//send to each client the message to check if he is alive
			for (OnlineGames.Client client : clientsList) {
				try {
					socket.send(new DatagramPacket(buffer, buffer.length,
							client.getAddress(), client.getUDPPort()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			//now wait for answers
			byte[] ans = new byte[100000];
			DatagramPacket answer = new DatagramPacket(ans, ans.length);
			try {
				socket.setSoTimeout(60000);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (true) {
				try {
					//set the receive timeout to be one minute(in milliseconds)
					//VALERIY: it is a problem here, the minute must be private to each client.
					server.printLog("UDP:Waiting for client response...\n");
					socket.receive(answer);
					byte[] ans2 = new byte[answer.getLength()];
					for (int i = 0; i < ans2.length; i++) {
						ans2[i] = ans[i];
					}
					String str = new String(ans2);
					server.printLog("\n----------------------------\nUDP recieved From Client: "+str.split(":")[0]
									+ ", "+answer.getAddress().toString()
									+ " The message is:\n----------------------------\n"
									+ str.split(":")[1]);
					
					//sleep for 20 seconds and then start over.
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
					// TODO: here we should write to server.OnlineGames that
					// answer.getAddress() replied

				} catch (SocketTimeoutException ex) {
					// The minute was exceeded now don't wait for answers, analyze the 
					//result , which client returned an answer
					System.out.println("TIMEOUT");
					break;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// TODO:the minute was exceeded so now we will check
				// who from the clients didn't reply to us!
				

			}
			
		}

	}

}
