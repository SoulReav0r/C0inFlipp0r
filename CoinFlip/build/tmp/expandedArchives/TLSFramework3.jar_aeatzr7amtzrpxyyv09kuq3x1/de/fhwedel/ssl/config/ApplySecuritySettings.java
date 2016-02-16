package de.fhwedel.ssl.config;

import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class ApplySecuritySettings {

	// https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#InstallationAndCustomization

	public static void printdisabledAlgorithms() {
		System.out.println(java.security.Security.getProperty("jdk.tls.disabledAlgorithms"));
		System.out.println(java.security.Security.getProperty("jdk.certpath.disabledAlgorithms"));
	}

	public static void setdisabledAlgorithms() {
		// Default for openJDK1.7 : SSLv3, DH keySize < 768
		java.security.Security.setProperty("jdk.tls.disabledAlgorithms",
				"SSLv2Hello, SSLv3, TLSv1, TLSv1.1, DH keySize < 1024");

		// Default for openJDK1.7 : MD2, RSA keySize < 1024
		// MD5 removed
		java.security.Security.setProperty("jdk.certpath.disabledAlgorithms", "MD2, DSA, RSA keySize < 2048");
		
	}

	public static void setFilteredCipher(Object sslSocket) {
		String[] ciphers;
		if (sslSocket instanceof SSLSocket) {
			ciphers = ((SSLSocket)sslSocket).getEnabledCipherSuites();
		}
		else {
			ciphers = ((SSLServerSocket)sslSocket).getEnabledCipherSuites();
		}
		
		ArrayList<String> list = new ArrayList<String>();

		// TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384
		// TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384
		// TLS_RSA_WITH_AES_256_CBC_SHA256
		// TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384
		// TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384
		// TLS_DHE_RSA_WITH_AES_256_CBC_SHA256
		// TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA
		// TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA
		// TLS_RSA_WITH_AES_256_CBC_SHA
		// TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA
		// TLS_ECDH_RSA_WITH_AES_256_CBC_SHA
		// TLS_DHE_RSA_WITH_AES_256_CBC_SHA
		// TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256
		// TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256
		// TLS_RSA_WITH_AES_128_CBC_SHA256
		// TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256
		// TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256
		// TLS_DHE_RSA_WITH_AES_128_CBC_SHA256
		// TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA
		// TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
		// TLS_RSA_WITH_AES_128_CBC_SHA
		// TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA
		// TLS_ECDH_RSA_WITH_AES_128_CBC_SHA
		// TLS_DHE_RSA_WITH_AES_128_CBC_SHA
		// TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA
		// TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA
		// SSL_RSA_WITH_3DES_EDE_CBC_SHA
		// TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA
		// TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA
		// SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA
		// TLS_EMPTY_RENEGOTIATION_INFO_SCSV

		for (String cipher : ciphers) {
			if (cipher.startsWith("SSL") || cipher.contains("3DES") || cipher.endsWith("SHA")) {
				break;
			}
			
			list.add(cipher);
		}
//		list.clear();
//		list.add("TLS_RSA_WITH_AES_128_CBC_SHA");

		String[] array = new String[list.size()];
		array = list.toArray(array);

		if (sslSocket instanceof SSLSocket) {
			((SSLSocket) sslSocket).setEnabledCipherSuites(array);
		}
		else {
			((SSLServerSocket)sslSocket).setEnabledCipherSuites(array);
		}
	}
}
