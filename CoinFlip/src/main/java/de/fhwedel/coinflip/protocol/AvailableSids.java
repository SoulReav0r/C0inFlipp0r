package de.fhwedel.coinflip.protocol;
import java.util.ArrayList;

public class AvailableSids {
	private ArrayList<Integer> sids;

	public ArrayList<Integer> getSids() {
		return sids;
	}

	public void setSids(ArrayList<Integer> sids) {
		this.sids = sids;
	}
	
	public AvailableSids () {
		this.sids = new ArrayList<Integer>();
	}
	
	public AvailableSids (AvailableSids availableSids) {
		this.sids = new ArrayList<Integer>();
		
		for (Integer i : availableSids.sids) {
			this.sids.add(i);
		}
	}

	@Override
	public String toString() {
		return "AvailableSids [sids=" + sids + "]";
	}
	
	@Override
	public boolean equals (Object obj) {
		if (!(obj instanceof AvailableSids))
            return false;
        if (obj == this)
            return true;
        
        AvailableSids as = (AvailableSids) obj;
        return this.sids.equals(as.sids);
	}
	
}
