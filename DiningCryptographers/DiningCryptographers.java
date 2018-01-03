package DiningCryptographers;

import java.math.BigInteger;

public class DiningCryptographers {

    /**
     * Returns the program output
     *
     * @param SA
     *            secret with Alice
     * @param SB
     *            secret with Bob
     * @param DA
     *            broadcast data by Alice
     * @param DB
     *            broadcast data by Bob
     * @param M
     *            message you may wish to send anonymously
     * @param b
     *            0 if you do not wish to send message or 1 if you wish to send
     *            the message
     * @return The message to output as a string
     */
    public String messageToAnnounce(String SA, String SB, String DA,
	    String DB, String M, int b) {
        if (SA.length() != 4 || SB.length() != 4 || DA.length() != 4
            || DB.length() != 4) {
            throw new IllegalArgumentException("Illegal arguments");
        }
        String sa = stringToBinary(SA);
        String sb = stringToBinary(SB);
        StringBuilder builder = new StringBuilder();
        if (b == 0) {
            int bit = 0;

            //SA XOR SB bitwise
            for (int i = 0; i < sa.length(); i++) {
                bit = xor(Character.getNumericValue(sa.charAt(i)),
                    Character.getNumericValue(sb.charAt(i)));
                builder.append(bit);
            }
            String[] fourBits = splitInto4Parts(builder.toString());
            //Convert to hex
            String message = binaryToHex(fourBits);

            String da = stringToBinary(DA);
            String db = stringToBinary(DB);
            builder = new StringBuilder();
            //SA XOR SB XOR DA XOR DB bitwise
            for (int i = 0; i < da.length(); i++) {
                bit = xor(Character.getNumericValue(sa.charAt(i)),
                    Character.getNumericValue(sb.charAt(i)));
                bit = xor(Character.getNumericValue(da.charAt(i)), bit);
                bit = xor(Character.getNumericValue(db.charAt(i)), bit);
                builder.append(bit);
            }
            fourBits = splitInto4Parts(builder.toString());
            return message + binaryToHex(fourBits);
        } else if (b == 1) {
            String m = stringToBinary(M);

            //SA XOR SB XOR M bitwise
            for (int i = 0; i < sa.length(); i++) {
                int bit = xor(Character.getNumericValue(sa.charAt(i)),
                    Character.getNumericValue(sb.charAt(i)));
                bit = xor(Character.getNumericValue(m.charAt(i)), bit);
                builder.append(bit);
            }
            String[] fourBits = splitInto4Parts(builder.toString());
            //Return the hex value
            return binaryToHex(fourBits);
        }
        return "";
    }

    /**
     * One bit XOR
     *
     * @param sa
     *            first bit
     * @param sb
     *            second bit
     * @return sa XOR sb as an int
     */
    private int xor(int sa, int sb) {
        return (sa + sb) % 2;
    }

    /**
     * Converts a binary string to hex
     *
     * @param fourBits
     *            string array containing the binary numbers to convert
     * @return the hex form of a binary number
     */
    private String binaryToHex(String[] fourBits) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fourBits.length; i++) {
            int bitValueInInt = Integer.parseInt(fourBits[i], 2);
            String hex = Integer.toString(bitValueInInt, 16);
            hex = hex.toUpperCase();
            builder.append(hex);
        }
        return builder.toString();
    }

    /**
     * Splits a string into four parts
     *
     * @param s
     *            the string to split into four parts
     * @return a string array
     */
    private String[] splitInto4Parts(String s) {
        String[] res = new String[4];
        for (int i = 0; i < 4; i++) {
            res[i] = s.substring(i * 4, (i * 4 + 4));
        }
        return res;
    }

    /**
     * Returns a string in binary form
     *
     * @param s
     *            the string to convert to binary
     * @return string in binary form
     */
    private String stringToBinary(String s) {
        String res = new BigInteger(s, 16).toString(2);
        //Pad with zeros to have length 16
        for (int i = 16 - res.length(); i > 0; i--) {
            res = "0" + res;
        }
        return res;
    }

    public static void main(String[] args) {
        DiningCryptographers dc = new DiningCryptographers();
        String SA = "27C2";
        String SB = "0879";
        String DA = "35F6";
        String DB = "1A4D";
        String M = "27BC";
        int b = 1;
        System.out.println(dc.messageToAnnounce(SA, SB, DA, DB, M, b));
    }
}
