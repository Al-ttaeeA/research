package scripts;

import checker.Checker;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import lempels.Lempels;

public class CheckNegativeToComplement {

    static final String NAS_EXE =
            "C:\\Users\\Admin\\research\\C\\negative free\\NAS.exe";
    static final int MAX_SEQ_LEN = 5_000_000;

    public static void main(String[] args) throws Exception {
        System.out.printf("%-4s  %-4s  %-14s  %-10s  %-6s  %-12s  %s%n",
                "k", "n", "|NAS(n-1,k)|", "punctured?", "cycle", "cycle len", "CF?");
        System.out.println("-".repeat(72));

        for (int k = 4; k <= 14; k += 2) {      // even k: 4, 6, 8
            for (int n = 4; n <= 8; n += 2) {   // even n: 4, 6, 8
                runPair(k, n);
            }
            System.out.println();
        }
    }

    private static void runPair(int k, int n) {
        int nasOrder = n - 1;

        if(Math.pow(k, nasOrder)/2 > MAX_SEQ_LEN) {
            System.out.printf("%-4d  %-4d  %-14d  %-10s  %-6s  (skipped – too large)%n",
                    k, n, (int)Math.pow(k, nasOrder), "-", "-");
            return;
        }

        // ---- Step 1: generate NAS at order n-1 ----
        String seq;
        try {
            seq = runNAS(nasOrder, k);
        } catch (Exception e) {
            System.out.printf("%-4d  %-4d  ERROR running NAS: %s%n", k, n, e.getMessage());
            return;
        }

        if (seq.isEmpty()) {
            System.out.printf("%-4d  %-4d  (empty NAS)%n", k, n);
            return;
        }

        int originalLen = seq.length();

        // ---- Step 2: Lempel's lift; retry with last digit deleted if weight ≠ 0 ----
        ArrayList<String> cycles = Lempels.lempelLift(seq, nasOrder, k, 1, true, false);
        boolean punctured = false;

        //if (!cycles.isEmpty() && cycles.get(0).equals(Lempels.WEIGHT_ERROR)) {
            seq = seq.substring(0, seq.length() - 1);   // delete last digit
            punctured = true;
            cycles = Lempels.lempelLift(seq, nasOrder, k, 1, false, true);
        //}

        if (seq.length() > MAX_SEQ_LEN) {
            System.out.printf("%-4d  %-4d  %-14d  %-10s  (skipped – too large)%n",
                    k, n, originalLen, punctured ? "yes" : "no");
            return;
        }

        // ---- Step 3: check each cycle for complement-free at window n ----
        for (int i = 0; i < cycles.size(); i++) {
            String cyc = cycles.get(i);
            String result = checkCF(cyc, n, k);
            System.out.printf("%-4d  %-4d  %-14d  %-10s  %-6d  %-12d  %s%n",
                    k, n, originalLen,
                    punctured ? "yes" : "no",
                    i, cyc.length(), result);
        }
    }

    /**
     * Run NAS.exe with the given order and alphabet size; return the output sequence
     * as a string of digit characters (info lines are stripped).
     */
    private static String runNAS(int n, int k) throws IOException, InterruptedException {
        Process proc = new ProcessBuilder(NAS_EXE)
                .redirectErrorStream(true)
                .start();
        proc.getOutputStream().write((n + " " + k + "\n").getBytes());
        proc.getOutputStream().close();

        StringBuilder seq = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Length") || line.contains("Expected")) continue;
                // Strip "Enter n k: " prompt prefix — the sequence may start on the same line
                if (line.startsWith("Enter")) {
                    int colon = line.indexOf(": ");
                    line = (colon >= 0) ? line.substring(colon + 2) : "";
                }
                for (char c : line.toCharArray()) {
                    if (Character.isDigit(c) || (c >= 'A' && c <= 'Z')) seq.append(c);
                }
            }
        }
        proc.waitFor();
        return seq.toString();
    }

    /**
     * Efficient complement-free check using a HashMap (O(L·n) vs ArrayList O(L²)).
     * Returns "YES" or a short description of the first violation.
     */
    static String checkCF(String seq, int window, int k) {
        int L = seq.length();
        if (L < window) return "NO  (sequence too short: len=" + L + ")";

        String ext = seq + seq.substring(0, window - 1);
        HashMap<String, Integer> seen = new HashMap<>();
        for (int i = 0; i < L; i++) {
            String gram = ext.substring(i, i + window);
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
