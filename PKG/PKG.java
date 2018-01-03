package PKG;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class PKG {
    // the jacobi function uses this lookup table
    private static final int[] jacobiTable = { 0, 1, 0, -1, 0, -1, 0, 1 };
    private static final BigInteger ZERO = BigInteger.valueOf(0);
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final BigInteger FIVE = BigInteger.valueOf(5);
    private static final BigInteger EIGHT = BigInteger.valueOf(8);

    public static void main(String[] args) {
        PKG pkg = new PKG();
        String id = "walterwhite@crypto.sec";
        String p = "9240633d434a8b71a013b5b00513323f";
        String q = "f870cfcd47e6d5a0598fc1eb7e999d1b";
        String[] enc_bits = {
                "2f2aa07cfb07c64be95586cfc394ebf26c2f383f90ce1d494dde9b2a3728ae9b",
                "63ed324439c0f6b823d4828982a1bbe5c34e66d985f55792028acd2e207daa4f",
                "85bb7964196bf6cce070a5e8f30bc822018a7ad70690b97814374c54fddf8e4b",
                "30fbcc37643cc433d42581f784e3a0648c91c9f46b5671b71f8cc65d2737da5c",
                "5a732f73fb288d2c90f537a831c18250af720071b6a7fac88a5de32b0df67c53",
                "504d6be8542e546dfbd78a7ac470fab7f35ea98f2aff42c890f6146ae4fe11d6",
                "10956aff2a90c54001e85be12cb2fa07c0029c608a51c4c804300b70a47c33bf",
                "461aa66ef153649d69b9e2c699418a7f8f75af3f3172dbc175311d57aeb0fd12" };
        pkg.compute(id, p, q, enc_bits);
    }

    /**
     * Computes and prints the public identity a, private key r and the
     * decryption value
     *
     * @param public_id
     *            ID
     * @param p_hex
     *            Prime number in hex
     * @param q_hex
     *            Prime number in hex
     * @param encrypted_bits
     *            Encrypted bits in hex
     */
    public void compute(String public_id, String p_hex, String q_hex,
            String[] encrypted_bits) {
        BigInteger p = new BigInteger(p_hex, 16);
        BigInteger q = new BigInteger(q_hex, 16);
        BigInteger M = p.multiply(q);
        String a = derivePublicIdentity(public_id, M);
        System.out.println("a: " + a);

        String r = derivePrivateKey(a, p, q, M);
        System.out.println("r: " + r);

        int decode = decode(encrypted_bits, r, M);
        System.out.println("Decode: " + decode);
    }

    /**
     * Decodes the encrypted bits
     *
     * @param encrypted_bits
     *            The encrypted bits
     * @param r_hex
     *            Private key in hex
     * @param M
     *            p * q
     * @return The decimal value of the decryption
     */
    private int decode(String[] encrypted_bits, String r_hex, BigInteger M) {
        BigInteger r = new BigInteger(r_hex, 16);
        String binary = "";
        for (int i = 0; i < encrypted_bits.length; i++) {
            BigInteger s = new BigInteger(encrypted_bits[i], 16);
            s = s.add(r).add(r);
            binary += Math.max(jacobi(s, M), 0);
        }
        return Integer.parseInt(binary, 2);
    }

    /**
     * Derives the private key r
     *
     * @param a_hex
     *            Public identity in hex
     * @param p
     *            Prime number
     * @param q
     *            Prime number
     * @param M
     *            p * q
     * @return The private key in hex
     */
    private String derivePrivateKey(String a_hex, BigInteger p, BigInteger q,
            BigInteger M) {
        BigInteger a = new BigInteger(a_hex, 16);
        BigInteger pq = p.add(q);
        BigInteger exponent = (M.add(FIVE).subtract(pq)).divide(EIGHT);
        BigInteger r = a.modPow(exponent, M);

        return r.toString(16);
    }

    /**
     * Derives the public identity a
     *
     * @param public_id
     *            identity in hex
     * @param M
     *            p * q
     * @return The public identity a in hex
     */
    private String derivePublicIdentity(String public_id, BigInteger M) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.reset();

        byte[] id = public_id.getBytes();
        BigInteger a = new BigInteger(id);
        md.update(id);
        id = md.digest();
        a = new BigInteger(DatatypeConverter.printHexBinary(id), 16);

        while (jacobi(a, M) != 1) {
            md.update(id);
            id = md.digest();
            a = new BigInteger(DatatypeConverter.printHexBinary(id), 16);
        }
        return a.toString(16);
    }

    /**
     * Computes the value of the Jacobi symbol (A|B)
     *
     * @source https://github.com/bcgit/bc-java/blob/master/core/src/main/java/
     *         org/ bouncycastle/pqc/math/linearalgebra/IntegerFunctions.java
     *
     * @author bcgit
     * @param A
     *            integer value
     * @param B
     *            integer value
     * @return value of the jacobi symbol (A|B)
     */
    public static int jacobi(BigInteger A, BigInteger B) {
        BigInteger a, b, v;
        long k = 1;

        k = 1;

        // test trivial cases
        if (B.equals(ZERO)) {
            a = A.abs();
            return a.equals(ONE) ? 1 : 0;
        }

        if (!A.testBit(0) && !B.testBit(0)) {
            return 0;
        }

        a = A;
        b = B;

        if (b.signum() == -1) { // b < 0
            b = b.negate(); // b = -b
            if (a.signum() == -1) {
                k = -1;
            }
        }

        v = ZERO;
        while (!b.testBit(0)) {
            v = v.add(ONE); // v = v + 1
            b = b.divide(TWO); // b = b/2
        }

        if (v.testBit(0)) {
            k = k * jacobiTable[a.intValue() & 7];
        }

        if (a.signum() < 0) { // a < 0
            if (b.testBit(1)) {
                k = -k; // k = -k
            }
            a = a.negate(); // a = -a
        }

        // main loop
        while (a.signum() != 0) {
            v = ZERO;
            while (!a.testBit(0)) { // a is even
                v = v.add(ONE);
                a = a.divide(TWO);
            }
            if (v.testBit(0)) {
                k = k * jacobiTable[b.intValue() & 7];
            }

            if (a.compareTo(b) < 0) { // a < b
                                        // swap and correct intermediate result
                BigInteger x = a;
                a = b;
                b = x;
                if (a.testBit(1) && b.testBit(1)) {
                    k = -k;
                }
            }
            a = a.subtract(b);
        }

        return b.equals(ONE) ? (int) k : 0;
    }
}
