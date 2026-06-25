package scripts;

import checker.Checker;
import construction.Construction;
import java.util.ArrayList;
import lempels.Lempels;

public class CheckComplementToComplement {
    static final int MAX_SEQ_LEN = 500_000;

    public static void main(String[] args) {
        // Iterate over all (n, k) pairs where n is odd and k is even
        int[] oddNs  = {3, 5, 7, 9};
        int[] evenKs = {2, 4, 6, 8};

        for (int n : oddNs) {
            for (int k : evenKs) {
                runPair(n, k);
            }
        }
    }

    private static void runPair(int n, int k) {
        System.out.println("=== n=" + n + ", k=" + k + " ===");

        // Step 1: Construct a complement-free sequence (even k, odd n branch)
        String sequence;
        try {
            sequence = Construction.evenOdd(k, n);
        } catch (Exception e) {
            System.out.println("  Construction failed: " + e.getMessage());
            System.out.println();
            return;
        }

        if (sequence == null || sequence.isEmpty()) {
            System.out.println("  Construction returned an empty sequence, skipping.");
            System.out.println();
            return;
        }

        if (sequence.length() > MAX_SEQ_LEN) {
            System.out.println("  Sequence too long (" + sequence.length() + " symbols), skipping.");
            System.out.println();
            return;
        }

        System.out.println("  Constructed sequence length: " + sequence.length());

        // Step 2: Apply Lempel's lift to produce k distinct sequences
        ArrayList<String> lifted;
        try {
            lifted = Lempels.lempelLift(sequence, n, k, 1, false, false);
        } catch (Exception e) {
            System.out.println("  Lempel's lift failed: " + e.getMessage());
            System.out.println();
            return;
        }

        System.out.println("  Lempel's lift produced " + lifted.size() + " sequences.");

        // Step 3: Check each lifted sequence for complement-freeness using (n+1)-length windows
        int windowSize = n + 1;
        int complementFreeCount = 0;
        for (int i = 0; i < lifted.size(); i++) {
            String liftedSeq = lifted.get(i);
            boolean isCF = Checker.checkComplementFree(liftedSeq, liftedSeq.length(), windowSize, k);
            System.out.printf("    C_%d (length %d, window=%d): %s%n",
                    i, liftedSeq.length(), windowSize,
                    isCF ? "complement-free" : "NOT complement-free");
            if (isCF) complementFreeCount++;
        }

        System.out.println("  Result: " + complementFreeCount + "/" + lifted.size()
                + " lifted sequences are complement-free with (n+1)=" + windowSize + "-length windows.");
        System.out.println();
    }
}
