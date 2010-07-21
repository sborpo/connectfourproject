package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class represents the AES encrypt manager.
 */
public class AESmanager {

	static private Cipher aesCipher = null;
	static final private String keyValue = "Py†ôDBõ^È~+Q²";

	static private SecretKeySpec aeskeySpec;
	
	/**
	 * Gets an instance of the encryption class.
	 */
	static private void getInstance(){
		if(aesCipher != null){
			return;
		}
		aeskeySpec = new SecretKeySpec(keyValue.getBytes(), "AES");
		try {
			aesCipher=Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			//MUST WORK PROPERTLY
		} catch (NoSuchPaddingException e) {
			//MUST WORK PROPERTLY
		}
	}
	
	/**
	 * Returns an encrypted output stream for a file.
	 * @param out
	 * @return encrypted output stream
	 * @throws FileNotFoundException
	 */
	static public CipherOutputStream getEncryptedOutStream(File out) throws FileNotFoundException{		
	    return getEncryptedOutStream(new FileOutputStream(out));
	}
	
	/**
	 * Returns an encrypted output stream for a regular
	 * output stream.
	 * @param out
	 * @return os
	 * @throws FileNotFoundException
	 */
	public static  CipherOutputStream getEncryptedOutStream(OutputStream out){	
		getInstance();
	    try {
			aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
		} catch (InvalidKeyException e) {
			//MUST NOT HAPPEND
		}
	    
	    CipherOutputStream os = null;
	    os = new CipherOutputStream(out, aesCipher);
	    return os;
    }
	
	/**
	 * Returns a decrypted input stream for a file.
	 * @param in
	 * @return decrypted input stream
	 * @throws FileNotFoundException
	 */
	static public CipherInputStream getDecryptedInStream(File in) throws FileNotFoundException  {
		return  getDecryptedInStream(new FileInputStream(in));
	  }
	 
	/**
	 * Returns an decrypted input stream for a regular
	 * input stream.
	 * @param in
	 * @return is
	 * @throws FileNotFoundException
	 */
	static public CipherInputStream getDecryptedInStream(InputStream in)  {
	    getInstance();
	    try {
			aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
		} catch (InvalidKeyException e) {
			//MUST NOT HAPPEND
		}
	    
	    CipherInputStream is = null;
		is = new CipherInputStream(in, aesCipher);
	    
	    return is;
	  }
	  
}
