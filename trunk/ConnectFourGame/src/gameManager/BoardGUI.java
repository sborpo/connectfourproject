package gameManager;
import gameManager.Board.Cell;
import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;
import gameManager.Player.Color;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

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

import javax.swing.JButton;
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
import java.lang.reflect.InvocationTargetException;

public class BoardGUI extends Board implements Serializable{


	public static class ConnectionBoxPrinter implements Runnable
	{
	
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
	
	public int[] getColumnsFil()
	{
		return columnsFil;
	}
	



	/**
	 * Initialize the whole board to be empty
	 */



	public boolean checkMove(int colNum)
	{
		if ((colNum < 0) || (colNum > (width - 1))
				|| ((columnsFil[colNum] + 1) == height)) {
			return false;
		}
		return true; 
	}

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


	public void markColNum(int colnum,Player.Color plays, JButton[][] slots2) {
		if (plays.equals(Player.Color.BLUE)) {
		slots2[columnsFil[colnum]+1][colnum].setBackground(java.awt.Color.BLUE);
		slots2[columnsFil[colnum]+1][colnum].repaint();
		}
		else
		{
			slots2[columnsFil[colnum]+1][colnum].setBackground(java.awt.Color.RED);
			slots2[columnsFil[colnum]+1][colnum].repaint();
		}
		
	}
}



