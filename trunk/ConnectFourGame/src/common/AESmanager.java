package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class AESmanager {

	static private Cipher aesCipher = null;
	static final private String keyValue = "Py†ôDBõ^È~+Q²";

	static private SecretKeySpec aeskeySpec;
	
	static private void getInstance(){
		if(aesCipher != null){
			return;
		}
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
	
	static public CipherOutputStream getEncryptedOutStream(File out) throws FileNotFoundException{		
	    return getEncryptedOutStream(new FileOutputStream(out));
	}
	
	public static  CipherOutputStream getEncryptedOutStream(OutputStream out){	
		getInstance();
	    try {
			aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    CipherOutputStream os = null;
	    os = new CipherOutputStream(out, aesCipher);
	    return os;
    }
	
	static public CipherInputStream getDecryptedInStream(File in) throws FileNotFoundException  {
		return  getDecryptedInStream(new FileInputStream(in));
	  }
	  
	static public CipherInputStream getDecryptedInStream(InputStream in)  {
	    getInstance();
	    try {
			aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    CipherInputStream is = null;
		is = new CipherInputStream(in, aesCipher);
	    
	    return is;
	  }
	  
//	public static void   printKey(){
//
//	       // Get the KeyGenerator
//
//	       KeyGenerator kgen = null;
//		try {
//			kgen = KeyGenerator.getInstance("AES");
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	       kgen.init(128); // 192 and 256 bits may not be available
//
//
//	       // Generate the secret key specs.
//	       SecretKey skey = kgen.generateKey();
//	      String r = new String( 	skey.getEncoded());
//	      System.out.println(r);
//	  }
	  
}
