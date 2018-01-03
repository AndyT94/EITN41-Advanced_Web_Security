package evoting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;

public class Paillier {

    @SuppressWarnings("resource")
    public BigInteger countVotes(int p, int q, BigInteger g, File encVotes) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(encVotes));
            int nbrVotes = 0;

            // Calculate n
            BigInteger n = BigInteger.valueOf(p * q);
            BigInteger nSquare = n.multiply(n);
            BigInteger prodOfCipher = new BigInteger("1");
            String currCiphertext;

            // Read from file
            while ((currCiphertext = reader.readLine()) != null) {
                BigInteger theCipher = new BigInteger(currCiphertext);
                // Multiply ciphertexts
                prodOfCipher = prodOfCipher.multiply(theCipher);
                nbrVotes++;
            }
            prodOfCipher = prodOfCipher.mod(nSquare);
            // Calculate lambda
            BigInteger lambda = BigInteger.valueOf(lcm(p - 1, q - 1));
            // Calculate g^lambda mod n^2 - 1
            BigInteger gToLambda = g.modPow(lambda, nSquare).subtract(
                new BigInteger("1"));
            // Calculate mu = L(gToLambda)^-1 mod n
            BigInteger mu = gToLambda.divide(n).modInverse(n);
            // Calculate L(c^lambda mod n^2) * mu mod n
            BigInteger sumVote = prodOfCipher.modPow(lambda, nSquare).subtract(
                new BigInteger("1")).divide(n).multiply(mu).mod(n);

            // Shift n if necessary
            if (sumVote.compareTo(BigInteger.valueOf(nbrVotes)) > 0) {
                sumVote = sumVote.subtract(n);
            }
            return sumVote;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calculates the least common multiple
     *
     * @param a
     *            double
     * @param b
     *            double
     * @return lcm as long
     */
    private long lcm(double a, double b) {
        return Math.round(a * (b / gcd(a, b)));
    }

    /**
     * Calculates the greatest common divisor
     *
     * @param a
     *            double
     * @param b
     *            double
     * @return gcd as double
     */
    private double gcd(double a, double b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    public static void main(String[] args) {
        Paillier paillier = new Paillier();
        int p = 1117;
        int q = 1471;
        BigInteger g = new BigInteger("652534095028");
        File encVotes = new File("testfiles/TestPaillier.txt");
        System.out.println(paillier.countVotes(p, q, g, encVotes).toString());
    }
}
