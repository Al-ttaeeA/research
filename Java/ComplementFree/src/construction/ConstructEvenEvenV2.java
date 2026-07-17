package construction;

import checker.Checker;

public class ConstructEvenEvenV2 {
    public static int n = 4;
    public static int k = 14;

    public static int MAX_SEQ_LEN = 5_000_000;

    // -------------------------------------------------------------------------
    // Static state mirroring NAS.c globals
    // -------------------------------------------------------------------------
    private static int[] a  = new int[100];   // current necklace tuple for FKM (1-indexed)
    private static int[] aa = new int[100];   // current necklace tuple for FKM2 (binary)
    private static int lempel;               // running prefix sum for Lempel lift
    private static int lempel2;              // running prefix sum for binary Lempel lift
    private static int ITERATION;            // which of the k lift iterations we are in
    private static int ITERS_TIL_PUNCTURE;  // period of the all-(k-1) cycle
    private static int n_inner;             // working order = n_orig - 1 (odd)
    private static int k_inner;             // alphabet size
    private static StringBuilder output;   // accumulated output

    public static void main(String[] args) {
        loop();
    }

    public static void loop() {
        for (int k = 4; k <= 14; k += 2) {
            for (int n = 4; n <= 8; n += 2) {
                long expectedLen = ((long) Math.pow(k, n) - (long) Math.pow(2, n)) / 2;
                if (expectedLen > MAX_SEQ_LEN) {
                    System.out.printf("Skipping n=%d, k=%d: sequence too long%n", n, k);
                    continue;
                }

                System.out.println("Constructing sequence for n=" + n + ", k=" + k);

                String cas = constructEvenEven(n, k);

                Checker.extensiveCheck(cas, k, n);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Main construction entry point
    // -------------------------------------------------------------------------

    /**
     * Generate a maximal complement-free NAS of order n over Z_k.
     *
     * Phase 1 — mirrors NAS.c even-case exactly:
     *   Lift the order-(n-1) NAS via Lempel's D^-1 lift.  Before each of the k
     *   lift iterations, if 0 < lempel <= k/2 - 1, a binary FKM2 cycle translated
     *   to {lempel, lempel + k/2} is appended inline.  After the k iterations the
     *   punctured all-(k-1) cycle is rejoined.  At this point the output is identical
     *   to NAS.c for the even-even case.
     *
     * Phase 2 — makes the sequence maximal CF:
     *   Append one final FKM2 binary cycle translated to {0, k/2} (the offset=0 cycle
     *   that NAS.c omits because lempel==0 at iteration 1).
     *
     * @param n_param even order >= 4
     * @param k_param even alphabet size >= 4
     * @return maximal CF NAS string, or "EMPTY-STRING" for invalid input
     */
    public static String constructEvenEven(int n_param, int k_param) {
        if (k_param % 2 != 0 || n_param % 2 != 0 || n_param < 4) {
            return "EMPTY-STRING";
        }

        // Initialise state
        output  = new StringBuilder();
        k_inner = k_param;
        n_inner = n_param - 1;   // odd underlying order
        a[0]    = lempel = 0;

        // ----- Phase 1: Lempel lift of order-(n-1) NAS, with inline FKM2 insertions -----
        for (ITERATION = 1; ITERATION <= k_inner; ITERATION++) {
            // Mirror NAS.c: if (lempel > 0 && lempel <= (k-1)/2) { FKM2(1,1,lempel); }
            // For even k, (k-1)/2 == k/2 - 1, so this fires for lempel in {1, ..., k/2-1}.
            if (lempel > 0 && lempel <= (k_inner - 1) / 2) {
                aa[0] = lempel2 = 0;
                fkm2(1, 1, lempel);
            }
            fkm(1, 1, 0);
        }

        // Rejoin the punctured all-(k-1) cycle (mirrors: for i=k/ITERS-1; i>=0; i--)
        for (int i = k_inner / ITERS_TIL_PUNCTURE - 1; i >= 0; i--) printSym(i);

        // ----- Phase 2: Append FKM2 cycle at offset 0 (symbols 0 and k/2) -----
        // This is the cycle NAS.c never adds (lempel==0 at iteration 1 prevents it).
        // Including it covers the missing {0, k/2}-alphabet complement pairs and
        // brings the sequence to maximum complement-free length.
        aa[0] = lempel2 = 0;
        fkm2(1, 1, 0);

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
                    int next_symbol = (lempel + a[i]) % k_inner;
                    ITERS_TIL_PUNCTURE = k_inner / gcd(k_inner, next_symbol);
                }
                if (ITERATION % ITERS_TIL_PUNCTURE == 0) return;
            }
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
    // -------------------------------------------------------------------------

    private static void fkm(int t, int p, int w) {
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
    // Binary Lempel lift printer — mirrors Print2(p, offset) in NAS.c
    // Applies a running-prefix-sum mod 2 to the binary necklace aa[1..p],
    // mapping cumulative sum 0 -> offset and 1 -> offset + k/2.
    // -------------------------------------------------------------------------

    private static void print2(int p, int offset) {
        for (int i = 1; i <= p; i++) {
            lempel2 = (lempel2 + aa[i]) % 2;
            printSym(lempel2 == 0 ? offset : offset + k_inner / 2);
        }
    }

    // -------------------------------------------------------------------------
    // Binary FKM — mirrors FKM2(t, p, offset) in NAS.c
    // Standard binary necklace enumeration over order n_inner; each leaf necklace
    // is output via print2 which applies the binary Lempel lift with the given offset.
    // -------------------------------------------------------------------------

    private static void fkm2(int t, int p, int offset) {
        if (t > n_inner) {
            if (n_inner % p == 0) print2(p, offset);
            return;
        }
        aa[t] = aa[t - p];
        fkm2(t + 1, p, offset);
        if (aa[t] == 0) {
            aa[t] = 1;
            fkm2(t + 1, t, offset);
        }
    }
}
