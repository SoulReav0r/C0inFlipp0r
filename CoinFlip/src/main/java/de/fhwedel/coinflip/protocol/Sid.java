package de.fhwedel.coinflip.protocol;

public class Sid {
	private int id;
	private String sraModulus;
	private String oaepAlg;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSraModulus() {
		return sraModulus;
	}
	public void setSraModulus(String sraModulus) {
		this.sraModulus = sraModulus;
	}
	public String getOaepAlg() {
		return oaepAlg;
	}
	public void setOaepAlg(String oaepAlg) {
		this.oaepAlg = oaepAlg;
	}
	@Override
	public String toString() {
		return "SidConfig [id=" + id + ", sraModulus=" + sraModulus + ", oaepAlg=" + oaepAlg + "]";
	}
}
