package de.fhwedel.coinflip.protocol;
import java.util.ArrayList;

public class AvailableVersions {
	private ArrayList<String> versions;

	public ArrayList<String> getVersions() {
		return versions;
	}
	
	public void setVersions(ArrayList<String> versions) {
		this.versions = versions;
	}
	
	public AvailableVersions() {
		this.versions = new ArrayList<String>();
	}
	
	public AvailableVersions(AvailableVersions availableVersions) {
		this.versions = new ArrayList<String>();
		for (String s : availableVersions.versions) {
			this.versions.add(s);
		}
	}
	
	@Override
	public boolean equals (Object obj) {
		if (!(obj instanceof AvailableVersions))
            return false;
        if (obj == this)
            return true;
        
        AvailableVersions av = (AvailableVersions) obj;
        return this.versions.equals(av.versions);
	}
	
	@Override
	public String toString() {
		return "AvailableVersions [versions=" + versions + "]";
	}
	
	public AvailableVersions (Builder builder) {
		this.versions = builder.versions;
	}
	
	public static class Builder {
		private ArrayList<String> versions;
		
		
		public Builder() {
			this.versions = new ArrayList<String>();
		}
		
		public Builder addElement(String element) {
			this.versions.add(element);
			return this;
		}
		
		public AvailableVersions build() {
			return new AvailableVersions(this);
		}
	}

	public static Builder Builder() {
		return new Builder();
	}
	
}
