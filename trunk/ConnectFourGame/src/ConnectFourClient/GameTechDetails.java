package ConnectFourClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class GameTechDetails {
	
	
	public static class GameDet{
		
		@Override
		public String toString() {
			return "GameDet [clientName=" + clientName + ", gameId=" + gameId
					+ ", opponentHost=" + opponentHost + ", opponentName="
					+ opponentName + ", opponentPort=" + opponentPort + "]";
		}
		public void setOpponentName(String opponentName) {
			this.opponentName = opponentName;
		}
		public void setOpponentHost(String opponentHost) {
			this.opponentHost = opponentHost;
		}
		public void setOpponentPort(String opponentPort) {
			this.opponentPort = opponentPort;
		}
		public void setGameId(String gameId) {
			this.gameId = gameId;
		}
		public void setClientName(String clientName) {
			this.clientName = clientName;
		}
		public GameDet(String opponentName, String opponentHost,
				String opponentPort, String gameId, String clientName) {
			super();
			this.opponentName = opponentName;
			this.opponentHost = opponentHost;
			this.opponentPort = opponentPort;
			this.gameId = gameId;
			this.clientName = clientName;
		}
		public GameDet() {
			// TODO Auto-generated constructor stub
		}
		public String getOpponentName() {
			return opponentName;
		}
		public String getOpponentHost() {
			return opponentHost;
		}
		public String getOpponentPort() {
			return opponentPort;
		}
		public String getGameId() {
			return gameId;
		}
		public String getClientName() {
			return clientName;
		}
		String opponentName;
		String opponentHost;
		String opponentPort;
		String gameId;
		String clientName;
		
	}
	
	String clientName;
	Properties props;
	public GameTechDetails(String clientName)
	{
		this.clientName=clientName;
		props = new Properties();
		try {
			props.load(new FileInputStream(clientName+".details"));
		} catch (FileNotFoundException e) {
			//the file is not found ,it will be created
		} catch (IOException e) {
			//will be read
			e.printStackTrace();
		}	

	}
	
	public void saveGame(GameDet gameDet) throws FileNotFoundException, IOException
	{
		props.setProperty("opName", gameDet.getOpponentName());
		props.setProperty("opHost", gameDet.getOpponentHost());
		props.setProperty("opPort", gameDet.getOpponentPort());
		props.setProperty("gameId", gameDet.getGameId());
		props.setProperty("clientName", gameDet.getClientName());
		FileOutputStream stream=new FileOutputStream(clientName+".details");
		props.store(stream, null);
		if (stream!=null)
		{
			stream.close();
		}
	}
	
	public boolean isThereGamePending()
	{
		if (!props.containsKey("opName"))
		{
			return false;
		}
		return true;
	}
	public GameDet loadGame()
	{
		//checks whenever it has game insode
		if (!isThereGamePending())
		{
			return null;
		}
		GameDet game = new GameDet();
		game.setOpponentName(props.getProperty("opName"));
		game.setOpponentHost(props.getProperty("opHost"));
		game.setOpponentPort(props.getProperty("opPort"));
		game.setGameId(props.getProperty("gameId"));
		game.setClientName(props.getProperty("clientName"));
		return game;
	}
	
	public void removeGame()
	{
		 try {
			storeEmpty();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void storeEmpty() throws IOException
	{
		props.clear();
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(clientName+".details");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		props.store(stream, null);
		if (stream!=null)
		{
			stream.close();
		}
	}
	
	
	
	
	

}
