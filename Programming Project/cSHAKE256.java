import java.math.BigInteger;

public class cSHAKE256 {

    public static String call(String X, BigInteger L, String N, String S) {

        /*
         * Validity Conditions: len(N)< 2**2040 and len(S)< 2**2040
         * 1. If N = "" and S = "":
         * return SHAKE256(X, L);
         * 2. Else:
         * return KECCAK[512](bytepad(encode_string(N) || encode_string(S), 136) || X ||
         * 00, L).
         */

        if (N.length() > Math.pow(2, 2040) || S.length() > Math.pow(2, 2040)) {
            throw new IllegalArgumentException();
        }

        if (N.equals("") && S.equals("")) {
            return SHAKE256.call(X, L);
        } else {
            String in = Utils.concat(Utils.encode_string(N), Utils.encode_string(S));
            in = Utils.bytepad(in, BigInteger.valueOf(136));
            in = Utils.concat(in, X);
            in = Utils.concat(in, "00");
            return KECCAK512.call(in, L);
        }

    }

}