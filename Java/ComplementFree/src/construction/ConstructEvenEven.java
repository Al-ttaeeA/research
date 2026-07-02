package construction;

public class ConstructEvenEven {
    public static int n = 4;
    public static int k = 12;

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
        String construction = constructEvenEven(n, k);
        System.out.println(construction);
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
}
