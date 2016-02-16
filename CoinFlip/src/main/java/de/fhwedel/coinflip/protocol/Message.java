package de.fhwedel.coinflip.protocol;

public interface Message {
	public String getStatusMessage();

	public int getStatusId();
	
	public int getProtocolId();

}
