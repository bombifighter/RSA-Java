import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;

public class Rsa {

    public static BigInteger[] EEA(BigInteger a, BigInteger b) {
        int N = 1;
        BigInteger c;
        BigInteger[] q = new BigInteger[100];
        while(!b.equals(BigInteger.ZERO)) {
            q[N] = a.divide(b);
            N++;
            c = a;
            a = b;
            b = c.remainder(b);
        }
        N--;
        BigInteger gcd = a;
        BigInteger x2 = BigInteger.ONE;
        BigInteger x1 = BigInteger.ZERO;
        BigInteger y2 = BigInteger.ZERO;
        BigInteger y1 = BigInteger.ONE;
        BigInteger x,y;

        for(int i = 1; i<N;i++) {
            x = (x1.multiply(q[i])).add(x2);
            y = (y1.multiply(q[i])).add(y2);
            x2 = x1;
            y2 = y1;
            x1 = x;
            y1 = y;
        }
        x = (BigInteger.valueOf(-1).pow(N)).multiply(x1);
        y = (BigInteger.valueOf(-1).pow(N+1)).multiply(y1);
        BigInteger[] result = new BigInteger[3];
        result[0] = gcd;
        result[1] = x;
        result[2] = y;

        return result;
    }

    public static BigInteger[] binaryFME(BigInteger base, BigInteger e, BigInteger m) {
        String pow = e.toString(2);
        BigInteger[] powmods = new BigInteger[pow.length()];
        powmods[pow.length()-1] = base.mod(m);
        int i = pow.length()-2;
        while(i>=0) {
            powmods[i] = powmods[i+1].pow(2).mod(m);
            i--;
        }
        return powmods;
    }

    public static BigInteger generalFME(BigInteger base, BigInteger e, BigInteger m) {
        String binarypow = e.toString(2);
        BigInteger numberbag = BigInteger.ONE;
        BigInteger[] help = binaryFME(base, e, m);
        for(int i=0;i<binarypow.length();i++) {
            if(binarypow.charAt(i) == '1') {
                numberbag = numberbag.multiply(help[i]);
            }
        }
        return numberbag.mod(m);
    }

    public static boolean MRTest(BigInteger n) {
        BigInteger d = n.subtract(BigInteger.ONE);
        int S = 0;
        while(d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            S++;
            d = d.divide(BigInteger.TWO);
        }

        int random = new Random().nextInt(100);
        BigInteger a = BigInteger.valueOf(random);

        if(a.modPow(d,n).equals(BigInteger.ONE)) {
            return false;
        }

        for(int r=0;r<S;r++) {
            if(a.modPow(d.multiply(BigInteger.TWO.pow(r)),n).equals(BigInteger.valueOf(-1).add(n))) {
                return false;
            }
        }
        return true;
    }

    public static BigInteger generatePrime () {
        boolean isComp = false;

        BigInteger p = BigInteger.ZERO;
        while(!isComp) {
            Random random = new Random();
            BigInteger testNum = new BigInteger(50,random);
            for(int i=0;i<3;i++) {
                if(Rsa.MRTest(testNum)) {
                    isComp=true;
                }
            }
            if(!isComp) {
                p=testNum;
                break;
            } else {
                isComp=false;
            }
        }
        return p;
    }

    public static BigInteger[] generateKeys() {
        BigInteger[] result = new BigInteger[3];
        BigInteger p = generatePrime();
        BigInteger q = generatePrime();
        while(p.equals(q)) {
            q = generatePrime();
        }
        BigInteger n = p.multiply(q);
        BigInteger fin = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.ZERO;
        BigInteger[] help;
        for(BigInteger i = BigInteger.valueOf(3);i.compareTo(fin)<1;i = i.add(BigInteger.TWO)) {
            help = EEA(i,fin);
            if (help[0].equals(BigInteger.ONE)) {
                e=i;
                break;
            }
        }
        BigInteger d;
        help = EEA(e,fin);
        if(help[1].compareTo(BigInteger.ZERO)==1) {
            d = help[1];
        } else {
            d = help[1].add(fin);
        }
        result[0]=n;
        result[1]=e;
        result[2]=d;
        return result;
    }

    public static BigInteger encrypt(BigInteger[] keys, BigInteger m) {
        return generalFME(m,keys[1],keys[0]);
    }

    public static BigInteger decrypt(BigInteger[] keys, BigInteger c) {
        return generalFME(c, keys[2], keys[0]);
    }

    public static void main(String[] args) {

        System.out.println("----- RSA -----");
        BigInteger[] keys = generateKeys();
        BigInteger n = keys[0];
        System.out.print("Add meg az üzenetet: ");
        Scanner in = new Scanner(System.in);
        BigInteger m = in.nextBigInteger();
        while(m.compareTo(n)>0) {
            System.out.println("A megadott üzenet mérete túl nagy!");
            System.out.print("Új üzenet: ");
            m = in.nextBigInteger();
        }
        System.out.println("Válaszd ki a kívánt eljárást! e - titkosítás vagy d - visszafejtés");
        String method = in.next();
        if(method.equals("e")) {
            BigInteger c = encrypt(keys,m);
            System.out.println("A titkosított üzenet: " + c);
            BigInteger ujm = decrypt(keys, c);
            System.out.println("Az eredeti üzenet: " + ujm);
        } else {
            BigInteger ujm2 = decrypt(keys,m);
            System.out.println("A visszafejtett üzenet: " + ujm2);
            BigInteger ujc = encrypt(keys, ujm2);
            System.out.println("Az eredeti titkosított üzenet: " + ujc);
        }
    }
}