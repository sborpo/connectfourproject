package gameManager;
import gameManager.Player.Color;

import javax.swing.JButton;
import javax.swing.JLabel;

import java.io.Serializable;

/**
 * This class represents the GUI components and view of the game board
 *
 */
public class BoardGUI extends Board implements Serializable{


	/**
	 *This class handles the connection box writing (where we present the current
	 *players of the game) the EDT runs this operation
	 *
	 */
	public static class ConnectionBoxPrinter implements Runnable
	{
	
		/**
		 * Prints the current player names of the game , including their colors
		 * @param printLabel
		 * @param printLabe2
		 * @param playerName1
		 * @param player1Col
		 * @param playerName2
		 * @param player2Col
		 */
		public ConnectionBoxPrinter(JLabel printLabel, JLabel printLabe2,
				String playerName1, Color player1Col, String playerName2,
				Color player2Col) {
			super();
			this.printLabel = printLabel;
			this.printLabe2 = printLabe2;
			this.playerName1 = playerName1;
			this.player1Col = player1Col;
			this.playerName2 = playerName2;
			this.player2Col = player2Col;
		}

		JLabel printLabel;
		JLabel printLabe2;
		String playerName1;
		Color player1Col;
		String playerName2;
		Color player2Col;
	
		@Override
		public void run() {
			printLabel.setText("Connected As: "+playerName1);
			printLabel.setForeground(player1Col.equals(Color.RED) ? java.awt.Color.RED : java.awt.Color.BLUE);
			printLabe2.setText("Playing Against: "+playerName2);
			printLabe2.setForeground(player2Col.equals(Color.RED) ? java.awt.Color.RED : java.awt.Color.BLUE);
			
			
			
		}
		
	}
	
	/**
	 * Prints the given text in the given JLable on the EDT 
	 * @author Boris
	 *
	 */
	public static class MessagePrinter implements Runnable
	{
		public MessagePrinter(JLabel printLabel, String text) {
			super();
			this.printLabel = printLabel;
			this.text = text;
		}
		JLabel printLabel;
		String text;
		@Override
		public void run() {
			printLabel.setText(text);	
		}
		
	}
	
	/**
	 * This class paints the board according to the players move
	 * It uses the given Jbutton[][] as cells
	 */
	public static class Painter implements Runnable
	{
		public Painter(int[] colFil, int col, Color plays, JButton[][] slots) {
			super();
			this.columnsFil = colFil;
			this.colNum = col;
			this.plays = plays;
			this.slots = slots;
		}
		int[] columnsFil;
		int colNum;
		Player.Color plays;
		JButton [] [] slots;
		@Override
		public void run() {
			if (plays.equals(Player.Color.BLUE)) {
				slots[columnsFil[colNum]][colNum].setBackground(java.awt.Color.BLUE);
				slots[columnsFil[colNum]][colNum].repaint();
				}
				else
				{
					slots[columnsFil[colNum]][colNum].setBackground(java.awt.Color.RED);
					slots[columnsFil[colNum]][colNum].repaint();
				}
			
		}
		
	}




	public BoardGUI(GameGUI father){
		width = 7;
		height = 6;
		board = new Cell[height][width];
		columnsFil = new int[width];
		initBoard();
	
		
	}
	
	/**
	 * Returns the current column fill array
	 * @return
	 */
	public int[] getColumnsFil()
	{
		return columnsFil;
	}
	



	/**
	 * Checks if the given move of the column is legal. (if the column is not full
	 * and in the borders)
	 * @param colNum
	 * @return
	 */
	public boolean checkMove(int colNum)
	{
		if ((colNum < 0) || (colNum > (width - 1))
				|| ((columnsFil[colNum] + 1) == height)) {
			return false;
		}
		return true; 
	}

	/**
	 * Like Board.playColumn , but also paints the cells on success
	 * @param colNum
	 * @param color
	 * @param slots
	 * @return
	 * @throws IllegalMove
	 */
	public GameState playColumn(int colNum, Player.Color color,JButton[][] slots)
			throws IllegalMove {

		// Checks that the columns is not full
		if ((colNum < 0) || (colNum > (width - 1))
				|| ((columnsFil[colNum] + 1) == height)) {
			throw new IllegalMove();
		}
		columnsFil[colNum]++;
		if (color.equals(Player.Color.BLUE)) {
			board[columnsFil[colNum]][colNum] = Cell.BLUE;
			slots[columnsFil[colNum]][colNum].setBackground(java.awt.Color.BLUE);
			slots[columnsFil[colNum]][colNum].repaint();
		} else {
			board[columnsFil[colNum]][colNum] = Cell.RED;
			slots[columnsFil[colNum]][colNum].setBackground(java.awt.Color.RED);
			slots[columnsFil[colNum]][colNum].repaint();
		}
		numOfSteps++;
		return checkWinning(columnsFil[colNum], colNum, color);

	}
}



