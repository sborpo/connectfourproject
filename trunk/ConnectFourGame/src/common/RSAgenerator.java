package common;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

public class RSAgenerator {
	static private Key publicKey = null;
	static private Key privateKey = null;

	static public void generatePair() throws NoSuchAlgorithmException{
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.genKeyPair();
		publicKey = kp.getPublic();
		privateKey = kp.getPrivate();
	}
	
	static public void setEncKey(Key pubKey){
		publicKey = pubKey;
	}
	
	static public Key getPubKey(){
		return publicKey;
		
	}
	
	static public Key getPrvKey(){
		return privateKey;
	}
	
	static public String encrypt(String msg) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException{
		/* Create the cipher */
		javax.crypto.Cipher rsaCipher = javax.crypto.Cipher.getInstance("RSA");

		// Initialize the cipher for encryption
		rsaCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
		
		BASE64Decoder de = new BASE64Decoder();
		
		byte[] enc= rsaCipher.doFinal(de.decodeBuffer(msg));
		// Now here i am using BASE64Encryptor and BASE64Decoder as specified by you
		
		BASE64Encoder en = new BASE64Encoder();
		return en.encodeBuffer(enc);
	}
	
	static public String decrypt(String msg) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException{
		/* Create the cipher */
		javax.crypto.Cipher rsaCipher = javax.crypto.Cipher.getInstance("RSA");

		// Initialize the cipher for encryption
		rsaCipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);
		
		//String decrypted = new String(rsaCipher.doFinal(msg));
		BASE64Decoder de = new BASE64Decoder();
		
		byte[] dec=rsaCipher.doFinal(de.decodeBuffer(msg));
		
		BASE64Encoder en = new BASE64Encoder();
		return en.encodeBuffer(dec);
	}

}
