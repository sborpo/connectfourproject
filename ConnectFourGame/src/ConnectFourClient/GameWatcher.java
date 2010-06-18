package ConnectFourClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import theProtocol.ClientServerProtocol;

public class GameWatcher implements Runnable{

	TheClient client=null;
	public GameWatcher(TheClient client)
	{
		this.client=client;
	}
	
	@Override
	public void run() {
		client.logger.print_info("Game Watcher Is Running! ");
		DatagramSocket socket=null;
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		try {
			socket = new DatagramSocket(client.getWatchPort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] message = new byte[100000];
		while (true) {
			//wait to an echo message from the player
			DatagramPacket mes = new DatagramPacket(message, message.length);
			try {
				socket.receive(mes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//copy the message to a buffer , in order to print later
			byte[] playerMessage = new byte[mes.getLength()];
			System.arraycopy(message, 0, playerMessage, 0, mes.getLength());
			String str = new String(playerMessage);
			String[] parsed = prot.parseCommand(str);
			if(parsed == null){
				client.logger.print_error(prot.result + ". Bad move report received!");
				continue;
			}
			client.logger.print_info("Got This Move: "+str);
			String winner = parseReport(parsed);
			if(winner != null){
				client.logger.print_info(winner + " is a winner!");
				break;
			}
		}
	}
	
	private String parseReport(String[] message){
		String winner = null;
		if(message[0].equalsIgnoreCase(ClientServerProtocol.GAMEREPORT)){
			if(Integer.parseInt(message[3]) == 1){
				winner = message[4];
			}
			else{
				winner = "NOBODY";
			}
		}
		return winner;
	}

}
