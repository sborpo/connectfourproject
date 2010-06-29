package gameManager;

import javax.swing.*;
import java.awt.event.*;

public class ConnectFourListener implements MouseListener {

GameGUI game;
int nextMove; 

public ConnectFourListener(GameGUI game) {
	
	this.game = game;
	game.gameBoard.addListener(this);
}

public void mouseClicked(MouseEvent event) {
	System.out.println("so far so good1");
	JLabel label = (JLabel) event.getComponent();
	System.out.println("so far so good2");
	int column = game.gameBoard.getColumn(label);
	
	nextMove = column;
}

public void mousePressed(MouseEvent event) {
}

public void mouseReleased(MouseEvent event) {
}

public void mouseEntered(MouseEvent event) {
}

public void mouseExited(MouseEvent event) {
}
}