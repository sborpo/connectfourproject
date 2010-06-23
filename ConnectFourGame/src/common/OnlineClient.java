package common;

import java.net.InetAddress;

public class OnlineClient {
	private InetAddress address;
	private String name;
	private int UDPport;
	private int TCPport;
	private int transmitPort;
	private String currentGame;
	
	public OnlineClient(InetAddress host, int UDPport,String name,int TCPPort,int transmitPort)
	{
		address=host;
		this.UDPport = UDPport;
		this.TCPport = TCPPort;
		this.transmitPort = transmitPort;
		this.name = name;
		currentGame = null;
	}
	
	public int getUDPPort()
	{
		return UDPport;
	}
	public int getTransmitPort()
	{
		return transmitPort;
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
	
	public synchronized void setTransmitPort(int port){
		transmitPort = port;
	}
	
	public synchronized void resetGame(){
		currentGame = null;
	}
	
	public synchronized void setGameForClient(String gameId){
		currentGame = gameId;
	}
	
	public synchronized String getGame(){
		return currentGame;
	}
}
