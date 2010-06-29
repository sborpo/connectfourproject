package gameManager;
import java.awt.BorderLayout;
import theProtocol.*;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.awt.Image;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import ConnectFourClient.SingUpWindow;


import java.io.Serializable;

public class BoardGUI extends JDialog implements Serializable{


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
		TIE, RED_WON, BLUE_WON, PROCEED
	}

	private Cell[][] board;
	private JFrame frame;
	private JLabel[][] slots;
	private int[] columnsFil;
	private int width;
	private int height;
	private int numOfSteps;
	private int winTokens = 4;

	public BoardGUI(){
		width = 7;
		height = 6;
		board = new Cell[height][width];
		columnsFil = new int[width];
		initBoard();
	}
	
	public void PrintBoard() {
		
		frame = new JFrame("Connect Four Game");
		JPanel panel = (JPanel) frame.getContentPane();
		panel.setLayout(new GridLayout(6,7));
		slots = new JLabel[7][6];
		for (int row=5; row>=0; row--) {
		for (int column=0; column<7; column++) {
		slots[column][row] = new JLabel();
		slots[column][row].setHorizontalAlignment(SwingConstants.CENTER);
		slots[column][row].setBorder(new LineBorder(Color.black));
		panel.add(slots[column][row]);

		
		}
		}
		frame.setContentPane(panel);
		frame.setSize(700,600);
		frame.setVisible(true);
		
		
		
		for (int i = height - 1; i >= 0; i--) {
			for (int j = 0; j < width; j++) {
				if (board[i][j].equals(Cell.BLUE)) {
					//System.out.print("| B |");
					set(j,i,"BLUE");
					continue;
				}
				if (board[i][j].equals(Cell.RED)) {
					//System.out.print("| R |");
					set(j,i,"RED");
					continue;
				}
				//System.out.print("| 0 |");

			}
			//System.out.println();		
//			if (i==0)
//			{
//				for (int j = 0; j < width; j++)
//				{
//					System.out.print("-----");
//				}
//				System.out.println();
//			}
		}
//		for (int i=0; i<width; i++)
//		{
//			System.out.print("| "+i+" |");
//		}
//		System.out.println();
//	
	}

	/**
	 * Initialize the whole board to be empty
	 */

	public void addListener(ConnectFourListener listener) {
		for (int row=0; row<6; row++) {
		for (int column=0; column<7; column++) {
		slots[column][row].addMouseListener(listener);
		}
		}
		}
	
	public int getColumn(JLabel label) {
		
		int returnColumn = -1;
		for (int row=0; row<6; row++) {
			for (int column=0; column<7; column++) {
				if (slots[column][row] == label) {
					returnColumn = column;
				}
			}
			}
		return returnColumn;
		}
	
	public void set(int column, int row, String str) {
		if (str.equals("BLUE")) {
//			ImageIcon ii = new ImageIcon ("gameManager.BlueBall.jpeg");
//			slots[column][row].setIcon(ii);
			slots[column][row].setText("*BLUE*");
		    
		}
		else {
			if (str.equals("RED")){
//				ImageIcon ii = new ImageIcon ("gameManager.RedBall.jpeg");
//				slots[column][row].setIcon(ii);
				slots[column][row].setText("*RED*");
			}
		}
		}


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
