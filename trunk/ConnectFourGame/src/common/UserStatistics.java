package common;

import java.io.Serializable;

/**
 * Represents user statistics.
 */
public class UserStatistics implements Serializable{
	String username;
	int wins;
	int loses;
	
	
	/**
	 * Constructor for user statistics class.
	 * @param username
	 * @param wins
	 * @param loses
	 */
	public UserStatistics(String username, int wins, int loses) {
		super();
		this.username = username;
		this.wins = wins;
		this.loses = loses;
	}
	
	/**
	 * Gets the rank of the player.
	 * @return the difference between wins and loses
	 */
	public int getRank()
	{
		return wins-loses;
	}
	
	/**
	 * Gets user name.
	 * @return username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Gets the number of wins.
	 * @return wins
	 */
	public int getWins() {
		return wins;
	}
	
	/**
	 * Gets the number of loses.
	 * @return loses
	 */
	public int getLoses() {
		return loses;
	}
}
