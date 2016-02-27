package de.fhwedel.coinflip.game;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.fhwedel.coinflip.protocol.PKCS1Signature;
import de.fhwedel.ssl.CreateSSLServerSocket;
import de.fhwedel.ssl.CreateSSLSocket;
import gr.planetz.PingingService;
import gr.planetz.impl.HttpPingingService;

public class GameServer {

	static int port = 0;
	public static Runnable main(String[] args) throws IOException {
		Security.addProvider(new BouncyCastleProvider());

		Properties prop = new Properties();
		prop.load(new FileInputStream("coinflip_config.conf"));

		port = Integer.parseInt(prop.getProperty("server_port"));
		String rootcert = prop.getProperty("server_tlsRootCert");
		String servercert = prop.getProperty("server_tlsClientCert");
		String servercertpw = prop.getProperty("server_tlsClientCertPW");
		
		boolean verbose = Boolean.parseBoolean(prop.getProperty("server_verbose"));


		if (Boolean.parseBoolean(prop.getProperty("server_useBroker"))) {
			if (Boolean.parseBoolean(prop.getProperty("server_enableMD5"))){
				java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, DH keySize < 768");
				java.security.Security.setProperty("jdk.certpath.disabledAlgorithms", "MD2, RSA keySize < 1024");	
			}
			try {
				String address;
				
				if (prop.getProperty("server_brokerExplicitAdress").compareTo("none")==0) {
					URL whatismyip = new URL("http://checkip.amazonaws.com");
				BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

				address = in.readLine() + port;

				
				}
				else{
					address = prop.getProperty("server_brokerExplicitAdress");
				}
					
				System.out.println("Adress: " + address);

				final PingingService service = new HttpPingingService(prop.getProperty("server_brokerURL"), prop.getProperty("server_brokerName"), address,
						prop.getProperty("server_brokerCert"), prop.getProperty("server_brokerCertPW"));
				service.start();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			new GameServer(port, rootcert, servercert, servercertpw, verbose);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public GameServer(int PORT, String TRUSTFILE, String OWNCERT, String OWNPW, boolean VERBOSE) throws Exception {

		SSLServerSocket tlsServerSocket = CreateSSLServerSocket.GetSSLServerSocket(PORT, OWNCERT, OWNPW, TRUSTFILE);
		System.out.println("Server is up on Port: " + port);

		while (true) {
			try {
				SSLSocket socket = (SSLSocket) tlsServerSocket.accept();

				PrivateKey ownPrivateKey = PKCS1Signature.readPrivateKeyFromFile(OWNCERT, OWNPW);
				PublicKey foreignPublicKey = CreateSSLSocket.getPublicKey(socket);

				System.out.println("New connection accepted from" + socket.getInetAddress() + ":" + socket.getPort() + " "
						+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));

				ServerThreadFactory request = new ServerThreadFactory(socket, ownPrivateKey, foreignPublicKey, VERBOSE);

				Thread thread = new Thread(request);
				thread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
}