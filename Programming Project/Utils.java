import java.math.BigInteger;

public class Utils {

    public static String concat(String a, String b) {
        return a + b;
    }

    public static String reverser(String encodedString) {

        // reverse the encoded string binary values - Michael
        // turns out we need this to do a conversion on the left_encode.
        StringBuilder reversedString = new StringBuilder();
        for (int i = 0; i < encodedString.length(); i += 8) {
            String substring = encodedString.substring(i, i + 8);
            StringBuilder reversedSubstring = new StringBuilder(substring).reverse();
            // int decimalValue = Integer.parseInt(reversedSubstring.toString(), 2);
            // String hexString = String.format("%02X", decimalValue);

            reversedString.append(reversedSubstring);

            // reversedString.append(hexString);
        }
        return reversedString.toString();
    }

    public static String right_encode(BigInteger x) {
        /*
         * Validity Conditions: 0 ≤ x < 2**2040
         * 1. Let n be the smallest positive integer for which 2**(8n) > x.
         * 2. Let x_1, x_2,..., x_n be the base-256 encoding of x satisfying:
         * x = ∑ 28(n-i)x_i, for i = 1 to n.
         * 3. Let O_i = enc8(x_i), for i = 1 to n.
         * 4. Let O_(n+1) = enc8(n).
         * 5. Return O = O_1 || O_2 || ... || O_n || O_(n+1).
         */

        byte[] bytes = x.toByteArray();
        byte[] out = new byte[bytes.length + 1];

        System.arraycopy(bytes, 0, out, 0, bytes.length);
        out[bytes.length] = (byte) bytes.length;

        StringBuilder str = new StringBuilder();

        for (int i = 0; i < out.length; i++) {
            byte b2 = (byte) out[i];
            String s2 = String.format("%8s", Integer.toBinaryString(b2 & 0xFF)).replace(' ', '0');
            str.append(new StringBuilder(s2).reverse().toString());
        }

        return str.toString();

    }

    public static String left_encode(BigInteger x) {
        /*
         * Validity Conditions: 0 ≤ x < 2**2040
         * 1. Let n be the smallest positive integer for which 2**(8n) > x.
         * 2. Let x1, x2, ..., x_n be the base-256 encoding of x satisfying:
         * x = ∑ 28(n-i)x i, for i = 1 to n.
         * 3. Let O_i = enc8(x_i), for i = 1 to n.
         * 4. Let O_0 = enc8(n).
         * 5. Return O = O_0 || O_1 || ... || O_(n−1) || O_n.
         */
        byte[] bytes = x.toByteArray();
        byte[] out = new byte[bytes.length + 1];

        System.arraycopy(bytes, 0, out, 1, bytes.length);
        StringBuilder str = new StringBuilder();

        // str.append((byte) bytes.length);

        out[0] = (byte) bytes.length;

        for (int i = 0; i < out.length; i++) {
            byte b2 = (byte) out[i];
            String s2 = String.format("%8s", Integer.toBinaryString(b2 & 0xFF)).replace(' ', '0');
            str.append(new StringBuilder(s2).reverse().toString());
        }

        return (str).toString();
    }

    // public static String encode_string(String S) {
    // /*
    // Validity Conditions: 0 ≤ len(S) < 2**2040
    // 1. Return left_encode(len(S)) || S.
    // */
    // String a = BigInteger.valueOf(S.length()).toString(2);
    // BigInteger x = new BigInteger(concat(a, S), 2);
    // return left_encode(x);
    // }

    public static String encode_string(String S) {
        /*
         * Validity Conditions: 0 ≤ len(S) < 2**2040
         * 1. Return left_encode(len(S)) || S.
         */
        String a = BigInteger.valueOf(S.length()).toString(2);
        // System.out.println("Length of S in binary is: " + a);

        // from NIST- "Note that if the bit string S is not byte-oriented (i.e., len(S)
        // is not a multiple of 8), the bit string returned from encode_string(S)
        // is also not byte-oriented. However, if len(S) is a multiple of 8,
        // then the length of the output of encode_string(S) will also be a multiple of
        // 8."

        // makes a new String called "a" and sets it equal to a BigInteger
        // such that the BigInteger is the value of the long that is input.
        // in this case, our input is the length of the input String S,
        // that we take from KMACXOF256 and cSHAKE256, but we take the Binary
        // representation of the length. This satisfies the "len(S)" portion
        // of the pseudo code.

        // getting an error, cannot convert string to binary when
        // concat(a,S) since a was binary and S was just a string
        // so I converted it to a bite array, got the bytes, then
        // turned it into bits, then back into a String of bits and
        // then concat. This might break everything and might be
        // completely wrong so feel free to delete if you have a
        // better fix to get KMACXOF256 working- Michael

        byte[] b = S.getBytes();
        // Now we take our input String S and we place it into a byte array
        // that we call "b"

        BigInteger c = new BigInteger(1, b);
        // Now, we make a new BigInteger and call it "c", fill it with the binary
        // representation of "b".

        String bitsOfStringS = c.toString(2);

        // System.out.println("bitsOfStringS is now: " + bitsOfStringS);
        // to verify contents when dealing with 0 value, fixed now. -Michael

        // BigInteger x = new BigInteger(concat(a, bitsOfStringS), 2);
        // return left_encode(x);

        BigInteger a1 = new BigInteger(a);
        // a = reverser(left_encode(a1));
        a = left_encode(a1);

        BigInteger x;
        if (bitsOfStringS == "0") {
            x = new BigInteger(a);
        } else {
            x = new BigInteger(concat(a, bitsOfStringS));
        }
        return x.toString();

        // Trying to fix the syntax for:
        // Return left_encode(len(S)) || S , instead of
        // left_encode(len(S) || S) -Michael

    }

    public static String bytepad(String X, BigInteger w) {
        /*
         * Validity Conditions: w > 0
         * 1. z = left_encode(w) || X.
         * 2. while len(z) mod 8 ≠ 0:
         * z = z || 0
         * 3. while (len(z)/8) mod w ≠ 0:
         * z = z || 00000000
         * 4. return z.
         */

        String z = Utils.concat(Utils.left_encode(w), X);

        while (z.length() % 8 != 0) {
            z = Utils.concat(z, "0");
        }
        while (BigInteger.valueOf(z.length() / 8).mod(w) != BigInteger.ZERO) {
            z = Utils.concat(z, "00000000");
        }

        return z;
    }

    public static String substring(String X, BigInteger a, BigInteger b) {
        /*
         * 1. If a ≥ b or a ≥ len(X):
         * return the empty string.
         * 2. Else if b ≤ len(X):
         * return the bits of X from position a to position b−1, inclusive.
         * 3. Else:
         * return theBitSet bits of X from position a to position len(X)−1, inclusive.
         */

        BigInteger len = BigInteger.valueOf(X.length());

        if (a.compareTo(len) != -1 || b.compareTo(len) != -1) {
            return "";
        } else if (b.compareTo(len) != 1) {
            return X.substring(a.intValue(), b.intValue());
        }

        return X.substring(a.intValue());

    }

}
