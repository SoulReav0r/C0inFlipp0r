import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import de.fhwedel.coinflip.game.Gui;
import de.fhwedel.coinflip.game.GameServer;

public class Start {

	public static void main(String[] args) throws KeyManagementException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
		
		
		if (args.length == 0) {//Client
			Gui.main(null);
		}
		else if (args[0].compareTo("--server") == 0)  {//Server
			GameServer.main(null);
		}
		else if (args[0].compareTo("--help") == 0){
			System.out.println("C0inFlippor can only be started as client without parameters or as server with --server");
		}
		else {
			System.out.println("Unknown parameter. For help type --help");
		}

	}

}