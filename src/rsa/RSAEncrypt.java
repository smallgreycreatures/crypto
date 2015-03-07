package rsa;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class RSAEncrypt {

	final int bits = 1024;
	String msg = "4";
	String outputFileName = "file.txt";
	// has to be 0 < k < m
	final BigInteger K = new BigInteger("65537");
	
	public void init() {
		long startTime = System.currentTimeMillis();
		BigInteger p = null, q = null, n = null, m = null, a;
		boolean findingPrimes = true;
		String filePath = "dummytext.txt";

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
		BigInteger b = new BigInteger("3");
		BigInteger c = new BigInteger("55");
		BigInteger d = new BigInteger("27");
		//BigInteger c = inverse(a, b);
		System.out.println("Special phi" + specialPhi(new BigInteger("5"), new BigInteger("11")).toString());
		System.out.println("inverse" + inverse(b, new BigInteger("40")));
		//System.out.println(c.toString());
		a = inverse(K, m);
//		BigInteger message = new BigInteger(msg.getBytes());
//		BigInteger encrypted = encrypt(K, n, message);
//		System.out.println(encrypted.toString());
		try {
			byte[] file = readFile(filePath);
			
			BigInteger message = new BigInteger(file);
			System.out.println(message.toString());
			BigInteger encrypted = encrypt(K, n, message);
			System.out.println(encrypted.toString());
			
			BigInteger decrypted = decrypt(a, n, encrypted);
			System.out.println(decrypted.toString());
			
			byte[] decryptedFile = decrypted.toByteArray();
			writeToFile(decryptedFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Runtime: " + (System.currentTimeMillis()-startTime) + "ms");
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
	//Encrypting message^k mod n
	public BigInteger encrypt(BigInteger k, BigInteger n, BigInteger message) {
		 return message.modPow(k, n);
	}
	
	//Decrypting message^a mod n with an additive chaining algorithm used in Math.BigInteger 
	public BigInteger decrypt(BigInteger a, BigInteger n, BigInteger encrypted) {
		BigInteger decrypted = BigInteger.ONE;
		
		while (a.compareTo(BigInteger.ZERO) == 1) {
			if (a.and(BigInteger.ONE).compareTo(BigInteger.ONE) == 0) {
				decrypted = calculateProduct(decrypted, encrypted).mod(n);
				
			}
			a = a.shiftRight(1);
			encrypted = calculateProduct(encrypted, encrypted).mod(n);
			
		}
		return decrypted;
	}
	
	public void writeToFile(byte[] fileArr) {
		
		try {
			FileOutputStream fos = new FileOutputStream(outputFileName);
			fos.write(fileArr);
			fos.close();
		}
		catch(IOException e) {
			
			System.out.println("fuggin ioexceptions");
		}
		
		System.out.println("Success in creating file named " + outputFileName);
	}
	
	public static void main(String[]args) {
		RSAEncrypt rsaEncrypt = new RSAEncrypt();
		rsaEncrypt.init();
		
	}
}
