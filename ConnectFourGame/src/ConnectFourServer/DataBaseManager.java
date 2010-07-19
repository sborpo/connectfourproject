package ConnectFourServer;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import common.PasswordHashManager;
import common.RSAgenerator;
import common.StatsReport;
import common.UnhandeledReport;
import common.UnhandledReports;
import common.UnhandledReports.FileChanged;
import common.UnhandledReports.NoReports;
import common.UserStatistics;
import common.PasswordHashManager.SystemUnavailableException;



/**
 * This class handles the communitcation with 
 * the databse using JDBC.
 */
public class DataBaseManager {



	/**
	 * 
	 * Thrown where trying to add a user which already exists in the system
	 *
	 */
	public static class UserAlreadyExists extends Exception{		
		public String getMessage(){
			return "User is already exists in the database";
		}
	}
	
	/**
	 * Thrown when trying to add a gameid which already exists
	 *
	 */
	public static class GameIdAlreadyExists extends Exception{
		public String getMessage(){
			return "The gameId is already exists in the database";
		}
	}
	
	/**
	 * Thrown when the gameId not exists
	 *
	 */
	public static class GameIdNotExists extends Exception{
		public String getMessage(){
			return "The gameId is already exists in the database";
		}
	}
	
	//These fields are for connecting to the database
	private static String dbName ; 
	private static String userName;
	private static String password;
	private static String url;
	private static Integer userslock;
	private static Integer gameslock;
	
	/**
	 * Initialiaze the parameters for connecting the database
	 */
	static
	{
		dbName="db";
		userName="root";
		password="123456";
		url="jdbc:mysql://localhost/";
		userslock=new Integer(0);
		gameslock=new Integer(0);
	}
	
	/**
	 * This method moves the unhandled user reports from the server reports file
	 * into the databse. (The unhandeled reports were saved in the file because the
	 * database was not available
	 */
	public static void treatUnhandeledReports()
	{
		try {
			UnhandledReports reports = new UnhandledReports(MainServer.ReportFileName);
			if (reports.getReportNumber()==0)
			{
				return;
			}
			for (UnhandeledReport report : reports.getUnhandeledReports()) {
				try {
					makeReport(report.getGameId(), report.getClientName(), report.getWinner());
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GameIdNotExists e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			reports.removeReportsFile();
		} catch (NoReports e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileChanged e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * initializes the database name
	 * @param dbName
	 */
	public static void initDBname(String dbName){
		DataBaseManager.dbName = dbName;
	}
	
	/**
	 * @return database name
	 */
	public static String getDBname(){
		return DataBaseManager.dbName;
	}
	
	/**
	 * creates a new database
	 * @param dbName
	 * @throws SQLException
	 */
	public static void createDB(String dbName) throws SQLException{
		DataBaseManager.dbName = dbName;
		Connection conn=getConnection("");
		String query = "CREATE DATABASE IF NOT EXISTS `" + DataBaseManager.dbName +"`";
		Statement statement = null;
		try {
			statement = conn.createStatement();
			//prepareStatement.setString(1,"`" + dbName + "`");
			statement.executeUpdate(query);
		} 
		finally
		{
			if(statement!=null){statement.close();}
			if (conn!=null){conn.close();}
		}
		
	}
	
	/**
	 * construct the tables of the database
	 * @throws SQLException
	 */
	public static void  constructTables() throws SQLException
	{
		Connection conn=getConnection(DataBaseManager.dbName);
		Statement statment=null;
		try{
		String tableName = "`" + DataBaseManager.dbName + "`.`users`";
		String users="CREATE TABLE IF NOT EXISTS " + tableName +
				"(`username` VARCHAR(100) NOT NULL ," +
				"`password` VARCHAR(100) NOT NULL ," +
				"PRIMARY KEY (`username`) );";
		 statment= conn.createStatement();
		 //statment.setString(1,"`database`.`users`");
		 statment.executeUpdate(users); 
		 
		 tableName = "`" + DataBaseManager.dbName + "`.`games`";
		 String games="CREATE  TABLE IF NOT EXISTS " + tableName +
		 		"(`gameid` VARCHAR(250) NOT NULL " +
		 		",`user1` VARCHAR(45) NOT NULL " +
		 		",`user2` VARCHAR(45) NOT NULL " +
		 		",`user1rep` VARCHAR(45) NULL " +
		 		",`user2rep` VARCHAR(45) NULL " +
		 		",PRIMARY KEY (`gameid`) );";
		 //statment= conn.prepareStatement(games);
		 //statment.setString(1,"`" + DataBaseManager.dbName + "`.`games`");
		 statment.executeUpdate(games); 
		 
		 tableName = "`" + DataBaseManager.dbName + "`.`stats`";
		 String stats="CREATE  TABLE IF NOT EXISTS " + tableName +" " +
		 		"(`username` varchar(100) NOT NULL," +
		 		"`wins` int(11) NOT NULL," +
		 		"`loses` int(11) NOT NULL," +
		 		"PRIMARY KEY (`username`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		 statment.executeUpdate(stats); 
		 
		
		}
		finally
		{
			if(statment!=null){statment.close();}
			if (conn!=null){conn.close();}
		}
		
	}
	
	/**
	 * Returns a new Connection to the database 
	 * through which other methods can execute queires
	 */
	public static Connection getConnection(String dbName)
	{
		try {
			//Class.forName("org.h2.Driver");
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection (url+dbName, userName, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null; 
	}
	
	/**
	 * Hashes a given password using the PasswordHashManager
	 * The hash is nessary to maintain basic security
	 * @param pass
	 * @return
	 * @throws SystemUnavailableException
	 */
	private static String hashPassword(String pass) throws SystemUnavailableException{
		String hashed = null;
		PasswordHashManager hashManager = PasswordHashManager.getInstance();
		hashed = hashManager.encrypt(pass);
		return hashed;
	}
	
	/**
	 * This method authenticates the given user with it's previous 
	 * details in the database. if they match , returns true , otherwise
	 * return false.
	 * @param username
	 * @param password
	 * @return
	 * @throws SQLException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 * @throws SystemUnavailableException
	 */
	public static boolean authenticateUser(String username,String password) throws SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, SystemUnavailableException
	{
		String decrypted = RSAgenerator.decrypt(password);
		String hashedPswd = DataBaseManager.hashPassword(decrypted);
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		try{
			System.out.println("Trying to authenticate: "+username + " PASS: '" + hashedPswd + "'");
			conn = getConnection(DataBaseManager.dbName);
			String query= "SELECT * FROM users WHERE username=? AND password=?";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,username);
			prepareStatement.setString(2,hashedPswd);
			synchronized (userslock) {
				return rowExists(prepareStatement);
			}
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}
	}
	
	/**
	 * Executes the prepared stamtement query on the database, if some row has returned
	 * the method returns true, otherwise if empty set returned , the method returns false
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	private static boolean rowExists(PreparedStatement st) throws SQLException
	{
		ResultSet set = st.executeQuery();
		if (set.first()==false)
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Returns true if the given clientName exists , false otherwise
	 * @param clientName
	 * @return
	 * @throws SQLException
	 */
	public static boolean isUserExists(String clientName) throws SQLException{
		Connection conn= getConnection(DataBaseManager.dbName);
		return checkUserExists(clientName,conn);
	}
	
	/**
	 * 
	 * @param username
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private static boolean checkUserExists(String username,Connection conn) throws SQLException
	{
			String query= "SELECT * FROM users WHERE username=?";
			PreparedStatement prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,username);
			return rowExists(prepareStatement);
	}
	
	/**
	 * Retunrs true if the gameId exists  , otherwise returns false
	 * @param gameId
	 * @return
	 * @throws SQLException
	 */
	public static boolean isGameIdExists(String gameId) throws SQLException{
		if (gameId==null)
		{
			return false;
		}
		Connection conn= getConnection(DataBaseManager.dbName);
		return checkGameIdExists(gameId,conn);
	}
	
	/**
	 * Returns true if the given game exists , otherwise returns false.
	 * this is an auxilary function which uses a given connection
	 * @param gameId
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	private static boolean checkGameIdExists(String gameId,Connection conn) throws SQLException
	{
			String query= "SELECT * FROM games WHERE gameid=?";
			PreparedStatement prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,gameId);
			return rowExists(prepareStatement);
	}
	
	/**
	 * Returns true if the given client name was a player in the given gameId ,
	 * otherwise returns false
	 * @param clientName
	 * @param gameId
	 * @return
	 * @throws SQLException
	 */
	public static boolean isClientPlayedGame(String clientName, String gameId) throws SQLException{

		Connection conn= getConnection(DataBaseManager.dbName);
		String query= "SELECT * FROM games WHERE gameid=? AND (user1 =? OR user2 =?)";
		PreparedStatement prepareStatement = conn.prepareStatement(query);
		prepareStatement.setString(1,gameId);
		prepareStatement.setString(2,clientName);
		prepareStatement.setString(3,clientName);
		return rowExists(prepareStatement);
	}
	
	
	
	/**
	 * This method Inserts a new username with a given password into the system, if the 
	 * username already exists , it throws an exception	
	 * @param username
	 * @param password
	 * @throws SQLException
	 * @throws UserAlreadyExists
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 * @throws SystemUnavailableException
	 */
	public static void insertUser(String username,String password) throws SQLException, UserAlreadyExists, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, SystemUnavailableException
	{
		String decrypted = RSAgenerator.decrypt(password);
		String hashedPswd = DataBaseManager.hashPassword(decrypted);
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		PreparedStatement preparedStatementStas= null;
		try{
			conn = getConnection(DataBaseManager.dbName);
			conn.setAutoCommit(false);
			String query= "INSERT INTO users VALUES(?,?);";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,username);
			prepareStatement.setString(2,hashedPswd);
			query= "INSERT INTO stats VALUES(?,?,?);";
			preparedStatementStas=conn.prepareStatement(query);
			preparedStatementStas.setString(1,username);
			preparedStatementStas.setInt(2,0);
			preparedStatementStas.setInt(3,0);
			synchronized (userslock) {
				if (!checkUserExists(username, conn))
				{
					System.out.println("Inserting....");
					prepareStatement.execute();
					preparedStatementStas.execute();
					conn.commit();
				}
				else{
					System.out.println("Exists....");
					throw new UserAlreadyExists();
				}
				
			}
		
		}
		catch (SQLException ex)
		{
			conn.rollback();
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if(preparedStatementStas!=null){preparedStatementStas.close();}
			if (conn!=null){conn.close();}
		}

	}
	
	/**
	 * Creates a new game with the given gameId , and the two given
	 * players , if the gameId already exists  , it throws exception
	 * @param username1
	 * @param username2
	 * @param gameId
	 * @throws SQLException
	 * @throws GameIdAlreadyExists
	 */
	public static void createGame(String username1,String username2,String gameId) throws SQLException, GameIdAlreadyExists
	{
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		try{
			conn = getConnection(DataBaseManager.dbName);
			String query= "INSERT INTO games VALUES(?,?,?,NULL,NULL);";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,gameId);
			prepareStatement.setString(2,username1);
			prepareStatement.setString(3,username2);
			synchronized (gameslock) {
					if(!checkGameIdExists(gameId,conn)){
						prepareStatement.execute();
					}
					else{
						throw new GameIdAlreadyExists();
					}
			}
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}	
		
	}
	
	/**
	 * Checks if the user-reports about the result of the given gameId are the same,
	 * if they are the same , it returns true, otherwise returns false.
	 * comment : if someone of the players not reported yet , the function returns false
	 * @param gameId
	 * @return
	 * @throws SQLException
	 */
	public static boolean areReportsTheSame(String gameId) throws SQLException
	{
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		try{
			conn = getConnection(DataBaseManager.dbName);
			String query= "SELECT * FROM games WHERE gameid=? AND NOT (user1rep=user2rep) AND user1rep IS NOT NULL AND user2rep IS NOT NULL;";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,gameId);
			return (true == rowExists(prepareStatement)) ? false : true;
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}	
		
	}
	
	/**
	 * Retuns a StatsReport object which contrains the statistics of the top ten users
	 * of the system according to their rank. the rank is winw-loses.
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public synchronized static StatsReport getTopTenUsers(String username) throws SQLException
	{
		treatUnhandeledReports();
		Connection conn=null;
		ResultSet set=null;
		PreparedStatement prepareStatement = null;
		StatsReport report= new StatsReport();
		ArrayList<UserStatistics> users = new ArrayList<UserStatistics>();
		try{
			conn = getConnection(DataBaseManager.dbName);
			String query= "SELECT username,wins,loses ,(wins-loses) AS score FROM stats ORDER BY score";
			prepareStatement = conn.prepareStatement(query);
			set=prepareStatement.executeQuery();
			int i=0;
			while (set.next())
			{
				if (i<10)
				{
					users.add(new UserStatistics(set.getString("username"),set.getInt("wins"), set.getInt("loses")));
				}
				if (set.getString("username").equals(username))
				{
					report.setCurrentUser(new UserStatistics(set.getString("username"),set.getInt("wins"), set.getInt("loses")));
				}
			}
			report.setTopTen(users);
			return report;
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}	

	}
	
	
	/**
	 * Make a user report about game result. The report is from the given username which has to be
	 * a memeber of the given game (given in the gameId) and the game result which is given in the report
	 * parameter
	 * @param gameId
	 * @param username
	 * @param report
	 * @throws SQLException
	 * @throws GameIdNotExists
	 */
	public static void makeReport(String gameId,String username,String report) throws SQLException, GameIdNotExists
	{
		
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		try{
			conn = getConnection(DataBaseManager.dbName);
			if(checkGameIdExists(gameId, conn)){			
				conn.setAutoCommit(false);
				String []query= {"UPDATE games SET user1rep=? WHERE gameid=? AND user1=?;","UPDATE games SET user2rep=? WHERE gameid=? AND user2=?;"};
				synchronized (gameslock) {
					for (int i=0; i<query.length; i++)
					{
						prepareStatement = conn.prepareStatement(query[i]);
						prepareStatement.setString(1,report);
						prepareStatement.setString(2,gameId);
						prepareStatement.setString(3, username);
						prepareStatement.executeUpdate();
					}
					conn.commit();
					conn.setAutoCommit(true);
				}
			}
			else{
				throw new GameIdNotExists();
			}
		}
		catch (SQLException ex)
		{
			conn.rollback();
			throw ex;
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}	
	}
	
	
	/**
	 * Removes the given gameId from the system
	 * @param gameId
	 * @throws SQLException
	 * @throws GameIdNotExists
	 */
	public static void  removeGame(String gameId) throws SQLException, GameIdNotExists
	{
	
		Connection conn=getConnection(DataBaseManager.dbName);
		if(!checkGameIdExists(gameId, conn)){	
			throw new GameIdNotExists();
		}
		Statement statment=null;
		try{
			String tableName = "`" + DataBaseManager.dbName + "`.`games`";
			String users="DELETE FROM " + tableName +
					     " WHERE gameid='"+gameId+"';";
			 statment= conn.createStatement();
			 //statment.setString(1,"`database`.`users`");
			 statment.executeUpdate(users); 
			 
		}
		finally
		{
			if(statment!=null){statment.close();}
			if (conn!=null){conn.close();}
		}
		
	}
	
	
	//Liat && Gabby
	//*******************************************************
	public static boolean isReported(String gameId) throws SQLException
	{
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		try{
			conn = getConnection(DataBaseManager.dbName);
			String query= "SELECT * FROM games WHERE gameid=? AND user1rep IS NULL AND user2rep IS NULL;";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,gameId);
			// false represent that rep#id is null
			return (true == rowExists(prepareStatement)) ? true : false;
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}	
		
	}

	public static String [] getPlayersName (String gameId)throws SQLException{
		String [] usernames = new String [2];
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		try{
			conn = getConnection(DataBaseManager.dbName);
			String query= "SELECT User1 FROM games WHERE gameid=?;";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,gameId);
			usernames[0] = query;
				query= "SELECT User2 FROM games WHERE gameid=?;";
				prepareStatement = conn.prepareStatement(query);
				prepareStatement.setString(1,gameId);
				usernames[1] = query;
		}
		catch (SQLException ex)
		{
			conn.rollback();
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}
		return usernames;
	}
	
	public static void addToStats(String gameId,String report)throws SQLException
	{
		String usernames[] = new String[2];
		usernames = getPlayersName(gameId);
		String username1 = usernames[0];
		String username2 = usernames[1];
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		try{
			conn = getConnection(DataBaseManager.dbName);
			conn.setAutoCommit(false);
			if(username1.equals(report))
			{		
					String []query= {"UPDATE stats SET wins = wins+1 WHERE username=?;"};
					synchronized (gameslock) {
						for (int i=0; i<query.length; i++)
						{
							prepareStatement = conn.prepareStatement(query[i]);
							prepareStatement.setString(1,username1);
							prepareStatement.executeUpdate();
						}
						conn.commit();
						conn.setAutoCommit(true);
					}
					String []query1= {"UPDATE stats SET loses = loses +1 WHERE username=?;"};
					synchronized (gameslock) {
						for (int i=0; i<query.length; i++)
						{
							prepareStatement = conn.prepareStatement(query1[i]);
							prepareStatement.setString(1,username2);
							prepareStatement.executeUpdate();
						}
						conn.commit();
						conn.setAutoCommit(true);
					}
			}
			else{ //in case the username lost
				String []query= {"UPDATE stats SET loses = loses +1 WHERE username=?;"};
				synchronized (gameslock) {
					for (int i=0; i<query.length; i++)
					{
						prepareStatement = conn.prepareStatement(query[i]);
						prepareStatement.setString(1,username1);
						prepareStatement.executeUpdate();
					}
					conn.commit();
					conn.setAutoCommit(true);
				}
				String []query1= {"UPDATE stats SET wins = wins +1 WHERE username=?;"};
				synchronized (gameslock) {
					for (int i=0; i<query.length; i++)
					{
						prepareStatement = conn.prepareStatement(query1[i]);
						prepareStatement.setString(1,username2);
						prepareStatement.executeUpdate();
					}
					conn.commit();
					conn.setAutoCommit(true);
				}
			}
		}
		catch (SQLException ex)
		{
			conn.rollback();
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}	
		
	}
	
	public static String getReport(String gameId, int userR) throws SQLException{
		String report = "";
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		try{
			if (userR == 1){
				conn = getConnection(DataBaseManager.dbName);
				String query= "SELECT user1rep FROM games WHERE gameid=?;";
				prepareStatement = conn.prepareStatement(query);
				prepareStatement.setString(1,gameId);
				return query;
			}
			else if (userR == 2)
			{
				conn = getConnection(DataBaseManager.dbName);
				String query= "SELECT user2rep FROM games WHERE gameid=?;";
				prepareStatement = conn.prepareStatement(query);
				prepareStatement.setString(1,gameId);
				return query;
			}
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}	

		return report;
	}
	
	public static void removeFromStats(String gameId, String userR) throws SQLException
	{
		String usernames[] = new String[2];
		usernames = getPlayersName(gameId);
		String username1 = usernames[0];
		String username2 = usernames[1];
		String report = ""; //report = the winner's name as reported the 1st time 
		if (userR.equals(username1))
			report = getReport(gameId, 2);
		else 
			report = getReport(gameId, 1);
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		try{
			conn = getConnection(DataBaseManager.dbName);
			conn.setAutoCommit(false);
			if(username1.equals(report))
			{		
					String []query= {"UPDATE stats SET wins = wins -1 WHERE username=?;"};
					synchronized (gameslock) {
						for (int i=0; i<query.length; i++)
						{
							prepareStatement = conn.prepareStatement(query[i]);
							prepareStatement.setString(1,username1);
							prepareStatement.executeUpdate();
						}
						conn.commit();
						conn.setAutoCommit(true);
					}
					String []query1= {"UPDATE stats SET loses = loses -1 WHERE username=?;"};
					synchronized (gameslock) {
						for (int i=0; i<query.length; i++)
						{
							prepareStatement = conn.prepareStatement(query1[i]);
							prepareStatement.setString(1,username2);
							prepareStatement.executeUpdate();
						}
						conn.commit();
						conn.setAutoCommit(true);
					}
			}
			else{ //in case the username lost
				String []query= {"UPDATE stats SET loses = loses -1 WHERE username=?;"};
				synchronized (gameslock) {
					for (int i=0; i<query.length; i++)
					{
						prepareStatement = conn.prepareStatement(query[i]);
						prepareStatement.setString(1,username1);
						prepareStatement.executeUpdate();
					}
					conn.commit();
					conn.setAutoCommit(true);
				}
				String []query1= {"UPDATE stats SET wins = wins -1 WHERE username=?;"};
				synchronized (gameslock) {
					for (int i=0; i<query.length; i++)
					{
						prepareStatement = conn.prepareStatement(query1[i]);
						prepareStatement.setString(1,username2);
						prepareStatement.executeUpdate();
					}
					conn.commit();
					conn.setAutoCommit(true);
				}
			}
		}
		catch (SQLException ex)
		{
			conn.rollback();
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}	
		
	}
	
	public static void updateStats(String gameId,String username,String report) throws SQLException
	{
		boolean bool = false;
		bool = isReported(gameId);
		if (bool == false) {  // in case we need to update stats
			addToStats(gameId,report);
		}
		else {
			bool = areReportsTheSame(gameId);
			if (bool == true) return; // the reports o.k.
			removeFromStats(gameId, username);
		}
	}
	
	public static int[] returnStats (String username) throws SQLException{
		
		int [] stats = new int [2];
		Connection conn=null;
		PreparedStatement prepareStatement = null;
		String query = "";
		try{
			conn = getConnection(DataBaseManager.dbName);
			query= "SELECT wins FROM stats WHERE username=?;";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,username);
			stats[0] = Integer.parseInt(query);
			
			query= "SELECT loses FROM stats WHERE username=?;";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,username);
			stats[1] = Integer.parseInt(query);
		}
		catch (SQLException ex)
		{
			conn.rollback();
		}
		finally
		{
			if(prepareStatement!=null){prepareStatement.close();}
			if (conn!=null){conn.close();}
		}
		return stats;
	}


	
}
