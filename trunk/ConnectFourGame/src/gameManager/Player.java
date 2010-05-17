package gameManager;

import ConnectFourServer.OnlineClients.Client;

public class Player {

	public static enum Color {
		BLUE, RED
	};

	private Color playerCol;
	private Client client;

	public Player(Color col,Client theClient) {
		client = theClient;
		playerCol = col;
	}

	public Color getColor() {
		return playerCol;
	}

	public Client getClient(){
		return client;
	}
}
