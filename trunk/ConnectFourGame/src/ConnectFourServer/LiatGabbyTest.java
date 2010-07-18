package ConnectFourServer;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import sun.font.CreatedFontTracker;

import com.mysql.jdbc.DatabaseMetaData;

import ConnectFourServer.DataBaseManager.UserAlreadyExists;
import common.RSAgenerator;
import common.PasswordHashManager.SystemUnavailableException;

public class LiatGabbyTest {

	
	
	public static void main(String[] args) {
		try{
		//will effect only the first time
		DataBaseManager.createDB("db");
		DataBaseManager.constructTables();
		
		//clear the tables
		removeFromTables();
		
		//insert 2 usernames into the db : adam1 and adam2
		insertUserForTest("adam1", "StamSisma");
		insertUserForTest("adam2", "StamSisma");
		//create a new game called: game#1 between adam1 and adam2
		DataBaseManager.createGame("adam1", "adam2", "game#1");
		
		
		int testNumber=1;
		//int testNumber=2;
		//int testNumber=3;
		
		//----------------Test Number 1-------------//
		if (testNumber==1)
		{
			//adam 1 makes a report that he won
			DataBaseManager.makeReport("game#1", "adam1", "adam1");
			DataBaseManager.updateStats("game#1", "adam1", "adam1");
			
			//adam2 makes a report that adam1 won
			DataBaseManager.makeReport("game#1", "adam2", "adam1");
			DataBaseManager.updateStats("game#1", "adam2", "adam1");
			
			int [] adam1Stats=DataBaseManager.returnStats("adam1");
			int [] adam2Stats=DataBaseManager.returnStats("adam2");
			//should return 1 ,0
			System.out.println("The stats of adam1 are: wins: "+adam1Stats[0]+" loses: "+adam1Stats[1]);
			//should return 0 ,1
			System.out.println("The stats of adam1 are: wins: "+adam2Stats[0]+" loses: "+adam2Stats[1]);
			return;
		}
		
		//----------------Test Number 2-------------//
		if (testNumber==2)
		{
			//adam 1 makes a report that he won
			DataBaseManager.makeReport("game#1", "adam1", "adam1");
			DataBaseManager.updateStats("game#1", "adam1", "adam1");
			
			//adam2 makes a report that he won
			DataBaseManager.makeReport("game#1", "adam2", "adam2");
			DataBaseManager.updateStats("game#1", "adam2", "adam2");
			
			int [] adam1Stats=DataBaseManager.returnStats("adam1");
			int [] adam2Stats=DataBaseManager.returnStats("adam2");
			//should return 0 ,0
			System.out.println("The stats of adam1 are: wins: "+adam1Stats[0]+" loses: "+adam1Stats[1]);
			//should return 0 ,0
			System.out.println("The stats of adam1 are: wins: "+adam2Stats[0]+" loses: "+adam2Stats[1]);
			return;
		}
		
		//----------------Test Number 3-------------//
		if (testNumber==3)
		{
			//adam 1 makes a report that he won
			DataBaseManager.makeReport("game#1", "adam1", "adam1");
			DataBaseManager.updateStats("game#1", "adam1", "adam1");
			
			//adam2 makes a report that he won
			DataBaseManager.makeReport("game#1", "adam2", "adam2");
			DataBaseManager.updateStats("game#1", "adam2", "adam2");
			
			//This code should not have an effect because both of the users already reported
			//adam 1 makes a report that he won
			DataBaseManager.makeReport("game#1", "adam1", "adam1");
			DataBaseManager.updateStats("game#1", "adam1", "adam1");
			
			int [] adam1Stats=DataBaseManager.returnStats("adam1");
			int [] adam2Stats=DataBaseManager.returnStats("adam2");
			//should return 1 ,0
			System.out.println("The stats of adam1 are: wins: "+adam1Stats[0]+" loses: "+adam1Stats[1]);
			//should return 0 ,1
			System.out.println("The stats of adam1 are: wins: "+adam2Stats[0]+" loses: "+adam2Stats[1]);
			return;
		}
		
		
		}
		
		
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		//remove everything from DB
		try{
		removeFromTables();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	
	
	
	
	
	
	

	public static void insertUserForTest(String username,String password) throws SQLException, UserAlreadyExists, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, SystemUnavailableException
	{

		Connection conn=null;
		PreparedStatement prepareStatement = null;
		PreparedStatement preparedStatementStas= null;
		try{
			conn = DataBaseManager.getConnection(DataBaseManager.getDBname());
			conn.setAutoCommit(false);
			String query= "INSERT INTO users VALUES(?,?);";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.setString(1,username);
			prepareStatement.setString(2,password);
			query= "INSERT INTO stats VALUES(?,?,?);";
			preparedStatementStas=conn.prepareStatement(query);
			preparedStatementStas.setString(1,username);
			preparedStatementStas.setInt(2,0);
			preparedStatementStas.setInt(3,0);
			System.out.println("Inserting....");
			prepareStatement.execute();
			preparedStatementStas.execute();
			conn.commit();
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
	
	public static void removeFromTables() throws SQLException, UserAlreadyExists, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, SystemUnavailableException
	{

		Connection conn=null;
		PreparedStatement prepareStatement = null;
		PreparedStatement preparedStatementStas= null;
		try{
			conn = DataBaseManager.getConnection(DataBaseManager.getDBname());
			conn.setAutoCommit(false);
			String query= "DELETE  FROM users ;";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.execute();
			query="DELETE  FROM stats";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.execute();
			query="DELETE  FROM games";
			prepareStatement = conn.prepareStatement(query);
			prepareStatement.execute();
			
			conn.commit();
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
	

}
