package theProtocol;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientServerProtocol {

	public static enum msgType {
		SERVER, CLIENT
	}
	
	public static enum parseRes{
		SUCCESS,WRONG_COMMAND,NOT_ENOUGH_PARAMETERS
	}
	
	private msgType type;
	public static final String MEETME = "MEETME";
	public static final String NEWGAME = "NEWGAME";
	public static final String PLAY = "PLAY";
	public static final String WATCH = "WATCH";
	public static final String OK = "OK";
	public static final String GAME = "GAME";
	public static final String NOCONN = "NO_CONNECTION";
	public static final String TAKE = "TAKE";
	public static final String KNOWYA = "KNOW_YOU?";
	public static final String WHAT = "DON'T_UNDERSTAND";
	public static final String DENIED = "DENIED";
	public static final String NICETM = "NICE_TO_MEET_YOU";
	public static final String GOGOGO = "GOGOGO";
	public static final String SERVPROB = "SERVER_INTERNAL_PROBLEMS";
	//server to client
	public static final String VIEWERTRANSMIT ="VIEWER_TRANSMIT";
	
	private ArrayList<String> legalCommands;
	private HashMap<String,Integer> numOfParametersForCmd;
	
	public parseRes result;
	
	public ClientServerProtocol(msgType type){
		this.type = type;
		legalCommands = new ArrayList<String>();
		numOfParametersForCmd = new HashMap<String,Integer>();
		result = parseRes.WRONG_COMMAND;
		//initialize the map command to number of parameters of the command
		mapInit();
		
		if(type.equals(msgType.SERVER)){
			legalCommands.add(MEETME);
			legalCommands.add(NEWGAME);
			legalCommands.add(PLAY);
			legalCommands.add(WATCH);
			legalCommands.add(OK);
			legalCommands.add(WHAT);
		}
		else{
			legalCommands.add(NOCONN);
			legalCommands.add(TAKE);
			legalCommands.add(KNOWYA);
			legalCommands.add(WHAT);
			legalCommands.add(DENIED);
			legalCommands.add(NICETM);
			legalCommands.add(GAME);
			legalCommands.add(GOGOGO);
			legalCommands.add(SERVPROB);
			legalCommands.add(VIEWERTRANSMIT);
		}
	}
	
	private void mapInit(){
		numOfParametersForCmd.put(MEETME, 2);
		numOfParametersForCmd.put(NEWGAME, 2);
		numOfParametersForCmd.put(PLAY, 3);
		numOfParametersForCmd.put(WATCH, 3);
		numOfParametersForCmd.put(OK, 0);
		numOfParametersForCmd.put(NOCONN, 0);
		numOfParametersForCmd.put(TAKE, 2);
		numOfParametersForCmd.put(KNOWYA, 0);
		numOfParametersForCmd.put(WHAT, 0);
		numOfParametersForCmd.put(DENIED, 0);
		numOfParametersForCmd.put(NICETM, 1);
		numOfParametersForCmd.put(GAME, 1);
		numOfParametersForCmd.put(GOGOGO, 3);
		numOfParametersForCmd.put(SERVPROB, 0);
		numOfParametersForCmd.put(VIEWERTRANSMIT, 3);
	}
	public String[] parseCommand(String command){
		String[] params = command.split(" +");
		if(legalCommands.contains(params[0]) 
				&& numOfParametersForCmd.get(params[0]).equals(params.length - 1)){
			return params;
		}
		else{
			return null;
		}
	}
}