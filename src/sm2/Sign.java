package sm2;


import sm2.support.Convert;
import sm2.support.ECPoint;
import sm3.SM3;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static sm2.Curve.*;

public class Sign {
    
    private byte[] R;
    private byte[] S;
    private byte[] za;
    private BigInteger d;
    private ECPoint P;
    
    public Sign(String IDA, KeyPair key) throws Exception {
        this.d = key.getPrivate();
        this.P = key.getPublic();
        this.za = ZA(IDA);
        this.R = null;
        this.S = null;
    }
    
    private byte[] ZA(String IDA) throws Exception {
        byte[] IDAbytes = IDA.getBytes(StandardCharsets.US_ASCII);
        int entlenA = IDAbytes.length * 8;
        byte[] ENTLA = new byte[]{(byte) (entlenA & 0xFF00), (byte) (entlenA & 0x00FF)};
        byte[] Za = SM3.hash(Convert.ByteArrayLink(ENTLA, IDAbytes, a.toByteArray(), b.toByteArray(),
                gx.toByteArray(), gy.toByteArray(), this.P.getX().toByteArray(), this.P.getY().toByteArray()));
        return Za;
    }
    
    //following functions are used to debug
    /*
    public static void print(BigInteger b) {
        System.out.println(b.toString(16));
    }
    
    public static void print(String b) {
        System.out.println(b);
    }
    
    public static void print(byte[] b) {
        System.out.println(Convert.Bytes_Integer(b).toString(16));
    }
    */
    
    public byte[] getR() {
        return this.R;
    }
    
    public byte[] getS() {
        return this.S;
    }
    
    public void signature(byte[] M) throws Exception {
        byte[] _M = Convert.ByteArrayLink(this.za, M);
        BigInteger e = new BigInteger(1, SM3.hash(_M));
        BigInteger k, r, x, s;
        ECPoint G = new ECPoint(gx, gy);
        do {
            do {
                k = new BigInteger(n.bitLength(), new Random());
                //k = new BigInteger("6CB28D99385C175C94F94E934817663FC176D925DD72B727260DBAAE1FB2F96F", 16);
                x = ECPoint.multiply(G, k).getX();
                r = e.add(x).mod(n);
            } while (r.compareTo(BigInteger.ZERO) == 0 || r.add(k).compareTo(n) == 0);
            s = this.d.add(BigInteger.ONE).modInverse(n).multiply(k.subtract(r.multiply(this.d))).mod(n);
        } while (s.compareTo(BigInteger.ZERO) == 0);
        this.R = r.toByteArray();
        this.S = s.toByteArray();
    }
}

class SM2Test {
    public static void main(String[] args) throws Exception {
        BigInteger dA = new BigInteger("128B2FA8BD433C6C068C8D803DFF79792A519A55171B1B650C23661D15897263", 16);
        BigInteger x = new BigInteger("0AE4C7798AA0F119471BEE11825BE46202BB79E2A5844495E97C04FF4DF2548A", 16);
        BigInteger y = new BigInteger("7C0240F88F1CD4E16352A73C17B7F16F07353E53A176D684A9FE0C6BB798E857", 16);
        ECPoint PA = new ECPoint(x, y);
        KeyPair key = new KeyPair(dA, PA);
        byte[] M = "message digest".getBytes(StandardCharsets.UTF_8);
        String IDA = "ALICE123@YAHOO.COM";
        Sign s = null;
        s = new Sign(IDA, key);
        for (int i = 0; i < 100; i++)
            s.signature(M);
        System.out.println(Convert.Bytes_Integer(s.getR()).toString(16));
        System.out.println(Convert.Bytes_Integer(s.getS()).toString(16));
    }
}
