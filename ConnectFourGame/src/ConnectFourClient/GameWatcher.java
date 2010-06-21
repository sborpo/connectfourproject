package ConnectFourClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
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
		ServerSocket socket=null;
		try{
			ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
			socket = new ServerSocket(client.getWatchPort());
			
			String inputLine = null;
			while(true){
				Socket watchSocket = socket.accept();
				BufferedReader in =  new BufferedReader(new InputStreamReader(watchSocket.getInputStream()));
				
				while((inputLine = in.readLine()) != null) {
					if(inputLine.equals("")){
						break;
					}
					String[] parsed = prot.parseCommand(inputLine);
					if(parsed == null){
						client.logger.print_error(prot.result + ". Bad move report received!");
					}
					else if(parsed[0].equals(ClientServerProtocol.GAMEMOVE)){
						client.logger.print_info("Got This Move: "+inputLine);
						//TODO: here we need to update the gameboard of watcher
					}
					else if(parsed[0].equals(ClientServerProtocol.GAMEREPORT)){
						String winner = parseReport(parsed);
						if(winner != null){
							client.logger.print_info(winner + " is a winner!");
							client.stopWatching();
							break;
						}
						else{
							client.logger.print_error("Don't understand who is the winner");
							break;
						}
					}
					else{
						client.logger.print_error("I don't understand what transmitter send");
						break;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
