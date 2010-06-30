package gameManager;
import gameManager.Board.Cell;
import gameManager.Board.GameState;
import gameManager.Board.IllegalMove;

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

public class BoardGUI extends Board implements Serializable{


	JPanel boardPanel;
	private JButton[][] slots;

	GameGUI father;


	public BoardGUI(GameGUI father){
		boardPanel = new JPanel();
		width = 7;
		height = 6;
		board = new Cell[height][width];
		columnsFil = new int[width];
		initBoard();
		this.father=father;
		adjustBoard();
		boardPanel.setVisible(true);
	}
	
	

	public JPanel getPanel()
	{
		return boardPanel;
	}
	public void adjustBoard() {
		
		boardPanel.setLayout(new GridLayout(6,7));
		boardPanel.setBorder(new LineBorder(Color.black));
		slots = new JButton[6][7];
		for (int row=5; row>=0; row--) {
		for (int column=0; column<7; column++) {
			slots[row][column] = new JButton();
			slots[row][column].setBorder(new LineBorder(Color.black));
			slots[row][column].setName(String.valueOf(row)+" "+String.valueOf(column));
			slots[row][column].setHorizontalAlignment(SwingConstants.CENTER);
			slots[row][column].addMouseListener(father);
			
			boardPanel.add(slots[row][column]);
		}
		boardPanel.setSize(700,600);
	}
		
		
		
//		for (int i = height - 1; i >= 0; i--) {
//			for (int j = 0; j < width; j++) {
//				if (board[i][j].equals(Cell.BLUE)) {
//					//System.out.print("| B |");
//					set(j,i,"BLUE");
//					continue;
//				}
//				if (board[i][j].equals(Cell.RED)) {
//					//System.out.print("| R |");
//					set(j,i,"RED");
//					continue;
//				}
				//System.out.print("| 0 |");

//			}
			//System.out.println();		
//			if (i==0)
//			{
//				for (int j = 0; j < width; j++)
//				{
//					System.out.print("-----");
//				}
//				System.out.println();
//			}
//		}
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
			slots[columnsFil[colNum]][colNum].setBackground(Color.BLUE);
			slots[columnsFil[colNum]][colNum].repaint();
		} else {
			board[columnsFil[colNum]][colNum] = Cell.RED;
			slots[columnsFil[colNum]][colNum].setBackground(Color.RED);
			slots[columnsFil[colNum]][colNum].repaint();
		}
		numOfSteps++;
		return checkWinning(columnsFil[colNum], colNum, color);

	}
}



