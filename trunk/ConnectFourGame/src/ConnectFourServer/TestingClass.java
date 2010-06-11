package ConnectFourServer;

public class TestingClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			
			DataBaseManager.createDB("database");
			DataBaseManager.constructTables();
			//DataBaseManager.insertUser("stam", "check");
			try{
				DataBaseManager.createGame("user1", "user2", "gameId");
			}
			catch(DataBaseManager.GameIdAlreadyExists e){
				System.out.println("Game is already exists");
			}
			
			
			DataBaseManager.makeReport("gameId", "user1", "myreport");
			DataBaseManager.makeReport("gameId", "user2", "myreport2");
			
			if(DataBaseManager.areReportsTheSame("gameId")){
				System.out.println("The reports are the same");
			}
			else{
				System.out.println("The reports are diff");
			}
		}
		catch	(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

}
