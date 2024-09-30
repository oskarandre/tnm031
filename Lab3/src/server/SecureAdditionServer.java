package server;

import java.io.*;
import javax.net.ssl.*;
import java.security.*;


public class SecureAdditionServer {
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "src/server/LIUkeystore.ks";
	static final String TRUSTSTORE = "src/server/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
	
	/** Constructor
	 * @param port The port where the server
	 *    will listen for requests
	 */
	SecureAdditionServer( int port ) {  
		this.port = port;
	}
	
	/** The method that does the work for the class */
	public void run() {
		try {
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			ks.load( new FileInputStream( KEYSTORE ), KEYSTOREPASS.toCharArray() );
			
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), TRUSTSTOREPASS.toCharArray() );
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, KEYSTOREPASS.toCharArray() );
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			
			SSLContext sslContext = SSLContext.getInstance( "TLS" );

			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket( port );
			
			sss.setEnabledCipherSuites( sss.getSupportedCipherSuites() );
			
			System.out.println("\n>>>> SecureAdditionServer: active ");
			SSLSocket incoming = (SSLSocket)sss.accept();


			DataInputStream socketIn = new DataInputStream( incoming.getInputStream());
			DataOutputStream socketOut = new DataOutputStream( incoming.getOutputStream());


			switch (socketIn.readInt()) {
				case 1:
					System.out.println("File download request");
					fileDownload(socketIn, socketOut);
					break;
				
				case 2:
					System.out.println("File upload request");
					fileUpload(socketIn, socketOut);
					break;

				case 3:
					System.out.println("File delete request");
					fileDelete(socketIn, socketOut);
					
					break;
				default:
					break;
			}

			
			incoming.close();
		}
		catch( Exception x ) {
			System.out.println( x );
			//x.printStackTrace();
		}
	}

	// do this NOT FINISHED
	private void fileDownload(DataInputStream socketIn, DataOutputStream socketOut) {
		try {
			String fileName = socketIn.readUTF(); // read the file name from the client

			FileInputStream fileIn = new FileInputStream("src/server/" + fileName);  // open the file

			byte[] fileData = fileIn.readAllBytes();  // read the file data

			fileIn.close();  // close the file

			socketOut.writeInt(fileData.length);  // send the file size to the client
			socketOut.write(fileData);  // send the file data to the client

			socketOut.writeUTF("File downloaded successfully");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void fileUpload(DataInputStream socketIn, DataOutputStream socketOut) {
		try {
			String fileName = socketIn.readUTF(); // read the file name from the client
			int fileSize = socketIn.readInt();  // read the file size from the client

			if (fileSize == -1) {
				System.out.println("File not found");
				return;
			}

			byte[] fileData = new byte[fileSize];  // create a byte array to store the file data

			socketIn.readFully(fileData, 0, fileSize);  // read the file data from the client
	
			FileOutputStream fileOut = new FileOutputStream("src/server/" + fileName);  // create a file output stream
			
			fileOut.write(fileData, 0, fileSize);  // write the file data to the file
			fileOut.close();  // close the file output stream

			socketOut.writeUTF("File uploaded successfully");

		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	private void fileDelete(DataInputStream socketIn, DataOutputStream socketOut) {
		try {
			String fileName = socketIn.readUTF();  // read the file name from the client
			File file = new File("src/server/" + fileName); // create a file object

			if (!file.exists()) {		// check if the file exists
				socketOut.writeUTF("File not found"); 
				return;
			}

			file.delete();  // delete the file
			socketOut.writeUTF("File deleted successfully");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/** The test method for the class
	 * @param args[0] Optional port number in place of
	 *        the default
	 */
	public static void main( String[] args ) {
		int port = DEFAULT_PORT;
		if (args.length > 0 ) {
			port = Integer.parseInt( args[0] );
		}
		SecureAdditionServer addServe = new SecureAdditionServer( port );
		addServe.run();
	}
}

