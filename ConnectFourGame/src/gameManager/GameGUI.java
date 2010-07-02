package gameManager;

import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;
import gameManager.Player.Color;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.sun.java.swing.SwingUtilities3;

import common.UnhandeledReport;

import theProtocol.ClientServerProtocol;

import ConnectFourClient.MainFrame;
import ConnectFourClient.TheClient;

public class GameGUI extends JDialog implements MouseListener, Runnable,Game,WindowListener {
	

	public static class Pending{
		public Pending()
		{
			pending=false;
		}
		public boolean isPending() {
			return pending;
		}

		public void setPending(boolean pending) {
			this.pending = pending;
		}

		private boolean pending;
		
	}
	public static class TimeEnded extends Exception{}
	/**
	 * 
	 */
	public static class AutomaticSurrender extends Exception{}

	//LOGIC PARAMETERS
	protected String gameId;
	protected Player red;
	protected Player blue;
	protected HashMap<String,Player> watchers;
	protected Board gameBoard;
	protected Player plays;
	protected GameState state;
	protected ArrayList<String> gameHistory;
	protected String gameReport;
	protected Pending pending;
	private Player clientPlayer;
	private Player opponentPlayer;
	private boolean iSurrend=false;
	private ServerSocket serverSocket;
	private Socket opponentSocket;
	
	
	//START ONLINE GAME PARAMETERS
	private int clientGamePort;
	private Object opponentHost;
	private int i;
	boolean b;
	
	
	
	//GUI COMPONENTS
	private MainFrame f;
	private TheClient theClient;
	private JButton startGame;
    private JButton surrender;
	private JPanel boardPane;
	private Box upperBox;
	private JButton[][] slots;
	private JLabel consoleArea;
	private JLabel connAs1;
	private JLabel connAs2;
	private int clickedColNum;
	private String clickedByPlayer;
	private UnhandeledReport gameResult;
	private Thread gameThread;
	
	
	public boolean isGameFull()
	{
		return (blue != null);
	}
	
	public Player isPlayer(String playerName){
		Player player = null;
		if((red != null && red.getName().equals(playerName))){
			player = red;
		}
		else if((blue != null && blue.getName().equals(playerName))){
			player= blue;
		}
		
		return player;
	}
	
	synchronized public Player addWatcher(String watchName, String playerName){
		Player player = isPlayer(playerName);
		
		if(player != null){
			if(watchers.containsKey(watchName)){
				player = watchers.get(watchName);
			}
			else{
				watchers.put(watchName, player);
			}
		}
		
		return player;
	}
	


	public GameGUI(String name1,String name2,String gameId,MainFrame f, int clientGamePort, Object opponentHost, int i, boolean b, TheClient theClient) {
		this.f=f;
		this.clientGamePort=clientGamePort;
		this.opponentHost=opponentHost;
		pending = new Pending();
		this.i=i;
		this.b=b;
		red = new Player(Player.Color.RED,name1);
		if(name2 != null){
			blue = new Player(Player.Color.BLUE,name2);
		}
		else{
			blue = null;
		}
		this.gameId = gameId;
		gameBoard = new BoardGUI(this);
		gameHistory = new ArrayList<String>();
		gameReport = "";
		watchers = new HashMap<String,Player>();
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		 upperBox = Box.createVerticalBox();
		boardPane= new JPanel();
		adjustGrid();
		upperBox.add(boardPane);
		Box lowerBox = Box.createHorizontalBox();
		startGame=new JButton("StartTheGame");
		startGame.addMouseListener(this);
		startGame.setHorizontalAlignment(SwingConstants.LEFT);
		surrender= new JButton("Surrender");
		surrender.setName("SURRENDED");
		surrender.addMouseListener(this);
		surrender.setHorizontalAlignment(SwingConstants.RIGHT);
		lowerBox.add(startGame);
		lowerBox.add(surrender);
		Box lowerBox3 = Box.createHorizontalBox();
		Box TopMost = Box.createHorizontalBox();
		 connAs1= new JLabel();
		 connAs2= new JLabel();
		TopMost.add(connAs1);
		TopMost.add(connAs2);
		lowerBox3.setSize(700, 200);
		consoleArea = new JLabel("Console Printer");
		lowerBox3.setAlignmentX(SwingConstants.LEFT);
		consoleArea.setAlignmentX(LEFT_ALIGNMENT);
		lowerBox3.add(consoleArea);
		this.theClient=theClient;
		gameBoard= new BoardGUI(this);
		this.getContentPane().add(TopMost);
		this.getContentPane().add(upperBox);
		this.getContentPane().add(lowerBox);
		this.getContentPane().add(lowerBox3);
		setSize(700,700);
		setModal(true);
		
		

		
		
	}
	
	private void adjustGrid()
	{
		boardPane.setLayout(new GridLayout(6,7));
		boardPane.setBorder(new LineBorder(java.awt.Color.black));
		slots = new JButton[6][7];
		for (int row=5; row>=0; row--) {
		for (int column=0; column<7; column++) {
			slots[row][column] = new JButton();
			slots[row][column].setBorder(new LineBorder(java.awt.Color.black));
			slots[row][column].setName(String.valueOf(row)+" "+String.valueOf(column));
			slots[row][column].setHorizontalAlignment(SwingConstants.CENTER);
			slots[row][column].addMouseListener(this);
			boardPane.add(slots[row][column]);
		}
		}
		boardPane.setSize(700,600);
	}
	
	public UnhandeledReport getReportStatus() {
		return gameResult;
	}
	
	public void writeClients(String playerName1,Color player1Col,String playerName2,Color player2Col)
	{
		try {
			SwingUtilities.invokeAndWait(new BoardGUI.ConnectionBoxPrinter(connAs1, connAs2, playerName1, player1Col, playerName2, player2Col));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	public UnhandeledReport startOnlineGame(int clientPort, String opponentHost,int opponentPort, boolean startsGame, TheClient theClient) {
	
		serverSocket = null;
		opponentSocket = null;
		ObjectInputStream opponentIn = null;
		ObjectOutputStream clientToOpponent = null;
		try {
			if (startsGame == true) {
				serverSocket = new ServerSocket(clientPort);
				// can be a timeout how much to wait for an opponent
				writeToScreen("Waiting for opponent to connect ...");
				opponentSocket = serverSocket.accept();
				clientPlayer = red;
				writeToScreen("Opponent Was Connected ,You Are The Red Player!  ");
			
			} else {
				InetAddress address = null;
				try {
					address = InetAddress.getByName(opponentHost);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// the opponent starts the game
				opponentSocket = new Socket(address, opponentPort);
				clientPlayer = blue;
				opponentPlayer=red;
				writeToScreen("You Are The Blue Player!  ");
				writeClients(blue.getName(),blue.getColor(),red.getName(),red.getColor());

			}
			clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
			opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
			excahngeClientName(clientToOpponent,opponentIn);
		} catch (IOException e) {
			// TODO Handle serverSocket initialization problem
			e.printStackTrace();
			return new UnhandeledReport(getId(), theClient.getClientName(), "0", "no-winner");
		}

		plays = red;
		//String playerStr;
		state = GameState.PROCEED;
		ClientServerProtocol prot = new ClientServerProtocol(ClientServerProtocol.msgType.CLIENT);
		while (state.equals(GameState.PROCEED)) {
			int colnum = -1;
			String inLine = null;
			try {
				if (plays.equals(clientPlayer)) {
					writeToScreen("Please Click Your Move");
					while(colnum == -1){
						synchronized (pending) {
							try {
								pending.setPending(true);
								//wait for a user input from the mouse
								pending.wait();
							} catch (InterruptedException e) {
								//
							}
							pending.setPending(false);
						}
						
						inLine=clickedByPlayer;
						if(inLine.equals("")){
							writeToScreen("Empty move, try again...");
						}
						else{
							if (inLine.equals(Surrended))
							{
								state=GameState.I_SURRENDED;
								break;
							}
							else
							{
								colnum	=	Integer.parseInt(inLine);
							}
						}
					}
				} else {
					writeToScreen("Waiting For Opponent Move!:\n");
					boolean reconnectOnRead= true;
					while (reconnectOnRead)
					{	
						reconnectOnRead=false;
						try{
						//try to read from the opponent	
							try {
								String move=(String)opponentIn.readObject();
								if (move.equals(Surrended))
								{
									state=GameState.OPPONENT_SURRENDED;
								}
								else
								{
									colnum = Integer.parseInt(move);
								}
							} catch (NumberFormatException e) {
								//cannot be
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					
						}
						catch (IOException ex)
						{
							if (iSurrend)
							{
								return null;
							}
							reconnectOnRead=true;
							handleReconnectionProcess(opponentSocket, serverSocket, opponentIn, startsGame, clientToOpponent, opponentHost, opponentPort);
						}
					}
				}
				//the clients not surrended
				if (state.equals(GameState.PROCEED))
				{
					state = gameBoard.playColumn(colnum, plays.getColor());
					try {
						SwingUtilities.invokeAndWait(new BoardGUI.Painter(((BoardGUI)gameBoard).getColumnsFil(), colnum, plays.getColor(), slots));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IllegalMove e) {
				writeToScreen("Illegal Move!!! Please Retry!\n\n");
				continue;
			} 
			catch (TimeEnded e)
			{
				//TODO what to do after retires
			}
			//send the move to the viewers
			//String colorStr=plays.getColor().equals(Color.BLUE) ? "Blue" : "Red";
			String move= (state.equals(GameState.I_SURRENDED) || state.equals(GameState.OPPONENT_SURRENDED)) ? Surrended : String.valueOf(colnum);
			String moveMsg = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEMOVE,
																			plays.getName(),
																			move});
			
			String[] parsed = prot.parseCommand(moveMsg);
			if(parsed == null){
				writeToScreen(prot.result + ". Bad move report: "+ moveMsg);
			}
			theClient.getTransmitWaiter().sendMoveToViewers(moveMsg);
			
			//add the move to the game history
			gameHistory.add(moveMsg);
			
			if (plays.equals(clientPlayer)) {
				// write your move
				boolean reconnectOnRead= true;
				while (reconnectOnRead)
				{	
					reconnectOnRead= false;
					try{
						clientToOpponent.writeObject(move);
					}
					catch (IOException ex)
					{
						if (iSurrend)
						{
							return null;
						}
						reconnectOnRead= true;
						try {
							handleReconnectionProcess(opponentSocket, serverSocket, opponentIn, startsGame, clientToOpponent, opponentHost, opponentPort);
						} catch (TimeEnded e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					}
				}
				
			}
			nextPlayer();
		}

		String winner = null;
		winner=decideWinner();
		Integer gameRes = (state.equals(GameState.TIE)) ? 0 : 1;
		gameReport = ClientServerProtocol.buildCommand(new String[] {ClientServerProtocol.GAMEREPORT,
																	 this.getId(),
																	 theClient.getClientName(),
																	 gameRes.toString(),
																	 winner,
																	 "dummy"});
		String[] parsed = prot.parseCommand(gameReport);
		if(parsed == null){
			System.out.println(prot.result + ". Bad game report: "+ gameReport);
		}
		theClient.getTransmitWaiter().sendMoveToViewers(gameReport);
		try {
			if( opponentSocket != null){
				opponentSocket.close();
				opponentSocket=null;
			}
			
			if(serverSocket != null){
				serverSocket.close();
				serverSocket=null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new UnhandeledReport(this.getId(), theClient.getClientName()	, gameRes.toString(), winner);

	}
	
	

	private void excahngeClientName(ObjectOutputStream clientToOpponent, ObjectInputStream opponentIn) throws IOException {
		if(clientPlayer.equals(blue)){
			clientToOpponent.writeObject((clientPlayer.getName()));
		}
		else{
			String name2 = null;
			try {
				name2 = (String)opponentIn.readObject();
			} catch (ClassNotFoundException e) {
				//cannot be
			}
			if( name2 != null){
				addPlayer(name2);
				opponentPlayer=blue;
				writeClients(red.getName(),red.getColor(),blue.getName(),blue.getColor());
			}
		}
		
	}

	private String decideWinner() {
		String winner;
		if (state.equals(GameState.TIE)) {
			writeToScreen("The game ended with Tie!\n");
			return "tie";
		}
		if (state.equals(GameState.I_SURRENDED))
		{
			System.out.println("You have surrended!\n");
//			winner = (plays.getColor().equals(Color.BLUE)) ? red.getName() : blue.getName();
			winner= opponentPlayer.getName();
			writeToScreen(winner + " player has won the game!\n");
			return winner;
		}
	    if (state.equals(GameState.OPPONENT_SURRENDED))
		{
			System.out.println("The opponent has surrended!\n");
//			winner = (plays.getColor().equals(Color.RED)) ? red.getName() : blue.getName();
			winner= clientPlayer.getName();
			writeToScreen(winner + " player has won the game!\n");
			return winner;
		}
//	    winner=state.equals(GameState.RED_WON) ? red.getName() : blue.getName();
	    winner= clientPlayer.getName();
	    writeToScreen(winner + " player has won the game!\n");
		return winner;
		
	}

	private void handleReconnectionProcess(Socket opponentSocket,ServerSocket serverSocket,ObjectInputStream opponentIn,boolean startGame, ObjectOutputStream clientToOpponent, String opponentHost, int opponentPort) throws  TimeEnded {
		try {
			opponentIn.close();
			clientToOpponent.close();
			opponentSocket.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if (startGame)
		{
			try {
				serverSocket.setSoTimeout(20000);
			} catch (SocketException e1) {
				// There is a problem with the server socket
				e1.printStackTrace();
			}
			int retries=0;
			while (retries<3)
			{
				try {
					
					opponentSocket = serverSocket.accept();
					opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
					clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
					return;
				} 
				catch (SocketTimeoutException e) {
					//There was a timeout , so the game must be closed and reoported as a winning
					throw new TimeEnded();
				}
				catch (IOException e) {
					// The client cannot start the server socket and reconnect
					e.printStackTrace();
					retries++;
					//sleep for five second
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
					}
				}
			}
		
		}
		//the client was not the host of the game
		else
		{
			int retries=0;
			while (retries<3)
			{
					InetAddress address = null;
					try {
						address = InetAddress.getByName(opponentHost);
						opponentSocket = new Socket(address, opponentPort);
						opponentIn = new ObjectInputStream((opponentSocket.getInputStream()));
						clientToOpponent = new ObjectOutputStream(opponentSocket.getOutputStream());
						return;
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 catch (IOException e) {
						try {
							Thread.sleep(20000);
						} catch (InterruptedException e1) {
						}
						e.printStackTrace();
					}
					 retries++;
			}
		}
		throw new TimeEnded();
}


	public void startGame() {
		System.out
				.println("Please Choose who will be Red and who will be Blue\n");
		Random randGen = new Random();
		String playerStr;
		int player = randGen.nextInt(2);
		plays = (player == 0) ? red : blue;
		GameState state = GameState.PROCEED;
		String playerStarts = (player == 0) ? "Red" : "Blue";
		System.out.println(playerStarts + " Player will start the game!\n");
		while (state.equals(GameState.PROCEED)) {
		//TODO:	gameBoard.PrintBoard();
			playerStr = (plays.getColor().equals(Player.Color.RED)) ? "Red"
					: "Blue";
			System.out.println("\n" + playerStr
					+ "Player, Please Enter You column number: ");
			int colnum = -1;
			BufferedReader stdin;
			try {
				stdin = new BufferedReader(new InputStreamReader(System.in));
				colnum = Integer.parseInt(stdin.readLine());
				state = gameBoard.playColumn(colnum, plays.getColor());
			} catch (IllegalMove e) {
				System.out.println("Illegal Move!!! Please Retry!\n\n");
				continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nextPlayer();

		}
	//TODO:	gameBoard.PrintBoard();
		if (state.equals(GameState.TIE)) {
			System.out.println("The game ended with Tie!\n\n");
		}
		String won = state.equals(GameState.RED_WON) ? "Red" : "Blue";

		System.out.println(won + " player has won the game!\n\n");
		
		return;

	}

	private void nextPlayer() {
		System.out.println("Current player: "+plays.getName());
		if (plays.getColor().equals(Player.Color.RED)) {
			plays = blue;
		} else {
			plays = red;
		}
		System.out.println("Changed to player: "+plays.getName());
	}
	
	synchronized public void addPlayer(String player2){
		blue = new Player(Player.Color.BLUE,player2);
	}
	 
	public Player getPlayer(Player.Color pColor){
		return pColor.equals(Player.Color.RED) ? red : blue;
	}

	public String getId(){
		return gameId;
	}
	
	public ArrayList<String> getGameHistory(){
		return gameHistory;
	}

	@Override
	public  void mouseClicked(MouseEvent e) {
		
		if (e.getComponent()==startGame)
		{
			startGame.removeMouseListener(this);
			startGame.setEnabled(false);
			gameThread= new Thread(this);
			gameThread.start();
		 return;
		}
		synchronized (pending) {
		if (pending.isPending())
		{
			String buttonName=((JButton)e.getComponent()).getName();
			if (buttonName.equals(Surrended))
			{
				this.clickedByPlayer=buttonName;
			}
			else
			{
				int colnum= (new Integer(Integer.parseInt(buttonName.split(" ")[1])));
				this.clickedByPlayer =(new Integer(colnum)).toString();
			}
				pending.setPending(false);
				pending.notify();
			}
		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
	
	GameGUI game = new GameGUI("asf","asf","asf",null, 3455, null, 5325, true, null);
	game.setVisible(true);
	}

	@Override
	public void run() {

		gameResult=startOnlineGame(clientGamePort,(String)opponentHost,i,b,theClient);
	
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (gameResult!=null)
		{
			//the game finished good
			return;
		}
		if ((red==null) || (blue==null))
		{
			gameResult=null;
			return;
		}
		if (gameThread!=null)
		{
			if (opponentSocket!=null){ try {
				opponentSocket.close();
				opponentSocket=null;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}}
			if (serverSocket!=null)
			{
				try {
					serverSocket.close();
					serverSocket=null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}
		
		
		
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


}
