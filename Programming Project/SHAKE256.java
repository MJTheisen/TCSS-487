import java.math.BigInteger;

public class SHAKE256 {
    public static String call(String K, BigInteger L) {
        //SHAKE256(M, d) = KECCAK[512] (M || 1111, d).
        String in = Utils.concat(K, "1111");
        return KECCAK512.call(in, L);
    }
}
