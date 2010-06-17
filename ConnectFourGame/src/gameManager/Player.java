package gameManager;

import java.io.Serializable;

import ConnectFourServer.OnlineClients.Client;

public class Player implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum Color implements Serializable {
		BLUE, RED
	};

	private Color playerCol;
	private String playerName;

	public Player(Color col,String name){ 
		playerName = name;
		playerCol = col;
	}

	public Color getColor() {
		return playerCol;
	}
	
	public String getName(){
		return playerName;
	}
}