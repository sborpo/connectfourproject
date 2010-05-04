package ConnectFourServer;

import java.net.Socket;

public class RequestHandler implements Runnable {

	//The client's socket from where we should read the
	// request and act according to it.
	private Socket clientSock;
	
	public RequestHandler(Socket clientSocket)
	{
		clientSock=clientSocket;
	}
	
	
	@Override
	public void run() {
		
		//TODO: read from clientSock the InputStream , and 
		//act according to it.
		
	}

}
