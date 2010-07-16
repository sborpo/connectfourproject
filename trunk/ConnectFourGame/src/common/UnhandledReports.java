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
			stream.close();
			if (reports.size()==0)
			{
				System.out.println("NO REPORTS IN THE FILE..");
				this.removeReportsFile();
				throw new NoReports();
			}
		} catch (FileNotFoundException e) {
			throw new NoReports();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			throw new FileChanged();
		}
	}
	
	public HashMap<String, UnhandeledReport> getUnhandeledReports()
	{
		return reports;
	}
	public int getReportNumber(){
		return reports.size();
	}
	
	public void removeReportsFile()
	{
		System.out.println("INSIDE");
		File f= new File(fileName);
		boolean isExists = f.exists();
		System.out.println("FLAG: "+ isExists);
		if (f.exists())
		{
			boolean res = f.delete();
			System.out.println(res+" :removed..." + fileName);
		}
		reports = null;
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
		if(list.isEmpty()){
			return null;
		}
		ArrayList<String> params = new  ArrayList<String>();
		params.add(ClientServerProtocol.BATCHGAMESREPORT);
		
		for (UnhandeledReport gameReport :list) {
			params.add(gameReport.getGameId());
			params.add(gameReport.getClientName());
			params.add(gameReport.getGameResult());
			params.add(gameReport.getWinner());
		}
		int dataLen = 4*reports.size() + 1;
		String [] arr = new String[dataLen];
		params.toArray(arr);
		String report = ClientServerProtocol.buildCommand(arr);
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
	
	
	public static ArrayList<UnhandeledReport> gameReportsFromReportsArray(String [] params )
	{
		ArrayList<UnhandeledReport> list = new ArrayList<UnhandeledReport>();
		int numOfReports= (params.length)/4 ;
		for (int i=0; i<numOfReports; i++)
		{
			String [] arr= new String[4];
			for (int j=0; j<4; j++)
			{
				arr[j]= params[4*i+j];
			}
			UnhandeledReport report = new UnhandeledReport(arr[0], arr[1], arr[2], arr[3]);
			System.out.println("Adding Report: "+report);
			list.add(report);
			
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
