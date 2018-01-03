package oaep;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class OAEPRest extends OAEP {
    public static void main(String[] args) {
        OAEPRest oaep = new OAEPRest();
        String M = "c107782954829b34dc531c14b40e9ea482578f988b719497aa0687";
        String seed_hex = "1e652ec152d0bfcd65190ffc604c0933d0423381";
        String EM = oaep.encode(M, seed_hex);
        System.out.println("EM: \n" + EM);

        System.out.println();

        EM = "0063b462be5e84d382c86eb6725f70e59cd12c0060f9d3778a18b7aa067f90b2"
                + "178406fa1e1bf77f03f86629dd5607d11b9961707736c2d16e7c668b367890bc"
                + "6ef1745396404ba7832b1cdfb0388ef601947fc0aff1fd2dcd279dabde9b10bf"
                + "c51efc06d40d25f96bd0f4c5d88f32c7d33dbc20f8a528b77f0c16a7b4dcdd8f";
        System.out.println("M: \n" + oaep.decode(EM));
    }

    /**
     * Returns the encoding, EM, of the message M with the seed seed_hex. Length
     * of the returned value is 128 bytes in order to prepare for 1024-bit RSA
     * encryption.
     *
     * @param M
     *            The message
     * @param seed_hex
     *            The seed in hex
     * @return The encoded message EM
     */
    public String encode(String M, String seed_hex) {
        int k = 128;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.reset();
        int hLen = md.getDigestLength();
        byte[] bytesM = convertHexToByteArray(M);

        if (bytesM.length > (k - 2 * hLen - 2)) {
            throw new IllegalArgumentException("message too long");
        }
        byte[] lHash = md.digest();
        int zeroes = k - bytesM.length - 2 * hLen - 2;
        byte[] ps = new byte[zeroes];
        Arrays.fill(ps, (byte) 0);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            output.write(lHash);
            output.write(ps);
            output.write((byte) 1);
            output.write(bytesM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] DB = output.toByteArray();

        String dbMask_hex = MGF1(seed_hex, k - hLen - 1);
        byte[] dbMask = convertHexToByteArray(dbMask_hex);
        byte[] maskedDB = new byte[DB.length];
        for (int i = 0; i < maskedDB.length; i++) {
            maskedDB[i] = (byte) (DB[i] ^ dbMask[i]);
        }

        String seedMask_hex = MGF1(DatatypeConverter.printHexBinary(maskedDB), hLen);
        byte[] seedMask = convertHexToByteArray(seedMask_hex);
        byte[] seed = convertHexToByteArray(seed_hex);
        byte[] maskedSeed = new byte[seed.length];
        for (int i = 0; i < maskedSeed.length; i++) {
            maskedSeed[i] = (byte) (seed[i] ^ seedMask[i]);
        }

        output = new ByteArrayOutputStream();
        output.write((byte) 0);
        try {
            output.write(maskedSeed);
            output.write(maskedDB);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return DatatypeConverter.printHexBinary(output.toByteArray()).toLowerCase();
    }

    /**
     * Returns the decoded message, M, of the encoded message EM
     *
     * @param EM_hex
     *            The encoded message in hex
     */
    public String decode(String EM_hex) {
        int k = 128;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.reset();
        int hLen = md.getDigestLength();
        byte[] EM = convertHexToByteArray(EM_hex);

        if (k != EM.length || k < 2 * hLen + 2) {
            throw new IllegalArgumentException("decryption error");
        }

//      String Y_hex = EM_hex.substring(0, 2);
        String maskedSeed_hex = EM_hex.substring(2, 2 + 2 * hLen);
        String maskedDB_hex = EM_hex.substring(2 + 2 * hLen);

        String seedMask_hex = MGF1(maskedDB_hex, hLen);
        byte[] maskedSeed = convertHexToByteArray(maskedSeed_hex);
        byte[] seedMask = convertHexToByteArray(seedMask_hex);
        byte[] seed = new byte[maskedSeed.length];
        for (int i = 0; i < seed.length; i++) {
            seed[i] = (byte) (maskedSeed[i] ^ seedMask[i]);
        }

        String dbMask_hex = MGF1(DatatypeConverter.printHexBinary(seed), k - hLen - 1);
        byte[] maskedDB = convertHexToByteArray(maskedDB_hex);
        byte[] dbMask = convertHexToByteArray(dbMask_hex);
        byte[] DB = new byte[maskedDB.length];
        for (int i = 0; i < DB.length; i++) {
            DB[i] = (byte) (maskedDB[i] ^ dbMask[i]);
        }

        byte[] lHash = md.digest();
        for (int i = 0; i < lHash.length; i++) {
            if (DB[i] != lHash[i]) {
                throw new IllegalArgumentException("decryption error");
            }
        }
        int ps_length = 0;
        while (DB[hLen + ps_length] == (byte) 0) {
            ps_length++;
        }
        if(DB[hLen + ps_length] != (byte) 1) {
            throw new IllegalArgumentException("decryption error");
        }
        if(EM[0] != (byte) 0) {
            throw new IllegalArgumentException("decryption error");
        }

        byte[] M = new byte[DB.length - lHash.length - ps_length - 1];
        System.arraycopy(DB, lHash.length + ps_length + 1, M, 0, M.length);
        return DatatypeConverter.printHexBinary(M).toLowerCase();
    }
}
