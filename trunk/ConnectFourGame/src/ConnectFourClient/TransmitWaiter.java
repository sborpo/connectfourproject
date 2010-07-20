package ConnectFourClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collection;

import common.AESmanager;

import ConnectFourClient.TheClient.Viewer;
import ConnectFourClient.TheClient.Viewer.SendingToWatcherProblem;

import theProtocol.ClientServerProtocol;
import theProtocol.ClientServerProtocol.msgType;

public class TransmitWaiter extends Thread {
	// the client to which the waiter is bind to
	private TheClient client;
	ServerSocket transmitWaiterSocket = null;
	Socket transmitCommandSocket = null;
	BufferedReader in = null;
	
	public TransmitWaiter(ServerSocket transmitWaiterSocket,TheClient client) {
		this.client = client;
		this.transmitWaiterSocket = transmitWaiterSocket;
		transmitCommandSocket = null;
		in = null;
	}

	public void run() {
		// open a UDP socket , from which we will do the communications
		// with the server
		client.logger.print_info("Client starting waiting for transmit message on: "
				+ client.getTransmitWaiterPort());
		try{
			while (transmitWaiterSocket != null) {
					try{
						transmitCommandSocket = transmitWaiterSocket.accept();
						client.logger.print_info("Transmition socket was opened");
					}
					catch(SocketException sck_exc){
						client.logger.print_info("Transmition socket was closed");
						break;
					}
					
					in = new BufferedReader(new InputStreamReader(transmitCommandSocket.getInputStream()));
					String inputLine = null;
					try{
						if((inputLine = in.readLine()) != null) {
							//print the received message from the server
							client.logger.print_info("Message received on transmit waiter: '" + inputLine +"'");
							
							//check if the message received was ok
							if (!treatMessage(inputLine)){
								client.logger.print_error("Bad transmit command");
							}
						}
					}catch (Exception e)
					{
						//do nothing , client closed the socket
					}
			}
		} 
		catch (Exception e) {
			client.logger.print_error("In transmit receiving");
			e.printStackTrace();
		}
	}
	
	public void sendMoveToViewers(String move)
	{
		Collection<Viewer> viewers=client.getViewerList().values();
		for (Viewer viewer : viewers) {
			try {
				viewer.sendMove(move);
			} catch (SendingToWatcherProblem e) {
				client.removeViewerIfExists(viewer.getName());
			}
		}	
	}

	private boolean treatMessage(String message){
		boolean result = true;
		ClientServerProtocol prot= new ClientServerProtocol(msgType.CLIENT);
		String [] messageCommand=prot.parseCommand(message);
		if(messageCommand == null){
			client.logger.print_error(prot.result + ". Bad server command.");
			result = false;
		}
		//If it is the transmit command
		else if (messageCommand[0].equals(ClientServerProtocol.VIEWERTRANSMIT))
		{
			int watcherPort= Integer.parseInt(messageCommand[1]);
			InetAddress address=null;
			try {
				address = InetAddress.getByName(messageCommand[2]);
			} catch (UnknownHostException e) {
				client.logger.print_error(e.getMessage());
				result = false;
			}
			String watchName= messageCommand[3];
			client.removeViewerIfExists(watchName);
			TheClient.Viewer viewer = new TheClient.Viewer(client,address,watcherPort,watchName);
			client.addToViewerList(viewer);
			viewer.sendPreviousMoves();
		}
		else if(messageCommand[0].equals(ClientServerProtocol.SOCKETREFRESH)){
			try {
				PrintWriter clientToOpponent = new PrintWriter(transmitCommandSocket.getOutputStream(),true);
				clientToOpponent.println(ClientServerProtocol.OK);
			} catch (IOException e) {
				client.logger.print_error("While sending ok to opponent: "+e.getMessage());
			}
			client.refreshGameConnection();
		}
		else if(messageCommand[0].equals(ClientServerProtocol.ISURRENDER)){
			try {
				PrintWriter clientToOpponent = new PrintWriter(transmitCommandSocket.getOutputStream(),true);
				clientToOpponent.println(ClientServerProtocol.OK);
			} catch (IOException e) {
				client.logger.print_error("While sending ok to opponent: "+e.getMessage());
			}
			client.opponentSurrender();
		}
		return result;
	}
	
	public void endTransmition(){
		try {
			client.logger.print_info("Closing transmit waiter socket");
			if(in != null){
				in.close();
				in = null;
			}
			if(transmitCommandSocket != null){
					transmitCommandSocket.close();
					transmitCommandSocket = null;
			}
			if(transmitWaiterSocket != null){
				transmitWaiterSocket.close();
				transmitWaiterSocket = null;
			}
		} catch (IOException e) {
			client.logger.print_error("Problem closing socket: " + e.getMessage());
		}
		
	}
}
