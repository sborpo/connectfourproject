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

	static public class GameEndedException extends Exception{}; 
	
	TheClient client=null;
	ServerSocket socket=null;
	Socket watchSocket = null;
	BufferedReader watcherIn = null;
	
	public GameWatcher(TheClient client)
	{
		this.client=client;
		this.watcherIn = null;
	}
	
	@Override
	public void run() {
		client.logger.print_info("Game Watcher Is Running! ");
		try{
			ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
			socket = new ServerSocket(client.getWatchPort());
			watchSocket = socket.accept();
			watcherIn =  new BufferedReader(new InputStreamReader(watchSocket.getInputStream()));
			
			String inputLine = null;
			client.logger.print_info("Waiting for a transmit message...");
			while(true){
				while((inputLine = watcherIn.readLine()) != null) {
					if(inputLine.equals("")){
						break;
					}
					client.logger.print_info("Transmition received: " + inputLine);
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
							throw new GameEndedException();
						}
						else{
							client.logger.print_error("Don't understand who is the winner");
							throw new GameEndedException();
						}
					}
					else{
						client.logger.print_error("I don't understand what transmitter send");
						throw new GameEndedException();
					}
				}
			}
		} catch (IOException e) {
			client.logger.print_info(e.getMessage() + ". Stopping watching...");
			this.stopWatching();
			client.stopWatching();
			e.printStackTrace();
		}
		catch (GameEndedException myExc){
			client.logger.print_info("The game is over!");
			this.stopWatching();
			client.stopWatching();
		}
	}
	
	private void stopWatching(){
		try {
			if(watcherIn != null){
				watcherIn.close();
				watcherIn = null;
			}
			if(watchSocket != null){
				watchSocket.close();
				watchSocket = null;
			}
			if(socket != null){
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
			client.logger.print_error("Cannot close input stream for watch");
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
