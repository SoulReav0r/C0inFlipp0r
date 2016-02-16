package de.fhwedel.coinflip.protocol;

import java.util.List;

public class Sids {
	private List<Sid> sids;

	public List<Sid> getSids() {
		return sids;
	}

	public void setSids(List<Sid> sids) {
		this.sids = sids;
	}

	@Override
	public String toString() {
		return "Sids [sids=" + sids + "]";
	}	
}
