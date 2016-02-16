package de.fhwedel.coinflip.protocol;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jcajce.provider.asymmetric.sra.SRADecryptionKeySpec;
import org.bouncycastle.jcajce.provider.asymmetric.sra.SRAEncryptionKeySpec;
import org.bouncycastle.jcajce.provider.asymmetric.sra.SRAKeyGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CoinFlipProtocol {
	private KeyPair keypair;
	private KeyPair foreignKeypair;
	private Sids sids;
	private Message previousMessage;
	private Status status;
	private PrivateKey ownPrivateKey;
	private PublicKey foreignPublicKey;
	private boolean DEBUG = false;
	private boolean isWinner = false;
	
	public CoinFlipProtocol(PrivateKey ownPrivateKey, PublicKey foreignPublicKey) {
		this.status = Status.PROTOCOL_OK;
		this.ownPrivateKey = ownPrivateKey;
		this.foreignPublicKey = foreignPublicKey;
	}
	
	public Status getStatus() {
		return status;
	}

	public Message processInput(JsonNode input) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);

		try {
			// ErrorMessage
			if (input.path("statusId").asInt() != 0) {
				return null;
			} else {
				// Verify protocol version
				if (input.path("protocolNegotiation").path("version").isNull()
						|| input.path("protocolNegotiation").path("version").isMissingNode()) {
					return _processInput(objectMapper.treeToValue(input, MessageInit.class));
				} else if (input.path("protocolNegotiation").path("version").asText().equals("1.0")) {
					return _processInput(objectMapper.treeToValue(input, MessageExtendV1.class));
				} else {
					throw new ProtocolException("Protocol version not found");
				}
			}
		} catch (JsonProcessingException | ProtocolException e) {
			this.status = Status.PROTOCOL_ERROR;
			MessageInit error = new MessageInit();
			error.setStatusId(1);
			error.setStatusMessage(e.getMessage());
			return error;
		}
	}

	public MessageInit createMessageInit() {
		MessageInit m = new MessageInit();
		m.setStatusId(0);
		m.setStatusMessage("OK");

		m.getProtocolNegotiation().getAvailableVersions().add(AvailableVersions.Builder().addElement("1.0").build());

		this.previousMessage = m;
		return m;
	}

	private Message _processInput(MessageInit theInput) {
		try {
			switch (theInput.getProtocolId()) {

			case 0:
				// verify initial message
				if (theInput.getProtocolNegotiation() == null) {
					throw new ProtocolException("Protocol negotiation is missing");
				}
				if (theInput.getProtocolNegotiation().getAvailableVersions() == null) {
					throw new ProtocolException("Protocol negotiation list is missing");
				}
				if (theInput.getProtocolNegotiation().getAvailableVersions().size() != 1) {
					throw new ProtocolException("Wrong protocol negotiation list size");
				}

				// create response
				theInput.setProtocolId(1);
				theInput.getProtocolNegotiation().getAvailableVersions()
						.add(AvailableVersions.Builder().addElement("1.0").build());

				theInput.getProtocolNegotiation()
						.setVersion(getCommonProtocolVersion(theInput.getProtocolNegotiation().getAvailableVersions()));
				
				//Server
				System.out.println(
						"InitResponse: Protocol list " + theInput.getProtocolNegotiation().getAvailableVersions().get(1)
								+ " and choose protocol: " + theInput.getProtocolNegotiation().getVersion());
				this.previousMessage = theInput;
				
				break;
			default:
				theInput.setStatusId(1);
				theInput.setStatusMessage("Unknown protocol step in protocol negotiation");
				break;
			}
		} catch (ProtocolException e) {
			this.status = Status.PROTOCOL_ERROR;
			MessageInit error = new MessageInit();
			error.setStatusId(1);
			error.setStatusMessage(e.getMessage());
			return error;
		}
		return theInput;
	}

	private Message _processInput(MessageExtendV1 theInput) {
		try {
			// verify
			if (!((theInput.getProtocolId() - 1) == ((MessageInit) this.previousMessage).getProtocolId())) {
				throw new ProtocolException("Protocol id error");
			}

			switch (theInput.getProtocolId()) {
			case 1:
				if (!(((MessageInit) this.previousMessage).getProtocolNegotiation().getAvailableVersions().get(0)
						.equals(theInput.getProtocolNegotiation().getAvailableVersions().get(0)))) {
					throw new ProtocolException("Protocol list change");
				}
				if (!(theInput.getProtocolNegotiation().getAvailableVersions().get(0).getVersions()
						.contains(theInput.getProtocolNegotiation().getVersion()))) {
					throw new ProtocolException("Wrong protocol choose");
				}
				if (theInput.getProtocolNegotiation().getAvailableVersions().size() != 2) {
					throw new ProtocolException("Wrong protocol negotiation list size");
				}
				
				break;
			case 2:
				if (!(((MessageInit) this.previousMessage).getProtocolNegotiation()
						.equals(theInput.getProtocolNegotiation()))) {
					throw new ProtocolException("Protocol negotiation data changed");
				}
				if (theInput.getKeyNegotiation().getAvailableSids().size() != 1) {
					throw new ProtocolException("Wrong key negotiation sid list size");
				}
				break;
			case 3:
				if (!(((MessageInit) this.previousMessage).getProtocolNegotiation()
						.equals(theInput.getProtocolNegotiation()))) {
					throw new ProtocolException("Protocol negotiation data changed");
				}
				if (theInput.getKeyNegotiation().getAvailableSids().size() != 2) {
					throw new ProtocolException("Wrong key negotiation sid list size");
				}
				if (!(((MessageExtendV1) this.previousMessage).getKeyNegotiation().getAvailableSids().get(0)
						.equals(theInput.getKeyNegotiation().getAvailableSids().get(0)))) {
					throw new ProtocolException("Key list change");
				}
				if (!(theInput.getKeyNegotiation().getAvailableSids().get(0).getSids()
						.contains(theInput.getKeyNegotiation().getSid()))) {
					throw new ProtocolException("Wrong key choose");
				}
				// verfiy p&q via key generator
				if (DEBUG){
				System.out.println("Server Sid List: " + theInput.getKeyNegotiation().getAvailableSids().get(1));
				System.out.println("Sid: " + theInput.getKeyNegotiation().getSid());
				}
				break;
			case 4:
				if (!(((MessageInit) this.previousMessage).getProtocolNegotiation()
						.equals(theInput.getProtocolNegotiation()))) {
					throw new ProtocolException("Protocol negotiation data changed");
				}
				if (!(((MessageExtendV1) this.previousMessage).getKeyNegotiation()
						.equals(theInput.getKeyNegotiation()))) {
					throw new ProtocolException("Key negotiation data changed");
				}

				// verify initial coins
				if (theInput.getPayload().getInitialCoin().get(0).isEmpty()
						|| theInput.getPayload().getInitialCoin().get(1).isEmpty()) {
					throw new ProtocolException("Initial keys empty");
				}

				if (theInput.getPayload().getInitialCoin().get(0)
						.equals(theInput.getPayload().getInitialCoin().get(1))) {
					throw new ProtocolException("Initial keys are equal");
				}

				// verify
				if (theInput.getPayload().getEncryptedCoin().size() != 2) {
					throw new ProtocolException("Encrypted coins missing");
				}

				break;
			case 5:
				if (!(((MessageInit) this.previousMessage).getProtocolNegotiation()
						.equals(theInput.getProtocolNegotiation()))) {
					throw new ProtocolException("Protocol negotiation data changed");
				}
				if (!(((MessageExtendV1) this.previousMessage).getKeyNegotiation()
						.equals(theInput.getKeyNegotiation()))) {
					throw new ProtocolException("Key negotiation data changed");
				}
				// check enchoosencoin
				if (theInput.getPayload().getEnChosenCoin().isEmpty()) {
					throw new ProtocolException("EnChoosenCoin is missing");
				}
				if (!(theInput.getPayload().getInitialCoin().contains(theInput.getPayload().getDesiredCoin()))) {
					throw new ProtocolException("DesiredCoin not found");
				}

				if (!(((MessageExtendV1) this.previousMessage).getPayload().getInitialCoin()
						.equals(theInput.getPayload().getInitialCoin())
						&& ((MessageExtendV1) this.previousMessage).getPayload().getEncryptedCoin()
								.equals(theInput.getPayload().getEncryptedCoin()))) {
					throw new ProtocolException("Payload data changed 5");

				}

				break;
			case 6:
				if (!(((MessageInit) this.previousMessage).getProtocolNegotiation()
						.equals(theInput.getProtocolNegotiation()))) {
					throw new ProtocolException("Protocol negotiation data changed");
				}
				if (!(((MessageExtendV1) this.previousMessage).getKeyNegotiation()
						.equals(theInput.getKeyNegotiation()))) {
					throw new ProtocolException("Protocol negotiation data changed");
				}
				// check dechoosencoin
				if (theInput.getPayload().getDeChosenCoin().isEmpty()) {
					throw new ProtocolException("DeChoosenCoin is missing");
				}
				// check alices key
				if (theInput.getPayload().getKeyA().isEmpty()) {
					throw new ProtocolException("Alice key is missing");
				}

				if (!(((MessageExtendV1) this.previousMessage).getPayload().getInitialCoin()
						.equals(theInput.getPayload().getInitialCoin())
						&& ((MessageExtendV1) this.previousMessage).getPayload().getEncryptedCoin()
								.equals(theInput.getPayload().getEncryptedCoin())
						&& ((MessageExtendV1) this.previousMessage).getPayload().getEnChosenCoin()
								.equals(theInput.getPayload().getEnChosenCoin()))) {
					throw new ProtocolException("Payload data changed 6");

				}
				break;
			case 7:
				if (!(((MessageInit) this.previousMessage).getProtocolNegotiation()
						.equals(theInput.getProtocolNegotiation()))) {
					throw new ProtocolException("Protocol negotiation data changed");
				}
				if (!(((MessageExtendV1) this.previousMessage).getKeyNegotiation()
						.equals(theInput.getKeyNegotiation()))) {
					throw new ProtocolException("Protocol negotiation data changed");
				}

				// check bobs key
				if (theInput.getPayload().getKeyB().isEmpty()) {
					throw new ProtocolException("Bobs key is missing");
				}

				if (!(((MessageExtendV1) this.previousMessage).getPayload().getInitialCoin()
						.equals(theInput.getPayload().getInitialCoin())
						&& ((MessageExtendV1) this.previousMessage).getPayload().getEncryptedCoin()
								.equals(theInput.getPayload().getEncryptedCoin())
						&& ((MessageExtendV1) this.previousMessage).getPayload().getEnChosenCoin()
								.equals(theInput.getPayload().getEnChosenCoin())
						&& ((MessageExtendV1) this.previousMessage).getPayload().getDeChosenCoin()
								.equals(theInput.getPayload().getDeChosenCoin()))) {
					throw new ProtocolException("Payload data changed 7");
				}
				break;
			}

			// response
			switch (theInput.getProtocolId()) {
			case 1:
				//Client
				theInput.setProtocolId(2);
				theInput.getKeyNegotiation().getAvailableSids().add(getAvaiableSidList());
				this.previousMessage = theInput;
				if (DEBUG){
				System.out.println("Key list: " + getAvaiableSidList());
				}
				break;
			case 2:
				//Server
				theInput.setProtocolId(3);
				theInput.getKeyNegotiation().getAvailableSids().add(getAvaiableSidList());
				theInput.getKeyNegotiation().setSid(getCommonSid(theInput.getKeyNegotiation().getAvailableSids()));
				if (DEBUG){
				System.out.println(getSid(theInput.getKeyNegotiation().getSid(), this.sids));
				}
				KeyPairGenerator generator = KeyPairGenerator.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);

				generator.initialize(
						Integer.parseInt(getSid(theInput.getKeyNegotiation().getSid(), this.sids).getSraModulus()));
				this.keypair = generator.generateKeyPair();

				KeyFactory factory = KeyFactory.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
				SRADecryptionKeySpec spec = factory.getKeySpec(this.keypair.getPrivate(), SRADecryptionKeySpec.class);

				theInput.getKeyNegotiation().setP(spec.getP());
				theInput.getKeyNegotiation().setQ(spec.getQ());

				this.previousMessage = theInput;
				System.out.println("Server: Send key list and send choosen key + pq");

				break;

			case 3:
				//Client
				theInput.setProtocolId(4);
				SRAKeyGenParameterSpec sraKeyGenParameterSpec2 = new SRAKeyGenParameterSpec(
						Integer.parseInt(getSid(theInput.getKeyNegotiation().getSid(), this.sids).getSraModulus()),
						theInput.getKeyNegotiation().getP(), theInput.getKeyNegotiation().getQ());
				KeyPairGenerator generator2 = KeyPairGenerator.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
				generator2.initialize(sraKeyGenParameterSpec2);
				this.keypair = generator2.generateKeyPair();

				String m1 = "HEAD";
				String m2 = "TAIL";
				theInput.setPayload(new Payload());
				theInput.getPayload().getInitialCoin().add(m1);
				theInput.getPayload().getInitialCoin().add(m2);

				theInput.getPayload().getEncryptedCoin().add(encryptOAEP(m1,
						getSid(theInput.getKeyNegotiation().getSid(), this.sids).getOaepAlg(), this.keypair));
				theInput.getPayload().getEncryptedCoin().add(encryptOAEP(m2,
						getSid(theInput.getKeyNegotiation().getSid(), this.sids).getOaepAlg(), this.keypair));
				Collections.shuffle(theInput.getPayload().getEncryptedCoin(), new SecureRandom());

				this.previousMessage = theInput;
				System.out.println("Send encrypted coin: " + m1 + " " + m2);
				break;
			case 4:
				//Server
				theInput.setProtocolId(5);

				theInput.getPayload().setEnChosenCoin(theInput.getPayload().getEncryptedCoin()
						.get(new SecureRandom().nextInt(theInput.getPayload().getEncryptedCoin().size())));
				theInput.getPayload().setEnChosenCoin(encrypt(theInput.getPayload().getEnChosenCoin(), this.keypair));

				theInput.getPayload().setDesiredCoin(theInput.getPayload().getInitialCoin().get(1));

				this.previousMessage = theInput;
				System.out.println(
						"Server: Send encrypted choise and choise: " + theInput.getPayload().getInitialCoin().get(1));
				break;
			case 5:
				//Client
				theInput.setProtocolId(6);

				theInput.getPayload().setDeChosenCoin(decrypt(theInput.getPayload().getEnChosenCoin(), this.keypair));

				theInput.getPayload().setKeyA(new ArrayList<BigInteger>());
				theInput.getPayload().getKeyA().add(((RSAPublicKey) this.keypair.getPublic()).getPublicExponent());
				theInput.getPayload().getKeyA().add(((RSAPrivateKey) this.keypair.getPrivate()).getPrivateExponent());

				theInput.getPayload().setSignatureA(
						Hex.toHexString(
								PKCS1Signature.sign(
										createString4Signature(
												theInput.getPayload().getDesiredCoin(),
												theInput.getPayload().getEnChosenCoin(), 
												theInput.getPayload().getKeyA().get(0), 
												theInput.getPayload().getKeyA().get(1)
												), this.ownPrivateKey)
								)
							);
				
				this.previousMessage = theInput;
				if (DEBUG){
				System.out.println("Decrypt EnChosenCoin");
				System.out.println("Send own key");
				System.out.println("Create signature");
				}
				break;

			case 6:
				//Server
				theInput.setProtocolId(7);

				this.foreignKeypair = generateForeignKeypair(theInput.getKeyNegotiation().getP(),
						theInput.getKeyNegotiation().getQ(), theInput.getPayload().getKeyA().get(0),
						theInput.getPayload().getKeyA().get(1));

				/// HERE
				System.out.println("Server: Verify Encryption with foreignKey: " + verifyEncryption(theInput));
				
				System.out.println("Server: Verify Signature: " +		PKCS1Signature.verify(
										createString4Signature(
												theInput.getPayload().getDesiredCoin(),
												theInput.getPayload().getEnChosenCoin(), 
												theInput.getPayload().getKeyA().get(0), 
												theInput.getPayload().getKeyA().get(1)
												), 
											Hex.decode(theInput.getPayload().getSignatureA()),
											this.foreignPublicKey)
				);
				
				
				

				theInput.getPayload().setKeyB(new ArrayList<BigInteger>());
				theInput.getPayload().getKeyB().add(((RSAPublicKey) this.keypair.getPublic()).getPublicExponent());
				theInput.getPayload().getKeyB().add(((RSAPrivateKey) this.keypair.getPrivate()).getPrivateExponent());

				this.previousMessage = theInput;
				System.out.println("Server: Send own key and coin result is: "
						+ decryptOAEP(theInput.getPayload().getDeChosenCoin(),
								getSid(theInput.getKeyNegotiation().getSid(), this.sids).getOaepAlg(), this.keypair));

				this.status = Status.PROTOTOCOL_FINISHED;

				break;
			case 7:
				//Client
				this.foreignKeypair = generateForeignKeypair(theInput.getKeyNegotiation().getP(),
						theInput.getKeyNegotiation().getQ(), theInput.getPayload().getKeyB().get(0),
						theInput.getPayload().getKeyB().get(1));

				/// HERE
				System.out.println("Verify foreignKey: " + verifyEncryption(theInput));
				System.out.println("Server: " + theInput.getPayload().getDesiredCoin());
				if (theInput.getPayload().getDesiredCoin().compareTo(

						decryptOAEP(decrypt(theInput.getPayload().getEnChosenCoin(), this.keypair)

				, getSid(theInput.getKeyNegotiation().getSid(), this.sids).getOaepAlg(), this.foreignKeypair)

				) == 0) {
					System.out.println("Result: "
							+ decryptOAEP(decrypt(theInput.getPayload().getEnChosenCoin(), this.keypair)

					, getSid(theInput.getKeyNegotiation().getSid(), this.sids).getOaepAlg(), this.foreignKeypair)+
							"\n\nYOU LOOSE!");

				} else {
					System.out.println("Result: "
							+ decryptOAEP(decrypt(theInput.getPayload().getEnChosenCoin(), this.keypair)

					, getSid(theInput.getKeyNegotiation().getSid(), this.sids).getOaepAlg(), this.foreignKeypair)+
							"\n\nYOU WIN!");
					isWinner = true;
				}

				theInput = null;

				this.status = Status.PROTOTOCOL_FINISHED;

				break;
			default:
				theInput.setStatusId(1);
				theInput.setStatusMessage("Unknown protocol step in key negotiation or payload");
				break;

			}
		} catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException
				| ArrayIndexOutOfBoundsException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidAlgorithmParameterException | IllegalArgumentException
				| ProtocolException | InvalidKeySpecException | SignatureException e) {
			this.status = Status.PROTOCOL_ERROR;
			MessageInit error = new MessageInit();
			error.setStatusId(1);
			error.setStatusMessage(e.getMessage());
			return error;
		}

		return theInput;
	}

	private String getCommonProtocolVersion(List<AvailableVersions> l) throws ProtocolException {
		if (l.get(0).getVersions().isEmpty() || l.get(1).getVersions().isEmpty()) {
			throw new ProtocolException("No common protocol");
		} else {
			for (String k : (l.get(0).getVersions())) {
				if (l.get(1).getVersions().contains(k)) {
					return k;
				}
			}
		}
		throw new ProtocolException("No common protocol");
	}

	public String encrypt(String m, KeyPair k) throws NoSuchAlgorithmException, NoSuchProviderException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
		engine.init(Cipher.ENCRYPT_MODE, k.getPublic());
		return Hex.toHexString(engine.doFinal(DatatypeConverter.parseHexBinary(m)));
	}

	public String decrypt(String m, KeyPair k) throws NoSuchAlgorithmException, NoSuchProviderException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
		engine.init(Cipher.DECRYPT_MODE, k.getPrivate());
		return Hex.toHexString(engine.doFinal(DatatypeConverter.parseHexBinary(m)));
	}

	public String encryptOAEP(String m, String algorithm, KeyPair k)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, ProtocolException {

		Cipher engine = Cipher.getInstance(getAlgorithmString(algorithm), BouncyCastleProvider.PROVIDER_NAME);
		engine.init(Cipher.ENCRYPT_MODE, k.getPublic());
		return Hex.toHexString(engine.doFinal(m.getBytes()));
	}

	public String decryptOAEP(String m, String algorithm, KeyPair k)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, ProtocolException {

		Cipher engine = Cipher.getInstance(getAlgorithmString(algorithm), BouncyCastleProvider.PROVIDER_NAME);
		engine.init(Cipher.DECRYPT_MODE, k.getPrivate());
		return new String(engine.doFinal(DatatypeConverter.parseHexBinary(m)));
	}

	public String getAlgorithmString(String algorithm) throws ProtocolException {
		switch (algorithm) {
		case "SHA1":
			return "SRA/NONE/OAEPPADDING";
		case "SHA256":
			return "SRA/NONE/OAEPWITHSHA256ANDMGF1PADDING";
		case "SHA512":
			return "SRA/NONE/OAEPWITHSHA512ANDMGF1PADDING";
		default:
			throw new ProtocolException("Algorithm not found");
		}
	}

	public AvailableSids getAvaiableSidList() throws JsonParseException, JsonMappingException, IOException {

		this.sids = loadSids();

		AvailableSids as = new AvailableSids();

		for (int x = 0; x < sids.getSids().size(); x++) {
			as.getSids().add(sids.getSids().get(x).getId());
		}

		return as;
	}

	public int getCommonSid(List<AvailableSids> l) throws ProtocolException {
		if (l.get(0).getSids().isEmpty() || l.get(1).getSids().isEmpty()) {
			throw new ProtocolException("No common Sid: " + l.get(1).getSids());
		} else {
			for (int k : (l.get(0).getSids())) {
				if (l.get(1).getSids().contains(k)) {
					return k;
				}
			}
		}

		throw new ProtocolException("No common Sid: " + l.get(1).getSids());
	}

	public static Sids loadSids() {
		Sids sids = new Sids();
		sids.setSids(new ArrayList<Sid>());

		Sid sid11 = new Sid();
		sid11.setId(11);
		sid11.setOaepAlg("SHA256");
		sid11.setSraModulus("2048");

		Sid sid12 = new Sid();
		sid12.setId(12);
		sid12.setOaepAlg("SHA256");
		sid12.setSraModulus("3072");

		Sid sid13 = new Sid();
		sid13.setId(13);
		sid13.setOaepAlg("SHA256");
		sid13.setSraModulus("4096");
		
		Sid sid14 = new Sid();
		sid14.setId(14);
		sid14.setOaepAlg("SHA256");
		sid14.setSraModulus("8192");
		
		Sid sid15 = new Sid();
		sid15.setId(15);
		sid15.setOaepAlg("SHA256");
		sid15.setSraModulus("16384");
		
		Sid sid20 = new Sid();
		sid20.setId(20);
		sid20.setOaepAlg("SHA512");
		sid20.setSraModulus("2048");

		Sid sid21 = new Sid();
		sid21.setId(21);
		sid21.setOaepAlg("SHA512");
		sid21.setSraModulus("3072");

		Sid sid22 = new Sid();
		sid22.setId(22);
		sid22.setOaepAlg("SHA512");
		sid22.setSraModulus("4096");

		Sid sid23 = new Sid();
		sid23.setId(23);
		sid23.setOaepAlg("SHA512");
		sid23.setSraModulus("8192");

		Sid sid24 = new Sid();
		sid24.setId(24);
		sid24.setOaepAlg("SHA512");
		sid24.setSraModulus("16384");
		
		sids.getSids().add(sid11); // 2048 SHA256
		sids.getSids().add(sid12); // 3072 SHA256
		sids.getSids().add(sid13); // 4096 SHA256
		sids.getSids().add(sid14); // 8192 SHA256
//		sids.getSids().add(sid15); // 16384 SHA256
		sids.getSids().add(sid20); // 2048 SHA512
		sids.getSids().add(sid21); // 3072 SHA512
		sids.getSids().add(sid22); // 4096 SHA512
		sids.getSids().add(sid23); // 8192 SHA512
//		sids.getSids().add(sid24); // 16384 SHA512

		return sids;
	}

	public Sid getSid(int chosenSid, Sids sids) throws ProtocolException {
		for (Sid i : sids.getSids()) {
			if (i.getId() == chosenSid) {
				return i;
			}
		}
		throw new ProtocolException("Chosen sid not found");
	}

	public boolean verifyEncryption(MessageExtendV1 theInput)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, ProtocolException,
			InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String alg = getSid(theInput.getKeyNegotiation().getSid(), this.sids).getOaepAlg();
		return theInput.getPayload().getInitialCoin().get(0)
				.equals(decryptOAEP(
						decrypt(encrypt(encryptOAEP(theInput.getPayload().getInitialCoin().get(0), alg, this.keypair),
								this.foreignKeypair), this.keypair),
						alg, this.foreignKeypair))
				&& theInput.getPayload().getInitialCoin()
						.get(1).equals(
								decryptOAEP(
										decrypt(encrypt(encryptOAEP(theInput.getPayload().getInitialCoin().get(1), alg,
												this.keypair), this.foreignKeypair), this.keypair),
										alg, this.foreignKeypair))
				&& theInput.getPayload().getInitialCoin().contains(decryptOAEP(
						decrypt(theInput.getPayload().getEnChosenCoin(), this.keypair), alg, this.foreignKeypair));

	}

	public KeyPair generateForeignKeypair(BigInteger p, BigInteger q, BigInteger e, BigInteger d)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		KeyFactory factory = KeyFactory.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);

		BigInteger n = p.multiply(q);
		PrivateKey privateKey = factory.generatePrivate(new SRADecryptionKeySpec(p, q, d, e));
		PublicKey publicKey = factory.generatePublic(new SRAEncryptionKeySpec(n, e));

		return new KeyPair(publicKey, privateKey);
	}
	
	public String createString4Signature(String desiredCoin, String enChosenCoin, BigInteger p, BigInteger q)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		
		return desiredCoin + enChosenCoin + Hex.toHexString(p.toByteArray()) + Hex.toHexString(q.toByteArray());

	}

	public boolean isWinner() {
		return isWinner;
	}	
}