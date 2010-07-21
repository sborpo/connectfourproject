package common;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

/**
 * RSA manager class.
 */
public class RSAgenerator {
	static private Key publicKey = null;
	static private Key privateKey = null;

	/**
	 * Generates a pair of public/private keys.
	 * @throws NoSuchAlgorithmException
	 */
	static public void generatePair() throws NoSuchAlgorithmException{
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.genKeyPair();
		publicKey = kp.getPublic();
		privateKey = kp.getPrivate();
	}
	
	/**
	 * Sets a public keyfor the class.
	 * @param pubKey
	 */
	static public void setEncKey(Key pubKey){
		publicKey = pubKey;
	}
	
	/**
	 * Gets a public key of the class.
	 * @return
	 */
	static public Key getPubKey(){
		return publicKey;
		
	}
	
	/**
	 * Encrypts a message with the public key.
	 * @param msg
	 * @return encrypted message
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	static public String encrypt(String msg) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException{
		/* Create the cipher */
		javax.crypto.Cipher rsaCipher = javax.crypto.Cipher.getInstance("RSA");

		// Initialize the cipher for encryption
		rsaCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
		
		BASE64Decoder de = new BASE64Decoder();
		
		byte[] enc= rsaCipher.doFinal(de.decodeBuffer(msg));
		
		BASE64Encoder en = new BASE64Encoder();
		return en.encodeBuffer(enc);
	}
	
	/**
	 * Decrypts the encrypted message with the private key.
	 * @param msg
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
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
