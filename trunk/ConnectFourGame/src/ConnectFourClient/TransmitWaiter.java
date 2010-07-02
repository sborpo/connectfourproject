package ConnectFourClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collection;

import common.AESmanager;

import ConnectFourClient.TheClient.Viewer;

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
					
					in = new BufferedReader(new InputStreamReader(AESmanager.getDecryptedInStream(transmitCommandSocket.getInputStream())));
					String inputLine = null;
					
					if((inputLine = in.readLine()) != null) {
						//print the received message from the server
						client.logger.print_info("Server transmit command: " + inputLine);
						
						//check if the message received was ok
						if (!treatMessage(inputLine)){
							client.logger.print_error("Bad transmit command");
						}
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
			viewer.sendMove(move);
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