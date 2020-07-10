package sm2.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;


public class Convert {
    public static byte[] ByteArrayLink(byte[]... params) throws IOException {
        ByteArrayOutputStream bytearray = new ByteArrayOutputStream();
        byte[] result = null;
        for (int i = 0; i < params.length; i++) {
            bytearray.write(params[i]);
        }
        result = bytearray.toByteArray();
        return result;
    }
    
    public static BigInteger Bytes_Integer(byte[] m) {
        int mLen = m.length;
        BigInteger x = BigInteger.valueOf(0);
        int startPos = mLen - 1, endPos = -1;
        while (true) {
            if (m[++endPos] != 0) {
                break;
            }
        }
        for (int i = endPos; i <= startPos; i++) {
            x = x.shiftLeft(8);
            x = x.add(BigInteger.valueOf((m[i] & 0xff)));
        }
        return x;
    }
    
    public static byte[] Integer_Bytes(BigInteger x, int k) throws Exception {
        if (x.compareTo(BigInteger.valueOf(2).pow(8 * k)) >= 0) {
            throw new Exception("x is too large to convert");
        }
        byte[] m = new byte[k];
        for (int i = k - 1; i >= 0; i--) {
            m[i] = (byte) x.and(BigInteger.valueOf(0xff)).intValue();
            x = x.shiftRight(8);
        }
        return m;
    }
}

class ConvertTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String a = sc.nextLine();
        String b = sc.nextLine();
        String c = sc.nextLine();
        try {
            byte[] result = Convert.ByteArrayLink(a.getBytes(), b.getBytes(), c.getBytes());
            System.out.println(new String(result));
            while (true) {
                String str = sc.next();
                byte[] bytes = Convert.Integer_Bytes(new BigInteger(str), 100);
                System.out.println(Convert.Bytes_Integer(bytes));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
