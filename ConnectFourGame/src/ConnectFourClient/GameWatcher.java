package ConnectFourClient;

import gameManager.Board;
import gameManager.BoardGUI;
import gameManager.GameGUI;
import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;
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
import java.net.SocketTimeoutException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import theProtocol.ClientServerProtocol;

public class GameWatcher extends GameGUI implements Runnable{

	static public class GameEndedException extends Exception{}; 
	private TheClient client=null;
	private ServerSocket Isocket=null;
	private Socket watchSocket = null;
	private BufferedReader watcherIn = null;
	private Thread watcher= null;
	private String redPlayer;
	private String bluePlayer;
	
	public GameWatcher(TheClient client,String redPlayer,String bluePlayer)
	{	
		this.redPlayer=redPlayer;
		this.bluePlayer=bluePlayer;
		gameBoard = new BoardGUI(this);
		this.client=client;
		this.watcherIn = null;
		Box [] arr= new Box[3];
		arr[0]=createUserNamesBox();
		arr[1]= createGridsBox();
		arr[2]=createConsolseBox();
		this.addWindowListener(this);
		AdjustGUIView(arr);	
		connAs1.setText(redPlayer+"  ");
		connAs1.setForeground(java.awt.Color.RED);
		connAs2.setText("  "+bluePlayer);
		connAs2.setForeground(java.awt.Color.BLUE);
	}
	
	
	public void writeToScreen(String message)
	{
		try {
				SwingUtilities.invokeAndWait(new BoardGUI.MessagePrinter(consoleArea,message));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		client.logger.print_info("Game Watcher Is Running! ");
		try{
			ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
			Isocket = new ServerSocket(client.getWatchPort());
			watchSocket = Isocket.accept();
			watcherIn =  new BufferedReader(new InputStreamReader(watchSocket.getInputStream()));
			//set the timeout to read
			watchSocket.setSoTimeout((int)(moveTime*1.2*1000));
			String inputLine = null;
			client.logger.print_info("Waiting for a transmit message...");
			while(true){
				while((inputLine = watcherIn.readLine()) != null) {
					if(inputLine.equals("")){
						break;
					}
					if (inputLine==null)
					{
						throw new IOException();
					}
					client.logger.print_info("Transmition received: " + inputLine);
					String[] parsed = prot.parseCommand(inputLine);
					if(parsed == null){
						client.logger.print_error(prot.result + ". Bad move report received!");
					}
					else if(parsed[0].equals(ClientServerProtocol.GAMEMOVE)){
					
						Color c =(parsed[3].equals("red"))? Color.RED : Color.BLUE;
						try {
							state = gameBoard.playColumn(Integer.parseInt(parsed[2]), c);
							SwingUtilities.invokeAndWait(new BoardGUI.Painter(((BoardGUI)gameBoard).getColumnsFil(),Integer.parseInt(parsed[2]),c, slots));
							writeToScreen("Waiting for "+c.getColorStr()+" turn...");
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalMove e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if(parsed[0].equals(ClientServerProtocol.GAMEREPORT)){
						String winner = parseReport(parsed);
						if(winner != null){
							client.logger.print_info(winner + " is the winner!");
							writeToScreen(winner + " is the winner!");
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
		}
		catch (SocketTimeoutException e)
		{
			client.logger.print_info(e.getMessage() + ". Stopping watching...");
			writeToScreen("Connection Timeout , something went wrong , please reconnect!");
		}
		catch (IOException e) {
			client.logger.print_info(e.getMessage() + ". Stopping watching...");
			//writeToScreen("The other player closed the connection!");
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
			if(watchSocket != null){
				watchSocket.close();
				watchSocket = null;
			}
			client.logger.print_error("Closed watch socket");
			if(watcherIn != null){
				watcherIn.close();
				watcherIn = null;
			}
			client.logger.print_error("Closed watchIn");

			if(Isocket != null){
				Isocket.close();
				Isocket = null;
			}
			client.logger.print_error("Closed watchIn");
		} catch (IOException e) {
		//	client.logger.print_error("Cannot close input stream for watch");
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
		this.stopWatching();
		client.stopWatching();
		try {
			watcher.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
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
