package ConnectFourServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class DataBaseManager {

	public static class UserAlreadyExists extends Exception{}
	private static String dbName ; 
	private static String userName;
	private static String password;
	private static String url;
	private static Integer userslock;
	private static Integer gameslock;
	
	static
	{
		dbName="db";
		userName="root";
		password="123456";
		url="jdbc:mysql://localhost";
		userslock=new Integer(0);
		gameslock=new Integer(0);
	}
	
	public static void  constructTables() throws SQLException
	{
		Connection conn=getConnection();
		Statement statment=null;
		try{
		String users="CREATE  TABLE IF NOT EXISTS `db`.`users` " +
				"(`username` VARCHAR(100) NOT NULL ," +
				"`password` VARCHAR(100) NOT NULL ," +
				"PRIMARY KEY (`username`) );";
		 statment= conn.createStatement(); 
		 statment.executeUpdate(users); 
		 
		 String games="CREATE  TABLE IF NOT EXISTS `db`.`games` " +
		 		"(`gameid` VARCHAR(250) NOT NULL " +
		 		",`user1` VARCHAR(45) NOT NULL " +
		 		",`user2` VARCHAR(45) NOT NULL " +
		 		",`user1rep` VARCHAR(45) NULL " +
		 		",`user2rep` VARCHAR(45) NULL " +
		 		",PRIMARY KEY (`gameid`) );";
		 statment.executeUpdate(games); 
		}
		finally
		{
			if (conn!=null){conn.close();}
		}
		
	}
	
	
	
	
	private static Connection getConnection()
	{
		try {
			return DriverManager.getConnection (url+"/"+dbName, userName, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}
	
	
	public static boolean authenticateUser(String username,String password) throws SQLException
	{
		Connection conn=null;
		try{
		conn = getConnection();
		String query= "SELECT * FROM users WHERE username=? AND password=?";
		PreparedStatement prepareStatement = conn.prepareStatement(query);
		prepareStatement.setString(1,username);
		prepareStatement.setString(2,password);
		synchronized (userslock) {
			return rowExists(prepareStatement);
		}
		}
		finally
		{
			if (conn!=null){conn.close();}
		}
	}
	
	
	private static boolean rowExists(PreparedStatement st) throws SQLException
	{
		ResultSet set = st.executeQuery();
		if (set.first()==false)
		{
			return false;
		}
		return true;
	}
	
	private static boolean checkUserExists(String username,Connection conn) throws SQLException
	{
			String query= "SELECT * FROM users WHERE username=?";
			PreparedStatement prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,username);
			return rowExists(prepareStatement);
	}
		
	public static void insertUser(String username,String password) throws SQLException, UserAlreadyExists
	{
		Connection conn=null;
		try{
		conn = getConnection();
		String query= "INSERT INTO users VALUES(?,?);";
		PreparedStatement prepareStatement = conn.prepareStatement(query);
		prepareStatement.setString(1,username);
		prepareStatement.setString(2,password);
		synchronized (userslock) {
			if (!checkUserExists(username, conn))
			{
				prepareStatement.execute();
			}
			throw new UserAlreadyExists();	
		}
		}
		finally
		{
			if (conn!=null){conn.close();}
		}
	}
	
	public static void createGame(String username1,String username2,String gameId) throws SQLException
	{
		Connection conn=null;
		try{
		conn = getConnection();
		String query= "INSERT INTO games VALUES(?,?,?,NULL,NULL);";
		PreparedStatement prepareStatement = conn.prepareStatement(query);
		prepareStatement.setString(1,gameId);
		prepareStatement.setString(2,username1);
		prepareStatement.setString(3,username2);
		synchronized (gameslock) {
				prepareStatement.execute();
		}
		}
		finally
		{
			if (conn!=null){conn.close();}
		}	
		
	}
	
	public static boolean areReportsTheSame(String gameId) throws SQLException
	{
		Connection conn=null;
		try{
		conn = getConnection();
		String query= "SELECT * FROM games WHERE gameid=? AND user1rep=user2rep AND user1rep IS NOT NULL;";
		PreparedStatement prepareStatement = conn.prepareStatement(query);
		prepareStatement.setString(1,gameId);
		return rowExists(prepareStatement);
		}
		finally
		{
			if (conn!=null){conn.close();}
		}	
		
	}
	
	public static void makeReport(String gameId,String username,String report) throws SQLException
	{
		Connection conn=null;
		try{
		conn = getConnection();
		conn.setAutoCommit(false);
		String []query= {"UPDATE games SET user1rep=? WHERE gameid=? AND user1=?;","UPDATE games SET user2rep=? WHERE gameid=? AND user2=?;"};
		synchronized (gameslock) {
		for (int i=0; i<query.length; i++)
		{
		PreparedStatement prepareStatement = conn.prepareStatement(query[i]);
		prepareStatement.setString(1,report);
		prepareStatement.setString(2,gameId);
		prepareStatement.setString(3, username);
		
				prepareStatement.executeUpdate();
		}
		conn.commit();
		conn.setAutoCommit(true);
		}
		}
		catch (SQLException ex)
		{
			conn.rollback();
		}
		finally
		{
			if (conn!=null){conn.close();}
		}	
		
	}
	
	
	
}
