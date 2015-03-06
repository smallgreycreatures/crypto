package rsa;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class RSAEncrypt {

	final int bits = 1024;
	
	// has to be 0 < k < m
	final BigInteger K = new BigInteger("65537");
	
	public void init() {
		BigInteger p, q, n, m = null, d;
		boolean findingPrimes = true;
		String filePath = "file.txt";

		//p and q = primes
		//n = pq
		//m = (p-1)(q-1)
		while(findingPrimes) {
			//find two big primes p and q
			p		= generateProbablePrime(bits);
			q		= generateProbablePrime(bits);
			//n = pq
			n		= calculateProduct(p,q);
			//m = (p-1)(q-1)
			m		= specialPhi(p,q);
			System.out.println("p = " + p.toString()
							+ "\nq = " +q.toString());
			//check if gcd(k,m) = 1
			if (gcd(K, m).equals(BigInteger.ONE))
				findingPrimes = false;
		}
		//just testing Works!
		//BigInteger a = new BigInteger("21");
		//BigInteger b = new BigInteger("79");
		//BigInteger c = inverse(a, b);
		
		//System.out.println(c.toString());
		d = inverse(K, m);
		
		/*try {
			byte[] file = readFile(filePath);
			
			BigInteger message = new BigInteger(file);
			
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
	}
	
	private BigInteger generateProbablePrime(int size) {	
		return BigInteger.probablePrime(size, new Random());
	}
	
	//p*q
	private BigInteger calculateProduct(BigInteger p, BigInteger q) {
		return p.multiply(q);
	}
	
	//m = (p-1)(q-1)
	private BigInteger specialPhi(BigInteger p, BigInteger q) {
		BigInteger n = p.subtract(BigInteger.ONE);
		n = n.multiply(q.subtract(BigInteger.ONE));
		return n;
	}
	
	//gcd method that cares about javas stack size and therefor does not use recursion
	private BigInteger gcd(BigInteger k, BigInteger m) {
		BigInteger tmp;
		if (m.compareTo(k) > 0) {
			tmp = k;
			k = m;
			m = tmp;
		}
		
		for(;;) {
			if (m.equals(BigInteger.ZERO)) 
				return k;
			if (m.equals(BigInteger.ONE))
				return m;
			
			tmp = m;
			m = k.mod(m);
			k = tmp;
		}
	}
	
	//Good looking recursive implementation of gcd
	private BigInteger gcdRec(BigInteger k, BigInteger m) {
		if (m.equals(BigInteger.ZERO))
			return k;
		
		return gcdRec(m, k.mod(m));
	}
	
	
	private byte[] readFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		byte[] rawData = Files.readAllBytes(path);
		
		return rawData;
	}
	
	private BigInteger inverse(BigInteger k, BigInteger m) {
		BigInteger[] ba = extendedEuclidean(k, m);
		
		for (int i = 0; i < ba.length; i++) {
			
			System.out.println(ba[i].toString());
		}
		if (ba[1].compareTo(BigInteger.ZERO) == 1) {
			return ba[1];
		} else {
			return ba[1].add(m);
		}
	}
	
	private BigInteger[] extendedEuclidean(BigInteger k, BigInteger m) {
		
		if (m.equals(BigInteger.ZERO)) {
			return new BigInteger[] {k, BigInteger.ONE, BigInteger.ZERO};
		}
		
		BigInteger[] answer = extendedEuclidean(m, k.mod(m));
		
		BigInteger x = answer[0];
		BigInteger y = answer[2];
		BigInteger temp = calculateProduct(k.divide(m), answer[2]);
		BigInteger z = answer[1].subtract(temp);
		
		return new BigInteger[] {x, y, z}; 
	}
	
	public static void main(String[]args) {
		RSAEncrypt rsaEncrypt = new RSAEncrypt();
		rsaEncrypt.init();
		
	}
}
