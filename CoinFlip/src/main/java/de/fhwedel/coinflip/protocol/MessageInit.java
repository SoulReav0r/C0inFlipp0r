package de.fhwedel.coinflip.protocol;

public class MessageInit implements Message {
	private int protocolId;
	private int statusId;
	private String statusMessage;

	private ProtocolNegotiation protocolNegotiation;

	public int getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(int protocolId) {
		this.protocolId = protocolId;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public ProtocolNegotiation getProtocolNegotiation() {
		return protocolNegotiation;
	}

	public void setProtocolNegotiation(ProtocolNegotiation protocolNegotiation) {
		this.protocolNegotiation = protocolNegotiation;
	}

	@Override
	public String toString() {
		return "MessageInit [protocolId=" + protocolId + ", statusId=" + statusId + ", statusMessage=" + statusMessage
				+ ", protocolNegotiation=" + protocolNegotiation + "]";
	}
	
	public MessageInit() {
		this.protocolNegotiation = new ProtocolNegotiation();
	}
	
	//Copy-Constuktor
	public MessageInit (MessageInit messageInit) {
		this.protocolId = messageInit.protocolId;
		this.statusId = messageInit.statusId;
		this.statusMessage = messageInit.statusMessage;
		this.protocolNegotiation = new ProtocolNegotiation(messageInit.protocolNegotiation);
	}
}
