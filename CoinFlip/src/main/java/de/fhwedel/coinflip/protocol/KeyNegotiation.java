package de.fhwedel.coinflip.protocol;
import java.math.BigInteger;
import java.util.ArrayList;

public class KeyNegotiation {
	private BigInteger p,q;
	private Integer sid;
	private ArrayList<AvailableSids> availableSids;
	
	public BigInteger getP() {
		return p;
	}
	public void setP(BigInteger p) {
		this.p = p;
	}
	public BigInteger getQ() {
		return q;
	}
	public void setQ(BigInteger q) {
		this.q = q;
	}
	public Integer getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public ArrayList<AvailableSids> getAvailableSids() {
		return availableSids;
	}
	public void setAvailableSids(ArrayList<AvailableSids> availableSids) {
		this.availableSids = availableSids;
	}
	
	public KeyNegotiation() {
		this.availableSids = new ArrayList<AvailableSids>();
	}
	
	public KeyNegotiation (KeyNegotiation keyNegotiation) {
		// BigInteger , String, Integer immutable
		this.p = keyNegotiation.p;
		this.q = keyNegotiation.q;
		this.sid = keyNegotiation.sid;
		this.availableSids = new ArrayList<AvailableSids>();
		
		for(AvailableSids a : keyNegotiation.availableSids) {
			this.availableSids.add(new AvailableSids(a));
		}
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KeyNegotiation))
            return false;
        if (obj == this)
            return true;
        
        KeyNegotiation kn = (KeyNegotiation) obj;
        
        return this.p.equals(kn.p) && this.q.equals(kn.q) && this.sid == kn.sid && this.availableSids.equals(kn.availableSids);

	}
	
	@Override
	public String toString() {
		return "KeyNegotiation [p=" + p + ", q=" + q + ", sid=" + sid + ", availableSids=" + availableSids + "]";
	}
}
