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
 
/**
 * Represents a class for logging.
 */
public class LogPrinter { 
	private final static DateFormat time_format = new SimpleDateFormat ("hh:mm:ss");
	private final static DateFormat date_format = new SimpleDateFormat ("yyyy-MM-dd");
	private String logPath = "c:\\log.txt";
	FileWriter aWriter = null;
	
	/**
	 * Creates an instance of the logger class.
	 * Creates a new file named "name_<date_time>.txt".
	 * @param name
	 * @throws IOException
	 */
	public LogPrinter(String name) throws IOException{  
		String time = getTime();
        logPath = name + "_" + getDate() + "_time_" + time + ".txt";
        logPath = logPath.replaceAll("[-:]", "_");
        new File(logPath).delete();
        aWriter = new FileWriter(logPath, true);
	}

	/**
	 * Gets the current time
	 * @return currentTime
	 */
	static private String getTime(){
		Date now = new Date();
        String currentTime = LogPrinter.time_format.format(now);
        return currentTime;
	}
	
	/**
	 * Gets the current date.
	 * @return currentDate
	 */
	static private String getDate(){
		Date now = new Date();
        String currentDate = LogPrinter.date_format.format(now);
        return currentDate;
	}
	
	/**
	 * Internal function for info message writing.
	 * @param message
	 * @throws IOException
	 */
	synchronized private void log_info(String message) throws IOException{
		aWriter.write(LogPrinter.info_msg(message));
		aWriter.flush();
    }
    
	/**
	 * Internal function for error message writing.
	 * @param message
	 * @throws IOException
	 */
	synchronized private void log_error(String message) throws IOException{
		aWriter.write(LogPrinter.error_msg(message));
		aWriter.flush();
    }
	
	/**
	 * Print the info message to the log file.
	 * @param message
	 */
	synchronized public void print_info(String message){
		try {
			log_info(message);
		} catch (IOException e) {
			System.out.println("Problem writing to file: " + e.getMessage());
		}
		System.out.println(LogPrinter.info_msg(message));
    }
    
	/**
	 * Print the error message to the log file.
	 * @param message
	 */
	synchronized public void print_error(String message){
		try {
			log_error(message);
		} catch (IOException e) {
			System.out.println("Problem writing to file: " + e.getMessage());
		}
		System.out.println(LogPrinter.error_msg(message));
    }
	
	/**
	 * Return a string with current date and time for info message.
	 * @param message
	 * @return header for info message
	 */
	static public String info_msg(String message){
		return getDate() + " " + getTime() + " Info: " + message + "\n";
    }
    
	/**
	 * Return a string with current date and time for error message.
	 * @param message
	 * @return header for error message
	 */
	static public String error_msg(String message){
		return getDate() + " " + getTime() + " Error: " + message + "\n";
    }
} 