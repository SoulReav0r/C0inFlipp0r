package de.fhwedel.coinflip.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.net.ssl.SSLSocket;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.fhwedel.coinflip.protocol.CoinFlipProtocol;
import de.fhwedel.coinflip.protocol.Message;
import de.fhwedel.coinflip.protocol.PKCS1Signature;
import de.fhwedel.coinflip.protocol.Status;
import de.fhwedel.ssl.CreateSSLSocket;

public class GameClient implements Runnable {

	private String HOST;
	private int PORT;
	private String TRUSTFILE;
	private String OWNCERT;
	private String OWNPW;
	private boolean DEBUG;

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());

		String hostName = "localhost";
		int portNumber = 6666;

		boolean verbose = true;

		try {

			new Thread(new GameClient(hostName, portNumber, "ssl-certs/root", "ssl-certs/client", "fhwedel", verbose))
					.start();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public GameClient(String HOST, int PORT, String TRUSTFILE, String OWNCERT, String OWNPW, boolean VERBOSE) {

		this.HOST = HOST;
		this.PORT = PORT;
		this.TRUSTFILE = TRUSTFILE;
		this.OWNCERT = OWNCERT;
		this.OWNPW = OWNPW;
		this.DEBUG = VERBOSE;
	}

	@Override
	public void run() {
		try {

			SSLSocket socket = CreateSSLSocket.getSSLSocket(HOST, PORT, OWNCERT, OWNPW, TRUSTFILE);

			PrivateKey ownPrivateKey = PKCS1Signature.readPrivateKeyFromFile(OWNCERT, OWNPW);
			PublicKey foreignPublicKey = CreateSSLSocket.getPublicKey(socket);

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			ObjectMapper objectMapper = new ObjectMapper();
			
			objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
			objectMapper.setSerializationInclusion(Include.NON_NULL);

			objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);

			ObjectWriter writer = objectMapper.writer().withDefaultPrettyPrinter();

			CoinFlipProtocol cfp = new CoinFlipProtocol(ownPrivateKey, foreignPublicKey);

			// Build InitMessage
			objectMapper.writeValue(out, cfp.createMessageInit());
			out.println("");
			out.flush();
			System.out.println("Protocol Version 1.0");
			if (DEBUG) {
				System.out.println("FromClient:\n" + writer.writeValueAsString(cfp.createMessageInit()));
			}
			JsonNode fromServer;

			while (cfp.getStatus() == Status.PROTOCOL_OK) {
				fromServer = objectMapper.readTree(in);

				if (DEBUG)
					System.out.println("\nFromServer:\n" + writer.writeValueAsString(fromServer));

				Message fromClient = cfp.processInput(fromServer);
				if (fromClient == null) {
					break;
				} else {
					objectMapper.writeValue(out, fromClient);
					out.println("");
					out.flush();
					if (DEBUG) {
						System.out.println("\nFromClient:" + writer.writeValueAsString(fromClient));
					}
					if (cfp.getStatus() == Status.PROTOCOL_ERROR) {
						System.out.println("\nFromClient:" + writer.writeValueAsString(fromClient));
						break;
					}
				}
			}
			in.close();
			out.close();
			socket.close();
			
		} catch (UnknownHostException e) {
			System.err.println("Server not found " + HOST);

		} catch (IOException e) {
			System.err.println("connection refused with " + HOST);
			// e.printStackTrace();

		} catch (Exception e) {
			System.err.println("something really bad happens...");

			e.printStackTrace();

		}
		
	}
}