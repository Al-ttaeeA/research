package checker;

import java.util.ArrayList;
import java.util.Arrays;

public class Checker {
    public static String sequence = "003303310031103021210203103332232203320032310103132032221121132213321203032021321110010021102210132321310210";

    public static int n = 4;
    public static int k = 4;

    public static void main(String[] args) {
        extensiveCheck(sequence, k, n);
    }

    public static void check(String sequence, int k, int n) {
        int len = sequence.length();
        String extended = sequence + sequence.substring(0, n - 1); // append first n-1 chars to end for wraparound

        int total = (int) Math.pow(k, n);
        int[] seen = new int[total]; // seen[idx] = position where string was first seen, or -1
        Arrays.fill(seen, -1);

        for (int i = 0; i < len; i++) {
            String s = extended.substring(i, i + n);
            int idx = stringToIndex(s, k);
            int compIdx = total - 1 - idx; // complement index: each digit d -> k-1-d flips idx to (total-1-idx)

            if (seen[idx] != -1) {
                System.out.println("Duplicate string: " + s + " at positions " + i + " and " + seen[idx]);
                return;
            }

            if (seen[compIdx] != -1) {
                System.out.println("Not complement-free: " + s + " and " + complementString(s, k) + " at positions " + i + " and " + seen[compIdx]);
                return;
            }

            seen[idx] = i;
        }

        System.out.println("Sequence is complement-free.");

        // Check all possible single-digit insertions and add them, then check again and loop 
        
        
    }

    public static void extensiveCheck(String sequence, int k, int n) {
        int len = sequence.length();
        String extended = sequence + sequence.substring(0, n - 1);

        int total = (int) Math.pow(k, n);
        int[] seen = new int[total];
        Arrays.fill(seen, -1);

        for (int i = 0; i < len; i++) {
            String s = extended.substring(i, i + n);
            int idx = stringToIndex(s, k);
            int compIdx = total - 1 - idx;

            if (seen[idx] != -1) {
                System.out.println("Duplicate string: " + s + " at positions " + i + " and " + seen[idx]);
                return;
            }

            if (seen[compIdx] != -1) {
                System.out.println("Not complement-free: " + s + " and " + complementString(s, k) + " at positions " + i + " and " + seen[compIdx]);
                return;
            }

            seen[idx] = i;
        }

        System.out.println("Sequence is complement-free.");

        int maxLength = (k % 2 == 1) ? (total - 1) / 2 : total / 2;
        if (len == maxLength) {
            System.out.println("Sequence has maximum length (" + maxLength + ").");
            return;
        }

        System.out.println("Sequence length " + len + " is less than maximum " + maxLength + ". Missing complement pairs:");

        boolean[] printed = new boolean[total];
        for (int idx = 0; idx < total; idx++) {
            int compIdx = total - 1 - idx;
            if (idx == compIdx) continue;
            if (printed[idx] || printed[compIdx]) continue;
            if (seen[idx] == -1 && seen[compIdx] == -1) {
                System.out.println(indexToString(idx, k, n) + " / " + indexToString(compIdx, k, n));
                printed[idx] = true;
                printed[compIdx] = true;
            }
        }
    }

    public static String indexToString(int idx, int k, int n) {
        char[] result = new char[n];
        for (int i = n - 1; i >= 0; i--) {
            result[i] = intToChar(idx % k);
            idx /= k;
        }
        return new String(result);
    }

    public static int stringToIndex(String s, int k) {
        int idx = 0;
        for (char c : s.toCharArray()) {
            idx = idx * k + charToInt(c);
        }
        return idx;
    }

    public static String complementString(String s, int k) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(complement(c, k));
        }
        return sb.toString();
    }

    public static char complement(char c, int k) {
        int val = charToInt(c);
        int compVal = k - 1 - val; // complement in Z_k
        return intToChar(compVal);
    }

    public static int charToInt(char c) {
        return Character.getNumericValue(c);
    }

    public static char intToChar(int val) {
        if (val < 10) return (char) ('0' + val);
        return (char) ('A' + val - 10);
    }

    public static boolean checkComplementFree(String seq, int len, int n, int k) {
        String extended = seq + seq.substring(0, n - 1);
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            String s = extended.substring(i, i + n);
            if (strings.contains(s) || strings.contains(complementString(s, k))) {
                return false;
            }
            strings.add(s);
        }
        return true;
    }

    public static int modK(int num, int k) {
        int value = num % k;
        if (value < 0) {
            value = value + k;
        }
        return value;
    }
}
