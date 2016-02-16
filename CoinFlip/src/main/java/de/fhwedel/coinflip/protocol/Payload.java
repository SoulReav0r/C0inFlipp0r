package de.fhwedel.coinflip.protocol;

import java.math.BigInteger;
import java.util.ArrayList;

public class Payload {
	private ArrayList<String> initialCoin;
	private String desiredCoin;
	private ArrayList<String> encryptedCoin;
	private String enChosenCoin;
	private String deChosenCoin;
	private ArrayList<BigInteger> keyA;
	private ArrayList<BigInteger> keyB;
	private String signatureA;
	
	public Payload() {
		this.initialCoin = new ArrayList<String>();
		this.encryptedCoin = new ArrayList<String>();
//		this.keyA = new ArrayList<BigInteger>();
//		this.keyB = new ArrayList<BigInteger>();
	}
	
	public ArrayList<String> getInitialCoin() {
		return initialCoin;
	}

	public void setInitialCoin(ArrayList<String> initialCoin) {
		this.initialCoin = initialCoin;
	}

	public String getDesiredCoin() {
		return desiredCoin;
	}

	public void setDesiredCoin(String desiredCoin) {
		this.desiredCoin = desiredCoin;
	}

	public ArrayList<String> getEncryptedCoin() {
		return encryptedCoin;
	}

	public void setEncryptedCoin(ArrayList<String> encryptedCoin) {
		this.encryptedCoin = encryptedCoin;
	}

	public String getEnChosenCoin() {
		return enChosenCoin;
	}

	public void setEnChosenCoin(String enChosenCoin) {
		this.enChosenCoin = enChosenCoin;
	}

	public String getDeChosenCoin() {
		return deChosenCoin;
	}

	public void setDeChosenCoin(String deChosenCoin) {
		this.deChosenCoin = deChosenCoin;
	}

	public ArrayList<BigInteger> getKeyA() {
		return keyA;
	}

	public void setKeyA(ArrayList<BigInteger> keyA) {
		this.keyA = keyA;
	}

	public ArrayList<BigInteger> getKeyB() {
		return keyB;
	}

	public void setKeyB(ArrayList<BigInteger> keyB) {
		this.keyB = keyB;
	}

	public String getSignatureA() {
		return signatureA;
	}

	public void setSignatureA(String signatureA) {
		this.signatureA = signatureA;
	}
	
	public Payload(Payload payload) {
		this.initialCoin = new ArrayList<String>();
		for (String s : payload.initialCoin) {
			this.initialCoin.add(s);
		}

		this.desiredCoin = payload.desiredCoin;

		this.encryptedCoin = new ArrayList<String>();
		for (String s : payload.encryptedCoin) {
			this.encryptedCoin.add(s);
		}

		this.enChosenCoin = payload.enChosenCoin;
		this.deChosenCoin = payload.deChosenCoin;

		this.keyA = new ArrayList<BigInteger>();
		for (BigInteger b : payload.keyA) {
			this.keyA.add(b);
		}
		this.keyB = new ArrayList<BigInteger>();
		for (BigInteger b : payload.keyB) {
			this.keyB.add(b);
		}

		this.signatureA = payload.signatureA;
	}


	@Override
	public String toString() {
		return "Payload [initialCoin=" + initialCoin + ", desiredCoin=" + desiredCoin + ", encryptedCoin="
				+ encryptedCoin + ", enChosenCoin=" + enChosenCoin + ", deChosenCoin=" + deChosenCoin + ", keyA=" + keyA
				+ ", keyB=" + keyB + ", signaturA=" + signatureA + "]";
	}
}
