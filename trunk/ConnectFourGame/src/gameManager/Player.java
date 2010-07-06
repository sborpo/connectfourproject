package gameManager;

import java.io.Serializable;

import common.Timer;
import common.Timer.TimerListener;

public class Player implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum Color implements Serializable {
		BLUE, RED
	};

	private Color playerCol;
	private String playerName;
	private Timer timer;

	public Player(Color col,String name){ 
		playerName = name;
		playerCol = col;
		timer = null;
	}

	public Color getColor() {
		return playerCol;
	}
	
	public String getName(){
		return playerName;
	}
	
	public Timer setTimer(int moveTime,TimerListener lst){
		if(timer == null){
			timer = new Timer(moveTime,lst);
		}
		else{
			timer.pause();
			timer.reset();
		}
		return timer;
	}
	
	public Timer getTimer(){
		return timer;
	}
	
	public void unsetTimer(){
		timer = null;
	}

}