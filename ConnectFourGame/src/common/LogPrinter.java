package common;

/**
 * Synchronized log , every thread will print to the sysout alternaly
 * 
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException; 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
 
 
public class LogPrinter { 
	private final static DateFormat time_format = new SimpleDateFormat ("hh:mm:ss");
	private final static DateFormat date_format = new SimpleDateFormat ("yyyy-MM-dd");
	private String logPath = "c:\\log.txt";
	FileWriter aWriter = null;
	
	public LogPrinter(String name) throws IOException{  
		String time = getTime();
        logPath = name + "_" + getDate() + "_time_" + time + ".txt";
        logPath = logPath.replaceAll("[-:]", "_");
        new File(logPath).delete();
        aWriter = new FileWriter(logPath, true);
	}

	static private String getTime(){
		Date now = new Date();
        String currentTime = LogPrinter.time_format.format(now);
        return currentTime;
	}
	
	static private String getDate(){
		Date now = new Date();
        String currentTime = LogPrinter.date_format.format(now);
        return currentTime;
	}
	
	synchronized public void log_info(String message) throws IOException{
		aWriter.write(LogPrinter.info_msg(message));
		aWriter.flush();
    }
    
	synchronized public void log_error(String message) throws IOException{
		aWriter.write(LogPrinter.error_msg(message));
		aWriter.flush();
    }
	
	synchronized public void print_info(String message){
		try {
			log_info(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(LogPrinter.info_msg(message));
    }
    
	synchronized public void print_error(String message){
		try {
			log_error(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(LogPrinter.error_msg(message));
    }
	
	static public String info_msg(String message){
		return getDate() + " " + getTime() + " Info: " + message + "\n";
    }
    
	static public String error_msg(String message){
		return getDate() + " " + getTime() + " Error: " + message + "\n";
    }
} 