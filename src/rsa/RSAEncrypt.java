package rsa;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class RSAEncrypt {

	//Size of random primes
	final int bits = 1024;
	
	// has to be 0 < K < m
	final BigInteger K = new BigInteger("65537");
	
	public void testing(String filePath) {
		long startTime = System.currentTimeMillis();
		
		BigInteger[] keys = generateKeyPair();
		BigInteger privateExponent	= keys[0];
		BigInteger modValue			= keys[1];
		BigInteger publicExponent	= keys[2];
		try {
			encryptFile(filePath, "encryptedFile.txt", publicExponent, modValue);
			
			decryptFile("encryptedFile.txt", "decryptedFile.txt", privateExponent, modValue);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Runtime: " + (System.currentTimeMillis()-startTime) + "ms");
	}
	
	
	/**
	 * 
	 * @return [privateExponent, modvalue, publicExponent]
	 */
	public BigInteger[] generateKeyPair() {
		BigInteger[] generatedKeys = new BigInteger[3];
		BigInteger p = null, q = null, n = null, m = null, a;
		
		boolean findingPrimes = true;
		
		while(findingPrimes) {
			//find two big primes p and q
			p		= generateProbablePrime(bits);
			q		= generateProbablePrime(bits);
			//n = pq
			n		= calculateProduct(p,q);
			//m = (p-1)(q-1)
			m		= specialPhi(p,q);
			//System.out.println("p = " + p.toString() + "\nq = " +q.toString());
			//check if gcd(k,m) = 1
			if (gcd(K, m).equals(BigInteger.ONE))
				findingPrimes = false;
		}
		//a*K congruent 1 mod m (finding inverse with extended euclides)
		a = inverse(K, m);
		generatedKeys[0] = a;
		generatedKeys[1] = n;
		generatedKeys[2] = K;
		return generatedKeys;
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
	
	//gcd method that cares about java's stack size and therefore does not use recursion
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
		
		/* Test printing if inverse is ok.
		 * for (int i = 0; i < ba.length; i++) {
			
			System.out.println(ba[i].toString());
		}*/
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
	
	/**
	 * Encrypting message^k mod n
	 * @param publicKey
	 * @param modVal
	 * @param message
	 * @return
	 */
	private BigInteger encrypt(BigInteger k, BigInteger n, BigInteger message) {
		 return message.modPow(k, n);
	}

	/**
	 * Decrypting message^a mod n with an additive chaining algorithm used in Math.BigInteger 
	 * and explained on p. 244 of "Applied Cryptography, Second Edition" by Bruce Schneier
	 * @param privateKey
	 * @param modVal
	 * @param encrypted
	 * @return
	 */
	private BigInteger decrypt(BigInteger a, BigInteger n, BigInteger encrypted) {
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
	
	private void writeToFile(byte[] fileArr, String fileName) {
		
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(fileArr);
			fos.close();
		}
		catch(IOException e) {
			
			e.printStackTrace();
		}
		
		System.out.println("Success in creating file named " + fileName);
	}
	
	/**
	 * 
	 * @param filename
	 * @param encryptedFileName
	 * @param publicKey
	 * @param modVal
	 * @throws IOException
	 */
	public void encryptFile(String filename, String encryptedFileName, BigInteger exponent, BigInteger n) throws IOException {
		byte[] file = readFile(filename);
		
		BigInteger fileAsInt = new BigInteger(file);
		
		BigInteger encryptedFile = encrypt(exponent, n, fileAsInt);
		
		writeToFile(encryptedFile.toByteArray(), encryptedFileName);
	}
	
	/**
	 * 
	 * @param fileName
	 * @param decryptedFileName
	 * @param privateKey
	 * @param modVal
	 * @throws IOException
	 */
	public void decryptFile(String fileName, String decryptedFileName, BigInteger exponent, BigInteger n) throws IOException {
		byte[] file = readFile(fileName);
		
		BigInteger fileAsInt = new BigInteger(file);
		
		BigInteger decryptedFile = decrypt(exponent, n, fileAsInt);
		
		writeToFile(decryptedFile.toByteArray(), decryptedFileName);
	}
	
	public BigInteger encryptString(String text, BigInteger exponent, BigInteger n) {
		BigInteger stringAsInt = new BigInteger(text.getBytes());
		
		return encrypt(exponent, n, stringAsInt);
	}
	public BigInteger decryptString(String text, BigInteger exponent, BigInteger n) {
		
		return new BigInteger(text.getBytes());
	}
	
	public static void main(String[]args) {
		RSAEncrypt rsaEncrypt = new RSAEncrypt();
		String filePath = "dummytext.txt";
		rsaEncrypt.testing(filePath);
		
	}
}
