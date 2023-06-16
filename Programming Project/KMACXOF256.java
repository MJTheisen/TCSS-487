import java.math.BigInteger;

public class KMACXOF256 {
    public static String call(String K, String X, BigInteger L, String S) {
        /*
         * Validity Conditions: len(K) < 2**2040 and 0 ≤ L and len(S) < 2**2040
         * 1. newX = bytepad(encode_string(K), 136) || X || right_encode(0).
         * 2. return cSHAKE256(newX, L, “KMAC”, S).
         */

        if (K.length() >= Math.pow(2, 2040) || S.length() >= Math.pow(2, 2040) || L.signum() == -1) {
            throw new IllegalArgumentException();
        }

        String newX = Utils.bytepad(Utils.encode_string(K), BigInteger.valueOf(136));
        newX = Utils.concat(newX, X);
        newX = Utils.concat(newX, Utils.right_encode(BigInteger.valueOf(0)));

        // String h = new BigInteger("KMAC".getBytes()).toString(2);
        String h = ""; // testing for empty string N

        return cSHAKE256.call(newX, L, h, S);
    }
}
