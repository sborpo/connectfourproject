package theProtocol;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientServerProtocol {

	public static enum msgType {
		SERVER, CLIENT
	}
	
	public static class parseRes{
		public static final String SUCCESS = "SUCCESS";
		public static final String WRONG_COMMAND = "WRONG COMMAND";
		public static final String WRONG_NUM_OF_PARAMETERS = "WRONG NUMBER OF PARAMETERS";
	}
	
	static public String noGame = "noGame";
	
	private msgType type;
	public static final String MEETME = "MEETME";
	public static final String NEWGAME = "NEWGAME";
	public static final String PLAY = "PLAY";
	public static final String WATCH = "WATCH";
	public static final String OK = "OK";
	public static final String GAME = "GAME";
	public static final String NOCONN = "NO_CONNECTION";
	public static final String KNOWYA = "KNOW_YOU?";
	public static final String WHAT = "DON'T_UNDERSTAND";
	public static final String DENIED = "DENIED";
	public static final String NICETM = "NICE_TO_MEET_YOU";
	public static final String GOGOGO = "GOGOGO";
	public static final String SERVPROB = "SERVER_INTERNAL_PROBLEMS";
	public static final String VIEWERTRANSMIT ="VIEWER_TRANSMIT";
	public static final String ENJOYWATCH ="ENJOY_WATCHING";
	public static final String GAMEMOVE ="GAME_MOVE";
	public static final String GAMEREPORT ="GAME_REPORT";
	public static final String USERNOTEXISTS="USERNAME_NOT_EXISTS";
	public static final String USERALREADYEXISTS= "USERNAME_ALREADY_EXISTS";
	public static final String SIGNUP="SIGN_UP";
	public static final String GAMELIST="GAME_LIST";
	//public static final String YOUALIVE ="ARE_YOU_ALIVE?";
	public static final String IMALIVE ="I'M_ALIVE!";
	
	private ArrayList<String> legalCommands;
	private HashMap<String,Integer> numOfParametersForCmd;
	
	public String result;
	
	public ClientServerProtocol(msgType type){
		this.type = type;
		legalCommands = new ArrayList<String>();
		numOfParametersForCmd = new HashMap<String,Integer>();
		result = parseRes.SUCCESS;
		//initialize the map command to number of parameters of the command
		mapInit();
		//Commands SERVER can receive
		if(type.equals(msgType.SERVER)){
			legalCommands.add(MEETME);
			legalCommands.add(NEWGAME);
			legalCommands.add(PLAY);
			legalCommands.add(WATCH);
			legalCommands.add(OK);
			legalCommands.add(WHAT);
			legalCommands.add(IMALIVE);
			legalCommands.add(GAMEREPORT);
			legalCommands.add(SIGNUP);
			legalCommands.add(GAMELIST);
		}
		//Commands CLIENT can receive
		else{
			legalCommands.add(NOCONN);
			legalCommands.add(KNOWYA);
			legalCommands.add(WHAT);
			legalCommands.add(DENIED);
			legalCommands.add(NICETM);
			legalCommands.add(GAME);
			legalCommands.add(GOGOGO);
			legalCommands.add(SERVPROB);
			legalCommands.add(VIEWERTRANSMIT);
			legalCommands.add(GAMEMOVE);
			legalCommands.add(GAMEREPORT);
			legalCommands.add(OK);
			legalCommands.add(ENJOYWATCH);
			legalCommands.add(USERNOTEXISTS);
			legalCommands.add(USERALREADYEXISTS);
		}
	}
	
	private void mapInit(){
		numOfParametersForCmd.put(MEETME, 4);
		numOfParametersForCmd.put(NEWGAME, 2);
		numOfParametersForCmd.put(PLAY, 3);
		numOfParametersForCmd.put(WATCH, 3);
		numOfParametersForCmd.put(OK, 0);
		numOfParametersForCmd.put(NOCONN, 0);
		numOfParametersForCmd.put(KNOWYA, 0);
		numOfParametersForCmd.put(WHAT, 0);
		numOfParametersForCmd.put(DENIED, 0);
		numOfParametersForCmd.put(NICETM, 1);
		numOfParametersForCmd.put(GAME, 1);
		numOfParametersForCmd.put(GOGOGO, 3);
		numOfParametersForCmd.put(SERVPROB, 0);
		numOfParametersForCmd.put(VIEWERTRANSMIT, 3);
		numOfParametersForCmd.put(GAMEMOVE, 2);
		numOfParametersForCmd.put(GAMEREPORT, 4);
		numOfParametersForCmd.put(IMALIVE, 4);
		numOfParametersForCmd.put(ENJOYWATCH, 0);
		numOfParametersForCmd.put(USERNOTEXISTS, 0);
		numOfParametersForCmd.put(USERALREADYEXISTS, 0);
		numOfParametersForCmd.put(SIGNUP, 2);
		numOfParametersForCmd.put(GAMELIST, 0);
	}
	public String[] parseCommand(String command){
		String[] params = command.split(" +");
		if(legalCommands.contains(params[0])){ 
			if(numOfParametersForCmd.get(params[0]).equals(params.length - 1)){
				result = parseRes.SUCCESS;
				return params;
			}
			else{
				result = parseRes.WRONG_NUM_OF_PARAMETERS;
				return null;
			}
		}
		else{
			result = parseRes.WRONG_COMMAND;
			return null;
		}
	}
}