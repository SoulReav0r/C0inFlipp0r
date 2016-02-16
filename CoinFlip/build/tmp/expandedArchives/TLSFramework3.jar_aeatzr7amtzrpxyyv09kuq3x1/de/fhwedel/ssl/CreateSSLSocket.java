package de.fhwedel.ssl;

import java.security.PublicKey;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import de.fhwedel.ssl.config.ApplySecuritySettings;
import de.fhwedel.ssl.keymanager.KeyManagerMethods;
import de.fhwedel.ssl.trustmanager.TrustManagerMethods;

public class CreateSSLSocket {

	public static SSLSocket getSSLSocket(String host, int port, String keyStore, String keyStorePassword,
			String trustStore) throws Exception {

		// Disable Algorithms
		ApplySecuritySettings.setdisabledAlgorithms();

		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

		sslContext.init(KeyManagerMethods.getKeyManagers(keyStore, keyStorePassword),
				TrustManagerMethods.getTrustManagers(trustStore),
				new SecureRandom());
		
		SSLSocketFactory sslsocketfactory = sslContext.getSocketFactory();
		SSLSocket sslSocket = (SSLSocket) sslsocketfactory.createSocket(host, port);

		// Disable cipher suites
		ApplySecuritySettings.setFilteredCipher(sslSocket);
		

//		for (String protocol : sslSocket.getEnabledProtocols()) {
//			System.out.println("Client: " + protocol); // prints "TLSv1.2"
//		}
//
//		for (String cipher : sslSocket.getEnabledCipherSuites()) {
//			System.out.println("Client: " + cipher);
//		}

		return sslSocket;
	}
	
	public static PublicKey getPublicKey(SSLSocket socket) throws SSLPeerUnverifiedException {
		return socket.getSession().getPeerCertificates()[0].getPublicKey();
	}
}
