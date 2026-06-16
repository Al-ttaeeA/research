package construction;

import checker.Checker;
import java.util.ArrayList;
import java.util.Scanner;

public class Construction {
    static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {
        System.out.println("Enter k and n:");
        int k = scanner.nextInt();
        int n = scanner.nextInt();

        String sequence;

        if(k % 2 == 0) {
            if(n % 2 ==0) {
                System.out.println("Even k, Even n");
                sequence = evenEven(k, n);
            }
            else{
                System.out.println("Even k, Odd n");
                sequence = evenOdd(k, n);
            }
        }
        else{
            if(n % 2 ==0) {
                System.out.println("Odd k, Even n");
                sequence = oddEven(k, n);
            }
            else{
                System.out.println("Odd k, Odd n");
                sequence = oddOdd(k, n);
            }
        }

        System.out.println(sequence);

        Checker.check(sequence, k, n);
    }

    public static String evenOdd(int k, int n) {
        int weight = n * (k-1) / 2;

        ArrayList<String> necklaces = generateNecklacesLex(k, n);

        StringBuilder sb = new StringBuilder();
        for (String necklace : necklaces) {
            int sum = 0;
            for (char c : necklace.toCharArray()) {
                sum += c - '0';
            }
            if (sum > weight) {
                sb.append(leastPeriod(necklace));
            }
        }
        return sb.toString();
    }

    public static ArrayList<String> generateNecklacesLex(int k, int n) {
        ArrayList<String> necklaces = new ArrayList<>();
        generateNecklacesHelper("", k, n, necklaces);
        return necklaces;
    }

    private static void generateNecklacesHelper(String current, int k, int n, ArrayList<String> necklaces) {
        if (current.length() == n) {
            if (isNecklace(current)) {  
                necklaces.add(current);
            }
            return;
        }
        for (int c = 0; c < k; c++) {
            generateNecklacesHelper(current + (char) ('0' + c), k, n, necklaces);
        }
    }

    private static boolean isNecklace(String s) {
        for (int i = 1; i < s.length(); i++) {
            String rotation = s.substring(i) + s.substring(0, i);
            if (rotation.compareTo(s) < 0) {
                return false;
            }
        }
        return true;
    }

    // Returns the least period of a string, or the string itself if it is not periodic
    private static String leastPeriod(String s) {
        int n = s.length();
        for (int p = 1; p <= n / 2; p++) {
            if (n % p == 0) {
                String period = s.substring(0, p);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < n / p; i++) {
                    sb.append(period);
                }
                if (sb.toString().equals(s)) {
                    return period;
                }
            }
        }
        return s;
    }

    public static String evenEven(int k, int n) {
        return null;
    }

    public static String oddEven(int k, int n) {
        return null;
    }

    public static String oddOdd(int k, int n) {
        return null;
    }
}