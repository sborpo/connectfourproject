package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class AESmanager {

	static private Cipher aesCipher = null;
	static final private String keyValue = "12123qssd14d3fgghjuuy77yyrdfllmm";
	SecretKeySpec aeskeySpec;
	
	public AESmanager(){
		aeskeySpec = new SecretKeySpec(keyValue.getBytes(), "AES");
	}
	
	public CipherOutputStream getEncryptedOutStream(File out,String hashedPass) throws IOException, InvalidKeyException {		
	    aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
	    
	    CipherOutputStream os = new CipherOutputStream(new FileOutputStream(out), aesCipher);
	    
	    return os;
	  }
	  
	  public CipherInputStream getDecryptedInStream(File in) throws IOException, InvalidKeyException {
	    aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
	    
	    CipherInputStream is = new CipherInputStream(new FileInputStream(in), aesCipher);
	    
	    return is;
	  }
	  
}
