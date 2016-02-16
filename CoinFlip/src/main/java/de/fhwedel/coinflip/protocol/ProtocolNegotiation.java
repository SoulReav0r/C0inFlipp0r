package de.fhwedel.coinflip.protocol;
import java.util.ArrayList;

public class ProtocolNegotiation {	
    private String version;
    private ArrayList<AvailableVersions> availableVersions;
    
	public ArrayList<AvailableVersions> getAvailableVersions() {
		return availableVersions;
	}

	public void setAvailableVersions(ArrayList<AvailableVersions> availableVersions) {
		this.availableVersions = availableVersions;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "ProtocolNegotiation [version=" + version + ", availableVersions=" + availableVersions + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProtocolNegotiation))
            return false;
        if (obj == this)
            return true;
        
        ProtocolNegotiation pn = (ProtocolNegotiation) obj;
        
		return this.version.equals(pn.version) && this.availableVersions.equals(pn.availableVersions);
	}
	
	public ProtocolNegotiation() {
		this.availableVersions = new ArrayList<AvailableVersions>();
	}
	
	public ProtocolNegotiation (ProtocolNegotiation protocolNegotiation) {
		this.version = protocolNegotiation.version;
		
		//this.availableVersions = new ArrayList<AvailableVersions>(protocolNegotiation.availableVersions);
		this.availableVersions = new ArrayList<AvailableVersions>();
		for (AvailableVersions a : protocolNegotiation.getAvailableVersions()) {
			this.availableVersions.add(new AvailableVersions(a));
		}
	}	
}
