package sm2.support;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

import static sm2.Curve.*;

public class ECPoint {
    
    private BigInteger x;
    private BigInteger y;
    
    /**
     * 构造非无穷远点
     *
     * @param x 点的x坐标
     * @param y 点的y坐标
     */
    public ECPoint(BigInteger x, BigInteger y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 构造无穷远点，赋值null
     */
    public ECPoint() {
        this.x = null;
        this.y = null;
    }
    
    /**
     * 获取点的x坐标
     *
     * @return 返回点的x坐标
     */
    public BigInteger getX() {
        return this.x;
    }
    
    /**
     * 获取点的y坐标
     *
     * @return 返回点的y坐标
     */
    public BigInteger getY() {
        return this.y;
    }
    
    /**
     * 将点的x坐标置为特定值
     *
     * @param x 点的x坐标
     */
    private void setX(BigInteger x) {
        this.x = x;
    }
    
    /**
     * 将点的y坐标置为特定值
     *
     * @param y 点的y坐标
     */
    private void setY(BigInteger y) {
        this.y = y;
    }
    
    
    /**
     * 判断是否为无穷远点
     *
     * @param p 待判断的点
     * @return 判断结果
     */
    public static boolean isO(ECPoint p) {
        return p.x == null && p.y == null;
    }
    
    /**
     * 将点按照给定进制转换为字符串
     *
     * @param radix 转换的进制
     * @return 指定进制表示的字符串
     */
    public String toString(int radix) {
        BigInteger px = this.x;
        BigInteger py = this.y;
        return ("(" + px.toString(radix) + ", " + py.toString(radix) + ")");
    }
    
    /**
     * 椭圆曲线上的点加运算
     *
     * @param p2 点2
     * @return 点加结果
     */
    public ECPoint plus(ECPoint p2) {
        if (isO(this)) return p2;
        if (isO(p2)) return this;
        BigInteger L, x3, y3;
        if (this.x.compareTo(p2.x) == 0) {
            if (this.y.compareTo(p2.getY()) == 0) {
                L = BigInteger.valueOf(3).multiply(this.x.pow(2)).add(a).multiply(this.y.multiply(BigInteger.valueOf(2)).modInverse(p));
                x3 = L.pow(2).subtract(this.x.multiply(BigInteger.valueOf(2))).mod(p);
                y3 = L.multiply(this.x.subtract(x3)).subtract(this.y).mod(p);
                return new ECPoint(x3, y3);
            } else {
                return new ECPoint();
            }
        } else {
            L = p2.y.subtract(this.y).multiply(p2.x.subtract(this.x).modInverse(p));
            x3 = L.pow(2).subtract(this.x.add(p2.x)).mod(p);
            y3 = L.multiply(this.x.subtract(x3)).subtract(this.y).mod(p);
            return new ECPoint(x3, y3);
        }
    }
    
    /**
     * 椭圆曲线上的倍点运算(由于运算速度较NAFw慢因此放弃使用)
     *
     * @param times 倍数
     * @return 倍点结果
     */
    /*
    public ECPoint multiply(BigInteger times) {
        ECPoint tmp = this.plus(new ECPoint());
        ECPoint result = new ECPoint();
        do {
            if (times.and(BigInteger.ONE).intValue() == 1) {
                result = result.plus(tmp);
            }
            tmp = tmp.plus(tmp);
            times = times.shiftRight(1);
        } while (times.compareTo(BigInteger.ZERO) != 0);
        return result;
    }
    */
    
    /**
     * NAFw算法
     *
     * @param k 倍数
     * @param w 窗口大小
     * @return NAFw(k)
     */
    public static ArrayList<Integer> NAFw(BigInteger k, int w) {
        ArrayList<Integer> list = new ArrayList<>();
        BigInteger ki, MASK = BigInteger.valueOf((1 << w) - 1);
        while (k.compareTo(BigInteger.ONE) >= 0) {
            if (k.and(BigInteger.ONE).intValue() == 1) {
                ki = k.and(MASK);
                k = k.subtract(ki);
                list.add(ki.intValue());
            } else {
                list.add(0);
            }
            k = k.shiftRight(1);
        }
        return list;
    }
    
    
    /**
     * 椭圆曲线上的倍点运算
     *
     * @param k 倍数
     * @return 倍点结果
     */
    public ECPoint multiply(BigInteger k) {
        ArrayList<Integer> kList = NAFw(k, 4);
        ArrayList<ECPoint> pList = new ArrayList<>();
        ECPoint P2 = this.plus(this), Q = new ECPoint(), tmp = this.plus(new ECPoint());
        pList.add(null);
        pList.add(this);
        int ki = 0;
        for (int i = 3; i <= (1 << 4) - 1; i += 2) {
            pList.add(null);
            tmp = tmp.plus(P2);
            pList.add(tmp);
        }
        for (int i = kList.size() - 1; i >= 0; i--) {
            Q = Q.plus(Q);
            ki = kList.get(i);
            if (ki != 0) {
                Q = Q.plus(pList.get(ki));
            }
        }
        return Q;
    }
    
    /**
     * 椭圆曲线上的取反运算
     *
     * @return 点的取反结果
     */
    private ECPoint negative() {
        return new ECPoint(this.x, this.y.negate());
    }
    
    /**
     * Lucas序列生成，用于点的压缩表示
     *
     * @param X 参数X
     * @param Y 参数Y
     * @param p 大素数模
     * @param k 参数k
     * @return Lucas序列结果
     */
    private static BigInteger[] Lucas(BigInteger X, BigInteger Y, BigInteger p, BigInteger k) {
        BigInteger delta = X.pow(2).subtract(Y.multiply(BigInteger.valueOf(4)));
        BigInteger U = BigInteger.ONE, V = X.add(BigInteger.ZERO);
        BigInteger Utmp;
        int r = k.bitLength() - 1;
        for (int i = r - 1; i >= 0; i--) {
            Utmp = U.multiply(V).mod(p);
            V = V.pow(2).add(delta.multiply(U.pow(2))).shiftRight(1).mod(p);
            U = Utmp.add(BigInteger.ZERO);
            if (k.testBit(i)) {
                Utmp = X.multiply(U).add(V).shiftRight(1).mod(p);
                V = X.multiply(V).add(delta.multiply(U)).shiftRight(1);
                U = Utmp.add(BigInteger.ZERO);
            }
        }
        return new BigInteger[]{U, V};
    }
    
    /**
     * 求解模素数的平方根，用于点的压缩表示
     *
     * @param g 待求平方根的数
     * @param p 大素数模
     * @return 模素数的平方根
     */
    private static BigInteger modroot(BigInteger g, BigInteger p) {
        BigInteger z, y, u;
        if (p.mod(BigInteger.valueOf(4)).compareTo(BigInteger.valueOf(3)) == 0) {
            u = p.subtract(BigInteger.valueOf(3)).shiftRight(2);
            y = g.modPow(u.add(BigInteger.ONE), p);
            z = y.modPow(BigInteger.valueOf(2), p);
            if (z.compareTo(g) == 0) {
                return y;
            } else {
                return null;
            }
        } else if (p.mod(BigInteger.valueOf(8)).compareTo(BigInteger.valueOf(5)) == 0) {
            u = p.subtract(BigInteger.valueOf(5)).shiftRight(3);
            z = g.modPow(u.multiply(BigInteger.valueOf(2)).add(BigInteger.ONE), p);
            if (z.mod(p).compareTo(BigInteger.ONE) == 0) {
                y = g.modPow(u.add(BigInteger.ONE), p);
                return y;
            } else if (z.mod(p).compareTo(p.subtract(BigInteger.ONE)) == 0) {
                y = g.multiply(BigInteger.valueOf(2)).multiply(g.multiply(BigInteger.valueOf(4)).modPow(u, p)).mod(p);
                return y;
            }
        } else if (p.mod(BigInteger.valueOf(8)).compareTo(BigInteger.ONE) == 0) {
            u = p.subtract(BigInteger.valueOf(1)).shiftRight(3);
            BigInteger X, U, V;
            BigInteger[] lucas;
            BigInteger Y = g.add(BigInteger.ZERO);
            while (true) {
                X = new BigInteger(p.bitLength(), new Random());
                lucas = Lucas(X, Y, p, u.multiply(BigInteger.valueOf(4)).add(BigInteger.ONE));
                U = lucas[0];
                V = lucas[1];
                if (V.modPow(BigInteger.valueOf(2), p).compareTo(Y.multiply(BigInteger.valueOf(4))) == 0) {
                    return V.shiftRight(1).mod(p);
                } else if ((U.mod(p).compareTo(BigInteger.ONE) != 0) && (U.mod(p).compareTo(p.subtract(BigInteger.ONE)) != 0)) {
                    return null;
                }
            }
        }
        return null;
    }
    
    /**
     * 点的解压缩函数
     *
     * @param x  点的x坐标
     * @param yb y的最右位
     * @return 点的一个y坐标
     */
    public static BigInteger decompress(BigInteger x, int yb) {
        BigInteger alpha = x.pow(3).add(a.multiply(x)).add(b).mod(p);
        BigInteger beta = modroot(alpha, p);
        boolean y0 = beta.testBit(0);
        if ((y0 && yb == 1) || (!y0 && yb == 0)) {
            return beta;
        } else {
            return p.subtract(beta);
        }
    }
    
}

class ECPointTest {
    public static void main(String[] args) {
        
        ECPoint p1 = new ECPoint(new BigInteger("98249f48aa444068f07b943ac27827786192fe0e06c0cf60df650d42e86f7904", 16),
                new BigInteger("18400dcd5fe8c19a56971b6bb2d8dec9c74c4c990d7063c60808555db729185d", 16));
        ECPoint p2 = new ECPoint(BigInteger.valueOf(9), BigInteger.valueOf(6));
        ECPoint result;
        //result = p1.plus(p2);
        //System.out.println(result.toString(16));
        BigInteger times = new BigInteger(p.bitLength(), new Random());
        //BigInteger times = BigInteger.valueOf(1123456789);
        result = p1.multiply(times);
        System.out.println(result.toString(16));
        /*
        result = p1.multi(times);
        System.out.println(result.toString(16));
        result = p1.mult(times);
        System.out.println(result.toString(16));
        */
        
        /*
        BigInteger gx = new BigInteger(
                "32C4AE2C" + "1F198119" + "5F990446" + "6A39C994" + "8FE30BBF" + "F2660BE1" + "715A4589" + "334C74C7", 16);
        String tmp;
        tmp = ECPoint.decompress(gx, 0).toString(16);
        System.out.println(tmp);
        */
    }
}