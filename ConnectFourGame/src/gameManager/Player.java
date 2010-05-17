package gameManager;

import ConnectFourServer.OnlineClients.Client;

public class Player {

	public static enum Color {
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