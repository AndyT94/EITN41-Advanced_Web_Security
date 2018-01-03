package otr;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

class Client {
    public static void main(String[] args) {
        new Client().run();
    }

    void run() {
        String serverName = "eitn41.eit.lth.se";
        int port = 1337;
        Random rnd = new Random();
        // the p shall be the one given in the manual
        BigInteger p = new BigInteger(
                "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C66"
                        + "28B80DC1CD129024E088A67CC7402"
                        + "0BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F"
                        + "14374FE1356D6D51C245E485B5766"
                        + "25E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F"
                        + "24117C4B1FE649286651ECE45B3DC"
                        + "2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA"
                        + "3AD961C62F356208552BB9ED5290770"
                        + "96966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF", 16);
        BigInteger g = new BigInteger("2");
        try {
            Socket client = new Socket(serverName, port);
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    client.getInputStream()));

            // receive g**x1 and convert to a number
            String g_x1_str = in.readLine();
            System.out.println("g**x1: " + g_x1_str);
            BigInteger g_x1 = new BigInteger(g_x1_str, 16);

            // generate g**x2, x2 shall be a random number
            BigInteger x2 = new BigInteger(p.bitLength(), rnd);
            // calculate g**x2 mod p
            BigInteger g_x2 = g.modPow(x2, p);
            // convert to hex-string and send.
            out.println(g_x2.toString(16));
            // read the ack/nak. This should yield a nak due to x2 being 0
            System.out.println("\nsent g_x2: " + in.readLine());

            // calculate Diffie-Hellman key
            BigInteger dhKey = g_x1.modPow(x2, p);

            // receive g**a2 and convert to a number
            String g_a2_str = in.readLine();
            System.out.println("g**a2: " + g_a2_str);
            BigInteger g_a2 = new BigInteger(g_a2_str, 16);
            // generate g**b2
            BigInteger b2 = new BigInteger(p.bitLength(), rnd);
            BigInteger g_b2 = g.modPow(b2, p);
            // send g**b2
            out.println(g_b2.toString(16));
            // calculate g2
            BigInteger g2 = g_a2.modPow(b2, p);
            // read the ack/nak
            System.out.println("\nsent g_b2: " + in.readLine());

            // receive g**a3 and convert to a number
            String g_a3_str = in.readLine();
            System.out.println("g**a3: " + g_a3_str);
            BigInteger g_a3 = new BigInteger(g_a3_str, 16);
            // generate g**b3
            BigInteger b3 = new BigInteger(p.bitLength(), rnd);
            BigInteger g_b3 = g.modPow(b3, p);
            // send g**b3
            out.println(g_b3.toString(16));
            // calculate g3
            BigInteger g3 = g_a3.modPow(b3, p);
            // read the ack/nak
            System.out.println("\nsent g_b3: " + in.readLine());

            // receive Pa
            String pa_str = in.readLine();
            System.out.println("pa: " + pa_str);
            BigInteger pa = new BigInteger(pa_str, 16);
            BigInteger b = new BigInteger(p.bitLength(), rnd);
            // send pb
            BigInteger pb = g3.modPow(b, p);
            out.println(pb.toString(16));
            // read the ack/nak
            System.out.println("\nsent pb: " + in.readLine());

            // receive Qa
            String qa_str = in.readLine();
            System.out.println("qa: " + qa_str);
            BigInteger qa = new BigInteger(qa_str, 16);
            // calculate shared secret
            String sharedPhrase = "eitn41 <3";

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            output.write(bigIntegerToByteArray(dhKey));
            output.write(sharedPhrase.getBytes());
            BigInteger keyWithPhrase = new BigInteger(output.toByteArray());
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            byte[] sharedSecret = md.digest(bigIntegerToByteArray(keyWithPhrase));
            BigInteger y = new BigInteger(sharedSecret);

            BigInteger qb = g.modPow(b, p).multiply(g2.modPow(y, p)).mod(p);
            out.println(qb.toString(16));
            // read the ack/nak
            System.out.println("\nsent qb: " + in.readLine());

            // receive ra
            String ra_str = in.readLine();
            System.out.println("ra: " + ra_str);
            BigInteger ra = new BigInteger(ra_str, 16);
            BigInteger rb = qa.multiply(qb.modInverse(p)).modPow(b3, p);
            out.println(rb.toString(16));
            // read the ack/nak
            System.out.println("\nsent rb: " + in.readLine());

            // read the ack/nak
            String auth_str = in.readLine();
            System.out.println("auth: " + auth_str);

            // check if Rab = Pa(Pb**-1)
            BigInteger rab = ra.modPow(b3, p);
            BigInteger papb = pa.multiply(pb.modInverse(p)).mod(p);
            if (rab.equals(papb)) {
                System.out.println("x=y SUCCESS!!");
            } else {
                System.out.println("Something is wrong!");
                return;
            }

            // msg XOR Diffie Hellman key
            BigInteger msg = new BigInteger("0123456789abcdef", 16);
            msg = msg.xor(dhKey);

            out.println(msg.toString(16));
            // receive response
            System.out.println("Response: " + in.readLine());

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a byte array of a BigInteger with removed leading 0x00
     *
     * @param bi
     *            The BigInteger to convert
     * @return byte array of the BigInteger with removed leading 0x00
     */
    private byte[] bigIntegerToByteArray(BigInteger bi) {
        byte[] bytes = bi.toByteArray();
        if (bytes[0] == 0) {
            byte[] res = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, res, 0, res.length);
            return res;
        }
        return bytes;
    }
}
