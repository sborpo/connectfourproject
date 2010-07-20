package gameManager;

import java.io.Serializable;

import common.Timer;
import common.Timer.TimerListener;

/**
 * This class represents a player of the game
 *
 */
public class Player implements Serializable{
	
	private static final long serialVersionUID = 1L;

	/**
	 * Represents the players colors
	 *
	 */
	public static enum Color implements Serializable {
		BLUE, RED;
		
		public String getColorStr()
		{
			if (this.equals(BLUE))
			{
				return "blue";
			}
			else
			{
				return "red";
			}
		}
		
		
	};

	//the player color
	private Color playerCol;
	//the player name
	private String playerName;
	//the player timer
	private Timer timer;

	public Player(Color col,String name){ 
		playerName = name;
		playerCol = col;
		timer = null;
	}
	
	
	/**
	 * Returns the color of the player
	 * @return
	 */
	public Color getColor() {
		return playerCol;
	}
	
	/**
	 * Returns player's name
	 * @return
	 */
	public String getName(){
		return playerName;
	}
	
	/**
	 * Sets player's timer with the Object which is listening to this timer
	 * @param moveTime
	 * @param lst
	 * @return
	 */
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
	
	/**
	 * Returns players timer
	 * @return
	 */
	public Timer getTimer(){
		return timer;
	}
	
	/**
	 * Unsets players timer
	 */
	public void unsetTimer(){
		timer = null;
	}

}