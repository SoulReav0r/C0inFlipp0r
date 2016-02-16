package de.fhwedel.ssl.trustmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class TrustManagerMethods {

	public static TrustManager[] getTrustManagers(String fnTrust) throws NoSuchAlgorithmException, CertificateException,
			IOException, KeyStoreException, NoSuchProviderException {

		KeyStore keystore;

		TrustManagerFactory tmf;

		File trustFile = new File(fnTrust);
		FileInputStream inKeystore;
		inKeystore = new FileInputStream(trustFile);
		keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(inKeystore, null);

		tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keystore);

		return tmf.getTrustManagers();

	}

	public static TrustManager[] getTrustManagers1(String fnTrust) throws Exception {
		return new TrustManager[] { new ReloadTrustManager(fnTrust) };
	}
}
