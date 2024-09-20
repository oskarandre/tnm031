import java.math.*;
import java.util.Random;
//import java.util.Scanner;

public class App {
    public BigInteger p, q, n, e, c, message;

    public void encrypt(){

        // create a integer value for bitLength 
        int length = 4; 
  
        // create a random object 
        Random random = new Random();
        
        p = BigInteger.probablePrime(length, random);

        q = BigInteger.probablePrime(length, random);

        //print p and q
        System.out.println("p: " + p);
        System.out.println("q: " + q);


    }
 
}

// public int gcd(int a, int b) {
//    if (b==0) return a;
//    return gcd(b,a%b);
// }
