package common;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Represent the statistics data for users.
 */
public class StatsReport implements Serializable {
	UserStatistics currentUser;
	ArrayList<UserStatistics> topTen;
	
	/**
	 * Gets the current user statistics.
	 * @return
	 */
	public UserStatistics getCurrentUser() {
		return currentUser;
	}
	
	/**
	 * Sets the current user statistics.
	 * @param currentUser
	 */
	public void setCurrentUser(UserStatistics currentUser) {
		this.currentUser = currentUser;
	}
	
	/**
	 * Gets the top ten users statistics data.
	 * @return
	 */
	public ArrayList<UserStatistics> getTopTen() {
		return topTen;
	}
	
	/**
	 * Sets the TOP ten users statistics data.
	 * @param topTen
	 */
	public void setTopTen(ArrayList<UserStatistics> topTen) {
		this.topTen = topTen;
	}
	
}
