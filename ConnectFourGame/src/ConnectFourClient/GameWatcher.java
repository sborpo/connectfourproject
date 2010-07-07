package ConnectFourClient;

import gameManager.Board;
import gameManager.BoardGUI;
import gameManager.GameGUI;
import gameManager.Board.GameState;
import gameManager.Player.Color;

import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import theProtocol.ClientServerProtocol;

public class GameWatcher extends GameGUI implements Runnable{

	static public class GameEndedException extends Exception{}; 
	Board gameBoard;
	TheClient client=null;
	ServerSocket socket=null;
	Socket watchSocket = null;
	BufferedReader watcherIn = null;
	Thread watcher= null;
	
	public GameWatcher(TheClient client)
	{	
		gameBoard = new BoardGUI(this);
		this.client=client;
		this.watcherIn = null;
		Box [] arr= new Box[4];
		arr[0]=createUserNamesBox();
		arr[1]= createGridsBox();
		arr[2]= createSurrenderBox();
		arr[3]=createConsolseBox();
		AdjustGUIView(arr);	
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
					
						Color c =(parsed[3].equals("red"))? Color.RED : Color.BLUE;
						try {
							SwingUtilities.invokeAndWait(new BoardGUI.Painter(((BoardGUI)gameBoard).getColumnsFil(),Integer.parseInt(parsed[2]),c, slots));
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
	
	
	@Override
	public void windowClosing(WindowEvent e) {
		
	}

	
	@Override
	public  void mouseClicked(MouseEvent e) {
	}
	
	
	@Override
	public void windowOpened(WindowEvent e) {
		watcher = new Thread(this);
		watcher.start();
	}

}
