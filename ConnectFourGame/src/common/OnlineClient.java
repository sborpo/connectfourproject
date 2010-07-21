package common;

import java.net.InetAddress;

/**
 * Class representing the OnlineClient
 */
public class OnlineClient {
	private InetAddress address;
	private String name;
	private int UDPport;
	private int TCPport;
	private int transmitPort;
	private String currentGame;
	
	/**
	 * The constructor for OnlineClient class.
	 * @param host
	 * @param UDPport
	 * @param name
	 * @param TCPPort
	 * @param transmitPort
	 */
	public OnlineClient(InetAddress host, int UDPport,String name,int TCPPort,int transmitPort)
	{
		address=host;
		this.UDPport = UDPport;
		this.TCPport = TCPPort;
		this.transmitPort = transmitPort;
		this.name = name;
		currentGame = null;
	}
	
	/**
	 * Gets the UDP port of the client.
	 * @return UDPport
	 */
	public int getUDPPort()
	{
		return UDPport;
	}
	
	/**
	 * Gets the transmit port of the client.
	 * @return transmitPort
	 */
	public int getTransmitPort()
	{
		return transmitPort;
	}
	
	/**
	 * Gets the TCP port of the client.
	 * @return TCPport
	 */
	public int getTCPPort()
	{
		return TCPport;
	}
	
	/**
	 * Gets the InetAdress of the client.
	 * @return address
	 */
	public InetAddress getAddress()
	{
		return address;
	}
	
	/**
	 * Gets the name of the client.
	 * @return name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Sets the TCP port of the client.
	 */
	public synchronized void setTCPPort(int port){
		TCPport = port;
	}
	
	/**
	 * Sets the transmit port of the client.
	 */
	public synchronized void setTransmitPort(int port){
		transmitPort = port;
	}
	
	/**
	 * Resets the game to the null.
	 */
	public synchronized void resetGame(){
		currentGame = null;
	}
	
	/**
	 * Sets a game for the client.
	 * @param gameId
	 */
	public synchronized void setGameForClient(String gameId){
		currentGame = gameId;
	}
	
	/**
	 * Gets the game of the client.
	 * @return
	 */
	public synchronized String getGame(){
		return currentGame;
	}
}
