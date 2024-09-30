package client;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;

public class SecureAdditionClient {
	private InetAddress host;
	private int port;
	// This is not a reserved port number 
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "src/client/LIUkeystore.ks";
	static final String TRUSTSTORE = "src/client/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
  
	
	// Constructor @param host Internet address of the host where the server is located
	// @param port Port number on the host where the server is listening
	public SecureAdditionClient( InetAddress host, int port ) {
		this.host = host;
		this.port = port;
	}
	
  // The method used to start a client object
	public void run() {
		try {
			KeyStore ks = KeyStore.getInstance( "JCEKS" );  //create keystore object with JCEKS algorithm 
			ks.load( new FileInputStream( KEYSTORE ), KEYSTOREPASS.toCharArray() );  //load keystore file
			
			KeyStore ts = KeyStore.getInstance( "JCEKS" );  //create truststore object with JCEKS algorithm
			ts.load( new FileInputStream( TRUSTSTORE ), TRUSTSTOREPASS.toCharArray() );  //load truststore file
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );  //create key manager factory object with SunX509 algorithm
			kmf.init( ks, KEYSTOREPASS.toCharArray() );  //initialize key manager factory object
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );  //create trust manager factory object with SunX509 algorithm
			tmf.init( ts ); //initialize trust manager factory object
			
			SSLContext sslContext = SSLContext.getInstance( "TLS" );  //create SSL context object with TLS algorithm, which is used to create secure socket

			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );  //initialize SSL context object with key managers and trust managers

			SSLSocketFactory sslFact = sslContext.getSocketFactory();  //create SSL socket factory object

			SSLSocket client =  (SSLSocket)sslFact.createSocket(host, port); //create SSL socket object with host and port

			client.setEnabledCipherSuites( client.getSupportedCipherSuites() ); 

			System.out.println("\n>>>> SSL/TLS handshake completed");

			
			// BufferedReader socketIn;
			// socketIn = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
			// PrintWriter socketOut = new PrintWriter( client.getOutputStream(), true );
			
			// String numbers = "1.2 3.4 5.6";
			// System.out.println( ">>>> Sending the numbers " + numbers+ " to SecureAdditionServer" );
			// socketOut.println( numbers );
			// System.out.println( socketIn.readLine() );

			// socketOut.println ( "" );

			//============================START================================================================
			
			// Create input and output streams to read from and write to the server
			DataInputStream socketIn = new DataInputStream(client.getInputStream());
			DataOutputStream socketOut = new DataOutputStream(client.getOutputStream());
			
			//display menu
			printMenu();

			//read from keyboard the user's choice
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			int choice = Integer.parseInt(input.readLine());

			String fileName;
			switch (choice) {
				case 1:
					System.out.println("Download File...");
					System.out.println("Enter the file name: ");
					fileName = input.readLine();
					System.out.println("Downloading: " + fileName + "...");

					downloadFile(socketIn, socketOut, fileName, choice);

					break;

				case 2:
					System.out.println("Upload File...");
					System.out.println("Enter the file name: ");
			
					fileName = input.readLine();
					System.out.println("Uploading: " + fileName + "...");

					uploadFile(socketIn, socketOut, fileName, choice);

					break;

				case 3:
					System.out.println("Delete File...");
					System.out.println("Enter the file name: ");
					fileName = input.readLine();

					deleteFile(socketIn, socketOut, fileName, choice);

					break;
			
				default:
					System.out.println("Invalid choice");
					break;
			}

		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	
	// The test method for the class @param args Optional port number and host name
	public static void main( String[] args ) {
		try {
			InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
			if ( args.length > 0 ) {
				port = Integer.parseInt( args[0] );
			}
			if ( args.length > 1 ) {
				host = InetAddress.getByName( args[1] );
			}
			SecureAdditionClient addClient = new SecureAdditionClient( host, port );
			addClient.run();
		}
		catch ( UnknownHostException uhx ) {
			System.out.println( uhx );
			uhx.printStackTrace();
		}
	}

	public void downloadFile(DataInputStream socketIn, DataOutputStream socketOut, String fileName, int choice) {
		try {
			socketOut.writeInt(choice); 	//send choice to server
			socketOut.writeUTF(fileName);  //send filename to server

			int fileSize = socketIn.readInt(); //read file size from server

			if (fileSize == -1) {
				System.out.println("File not found");
				return;
			}

			byte[] fileData = new byte[fileSize]; 	//create byte array to store file data

			socketIn.readFully(fileData,0, fileSize); //read file data from server

			FileOutputStream fileOut = new FileOutputStream("src/client/" + fileName);  //create file output stream

			fileOut.write(fileData, 0, fileSize);  //write file data to file
			fileOut.close();  //close file output stream

			String response = socketIn.readUTF();  //read response from server
			System.out.println(response);

		} catch (Exception e) {
			System.out.println(e);
			// e.printStackTrace();
		}
	}

	public void uploadFile(DataInputStream socketIn, DataOutputStream socketOut, String fileName, int choice) {
		try {
			socketOut.writeInt(choice);  //send choice to server
			socketOut.writeUTF(fileName);  //send filename to server

			File file = new File("src/client/" + fileName);  //create file object

			if (!file.exists()) {
				System.out.println("File not found");
				socketOut.writeInt(-1);
				return;
			}

			socketOut.writeInt((int)file.length());  //send file size to server, to create byte array

			FileInputStream fileIn = new FileInputStream(file);  //create file input stream

			byte[] fileData = new byte[(int)file.length()];  //create byte array to store file data

			fileIn.read(fileData);  //read file data from file

			socketOut.write(fileData);  //send file data to server

			fileIn.close();  //close file input stream

			String response = socketIn.readUTF();  //read response from server
			System.out.println(response);

		} catch (Exception e) {
			System.out.println(e);
			// e.printStackTrace();
		}
	}

	public void deleteFile(DataInputStream socketIn, DataOutputStream socketOut, String fileName, int choice) {
		try {
			socketOut.writeInt(choice);  //send choice to server
			socketOut.writeUTF(fileName);  //send filename to server

			String response = socketIn.readUTF();  //read response from server

			System.out.println(response);

		} catch (Exception e) {
			System.out.println(e);
			// e.printStackTrace();
		}
	}

	//display menu
	public void printMenu() {
		System.out.println("Lab 3: SSL");
		System.out.println("1. Download File");
		System.out.println("2. Upload File");
		System.out.println("3. Delete File");
		System.out.println("Enter your choice: ");
	}

}
