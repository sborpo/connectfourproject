package common;

import java.io.Serializable;

public class UserStatistics implements Serializable{
	public UserStatistics(String username, int wins, int loses) {
		super();
		this.username = username;
		this.wins = wins;
		this.loses = loses;
	}
	
	public int getRank()
	{
		return wins-loses;
	}
	public String getUsername() {
		return username;
	}
	public int getWins() {
		return wins;
	}
	public int getLoses() {
		return loses;
	}
	String username;
	int wins;
	int loses;
	
}
