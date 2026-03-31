package aperiodic;

import java.util.*;

public class AperiodicCoollex {

	static class Perm {
        int[] p;

        Perm(int[] p) {
            this.p = p.clone();
        }

        @Override
        public boolean equals(Object o) {
            return Arrays.equals(p, ((Perm) o).p);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(p);
        }

        @Override
        public String toString() {
            return Arrays.toString(p);
        }
    }

    // rotate so n is first
    static Perm normalize(int[] perm, int n) {
        int idx = -1;
        for (int i = 0; i < perm.length; i++) {
            if (perm[i] == n) idx = i;
        }

        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            res[i] = perm[(idx + i) % n];
        }
        return new Perm(res);
    }

    static List<int[]> generateBasePermutations(int n) {
        List<int[]> res = new ArrayList<>();
        gen(n, res, new int[n - 1], new boolean[n + 1], 0);
        return res;
    }

    static void gen(int n, List<int[]> res, int[] curr, boolean[] used, int idx) {
        if (idx == n - 1) {
            res.add(curr.clone());
            return;
        }
        for (int i = 1; i <= n; i++) {
            if (i == n || used[i]) continue;
            used[i] = true;
            curr[idx] = i;
            gen(n, res, curr, used, idx + 1);
            used[i] = false;
        }
    }

    static List<Perm> buildVertices(int n) {
        List<int[]> base = generateBasePermutations(n);
        List<Perm> vertices = new ArrayList<>();
        for (int[] b : base) {
            int[] full = new int[n];
            full[0] = n;
            System.arraycopy(b, 0, full, 1, n - 1);
            vertices.add(new Perm(full));
        }
        return vertices;
    }

    static List<Perm> neighbors(Perm v, int n) {
        List<Perm> res = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            int[] copy = v.p.clone();
            int tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
            res.add(normalize(copy, n));
        }
        return res;
    }

    static Map<Perm, Perm> buildTree(List<Perm> vertices, int n) {
        Map<Perm, Perm> parent = new HashMap<>();
        Queue<Perm> q = new LinkedList<>();

        Perm root = vertices.get(0);
        q.add(root);
        parent.put(root, null);

        while (!q.isEmpty()) {
            Perm u = q.poll();
            for (Perm v : neighbors(u, n)) {
                if (!parent.containsKey(v)) {
                    parent.put(v, u);
                    q.add(v);
                }
            }
        }
        return parent;
    }

    // DFS traversal to build SP-cycle
    static void dfsSP(Perm u, Map<Perm, List<Perm>> adj, Set<Perm> visited, StringBuilder sb) {
        visited.add(u);
        sb.append(u.p[u.p.length - 1]); // last element of permutation
        if (adj.containsKey(u)) {
            for (Perm v : adj.get(u)) {
                if (!visited.contains(v)) {
                    dfsSP(v, adj, visited, sb);
                }
            }
        }
    }

    static String buildSPCycle(Map<Perm, Perm> tree) {
        // Build adjacency list
        Map<Perm, List<Perm>> adj = new HashMap<>();
        for (Map.Entry<Perm, Perm> e : tree.entrySet()) {
            Perm child = e.getKey();
            Perm par = e.getValue();
            if (par != null) {
                adj.computeIfAbsent(par, k -> new ArrayList<>()).add(child);
                adj.computeIfAbsent(child, k -> new ArrayList<>()).add(par);
            }
        }

        // DFS traversal to collect last elements
        StringBuilder sb = new StringBuilder();
        Set<Perm> visited = new HashSet<>();
        Perm root = tree.keySet().iterator().next();
        dfsSP(root, adj, visited, sb);
        return sb.toString();
    }

    public static void main(String[] args) {
        int n = 5;
        List<Perm> vertices = buildVertices(n);
        Map<Perm, Perm> tree = buildTree(vertices, n);

        // Optional: inject forbidden edge for aperiodicity (as before)
        Perm v = vertices.get(0);
        Perm w = neighbors(v, n).get(n - 1); // single violation
        tree.put(w, v); // add forbidden edge

        String spCycle = buildSPCycle(tree);
        System.out.println("Aperiodic SP-cycle (length " + spCycle.length() + "):");
        System.out.println(spCycle);
    }
}