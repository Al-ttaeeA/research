package construction;

import checker.Checker;

public class GenerateCycles2 {
    public static int k = 8;

    public static void main(String[] args) {
        String seq = generateNestedIterative();

        Checker.extensiveCheck(seq, k, 2);
    }

    public static String generateNested() {
        String seq = generateNestedHelper(k - 1);
        System.out.println("\n");
        return seq;
    }

    private static String generateNestedHelper(int i) {
        StringBuilder sb = new StringBuilder();

        sb.append(intToChar(i));
        System.out.print(intToChar(i));

        if (i == k / 2) {
            return sb.toString();
        }

        for (int j = complement(i - 1); j <= i - 2; j++) {
            sb.append(intToChar(j)).append(intToChar(i));
            System.out.print("" + intToChar(j) + intToChar(i));
        }

        if (i % 2 == 0) {
            sb.append(intToChar(i - 1));
            System.out.print(intToChar(i - 1));
        } else {
            int compi = complement(i);
            int compi1 = compi + 1;
            int compi1Comp = complement(compi1);

            sb.append(intToChar(compi)).append(intToChar(compi1)).append(intToChar(compi1Comp));
            System.out.print("" + intToChar(compi) + intToChar(compi1) + intToChar(compi1Comp));
        }

        sb.append(generateNestedHelper(i - 1));

        sb.append(intToChar(i));
        System.out.print(intToChar(i));

        return sb.toString();
    }

    public static String generateNestedIterative() {
        StringBuilder sb = new StringBuilder();

        // Forward pass: print each level's prefix top-down (k-1 down to k/2)
        for (int i = k - 1; i >= k / 2; i--) {
            sb.append(intToChar(i));
            System.out.print(intToChar(i));

            if (i > k / 2) {
                for (int j = complement(i - 1); j <= i - 2; j++) {
                    sb.append(intToChar(j)).append(intToChar(i));
                    System.out.print("" + intToChar(j) + intToChar(i));
                }

                if (i % 2 == 0) {
                    sb.append(intToChar(i - 1));
                    System.out.print(intToChar(i - 1));
                } else {
                    int compi = complement(i);
                    int compi1 = compi + 1;
                    int compi1Comp = complement(compi1);
                    sb.append(intToChar(compi)).append(intToChar(compi1)).append(intToChar(compi1Comp));
                    System.out.print("" + intToChar(compi) + intToChar(compi1) + intToChar(compi1Comp));
                }
            }
        }

        // Reverse pass: print each level's deferred suffix bottom-up (k/2+1 up to k-1)
        for (int i = k / 2 + 1; i <= k - 1; i++) {
            sb.append(intToChar(i));
            System.out.print(intToChar(i));
        }

        System.out.println("\n");
        return sb.toString();
    }

    public static void generate(int k) {
        for(int i = k-1; i >= k/2; i--) {
            System.out.print("\n" + intToChar(i));

            for(int j = complement(i-1); j <= i-2; j++) {
                System.out.print("" + intToChar(j) + "" + intToChar(i));
            }

            if(i % 2 == 0 && i != k/2) {
                System.out.print("" + intToChar(i-1) + "" + intToChar(i));
            }
            else if(i % 2 == 1 && (k/2 % 2 == 0 || i != k/2)) {
                int compi = complement(i);
                int compi1 = compi + 1;
                int compi1Comp = complement(compi1);

                System.out.print("" + intToChar(compi) + "" + intToChar(compi1) + "" + intToChar(compi1Comp) + "" + intToChar(i));
            }
        }
    }

    public static int complement(int digit) {
        return k - 1 - digit;
    }

    public static char intToChar(int digit) {
        if(digit < 10) {
            return (char) ('0' + digit);
        }
        else {
            return (char) ('A' + (digit - 10));
        }
    }
}
