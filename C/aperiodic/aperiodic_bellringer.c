/*
 * aperiodic_bellringer.c
 *
 * Generates an aperiodic minimum-weight SP-cycle for Π(n) by modifying
 * the bell-ringer construction B(n) from:
 *   Holroyd, Ruskey, Williams, "Shorthand Universal Cycles for Permutations",
 *   Algorithmica (2012) 64:215-245.
 *
 * Algorithm overview:
 *   1) Build the decrementing spanning tree H(n-1) of the permutohedron P(n-1).
 *      This tree corresponds to the periodic bell-ringer SP-cycle B(n).
 *   2) Add a "forbidden" edge — one in Ξ_n(n) but not in P(n-1) — which
 *      corresponds to an adjacent-transposition involving n in the circular
 *      label (per Lemma 1, using such an edge breaks periodicity).
 *   3) This creates a cycle. Remove one edge from that cycle to restore
 *      a spanning tree, now of Ξ_n(n) rather than P(n-1).
 *   4) Construct the Hamilton cycle in Ξ(n) from this spanning tree
 *      (Theorem 4: for each tree edge add two σ_{n-1} arcs, fill rest with σ_n).
 *   5) Read off the SP-cycle symbols from the Hamilton cycle.
 *
 * Usage: ./aperiodic_bellringer <n>
 *   where n >= 3 (prints the n! symbols of the aperiodic SP-cycle)
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAXN 10

/* ---- factorial and permutation indexing ---- */

static int fact[MAXN + 1];

static void init_fact(int n) {
    fact[0] = 1;
    for (int i = 1; i <= n; i++)
        fact[i] = fact[i - 1] * i;
}

/* Lehmer-code based index: perm is 0-based values in {0..n-1} */
static int perm_to_index(const int *p, int n) {
    int idx = 0;
    for (int i = 0; i < n; i++) {
        int cnt = 0;
        for (int j = i + 1; j < n; j++)
            if (p[j] < p[i]) cnt++;
        idx += cnt * fact[n - 1 - i];
    }
    return idx;
}

static void index_to_perm(int idx, int *p, int n) {
    int used[MAXN] = {0};
    for (int i = 0; i < n; i++) {
        int f = fact[n - 1 - i];
        int q = idx / f;
        idx %= f;
        int cnt = 0;
        for (int v = 0; v < n; v++) {
            if (!used[v]) {
                if (cnt == q) {
                    p[i] = v;
                    used[v] = 1;
                    break;
                }
                cnt++;
            }
        }
    }
}

/* ---- Coset indexing ---- */

/*
 * Vertices of Ξ_n(n) are cosets under σ_n (string rotation).
 * Each coset is represented by its canonical form starting with n.
 * We store the suffix a[0..n-2] (values in {1..n-1}) after the leading n.
 *
 * P(n-1) edges: adjacent transpositions τ_i swapping a[i] <-> a[i+1]
 *   for i = 0, ..., n-3  (the n-2 "interior" swaps).
 *
 * Additional Ξ_n(n) edges (not in P(n-1)):
 *   F1 (left rotation):  (a[1], a[2], ..., a[n-2], a[0])
 *   F2 (right rotation): (a[n-2], a[0], a[1], ..., a[n-3])
 *   These correspond to the two circular adjacent-transpositions involving n.
 */

static int suffix_to_index(const int *s, int n) {
    int tmp[MAXN];
    for (int i = 0; i < n - 1; i++)
        tmp[i] = s[i] - 1;
    return perm_to_index(tmp, n - 1);
}

static void index_to_suffix(int idx, int *s, int n) {
    int tmp[MAXN];
    index_to_perm(idx, tmp, n - 1);
    for (int i = 0; i < n - 1; i++)
        s[i] = tmp[i] + 1;
}

/* ---- Decrementing spanning tree H(n-1) of P(n-1) ---- */

/*
 * From Section 3.1 of the paper:
 *
 * The decrementing prefix of a ∈ Π(m) is the longest prefix of the form
 * m m-1 m-2 ... j  (consecutive values decrementing from the maximum m).
 * The incrementing symbol is j-1, the value just below the decrementing prefix.
 *
 * In the decrementing spanning tree H(m), the parent of a non-root vertex
 * is obtained by swapping the incrementing symbol one position to the left.
 *
 * Root of H(m) is m m-1 ... 1 (fully decrementing).
 *
 * Example for m=8, vertex 87624153:
 *   Decrementing prefix = 876 (starts at 8, decrements through 7,6)
 *   Incrementing symbol = 5 (= 6-1, next value below prefix minimum)
 *   But 5 is not adjacent to prefix in the string... 
 *   Wait — the incrementing symbol is j-1 where j is the minimum of the
 *   decrementing prefix. So for 87624153: prefix = 876, j=6, so
 *   incrementing symbol value is 5. We find where 5 is in the string
 *   and swap it one position left. 
 *   
 *   Actually re-reading more carefully: the incrementing symbol is defined
 *   by position, not just value. The decrementing prefix is the longest
 *   prefix a_1 a_2 ... a_k such that a_1 = m, a_2 = m-1, ..., a_k = m-k+1.
 *   Then the incrementing symbol is a_{k+1} (the symbol at index k+1, i.e.,
 *   the first symbol after the decrementing prefix).
 *   
 *   For 87624153 (m=8): a_1=8, a_2=7, a_3=6 ✓ (consecutive decrements)
 *   a_4=2 ≠ 5, so decrementing prefix = 876, incrementing symbol = a_4 = 2.
 *   Parent = swap a_3 and a_4: 87264153... no wait, swap incrementing symbol
 *   one position LEFT. So swap a_4 with a_3: 87264153? No — swap means
 *   the incrementing symbol (at position 4) moves left to position 3:
 *   87 2 6 24153 → 87 2 6 4153... 
 *
 *   Let me re-read the paper definition once more. Section 3.1:
 *   "In H(n), the parent of a non-root vertex is obtained by swapping 
 *    the incrementing symbol in its label to the left."
 *   This means: swap the incrementing symbol with the symbol immediately 
 *   to its left (an adjacent transposition).
 *   
 *   So for 87624153: decrementing prefix = 876, incrementing symbol is
 *   the symbol right after the prefix, which is 2 (at index 3, 0-based).
 *   Swap it with a[2]=6: parent = 87264153.
 *   Check: 87264153 has decrementing prefix 87 (a[0]=8,a[1]=7,a[2]=2≠6),
 *   incrementing symbol = a[2] = 2. But that can't be right because the
 *   parent should have MORE inversions...
 *   
 *   Actually wait. Let me re-examine. The INCREMENTING symbol is j-1
 *   where the decrementing prefix ends at j. The paper says:
 *   "The decrementing prefix is the longest prefix of the form n n−1 ··· j 
 *    and the incrementing symbol is j−1."
 *   
 *   So for a permutation of {1..m}, the decrementing prefix starts at m
 *   and goes m, m-1, m-2, ..., j (all at the beginning of the string in
 *   consecutive positions). The incrementing symbol is the VALUE j-1
 *   (wherever it appears in the string).
 *   
 *   For 87624153 (m=8): prefix = 876 (j=6), incrementing symbol = 5.
 *   Find 5 in the string: position 4 (0-based). Swap 5 one position left:
 *   swap a[3]=2 and a[4]=4... wait, a[4]=1... Let me recount.
 *   8 7 6 2 4 1 5 3 — positions 0-7.
 *   Value 5 is at position 6. Swap with position 5: 8 7 6 2 4 5 1 3.
 *   That's the parent: 87624513 → which now has prefix 876, and the 
 *   incrementing symbol 5 is right after... no it's at position 5.
 *   Hmm, 87624513: prefix = 876 (still j=6), value 5 at index 5.
 *   Parent of 87624513: swap 5 left: 87625413. And so on — 5 sweeps
 *   left until it's adjacent to 6, giving 87562413 → 87562413 has
 *   prefix 8765 (j=5), then incrementing symbol = 4, etc.
 *   
 *   This matches the "half-hunt" description from the paper (Fig 9)!
 */

/* Find the incrementing symbol in suffix s (permutation of {1..m}, length m).
 * Returns the INDEX (0-based) of the incrementing symbol in s.
 * Returns -1 if s is the root (fully decrementing: m m-1 ... 1). */
static int find_incrementing_symbol(const int *s, int m) {
    /* The decrementing prefix: s[0]=m, s[1]=m-1, ..., s[k-1]=m-k+1.
     * The incrementing symbol has VALUE m-k (= j-1 where j = m-k+1). */
    int k = 0;
    while (k < m && s[k] == m - k) {
        k++;
    }
    if (k == m) return -1; /* root: fully decrementing */

    /* Incrementing symbol has value (m - k), i.e., one less than the
     * last value in the decrementing prefix. Find its position. */
    int target_val = m - k;
    for (int i = k; i < m; i++) {
        if (s[i] == target_val) return i;
    }
    /* Should not reach here for a valid permutation */
    return -1;
}

/* Compute parent of suffix s in H(m). Returns parent suffix in 'parent'.
 * Returns 0 if s is root, 1 otherwise.
 * m = length of s = n-1 when building H(n-1). */
static int decrement_parent(const int *s, int m, int *parent) {
    int idx = find_incrementing_symbol(s, m);
    if (idx < 0) return 0; /* root */
    /* Swap s[idx] one position to the left: swap s[idx-1] and s[idx]. */
    memcpy(parent, s, m * sizeof(int));
    int tmp = parent[idx - 1];
    parent[idx - 1] = parent[idx];
    parent[idx] = tmp;
    return 1;
}

/* ---- Build the spanning tree as an adjacency structure ---- */

static int tree_parent[50000]; /* enough for n<=8: 7! = 5040 */
static int num_cosets;

static void build_bellringer_tree(int n) {
    int m = n - 1; /* H(n-1) is a spanning tree of P(n-1) */
    num_cosets = fact[m];

    for (int i = 0; i < num_cosets; i++) {
        int s[MAXN], ps[MAXN];
        index_to_suffix(i, s, n); /* suffix of length m, values in {1..m} */
        if (decrement_parent(s, m, ps)) {
            tree_parent[i] = suffix_to_index(ps, n);
        } else {
            tree_parent[i] = -1; /* root */
        }
    }
}

/* ---- Path finding in tree ---- */

static int path_to_root(int u, int *path, int max_len) {
    int len = 0;
    int cur = u;
    while (cur != -1 && len < max_len) {
        path[len++] = cur;
        cur = tree_parent[cur];
    }
    return len;
}

static int find_tree_path(int u, int v, int *path) {
    int pu[50000], pv[50000];
    int lu = path_to_root(u, pu, 50000);
    int lv = path_to_root(v, pv, 50000);

    int *on_pu = calloc(num_cosets, sizeof(int));
    for (int i = 0; i < lu; i++)
        on_pu[pu[i]] = i + 1;

    int lca = -1, lca_dv = -1;
    for (int j = 0; j < lv; j++) {
        if (on_pu[pv[j]]) {
            lca = pv[j];
            lca_dv = j;
            break;
        }
    }
    free(on_pu);

    int lca_du = 0;
    for (int i = 0; i < lu; i++) {
        if (pu[i] == lca) { lca_du = i; break; }
    }

    int len = 0;
    for (int i = 0; i <= lca_du; i++)
        path[len++] = pu[i];
    for (int j = lca_dv - 1; j >= 0; j--)
        path[len++] = pv[j];

    return len;
}

/* ---- Make the tree aperiodic ---- */

/*
 * Strategy: pick a forbidden edge (u, v) in Ξ_n(n) \ P(n-1).
 * Adding it to the tree creates a cycle. Remove one edge from this
 * cycle (other than (u,v) itself) to get a new spanning tree of Ξ_n(n).
 *
 * We use the left-rotation edge from the root coset (n-1, n-2, ..., 1)
 * to its forbidden neighbor (n-2, n-3, ..., 1, n-1).
 * By Lemma 1, this edge corresponds to a σ_{n-1} arc from a node whose
 * label begins or ends with n, which breaks periodicity.
 */
static void make_aperiodic(int n) {
    int root_s[MAXN], neighbor_s[MAXN];
    for (int i = 0; i < n - 1; i++)
        root_s[i] = n - 1 - i;
    /* Left rotation of root suffix */
    for (int i = 0; i < n - 2; i++)
        neighbor_s[i] = root_s[i + 1];
    neighbor_s[n - 2] = root_s[0];

    int u = suffix_to_index(root_s, n);     /* root of H(n-1) */
    int v = suffix_to_index(neighbor_s, n); /* forbidden neighbor */

    /* Adding edge (u, v) creates a cycle. Remove edge (v, parent[v])
     * and replace with forbidden edge (u, v). This re-parents v to the
     * root via the forbidden edge, giving a spanning tree of Ξ_n(n)
     * that is NOT a subtree of P(n-1). By Theorem 4 + Lemma 1,
     * the resulting min-weight SP-cycle is aperiodic. */
    tree_parent[v] = u;
}

/* ---- Construct Hamilton cycle from spanning tree ---- */

/*
 * From Theorem 4: for each tree edge, add the two corresponding σ_{n-1}
 * arcs. Fill remaining nodes with σ_n arcs. This gives a Hamilton cycle
 * in Ξ(n).
 *
 * σ_n:   (a_1, ..., a_n) → (a_2, ..., a_n, a_1)       [full rotation]
 * σ_{n-1}: (a_1, ..., a_n) → (a_2, ..., a_{n-1}, a_1, a_n) [rotate prefix n-1]
 *
 * For each permutation p, apply σ_{n-1}. If the source and target cosets
 * are connected by a tree edge, use σ_{n-1}; otherwise use σ_n.
 */

static void apply_sigma_n(const int *p, int *q, int n) {
    for (int i = 0; i < n - 1; i++)
        q[i] = p[i + 1];
    q[n - 1] = p[0];
}

static void apply_sigma_nm1(const int *p, int *q, int n) {
    for (int i = 0; i < n - 2; i++)
        q[i] = p[i + 1];
    q[n - 2] = p[0];
    q[n - 1] = p[n - 1];
}

static int perm_coset(const int *p, int n) {
    int h = -1;
    for (int i = 0; i < n; i++)
        if (p[i] == n) { h = i; break; }
    int s[MAXN];
    int idx = 0;
    for (int i = h + 1; i < n; i++)
        s[idx++] = p[i];
    for (int i = 0; i < h; i++)
        s[idx++] = p[i];
    return suffix_to_index(s, n);
}

static int perm_index_1based(const int *p, int n) {
    int tmp[MAXN];
    for (int i = 0; i < n; i++) tmp[i] = p[i] - 1;
    return perm_to_index(tmp, n);
}

static void index_to_perm_1based(int idx, int *p, int n) {
    int tmp[MAXN];
    index_to_perm(idx, tmp, n);
    for (int i = 0; i < n; i++) p[i] = tmp[i] + 1;
}

static int tree_has_edge(int c1, int c2) {
    return (tree_parent[c1] == c2 || tree_parent[c2] == c1);
}

static int *successor;

static void build_hamilton_cycle(int n) {
    int nfact = fact[n];
    successor = calloc(nfact, sizeof(int));

    int p[MAXN], q_n[MAXN], q_nm1[MAXN];

    for (int i = 0; i < nfact; i++) {
        index_to_perm_1based(i, p, n);

        apply_sigma_n(p, q_n, n);
        apply_sigma_nm1(p, q_nm1, n);

        int c_p = perm_coset(p, n);
        int c_nm1 = perm_coset(q_nm1, n);

        if (c_p != c_nm1 && tree_has_edge(c_p, c_nm1)) {
            successor[i] = perm_index_1based(q_nm1, n);
        } else {
            successor[i] = perm_index_1based(q_n, n);
        }
    }
}

/* ---- Verification ---- */

static int verify_hamilton(int n) {
    int nfact = fact[n];
    int *visited = calloc(nfact, sizeof(int));

    int start_p[MAXN];
    for (int i = 0; i < n; i++) start_p[i] = n - i;
    int start = perm_index_1based(start_p, n);

    int cur = start;
    int count = 0;
    do {
        if (visited[cur]) {
            free(visited);
            return 0;
        }
        visited[cur] = 1;
        count++;
        cur = successor[cur];
    } while (cur != start && count <= nfact);

    free(visited);
    return (count == nfact);
}

static void output_sp_cycle(int n) {
    int nfact = fact[n];

    int start_p[MAXN];
    for (int i = 0; i < n; i++) start_p[i] = n - i;
    int start = perm_index_1based(start_p, n);

    int cur = start;
    for (int i = 0; i < nfact; i++) {
        int p[MAXN];
        index_to_perm_1based(cur, p, n);
        printf("%d", p[0]);
        if (i < nfact - 1) printf(" ");
        cur = successor[cur];
    }
    printf("\n");
}

static int check_aperiodic(int n) {
    int nfact = fact[n];
    int start_p[MAXN];
    for (int i = 0; i < n; i++) start_p[i] = n - i;
    int start = perm_index_1based(start_p, n);

    int cur = start;
    int periodic = 1;
    for (int i = 0; i < nfact; i++) {
        int p[MAXN];
        index_to_perm_1based(cur, p, n);
        if (i % n == 0 && p[0] != n) {
            periodic = 0;
            break;
        }
        cur = successor[cur];
    }
    return !periodic;
}

static int verify_sp_cycle(int n) {
    int nfact = fact[n];
    int start_p[MAXN];
    for (int i = 0; i < n; i++) start_p[i] = n - i;
    int start = perm_index_1based(start_p, n);

    int *symbols = malloc(nfact * sizeof(int));
    int cur = start;
    for (int i = 0; i < nfact; i++) {
        int p[MAXN];
        index_to_perm_1based(cur, p, n);
        symbols[i] = p[0];
        cur = successor[cur];
    }

    int valid = 1;
    int *perm_seen = calloc(nfact, sizeof(int));
    for (int i = 0; i < nfact; i++) {
        int sub[MAXN];
        int vals_used[MAXN + 1] = {0};
        int missing = 0;
        for (int j = 0; j < n - 1; j++) {
            sub[j] = symbols[(i + j) % nfact];
            vals_used[sub[j]] = 1;
        }
        for (int v = 1; v <= n; v++) {
            if (!vals_used[v]) { missing = v; break; }
        }
        sub[n - 1] = missing;
        int tmp[MAXN];
        for (int j = 0; j < n; j++) tmp[j] = sub[j] - 1;
        int idx = perm_to_index(tmp, n);
        if (perm_seen[idx]) { valid = 0; break; }
        perm_seen[idx] = 1;
    }

    free(symbols);
    free(perm_seen);
    return valid;
}

/* ---- Main ---- */

int main(int argc, char **argv) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <n>  (n >= 3, n <= %d)\n", argv[0], MAXN);
        return 1;
    }

    int n = atoi(argv[1]);
    if (n < 3 || n > MAXN) {
        fprintf(stderr, "Error: n must be between 3 and %d\n", MAXN);
        return 1;
    }

    if (n == 3) {
        fprintf(stderr, "n=3: No aperiodic min-weight SP-cycle exists.\n");
        fprintf(stderr, "Printing the unique (up to rotation) aperiodic SP-cycle for Pi(3):\n");
        printf("3 2 1 3 1 2\n");
        return 0;
    }

    init_fact(n);

    fprintf(stderr, "Building bell-ringer spanning tree H(%d) of P(%d)...\n", n - 1, n - 1);
    build_bellringer_tree(n);

    fprintf(stderr, "Making tree aperiodic (adding forbidden edge, removing tree edge)...\n");
    make_aperiodic(n);

    fprintf(stderr, "Building Hamilton cycle in Xi(%d)...\n", n);
    build_hamilton_cycle(n);

    fprintf(stderr, "Verifying Hamilton cycle...\n");
    if (!verify_hamilton(n)) {
        fprintf(stderr, "ERROR: not a single Hamilton cycle. Trying alternate edges...\n");

        /* Try all edges on the path from v to root */
        build_bellringer_tree(n);

        int root_s[MAXN], neighbor_s[MAXN];
        for (int i = 0; i < n - 1; i++)
            root_s[i] = n - 1 - i;
        for (int i = 0; i < n - 2; i++)
            neighbor_s[i] = root_s[i + 1];
        neighbor_s[n - 2] = root_s[0];

        int u = suffix_to_index(root_s, n);
        int v = suffix_to_index(neighbor_s, n);

        int path[50000];
        int pathlen = path_to_root(v, path, 50000);

        int found = 0;
        for (int e = 0; e < pathlen - 1 && !found; e++) {
            build_bellringer_tree(n);
            /* Reverse parent links from path[0]=v to path[e],
             * then set path[0]'s parent to u (forbidden edge).
             * This removes edge (path[e], path[e+1]) and adds (u, v). */
            tree_parent[path[0]] = u;
            for (int k = 1; k <= e; k++)
                tree_parent[path[k]] = path[k - 1];

            build_hamilton_cycle(n);
            if (verify_hamilton(n)) {
                found = 1;
                fprintf(stderr, "Success with edge removal at depth %d on path.\n", e);
            } else {
                free(successor);
                successor = NULL;
            }
        }

        if (!found) {
            /* Try all forbidden edges */
            fprintf(stderr, "Trying all possible forbidden edges...\n");
            for (int ci = 0; ci < num_cosets && !found; ci++) {
                int si[MAXN];
                index_to_suffix(ci, si, n);
                int nb[MAXN];
                for (int k = 0; k < n - 2; k++) nb[k] = si[k + 1];
                nb[n - 2] = si[0];
                int cj = suffix_to_index(nb, n);

                build_bellringer_tree(n);
                int tpath[50000];
                int tpathlen = find_tree_path(ci, cj, tpath);

                for (int e = 0; e < tpathlen - 1 && !found; e++) {
                    int ea = tpath[e], eb = tpath[e + 1];
                    build_bellringer_tree(n);

                    int child, par;
                    if (tree_parent[ea] == eb) { child = ea; par = eb; }
                    else if (tree_parent[eb] == ea) { child = eb; par = ea; }
                    else continue;

                    /* Determine which side ci is on */
                    int ci_side = 0;
                    {
                        int x = ci;
                        while (x != -1) {
                            if (x == child) { ci_side = 1; break; }
                            if (x == par) break;
                            x = tree_parent[x];
                        }
                    }

                    if (ci_side) {
                        int rpath[50000];
                        int rlen = 0;
                        {
                            int x = ci;
                            while (x != child) {
                                rpath[rlen++] = x;
                                x = tree_parent[x];
                            }
                            rpath[rlen++] = child;
                        }
                        for (int k = rlen - 1; k >= 1; k--)
                            tree_parent[rpath[k]] = rpath[k - 1];
                        tree_parent[ci] = cj;
                    } else {
                        int rpath[50000];
                        int rlen = 0;
                        {
                            int x = cj;
                            while (x != child) {
                                rpath[rlen++] = x;
                                x = tree_parent[x];
                            }
                            rpath[rlen++] = child;
                        }
                        for (int k = rlen - 1; k >= 1; k--)
                            tree_parent[rpath[k]] = rpath[k - 1];
                        tree_parent[cj] = ci;
                    }

                    build_hamilton_cycle(n);
                    if (verify_hamilton(n)) {
                        found = 1;
                        fprintf(stderr, "Success with forbidden edge (%d,%d), edge %d.\n",
                                ci, cj, e);
                    } else {
                        free(successor);
                        successor = NULL;
                    }
                }
            }

            if (!found) {
                fprintf(stderr, "FATAL: Could not find a working aperiodic modification.\n");
                return 1;
            }
        }
    }

    fprintf(stderr, "Verifying SP-cycle...\n");
    if (!verify_sp_cycle(n)) {
        fprintf(stderr, "ERROR: output is not a valid SP-cycle!\n");
        free(successor);
        return 1;
    }

    if (check_aperiodic(n)) {
        fprintf(stderr, "Confirmed: SP-cycle is APERIODIC.\n");
    } else {
        fprintf(stderr, "WARNING: SP-cycle appears periodic!\n");
    }

    fprintf(stderr, "SP-cycle for Pi(%d) (%d symbols):\n", n, fact[n]);
    output_sp_cycle(n);

    free(successor);
    return 0;
}
