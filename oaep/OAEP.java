package oaep;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

public class OAEP {
    public static void main(String[] args) {
        OAEP oaep = new OAEP();
        String mgfSeed = "9b4bdfb2c796f1c16d0c0772a5848b67457e87891dbc8214";
        int maskLen = 21;
        System.out.println(oaep.MGF1(mgfSeed, maskLen));
    }

    /**
     * Returns the mask corresponding the seed and mask length
     *
     * @param mgfSeed
     *            seed in hex string
     * @param maskLen
     *            mask length in decimal
     * @return The mask in hex string
     */
    public String MGF1(String mgfSeed, int maskLen) {
        if (maskLen > Math.pow(2, 32)) {
            throw new IllegalArgumentException("mask too long");
        }

        byte[] seed = convertHexToByteArray(mgfSeed);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.reset();
        int hLen = md.getDigestLength();
        byte[] mask = new byte[maskLen];

        int limit = (int) Math.ceil(maskLen / hLen);
        for (int counter = 0; counter < limit; counter++) {
            byte[] C = I2OSP(counter, 4);
            md.update(seed);
            md.update(C);
            System.arraycopy(md.digest(), 0, mask, counter * hLen, hLen);
        }

        md.reset();
        if ((limit * hLen) < maskLen) {
            byte[] C = I2OSP(limit, 4);
            md.update(seed);
            md.update(C);
            System.arraycopy(md.digest(), 0, mask, limit * hLen,
                    maskLen - (limit * hLen));
        }
        return DatatypeConverter.printHexBinary(mask).toLowerCase();
    }

    /**
     * Converts a hex string to a byte array
     *
     * @param mgfSeed
     *            hex string to convert
     * @return byte array corresponding to the hex string
     */
    protected byte[] convertHexToByteArray(String mgfSeed) {
        return DatatypeConverter.parseHexBinary(mgfSeed);
    }

    /**
     * Converts an integer to octet byte array of a specified length
     *
     * @param x
     *            integer to convert
     * @param xLen
     *            intended length
     * @return Byte array corresponding to the conversion
     */
    private byte[] I2OSP(int x, int xLen) {
        if (x >= Math.pow(256, xLen)) {
            throw new IllegalArgumentException("integer too large");
        }

        byte[] res = new byte[xLen];
        for (int i = 0; i < xLen; i++) {
            res[i] = (byte) (x >>> (8 * (xLen - 1 - i)));
        }
        return res;
    }
}
