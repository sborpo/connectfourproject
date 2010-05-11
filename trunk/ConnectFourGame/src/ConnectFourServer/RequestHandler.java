package ConnectFourServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class RequestHandler implements Runnable {

	// The client's socket from where we should read the
	// request and act according to it.
	private Socket clientSock;

	// the server which the Request was sent to
	private MainServer server;

	public RequestHandler(Socket clientSocket, MainServer server) {
		clientSock = clientSocket;
		this.server = server;
	}

	@Override
	public void run() {
		PrintWriter out = null;
		BufferedReader in = null;
		String clientName = clientSock.getInetAddress().getHostName();
		String clientIP = clientSock.getInetAddress().getHostAddress();
		try {
			out = new PrintWriter(clientSock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSock
					.getInputStream()));
			int udpClientPort= Integer.parseInt(in.readLine());
			String inputLine;
			StringBuilder sb = new StringBuilder();
			// reads the input line by line and appends in to the string builder
			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
				if (inputLine.equals(""))
					break;
			}
			String logMessage = "Client with name :" + clientName + " IP: "
					+ clientIP + " Recieved This Message: \n -------------\n"
					+ sb.toString() + "\n-------------\n\n\n";
			server.printLog(logMessage);
			out.print("Server: I've Recieved Your Message");
			server.games.addClientToUdpList(new OnlineGames.Client(clientSock.getInetAddress(), udpClientPort));
			out.flush();
			out.close();
		} catch (IOException ex) {
			server.printLog("Problem reading from Client: " + clientName
					+ " with IP: " + clientIP);
		} finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					server.printLog("Problem closing socket from Client: "
							+ clientName + " with IP: " + clientIP);
					e.printStackTrace();
				}
			}

		}
	}


}
