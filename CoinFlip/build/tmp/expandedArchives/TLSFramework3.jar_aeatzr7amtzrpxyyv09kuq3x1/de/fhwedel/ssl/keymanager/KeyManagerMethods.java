package de.fhwedel.ssl.keymanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

public class KeyManagerMethods {

	public static KeyManager[] getKeyManagers(String fnKey, String pwKey) throws NoSuchAlgorithmException,
			CertificateException, IOException, KeyStoreException, UnrecoverableKeyException {
		KeyStore keystore;

		KeyManagerFactory kmf;

		File keyFile = new File(fnKey);
		FileInputStream inKeystore = new FileInputStream(keyFile);
		keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(inKeystore, pwKey.toCharArray());

		kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keystore, pwKey.toCharArray());

		return kmf.getKeyManagers();
	}
}
