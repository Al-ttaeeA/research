package construction;

import checker.Checker;
import java.util.HashMap;

/**
 * Optimal O(k^2) time, O(k^2) space construction of a maximal complement-free
 * sequence for n=2, even k.
 *
 * The original ConstructEven2 was O(k^3) due to linear scans inside the
 * insertion loop (k/4 iterations × O(k^2) scan each). This version replaces:
 *
 *   1. StringBuilder  →  doubly-linked list (Node)
 *      Gives O(1) insertion once the target node is known, with no character
 *      shifting. StringBuilder.insert() is O(L) because it shifts every
 *      subsequent character; the linked list avoids this entirely.
 *
 *   2. Linear scan for target 2-gram  →  HashMap<Long, Node>
 *      Gives O(1) lookup of the node immediately before any 2-gram.
 *      The map key encodes the 2-gram as a single long: key = a * k + b.
 *      After each insertion the map is updated in O(1): only the two new
 *      2-grams created and the one 2-gram destroyed need updating.
 *
 * Logical implementation is unchanged: FKM with weight pruning, wrap-around,
 * k/4 iterations each doing two targeted single-symbol insertions, then
 * removal of the wrap-around symbol.
 *
 * Complexity:
 *   Time  – FKM: O(k^2).  Insertions: O(k/4) iterations × O(1) each = O(k).
 *            Total: O(k^2).
 *   Space – Linked list: O(k^2) nodes.  HashMap: O(k^2) entries.
 *            Total: O(k^2) words.
 */
public class ConstructEven2Optimal {

    public static int n = 2;

    public static int MAX_SEQ_LEN = 1_000_000;

    // -----------------------------------------------------------------------
    // Doubly-linked list node
    // -----------------------------------------------------------------------
    private static class Node {
        int val;
        Node prev, next;
        Node(int val) { this.val = val; }
    }

    // -----------------------------------------------------------------------
    // Doubly-linked list with a sentinel head and tail for easy edge handling.
    // The list represents the open (non-cyclic) sequence during construction.
    // -----------------------------------------------------------------------
    private static class SeqList {
        final Node head; // sentinel; head.next is first real node
        final Node tail; // sentinel; tail.prev is last real node
        int size;

        SeqList() {
            head = new Node(-1);
            tail = new Node(-1);
            head.next = tail;
            tail.prev = head;
            size = 0;
        }

        /** Append a symbol to the end of the list in O(1). */
        void append(int val) {
            Node node = new Node(val);
            node.prev = tail.prev;
            node.next = tail;
            tail.prev.next = node;
            tail.prev = node;
            size++;
        }

        /**
         * Insert newVal immediately after 'before' in O(1).
         * Returns the newly created node.
         */
        Node insertAfter(Node before, int newVal) {
            Node node = new Node(newVal);
            node.prev = before;
            node.next = before.next;
            before.next.prev = node;
            before.next = node;
            size++;
            return node;
        }

        /** Remove the last node in O(1). */
        void removeLast() {
            if (size == 0) return;
            Node last = tail.prev;
            last.prev.next = tail;
            tail.prev = last.prev;
            size--;
        }

        /** Collect the sequence into a String for output. */
        String toSequenceString() {
            StringBuilder sb = new StringBuilder(size);
            Node cur = head.next;
            while (cur != tail) {
                sb.append(toChar(cur.val));
                cur = cur.next;
            }
            return sb.toString();
        }
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        loop(30);
    }

    public static void loop(int maxK) {
        for (int k = 4; k <= maxK; k += 2) {
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

    /**
     * Constructs the complement-free sequence for the given n=2, even k.
     *
     * Steps (identical logic to ConstructEven2, different data structures):
     *   1. Run FKM into a doubly-linked list, building the 2-gram map as we go.
     *   2. Wrap around: append seq[0] and register the new wrap 2-gram in the map.
     *   3. For i in [0, k/4): two O(1) targeted insertions per iteration,
     *      each updating the map in O(1).
     *   4. Remove the wrap-around symbol.
     *   5. Convert linked list to String.
     */
    public static String constructSequence(int n, int k) {
        SeqList list = new SeqList();

        // gramMap: 2-gram key (a*k + b) → the Node whose value is 'a',
        // i.e. the node such that node.val == a and node.next.val == b.
        // This lets us find the insertion point for any 2-gram in O(1).
        HashMap<Long, Node> gramMap = new HashMap<>();

        // --- Phase 1: FKM output into the linked list ---
        int[] arr = new int[n + 1];
        fkm(1, 1, 0, n, k, arr, list, gramMap);

        // --- Phase 2: wrap-around (append first symbol again) ---
        // This mirrors sb.append(sb.charAt(0)) in the original.
        int firstVal = list.head.next.val;
        Node lastBeforeWrap = list.tail.prev;

        list.append(firstVal);

        // Register the new 2-gram (lastBeforeWrap.val, firstVal) in the map.
        // The node "before" the wrap 2-gram is lastBeforeWrap.
        putGram(gramMap, lastBeforeWrap.val, firstVal, k, lastBeforeWrap);

        // --- Phase 3: k/4 insertion iterations ---
        for (int i = 0; i < k / 4; i++) {
            int digit     = i * 2;
            int comp      = k - 1 - digit;   // complement of digit
            int next      = digit + 1;        // digit + 1
            int compNext  = k - 2 - digit;    // complement of digit + 1

            // Step 1: find 2-gram [comp, next], insert digit between them.
            //   Before: ... comp → next ...
            //   After:  ... comp → digit → next ...
            //   Destroyed 2-gram: (comp, next)
            //   Created 2-grams:  (comp, digit) and (digit, next)
            Node beforeStep1 = gramMap.remove(gramKey(comp, next, k));
            Node inserted1   = list.insertAfter(beforeStep1, digit);
            putGram(gramMap, comp,  digit, k, beforeStep1);
            putGram(gramMap, digit, next,  k, inserted1);

            // Step 2: find 2-gram [comp, compNext], insert next between them.
            //   Before: ... comp → compNext ...
            //   After:  ... comp → next → compNext ...
            //   Destroyed 2-gram: (comp, compNext)
            //   Created 2-grams:  (comp, next) and (next, compNext)
            Node beforeStep2 = gramMap.remove(gramKey(comp, compNext, k));
            Node inserted2   = list.insertAfter(beforeStep2, next);
            putGram(gramMap, comp, next,     k, beforeStep2);
            putGram(gramMap, next, compNext, k, inserted2);
        }

        // --- Phase 4: remove the wrap-around symbol ---
        list.removeLast();

        // --- Phase 5: convert to String ---
        return list.toSequenceString();
    }

    // -----------------------------------------------------------------------
    // FKM necklace generator — identical pruning and emission logic to the
    // original, but writes into the linked list and registers 2-grams in
    // the map as each symbol is appended.
    // -----------------------------------------------------------------------
    private static void fkm(int t, int p, int weightSum,
                             int n, int k, int[] arr,
                             SeqList list, HashMap<Long, Node> gramMap) {
        if (2 * (weightSum + (n - t + 1) * (k - 1)) <= n * (k - 1)) return;
        if (t > n) {
            if (n % p == 0) {
                for (int i = 1; i <= p; i++) {
                    Node prev = list.tail.prev; // node that will precede the new one
                    list.append(arr[i]);
                    // Register the 2-gram (prev.val, arr[i]) only if prev is a real node
                    if (prev != list.head) {
                        putGram(gramMap, prev.val, arr[i], k, prev);
                    }
                }
            }
            return;
        }
        for (int j = arr[t - p]; j < k; j++) {
            arr[t] = j;
            fkm(t + 1, j == arr[t - p] ? p : t, weightSum + j, n, k, arr, list, gramMap);
        }
    }

    // -----------------------------------------------------------------------
    // HashMap helpers
    // -----------------------------------------------------------------------

    /** Encode 2-gram (a, b) as a single long key. */
    private static long gramKey(int a, int b, int k) {
        return (long) a * k + b;
    }

    /**
     * Register that the 2-gram (a, b) starts at node 'aNode'
     * (i.e. aNode.val == a and aNode.next.val == b).
     */
    private static void putGram(HashMap<Long, Node> map,
                                 int a, int b, int k, Node aNode) {
        map.put(gramKey(a, b, k), aNode);
    }

    // -----------------------------------------------------------------------
    // Symbol encoding (unchanged from original)
    // -----------------------------------------------------------------------
    private static char toChar(int v) {
        return v < 10 ? (char) ('0' + v) : (char) ('A' + v - 10);
    }
}