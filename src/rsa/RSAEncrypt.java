package rsa;

import java.math.BigInteger;
import java.util.Random;

public class RSAEncrypt {

	final int bits = 1024;
	
	final BigInteger k = new BigInteger("65537");
	
	public void init() {
		BigInteger p, q, n, m,d;
		boolean findingPrimes = true;
		//p and q = primes
		//n = pq
		//m = (p-1)(q-1)
		while(findingPrimes) {
			//Börja med att generera probable primes
			p		= generateProbablePrime(bits);
			q		= generateProbablePrime(bits);
			n		= calculateProduct(p,q);
			m		= specialPhi(p,q);
			System.out.println("p = " + p.toString()
								+ "\nq = " +q.toString());
			if (gcd(k, m).equals(BigInteger.ONE))
				findingPrimes = false;
		}
		

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
	
	public static void main(String[]args) {
		RSAEncrypt rsaEncrypt = new RSAEncrypt();
		rsaEncrypt.init();
		

	}
	
	
}
