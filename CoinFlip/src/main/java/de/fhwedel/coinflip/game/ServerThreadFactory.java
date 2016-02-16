package de.fhwedel.coinflip.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.net.ssl.SSLSocket;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.fhwedel.coinflip.protocol.CoinFlipProtocol;
import de.fhwedel.coinflip.protocol.Message;
import de.fhwedel.coinflip.protocol.Status;

public class ServerThreadFactory implements Runnable {
	private SSLSocket sslsocket;
	private Boolean verbose = true;
	private PrivateKey ownPrivateKey;
	private PublicKey foreignPublicKey;
	private CoinFlipProtocol cfp;

	public ServerThreadFactory(SSLSocket socket, PrivateKey ownPrivateKey, PublicKey foreignPublicKey, boolean verbose) {
		this.sslsocket = socket;
		this.ownPrivateKey = ownPrivateKey;
		this.foreignPublicKey = foreignPublicKey;
		this.cfp = new CoinFlipProtocol(this.ownPrivateKey, this.foreignPublicKey);
		this.verbose = verbose;
	}

	@Override
	public void run() {
		try {
			PrintWriter out;
			BufferedReader in;

			
				out = new PrintWriter(this.sslsocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(this.sslsocket.getInputStream()));
			
			
			ObjectMapper objectMapper = new ObjectMapper();
			// Settings are necessary because readValue and writeValue close the
			// socket...
			objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
			objectMapper.setSerializationInclusion(Include.NON_NULL);
			
			objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, false);

			// PrettyPrinting
			ObjectWriter writer = objectMapper.writer().withDefaultPrettyPrinter();

			JsonNode fromClient;

			// while (!((fromClient = objectMapper.readTree(in)).isNull())) {
			while (cfp.getStatus() == Status.PROTOCOL_OK) {
				fromClient = objectMapper.readTree(in);
				if (verbose)
					System.out.println("\nFromAlice:" + writer.writeValueAsString(fromClient));

				Message fromServer = cfp.processInput(fromClient);
				if (fromServer == null) {
					break;
				} else {
					objectMapper.writeValue(out, fromServer);
					out.println("");
					out.flush();
					if (verbose) {
						System.out.println("\nFromBob:" + writer.writeValueAsString(fromServer));
					}
					if (cfp.getStatus() == Status.PROTOCOL_ERROR) {
						// if (fromServer.getStatusId() != 0) {
						System.out.println("\nFromBob:" + writer.writeValueAsString(fromServer));
						break;

					}

					if (cfp.getStatus() == Status.PROTOTOCOL_FINISHED) {
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}