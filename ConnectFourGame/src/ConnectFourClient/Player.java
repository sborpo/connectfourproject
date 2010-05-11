package ConnectFourClient;

public class Player {

	public static enum Color {
		BLUE, RED
	};

	private Color playerCol;

	public Player(Color col) {
		playerCol = col;
	}

	public Color getColor() {
		return playerCol;
	}

}
