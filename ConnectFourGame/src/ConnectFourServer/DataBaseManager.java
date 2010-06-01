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
	private static Integer lock;
	
	static
	{
		dbName="db";
		userName="root";
		password="123456";
		url="jdbc:mysql://localhost";
		lock=new Integer(0);
	}
	
	private static void  constructTables() throws SQLException
	{
		Connection conn=null;
		Statement statment=null;
		try{
		String users="CREATE  TABLE IF NOT EXISTS `db`.`users` " +
				"(`username` VARCHAR(100) NOT NULL ," +
				"`password` VARCHAR(100) NOT NULL ," +
				"PRIMARY KEY (`username`) );";
		 statment= conn.createStatement(); 
		 statment.executeUpdate(users); 
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
		synchronized (lock) {
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
		synchronized (lock) {
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
	
}
