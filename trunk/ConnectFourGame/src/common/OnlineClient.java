package common;

import java.net.InetAddress;

public class OnlineClient {
	private InetAddress address;
	private String name;
	private int UDPport;
	private int TCPport;
	private int UDPtransmitPort;
	private String currentGame;
	
	public OnlineClient(InetAddress host, int UDPport,String name,int TCPPort,int UDPtransmitPort)
	{
		address=host;
		this.UDPport = UDPport;
		this.TCPport = TCPPort;
		this.UDPtransmitPort = UDPtransmitPort;
		this.name = name;
		currentGame = null;
	}
	
	public int getUDPPort()
	{
		return UDPport;
	}
	public int getUDPtransmitPort()
	{
		return UDPtransmitPort;
	}
	public int getTCPPort()
	{
		return TCPport;
	}
	public InetAddress getAddress()
	{
		return address;
	}
	public String getName(){
		return name;
	}
	
	public synchronized void setTCPPort(int port){
		TCPport = port;
	}
	
	public synchronized void resetGame(){
		currentGame = "";
	}
	
	public synchronized void setGameForClient(String gameId){
		currentGame = gameId;
	}
	
	public synchronized String getGame(){
		return currentGame;
	}
}
