import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.Random;
import java.util.InputMismatchException;

public class Main {

    public static void decrypt(Scanner scanner) {
        // Decrypting a symmetric cryptogram (z, c, t) under passphrase pw:
        // ▪ (ke || ka) <- KMACXOF256(z || pw, “”, 1024, “S”)
        // ▪ m <- KMACXOF256(ke, “”, |c|, “SKE”)  c
        // ▪ t’ <- KMACXOF256(ka, m, 512, “SKA”)
        // ▪ accept if, and only if, t’ = t
        System.out.print("Enter z: ");
        String z = scanner.nextLine();
        System.out.print("Enter c: ");
        String c = scanner.nextLine();
        System.out.print("Enter t: ");
        String t = scanner.nextLine();
        System.out.print("Enter the password: ");
        String password = scanner.nextLine();
        String pw = bytesToBitString(password.getBytes());
        String s = bytesToBitString("S".getBytes());

        String in = z.concat(pw);
        String keka = KMACXOF256.call(in, "", BigInteger.valueOf(1024), s);
        String ke = keka.substring(0, (keka.length() / 2));
        String ka = keka.substring((keka.length() / 2));
        String ske = bytesToBitString("SKE".getBytes());
        String ska = bytesToBitString("SKA".getBytes());
        String m = KMACXOF256.call(ke, "", BigInteger.valueOf(c.length()), ske);
        String tp = KMACXOF256.call(ka, m, BigInteger.valueOf(512), ska);

        if (!tp.equals(t)) {
            System.out.println("T' != T");
            return;
        }

        System.out.println("Message: " + m);

    }

    public static String bytesToBitString(byte[] a) {
        return new BigInteger(a).toString(2);
    }

    public static String hexToBitString(String hexString) {
        return new BigInteger(hexString, 16).toString(2);
    }

    // This is the code from the separate project file I was running the test in.
    // just in case the above "hexToBitString" method is not working. The below
    // code works, but I condensed it. Let me know if you have a question. - Michael

    // public class Main {
    // public static void main(String[] args) {
    // System.out.println("Hello World!\n");
    // String mainInput =
    // "23cde73d4ab2c9e02f726936e7a34afa8b5f435215440ba219cc6290337e473f319e4e13b3e8b593854e90e20bbb5b8b85c41f5359c4feb15e8f0eddde478e2a";
    // String a = "a";
    // System.out.println("2 Methods for translating the hex code a1b2c3 without
    // first chaning it to A1B2C3.\n");

    // // Method 1
    // System.out.println("Method 1: \n");
    // String hexString = "a1b2c3";
    // StringBuilder bitString = new StringBuilder();

    // for (int i = 0; i < hexString.length(); i += 2) {
    // String hex = hexString.substring(i, i + 2);
    // int decimal = Integer.parseUnsignedInt(hex, 16);
    // String binary = Integer.toBinaryString(decimal);
    // bitString.append(binary);
    // }
    // System.out.println("A1B2C3 should say: \n" );
    // System.out.println("101000011011001011000011 based on
    // https://ascii-tables.com/\n");
    // System.out.println();
    // System.out.println(bitString.toString());
    // System.out.println();

    // // Method 2
    // System.out.println("Method 2: \n");
    // System.out.println("A1B2C3 should say: \n" );
    // System.out.println("101000011011001011000011 based on
    // https://ascii-tables.com/\n");
    // BigInteger s = new BigInteger(hexString, 16);
    // String binaryString = s.toString(2);
    // System.out.println(binaryString);
    // }
    // }

    public static void encrypt(Scanner scanner) {
        // Encrypting a byte array m symmetrically under passphrase pw:
        // ▪ z <- Random(512)
        // ▪ (ke || ka) <- KMACXOF256(z || pw, “”, 1024, “S”)
        // ▪ c <- KMACXOF256(ke, “”, |m|, “SKE”)  m
        // ▪ t <- KMACXOF256(ka, m, 512, “SKA”)
        // ▪ symmetric cryptogram: (z, c, t)
        System.out.print("Enter the given data file to encrypt: ");
        String bytes = scanner.nextLine();
        System.out.print("Enter the password: ");
        String password = scanner.nextLine();
        String m = bytesToBitString(bytes.getBytes());
        String pw = bytesToBitString(password.getBytes());
        String z = Random(512);
        String in = z.concat(pw);
        String ske = bytesToBitString("SKE".getBytes());
        String ska = bytesToBitString("SKA".getBytes());
        String s = bytesToBitString("S".getBytes());
        String keka = KMACXOF256.call(in, "", BigInteger.valueOf(1024), s);
        String ke = keka.substring(0, (keka.length() / 2));
        String ka = keka.substring((keka.length() / 2));
        String c = KMACXOF256.call(ke, "", BigInteger.valueOf(m.length()), ske);
        String t = KMACXOF256.call(ka, m, BigInteger.valueOf(512), ska);

        System.out.println("Symmetric cryptogram: ( z: " + z + ", c: " + c + ", t: " + t + " )");
    }

    public static void mac(Scanner scanner) {
        // Compute an authentication tag t of a byte array m under passphrase pw:
        // t <- KMACXOF256(pw, m, 512, “T”)
        try {
            System.out.print("Enter the filename to generate the MAC of: ");
            String filename = scanner.nextLine();
            System.out.print("Enter the password: ");
            String password = scanner.nextLine();

            // Read the contents of the input file
            byte[] fileData = null;
            try {
                FileInputStream inputStream = new FileInputStream(filename);
                fileData = inputStream.readAllBytes();
                inputStream.close();
            } catch (IOException e) {
                System.out.println("Error: Failed to read input file.");
                return;
            }
            String m = new BigInteger(fileData).toString(2); // asciiToLittleEndian(fileData);
            String pw = asciiToLittleEndian(password); // new BigInteger(password).toString(2);
            String t = asciiToLittleEndian("T"); // new BigInteger("T".getBytes()).toString(2);
            String out = KMACXOF256.call(m, pw, BigInteger.valueOf(512), t);
            System.out.println(out);

        } catch (InputMismatchException e) {
            System.out.println("\nNot a valid input! "); // Take in random bits
        }
    }

    public static void hash(Scanner scanner) {
        // Computing a cryptographic hash h of a byte array m:
        // h <- KMACXOF256(“”, m, 512, “D”)
        System.out.print("Enter the filename to hash: ");
        String filename = scanner.nextLine();

        // Read the contents of the input file
        byte[] fileData = null;
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            fileData = inputStream.readAllBytes();
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to read input file.");
            scanner.close();
            return;
        }

        String m = new BigInteger(fileData).toString(2);
        String d = new BigInteger("Email Signature".getBytes()).toString(2);
        String out = KMACXOF256.call("", m, BigInteger.valueOf(512), d);
        System.out.println(out);
        scanner.close();
    }

    // moved reverser to Utils

    public static String asciiToLittleEndian(String asciiString) {
        StringBuilder sb = new StringBuilder();
        for (char c : asciiString.toCharArray()) {
            String binaryString = Integer.toBinaryString(c);
            while (binaryString.length() < 8) {
                binaryString = "0" + binaryString;
            }
            for (int i = 0; i < 8; i++) {
                sb.append(binaryString.charAt(7 - i));
            }
        }
        return sb.toString();
    }

    // https://www.di-mgt.com.au/sha_testvectors.html
    private static void test_keccak() {
        String t1 = "";
        String o1 = KECCAK512.call(t1, BigInteger.valueOf(512));
        System.out.println("Test 1 Works: " + o1.equals(
                "a69f73cca23a9ac5c8b567dc185a756e97c982164fe25859e0d1dcc1475c80a615b2123af1f5f94c11e3e9402c3ac558f500199d95b6d3e301758586281dcd26"));

        String t2 = asciiToLittleEndian("abc");
        String o2 = KECCAK512.call(t2, BigInteger.valueOf(512));
        System.out.println("Test 2 Works: " + o2.equals(
                "b751850b1a57168a5693cd924b6b096e08f621827444f70d884f5d0240d2712e10e116e9192af3c91a7ec57647e3934057340b4cf408d5a56592f8274eec53f0"));

        String t3 = asciiToLittleEndian("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq");
        String o3 = KECCAK512.call(t3, BigInteger.valueOf(512));
        System.out.println("Test 3 Works: " + o3.equals(
                "04a371e84ecfb5b8b77cb48610fca8182dd457ce6f326a0fd3d7ec2f1e91636dee691fbe0c985302ba1b0d8dc78c086346b533b49c030d99a27daf1139d6e75e"));
    }

    private static String convertLittleEndianToBigEndian(String littleEndian) {
        int length = littleEndian.length();
        StringBuilder bigEndian = new StringBuilder(length);

        for (int i = length - 1; i >= 0; i--) {
            bigEndian.append(littleEndian.charAt(i));
        }

        return bigEndian.toString();
    }

    private static void generate_key_pair(Scanner scanner) {
        System.out.print("Enter the password: ");
        String password = scanner.nextLine();
        String pw = asciiToLittleEndian(password);
        
        String s = KMACXOF256.call("", pw, BigInteger.valueOf(512), asciiToLittleEndian("SK"));

        BigInteger sk = new BigInteger(s, 16).multiply(BigInteger.valueOf(4)).mod(Ed448GoldilocksPoint.P);

        BigInteger gx = BigInteger.valueOf(8);
        BigInteger gy = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(gx, gy);

        Ed448GoldilocksPoint pk = G.multiply(sk);

        System.out.println("Public Key: (" + pk.getX() + ", " + pk.getY() + ")");
        System.out.println("Private Key: " + sk);
        System.out.print("Enter the filename to write the public key to: ");
        String filename = scanner.nextLine();
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(pk.getX() + ", " + pk.getY());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to write to file.");
            scanner.close();
            return;
        }
        scanner.close();
    }

    public static void verify(Scanner scanner) {
        System.out.print("Enter the filename of the public key: ");
        String filename = scanner.nextLine();
        System.out.print("Enter the filename of the signature: ");
        String filename2 = scanner.nextLine();
        System.out.print("Enter the filename of the file to verify: ");
        String filename3 = scanner.nextLine();

        String pk = null;
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            pk = new String(inputStream.readAllBytes());
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to read public key file.");
            scanner.close();
            return;
        }

        String[] splitPK = pk.split(",");
        String x = splitPK[0].strip();
        String y = splitPK[1].strip();

        Ed448GoldilocksPoint V = new Ed448GoldilocksPoint(new BigInteger(x), new BigInteger(y));

        String signature = null;
        try {
            FileInputStream inputStream = new FileInputStream(filename2);
            signature = new String(inputStream.readAllBytes());
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to read signature file.");
            scanner.close();
            return;
        }

        String[] splitSignature = signature.split(",");
        BigInteger h = new BigInteger(splitSignature[0].strip(), 2);
        BigInteger z = new BigInteger(splitSignature[1].strip(), 2);

        byte[] fileData = null;
        try {
            FileInputStream inputStream = new FileInputStream(filename3);
            fileData = inputStream.readAllBytes();
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to read input file.");
            scanner.close();
            return;
        }

        BigInteger gx = BigInteger.valueOf(8);
        BigInteger gy = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");
        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(gx, gy);

        Ed448GoldilocksPoint U = G.multiply(z).add(V.multiply(h));

        String m = new BigInteger(fileData).toString(2);
        String d = asciiToLittleEndian("T");
        String out = KMACXOF256.call(U.getX().toString(2), m, BigInteger.valueOf(512), d);

        if (out.equals(h.toString(2))) {
            System.out.println("Signature is valid.");
        } else {
            System.out.println("Signature is invalid.");
        }

    }

    public static void encrypt_elliptic(Scanner scanner) {
        System.out.print("Enter the filename of the public key: ");
        String filename = scanner.nextLine();
        String pk = null;
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            pk = new String(inputStream.readAllBytes());
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to read public key file.");
            scanner.close();
            return;
        }

        String[] splitPK = pk.split(",");
        String x = splitPK[0];
        String y = splitPK[1].strip();

        Ed448GoldilocksPoint V = new Ed448GoldilocksPoint(new BigInteger(x), new BigInteger(y));

        System.out.print("Enter the filename of the file to encrypt: ");
        String filename2 = scanner.nextLine();
        byte[] fileData = null;
        try {
            FileInputStream inputStream = new FileInputStream(filename2);
            fileData = inputStream.readAllBytes();
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to read input file.");
            scanner.close();
            return;
        }

        BigInteger gx = BigInteger.valueOf(8);
        BigInteger gy = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");
        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(gx, gy);

        String k = Random(512);
        BigInteger kInt = new BigInteger(k, 2).multiply(BigInteger.valueOf(4));

        Ed448GoldilocksPoint W = V.multiply(kInt);
        Ed448GoldilocksPoint Z = G.multiply(kInt);

        String kake = KMACXOF256.call(W.getX().toString(2), W.getY().toString(2), BigInteger.valueOf(512), asciiToLittleEndian("S"));

        // Convert kake from hex string to binary string
        kake = new BigInteger(kake, 16).toString(2);

        String ka = kake.substring(0, 256);
        String ke = kake.substring(256);

        String m = new BigInteger(fileData).toString(2);

        String PKE = asciiToLittleEndian("PKE");
        String PKA = asciiToLittleEndian("PKA");

        String c = KMACXOF256.call(ke, "", BigInteger.valueOf(m.length()), PKE);

        c = new BigInteger(c, 16).xor(new BigInteger(m, 2)).toString(2);

        String t = KMACXOF256.call(ka, m, BigInteger.valueOf(512), PKA);

        String cryptogram = Z.getX().toString(2) + "," + Z.getY().toString(2) + "," + c + "," + t;

        // Write to file
        try {
            FileOutputStream outputStream = new FileOutputStream("ciphertext.txt");
            outputStream.write(cryptogram.getBytes());
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to write to ciphertext file.");
            scanner.close();
            return;
        }
    }

    public static void decrypt_elliptic(Scanner scanner) {
        System.out.print("Enter the filename of the file to decrypt: ");
        String filename = scanner.nextLine();
        String cryptogram = null;
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            cryptogram = new String(inputStream.readAllBytes());
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to read cryptogram file.");
            scanner.close();
            return;
        }

        String[] splitCryptogram = cryptogram.split(",");
        String zx = splitCryptogram[0].strip();
        String zy = splitCryptogram[1].strip();
        String c = splitCryptogram[2].strip();
        String t = splitCryptogram[3].strip();

        System.out.print("Enter the password: ");
        String password = scanner.nextLine();
        password = asciiToLittleEndian(password);

        String s = KMACXOF256.call(password, "", BigInteger.valueOf(512), asciiToLittleEndian("SK"));
        BigInteger newS = new BigInteger(s, 16).multiply(BigInteger.valueOf(4));

        Ed448GoldilocksPoint Z = new Ed448GoldilocksPoint(new BigInteger(zx), new BigInteger(zy));
        Ed448GoldilocksPoint W = Z.multiply(newS);

        String kake = KMACXOF256.call(W.getX().toString(2), "", BigInteger.valueOf(1024), asciiToLittleEndian("PK"));

        kake = new BigInteger(kake, 16).toString(2);

        String ka = kake.substring(0, 512);
        String ke = kake.substring(512);

        String m = KMACXOF256.call(ke, "", BigInteger.valueOf(c.length()), asciiToLittleEndian("PKE"));

        m = new BigInteger(m, 16).xor(new BigInteger(c, 2)).toString(2);

        String t2 = KMACXOF256.call(ka, m, BigInteger.valueOf(512), asciiToLittleEndian("PKA"));

        if (t.equals(t2)) {
            System.out.println("The tag is valid.");
        } else {
            System.out.println("The tag is invalid.");
        }

    }

    public static void sign(Scanner scanner) {
        // Sign a given file from a given password and write the signature to a file
        System.out.print("Enter the filename of the file to sign: ");
        String filename = scanner.nextLine();
        byte[] fileData = null;
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            fileData = inputStream.readAllBytes();
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to read input file.");
            scanner.close();
            return;
        }

        //File data to binary
        String m = new BigInteger(fileData).toString(2);

        System.out.print("Enter the password: ");
        String password = scanner.nextLine();
        password = asciiToLittleEndian(password);

        String s = KMACXOF256.call(password, "", BigInteger.valueOf(512), asciiToLittleEndian("SK"));
        BigInteger newS = new BigInteger(s, 16).multiply(BigInteger.valueOf(4));

        String k = KMACXOF256.call(s, m, BigInteger.valueOf(512), asciiToLittleEndian("N"));
        BigInteger newK = new BigInteger(k, 16).multiply(BigInteger.valueOf(4));

        BigInteger gx = BigInteger.valueOf(8);
        BigInteger gy = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");
        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(gx, gy);

        Ed448GoldilocksPoint U = G.multiply(newK);

        String h = KMACXOF256.call(U.getX().toString(2), m, BigInteger.valueOf(512), asciiToLittleEndian("T"));

        BigInteger r = new BigInteger("2").pow(446).subtract(new BigInteger("13818066809895115352007386748515426880336692474882178609894547503885"));

        BigInteger hs = new BigInteger(h, 16).multiply(newS);

        BigInteger z = r.subtract(hs).mod(r);

        h = new BigInteger(h, 16).toString(2);

        String signature = h + "," + z.toString(2);

        try {
            FileOutputStream outputStream = new FileOutputStream("signature.txt");
            outputStream.write(signature.getBytes());
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to write to signature file.");
            scanner.close();
            return;
        }

    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Create a Scanner object

        int choice;
        while (true) {

            try {

                System.out.println(
                        """
                                \nChoose an option:
                                1. Compute a plain cryptographic hash of a given file
                                2. Compute an authentication tag (MAC) of a given file under a given passphrase
                                3. Encrypt a given data file symmetrically under a given passphrase
                                4. Decrypt a given symmetric cryptogram under a given passphrase
                                5. Random bit generator.
                                6. Generate an elliptic key pair from a given passphrase and write the public key to a file
                                7. Encrypt a data file under a given elliptic public key file and write the ciphertext to a file
                                8. Decrypt a given elliptic-encrypted file from a given password and write the decrypted data to a file
                                9. Sign a given file from a given password and write the signature to a file
                                10. Verify a given data file and its signature file under a given public key file
                                11. Run Tests
                                """);

                choice = scanner.nextInt(); // Read user input
                scanner.nextLine();
                System.out.println("\nChoice is: " + choice); // Output user input

                if (choice >= 1 && choice <= 11)
                    break;

                System.out.println("\nThat choice is invalid!\n");

            }

            catch (Exception e) {
                System.out.println("\nThat choice is invalid!\n");
                scanner.next();

            }

        }

        switch (choice) {
            case 1:
                hash(scanner);
                break;

            case 2:
                mac(scanner);
                break;
            case 3:
                encrypt(scanner);
                break;
            case 4:
                decrypt(scanner);
                break;
            case 5:
                try {
                    System.out.println("\nHow many random bits? "); // Take in random bits
                    int rand = scanner.nextInt();
                    Random(rand);
                } catch (InputMismatchException e) {
                    System.out.println("\nNot a number! "); // Take in random bits
                }
                break;
            case 6:
                generate_key_pair(scanner);
                break;
            case 7:
                encrypt_elliptic(scanner);
                break;
            case 8:
                decrypt_elliptic(scanner);
                break;
            case 9:
                sign(scanner);
                break;
            case 10:
                verify(scanner);
                break;
            case 11:
                run_tests();
                break;
            default:
                break;
        }
        scanner.close();
    }

    private static void run_tests() {
        Ed448GoldilocksPoint.test();
        test_keccak();
        test_concat();
        test_right_encode();
        test_left_encode();
        test_encode_string();
        test_bytepad();
        test_substring();
        test_cSHAKE_Sample();
    }

    private static void test_concat() {
        String a = "11001";
        String b = "010";
        String expected = "11001010";

        if (Utils.concat(a, b).equals(expected)) {
            System.out.println("concat is successful");
            // System.out.println(Utils.concat(a, b));
        } else {
            System.out.println("concat failed");
            System.out.println(Utils.concat(a, b));
        }
    }

    private static void test_right_encode() {
        // Test right_encode
        BigInteger x = BigInteger.valueOf(0);
        String expected = "0000000010000000";

        if (Utils.right_encode(x).equals(expected)) {
            System.out.println("right_encode is successful");
            // System.out.println(Utils.right_encode(x));
        } else {
            System.out.println("right_encode failed");
            System.out.println(Utils.right_encode(x));
        }

    }

    private static void test_left_encode() {
        // Test right_encode
        BigInteger x = BigInteger.valueOf(0);
        String expected = "1000000000000000";

        if (Utils.left_encode(x).equals(expected)) {
            System.out.println("left_encode is successful");
            // System.out.println(Utils.left_encode(x));
        } else {
            System.out.println("left_encode failed");
            System.out.println(Utils.left_encode(x));
        }

    }

    private static void test_encode_string() {
        // Test right_encode
        String x = "";
        String expected = "1000000000000000";

        if (Utils.encode_string(x).equals(expected)) {
            System.out.println("encode_string is successful");
            // System.out.println(Utils.encode_string(x));
        } else {
            System.out.println("encode_string failed");
            System.out.println(Utils.encode_string(x));
        }

    }

    private static void test_bytepad() {

        String x = "111";
        BigInteger w = BigInteger.valueOf(3);
        String expected = "";

        if (Utils.bytepad(x, w).equals(expected)) {
            System.out.println("bytepad is successful");
            // System.out.println(Utils.bytepad(x, w));
        } else {
            System.out.println("bytepad failed");
            System.out.println(Utils.bytepad(x, w));
        }
    }

    private static void test_substring() {

        String x = "100101";
        BigInteger a = BigInteger.valueOf(3);
        BigInteger b = BigInteger.valueOf(5);
        String expected = "10";

        if (Utils.substring(x, a, b).equals(expected)) {
            System.out.println("substring is successful");
            // System.out.println(Utils.substring(x, a, b));
        } else {
            System.out.println("substring failed");
            System.out.println(Utils.substring(x, a, b));
        }
    }

    private static String Random(int rand) {
        // Created the Random number generator requested
        // from the High-level specification - Michael
        Random random = new Random(System.nanoTime());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rand; i++) {
            boolean bit = random.nextBoolean();
            sb.append(bit ? "1" : "0");
        }

        // System.out.println("\nRandom bits: \n\n" + sb.toString() + "\n");
        // System.out.println("You input " + rand + " bits... \nYep, thats "
        //         + sb.length() + " bits alright.\n");

        return sb.toString();

    }

    private static void test_cSHAKE_Sample() {

        // Testing for cSHAKE: Sample #3, switching N to empty string,
        // and S to "Email Signature".

        // for this
        String filename = "sample.txt";

        // Read the contents of the input file
        byte[] fileData = null;
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            fileData = inputStream.readAllBytes();
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Error: Failed to read input file.");
            return;
        }

        String m = new BigInteger(fileData).toString(2);

        String d = "Email Signature";
        byte[] bytes = d.getBytes();
        BigInteger dBytes = new BigInteger(bytes);
        String binaryString = dBytes.toString(2);
        // Add leading zeros if necessary
        int binaryStringLength = bytes.length * 8; // Calculate expected binary string length
        binaryString = String.format("%" + binaryStringLength + "s", binaryString).replace(' ', '0');

        // String n = "";
        // byte[] bytesn = n.getBytes();
        // BigInteger nBytes = new BigInteger(bytesn);
        // String nbinaryString = nBytes.toString(2);
        // // Add leading zeros if necessary
        // int nbinaryStringLength = bytesn.length * 8; // Calculate expected binary
        // string length
        // nbinaryString = String.format("%" + nbinaryStringLength + "s",
        // nbinaryString).replace(' ', '0');

        String out = KMACXOF256.call("", m, BigInteger.valueOf(512), d);

        System.out.println("\ncShake: Sample #3:\n");
        System.out.println("Make sure to set (h = \"\") in KMACXOF256.");

        System.out.println("Security Strength: 256-bits");
        System.out.println("Length of data is 32-bits");
        System.out.println("Data is 00 01 02 03");
        System.out.println("Requested output length is 512-bits\n");

        System.out.println("Sample Encoded N: 01 00");
        String encodedN = Utils.encode_string("");
        System.out.println("Actual Encoded N: " + Utils.reverser(encodedN));

        System.out.println("====================================================\n");

        System.out.println("Sample Encoded S: 01 78 45 6D 61 69 6C 20 53 69 67 6E 61 74 75 72 65");
        String encodedS = Utils.encode_string(d);
        System.out.println("Actual Encoded S: " + encodedS);

        System.out.println("Email Signature in binary is: " + binaryString);
        System.out.println("");

        int dLength = binaryString.length();
        System.out.println("The length of Email Signature in binary is: " + dLength);

        System.out.println("");

        BigInteger theDLength = BigInteger.valueOf(dLength);
        System.out.println("left_encode() function outputs: " + Utils.left_encode(theDLength));
        BigInteger dHex = new BigInteger(Utils.left_encode(theDLength), 2);
        String hexString1 = dHex.toString(16);
        System.out.println("Which in hex is: " + hexString1);

        BigInteger dHex2 = new BigInteger(Utils.left_encode(theDLength), 2);
        System.out.println("Which when reversed is: " + Utils.reverser(dHex2.toString(2)));
        // reverser(dHex2.toString(2));
        // String hexString12 = dHex2.toString(16);
        // System.out.println("Which in hex is: " + hexString12);

        // System.out.println("this " + dHex.toString());
        // BigInteger dHex2 = new BigInteger(reverser(dHex.toString()), 2);
        // String hexString12 = dHex2.toString(16);
        // System.out.println("reversed is" + hexString12);

        System.out.println("====================================================\n");

        System.out.println();
        System.out.println("Sample Outval is: ");
        System.out.println("D0 08 82 8E 2B 80 AC 9D 22 18 FF EE 1D 07 0C 48\n" +
                "B8 E4 C8 7B FF 32 C9 69 9D 5B 68 96 EE E0 ED D1\n" +
                "64 02 0E 2B E0 56 08 58 D9 C0 0C 03 7E 34 A9 69\n" +
                "37 C5 61 A7 4C 41 2B B4 C7 46 46 95 27 28 1C 8C\n");
        System.out.println("Actual Outval is: ");
        System.out.println(out);
        System.out.println();

    }

}