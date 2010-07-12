package common;

import java.io.Serializable;
import java.util.ArrayList;



public class StatsReport implements Serializable {
	public UserStatistics getCurrentUser() {
		return currentUser;
	}
	public void setCurrentUser(UserStatistics currentUser) {
		this.currentUser = currentUser;
	}
	public ArrayList<UserStatistics> getTopTen() {
		return topTen;
	}
	public void setTopTen(ArrayList<UserStatistics> topTen) {
		this.topTen = topTen;
	}
	UserStatistics currentUser;
	ArrayList<UserStatistics> topTen;
}
