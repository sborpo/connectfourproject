package ConnectFourServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

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
		String message = ClientServerProtocol.YOUALIVE;
		byte[] buffer = message.getBytes();
		Timer waitingTime = new Timer(30);
		waitingTime.start();
		//now do it infinitely
		while (true) {
			ArrayList<String> clientsList=null;
			//get the clients
			clientsList = server.clients.getClients();
			
			if(waitingTime.getElapsed() >= waitingTime.getLength()){
				//if there are no clients
				if (clientsList.size()==0)
				{
					server.printLog("No clients yet, waiting...");
					waitingTime.reset();
					continue;
				}
			}
			else{
				continue;
			}
			
			synchronized (server.clients) {
				//reset the alive map
				server.clients.resetIsAliveMap();
			}
			
			server.printLog("SENDING ALIVE MESSAGES...\n");
			//send to each client the message to check if he is alive
			for (String clientName : clientsList) {
				try {
					OnlineClients.Client client = server.clients.getClient(clientName);
					server.printLog("To: "+client.getName()+", to port: "+client.getUDPPort()+"\n");
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
				socket.setSoTimeout(30000);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			server.printLog("UDP:Waiting for client response...\n");
			int receivedAns = 0;
			//start a 30 seconds timer
			Timer timer= new Timer(30);
			timer.start();
			while (true) {
				try {

					socket.receive(answer);
					//update the socket timeout accorfing to the Timer elapsed time. if it passed 
					//the 30 seconds , so put 1 milisecond timeout , this way in the next iteration
					//socket timeout will be thrown
					socket.setSoTimeout((30000-timer.getElapsed())>0 ? (30000-timer.getElapsed()) : 1);
					byte[] ans2 = new byte[answer.getLength()];
					for (int i = 0; i < ans2.length; i++) {
						ans2[i] = ans[i];
					}
					String aliveResponse = new String(ans2);
					ClientServerProtocol prot= new ClientServerProtocol(msgType.SERVER);
					String[] theResponse = prot.parseCommand(aliveResponse);
					
					if(theResponse == null){
						server.printLog("I don't understand clients respond to alive message\n");
						break;
					}
					
					String clientName = theResponse[1];
					String clientMessage = theResponse[0];
					server.printLog("\n----------------------------\nUDP recieved From Client: "+clientName
									+ ", "+answer.getAddress().toString()
									+ " The message is:\n----------------------------\n"
									+ clientMessage);
					//set the client alive
					if(!server.clients.setAliveIfExists(clientName)){
						server.printLog("Client with name: "+ clientName + " doesn't exists\n");
					}
					receivedAns++;
//					if(receivedAns >= clientsList.size()){
//						//sleep for 5 seconds and then start over.
//						try {
//							Thread.sleep(5000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						break;
//					}
					
					//break;
					// TODO: here we should write to server.OnlineGames that
					// answer.getAddress() replied

				} catch (SocketTimeoutException ex) {
					// The minute was exceeded now don't wait for answers, analyze the 
					//result , which client returned an answer
					server.printLog("TimeOUT...\n");
					server.clients.removeIfNotAlive();					
					break;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// TODO:the minute was exceeded so now we will check
				// who from the clients didn't reply to us!
				

			}
			//server.printLog("No clients yet, waiting...\n");
			
			//after checking , sleep for 30 seconds , and then start over
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
