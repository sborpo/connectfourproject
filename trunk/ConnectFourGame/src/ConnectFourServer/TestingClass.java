package ConnectFourServer;

public class TestingClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			
		
	DataBaseManager.constructTables();
	//DataBaseManager.insertUser("stam", "check");
	DataBaseManager.createGame("a", "b", "c");
	DataBaseManager.makeReport("c", "b", "myreport");
		}
		catch	(Exception e)
		{
			
		}

	}

}
