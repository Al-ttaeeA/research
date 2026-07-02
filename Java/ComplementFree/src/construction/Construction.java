package construction;

import checker.Checker;
import java.util.ArrayList;
import java.util.Scanner;

public class Construction {
    static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {
        System.out.println("Enter k and n:");
        int k = scanner.nextInt();
        int n = scanner.nextInt();

        String sequence;

        if(k % 2 == 0) {
            if(n % 2 ==0) {
                System.out.println("Even k, Even n");
                sequence = evenEven(k, n);
            }
            else{
                System.out.println("Even k, Odd n");
                sequence = evenOdd(k, n);
            }
        }
        else{
            if(n % 2 ==0) {
                System.out.println("Odd k, Even n");
                sequence = oddEven(k, n);
            }
            else{
                System.out.println("Odd k, Odd n");
                sequence = oddOdd(k, n);
            }
        }

        System.out.println(sequence);

        Checker.check(sequence, k, n);
    }

    public static String evenOdd(int k, int n) {
        int weight = n * (k-1) / 2;

        ArrayList<String> necklaces = generateNecklacesLex(k, n);

        StringBuilder sb = new StringBuilder();
        for (String necklace : necklaces) {
            int sum = 0;
            for (char c : necklace.toCharArray()) {
                sum += c - '0';
            }
            if (sum > weight) {
                sb.append(leastPeriod(necklace));
            }
        }
        return sb.toString();
    }

    public static ArrayList<String> generateNecklacesLex(int k, int n) {
        ArrayList<String> necklaces = new ArrayList<>();
        generateNecklacesHelper("", k, n, necklaces);
        return necklaces;
    }

    private static void generateNecklacesHelper(String current, int k, int n, ArrayList<String> necklaces) {
        if (current.length() == n) {
            if (isNecklace(current)) {  
                necklaces.add(current);
            }
            return;
        }
        for (int c = 0; c < k; c++) {
            generateNecklacesHelper(current + (char) ('0' + c), k, n, necklaces);
        }
    }

    private static boolean isNecklace(String s) {
        for (int i = 1; i < s.length(); i++) {
            String rotation = s.substring(i) + s.substring(0, i);
            if (rotation.compareTo(s) < 0) {
                return false;
            }
        }
        return true;
    }

    // Returns the least period of a string, or the string itself if it is not periodic
    private static String leastPeriod(String s) {
        int n = s.length();
        for (int p = 1; p <= n / 2; p++) {
            if (n % p == 0) {
                String period = s.substring(0, p);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < n / p; i++) {
                    sb.append(period);
                }
                if (sb.toString().equals(s)) {
                    return period;
                }
            }
        }
        return s;
    }

    /**
     * Construct a maximal complement-free sequence for even k, even n (n >= 4).
     *
     * Strategy (mirrors ConstructEvenEven.constructEvenEven):
     *   1. Run the Lempel D^-1 lift of the odd-order-(n-1) NAS k times using the
     *      existing nasFkm / nasPrint infrastructure.  n_orig_nas is set to n (even)
     *      so nasPrint automatically takes the Lempel lift path.  The punctured
     *      all-(k-1) cycle is rejoined afterwards.
     *   2. Insert k/4 complement-paired binary cycles, each translated to the symbol
     *      pair (i, i+k/2), using a binary de Bruijn sequence of order n.
     *   3. If k/2 is odd, insert one extra cycle derived from a Lempel-lifted binary
     *      de Bruijn sequence of order n-1, translated to (k/4, k/4+k/2).
     *
     * Returns "EMPTY-STRING" for invalid input (k odd, n odd, or n < 4).
     */
    public static String evenEven(int k, int n) {
        if (k % 2 != 0 || n % 2 != 0 || n < 4) {
            return "EMPTY-STRING";
        }

        if (k == 2) {
            String dbseq = genericFKM(n-1, 2);
            String lifted = lempelLift(dbseq, n - 1, 2, 1).get(0);
            return lifted;
        }

        // Initialise NAS state: underlying odd order = n-1, full even order = n.
        // n_orig_nas = n (even) causes nasPrint to take the Lempel D^-1 lift path.
        nas_output = new StringBuilder();
        k_nas      = k;
        n_nas      = n - 1;   // odd underlying order for nasFkm / nasSmallerThanNeg
        n_orig_nas = n;       // even: tells nasPrint to apply the Lempel lift
        a_nas[0]   = lempel_nas = 0;

        for (ITERATION_nas = 1; ITERATION_nas <= k; ITERATION_nas++) {
            nasFkm(1, 1, 0);
        }

        // Rejoin the punctured all-(k-1) cycle to close the Eulerian circuit.
        for (int i = k / ITERS_TIL_PUNCTURE_nas - 1; i >= 0; i--) nasPrintSym(i);

        // OPTIMIZATION: genericFKM(n, 2) is invariant across the loop below — compute
        // it once here rather than regenerating it k/4 times.
        String dbseqN = genericFKM(n, 2);

        // Insert k/4 complement-paired binary cycles translated to symbol pair (i, i+k/2).
        for (int i = 0; i < k / 4; i++) {
            String translated = binaryTranslate(dbseqN, i, i + k / 2);
            nas_output = new StringBuilder(insertSequence(nas_output.toString(), translated, n));
        }

        // If k/2 is odd, insert one extra cycle via a Lempel-lifted binary de Bruijn sequence.
        if ((k / 2) % 2 == 1) {
            String dbseq      = genericFKM(n - 1, 2);
            String lifted     = lempelLift(dbseq, n - 1, 2, 1).get(0);
            String translated = binaryTranslate(lifted, k / 4, k / 4 + k / 2);
            nas_output = new StringBuilder(insertSequence(nas_output.toString(), translated, n));
        }

        return nas_output.toString();
    }

    // ==========================================================================================
    // NAS-BASED HELPERS  (mirrors NAS.c by Joe Sawada, 2026)
    //
    // Mathematical basis
    // ------------------
    // For odd k:
    //   negative of digit a  = (-a) mod k  = (k - a) mod k
    //   complement of digit a = k - 1 - a
    //
    // Let s = (k-1)/2.  Then 2s = k-1 ≡ -1 (mod k).
    // Shifting digit a by s gives (a+s) mod k, and:
    //   negative((a+s) mod k) = (k - a - s) mod k
    //   complement(a)         = k - 1 - a
    // These coincide (mod k) because k - 1 - a = k - a - 1 = k - a - (2s) mod k when
    // we treat them symmetrically.  Concretely: applying the shift s to every symbol of
    // a negative-free (NAS) sequence yields a complement-free sequence.
    //
    // Implementation
    // --------------
    // The helpers below are a line-for-line Java translation of NAS.c.  They build the
    // antinegative Eulerian subgraph Wk(n-1) (Construction 5.1, odd n) or Zk(n)
    // (Theorem 7.2, even n) from the paper "Negative Avoiding Sequences" by Mitchell &
    // Wild (2026), then collect the resulting NAS into a StringBuilder.  After generation
    // applyComplementShift converts the NAS into a complement-free sequence.
    // ==========================================================================================

    // --- Static state (mirrors global variables in NAS.c) ---

    /** Current necklace tuple being built by nasFkm (1-indexed). Mirrors a[] in NAS.c. */
    private static int[] a_nas = new int[100];

    /** Running prefix sum for the Lempel D^-1 homomorphism. Mirrors lempel in NAS.c. */
    private static int lempel_nas;

    /** Which of the k Lempel-lift iterations we are currently in. Mirrors ITERATION. */
    private static int ITERATION_nas;

    /**
     * Period of the special constant-(k-1) cycle that must be punctured once per
     * ITERS_TIL_PUNCTURE iterations so the k pre-image circuits fuse into one Eulerian
     * circuit.  Mirrors ITERS_TIL_PUNCTURE in NAS.c.
     */
    private static int ITERS_TIL_PUNCTURE_nas;

    /**
     * Working span used inside nasFkm.
     * Equals n_orig_nas for odd n_orig; equals n_orig_nas - 1 for even n_orig (the
     * underlying odd-order NAS used by the Lempel lift).
     */
    private static int n_nas;

    /** Original span as passed to oddOdd / oddEven. */
    private static int n_orig_nas;

    /** Alphabet size. */
    private static int k_nas;

    /** Accumulated output symbols (replaces printf / PrintSym in NAS.c). */
    private static StringBuilder nas_output;

    // --- Low-level output ---

    /**
     * Append one symbol x to nas_output.
     * Values 0-9 use characters '0'-'9'; values 10+ use 'A'-'Z'.
     * Mirrors PrintSym in NAS.c.
     */
    private static void nasPrintSym(int x) {
        if (x < 10) nas_output.append((char) ('0' + x));
        else        nas_output.append((char) ('A' + (x - 10)));
    }

    // --- Arithmetic helper ---

    /** Recursive Euclidean GCD. Mirrors GCD in NAS.c. */
    private static int nasGcd(int a, int b) {
        return b == 0 ? a : nasGcd(b, a % b);
    }

    // --- Necklace / negation helpers ---

    /**
     * Compute the lexicographically smallest rotation (necklace representative) of
     * b[1..n_nas] and store it in neck[1..n_nas].  neck must have size >= 2*n_nas+1.
     * Uses the same doubling-plus-scan algorithm as GetNecklace in NAS.c.
     */
    private static void nasGetNecklace(int[] b, int[] neck) {
        int n = n_nas;
        // Double the array so every rotation appears as a contiguous sub-array
        for (int j = 1; j <= n; j++) neck[j] = neck[n + j] = b[j];
        int j = 1, t = 1, p = 1;
        do {
            t = t + p * ((j - t) / p);
            j = t + 1;
            p = 1;
            while (j <= 2 * n && neck[j - p] <= neck[j]) {
                if (neck[j - p] < neck[j]) p = j - t + 1;
                j++;
            }
        } while (p * ((j - t) / p) < n);
        // Copy the canonical rotation to the front of neck[]
        for (j = 1; j <= n; j++) neck[j] = neck[j + t - 1];
    }

    /**
     * Return true iff a_nas[1..n_nas] is lexicographically smaller than the necklace
     * representative of its component-wise negative (k - a_i) mod k.
     *
     * This selects exactly one member from each {necklace, negative-necklace} pair —
     * implementing Construction 5.1 (Lemma 5.5) of the paper.
     * Mirrors SmallerThanNeg in NAS.c.
     */
    private static boolean nasSmallerThanNeg() {
        int n = n_nas, k = k_nas;
        int[] b    = new int[100];
        int[] neck = new int[200];
        for (int i = 1; i <= n; i++) b[i] = (k - a_nas[i]) % k;  // compute negative
        nasGetNecklace(b, neck);                                    // canonicalise
        for (int i = 1; i <= n; i++) {
            if (a_nas[i] < neck[i]) return true;
            if (a_nas[i] > neck[i]) return false;
        }
        return false;  // equal => self-negative; do not include
    }

    // --- Output / Lempel-lift helper ---

    /**
     * Output the Lyndon prefix of the current necklace (odd n_orig_nas), or apply the
     * Lempel D^-1 homomorphism to it (even n_orig_nas).
     *
     * Odd n_orig path  : append a_nas[1..p] directly (they form the Lyndon word).
     *
     * Even n_orig path : maintain the running prefix sum  lempel_nas = sum(a[i]) mod k
     *                    and append those values.  Also handles the puncture of the
     *                    constant-(k-1) necklace: one occurrence per
     *                    ITERS_TIL_PUNCTURE_nas iterations is suppressed so that the k
     *                    pre-image circuits fuse into a single Eulerian circuit
     *                    (Theorem 7.2 of the paper).
     *
     * Mirrors Print(p) in NAS.c.
     */
    private static void nasPrint(int p) {
        if (n_orig_nas % 2 == 0) {
            // ---- Even n_orig: Lempel D^-1 lift ----
            for (int i = 1; i <= p; i++) {
                // The constant-(k-1) necklace has Lyndon word of length 1, value k-1.
                // Detect it and apply the puncture logic.
                if (p == 1 && a_nas[i] == k_nas - 1) {
                    if (ITERATION_nas == 1) {
                        // First encounter in the first iteration: compute puncture period.
                        // next_symbol is what lempel_nas would become after absorbing this
                        // symbol (computed before the prefix-sum update below).
                        int next_symbol = (lempel_nas + a_nas[i]) % k_nas;
                        ITERS_TIL_PUNCTURE_nas = k_nas / nasGcd(k_nas, next_symbol);
                    }
                    // Skip (puncture) on every ITERS_TIL_PUNCTURE_nas-th iteration
                    if (ITERATION_nas % ITERS_TIL_PUNCTURE_nas == 0) return;
                }
                // D^-1: the output symbol is the running prefix sum
                lempel_nas = (lempel_nas + a_nas[i]) % k_nas;
                nasPrintSym(lempel_nas);
            }
        } else {
            // ---- Odd n_orig: print Lyndon prefix directly ----
            for (int i = 1; i <= p; i++) nasPrintSym(a_nas[i]);
        }
    }

    // --- Core FKM generator ---

    /**
     * Recursive FKM necklace generator restricted to k-ary n_nas-tuples that satisfy the
     * pseudoweight condition for negative-free sequences (Definitions 5.2, Construction 5.1).
     *
     * w tracks TWICE the pseudoweight of a_nas[1..t-1] (kept integer):
     *   symbol 0 contributes k   (f(0) = k/2, doubled = k)
     *   symbol j > 0 contributes 2*j
     *
     * A necklace at a leaf is emitted iff:
     *   w > k*n_nas  (pseudoweight strictly above kn/2 — entire Ek(n) set), OR
     *   w == k*n_nas AND nasSmallerThanNeg()  (exactly kn/2 — selects one from each
     *                                          conjugate pair, per Lemma 5.5).
     *
     * Mirrors FKM(t, p, w) in NAS.c.
     */
    private static void nasFkm(int t, int p, int w) {
        int n = n_nas, k = k_nas;
        // Pruning: even assigning max contribution 2*(k-1) to every remaining position
        // cannot reach the required doubled pseudoweight k*n -> dead end.
        if ((n - t + 1) * 2 * (k - 1) + w < k * n) return;
        if (t > n) {
            // Leaf: emit Lyndon prefix iff this is a valid necklace period AND the
            // pseudoweight condition passes.
            if (n % p == 0 && (w > k * n || nasSmallerThanNeg())) nasPrint(p);
            return;
        }
        // Enumerate symbols j >= a_nas[t-p] (maintains the necklace lex-order invariant)
        for (int j = a_nas[t - p]; j < k; j++) {
            a_nas[t] = j;
            int x = (j == 0) ? k : 2 * j;          // contribution to doubled pseudoweight
            if (j == a_nas[t - p]) nasFkm(t + 1, p, w + x);  // period candidate unchanged
            else                   nasFkm(t + 1, t, w + x);   // new period candidate = t
        }
    }

    // --- Alphabet shift: negative-free -> complement-free ---

    /**
     * Apply alphabet shift s = (k-1)/2 to every symbol in seq.
     *
     * For odd k, 2s = k-1 ≡ -1 (mod k), so the shift maps the "negative" relation
     * a <-> -a to the "complement" relation a <-> k-1-a.  Consequently, any
     * negative-free (NAS) sequence becomes complement-free after this shift.
     */
    private static String applyComplementShift(String seq, int k) {
        int shift = (k - 1) / 2;
        StringBuilder sb = new StringBuilder(seq.length());
        for (int ci = 0; ci < seq.length(); ci++) {
            char c = seq.charAt(ci);
            int sym = (c >= '0' && c <= '9') ? (c - '0') : (c - 'A' + 10);
            int shifted = (sym + shift) % k;
            if (shifted < 10) sb.append((char) ('0' + shifted));
            else              sb.append((char) ('A' + (shifted - 10)));
        }
        return sb.toString();
    }

    // ==========================================================================================
    // Public construction methods
    // ==========================================================================================

    /**
     * Construct a maximal complement-free sequence for odd k, odd n.
     *
     * Mirrors the ODD CASE of NAS.c main():
     *   1. nasFkm generates the Fk(n) edge set (Construction 5.1) — a maximal NASk(n).
     *   2. applyComplementShift converts the NAS to complement-free by shifting each
     *      symbol by (k-1)/2.
     */
    public static String oddOdd(int k, int n) {
        // Initialise all static state before the run
        nas_output = new StringBuilder();
        n_nas      = n;
        n_orig_nas = n;
        k_nas      = k;
        a_nas[0]   = 0;

        nasFkm(1, 1, 0);  // Generate the maximal NASk(n) via pseudoweight-filtered FKM

        // Shift each symbol by (k-1)/2 to convert negative-free -> complement-free
        return applyComplementShift(nas_output.toString(), k);
    }

    /**
     * Construct a maximal complement-free sequence for odd k, even n.
     *
     * Two sub-cases (mirrors NAS.c main()):
     *
     * n == 2 : Direct construction of Y_k (Definition 6.1 / Theorem 6.2).
     *          For each starting vertex j = 0..k-1, outputs:
     *            - The vertex j itself (step-1 main cycle).
     *            - An extra j for the diagonal edge (j,j) when 0 < j <= (k-1)/2.
     *            - For each step i = 2..(k-1)/2: if j is the canonical seed of the
     *              i-step cycle (j < gcd(i,k)), the full cycle of length k/gcd(i,k).
     *
     * n >= 4 : Lempel D^-1 lift (Theorem 7.2).
     *          nasFkm is run k times (each time with the Lempel prefix sum continuing
     *          from the previous iteration) to generate the D^-1 lift of the order-(n-1)
     *          NAS.  For odd k, the extra self-negative circuit b_x (which has exactly
     *          one edge and therefore contributes one output symbol) is inserted before
     *          each FKM pass when lempel_nas is in (0, (k-1)/2].  One copy of the
     *          constant-(k-1) necklace is punctured every ITERS_TIL_PUNCTURE_nas
     *          iterations; the missing tail is appended afterwards to close the circuit.
     *
     * The result is shifted by (k-1)/2 to produce the complement-free output.
     */
    public static String oddEven(int k, int n) {
        // Initialise all static state before the run
        nas_output = new StringBuilder();
        k_nas      = k;
        n_orig_nas = n;

        if (n == 2) {
            // ---- Special case: n = 2, odd k ----------------------------------------
            // Mirrors the k-odd sub-block of the n==2 special case in NAS.c main().
            // Iterates over starting vertices j = 0..k-1 and joins cycles by step size.
            for (int j = 0; j < k; j++) {
                nasPrintSym(j);  // step-1 main cycle: outputs vertex j

                // Diagonal edge (j, j) is in Y_k for j = 1,...,(k-1)/2
                if (j > 0 && j <= k / 2) nasPrintSym(j);

                // Cycles of step i, for i = 2,...,(k-1)/2
                for (int i = 2; i <= k / 2; i++) {
                    int g = nasGcd(i, k);
                    // Only the canonical seed j < gcd(i,k) outputs this cycle
                    if (j < g) {
                        for (int t = 1; t <= k / g; t++) nasPrintSym((j + i * t) % k);
                    }
                }
            }
        } else {
            // ---- Even n > 2, odd k --------------------------------------------------
            // Mirrors the EVEN CASE (n_orig % 2 == 0 && n_orig > 2) of NAS.c main().
            // The underlying NAS has odd order n-1; nasFkm uses n_nas = n-1, while
            // nasPrint applies the Lempel D^-1 lift because n_orig_nas is even.
            n_nas    = n - 1;
            a_nas[0] = lempel_nas = 0;

            for (ITERATION_nas = 1; ITERATION_nas <= k; ITERATION_nas++) {
                // For odd k, when lempel_nas is in (0, (k-1)/2] the pre-image of the
                // self-negative loop [0,0,...,0] in Tk(n-2) under D^-1 is the single-
                // edge circuit b_x = [x,x,...,x] at vertex (x,...,x) in Bk(n-1),
                // which contributes exactly 1 output symbol (Theorem 7.2, odd-k case).
                if (lempel_nas > 0 && lempel_nas <= (k - 1) / 2) {
                    nasPrintSym(lempel_nas);
                }

                // Generate the order-(n-1) NAS; nasPrint applies D^-1 to lift to order n
                nasFkm(1, 1, 0);
            }

            // Append the tail of the punctured cycle to close the Eulerian circuit.
            // The tail descends from (k / ITERS_TIL_PUNCTURE_nas - 1) down to 0.
            for (int i = k / ITERS_TIL_PUNCTURE_nas - 1; i >= 0; i--) nasPrintSym(i);
        }

        // Shift each symbol by (k-1)/2 to convert negative-free -> complement-free
        return applyComplementShift(nas_output.toString(), k);
    }

    // ==========================================================================================
    // Even-even construction helpers
    // ==========================================================================================

    /**
     * Generate the Granddaddy de Bruijn sequence of order n over an alphabet of size k
     * using the standard FKM necklace enumeration (no pseudoweight filter).
     * Enumerates all necklaces in lex order; concatenates each Lyndon word.
     * All state is local — no NAS globals are touched.
     */
    private static String genericFKM(int n, int k) {
        StringBuilder sb = new StringBuilder();
        int[] arr = new int[n + 1];  // 1-indexed; arr[0] == 0 acts as sentinel
        fkmGenerate(1, 1, n, k, arr, sb);
        return sb.toString();
    }

    /** Recursive FKM enumeration helper for genericFKM. All state is passed as parameters. */
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

    /**
     * Lempel D^-1 lift of a de Bruijn sequence of order n over Z_k with multiplier beta.
     * Returns k vertex-disjoint cycles C_0,...,C_{k-1} each of length k^n.
     * C_0[j] = beta^{-1} * prefix_sum(seqStr, j+1) mod k; C_i = C_0 + i (mod k).
     *
     * OPTIMIZATION: the C_0 prefix-sum array is computed once; each C_i is derived by
     * adding i mod k per element, avoiding k separate prefix-sum passes.
     */
    private static ArrayList<String> lempelLift(String seqStr, int n, int k, int beta) {
        int betaInv = modInverse(beta, k);
        int len = seqStr.length();

        int[] C0 = new int[len];
        int sum = 0;
        for (int j = 0; j < len; j++) {
            sum = (sum + Character.getNumericValue(seqStr.charAt(j))) % k;
            C0[j] = (betaInv * sum) % k;
        }

        ArrayList<String> cycles = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            StringBuilder sb = new StringBuilder(len);
            for (int j = 0; j < len; j++) {
                int v = (C0[j] + i) % k;
                sb.append(v < 10 ? (char) ('0' + v) : (char) ('A' + v - 10));
            }
            cycles.add(sb.toString());
        }
        return cycles;
    }

    /** Modular multiplicative inverse via the extended Euclidean algorithm. */
    private static int modInverse(int a, int m) {
        a = ((a % m) + m) % m;
        int r0 = m, r1 = a, s0 = 0, s1 = 1;
        while (r1 != 0) {
            int q   = r0 / r1;
            int tmp = r0 - q * r1; r0 = r1; r1 = tmp;
            tmp = s0 - q * s1;     s0 = s1; s1 = tmp;
        }
        return ((s0 % m) + m) % m;
    }

    /**
     * Map each symbol of a binary sequence to either {@code zero} (for '0') or
     * {@code one} (for '1'), producing a k-ary sequence.
     *
     * OPTIMIZATION: iterates by index to avoid the char[] allocation of toCharArray().
     */
    private static String binaryTranslate(String sequence, int zero, int one) {
        StringBuilder sb = new StringBuilder(sequence.length());
        for (int ci = 0; ci < sequence.length(); ci++) {
            int v = (sequence.charAt(ci) == '0') ? zero : one;
            sb.append(v < 10 ? (char) ('0' + v) : (char) ('A' + v - 10));
        }
        return sb.toString();
    }

    /**
     * Insert {@code toInsert} into {@code original} at the first cyclic occurrence of
     * {@code toInsert}'s leading (n-1)-symbol prefix.
     *
     * OPTIMIZATION: loop bound is simplified to original.length() (equivalent to
     * sequence.length() - n + 1 since sequence = original + original[0..n-2]).
     */
    private static String insertSequence(String original, String toInsert, int n) {
        StringBuilder sb = new StringBuilder();
        String prefix   = toInsert.substring(0, n - 1);
        // Extend cyclically by n-1 symbols so every cyclic window is covered.
        String sequence = original + original.substring(0, n - 1);
        boolean found   = false;
        for (int i = 0; i < original.length(); i++) {
            String current = sequence.substring(i, i + n - 1);
            if (!found && current.equals(prefix)) {
                sb.append(toInsert);
                found = true;
            }
            sb.append(sequence.charAt(i));
        }
        return sb.toString();
    }
}