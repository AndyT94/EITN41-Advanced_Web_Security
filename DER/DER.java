package DER;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import javax.xml.bind.DatatypeConverter;

public class DER {
    private static final BigInteger ZERO = BigInteger.valueOf(0);
    private static final BigInteger ONE = BigInteger.valueOf(1);

    public static void main(String[] args) {
        DER der = new DER();
        BigInteger value = new BigInteger("2530368937");
        String DER = DatatypeConverter.printHexBinary(der.encode(value)).toLowerCase();
        System.out.println("DER: \n" + DER);

        BigInteger p = new BigInteger("2530368937");
        BigInteger q = new BigInteger("2612592767");
        BigInteger e = new BigInteger("65537");
        String base64 = der.base64encode(p, q, e);
        System.out.println("Base64: \n" + base64);
    }

    /**
     * Encodes a decimal to DER
     *
     * @param value
     *            integer
     * @return DER encoding
     */
    public byte[] encode(BigInteger value) {
        byte[] val = value.toByteArray();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            output.write((byte) 2);
            int val_length = val.length;
            output.write((byte) val_length);
            output.write(val);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toByteArray();
    }

    /**
     * Encodes DER to base64
     *
     * @param p
     *            integer
     * @param q
     *            integer
     * @param e
     *            integer
     * @return Base64 encoding
     */
    public String base64encode(BigInteger p, BigInteger q, BigInteger e) {
        byte[] version_der = encode(ZERO);
        BigInteger n = p.multiply(q);
        byte[] n_der = encode(n);

        byte[] e_der = encode(e);

        BigInteger pq = p.subtract(ONE).multiply(q.subtract(ONE));
        BigInteger d = e.modInverse(pq);
        byte[] d_der = encode(d);

        byte[] p_der = encode(p);

        byte[] q_der = encode(q);

        BigInteger dp = d.mod(p.subtract(ONE));
        byte[] dp_der = encode(dp);
        BigInteger dq = d.mod(q.subtract(ONE));
        byte[] dq_der = encode(dq);

        BigInteger coeff = q.modInverse(p);
        byte[] coeff_der = encode(coeff);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(0x30);
        int total_length = version_der.length + n_der.length + e_der.length
                + d_der.length + p_der.length + q_der.length
                + dp_der.length + dq_der.length + coeff_der.length;
        output.write((byte) total_length);
        try {
            output.write(version_der);
            output.write(n_der);
            output.write(e_der);
            output.write(d_der);
            output.write(p_der);
            output.write(q_der);
            output.write(dp_der);
            output.write(dq_der);
            output.write(coeff_der);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return DatatypeConverter.printBase64Binary(output.toByteArray());
    }
}
