package sm2;

import sm2.support.ECPoint;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

import static sm2.Curve.gx;
import static sm2.Curve.gy;
import static sm2.Curve.p;
import static sm2.Curve.a;
import static sm2.Curve.b;
import static sm2.Curve.n;

public class KeyPair {
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
