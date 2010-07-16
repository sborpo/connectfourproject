package common;

import java.io.IOException;
import java.util.HashMap;

import common.UnhandledReports.FileChanged;
import common.UnhandledReports.NoReports;

public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UnhandledReports reports =null;
	try {
		 reports = new UnhandledReports("server");
	} catch (NoReports e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (FileChanged e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		reports.addReport(new UnhandeledReport("asf", "asf", "asf", "asfasf"));
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		reports= new UnhandledReports("server");
	} catch (NoReports e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (FileChanged e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	HashMap<String, UnhandeledReport> list=reports.getUnhandeledReports();
	for (String string : list.keySet()) {
		System.out.println(string);
	}
	
	

	}

}