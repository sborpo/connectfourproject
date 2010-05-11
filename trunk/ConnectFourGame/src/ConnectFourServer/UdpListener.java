package ConnectFourServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
		
		//now do it inffinitely
		while (true) {
			//get the clients which opened a game
			ArrayList<OnlineGames.Client> clientsList = server.games
					.getUdpList();
			//if there are no clients , sleep for a 5 seconds and retry
			if (clientsList.size()==0)
			{
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			//send to each client the message to check if he is alive
			for (OnlineGames.Client client : clientsList) {
				try {
					socket.send(new DatagramPacket(buffer, buffer.length,
							client.getAddress(), client.getPort()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			//now wait for answers
			byte[] ans = new byte[100000];
			DatagramPacket answer = new DatagramPacket(ans, ans.length);
			while (true) {
				try {
					//set the recieve timeout to be one minute
					socket.setSoTimeout(1000);
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

					// TODO: here we should write to server.OnlineGames that
					// answer.getAddress() replied

				} catch (SocketTimeoutException ex) {
					// The minute was exceeded now dont wait for answers, analyze the 
					//result , which client returned an answer
					break;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// TODO:the minute was exceeded so now we will check
				// who from the clients didn't reply to us!
				
				
				//sleep for 20 seconds and then start over.
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
		}

	}

}
