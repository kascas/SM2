package sm2;


import sm2.support.Convert;
import sm2.support.ECPoint;
import sm3.SM3;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import static sm2.Curve.*;

public class SM2 {
    
    private byte[] za;
    private BigInteger d;
    private ECPoint P;
    
    /**
     * 利用IDA和密钥对，实现SM2的初始化
     *
     * @param IDA     用户可辨别标识
     * @param keypair 密钥对
     * @throws Exception ZA中的异常
     */
    public SM2(String IDA, KeyPair keypair) throws Exception {
        this.d = keypair.getPrivate();
        this.P = keypair.getPublic();
        this.za = null;
        this.ZA(IDA);
    }
    
    public SM2(byte[] za, KeyPair keypair) {
        this.d = keypair.getPrivate();
        this.P = keypair.getPublic();
        this.za = za;
    }
    
    public SM2(String IDA, ECPoint publickey) throws Exception {
        this.d = null;
        this.P = publickey;
        this.za = null;
        this.ZA(IDA);
    }
    
    public SM2(byte[] za, ECPoint publickey) {
        this.d = null;
        this.P = publickey;
        this.za = za;
    }
    
    /**
     * 用户信息生成函数ZA
     *
     * @param IDA 用户可辨别标识
     * @throws Exception bytearray中可能出现异常
     */
    private void ZA(String IDA) throws Exception {
        byte[] IDAbytes = IDA.getBytes(StandardCharsets.US_ASCII);
        int entlenA = IDAbytes.length * 8;
        byte[] ENTLA = new byte[]{(byte) (entlenA & 0xFF00), (byte) (entlenA & 0x00FF)};
        this.za = SM3.hash(Convert.ByteArrayLink(ENTLA, IDAbytes, a.toByteArray(), b.toByteArray(),
                gx.toByteArray(), gy.toByteArray(), this.P.getX().toByteArray(), this.P.getY().toByteArray()));
    }
    
    public static byte[] ZA(String IDA, ECPoint P) throws Exception {
        byte[] IDAbytes = IDA.getBytes(StandardCharsets.US_ASCII);
        int entlenA = IDAbytes.length * 8;
        byte[] ENTLA = new byte[]{(byte) (entlenA & 0xFF00), (byte) (entlenA & 0x00FF)};
        return SM3.hash(Convert.ByteArrayLink(ENTLA, IDAbytes, a.toByteArray(), b.toByteArray(),
                gx.toByteArray(), gy.toByteArray(), P.getX().toByteArray(), P.getY().toByteArray()));
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
    
    public byte[] getZA() {
        return this.za;
    }
    
    /**
     * 签名函数
     *
     * @param M 待签名消息
     * @return 签名的列表(r, s)
     * @throws Exception 异常
     */
    public ArrayList<byte[]> sign(byte[] M) throws Exception {
        byte[] _M = Convert.ByteArrayLink(this.za, M);
        ArrayList<byte[]> SIGN = new ArrayList<>();
        BigInteger e = new BigInteger(1, SM3.hash(_M));
        BigInteger k, r, x, s;
        ECPoint G = new ECPoint(gx, gy);
        do {
            do {
                k = new BigInteger(n.bitLength(), new Random());
                //k = new BigInteger("6CB28D99385C175C94F94E934817663FC176D925DD72B727260DBAAE1FB2F96F", 16);
                x = G.multiply(k).getX();
                r = e.add(x).mod(n);
            } while (r.compareTo(BigInteger.ZERO) == 0 || r.add(k).compareTo(n) == 0);
            s = this.d.add(BigInteger.ONE).modInverse(n).multiply(k.subtract(r.multiply(this.d))).mod(n);
        } while (s.compareTo(BigInteger.ZERO) == 0);
        SIGN.add(r.toByteArray());
        SIGN.add(s.toByteArray());
        return SIGN;
    }
    
    /**
     * 验证函数
     *
     * @param M    待验证消息
     * @param sign 数字签名
     * @return 验证结果
     * @throws Exception 异常
     */
    public boolean verify(byte[] M, ArrayList<byte[]> sign) throws Exception {
        BigInteger r = new BigInteger(1, sign.get(0));
        BigInteger s = new BigInteger(1, sign.get(1));
        if (s.compareTo(n) >= 0 || s.compareTo(BigInteger.ZERO) == 0) {
            return false;
        }
        if (r.compareTo(n) >= 0 || r.compareTo(BigInteger.ZERO) == 0) {
            return false;
        }
        byte[] _M = Convert.ByteArrayLink(this.za, M);
        BigInteger e = new BigInteger(1, SM3.hash(_M));
        BigInteger t = r.add(s).mod(n);
        if (t.compareTo(BigInteger.ZERO) == 0) {
            return false;
        }
        ECPoint point = new ECPoint(gx, gy).multiply(s).plus(this.P.multiply(t));
        return e.add(point.getX()).mod(n).compareTo(r) == 0;
    }
}

class SM2Test {
    public static void main(String[] args) throws Exception {
        /*
        BigInteger dA = new BigInteger("128B2FA8BD433C6C068C8D803DFF79792A519A55171B1B650C23661D15897263", 16);
        BigInteger x = new BigInteger("0AE4C7798AA0F119471BEE11825BE46202BB79E2A5844495E97C04FF4DF2548A", 16);
        BigInteger y = new BigInteger("7C0240F88F1CD4E16352A73C17B7F16F07353E53A176D684A9FE0C6BB798E857", 16);
        ECPoint PA = new ECPoint(x, y);
        KeyPair key = new KeyPair(dA, PA);
        */
        KeyPair key = new KeyPair();
        System.out.println("Private key: " + key.getPrivate().toString(16));
        System.out.println("Public  key: " + key.getPublic().toString(16));
        byte[] M = "message digest".getBytes(StandardCharsets.UTF_8);
        byte[] _M = "message dagest".getBytes(StandardCharsets.UTF_8);
        String IDA = "ALICE123@YAHOO.COM";
        SM2 s;
        ArrayList<byte[]> SIGN;
        
        s = new SM2(IDA, key);
        for (int i = 0; i < 100; i++) {
            SIGN = s.sign(M);
        }
        SIGN = s.sign(M);
        System.out.println(">>> sign");
        System.out.println("r: " + Convert.Bytes_Integer(SIGN.get(0)).toString(16));
        System.out.println("s: " + Convert.Bytes_Integer(SIGN.get(1)).toString(16));
        
        s = new SM2(IDA, key.getPublic());
        for (int i = 0; i < 100; i++) {
            s.verify(M, SIGN);
        }
        System.out.println(">>> verify");
        System.out.println("result: " + s.verify(M, SIGN));
        System.out.println("another msg's verify: " + s.verify(_M, SIGN));
    }
}
