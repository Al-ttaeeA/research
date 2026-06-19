package checker;

import java.util.ArrayList;
import java.util.Arrays;

public class Checker {
    public static String sequence = "3333222232222033332000032223322231000102223211133222010002033310222032221133322111010003133320333212221022210330021122133003223032231322310223103303311211001010010200102112101121022321002203311022003220132201022010331332202322020331320020322033220303310200213220323310311321003323322133221003021101022121003121102022131003101102133203221013321211031221020032033210110320032101203230310121030202120213020323101013203021023103232321213101021303103032023213232102321031313202032021320210310210210321";

    public static int n = 6;
    public static int k = 4;

    public static void main(String[] args) {
        check(sequence, k, n);
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

        // Check all possible single-digit insertions
        // System.out.println("\nChecking all possible single-digit insertions...");
        // boolean foundInsertion = false;
        // for (int pos = 0; pos <= len; pos++) {
        //     for (int digit = 0; digit < k; digit++) {
        //         String newSeq = sequence.substring(0, pos) + digit + sequence.substring(pos);
        //         if (checkComplementFree(newSeq, len + 1, n, k)) {
        //             System.out.println("Valid insertion: digit " + digit + " at position " + pos + " -> " + newSeq);
        //             foundInsertion = true;
        //         }
        //     }
        // }
        // if (!foundInsertion) {
        //     System.out.println("No valid single-digit insertion found.");
        // }
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
        return (char) (compVal + '0'); // convert back to char
    }

    public static int charToInt(char c) {
        return Character.getNumericValue(c);
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
