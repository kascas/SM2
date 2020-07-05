package sm2.support;

import java.math.BigInteger;
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
    
    //扩展的欧几里得算法
    /*
    private static BigInteger[] gcd(BigInteger a, BigInteger b) {
        BigInteger x1 = BigInteger.ONE, x2 = BigInteger.ZERO;
        BigInteger y1 = BigInteger.ZERO, y2 = BigInteger.ONE;
        BigInteger t1 = BigInteger.ZERO, t2 = BigInteger.ZERO;
        BigInteger tmp;
        while (true) {
            t1 = x2.add(BigInteger.ZERO);
            t2 = y2.add(BigInteger.ZERO);
            x2 = x1.subtract(a.divide(b).multiply(x2));
            y2 = y1.subtract(a.divide(b).multiply(y2));
            x1 = t1.add(BigInteger.ZERO);
            y1 = t2.add(BigInteger.ZERO);
            if (a.mod(b).compareTo(BigInteger.ZERO) == 0) {
                break;
            }
            tmp = a.add(BigInteger.ZERO);
            a = b.add(BigInteger.ZERO);
            b = tmp.mod(b);
        }
        if (b.compareTo(BigInteger.ZERO) < 0) {
            b = b.negate();
            x1 = x1.negate();
            y1 = y1.negate();
        }
        return new BigInteger[]{b, x1, y1};
    }
    
    private static BigInteger modinvert(BigInteger a, BigInteger mod) {
        BigInteger x = gcd(a, mod)[1];
        while (x.compareTo(BigInteger.ZERO) <= 0) {
            x = x.add(mod);
        }
        return x;
    }
    */
    
    /**
     * 判断是否为无穷远点
     *
     * @param p 待判断的点
     * @return 判断结果
     */
    public static boolean isO(ECPoint p) {
        return p.getX() == null && p.getY() == null;
    }
    
    /**
     * 将点按照给定进制转换为字符串
     *
     * @param radix 转换的进制
     * @return 指定进制表示的字符串
     */
    public String toString(int radix) {
        BigInteger px = this.getX();
        BigInteger py = this.getY();
        return ("(" + px.toString(radix) + ", " + py.toString(radix) + ")");
    }
    
    /**
     * 椭圆曲线上的点加运算
     *
     * @param p1 点1
     * @param p2 点2
     * @return 点加结果
     */
    public static ECPoint plus(ECPoint p1, ECPoint p2) {
        if (isO(p1)) return p2;
        if (isO(p2)) return p1;
        BigInteger L, x3, y3;
        if (p1.getX().compareTo(p2.getX()) == 0) {
            if (p1.getY().compareTo(p2.getY()) == 0) {
                L = BigInteger.valueOf(3).multiply(p1.getX().pow(2)).add(a).multiply(p1.getY().multiply(BigInteger.valueOf(2)).modInverse(p));
                x3 = L.pow(2).subtract(p1.getX().multiply(BigInteger.valueOf(2))).mod(p);
                y3 = L.multiply(p1.getX().subtract(x3)).subtract(p1.getY()).mod(p);
                return new ECPoint(x3, y3);
            } else {
                return new ECPoint();
            }
        } else {
            L = p2.getY().subtract(p1.getY()).multiply(p2.getX().subtract(p1.getX()).modInverse(p));
            x3 = L.pow(2).subtract(p1.getX().add(p2.getX())).mod(p);
            y3 = L.multiply(p1.getX().subtract(x3)).subtract(p1.getY()).mod(p);
            return new ECPoint(x3, y3);
        }
    }
    
    /**
     * 椭圆曲线上的倍点运算
     *
     * @param p     点
     * @param times 倍数
     * @return 倍点结果
     */
    public static ECPoint multiply(ECPoint p, BigInteger times) {
        ECPoint tmp = plus(p, new ECPoint());
        ECPoint result = new ECPoint();
        do {
            if (times.and(BigInteger.ONE).intValue() == 1) {
                result = plus(result, tmp);
            }
            tmp = plus(tmp, tmp);
            times = times.shiftRight(1);
        } while (times.compareTo(BigInteger.ZERO) != 0);
        return result;
    }
    
    /**
     * 椭圆曲线上的取反运算
     *
     * @param p 点
     * @return 点的取反结果
     */
    private static ECPoint negative(ECPoint p) {
        return new ECPoint(p.getX(), p.getY().negate());
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
        /*
        ECPoint p1 = new ECPoint(BigInteger.valueOf(10), BigInteger.valueOf(2));
        ECPoint p2 = new ECPoint(BigInteger.valueOf(9), BigInteger.valueOf(6));
        ECPoint result = ECPoint.plus(p1, p2);
        System.out.println(result.toString(16));
        result = ECPoint.multiply(p1, BigInteger.valueOf(2));
        System.out.println(result.toString(16));
        System.out.println(ECPoint.decompress(BigInteger.valueOf(9), 1));
        */
        BigInteger gx = new BigInteger(
                "32C4AE2C" + "1F198119" + "5F990446" + "6A39C994" + "8FE30BBF" + "F2660BE1" + "715A4589" + "334C74C7", 16);
        String tmp;
        tmp = ECPoint.decompress(gx, 0).toString(16);
        System.out.println(tmp);
    }
}