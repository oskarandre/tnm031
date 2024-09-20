import java.io.IOException;
import java.math.*;
import java.util.Random;
import java.util.Scanner;

public class RSA {

    public static void main(String[] arg) throws IOException {
        // create scanner object
        Scanner input = new Scanner(System.in);

        // get message from user
        String stringMessage = input.nextLine();

        // convert string to biginteger
        BigInteger message = new BigInteger(stringMessage.getBytes());

        // create an instance of RSAKeys
        RSAKeys keyPair = new RSAKeys(1000);

        // encrypt the message
        BigInteger encryptedMessage = keyPair.encrypt(message);

        // decrypt the message
        BigInteger decryptedMessage = keyPair.decrypt(encryptedMessage);

        // convert decrypted message back to string
        String decryptedString = new String(decryptedMessage.toByteArray());
        // print
        //System.out.println("Message: " + message + "\n");
        System.out.println("Encrypted message: " + encryptedMessage + "\n");
        System.out.println("Decrypted message: " + decryptedString + "\n");

        // close scanner
        input.close();
    }

    private static class RSAKeys {
        private BigInteger p, q, n, phi, e, d;

        public RSAKeys(int bitLength) {
            generateKeys(bitLength);
        }

        // Calculation part
        private void generateKeys(int bitLength) {
            // generate two large prime numbers p and q
            p = BigInteger.probablePrime(bitLength, new Random()); 
            q = BigInteger.probablePrime(bitLength, new Random());

            // compute n = p * q   (public key)
            n = p.multiply(q);

            // compute phi = (p - 1) * (q - 1)
            phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

            // generate encryption exponent (public key)
            e = generateExponent(phi);

            // generate decryption exponent (private key)
            d = e.modInverse(phi);
        }

        // generate encryption exponent
        private BigInteger generateExponent(BigInteger phi) {
            Random rand = new Random();
            BigInteger e;
            // generate a random number e such that 1 < e < phi and gcd(e, phi) = 1
            do {
                e = new BigInteger(phi.bitLength(), rand); // generate a random number e, where e < phi
            } 
            while (!e.gcd(phi).equals(BigInteger.ONE) || e.compareTo(BigInteger.ONE) <= 0 || e.compareTo(phi) >= 0); // check if e is valid 
            return e;
        }

        // Encryption and Decryption part
        public BigInteger encrypt(BigInteger message) {
            // encrypt the message using the public key
            return message.modPow(e, n);    // c = m^e mod n
        }

        public BigInteger decrypt(BigInteger encryptedMessage) {
            // decrypt the message using the private key 
            return encryptedMessage.modPow(d, n); // m = c^d mod n
        }
    }
}