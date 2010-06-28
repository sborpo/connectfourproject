package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.w3c.dom.Element;

import theProtocol.ClientServerProtocol;

public class UnhandledReports {
	public static class NoReports extends Exception {}
	public static class FileChanged extends Exception {}
	private String fileName;
	private HashMap<String, UnhandeledReport> reports;
	
	public UnhandledReports(String clientName) throws NoReports, FileChanged
	{
		fileName=clientName+".report";
		File f= new File(fileName);
		if (!f.exists())
		{
			reports= new HashMap<String, UnhandeledReport>();
			return;
		}
		ObjectInputStream stream;
		try {
			AESmanager manager = new AESmanager();
			stream = new ObjectInputStream(manager.getDecryptedInStream(f));
			reports=(HashMap<String,UnhandeledReport>)stream.readObject();
		} catch (FileNotFoundException e) {
			throw new NoReports();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			throw new FileChanged();
		}
	}
	
	
	public String printReports()
	{
		 Collection<UnhandeledReport> list = reports.values();
		 StringBuilder sb = new StringBuilder();
			
			for (UnhandeledReport gameReport :list) {
			sb.append(gameReport.toString()+"\n");
			}
			return sb.toString();
	}
	public String createGamesReportString()
	{
	    Collection<UnhandeledReport> list = reports.values();
		
		ArrayList<String> params = new  ArrayList<String>();
		params.add(ClientServerProtocol.BATCHGAMESREPORT);
		
		for (UnhandeledReport gameReport :list) {
			params.add(gameReport.getGameId());
			params.add(gameReport.getClientName());
			params.add(gameReport.getGameResult());
			params.add(gameReport.getWinner());
		}
		String [] arr = new String[4];
	
		String report = ClientServerProtocol.buildCommand(params.toArray(arr));
		return report;
	}
	
	public void addReport(UnhandeledReport report) throws IOException
	{
		reports.put(report.getGameId(), report);
		saveFile();
	}
	public void removeReport(String gameId) throws IOException
	{
		if (reports.containsKey(gameId))
		{
			reports.remove(gameId);
		}
		saveFile();
		
	}
	
	
	public static ArrayList<UnhandeledReport> gameReportsFromReportString(String theCommand)
	{
		String[] params = theCommand.split(ClientServerProtocol.paramSeparator);
		ArrayList<UnhandeledReport> list = new ArrayList<UnhandeledReport>();
		int numOfReports= (params.length-1)/4;
		for (int i=0; i<numOfReports; i++)
		{
			String [] arr= new String[4];
			for (int j=0; j<4; j++)
			{
				arr[j]= params[4*i+1+j];
			}
			list.add(new UnhandeledReport(arr[0], arr[1], arr[2], arr[3]));
			
		}
		return list;
	}
	
	private void saveFile() throws IOException
	{
		AESmanager manager = new AESmanager();
		ObjectOutputStream stream;
		File f= new File(fileName);
		stream = new ObjectOutputStream(manager.getEncryptedOutStream(f));
		stream.writeObject(reports);
		stream.close();
	}
	
	

}
