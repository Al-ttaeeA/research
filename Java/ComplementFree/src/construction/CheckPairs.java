package construction;

import checker.Checker;
import java.util.HashMap;

public class CheckPairs {

    /** Sequences longer than this are skipped (too slow / too much memory). */
    static final int MAX_SEQ_LEN = 5_000_000;

    public static void main(String[] args) {
        System.out.printf("%-4s  %-4s  %-16s  %-12s  %s%n",
                "k", "n", "case", "length", "complement-free?");
        System.out.println("-".repeat(58));

        for (int k = 2; k <= 14; k++) {
            for (int n = 2; n <= 9; n++) {
                if (Math.pow(k, n)/2 > MAX_SEQ_LEN) {
                    System.out.printf("%-4d  %-4d  %-16s  %-12s  (skipped – too large)%n",
                            k, n, "", "N/A");
                    continue;
                }
                
                String caseLabel;
                String seq;
                try {
                    if (k % 2 == 0 && n % 2 == 0) {
                        caseLabel = "even k / even n";
                        seq = Construction.evenEven(k, n);
                    } else if (k % 2 == 0) {
                        caseLabel = "even k / odd n";
                        seq = Construction.evenOdd(k, n);
                    } else if (n % 2 == 0) {
                        caseLabel = "odd k / even n";
                        seq = Construction.oddEven(k, n);
                    } else {
                        caseLabel = "odd k / odd n";
                        seq = Construction.oddOdd(k, n);
                    }
                } catch (Exception e) {
                    System.out.printf("%-4d  %-4d  %-16s  %-12s  ERROR: %s%n",
                            k, n, "", "-", e.getMessage());
                    continue;
                }

                // evenEven returns "EMPTY-STRING" for n < 4 (no construction defined there)
                int len = (seq == null || seq.equals("EMPTY-STRING")) ? 0 : seq.length();
                if (len == 0) {
                    System.out.printf("%-4d  %-4d  %-16s  %-12d  (empty)%n",
                            k, n, caseLabel, len);
                    continue;
                }

                String result = checkCF(seq, n, k);
                System.out.printf("%-4d  %-4d  %-16s  %-12d  %s%n",
                        k, n, caseLabel, len, result);
            }
        }
    }

    /**
     * Efficient complement-free check using a HashMap.
     * Returns "YES" if the sequence is complement-free, otherwise a short
     * description of the first violation found.
     */
    static String checkCF(String seq, int n, int k) {
        int L = seq.length();
        String ext = seq + seq.substring(0, n - 1);   // cyclic wrap-around
        HashMap<String, Integer> seen = new HashMap<>();
        for (int i = 0; i < L; i++) {
            String gram = ext.substring(i, i + n);
            String comp = Checker.complementString(gram, k);
            if (seen.containsKey(gram))
                return "NO  (duplicate '" + gram + "' at " + seen.get(gram) + " and " + i + ")";
            if (seen.containsKey(comp))
                return "NO  (complement pair '" + gram + "'/'" + comp + "')";
            seen.put(gram, i);
        }
        return "YES";
    }
}
