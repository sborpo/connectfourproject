package ConnectFourServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
				BufferedReader opponentIn=null;
				try {
					 opponentIn = new BufferedReader(new InputStreamReader(opponentSocket.getInputStream()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				PrintStream clientToOpponent=null;
				try {
					 clientToOpponent = new PrintStream(opponentSocket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					String what=opponentIn.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
				BufferedReader opponentIn=null;
				try {
					 opponentIn = new BufferedReader(new InputStreamReader(opponentSocket.getInputStream()));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				PrintStream clientToOpponent=null;
				try {
					 clientToOpponent = new PrintStream(opponentSocket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				clientToOpponent.println("checks!");
		}
	}

}
