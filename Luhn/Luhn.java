package Luhn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 1. Double the value of every second digit
 * from the rightmost digit moving left. If the product is greater
 * than 9 then substract 9 from the product.
 *
 * 2. Sum all the digits
 *
 * 3. If the sum modulo 10 is equal to 0 it is valid
 */

public class Luhn {
    public boolean isValid(String cardNbr) {
        //The sum
        int sum = 0;
        //Flag when to double
        boolean doubling = false;
        //Start from rightmost digit moving left
        for(int i = cardNbr.length() - 1; i >= 0; i--) {
            //Get the digit
            int digit = Integer.parseInt(Character.toString(cardNbr.charAt(i)));
            //Double the digit if necessary
            if(doubling) {
                digit *= 2;
                //Subtract 9 if the product is greater than 9
                if(digit > 9) {
                    digit -= 9;
                }
            }
            //Toggle the flag
            doubling = !doubling;
            //Add to sum
            sum += digit;
        }
        //If the sum modulo 10 is equal to 0 it is valid
        return sum % 10 == 0;
    }

    public static void main(String[] args) {
        Luhn luhn = new Luhn();
        BufferedReader reader = null;
        try {
            //Read from file
            reader = new BufferedReader(new FileReader("testfiles/Test100.txt"));
            String currNbr;
            while ((currNbr = reader.readLine()) != null) {
                //Replace X with a digit 0-9 and test for validity
                for(int i = 0; i < 10; i++) {
                    String replaceNbr = currNbr.replace("X", Integer.toString(i));
                    if(luhn.isValid(replaceNbr)) {
                        //Print the digit that makes the card number valid
                        System.out.print(i);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
