import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

// When implementing the KECCAK and other Algorithms from the FIPS 202, it is important to look
// back at the actual documentation to ensure that the written out algorithm documentatin her is
// the same, since some errors in formatting may occur.

// References
// https://keccak.team/keccak_specs_summary.html
// https://chenglongma.com/10/simple-keccak/

public class KECCAK512 {

    // KECCAK-p permutations are specified with two parameters:
    // 1) the fixed length of the strings that are permuted, called the width of the
    // permutation,
    // 2) the number of iterations of an internal transformation, called a round.
    // however, the number of rounds n is 12 + 2 * l.
    // b = width, and n = rounds.

    private static final long[] rc = {
            0x0000000000000001L,
            0x0000000000008082L,
            0x800000000000808AL,
            0x8000000080008000L,
            0x000000000000808BL,
            0x0000000080000001L,
            0x8000000080008081L,
            0x8000000000008009L,
            0x000000000000008AL,
            0x0000000000000088L,
            0x0000000080008009L,
            0x000000008000000AL,
            0x000000008000808BL,
            0x800000000000008BL,
            0x8000000000008089L,
            0x8000000000008003L,
            0x8000000000008002L,
            0x8000000000000080L,
            0x000000000000800AL,
            0x800000008000000AL,
            0x8000000080008081L,
            0x8000000000008080L,
            0x0000000080000001L,
            0x8000000080008008L
    };

    // https://stackoverflow.com/a/13006907
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // https://stackoverflow.com/a/34807360
    public static String longToHex(long l) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(l);
        byte[] result = byteBuffer.array();
        return byteArrayToHex(result);
    }

    private static void printState(long[][] S) {
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                System.out.print(longToHex(S[x][y]) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private static void printStateLanes(long[][] S) {
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                System.out.println("[" + x + "," + y + "]" + " = " + String.format("%016x ", S[x][y]));
            }
        }
        System.out.println();

    }

    private static long[][] stringToStateArray(String s, int numBlocks) {
        long[][] A = new long[5][5];
        int i = 0;

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                String blockString = s.substring(i * 64, (i + 1) * 64);
                A[x][y] = Long.reverse(Long.parseUnsignedLong(blockString, 2));
                i++;
                if (i > numBlocks) {
                    return A;
                }
            }
        }

        return A;
    }

    public static String call(String input, BigInteger L) {

        // rate
        final int r = 576;
        // capacity
        final int c = 1024;
        // rate + capacity
        final int b = 1600;
        // w is the lane size of a KECCAK-p permutation in bits.
        final int w = 64; // int w = (b / 25);
        // l is the binary logarithm of the lane size.
        final int l = 6; // int l = (int) (Math.log((b / 25)) / Math.log(2));
        // n is the number of rounds.
        final int n = (12 + 2 * l);

        final int blockSize = r / 8;

        // 10*1 padding
        int len = input.length();
        int padLen = r - ((len + 9) % r);
        StringBuilder paddedInputBuilder = new StringBuilder(input);
        paddedInputBuilder.append("01100000");
        for (int i = 0; i < padLen; i++) {
            paddedInputBuilder.append("0");
        }
        paddedInputBuilder.append("1");

        String paddedInput = paddedInputBuilder.toString();

        long[][] S = new long[5][5];

        // Initialize S array to 0
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                S[i][j] = 0;
            }
        }

        int num_rate_blocks = paddedInput.length() / r;

        for (int t = 0; t < num_rate_blocks; t++) {
            String m = paddedInput.substring(t * r, (t + 1) * r);
            int numBlocks = m.length() / blockSize;
            long[][] pi = stringToStateArray(m, numBlocks);
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    S[i][j] ^= pi[i][j];
                }
            }
            // System.out.println("Permutation " + t);
            S = keccakf(S, n);
            // printState(S);
        }

        StringBuilder Z = new StringBuilder();

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                Z.append(longToHex(S[x][y]));
            }
        }

        return Z.toString().substring(0, 512 / 8 * 2);

    }

    public static long[][] keccakf(long[][] A, int n) {
        for (int i = 0; i < n; i++) {
            A = keccakRound(A, i);
        }
        return A;
    }

    public static long[][] keccakRound(long[][] A, int round) {
        A = theta(A);
        A = pi_rho(A);
        A = chi(A);
        A = iota(A, round);
        return A;
    }

    // https://stackoverflow.com/a/4403631
    public static long[][] theta(long[][] A) {
        long[] C = new long[5];
        for (int x = 0; x < 5; x++) {
            C[x] = A[x][0] ^ A[x][1] ^ A[x][2] ^ A[x][3] ^ A[x][4];
        }

        long[] D = new long[5];
        for (int x = 0; x < 5; x++) {
            D[x] = (C[(x + 4) % 5]) ^ rotateLeft(C[(x + 1) % 5], 1);
        }

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                A[x][y] ^= D[x];
            }
        }
        return A;
    }

    // https://github.com/XKCP/XKCP/blob/master/Standalone/CompactFIPS202/Python/CompactFIPS202.py
    private static long rotateLeft(long x, int n) {
        return (x << n) | (x >>> (64 - n));
    }

    public static long[][] pi_rho(long[][] A) {

        final int[][] offsets = new int[][] {
                { 0, 1, 62, 28, 27 },
                { 36, 44, 6, 55, 20 },
                { 3, 10, 43, 25, 39 },
                { 41, 45, 15, 21, 8 },
                { 18, 2, 61, 56, 14 }
        };

        long[][] B = new long[5][5];
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                int newX = 2 * x + 3 * y;
                newX %= 5;
                int newY = y;

                long rotatedValue = rotateLeft(A[x][y], offsets[y][x]);

                B[newY][newX] = rotatedValue;
            }
        }

        return B;

    }

    public static long[][] chi(long[][] A) {
        // A[x,y] = B[x,y] xor ((not B[x+1,y]) and B[x+2,y]), for (x,y) in (0…4,0…4)
        long[][] C = new long[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                C[i][j] = A[i][j] ^ ((~A[(i + 1) % 5][j]) & A[(i + 2) % 5][j]);
            }
        }
        return C;
    }

    public static long[][] iota(long[][] A, int round) {
        long RC = rc[round];
        A[0][0] ^= RC;
        return A;
    }

}