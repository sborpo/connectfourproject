package common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import common.UnhandledReports.FileChanged;
import common.UnhandledReports.NoReports;

public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UnhandledReports reports =null;
//	try {
//		 reports = new UnhandledReports("server");
//	} catch (NoReports e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	} catch (FileChanged e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	try {
//		reports.addReport(new UnhandeledReport("1", "asf", "asf", "asfasf"));
//		reports.addReport(new UnhandeledReport("1", "abc", "asf", "asfasf"));
//		reports.addReport(new UnhandeledReport("1","abc","danidin","hhh"));
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
	try {
		reports= new UnhandledReports("server");
	} catch (NoReports e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (FileChanged e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	ArrayList< UnhandeledReport> list=reports.getUnhandeledReports();
	for (UnhandeledReport string : list) {
		System.out.println(string);
	}
	
	

	}

}
