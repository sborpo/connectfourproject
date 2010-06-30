package gameManager;

import java.io.Serializable;

public class Board implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4739665432455995867L;

	public static class IllegalMove extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7438721808184643398L;
	}

	public static enum Cell implements Serializable{
		EMPTY, RED, BLUE
	}

	public static enum GameState implements Serializable{
		TIE, RED_WON, BLUE_WON, PROCEED , I_SURRENDED,OPPONENT_SURRENDED
	}

	private Cell[][] board;
	private int[] columnsFil;
	private int width;
	private int height;
	private int numOfSteps;
	private int winTokens = 4;

	public Board() {
		width = 7;
		height = 6;
		board = new Cell[height][width];
		columnsFil = new int[width];
		initBoard();
	}

	public void PrintBoard() {
		for (int i = height - 1; i >= 0; i--) {
			for (int j = 0; j < width; j++) {
				if (board[i][j].equals(Cell.BLUE)) {
					System.out.print("| L |");
					continue;
				}
				if (board[i][j].equals(Cell.RED)) {
					System.out.print("| R |");
					continue;
				}
				System.out.print("|   |");

			}
			System.out.println();		
			if (i==0)
			{
				for (int j = 0; j < width; j++)
				{
					System.out.print("-----");
				}
				System.out.println();
			}
		}
		for (int i=0; i<width; i++)
		{
			System.out.print("| "+i+" |");
		}
		System.out.println();
	}

	/**
	 * Initialize the whole board to be empty
	 */
	public void initBoard() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = Cell.EMPTY;
			}
		}

		for (int j = 0; j < columnsFil.length; j++) {
			columnsFil[j] = -1;
		}
		numOfSteps = 0;
	}

	public boolean isFull() {
		return (numOfSteps == width * height);
	}

	public GameState playColumn(int colNum, Player.Color color)
			throws IllegalMove {

		// Checks that the columns is not full
		if ((colNum < 0) || (colNum > (width - 1))
				|| ((columnsFil[colNum] + 1) == height)) {
			throw new IllegalMove();
		}
		columnsFil[colNum]++;
		if (color.equals(Player.Color.BLUE)) {
			board[columnsFil[colNum]][colNum] = Cell.BLUE;
		} else {
			board[columnsFil[colNum]][colNum] = Cell.RED;
		}
		numOfSteps++;
		return checkWinning(columnsFil[colNum], colNum, color);

	}

	private GameState checkWinning(int lastRow, int lastCol, Player.Color color) {
		Cell cellColor;
		GameState canWin;
		if (color.equals(Player.Color.BLUE)) {
			cellColor = Cell.BLUE;
			canWin = GameState.BLUE_WON;
		} else {
			cellColor = Cell.RED;
			canWin = GameState.RED_WON;
		}
		if (checkRow(lastRow, lastCol, cellColor)) {
			return canWin;
		}
		if (checkColumn(lastRow, lastCol, cellColor)) {
			return canWin;
		}
		if (checkRow(lastRow, lastCol, cellColor)) {
			return canWin;
		}
		if (checkDiagonals(lastRow, lastCol, cellColor)) {
			return canWin;
		}
		if (isFull()) {
			return GameState.TIE;
		}
		return GameState.PROCEED;
	}

	private boolean checkRow(int lastRow, int lastCol, Cell color) {
		int count = 1;
		for (int j = lastCol + 1; j < width; j++) {
			if (board[lastRow][j].equals(color)) {
				count++;
				continue;
			}
			break;
		}

		for (int j = lastCol - 1; j >= 0; j--) {
			if (board[lastRow][j].equals(color)) {
				count++;
				continue;
			}
			break;
		}
		if (count >= winTokens) {
			return true;
		}
		return false;

	}

	private boolean checkColumn(int lastRow, int lastCol, Cell color) {
		int count = 1;
		for (int i = lastRow + 1; i < height; i++) {
			if (board[i][lastCol].equals(color)) {
				count++;
				continue;
			}
			break;
		}

		for (int i = lastRow - 1; i >= 0; i--) {
			if (board[i][lastCol].equals(color)) {
				count++;
				continue;
			}
			break;
		}
		if (count >= winTokens) {
			return true;
		}
		return false;

	}

	private boolean checkDiagonals(int lastRow, int lastCol, Cell color) {
		int count = 1;
		int i, j;
		// check first diagonal
		for (i = lastRow + 1, j = lastCol + 1; ((i < height) && (j < width)); i++, j++) {
			if (board[i][j].equals(color)) {
				count++;
				continue;
			}
			break;
		}

		for (i = lastRow - 1, j = lastCol - 1; ((i >= 0) && (j >= 0)); i--, j--) {
			if (board[i][j].equals(color)) {
				count++;
				continue;
			}
			break;
		}
		if (count >= winTokens) {
			return true;
		}
		count = 1;

		// check second diagonal
		for (i = lastRow + 1, j = lastCol - 1; ((i < height) && (j >= 0)); i++, j--) {
			if (board[i][j].equals(color)) {
				count++;
				continue;
			}
			break;
		}

		for (i = lastRow - 1, j = lastCol + 1; ((i >= 0) && (j < width)); i--, j++) {
			if (board[i][j].equals(color)) {
				count++;
				continue;
			}
			break;
		}
		if (count >= winTokens) {
			return true;
		}
		return false;

	}

}
