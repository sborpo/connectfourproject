package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class AESmanager {

	static private Cipher aesCipher = null;
	static final private String keyValue = "Py†ôDBõ^È~+Q²";
	SecretKeySpec aeskeySpec;
	
	public AESmanager(){
		
		aeskeySpec = new SecretKeySpec(keyValue.getBytes(), "AES");
		try {
			aesCipher=Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public CipherOutputStream getEncryptedOutStream(File out){		
	    try {
			aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    CipherOutputStream os = null;
		try {
			os = new CipherOutputStream(new FileOutputStream(out), aesCipher);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return os;
	  }
	  
	  public CipherInputStream getDecryptedInStream(File in)  {
		
	    try {
			aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    CipherInputStream is = null;
		try {
			is = new CipherInputStream(new FileInputStream(in), aesCipher);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    return is;
	  }
	  
	  public static void   printKey(){

	       // Get the KeyGenerator

	       KeyGenerator kgen = null;
		try {
			kgen = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	       kgen.init(128); // 192 and 256 bits may not be available


	       // Generate the secret key specs.
	       SecretKey skey = kgen.generateKey();
	      String r = new String( 	skey.getEncoded());
	      System.out.println(r);
	  }
	  
}
