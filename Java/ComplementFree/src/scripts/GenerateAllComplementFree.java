package scripts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerateAllComplementFree {
    public static int k = 6;
    public static int n = 2;

    public static void main(String[] args) {
        //generateAllComplementFree(k, n);

        generateByBrute(k, n);
    }

    // Brute-force: generates every string of the maximal complement-free length where
    // each digit appears at most k^{n-1} times, then prints those that are complement-free
    // when read as a cyclic sequence with window size n.
    // Requires total = k^n <= 64 so that the seen-gram state fits in a long bitmask.
    public static void generateByBrute(int k, int n) {
        int total = (int) Math.pow(k, n);
        int maxLen = (k % 2 == 0) ? total / 2 : (total - 1) / 2;
        int maxDigitCount = (int) Math.pow(k, n - 1);

        if (total > 64) {
            System.out.println("k^n=" + total + " exceeds 64; long bitmask too small. Reduce k or n.");
            return;
        }

        System.out.printf("Optimized brute-force: k=%d, n=%d, length=%d, max per digit=%d%n",
                k, n, maxLen, maxDigitCount);

        int[] counts = new int[k];
        int[] current = new int[maxLen];
        long[] checked = {0};
        int[] found = {0};
        long[] lastPrint = {System.currentTimeMillis()};

        bruteHelper(0, maxLen, k, n, total, maxDigitCount, counts, current, found, 0L, checked, lastPrint);

        System.err.printf("\r%-80s\r", "");
        System.err.flush();
        System.out.printf("Explored %,d nodes. Found %d complement-free sequences.%n", checked[0], found[0]);
    }

    private static void bruteHelper(int pos, int maxLen, int k, int n, int total,
                                    int maxDigitCount, int[] counts, int[] current,
                                    int[] found, long seen, long[] checked, long[] lastPrint) {
        // Count every node visited (not just leaves) so the progress bar updates frequently.
        checked[0]++;
        if ((checked[0] & 65535) == 0) {
            long now = System.currentTimeMillis();
            if (now - lastPrint[0] >= 250) {
                System.err.printf("\rNodes: %,d | Found: %d | Depth: %d/%d",
                        checked[0], found[0], pos, maxLen);
                System.err.flush();
                lastPrint[0] = now;
            }
        }

        if (pos == maxLen) {
            // All interior and wrap-around grams were validated during construction.
            char[] chars = new char[maxLen];
            for (int i = 0; i < maxLen; i++) chars[i] = (char) ('0' + current[i]);
            System.out.println(new String(chars));
            found[0]++;
            return;
        }

        // Pair-sum feasibility: for even k each complementary digit-pair (d, k-1-d) must
        // accumulate exactly maxDigitCount total appearances. Prune if either bound is broken.
        if (k % 2 == 0) {
            int remaining = maxLen - pos;
            for (int d = 0; d < k / 2; d++) {
                int pairSum = counts[d] + counts[k - 1 - d];
                if (pairSum > maxDigitCount || maxDigitCount - pairSum > remaining) return;
            }
        }

        for (int d = 0; d < k; d++) {
            if (counts[d] >= maxDigitCount) continue;

            // seen is a value type (long primitive), so copying it is free and
            // no undo is needed when we backtrack.
            long newSeen = seen;

            // Interior gram: the n-gram ending at position pos with digit d.
            // Only exists once we have n consecutive characters (pos >= n-1).
            if (pos >= n - 1) {
                int gram = 0;
                for (int i = pos - n + 1; i < pos; i++) gram = gram * k + current[i];
                gram = gram * k + d;
                int comp = total - 1 - gram;
                if (((newSeen >>> gram) & 1) == 1 || ((newSeen >>> comp) & 1) == 1) continue;
                newSeen |= (1L << gram);
            }

            // Wrap-around grams: only fully determined when placing the last character.
            // For n=2 this is one gram (current[maxLen-1], current[0]);
            // for larger n there are n-1 such grams.
            if (pos == maxLen - 1) {
                boolean invalid = false;
                for (int startPos = maxLen - n + 1; startPos < maxLen; startPos++) {
                    int gram = 0;
                    for (int i = startPos; i < maxLen; i++)
                        gram = gram * k + (i == pos ? d : current[i]);
                    for (int i = 0, wrapLen = startPos + n - maxLen; i < wrapLen; i++)
                        gram = gram * k + current[i];
                    int comp = total - 1 - gram;
                    if (((newSeen >>> gram) & 1) == 1 || ((newSeen >>> comp) & 1) == 1) {
                        invalid = true;
                        break;
                    }
                    newSeen |= (1L << gram);
                }
                if (invalid) continue;
            }

            counts[d]++;
            current[pos] = d;
            bruteHelper(pos + 1, maxLen, k, n, total, maxDigitCount, counts, current, found, newSeen, checked, lastPrint);
            counts[d]--;
        }
    }

    // Generates all complement-free sequences of order n for a k-ary alphabet using
    // the Eulerian graph (De Bruijn graph) method. Traverses the graph via backtracking
    // DFS, only using an edge when neither it nor its complement has already been used.
    // Each cycle found corresponds to a distinct complement-free cyclic sequence.
    // Self-complementary edges (e == k^n - 1 - e, possible when k is odd) are excluded.
    public static void generateAllComplementFree(int k, int n) {
        int total = (int) Math.pow(k, n);
        int nodeCount = (int) Math.pow(k, n - 1);

        // Build De Bruijn graph adjacency lists.
        // Edge e (the n-gram at index e) runs from node (e / k) to node (e % nodeCount).
        @SuppressWarnings("unchecked")
        List<Integer>[] adj = new ArrayList[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            adj[i] = new ArrayList<>();
        }
        for (int e = 0; e < total; e++) {
            if (e == total - 1 - e) continue; // skip self-complementary edges
            adj[e / k].add(e);
        }

        int maxLen = (k % 2 == 0) ? total / 2 : (total - 1) / 2;

        boolean[] edgeUsed = new boolean[total];
        List<Integer> path = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int[] found = {0};
        long[] checked = {0};
        long[] lastPrint = {System.currentTimeMillis()};

        for (int startNode = 0; startNode < nodeCount; startNode++) {
            dfsCF(startNode, startNode, adj, edgeUsed, path, k, n, nodeCount, total, maxLen,
                    seen, found, checked, lastPrint);
        }

        System.err.printf("\r%-80s\r", "");
        System.err.flush();
        System.out.println("Found " + found[0] + " maximal complement-free sequences of order "
                + n + " (length=" + maxLen + ") for k=" + k + ".");
    }

    // Backtracking DFS over the De Bruijn graph. Prints each new sequence immediately
    // as it is found (deduplicated up to cyclic rotation) and updates a progress bar.
    private static void dfsCF(int startNode, int curNode,
                               List<Integer>[] adj, boolean[] edgeUsed,
                               List<Integer> path, int k, int n, int nodeCount, int total,
                               int maxLen, Set<String> seen, int[] found,
                               long[] checked, long[] lastPrint) {
        checked[0]++;
        if ((checked[0] & 65535) == 0) {
            long now = System.currentTimeMillis();
            if (now - lastPrint[0] >= 250) {
                System.err.printf("\rNodes: %,d | Found: %d | Path: %d/%d",
                        checked[0], found[0], path.size(), maxLen);
                System.err.flush();
                lastPrint[0] = now;
            }
        }

        if (path.size() == maxLen && curNode == startNode) {
            String seq = cfPathToSequence(path, k, n);
            String canonical = cfCanonicalize(seq);
            if (seen.add(canonical)) {
                // Clear the progress line so the sequence prints cleanly, then reprint bar.
                System.err.printf("\r%-80s\r", "");
                System.err.flush();
                System.out.println(seq);
                found[0]++;
            }
            return; // maximal length reached, no point extending further
        }

        for (int edge : adj[curNode]) {
            if (edgeUsed[edge]) continue;
            int compEdge = total - 1 - edge;
            if (edgeUsed[compEdge]) continue; // complement already used

            int nextNode = edge % nodeCount;
            edgeUsed[edge] = true;
            path.add(edge);

            dfsCF(startNode, nextNode, adj, edgeUsed, path, k, n, nodeCount, total, maxLen,
                    seen, found, checked, lastPrint);

            path.remove(path.size() - 1);
            edgeUsed[edge] = false;
        }
    }

    // Converts a path of edge indices to a cyclic sequence string.
    // Each edge contributes the first character of its n-gram; the last n-1 characters
    // are implied by the wrap-around.
    private static String cfPathToSequence(List<Integer> path, int k, int n) {
        StringBuilder sb = new StringBuilder();
        for (int edge : path) {
            sb.append(indexToString(edge, k, n).charAt(0));
        }
        return sb.toString();
    }

    // Returns the lexicographically smallest rotation of s (canonical form for cyclic sequences).
    private static String cfCanonicalize(String s) {
        String min = s;
        for (int i = 1; i < s.length(); i++) {
            String rot = s.substring(i) + s.substring(0, i);
            if (rot.compareTo(min) < 0) min = rot;
        }
        return min;
    }

    public static String complementString(String s, int k) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            int digit = c - '0';
            int compDigit = k - 1 - digit;
            sb.append(compDigit);
        }
        return sb.toString();
    }

    public static String indexToString(int idx, int k, int n) {
        char[] result = new char[n];
        for (int i = n - 1; i >= 0; i--) {
            result[i] = (char) ('0' + (idx % k));
            idx /= k;
        }
        return new String(result);
    }

    public static int stringToIndex(String s, int k) {
        int idx = 0;
        for (char c : s.toCharArray()) {
            idx = idx * k + (c - '0');
        }
        return idx;
    }   
}
