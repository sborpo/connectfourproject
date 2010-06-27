package ConnectFourServer;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.SAXException;

import ConnectFourClient.GameTechDetails;

import common.GameReport;
import common.XmlReports;
import common.XmlReports.WrongSchema;

public class TestingClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		try{
//			
//			DataBaseManager.createDB("database");
//			DataBaseManager.constructTables();
//			//DataBaseManager.insertUser("stam", "check");
//			try{
//				DataBaseManager.createGame("user1", "user2", "gameId");
//			}
//			catch(DataBaseManager.GameIdAlreadyExists e){
//				System.out.println("Game is already exists");
//			}
//			
//			
//			DataBaseManager.makeReport("gameId", "user1", "myreport");
//			DataBaseManager.makeReport("gameId", "user2", "myreport2");
//			
//			if(DataBaseManager.areReportsTheSame("gameId")){
//				System.out.println("The reports are the same");
//			}
//			else{
//				System.out.println("The reports are diff");
//			}
//		}
//		catch	(Exception e)
//		{
//			System.out.println(e.getMessage());
//		}
		
//		try {
//			XmlReports rep= new XmlReports("avi");
//		rep.addGameReport("1", "avi", "13", "winner");
//		rep.addGameReport("2", "moshe", "15", "lost");
//		rep.addGameReport("3", "liron", "14", "winner");
//			System.out.println("\n\n");
//			for (GameReport report : rep.generateGameList()) {
//				System.out.println(report.toString());
//			}
//			System.out.println("\n\nThe String Is:\n\n");
//			String s=rep.createGamesReportString();
//		System.out.println(s);	
//		
//		System.out.println("\n\nThe List array:\n\n");
//		for (GameReport report : XmlReports.gameReportsFromReportString(s.split(" +"))) {
//			System.out.println(report.toString());
//		}	
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (WrongSchema e) {
//			// TODO Auto-generated catch block
//			System.out.println("The Document Is not Valid!");
//		}
		
		GameTechDetails.GameDet det = new GameTechDetails.GameDet("asf", "asfasf", "Asf", "asfasf", "asfasf");
		GameTechDetails details = new GameTechDetails("avi");

		GameTechDetails.GameDet det2= details.loadGame();
	
		System.out.println(det2);
		details.removeGame();
		
	}

}
