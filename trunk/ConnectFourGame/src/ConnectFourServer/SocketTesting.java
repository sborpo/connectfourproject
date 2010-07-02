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
import java.net.SocketException;
import java.net.UnknownHostException;

public class SocketTesting {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean flag=false;
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
				int localport = opponentSocket.getLocalPort();
				int theirPort= opponentSocket.getPort();
				InetAddress add= opponentSocket.getInetAddress();
				InetAddress localAdd=opponentSocket.getLocalAddress();
			
				while (true)
				{
					try {
						String what = null;
						try {
							what = (String)opponentIn.readObject();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println(what);
					} catch (IOException e) {
						 try {
						 if (e.getMessage().equals("Connection reset"))
						 {
							 		try{
									opponentSocket.close(); 
										} catch (IOException ex1) {
											// TODO Auto-generated catch block
											ex1.printStackTrace();
										}	
									opponentSocket= serverSocket.accept();
									opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
						 }
						 else
						 {
							 		try{
									opponentSocket.close(); 
									opponentSocket= new Socket(add, theirPort, localAdd	,localport);
									//opponentSocket.close(); 
										} catch (IOException ex1) {
											// TODO Auto-generated catch block
											ex1.printStackTrace();
										}	
									//opponentSocket= serverSocket.accept();
									opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
						 }
								
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
					address = InetAddress.getByName("10.0.0.6");
					
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
