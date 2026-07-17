package construction;

import checker.Checker;
import java.util.ArrayList;

public class ConstructEvenEven {
    public static int n = 4;
    public static int k = 14;

    public static int MAX_SEQ_LEN = 5_000_000;

    // -------------------------------------------------------------------------
    // Static state mirroring NAS.c globals
    // -------------------------------------------------------------------------
    private static int[] a = new int[100];   // current necklace tuple (1-indexed)
    private static int lempel;               // running prefix sum for Lempel lift
    private static int ITERATION;            // which of the k lift iterations we are in
    private static int ITERS_TIL_PUNCTURE;  // period of the all-(k-1) cycle
    private static int n_inner;             // working order = n_orig - 1 (odd)
    private static int k_inner;             // alphabet size
    private static StringBuilder output;   // accumulated output

    public static void main(String[] args) {
        //System.out.println(constructEvenEven(n, k));
        loop();
    }

    public static void loop() {
        // Loop through values k = 4, 6, 8, ..., 14 and n = 4, 6, 8
        for (int k = 4; k <= 14; k += 2) {
            for (int n = 4; n <= 8; n += 2) {
                if (Math.pow(k, n)/2 > MAX_SEQ_LEN) {
                    System.out.printf("Skipping n=%d, k=%d: sequence too long%n", n, k);
                    continue;
                }

                System.out.println("Constructing sequence for n=" + n + ", k=" + k);

                String cas = constructEvenEven(n, k);
                
                // Check if the constructed sequence is complement-free
                Checker.extensiveCheck(cas, k, n);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Main construction entry point
    // -------------------------------------------------------------------------

    /**
     * Generate a NAS of order n over Z_k using Lempel's lift of the order-(n-1) NAS.
     * Mirrors the EVEN CASE block of NAS.c main(), but omits the FKM2 binary-cycle
     * additions so that only the lifted order-(n-1) NAS contributes to the output.
     *
     * @param n_param even order >= 4
     * @param k_param even alphabet size >= 2
     * @return NAS string, or "EMPTY-STRING" for invalid input
     */
    public static String constructEvenEven(int n_param, int k_param) {
        if (k_param % 2 != 0 || n_param % 2 != 0 || n_param < 4) {
            return "EMPTY-STRING";
        }

        // Initialise state (mirrors: n = n_orig-1; a[0] = lempel = 0; in NAS.c)
        output  = new StringBuilder();
        k_inner = k_param;
        n_inner = n_param - 1;   // odd underlying order
        a[0]    = lempel = 0;

        for (ITERATION = 1; ITERATION <= k_inner; ITERATION++) {
            // Binary-cycle insertions (FKM2) are intentionally omitted here.
            // Only lift the order-(n-1) NAS via Lempel.
            fkm(1, 1, 0);
        }

        // Rejoin the punctured all-(k-1) cycle (mirrors: for i=k/ITERS-1; i>=0; i--)
        for (int i = k_inner / ITERS_TIL_PUNCTURE - 1; i >= 0; i--) printSym(i);

        //System.out.println(output.toString());

        // Now we need to add the extra cycles
        for (int i = 0; i < k_param/4; i++) {
            String dbseq = genericFKM(n_param, 2);

            String translated = binaryTranslate(dbseq, i, i + k_param/2);

            //System.out.println("Adding cycle \n" + translated + " to output, original dbseq = \n" + dbseq);

            String newSequence = insertSequence(output.toString(), translated, n_param);

            output = new StringBuilder(newSequence);
        }

        // If k/2 is odd, add a complement free DB sequence of order n 
        if((k_param / 2) % 2 == 1) {
            String dbseq = genericFKM(n_param - 1, 2);

            String lifted = lempelLift(dbseq, n_param - 1, 2, 1).get(0);

            String translated = binaryTranslate(lifted, k_param/4, k_param/4 + k_param/2);
            
            //System.out.println("Adding cycle \n" + translated + " to output, original dbseq = \n" + lifted);

            String newSequence = insertSequence(output.toString(), translated, n_param);

            output = new StringBuilder(newSequence);
        }

        return output.toString();
    }

    // -------------------------------------------------------------------------
    // Output helper — mirrors PrintSym in NAS.c
    // -------------------------------------------------------------------------

    private static void printSym(int x) {
        if (x < 10) output.append((char) ('0' + x));
        else        output.append((char) ('A' + (x - 10)));
    }

    // -------------------------------------------------------------------------
    // GCD — mirrors GCD in NAS.c
    // -------------------------------------------------------------------------

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    // -------------------------------------------------------------------------
    // Necklace canonicalisation — mirrors GetNecklace in NAS.c
    // -------------------------------------------------------------------------

    private static void getNecklace(int[] b, int[] neck) {
        for (int j = 1; j <= n_inner; j++) neck[j] = neck[n_inner + j] = b[j];
        int j = 1, t = 1, p = 1;
        do {
            t = t + p * ((j - t) / p);
            j = t + 1;
            p = 1;
            while (j <= 2 * n_inner && neck[j - p] <= neck[j]) {
                if (neck[j - p] < neck[j]) p = j - t + 1;
                j++;
            }
        } while (p * ((j - t) / p) < n_inner);
        for (j = 1; j <= n_inner; j++) neck[j] = neck[j + t - 1];
    }

    // -------------------------------------------------------------------------
    // Complement test — mirrors SmallerThanNeg in NAS.c
    // Returns true iff a[1..n_inner] is lex-smaller than the necklace rep of its
    // negative, selecting exactly one from each {necklace, negative} pair.
    // -------------------------------------------------------------------------

    private static boolean smallerThanNeg() {
        int[] b    = new int[100];
        int[] neck = new int[200];
        for (int i = 1; i <= n_inner; i++) b[i] = (k_inner - a[i]) % k_inner;
        getNecklace(b, neck);
        for (int i = 1; i <= n_inner; i++) {
            if (a[i] < neck[i]) return true;
            if (a[i] > neck[i]) return false;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Lempel lift printer — mirrors Print(p) even-case branch in NAS.c
    // Outputs the running prefix sum mod k for each symbol in the Lyndon prefix.
    // Also handles puncturing of the all-(k-1) cycle.
    // -------------------------------------------------------------------------

    private static void print(int p) {
        for (int i = 1; i <= p; i++) {
            // Detect the constant-(k-1) necklace (period 1, value k-1)
            if (p == 1 && a[i] == k_inner - 1) {
                if (ITERATION == 1) {
                    // First encounter: compute how often this cycle must be punctured.
                    // next_symbol is the first lifted symbol this necklace would emit.
                    int next_symbol = (lempel + a[i]) % k_inner;
                    ITERS_TIL_PUNCTURE = k_inner / gcd(k_inner, next_symbol);
                }
                // Puncture: skip one occurrence every ITERS_TIL_PUNCTURE iterations
                if (ITERATION % ITERS_TIL_PUNCTURE == 0) return;
            }
            // D^-1 Lempel lift: output the running prefix sum mod k
            lempel = (lempel + a[i]) % k_inner;
            printSym(lempel);
        }
    }

    // -------------------------------------------------------------------------
    // Pseudoweight-filtered FKM — mirrors FKM(t, p, w) in NAS.c
    //
    // w = double the pseudoweight of a[1..t-1]:
    //   symbol 0   contributes k   (so half-weight = k/2)
    //   symbol j>0 contributes 2j
    //
    // A leaf necklace is emitted iff:
    //   w > k*n_inner               (strictly above the midpoint), OR
    //   w == k*n_inner AND smallerThanNeg()   (tie-break by lex order)
    // -------------------------------------------------------------------------

    private static void fkm(int t, int p, int w) {
        // Prune: max remaining contribution cannot reach required pseudoweight
        if ((n_inner - t + 1) * 2 * (k_inner - 1) + w < k_inner * n_inner) return;
        if (t > n_inner) {
            if (n_inner % p == 0 && (w > k_inner * n_inner || smallerThanNeg())) print(p);
            return;
        }
        for (int j = a[t - p]; j < k_inner; j++) {
            a[t] = j;
            int x = (j == 0) ? k_inner : 2 * j;
            if (j == a[t - p]) fkm(t + 1, p, w + x);
            else               fkm(t + 1, t, w + x);
        }
    }


    // -------------------------------------------------------------------------
    // Method to generate full DB sequence of order n over Z_k using generic FKM
    // Enumerates necklaces in lex order; concatenates each Lyndon word (least period)
    // to produce the Granddaddy de Bruijn sequence.
    // -------------------------------------------------------------------------
    private static String genericFKM(int n, int k) {
        StringBuilder sb = new StringBuilder();
        int[] arr = new int[n + 1]; // 1-indexed; arr[0] unused (stays 0, acts as sentinel)
        fkmGenerate(1, 1, n, k, arr, sb);
        return sb.toString();
    }

    // Recursive helper for genericFKM — standard FKM necklace enumeration.
    // Appends the Lyndon word a[1..p] whenever n % p == 0 (valid necklace root).
    private static void fkmGenerate(int t, int p, int n, int k, int[] arr, StringBuilder sb) {
        if (t > n) {
            if (n % p == 0) {
                for (int i = 1; i <= p; i++) {
                    int v = arr[i];
                    sb.append(v < 10 ? (char) ('0' + v) : (char) ('A' + v - 10));
                }
            }
            return;
        }
        for (int j = arr[t - p]; j < k; j++) {
            arr[t] = j;
            fkmGenerate(t + 1, j == arr[t - p] ? p : t, n, k, arr, sb);
        }
    }

    // -------------------------------------------------------------------------
    // Method to lift a DB sequence of order n-1 over Z_k to order n using Lempel's lift
    // (Alhakim et al. general k-ary Lempel lift, unpunctured case).
    // Returns a list of k disjoint cycles C_0, ..., C_{k-1} each of length k^(n-1).
    // -------------------------------------------------------------------------
    private static ArrayList<String> lempelLift(String seqStr, int n, int k, int beta) {
        int betaInv = modInverse(beta, k);
        int len = seqStr.length(); // k^(n-1)

        // Build C_0: C_0[j] = beta^{-1} * (gamma_1 + ... + gamma_{j+1}) mod k
        int[] C0 = new int[len];
        int sum = 0;
        for (int j = 0; j < len; j++) {
            sum = (sum + Character.getNumericValue(seqStr.charAt(j))) % k;
            C0[j] = (betaInv * sum) % k;
        }

        // C_i = C_0 + i (mod k), giving k vertex-disjoint cycles (Theorem 3.2(c))
        ArrayList<String> cycles = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                int v = (C0[j] + i) % k;
                sb.append(v < 10 ? (char) ('0' + v) : (char) ('A' + v - 10));
            }
            cycles.add(sb.toString());
        }
        return cycles;
    }

    // Modular multiplicative inverse via extended Euclidean algorithm
    private static int modInverse(int a, int m) {
        a = ((a % m) + m) % m;
        int r0 = m, r1 = a, s0 = 0, s1 = 1;
        while (r1 != 0) {
            int q  = r0 / r1;
            int tmp = r0 - q * r1; r0 = r1; r1 = tmp;
            tmp = s0 - q * s1;     s0 = s1; s1 = tmp;
        }
        return ((s0 % m) + m) % m;
    }

    // Translate a binary sequence (0,1) to a k-ary sequence (zero,one)
    private static String binaryTranslate(String sequence, int zero, int one) {
        StringBuilder sb = new StringBuilder();
        for (char c : sequence.toCharArray()) {
            int v = (c == '0') ? zero : one;
            sb.append(v < 10 ? (char) ('0' + v) : (char) ('A' + v - 10));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Public API for scripts
    // -------------------------------------------------------------------------

    /**
     * Constructs the base sequence: NAS lift + rejoined punctured cycle,
     * before any extra binary/complement-free sequences are inserted.
     */
    public static String constructBaseSequence(int n_param, int k_param) {
        if (k_param % 2 != 0 || n_param % 2 != 0 || n_param < 4) {
            return "EMPTY-STRING";
        }
        output  = new StringBuilder();
        k_inner = k_param;
        n_inner = n_param - 1;
        a[0]    = lempel = 0;

        for (ITERATION = 1; ITERATION <= k_inner; ITERATION++) {
            fkm(1, 1, 0);
        }
        for (int i = k_inner / ITERS_TIL_PUNCTURE - 1; i >= 0; i--) printSym(i);

        return output.toString();
    }

    /**
     * Returns the ordered list of translated binary sequences that the construction
     * would insert for a given (n, k):
     *   - k/4 binary de Bruijn sequences (translated from {0,1} to {i, i+k/2})
     *   - when k/2 is odd, one additional complement-free sequence (Lempel-lifted,
     *     translated to {k/4, k/4+k/2})
     */
    public static ArrayList<String> getExtraSequences(int n_param, int k_param) {
        ArrayList<String> seqs = new ArrayList<>();

        for (int i = 0; i < k_param / 4; i++) {
            String dbseq      = genericFKM(n_param, 2);
            String translated = binaryTranslate(dbseq, i, i + k_param / 2);
            seqs.add(translated);
        }

        if ((k_param / 2) % 2 == 1) {
            String dbseq      = genericFKM(n_param - 1, 2);
            String lifted     = lempelLift(dbseq, n_param - 1, 2, 1).get(0);
            String translated = binaryTranslate(lifted, k_param / 4, k_param / 4 + k_param / 2);
            seqs.add(translated);
        }

        return seqs;
    }

    /**
     * Finds every position in {@code original} (treated cyclically) where
     * {@code toInsert} could be spliced in — i.e. where the (n-1)-length prefix
     * of {@code toInsert} matches a window of the cyclic sequence.
     * The returned indices correspond to positions in the original (non-extended) string.
     */
    public static ArrayList<Integer> findAllInsertionIndices(String original, String toInsert, int n) {
        ArrayList<Integer> indices = new ArrayList<>();
        String prefix   = toInsert.substring(0, n - 1);
        String sequence = original + original.substring(0, n - 1);

        for (int i = 0; i < original.length(); i++) {
            if (sequence.substring(i, i + n - 1).equals(prefix)) {
                indices.add(i);
            }
        }
        return indices;
    }

    // Insert a cycle into an original sequence at first occurance of the n-1 length prefix 
    private static String insertSequence(String original, String toInsert, int n) {
        StringBuilder sb = new StringBuilder();

        String prefix = toInsert.substring(0, n - 1);

        String sequence = original + original.substring(0, n - 1);

        //System.out.println("Prefix: " + prefix + ", toInsert: " + toInsert);

        boolean found = false;

        for(int i = 0; i < sequence.length() - n + 1; i++) {
            String current = sequence.substring(i, i + n - 1);
            if (current.equals(prefix) && !found) {
                sb.append(toInsert);
                found = true;
            }
            sb.append(sequence.charAt(i));
        }

        return sb.toString();
    }
}
