package ConnectFourServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class SocketTesting {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean flag=true;
		if (flag)
		{
				ServerSocket serverSocket = null;
				try {
					serverSocket = new ServerSocket(8887);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Socket opponentSocket=null;
				// can be a timeout how much to wait for an opponent
				System.out.println("Waiting for opponent to connect ...\n");
				try {
					 opponentSocket = serverSocket.accept();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ObjectOutputStream clientToOpponent=null;
				try {
					 clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ObjectInputStream opponentIn=null;
				try {
					 opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
				while (true)
				{
					try {
						String what=opponentIn.readLine();
						System.out.println(what);
					} catch (IOException e) {
						 try {
							opponentSocket= serverSocket.accept();
							opponentIn =opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	
						e.printStackTrace();
					}
				}
		}
		else
		{
				
				InetAddress address = null;
				try {
					address = InetAddress.getByName("10.0.0.1");
					
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// the opponent starts the game
				Socket opponentSocket=null;
				try {
					 opponentSocket = new Socket(address, 8887);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ObjectOutputStream clientToOpponent=null;
				try {
					 clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ObjectInputStream opponentIn=null;
				try {
					opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				int localport = opponentSocket.getLocalPort();
				int theirPort= opponentSocket.getPort();
				InetAddress localAdd=opponentSocket.getLocalAddress();
				SocketAddress add=opponentSocket.getRemoteSocketAddress();
				while (true){
					try{
				clientToOpponent.writeObject("checks!");
					}
					catch (IOException ex)
					{
							try {
								opponentSocket.close();
								opponentSocket= new Socket(address, theirPort, localAdd	,localport);
								 clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
		
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
				}
		}
	}

}
