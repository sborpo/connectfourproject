package ConnectFourClient;

public class Board {
	
	public static enum Cell{EMPTY, RED, BLUE}
	private Cell[][] board;
	
	/**
	 * Initialize the whole board to be empty
	 */
	public void initBoard()
	{
		for (Cell[] row : board) {
			for (Cell cell : row) {
				cell= Cell.EMPTY;
			}
		}
	}
	
	public void playColumn(int colNum,Player.Color color)
	{
		
	}
	

}
