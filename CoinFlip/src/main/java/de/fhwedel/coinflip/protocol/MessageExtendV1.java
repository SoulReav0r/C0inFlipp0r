package de.fhwedel.coinflip.protocol;

public class MessageExtendV1 extends MessageInit{

	private KeyNegotiation keyNegotiation;
	private Payload payload;
	
	public Payload getPayload() {
		return payload;
	}

	public void setPayload(Payload payload) {
		this.payload = payload;
	}

	public KeyNegotiation getKeyNegotiation() {
		return keyNegotiation;
	}

	public void setKeyNegotiation(KeyNegotiation keyNegotiation) {
		this.keyNegotiation = keyNegotiation;
	}
	
	public MessageExtendV1 () {
		super();
		this.keyNegotiation = new KeyNegotiation();
//		this.payload = new Payload();
	}
	
	public MessageExtendV1 (MessageExtendV1 messageExtendV1) {
		super(messageExtendV1);
		this.keyNegotiation = new KeyNegotiation(messageExtendV1.keyNegotiation);
		if (messageExtendV1.payload != null)
			this.payload = new Payload(messageExtendV1.payload);
	}
	
	@Override
	public String toString() {
		return super.toString() + ", MessageExtendV1 [keyNegotiation=" + keyNegotiation + ", payload=" + payload + "]";
	}
}
