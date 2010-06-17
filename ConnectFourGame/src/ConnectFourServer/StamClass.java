package ConnectFourServer;

import java.io.Serializable;

public class StamClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2520484088088006368L;
	private int x;
	private int y;
	public StamClass(int a ,int b)
	{
		x=a;
		y=b;
	}
	
	public Integer getX()
	{
		return new Integer(x);
	}
	public Integer getY()
	{
		return new Integer(y);
	}
}
