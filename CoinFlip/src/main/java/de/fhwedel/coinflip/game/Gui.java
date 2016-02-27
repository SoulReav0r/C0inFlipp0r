package de.fhwedel.coinflip.game;

import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import gr.planetz.PingingService;
import gr.planetz.impl.HttpPingingService;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JList;

public class Gui extends JFrame {

	private JPanel contentPane;
	private JFormattedTextField txtAddress;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui frame = new Gui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	//Globals
	Thread threadServer;
	JTextArea txtrInfo;
	Properties prop;
	InputStream input;
	Map<String, String> clientsFromBroker;
	
	JLabel lblImageLose;
	JLabel lblImageWin;
	BufferedImage win;
	BufferedImage lose;
	
	private JLabel lblServer;
	private JButton btnExit;
	private JList Lobbylist;
	private JButton btnStopServer;
	private boolean enableGraphics;

	/**
	 * Create the frame.
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws CertificateException 
	 * @throws KeyManagementException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Gui() throws ParseException, KeyManagementException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
		
		this.prop = new Properties();
		try {
			this.input = new FileInputStream("coinflip_config.conf");
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			this.prop.load(input);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		clientsFromBroker = new HashMap<String, String>();
		enableGraphics= Boolean.parseBoolean(this.prop.getProperty("client_enableGraphics"));
		//Broker Test
		//clientsFromBroker.put("test", "192.168.2.30");
		//clientsFromBroker.put("test2", "127.0.0.1");
		
		if (Boolean.parseBoolean(this.prop.getProperty("client_useBroker"))) {
			if (Boolean.parseBoolean(this.prop.getProperty("client_enableMD5"))){
				java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, DH keySize < 768");
				java.security.Security.setProperty("jdk.certpath.disabledAlgorithms", "MD2, RSA keySize < 1024");	
			}
			final PingingService service = new HttpPingingService(this.prop.getProperty("client_brokerURL"), "", "",
			this.prop.getProperty("client_brokerCert"), this.prop.getProperty("client_brokerCertPW"));
			this.clientsFromBroker = service.getPlayersDirectlyOverHttpGetRequest();
		}
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(200, 500, 850, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//MaskFormatter IPformatter = new MaskFormatter("###.#.#.#:####");
		txtAddress = new JFormattedTextField();
		txtAddress.setBounds(72, 12, 125, 19);
		contentPane.add(txtAddress);
		txtAddress.setColumns(10);
		txtAddress.setText(prop.getProperty("client_defaultIP_Port"));
		
		JButton btnConnect = new JButton("connect");
		btnConnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				button_connect_click();
					
			}

					});
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnConnect.setBounds(262, 9, 117, 25);
		contentPane.add(btnConnect);
		
		txtrInfo = new JTextArea();
		txtrInfo.setEditable(false);
		
		txtrInfo.setBounds(37, 78, 301, 269);
		contentPane.add(txtrInfo);
		
		JLabel lblStatus = new JLabel("Status");
		lblStatus.setBounds(154, 51, 70, 15);
		contentPane.add(lblStatus);
		
		JLabel lblLobby = new JLabel("Lobby");
		lblLobby.setBounds(437, 51, 70, 15);
		contentPane.add(lblLobby);
		
		lblServer = new JLabel("Server:");
		lblServer.setBounds(12, 14, 70, 15);
		contentPane.add(lblServer);
		
		btnExit = new JButton("Exit");
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			System.exit(0);
			}
		});
		btnExit.setBounds(421, 9, 117, 25);
		contentPane.add(btnExit);
		Lobbylist = new JList(this.clientsFromBroker.keySet().toArray());
		Lobbylist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				copyIP();
			}
		});
		Lobbylist.setBounds(408, 77, 114, 234);
		contentPane.add(Lobbylist);
		
		if (enableGraphics){
			
		win = ImageIO.read(new File("win.jpg"));
		lose = ImageIO.read(new File("lose.jpg"));
		lblImageLose = new JLabel((new ImageIcon(lose.getScaledInstance(250, 250, Image.SCALE_DEFAULT))));
		lblImageWin = new JLabel((new ImageIcon(win.getScaledInstance(250, 250, Image.SCALE_DEFAULT))));
			
			
		lblImageLose.setBounds(562, 78, 250, 250);
		contentPane.add(lblImageLose);
		lblImageLose.setVisible(false);
		
		lblImageWin.setBounds(562, 78, 250, 250);
		contentPane.add(lblImageWin);
		lblImageWin.setVisible(false);
		
		
		}
		}

	
	
	public void copyIP () {
		txtAddress.setText(clientsFromBroker.get(Lobbylist.getSelectedValue()));
	}
	
	public void button_connect_click() {

		txtrInfo.setText("");
		if (enableGraphics){
		lblImageLose.setVisible(false);
		lblImageWin.setVisible(false);
		}
		Security.addProvider(new BouncyCastleProvider());

		String hostName = txtAddress.getText().split(":")[0];

		int portNumber = Integer.parseInt(txtAddress.getText().split(":")[1]);

		boolean debug = false;

		PrintStream printStream = new PrintStream(new JTextHelper(txtrInfo));

		System.setOut(printStream);
		System.setErr(printStream);

		Thread threadClient = new Thread(new GameClient(hostName, portNumber, this.prop.getProperty("client_tlsRootCert"),
				this.prop.getProperty("client_tlsClientCert"), this.prop.getProperty("client_tlsClientCertPW"),
				debug));
		threadClient.start();
		try {
			threadClient.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (txtrInfo.getText().contains("WIN") && enableGraphics){
			lblImageWin.setVisible(true);
		} else if (txtrInfo.getText().contains("LOOSE") && enableGraphics) {
			lblImageLose.setVisible(true);
		}
		
	}
}
