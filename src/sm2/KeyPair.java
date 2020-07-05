package sm2;

import sm2.support.ECPoint;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class KeyPair {
    /**
     * SM2椭圆曲线公钥密码算法推荐曲线参数
     */
    private static BigInteger n = new BigInteger(
            "FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "7203DF6B" + "21C6052B" + "53BBF409" + "39D54123", 16);
    private static BigInteger p = new BigInteger(
            "FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "00000000" + "FFFFFFFF" + "FFFFFFFF", 16);
    private static BigInteger a = new BigInteger(
            "FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "00000000" + "FFFFFFFF" + "FFFFFFFC", 16);
    private static BigInteger b = new BigInteger(
            "28E9FA9E" + "9D9F5E34" + "4D5A9E4B" + "CF6509A7" + "F39789F5" + "15AB8F92" + "DDBCBD41" + "4D940E93", 16);
    private static BigInteger gx = new BigInteger(
            "32C4AE2C" + "1F198119" + "5F990446" + "6A39C994" + "8FE30BBF" + "F2660BE1" + "715A4589" + "334C74C7", 16);
    private static BigInteger gy = new BigInteger(
            "BC3736A2" + "F4F6779C" + "59BDCEE3" + "6B692153" + "D0A9877C" + "C62A4740" + "02DF32E5" + "2139F0A0", 16);
    
    /**
     * 泛型数组存放密钥对，index=0为私钥，index=1位公钥
     */
    private ArrayList<Object> keypair;
    
    /**
     * 构造器调用generator生成密钥对
     */
    public KeyPair() {
        this.keypair = generator();
    }
    
    /**
     * 密钥对生成器，需要对密钥进行验证
     *
     * @return 泛型数组
     */
    private static ArrayList<Object> generator() {
        BigInteger d;
        ECPoint P;
        ArrayList<Object> key;
        do {
            do {
                d = new BigInteger(p.bitLength(), new Random());
            } while (d.compareTo(n.subtract(BigInteger.valueOf(2))) > 0 || d.compareTo(BigInteger.ZERO) == 0);
            P = ECPoint.multiply(new ECPoint(gx, gy), d);
            key = new ArrayList<>();
            key.add(d);
            key.add(P);
        } while (!test(P));
        return key;
    }
    
    /**
     * 获取私钥
     *
     * @return 私钥
     */
    public BigInteger getPrivate() {
        return (BigInteger) this.keypair.get(0);
    }
    
    /**
     * 获取公钥
     *
     * @return 公钥
     */
    public ECPoint getPublic() {
        return (ECPoint) this.keypair.get(1);
    }
    
    /**
     * 公钥的验证
     *
     * @param P 公钥点
     * @return 验证结果
     */
    private static boolean test(ECPoint P) {
        BigInteger x = P.getX(), y = P.getY();
        if (ECPoint.isO(P)) {
            return false;
        } else if (x.compareTo(p) > 0 || y.compareTo(p) > 0) {
            return false;
        } else if (y.pow(2).mod(p).compareTo(x.pow(3).add(x.multiply(a)).add(b).mod(p)) != 0) {
            return false;
        } else return ECPoint.isO(ECPoint.multiply(P, n));
    }
}

class KeyPairTest {
    public static void main(String[] args) {
        KeyPair key = new KeyPair();
        System.out.println(key.getPrivate().toString(16));
        System.out.println(key.getPublic().toString(16));
    }
}
