package construction;

import checker.Checker;

public class ConstructEven2 {
    public static int n = 2;
    public static int k = 8;

    public static int MAX_SEQ_LEN = 1000000; // maximum length of the sequence to construct

    public static void main(String[] args) {
        // loop(30);

        // Just FKM construction
        // int[] arr = new int[n + 1];
        // StringBuilder sb = new StringBuilder();
        // fkm(1, 1, 0, n, k, arr, sb);
        // System.out.println("Constructed sequence: " + sb.toString());

        String seq = constructSequence(n, k);
        System.out.println("Constructed sequence: " + seq);
    }

    public static void loop(int maxK) {
        for(int k = 4; k <= maxK; k += 2) {
            long expectedLen = ((long) Math.pow(k, n) - (long) Math.pow(2, n)) / 2;
            if (expectedLen > MAX_SEQ_LEN) {
                System.out.printf("Skipping n=%d, k=%d: sequence too long%n", n, k);
                continue;
            }

            System.out.println("Constructing sequence for n=" + n + ", k=" + k);

            String cas = constructSequence(n, k);

            Checker.extensiveCheck(cas, k, n);
        }
    }

    // Constructs a complement-free sequence using FKM with weight restriction w > n*(k-1)/2.
    // Enumerates all necklaces of length n over {0,...,k-1} via the FKM algorithm and
    // outputs each Lyndon word (primitive root) whose weight sum strictly exceeds n*(k-1)/2.
    // The weight condition is checked as 2*weightSum > n*(k-1) to avoid floating-point.
    // 2-grams with weight w > n*(k-1)/2 have complements with weight n*(k-1)-w < n*(k-1)/2,
    // so no n-gram and its complement both appear, guaranteeing complement-freedom.
    public static String constructSequence(int n, int k) {
        StringBuilder sb = new StringBuilder();
        int[] arr = new int[n + 1]; // 1-indexed; arr[0] = 0 acts as the necklace sentinel
        fkm(1, 1, 0, n, k, arr, sb);

        sb.append(sb.charAt(0)); // wrap around to make it cyclic

        for (int i = 0; i < k / 4; i++) {
            int  digit     = i * 2;
            char cDigit    = toChar(digit);
            char cComp     = toChar(k - 1 - digit);   // complement of digit
            char cNext     = toChar(digit + 1);        // digit + 1
            char cCompNext = toChar(k - 2 - digit);    // complement of digit + 1

            // Step 1: find [complement(digit), digit+1] and insert digit between them
            //System.out.println("Attempting to insert " + cComp + "" + cNext + " into sequence: " + sb);
            for (int j = 0, len = sb.length() - 1; j < len; j++) {
                if (sb.charAt(j) == cComp && sb.charAt(j + 1) == cNext) {
                    sb.insert(j + 1, cDigit);
                    break;
                }
            }

            // Step 2: find [complement(digit), complement(digit+1)] and insert digit+1 between them
            //System.out.println("Attempting to insert complement " + cComp + "" + cCompNext + " into sequence: " + sb);
            for (int j = 0, len = sb.length() - 1; j < len; j++) {
                if (sb.charAt(j) == cComp && sb.charAt(j + 1) == cCompNext) {
                    sb.insert(j + 1, cNext);
                    break;
                }
            }
        }

        sb.deleteCharAt(sb.length() - 1); // remove wrap-around to make it non-cyclic
        return sb.toString();
    }

    private static char toChar(int v) {
        return v < 10 ? (char) ('0' + v) : (char) ('A' + v - 10);
    }

    // Recursive FKM necklace generator with weight-restriction pruning.
    // t       : current position (1-indexed)
    // p       : length of the current shortest necklace period candidate
    // weightSum: sum of arr[1..t-1]
    private static void fkm(int t, int p, int weightSum, int n, int k, int[] arr, StringBuilder sb) {
        // Prune: even assigning the max symbol (k-1) to all remaining positions cannot
        // push the total weight strictly above the threshold n*(k-1)/2.
        if (2 * (weightSum + (n - t + 1) * (k - 1)) <= n * (k - 1)) return;
        if (t > n) {
            // Leaf: emit the Lyndon word arr[1..p] only when p divides n (valid necklace period).
            // The weight check is subsumed by the pruning above (remaining positions = 0 there).
            if (n % p == 0) {
                for (int i = 1; i <= p; i++) sb.append(toChar(arr[i]));
            }
            return;
        }
        // Enumerate symbols j >= arr[t-p] to maintain the necklace lex-order invariant.
        for (int j = arr[t - p]; j < k; j++) {
            arr[t] = j;
            fkm(t + 1, j == arr[t - p] ? p : t, weightSum + j, n, k, arr, sb);
        }
    }

    private static String complementString(String s, int k) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            int digit = Character.isDigit(c) ? c - '0' : c - 'A' + 10;
            int complementDigit = k - 1 - digit;
            sb.append(complementDigit < 10 ? (char) ('0' + complementDigit) : (char) ('A' + complementDigit - 10));
        }
        return sb.toString();
    }
}
