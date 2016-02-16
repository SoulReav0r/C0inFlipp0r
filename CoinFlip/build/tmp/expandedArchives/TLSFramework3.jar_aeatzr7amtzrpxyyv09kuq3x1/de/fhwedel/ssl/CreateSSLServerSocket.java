package de.fhwedel.ssl;

import java.security.PublicKey;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import de.fhwedel.ssl.config.ApplySecuritySettings;
import de.fhwedel.ssl.keymanager.KeyManagerMethods;
import de.fhwedel.ssl.trustmanager.TrustManagerMethods;;

public class CreateSSLServerSocket {

	public static SSLServerSocket GetSSLServerSocket(int port, String keyStore, String keyStorePassword,
			String trustStore) throws Exception {

		// ApplySecuritySettings.printdisabledAlgorithms();
		ApplySecuritySettings.setdisabledAlgorithms();
		// ApplySecuritySettings.printdisabledAlgorithms();

		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(KeyManagerMethods.getKeyManagers(keyStore, keyStorePassword),
				TrustManagerMethods.getTrustManagers(trustStore),
				new SecureRandom());

		SSLServerSocketFactory sslserversocketfactory = sslContext.getServerSocketFactory();
		SSLServerSocket sslServerSocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(port);

		// force client mutual authentication
		sslServerSocket.setNeedClientAuth(true);

		// Disable cipher suites
		ApplySecuritySettings.setFilteredCipher(sslServerSocket);

//		for (String protocol : sslServerSocket.getSSLParameters().getProtocols()) {
//			System.out.println("Server: " + protocol); // prints "TLSv1.2"
//		}
//
//		for (String cipher : sslServerSocket.getSSLParameters().getCipherSuites()) {
//			System.out.println("Server: " + cipher);
//		}

		return sslServerSocket;
	}

	public static PublicKey getPublicKey(SSLSocket socket) throws SSLPeerUnverifiedException {
		return socket.getSession().getPeerCertificates()[0].getPublicKey();
	}

}
