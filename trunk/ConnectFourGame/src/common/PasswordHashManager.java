package common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordHashManager {
	
	public class SystemUnavailableException extends Exception { 
		public SystemUnavailableException(String mes){
			super(mes);
		}
	};
	
	private static PasswordHashManager instance;

	public synchronized String encrypt(String plaintext) throws SystemUnavailableException
	  {
	    MessageDigest md = null;
	    try
	    {
	      md = MessageDigest.getInstance("SHA"); //step 2
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	      throw new SystemUnavailableException(e.getMessage());
	    }
	    try
	    {
	      md.update(plaintext.getBytes("UTF-8")); //step 3
	    }
	    catch(UnsupportedEncodingException e)
	    {
	      throw new SystemUnavailableException(e.getMessage());
	    }

	    byte raw[] = md.digest(); //step 4
	    String hash = byteArrayToHexString(raw); //step 5
	    return hash; //step 6
	  }
	  
	  public static synchronized PasswordHashManager getInstance() //step 1
	  {
	    if(instance == null)
	    {
	       instance = new PasswordHashManager(); 
	    } 
	    return instance;
	  }

	  public static synchronized String byteArrayToHexString(byte[] b){
		     StringBuffer sb = new StringBuffer(b.length * 2);
		     for (int i = 0; i < b.length; i++){
		       int v = b[i] & 0xff;
		       if (v < 16) {
		         sb.append('0');
		       }
		       sb.append(Integer.toHexString(v));
		     }
		     return sb.toString().toUpperCase();
		  }

}
