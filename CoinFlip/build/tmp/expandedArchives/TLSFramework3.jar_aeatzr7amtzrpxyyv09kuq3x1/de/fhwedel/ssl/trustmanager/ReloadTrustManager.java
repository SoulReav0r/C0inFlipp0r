package de.fhwedel.ssl.trustmanager;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

class ReloadTrustManager implements X509TrustManager {
	private final String trustStorePath;
	private X509TrustManager trustManager;
	private List<Certificate> tempCertList = new ArrayList<Certificate>();

	public ReloadTrustManager(String tspath) throws Exception {
		this.trustStorePath = tspath;
		reloadTrustManager();
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		
		try {
			trustManager.checkClientTrusted(chain, authType);
		} catch (CertificateException cx) {
			addServerCertAndReload(chain[0]);
			trustManager.checkClientTrusted(chain, authType);
		}		
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		
		try {
			trustManager.checkServerTrusted(chain, authType);
		} catch (CertificateException cx) {
			addServerCertAndReload(chain[0]);
			trustManager.checkServerTrusted(chain, authType);
		}
		
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		X509Certificate[] issuers = trustManager.getAcceptedIssuers();
		return issuers;
	}

	private void reloadTrustManager() throws Exception {
		// load keystore from specified cert store (or default)
		KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in = new FileInputStream(trustStorePath);
		try {
			ts.load(in, null);
		} finally {
			in.close();
		}

		// add all temporary certs to KeyStore (ts)
		for (Certificate cert : tempCertList) {
			ts.setCertificateEntry(UUID.randomUUID().toString(), cert);
		}

		// initialize a new TMF with the ts we just loaded
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ts);

		// acquire X509 trust manager from factory
		TrustManager tms[] = tmf.getTrustManagers();
		for (int i = 0; i < tms.length; i++) {
			if (tms[i] instanceof X509TrustManager) {
				trustManager = (X509TrustManager) tms[i];
				return;
			}
		}
		
		throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
	}

	private void addServerCertAndReload(Certificate cert) {
		tempCertList.add(cert);
		try {
			reloadTrustManager();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}