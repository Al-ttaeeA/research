package checker;

import java.util.ArrayList;
import java.util.Arrays;

public class Checker {
    public static String sequence = "71727374757016626364655352344";

    public static int mainN = 2;
    public static int mainK = 8;

    public static void main(String[] args) {
        extensiveCheck(sequence, mainK, mainN);
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
        for (int digit = 0; digit < k; digit++) {
            for (int pos = 0; pos <= len; pos++) {
                String newSeq = sequence.substring(0, pos) + digit + sequence.substring(pos);
                if (checkComplementFree(newSeq, len + 1, n, k)) {
                    System.out.println("Can insert " + digit + " at position " + pos + " to get complement-free sequence: " + newSeq);
                }
            }
        }
        
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
        else{
            if(n == 2 && k % 2 == 0) {
                System.out.println("Sequence is maximal length since n=2 and k is even. Here's the missing pair:");
            }
            else{
                System.out.println("Sequence length " + len + " is less than maximum " + maxLength + ". Missing complement pairs:");
            }
        }

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

    /**
     * Returns the list of fully-missing complement pairs (neither string nor its
     * complement appears in {@code sequence} as an n-gram).
     * Returns {@code null} if the sequence is NOT complement-free (violation found).
     * Returns an empty list if the sequence is CF and already at maximum length.
     */
    public static ArrayList<String[]> getMissingPairs(String sequence, int k, int n) {
        int len = sequence.length();
        if (len < n) return null;
        String extended = sequence + sequence.substring(0, n - 1);

        int total = (int) Math.pow(k, n);
        int[] seen = new int[total];
        Arrays.fill(seen, -1);

        for (int i = 0; i < len; i++) {
            String s = extended.substring(i, i + n);
            int idx = stringToIndex(s, k);
            int compIdx = total - 1 - idx;

            if (seen[idx] != -1) return null;       // duplicate — not CF
            if (seen[compIdx] != -1) return null;   // complement present — not CF
            seen[idx] = i;
        }

        // CF: collect pairs where both string and complement are absent
        ArrayList<String[]> missing = new ArrayList<>();
        boolean[] recorded = new boolean[total];
        for (int idx = 0; idx < total; idx++) {
            int compIdx = total - 1 - idx;
            if (idx == compIdx) continue;           // self-complementary (odd k only)
            if (recorded[idx] || recorded[compIdx]) continue;
            if (seen[idx] == -1 && seen[compIdx] == -1) {
                missing.add(new String[]{indexToString(idx, k, n), indexToString(compIdx, k, n)});
                recorded[idx] = true;
                recorded[compIdx] = true;
            }
        }
        return missing;
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
