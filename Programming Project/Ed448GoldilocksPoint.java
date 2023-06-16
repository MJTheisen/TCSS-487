import java.math.BigInteger;
import java.util.Random;

public class Ed448GoldilocksPoint {
    public static final BigInteger P = BigInteger.valueOf(2)
                                                  .pow(448)
                                                  .subtract(BigInteger.valueOf(2)
                                                  .pow(224))
                                                  .subtract(BigInteger.ONE);
    
    public static final BigInteger D = BigInteger.valueOf(-39081);

    private BigInteger x;
    private BigInteger y;

    // Neutral element constructor
    public Ed448GoldilocksPoint() {
        this.x = BigInteger.ZERO;
        this.y = BigInteger.ONE;
    }

    // Constructor for a curve point given x and y coordinates
    public Ed448GoldilocksPoint(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }

    // Constructor for a curve point given x coordinate and least significant bit of y
    public Ed448GoldilocksPoint(BigInteger x, boolean yLsb) {

        BigInteger t1 = BigInteger.ONE.add(D.multiply(x.pow(2))).mod(P);
        BigInteger t2 = BigInteger.ONE.subtract(x.pow(2)).mod(P);
        BigInteger y = Ed448GoldilocksPoint.sqrt(t2.divide(t1), P, yLsb);

        if (y == null) {
            throw new IllegalArgumentException();
        }

        this.x = x;
        this.y = y;

    }

    public BigInteger getX() {
        return this.x;
    }

    public BigInteger getY() {
        return this.y;
    }

    public boolean equals(Ed448GoldilocksPoint other) {
        return this.x.equals(other.x) && this.y.equals(other.y);
    }

    public Ed448GoldilocksPoint negate() {
        return new Ed448GoldilocksPoint(this.x.negate(), this.y);
    }

    public Ed448GoldilocksPoint add(Ed448GoldilocksPoint point) {
        if (this.equals(point.negate())) {
            return new Ed448GoldilocksPoint();
        }
    
        BigInteger t1 = this.x.multiply(point.y);
        BigInteger t2 = this.y.multiply(point.x);
        BigInteger t3 = this.y.multiply(point.y);
        BigInteger t4 = this.x.multiply(point.x);
        
        BigInteger n1 = t1.add(t2);
        BigInteger n2 = t3.subtract(t4);

        BigInteger d1 = BigInteger.ONE.add(D.multiply(t4).multiply(t3));
        BigInteger d2 = BigInteger.ONE.subtract(D.multiply(t4).multiply(t3));

        BigInteger x3 = n1.multiply(d1.modInverse(P)).mod(P);
        BigInteger y3 = n2.multiply(d2.modInverse(P)).mod(P);
    
        return new Ed448GoldilocksPoint(x3, y3);
    }

    public Ed448GoldilocksPoint multiply(BigInteger scalar) {
        if (scalar.equals(BigInteger.ZERO)) {
            return new Ed448GoldilocksPoint();
        }

        Ed448GoldilocksPoint V = new Ed448GoldilocksPoint();

        String s = scalar.toString(2);

        for (int i = 0; i < s.length(); i++) {
            char scalarBit = s.charAt(i);
            V = V.add(V);
            if (scalarBit == '1') {
                V = V.add(this);
            }
        }
    
        return V;
    }

    public static BigInteger sqrt(BigInteger v, BigInteger p, boolean lsb) {
        assert (p.testBit(0) && p.testBit(1)); // p = 3 (mod 4)
        if (v.signum() == 0) {
            return BigInteger.ZERO;
        }
        BigInteger r = v.modPow(p.shiftRight(2).add(BigInteger.ONE), p);
        if (r.testBit(0) != lsb) {
            r = p.subtract(r); // correct the lsb
        }
        return (r.multiply(r).subtract(v).mod(p).signum() == 0) ? r : null;
    }

    public static void test() {
        testNegation();
        test1();
        test2();
        test3();
        test4();
        test4Addition();
        test5();
        test6();
        test7();
        test8();
        test9();
        test10();
        test11();
        test12();
        test13();
        test14();
        test15();
        test16();
    }

    private static void testNegation() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = BigInteger.valueOf(-8);
        BigInteger ey = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        System.out.println("Test Negation: " + G.negate().equals(expected));

    }

    private static void test1() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint();

        System.out.println("G + (-G) = (0, 1): " + G.add(G.negate()).equals(expected));

    }

    private static void test2() {
        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint();

        System.out.println("0 * G = (0, 1): " + G.multiply(BigInteger.ZERO).equals(expected));
    }

    private static void test3() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);

        System.out.println("1 * G = G: " + G.multiply(BigInteger.ONE).equals(G));

    }

    private static void test4() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = new BigInteger("42007552351264578789797228057894459961356635936091310669348309451323231688505161412213361059716631570103207189550914106284809891202283");
        BigInteger ey = new BigInteger("641561412352177824576862244372569105796243722448246439485611207948993206158064852999956196096946797240496633864263100472962939298534384");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        BigInteger scalar = new BigInteger("2");

        System.out.println("2 * G: " + G.multiply(scalar).equals(expected));

    }

    private static void test4Addition() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = new BigInteger("42007552351264578789797228057894459961356635936091310669348309451323231688505161412213361059716631570103207189550914106284809891202283");
        BigInteger ey = new BigInteger("641561412352177824576862244372569105796243722448246439485611207948993206158064852999956196096946797240496633864263100472962939298534384");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        System.out.println("G + G: " + G.add(G).equals(expected));


    }

    private static void test5() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = new BigInteger("129032643833714290833858544491244228016814773782190073611260267833419012499651216005301827440390263059088595435476889081593143579977481");
        BigInteger ey = new BigInteger("480623012143127020110526778401068443288909801891983852372846534358695169309060365611199893137169820679553209338654251091748650611116231");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        BigInteger scalar = new BigInteger("3");

        System.out.println("3 * G: " + G.multiply(scalar).equals(expected));

    }

    private static void test6() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = new BigInteger("643803048579566018101435101984667543413799263105802319829573773648738777526265040407467112090895979118454320373494698440183350716180025");
        BigInteger ey = new BigInteger("18046486704703396559443919260262355086851280454601061888995683053273156437893951363867170785092417158250681233590484105769844249414878");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        BigInteger scalar = new BigInteger("4");

        System.out.println("4 * G: " + G.multiply(scalar).equals(expected));

    }

    private static void test7() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = new BigInteger("309945679270364188010733639403210715114146511612268041913479485050809416163403241257450519440627332119240433605829157179701391106422192");
        BigInteger ey = new BigInteger("442144692123427791446741391667103850735186996340347897485241760532045814242949864238061468292719825690696712724478529995158228180808565");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        BigInteger scalar = new BigInteger("5");

        System.out.println("5 * G: " + G.multiply(scalar).equals(expected));

    }

    private static void test8() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = new BigInteger("639127612046933013957233842532433616328303658870956500869326504710209850502619825682287547630958434542955842914726802407378128867741433");
        BigInteger ey = new BigInteger("723942701470481011422720562389574247094610395466744784577853342285048267822645093913011985122362141667638111822879849148099030621119147");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        BigInteger scalar = new BigInteger("6");

        System.out.println("6 * G: " + G.multiply(scalar).equals(expected));

    }

    private static void test9() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = new BigInteger("519118340733949795181192486446842611377288178642996119887871491389401218829349765740221396364814620087693462174423294868251687163974704");
        BigInteger ey = new BigInteger("645177178362295980418831784965993095805073166300122523949891058364019469763654981296820434837453312402937989848236926578987264921862783");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        BigInteger scalar = new BigInteger("7");

        System.out.println("7 * G: " + G.multiply(scalar).equals(expected));

    }

    private static void test10() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = new BigInteger("322458695266717292244233504623819713982940201170086860753133253653246618490358078354698063941088474530349739644255432152966625757785206");
        BigInteger ey = new BigInteger("275896460466705704734935549735944857172840076155469986901454984644766316915006013707938105197446415437527868162799018964936077024972356");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        BigInteger scalar = new BigInteger("8");

        System.out.println("8 * G: " + G.multiply(scalar).equals(expected));

    }

    private static void test11() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger ex = new BigInteger("172920336268845609411414579520555377916644613369033657162389810010703201049130450056635106198937252144075474001293730082819165964341658");
        BigInteger ey = new BigInteger("232799299980025230427898028084919250406387075449199690186462259001164254620436632976157873478428832929391960079750893467691017680283614");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint(ex, ey);

        BigInteger scalar = new BigInteger("9");

        System.out.println("9 * G: " + G.multiply(scalar).equals(expected));

    }

    private static void test12() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
        Ed448GoldilocksPoint expected = new Ed448GoldilocksPoint();

        BigInteger scalar = BigInteger.ZERO;

        System.out.println("0 * G: " + G.multiply(scalar).equals(expected));

    }

    // Test k*G = (k mod r)*G
    private static void test13() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger r = new BigInteger("2").pow(446).subtract(new BigInteger("13818066809895115352007386748515426880336692474882178609894547503885"));

        int n = 10;

        for (int i = 0; i < n; i++) {

            BigInteger scalar = new BigInteger(P.bitLength(), new Random());

            Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
            Ed448GoldilocksPoint expected = G.multiply(scalar.mod(r));

            if (!G.multiply(scalar).equals(expected)) {
                System.out.println("k * G = (k mod r) * G: false");
                return;
            }

        }

        System.out.println("k * G = (k mod r) * G: true");

    }

    // Test (k + 1)*G = (k*G) + G
    private static void test14() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        int n = 10;

        for (int i = 0; i < n; i++) {

            BigInteger scalar = new BigInteger(P.bitLength(), new Random());

            Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
            Ed448GoldilocksPoint expected = G.multiply(scalar).add(G);

            if (!G.multiply(scalar.add(BigInteger.ONE)).equals(expected)) {
                System.out.println("(k + 1) * G = (k * G) + G: false");
                return;
            }

        }

        System.out.println("(k + 1) * G = (k * G) + G: true");

    }

    // Test (k + t)*G = (k*G) + (t*G)
    private static void test15() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        int n = 10;

        for (int i = 0; i < n; i++) {

            BigInteger scalar1 = new BigInteger(P.bitLength(), new Random());
            BigInteger scalar2 = new BigInteger(P.bitLength(), new Random());

            Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
            Ed448GoldilocksPoint expected = G.multiply(scalar1).add(G.multiply(scalar2));

            if (!G.multiply(scalar1.add(scalar2)).equals(expected)) {
                System.out.println("(k + t) * G = (k * G) + (t * G): false");
                return;
            }

        }

        System.out.println("(k + t) * G = (k * G) + (t * G): true");

    }

    // Test k*(t*P) = t*(k*G) = (k*t mod r)*G
    private static void test16() {

        BigInteger x = BigInteger.valueOf(8);
        BigInteger y = new BigInteger("563400200929088152613609629378641385410102682117258566404750214022059686929583319585040850282322731241505930835997382613319689400286258");

        BigInteger r = new BigInteger("2").pow(446).subtract(new BigInteger("13818066809895115352007386748515426880336692474882178609894547503885"));

        int n = 10;

        for (int i = 0; i < n; i++) {

            BigInteger scalar1 = new BigInteger(P.bitLength(), new Random());
            BigInteger scalar2 = new BigInteger(P.bitLength(), new Random());

            Ed448GoldilocksPoint G = new Ed448GoldilocksPoint(x, y);
            Ed448GoldilocksPoint expected = G.multiply(scalar1.multiply(scalar2).mod(r));

            if (!G.multiply(scalar1).multiply(scalar2).equals(expected)) {
                System.out.println("k * (t * G) = t * (k * G) = (k * t mod r) * G: false");
                return;
            }

        }

        System.out.println("k * (t * G) = t * (k * G) = (k * t mod r) * G: true");

    }
}