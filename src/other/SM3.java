package other;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Scanner;

public class SM3 {

    private static char[] hexEnum = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String ivHexStr = "7380166f 4914b2b9 172442d7 da8a0600 a96f30bc 163138aa e38dee4d b0fb0e4e";
    private static final BigInteger IV = new BigInteger(ivHexStr.replaceAll(" ", ""), 16);
    private static final Integer T1 = 0x79cc4519;
    private static final Integer T2 = 0x7a879d8a;
    private static final byte[] FirstPadding = {(byte) 0x80};
    private static final byte[] ZeroPadding = {(byte) 0x00};

    private static int T(int j) {
        if (j <= 15) {
            return T1;
        } else {
            return T2;
        }
    }

    private static Integer FF(Integer x, Integer y, Integer z, int j) {
        if (j <= 15) {
            return x ^ y ^ z;
        } else {
            return (x & y) | (x & z) | (y & z);
        }
    }

    private static Integer GG(Integer x, Integer y, Integer z, int j) {
        if (j <= 15) {
            return x ^ y ^ z;
        } else {
            return (x & y) | (~x & z);
        }
    }

    private static Integer P0(Integer x) {
        return x ^ Integer.rotateLeft(x, 9) ^ Integer.rotateLeft(x, 17);
    }

    private static Integer P1(Integer x) {
        return x ^ Integer.rotateLeft(x, 15) ^ Integer.rotateLeft(x, 23);
    }

    /**
     * 'padding' is used to pad msg
     *
     * @param source is the msg
     * @return a padded msg
     * @throws IOException due to length judge
     */
    private static byte[] padding(byte[] source) throws IOException {
        if (source.length >= 0x2000000000000000L) {
            throw new RuntimeException("src data invalid.");
        }
        long sourceLen = source.length << 3;
        long zeroLen = 448 - ((sourceLen + 1) & 511);
        if (zeroLen < 0) {
            zeroLen = zeroLen + 512;
        }
        ByteArrayOutputStream bytearray = new ByteArrayOutputStream();
        bytearray.write(source);
        bytearray.write(FirstPadding);
        long i = zeroLen - 7;
        while (i > 0) {
            bytearray.write(ZeroPadding);
            i -= 8;
        }
        bytearray.write(LongToBytes(sourceLen));
        return bytearray.toByteArray();
    }

    private static byte[] LongToBytes(long l) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (l >> ((7 - i) << 3));
        }
        return bytes;
    }

    /**
     * 'hash' is the main function
     *
     * @param source is the msg
     * @return the value of hash
     * @throws IOException due to padding and CF
     */
    public static byte[] hash(byte[] source) throws IOException {
        byte[] m = padding(source);
        int blockNum = m.length >> 6;
        byte[] b;
        byte[] vi = IV.toByteArray();
        for (int i = 0; i < blockNum; i++) {
            b = Arrays.copyOfRange(m, i << 6, (i + 1) << 6);
            vi = CF(vi, b);
        }
        return vi;
    }

    /**
     * 'CF' is a compression function
     *
     * @param vi is a copy of VI
     * @param bi is the msg
     * @return a byte-array
     * @throws IOException due to toInteger()
     */
    private static byte[] CF(byte[] vi, byte[] bi) throws IOException {
        int a, b, c, d, e, f, g, h;
        a = toInteger(vi, 0);
        b = toInteger(vi, 1);
        c = toInteger(vi, 2);
        d = toInteger(vi, 3);
        e = toInteger(vi, 4);
        f = toInteger(vi, 5);
        g = toInteger(vi, 6);
        h = toInteger(vi, 7);

        int[] w = new int[68];
        int[] wp = new int[64];
        for (int i = 0; i < 16; i++) {
            w[i] = toInteger(bi, i);
        }
        for (int j = 16; j < 68; j++) {
            w[j] = P1(w[j - 16] ^ w[j - 9] ^ Integer.rotateLeft(w[j - 3], 15))
                    ^ Integer.rotateLeft(w[j - 13], 7) ^ w[j - 6];
        }
        for (int j = 0; j < 64; j++) {
            wp[j] = w[j] ^ w[j + 4];
        }
        int ss1, ss2, tt1, tt2;
        for (int j = 0; j < 64; j++) {
            ss1 = Integer.rotateLeft(Integer.rotateLeft(a, 12) + e + Integer.rotateLeft(T(j), j), 7);
            ss2 = ss1 ^ Integer.rotateLeft(a, 12);
            tt1 = FF(a, b, c, j) + d + ss2 + wp[j];
            tt2 = GG(e, f, g, j) + h + ss1 + w[j];
            d = c;
            c = Integer.rotateLeft(b, 9);
            b = a;
            a = tt1;
            h = g;
            g = Integer.rotateLeft(f, 19);
            f = e;
            e = P0(tt2);
        }
        byte[] v = toByteArray(a, b, c, d, e, f, g, h);
        for (int i = 0; i < v.length; i++) {
            v[i] = (byte) (v[i] ^ vi[i]);
        }
        return v;
    }

    /**
     * 'toInteger' extracts an integer from a byte-array
     *
     * @param source is a byte-array
     * @param index  is the index
     * @return an integer
     */
    private static int toInteger(byte[] source, int index) {
        StringBuilder valueStr = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            valueStr.append(hexEnum[(byte) ((source[index * 4 + i] & 0xf0) >> 4)]);
            valueStr.append(hexEnum[(byte) (source[index * 4 + i] & 0x0f)]);
        }
        return Long.valueOf(valueStr.toString(), 16).intValue();

    }

    /**
     * 'toByteArray' sets 8 integers into a byte-array
     *
     * @param a,b,c,d,e,f,g,h are integers
     * @return a byte-array
     * @throws IOException due to ByteArrayOutputStream
     */
    private static byte[] toByteArray(int a, int b, int c, int d, int e, int f,
                                      int g, int h) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        baos.write(toByteArray(a));
        baos.write(toByteArray(b));
        baos.write(toByteArray(c));
        baos.write(toByteArray(d));
        baos.write(toByteArray(e));
        baos.write(toByteArray(f));
        baos.write(toByteArray(g));
        baos.write(toByteArray(h));
        return baos.toByteArray();
    }

    /**
     * 'toByteArray' is used to convert integer into byte-array
     *
     * @param i is an integer
     * @return a byte-array
     */
    public static byte[] toByteArray(int i) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) (i >> 24);
        byteArray[1] = (byte) ((i >> 16) & 0xff);
        byteArray[2] = (byte) ((i >> 8) & 0xff);
        byteArray[3] = (byte) (i & 0xff);
        return byteArray;
    }

    /**
     * 'ByteToHex' is used to convert byte into hex-string
     *
     * @param b is a byte
     * @return a hex-string
     */
    private static String ByteToHex(byte b) {
        int n = (int) b & 0xff;
        int d1 = n >> 4;
        int d2 = n & 15;
        return "" + hexEnum[d1] + hexEnum[d2];
    }

    /**
     * 'hexdigit' is used to convert byte-array into hex-string
     *
     * @param b is a byte-array
     * @return a hex-string
     */
    public static String hexdigit(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte i : b) {
            resultSb.append(ByteToHex(i));
        }
        return resultSb.toString();
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        String a = sc.nextLine();
        for (int i = 0; i < 1000; i++) {
            SM3.hexdigit(SM3.hash(a.getBytes()));
        }
        System.out.println(SM3.hexdigit(SM3.hash(a.getBytes())));
    }
}