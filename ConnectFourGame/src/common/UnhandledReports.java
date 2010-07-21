package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import theProtocol.ClientServerProtocol;

/**
 * Represents a group of Unhandeled reports.
 */
public class UnhandledReports {
	public static class NoReports extends Exception {}
	public static class FileChanged extends Exception {}
	private String fileName;
	private ArrayList< UnhandeledReport> reports;
	
	/**
	 * The constructor for the group of Unhandled reports class.
	 * @param clientName
	 * @throws NoReports
	 * @throws FileChanged
	 */
	public UnhandledReports(String clientName) throws NoReports, FileChanged
	{
		fileName=clientName+".report";
		File f= new File(fileName);
		if (!f.exists())
		{
			reports= new ArrayList<UnhandeledReport>();
			return;
		}
		ObjectInputStream stream;
		try {
			AESmanager manager = new AESmanager();
			stream = new ObjectInputStream(manager.getDecryptedInStream(f));
			reports=(ArrayList<UnhandeledReport>)stream.readObject();
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
	

	/**
	 * Gets the unhandeled reports.
	 * @return reports
	 */
	public ArrayList< UnhandeledReport> getUnhandeledReports()
	{
		return reports;
	}
	
	/**
	 * Gets the number of the reports. 
	 * @return size of the reports list.
	 */
	public int getReportNumber(){
		return reports.size();
	}
	
	/**
	 * Removes reports file.
	 */
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
	
	/**
	 * Prints the reports.
	 * @return string with all the reports.
	 */
	public String printReports()
	{
		 Collection<UnhandeledReport> list = reports;
		 StringBuilder sb = new StringBuilder();
			
			for (UnhandeledReport gameReport :list) {
			sb.append(gameReport.toString()+"\n");
			}
			return sb.toString();
	}
	
	/**
	 * Creates a string for a batch reporting of the reports.
	 * @return report
	 */
	public String createGamesReportString()
	{
	    Collection<UnhandeledReport> list = reports;
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
	
	/**
	 * Adds a report to the reports list and saves the reports file
	 * updated with the new report.
	 * @param report
	 * @throws IOException
	 */
	public void addReport(UnhandeledReport report) throws IOException
	{
		if (reports.contains(report))
		{
			reports.remove(report);
			
		}
		reports.add(report);
		saveFile();
	}
	
	/**
	 * Removes a report from reports list and saves the 
	 * updated report file.
	 * @param gameId
	 * @param clientName
	 * @throws IOException
	 */
	public void removeReport(String gameId,String clientName) throws IOException
	{
		UnhandeledReport report= new UnhandeledReport(gameId, clientName, null, null);
		if (reports.contains(report))
		{
			reports.remove(report);
		}
		saveFile();
		
	}
	
	/**
	 * Creates a list of reports from report string array.
	 * @param params
	 * @return list
	 */
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
			list.add(report);
		}
		return list;
	}
	
	/**
	 * Saves an encrypted file with the reports list object.
	 * @throws IOException
	 */
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
