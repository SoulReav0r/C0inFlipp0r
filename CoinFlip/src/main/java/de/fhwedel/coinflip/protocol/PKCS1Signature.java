package de.fhwedel.coinflip.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class PKCS1Signature {

	public static PrivateKey readPrivateKeyFromFile(String filename, String password)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException,
			IOException, UnrecoverableKeyException {
		File keystoreFile = new File(filename);

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(keystoreFile), password.toCharArray());

		String alias = ks.aliases().nextElement();

		Key key = ks.getKey(alias, password.toCharArray());
		return (PrivateKey) key;
	}

	public static PublicKey readPublicKeyFromFile(String filename, String password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
		File keystoreFile = new File(filename);

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(keystoreFile), password.toCharArray());

		String alias = (ks.aliases()).nextElement();

		Certificate cert = ks.getCertificate(alias);
		return cert.getPublicKey();
	}

	/**
	 * @param message
	 * @param privKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static byte[] sign(String message, PrivateKey privKey)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
		Security.addProvider(new BouncyCastleProvider());

		Signature signature = Signature.getInstance("SHA256with" + privKey.getAlgorithm(), "BC");
		signature.initSign(privKey);
		signature.update(message.getBytes());

		return signature.sign();

	}

	/**
	 * @param message
	 * @param sigBytes
	 * @param pubKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 * @throws SignatureException
	 */
	public static boolean verify(String message, byte[] sigBytes, PublicKey pubKey)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException, SignatureException {
		Security.addProvider(new BouncyCastleProvider());

		Signature signer = Signature.getInstance("SHA256with" + pubKey.getAlgorithm(), "BC");
		signer.initVerify(pubKey);
		signer.update(message.getBytes());

		return signer.verify(sigBytes);
	}

	public static void main(String[] args)
			throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
			FileNotFoundException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Security.addProvider(new BouncyCastleProvider());

		String KEY_ANNA = "alice.private";
		String PASSWORD_KEY = "PW";

		PrivateKey privKey = PKCS1Signature.readPrivateKeyFromFile(KEY_ANNA, PASSWORD_KEY);

		PublicKey pubKey = PKCS1Signature.readPublicKeyFromFile(KEY_ANNA, PASSWORD_KEY);

		String message = "Tail";
		byte[] signedData = PKCS1Signature.sign(message, privKey);

		System.out.println(PKCS1Signature.verify(message, signedData, pubKey));
	}
}