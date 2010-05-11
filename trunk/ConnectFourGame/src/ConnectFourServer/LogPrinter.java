package ConnectFourServer;

/**
 * Synchronized log , every thread will print to the sysout alternaly
 * 
 */
public class LogPrinter {

	synchronized public void print(String message) {
		System.out.println(message);
	}

}
