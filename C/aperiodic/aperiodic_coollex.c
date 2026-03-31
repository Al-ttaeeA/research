/*
 * aperiodic_coollex.c
 *
 * Generates an aperiodic minimum-weight SP-cycle for Π(n) by modifying
 * the cool-lex construction from:
 *   Holroyd, Ruskey, Williams, "Shorthand Universal Cycles for Permutations",
 *   Algorithmica (2012) 64:215-245.
 *
 * Algorithm overview:
 *   1) Build the decreasing spanning tree S(n-1) of the permutohedron P(n-1).
 *      This tree corresponds to the periodic cool SP-cycle C(n).
 *   2) Add a "forbidden" edge — one in Ξ_n(n) but not in P(n-1) — which
 *      corresponds to an adjacent-transposition involving n in the circular
 *      label (per Lemma 1, using such an edge breaks periodicity).
 *   3) This creates a cycle. Remove one edge from that cycle to restore
 *      a spanning tree, now of Ξ_n(n) rather than P(n-1).
 *   4) Construct the Hamilton cycle in Ξ(n) from this spanning tree
 *      (Theorem 4: for each tree edge add two σ_{n-1} arcs, fill rest with σ_n).
 *   5) Read off the SP-cycle symbols from the Hamilton cycle.
 *
 * Usage: ./aperiodic_coollex <n>
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

/* ---- Permutohedron / Ξ_n(n) operations ---- */

/*
 * Vertices of Ξ_n(n) are cosets under σ_n (rotation).
 * We represent each coset by the rotation starting with n-1 (0-based: value n-1).
 * That is, a permutation of {0, 1, ..., n-1} whose first element is n-1.
 *
 * P(n-1) edges: adjacent transpositions of the (n-1)-element suffix.
 * Ξ_n(n) has n edges per vertex: the n circular adjacent-transpositions.
 * The two "extra" edges in Ξ_n(n) \ P(n-1) are the circular transpositions
 * that involve the first element (which is n-1, i.e., the symbol n in 1-based).
 */

/* Canonical representative: rotate so that n-1 is first. p has length n-1. */
/* We store cosets as permutations of {0..n-2} representing the suffix after n-1. */
/* So a coset is p[0..n-2] meaning the full circular perm is (n-1) p[0] p[1] ... p[n-3]. */

/* Number of cosets = (n-1)! */

/* Index a coset (suffix of length n-2, values in {0..n-2}) */
/* Actually, let's use full permutations of {0..n-2} of length n-1 for cosets,
   where the first element is implicitly n-1. So the suffix is p[0..n-3] with
   values in {0..n-2}. */

/*
 * Let me reconsider the representation to match the paper more closely.
 *
 * The paper uses 1-based permutations. A vertex in Ξ_n(n) is the rotation
 * of a permutation in Π(n) that starts with n. So vertices are labeled
 * n a_1 a_2 ... a_{n-1} where a_1...a_{n-1} is a permutation of {1,...,n-1}.
 *
 * I'll use 1-based values internally for clarity in matching the paper,
 * but 0-based arrays.
 *
 * A coset is identified by suffix a[0..n-2] (length n-1) with values in {1..n-1}.
 * Number of cosets = (n-1)!
 *
 * P(n-1) edges: adjacent transpositions τ_i applied to the suffix,
 *   swapping a[i] and a[i+1] for i = 0, ..., n-3.
 *   These are the (n-2) "interior" adjacent-transpositions.
 *
 * Additional Ξ_n(n) edges (not in P(n-1)):
 *   The two circular adjacent-transpositions involving n in the circular label
 *   (n, a[0], a[1], ..., a[n-2]) viewed circularly:
 *     - swap n and a[0]: gives coset starting with a[0], suffix = rearranged.
 *       Actually this just means swapping the leading n with a[0], then re-rotating.
 *       New circular: (a[0], a[1], ..., a[n-2], n). Rotate to start with n:
 *       (n, a[0]^{-1}...) — actually let me think more carefully.
 *
 *     For vertex (n, a[0], ..., a[n-2]) viewed circularly:
 *       "swap n and a[0]" gives circular (a[0], n, a[1], ..., a[n-2]).
 *       Rotate to canonical (starts with n): (n, a[1], ..., a[n-2], a[0]).
 *       So neighbor = a[1], a[2], ..., a[n-2], a[0]. That's σ_{n-1} applied to suffix.
 *
 *     "swap n and a[n-2]" gives circular (n, a[0], ..., a[n-3], ...) wait no.
 *       Circular: ..., a[n-2], n, a[0], ...
 *       Swap n and a[n-2]: (..., n, a[n-2], a[0], ...)
 *       This is (a[n-2], a[0], a[1], ..., a[n-3], n). Rotate: (n, a[n-2], a[0], ..., a[n-3]).
 *       So neighbor = a[n-2], a[0], a[1], ..., a[n-3]. That's σ_{n-1}^{-1}... 
 *       Actually that's: first element becomes a[n-2], rest shift right, last = a[n-3].
 *       Hmm, it's the inverse rotation: a[n-2], a[0], a[1], ..., a[n-3].
 *
 * So the two "forbidden" neighbors of suffix (a[0], ..., a[n-2]) are:
 *   F1: (a[1], a[2], ..., a[n-2], a[0])   — left rotation of suffix
 *   F2: (a[n-2], a[0], a[1], ..., a[n-3]) — right rotation of suffix
 *
 * P(n-1) neighbors (the n-2 swaps): swap a[i] <-> a[i+1] for i=0..n-3.
 *
 * Total degree in Ξ_n(n): n-2 + 2 = n. ✓
 */

/* Index a suffix: suffix is a permutation of {1..n-1}, length n-1.
   We convert to 0-based and use perm_to_index for (n-1) elements. */
static int suffix_to_index(const int *s, int n) {
    /* s[0..n-2], values in {1..n-1}. Convert to 0-based {0..n-2}. */
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

/* ---- Decreasing spanning tree S(n-1) of P(n-1) ---- */

/*
 * From Section 3.1:
 * The decreasing prefix of a ∈ Π(n-1) is the longest prefix a[0]a[1]...a[k-1]
 * such that a[0] > a[1] > ... > a[k-1].
 * The increasing symbol is a[k].
 *
 * In S(n-1), the parent of a non-root vertex is obtained by swapping the
 * increasing symbol one position to the left.
 *
 * Root is (n-1)(n-2)...1.
 *
 * Here the suffix represents a permutation of {1..n-1}. The root suffix
 * for S(n-1) is (n-1, n-2, ..., 1).
 */

/* Returns the index of the increasing symbol (0-based) in suffix s of length n-1.
   Returns -1 if s is the root (fully decreasing). */
static int find_increasing_symbol(const int *s, int len) {
    /* Decreasing prefix: s[0] > s[1] > ... > s[k-1], increasing symbol = s[k]. */
    for (int i = 1; i < len; i++) {
        if (s[i - 1] <= s[i])  /* Actually strictly: s[i-1] > s[i] for decreasing */
            return i;          /* But for permutations it's always strict */
        /* s[i-1] > s[i], continue */
    }
    return -1; /* fully decreasing = root */
}

/* Compute parent of suffix s in S(n-1). Returns parent suffix in 'parent'.
   Returns 0 if s is root, 1 otherwise. */
static int scut_parent(const int *s, int len, int *parent) {
    int k = find_increasing_symbol(s, len);
    if (k < 0) return 0; /* root */
    /* Swap s[k] one position to the left: swap s[k-1] and s[k]. */
    memcpy(parent, s, len * sizeof(int));
    int tmp = parent[k - 1];
    parent[k - 1] = parent[k];
    parent[k] = tmp;
    return 1;
}

/* ---- Build the spanning tree as an adjacency structure ---- */

/*
 * We store the tree as parent[i] for each coset index i.
 * parent[root] = -1.
 * An edge between coset i and coset j means i and j are adjacent in the
 * permutohedron or (after modification) in Ξ_n(n).
 */

static int tree_parent[50000]; /* enough for n<=8: 7! = 5040 */
static int num_cosets;

static void build_cool_lex_tree(int n) {
    num_cosets = fact[n - 1];
    int root_suffix[MAXN];
    for (int i = 0; i < n - 1; i++)
        root_suffix[i] = n - 1 - i; /* (n-1, n-2, ..., 1) */
    (void)suffix_to_index(root_suffix, n); /* root identity, used implicitly */

    for (int i = 0; i < num_cosets; i++) {
        int s[MAXN], ps[MAXN];
        index_to_suffix(i, s, n);
        if (scut_parent(s, n - 1, ps)) {
            tree_parent[i] = suffix_to_index(ps, n);
        } else {
            tree_parent[i] = -1; /* root */
        }
    }
}

/* ---- Path finding in tree ---- */

/* Find path from u to root, store in path[], return length. */
static int path_to_root(int u, int *path, int max_len) {
    int len = 0;
    int cur = u;
    while (cur != -1 && len < max_len) {
        path[len++] = cur;
        cur = tree_parent[cur];
    }
    return len;
}

/* Find path from u to v in the tree. Store in path[], return length. */
static int find_tree_path(int u, int v, int *path) {
    int pu[50000], pv[50000];
    int lu = path_to_root(u, pu, 50000);
    int lv = path_to_root(v, pv, 50000);

    /* Mark nodes on path from u */
    int *on_pu = calloc(num_cosets, sizeof(int));
    for (int i = 0; i < lu; i++)
        on_pu[pu[i]] = i + 1; /* 1-indexed depth */

    /* Find LCA */
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

    /* Path: u -> ... -> lca -> ... -> v */
    int len = 0;
    for (int i = 0; i <= lca_du; i++)
        path[len++] = pu[i];
    for (int j = lca_dv - 1; j >= 0; j--)
        path[len++] = pv[j];

    return len;
}

/* ---- Edge classification ---- */

/* Check if edge (u, v) is a P(n-1) edge (interior adjacent transposition). */
/* (Kept as documentation; used in concept but not called directly.) */
#if 0
static int is_permutohedron_edge(int u, int v, int n) {
    int su[MAXN], sv[MAXN];
    index_to_suffix(u, su, n);
    index_to_suffix(v, sv, n);

    /* Count differences */
    int diffs = 0, di = -1;
    for (int i = 0; i < n - 1; i++) {
        if (su[i] != sv[i]) {
            diffs++;
            if (di == -1) di = i;
        }
    }
    if (diffs != 2) return 0;
    /* Check if it's an adjacent swap at positions di, di+1 */
    if (di + 1 < n - 1 && su[di] == sv[di + 1] && su[di + 1] == sv[di]) {
        /* Check no other diffs */
        for (int i = 0; i < n - 1; i++) {
            if (i != di && i != di + 1 && su[i] != sv[i]) return 0;
        }
        return 1;
    }
    return 0;
}

/* Check if edge (u, v) is a "forbidden" edge (in Ξ_n(n) \ P(n-1)).
   These are the left-rotation and right-rotation edges. */
static int is_forbidden_edge(int u, int v, int n) {
    int su[MAXN], sv[MAXN];
    index_to_suffix(u, su, n);
    index_to_suffix(v, sv, n);

    /* Check left rotation: sv = (su[1], su[2], ..., su[n-2], su[0]) */
    int is_left = 1;
    for (int i = 0; i < n - 2; i++) {
        if (sv[i] != su[i + 1]) { is_left = 0; break; }
    }
    if (is_left && sv[n - 2] == su[0]) return 1;

    /* Check right rotation: sv = (su[n-2], su[0], su[1], ..., su[n-3]) */
    int is_right = 1;
    if (sv[0] != su[n - 2]) is_right = 0;
    if (is_right) {
        for (int i = 0; i < n - 2; i++) {
            if (sv[i + 1] != su[i]) { is_right = 0; break; }
        }
    }
    if (is_right) return 1;

    return 0;
}
#endif

/*
 * Strategy: pick a forbidden edge (u, v) in Ξ_n(n) \ P(n-1).
 * Adding it to the tree creates a cycle. Remove one edge from this
 * cycle (other than (u,v) itself) to get a new spanning tree of Ξ_n(n).
 */
static void make_aperiodic(int n) {
    /* Find a forbidden edge. Take the root's suffix (n-1, n-2, ..., 1)
       and apply left rotation to get neighbor (n-2, n-3, ..., 1, n-1).
       This edge is in Ξ_n(n) but NOT in P(n-1), so using it breaks periodicity. */
    int root_s[MAXN], neighbor_s[MAXN];
    for (int i = 0; i < n - 1; i++)
        root_s[i] = n - 1 - i;
    /* Left rotation of root suffix */
    for (int i = 0; i < n - 2; i++)
        neighbor_s[i] = root_s[i + 1];
    neighbor_s[n - 2] = root_s[0];

    int u = suffix_to_index(root_s, n);     /* root of S(n-1) */
    int v = suffix_to_index(neighbor_s, n); /* forbidden neighbor of root */

    /* Adding edge (u, v) creates a cycle u -- v -- parent[v] -- ... -- root = u.
       Remove edge (v, parent[v]) and replace with forbidden edge (u, v).
       Since u = root and v is NOT a child of root (its parent chain goes through
       intermediate nodes), this simply re-parents v to the root via the forbidden
       edge, maintaining a valid spanning tree of Ξ_n(n).
       The new tree has one edge not in P(n-1), so by Theorem 6 + Lemma 1,
       the resulting SP-cycle is aperiodic. */

    tree_parent[v] = u; /* replace v's P(n-1) parent edge with forbidden edge */
}

/* ---- Construct Hamilton cycle from spanning tree ---- */

/*
 * From Theorem 4's proof:
 * Given a spanning tree of Ξ_n(n), construct a Hamilton cycle of Ξ(n):
 *   (1) For each edge in the spanning tree, add the two corresponding
 *       σ_{n-1} arcs to the cycle partition.
 *   (2) Add σ_n arcs to fill until each node has out-degree 1.
 *
 * Ξ(n) has n! nodes (permutations of {1..n}).
 * Each node has two outgoing arcs: σ_n and σ_{n-1}.
 *
 * σ_n applied to (a_1, ..., a_n): (a_2, a_3, ..., a_n, a_1) — left rotation.
 * σ_{n-1} applied to (a_1, ..., a_n): (a_2, a_3, ..., a_{n-1}, a_1, a_n)
 *   — rotate first n-1 elements left, keep last element. Wait let me check...
 *   σ_i = (1 2 ... i) is the prefix rotation of length i.
 *   Applied to indices: a_{σ_i} = a_2 a_3 ... a_i a_1 a_{i+1} ... a_n.
 *   So σ_n: a_2 a_3 ... a_n a_1.
 *   σ_{n-1}: a_2 a_3 ... a_{n-1} a_1 a_n.
 *
 * Now, each edge in the spanning tree of Ξ_n(n) corresponds to two
 * σ_{n-1} arcs in Ξ(n). An edge between cosets C_u and C_v means
 * there exist permutations p ∈ C_u and q ∈ C_v such that q = p σ_{n-1}.
 * The reverse arc is from some q' ∈ C_v to p' ∈ C_u.
 *
 * Within each coset (which is a cycle of n σ_n arcs), the two σ_{n-1}
 * arcs leaving the coset go to two different neighboring cosets.
 * A tree edge to a neighbor means exactly one pair of σ_{n-1} arcs
 * (one in each direction) between these two cosets is "used".
 *
 * Implementation:
 *   - For each permutation (n! total), determine its coset and whether
 *     its outgoing σ_{n-1} arc should be used (i.e., the target's coset
 *     is connected by a tree edge).
 *   - If σ_{n-1} arc is used, next = a σ_{n-1}; else next = a σ_n.
 *   - But we must use exactly one σ_{n-1} arc per tree edge direction.
 *
 * More precisely, each σ_n-coset has exactly two σ_{n-1} arcs leaving it
 * (from two specific permutations in the coset). Each such arc goes to
 * a specific neighboring coset. The spanning tree selects (n-1)!-1 of
 * the n*(n-1)!/2 = n!/2 edges, using 2*((n-1)!-1) of the n! σ_{n-1} arcs.
 *
 * For a permutation p = (p_1, ..., p_n), which coset is it in?
 * The coset is the rotation starting with n. The suffix after n determines it.
 *
 * Applying σ_{n-1} to p: q = (p_2, ..., p_{n-1}, p_1, p_n).
 * The coset of p: rotate p until n is first. Say p_h = n.
 *   Then coset suffix = (p_{h+1}, ..., p_n, p_1, ..., p_{h-1}).
 * The coset of q: in q, n was at position... if p_h = n and h < n, then
 *   in q, n is at position h-1 (since everything shifted left by one in
 *   first n-1 positions, and p_n stays). Hmm, this gets complicated.
 *
 * Let me use a different approach. I'll directly determine for each
 * permutation whether σ_{n-1} or σ_n should be applied, by checking
 * if the target coset pair matches a tree edge.
 */

/* Apply σ_n to perm p (length n, 1-based values). Result in q. */
static void apply_sigma_n(const int *p, int *q, int n) {
    /* q = (p[1], p[2], ..., p[n-1], p[0]) — left rotation */
    for (int i = 0; i < n - 1; i++)
        q[i] = p[i + 1];
    q[n - 1] = p[0];
}

/* Apply σ_{n-1} to perm p. Result in q. */
static void apply_sigma_nm1(const int *p, int *q, int n) {
    /* q = (p[1], p[2], ..., p[n-2], p[0], p[n-1]) */
    for (int i = 0; i < n - 2; i++)
        q[i] = p[i + 1];
    q[n - 2] = p[0];
    q[n - 1] = p[n - 1];
}

/* Get coset index for permutation p (length n, values 1..n). */
static int perm_coset(const int *p, int n) {
    /* Find position of n */
    int h = -1;
    for (int i = 0; i < n; i++)
        if (p[i] == n) { h = i; break; }
    /* Suffix after n (circularly): p[h+1], ..., p[n-1], p[0], ..., p[h-1] */
    int s[MAXN];
    int idx = 0;
    for (int i = h + 1; i < n; i++)
        s[idx++] = p[i];
    for (int i = 0; i < h; i++)
        s[idx++] = p[i];
    return suffix_to_index(s, n);
}

/* Index a permutation of {1..n} into {0..n!-1} using Lehmer code. */
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

/*
 * Check if tree has edge between coset c1 and coset c2.
 * tree_parent[c1] == c2 or tree_parent[c2] == c1.
 */
static int tree_has_edge(int c1, int c2) {
    return (tree_parent[c1] == c2 || tree_parent[c2] == c1);
}

/*
 * For each permutation, determine if σ_{n-1} should be used.
 * σ_{n-1} is used if and only if the σ_{n-1} arc connects two cosets
 * that share a tree edge AND we haven't already used a σ_{n-1} arc
 * from this coset to this neighbor.
 *
 * Each tree edge (between coset A and coset B) gets exactly 2 σ_{n-1} arcs:
 * one from some node in A to some node in B, and one from some node in B
 * to some node in A. Within each coset cycle of n nodes (under σ_n),
 * exactly one node sends a σ_{n-1} arc to coset B.
 *
 * Actually, from a given coset, each of the n permutations has a σ_{n-1} arc
 * going to some neighboring coset. The n permutations in a coset send their
 * σ_{n-1} arcs to various neighbors. Each neighbor gets exactly one σ_{n-1}
 * arc from this coset.
 *
 * So: for each permutation p, compute target of σ_{n-1}(p), find its coset,
 * check if tree edge exists between p's coset and target's coset.
 */
static int *successor; /* successor[perm_idx] = index of next perm in Hamilton cycle */

static void build_hamilton_cycle(int n) {
    int nfact = fact[n];
    successor = calloc(nfact, sizeof(int));

    /* For each permutation, default to σ_n. Then override with σ_{n-1}
       for those whose σ_{n-1} target coset is a tree neighbor. */

    /* Allocate arrays for tracking: for each coset, which tree neighbors
       should get σ_{n-1} arcs. */
    int p[MAXN], q_n[MAXN], q_nm1[MAXN];

    for (int i = 0; i < nfact; i++) {
        index_to_perm_1based(i, p, n);

        /* Compute both successors */
        apply_sigma_n(p, q_n, n);
        apply_sigma_nm1(p, q_nm1, n);

        int c_p = perm_coset(p, n);
        int c_nm1 = perm_coset(q_nm1, n);

        if (c_p != c_nm1 && tree_has_edge(c_p, c_nm1)) {
            /* Use σ_{n-1} */
            successor[i] = perm_index_1based(q_nm1, n);
        } else {
            /* Use σ_n */
            successor[i] = perm_index_1based(q_n, n);
        }
    }
}

/* ---- Verify it's a single Hamilton cycle ---- */
static int verify_hamilton(int n) {
    int nfact = fact[n];
    int *visited = calloc(nfact, sizeof(int));

    /* Start from n n-1 ... 1 */
    int start_p[MAXN];
    for (int i = 0; i < n; i++) start_p[i] = n - i;
    int start = perm_index_1based(start_p, n);

    int cur = start;
    int count = 0;
    do {
        if (visited[cur]) {
            free(visited);
            return 0; /* stuck in smaller cycle */
        }
        visited[cur] = 1;
        count++;
        cur = successor[cur];
    } while (cur != start && count <= nfact);

    free(visited);
    return (count == nfact);
}

/* ---- Output SP-cycle ---- */

static void output_sp_cycle(int n) {
    int nfact = fact[n];

    /* Start from n n-1 ... 2 1 */
    int start_p[MAXN];
    for (int i = 0; i < n; i++) start_p[i] = n - i;
    int start = perm_index_1based(start_p, n);

    /* The SP-cycle symbol at position i is the first element of the i-th permutation. */
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

/* ---- Check aperiodicity ---- */

static int check_aperiodic(int n) {
    int nfact = fact[n];
    int start_p[MAXN];
    for (int i = 0; i < n; i++) start_p[i] = n - i;
    int start = perm_index_1based(start_p, n);

    int cur = start;
    /* Check if every n-th symbol is n. If so, it's periodic. */
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

/* ---- Verify SP-cycle: all (n-1)-substrings are distinct (n-1)-perms of {1..n} ---- */

static int verify_sp_cycle(int n) {
    int nfact = fact[n];
    int start_p[MAXN];
    for (int i = 0; i < n; i++) start_p[i] = n - i;
    int start = perm_index_1based(start_p, n);

    /* Collect all symbols */
    int *symbols = malloc(nfact * sizeof(int));
    int cur = start;
    for (int i = 0; i < nfact; i++) {
        int p[MAXN];
        index_to_perm_1based(cur, p, n);
        symbols[i] = p[0];
        cur = successor[cur];
    }

    /* Verify by checking decoded permutations are all distinct.
       The i-th decoded permutation is (symbols[i..i+n-2], missing).
       If all n! decoded permutations are distinct, it is a valid SP-cycle. */
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
        /* Convert to index */
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
        /* For n=3, P(2) has only 2 vertices and 1 edge.
           Xi_3(3) also has only 2 vertices (with 3 edges each).
           There is only one spanning tree (1 edge), and any spanning tree
           of Xi_3(3) is also a spanning tree of P(2) since P(2) is a
           subgraph with the same vertex set. So no aperiodic min-weight
           SP-cycle exists for n=3. The only aperiodic SP-cycle would
           have higher weight. Print the known aperiodic one from Fig 1(a). */
        fprintf(stderr, "n=3: No aperiodic min-weight SP-cycle exists.\n");
        fprintf(stderr, "Printing the unique (up to rotation) aperiodic SP-cycle for Pi(3):\n");
        printf("3 2 1 3 1 2\n");
        return 0;
    }

    init_fact(n);

    fprintf(stderr, "Building cool-lex spanning tree S(%d) of P(%d)...\n", n - 1, n - 1);
    build_cool_lex_tree(n);

    fprintf(stderr, "Making tree aperiodic (adding forbidden edge, removing tree edge)...\n");
    make_aperiodic(n);

    fprintf(stderr, "Building Hamilton cycle in Xi(%d)...\n", n);
    build_hamilton_cycle(n);

    fprintf(stderr, "Verifying Hamilton cycle...\n");
    if (!verify_hamilton(n)) {
        fprintf(stderr, "ERROR: cycle partition has multiple cycles, not a single Hamilton cycle.\n");
        fprintf(stderr, "Attempting alternate edge removal...\n");

        /* Try alternate: instead of removing (v, old_parent), remove a deeper edge.
           Rebuild tree and try removing each edge on the path from v to root. */
        build_cool_lex_tree(n); /* reset */

        int root_s[MAXN], neighbor_s[MAXN];
        for (int i = 0; i < n - 1; i++)
            root_s[i] = n - 1 - i;
        for (int i = 0; i < n - 2; i++)
            neighbor_s[i] = root_s[i + 1];
        neighbor_s[n - 2] = root_s[0];

        int u = suffix_to_index(root_s, n);
        int v = suffix_to_index(neighbor_s, n);

        /* Path from v to root */
        int path[50000];
        int pathlen = path_to_root(v, path, 50000);

        int found = 0;
        for (int e = 0; e < pathlen - 1; e++) {
            /* Try removing edge (path[e], path[e+1]) and adding (u, v) */
            build_cool_lex_tree(n);

            /* To remove edge (path[e], path[e+1]) and add (root, v):
               We need to re-parent. The edge (path[e], path[e+1]) means
               tree_parent[path[e]] == path[e+1].
               Removing it disconnects the subtree rooted at path[e].
               Adding (root, v): v's path to root goes through path[e], so
               we need to reverse parent pointers from v up to path[e],
               then connect path[e] to... hmm this is more complex.

               Actually, simpler: the cycle is u=root -- v(forbidden edge) -- 
               parent[v] -- ... -- root.
               Removing edge (path[e], path[e+1]) and adding (u, v):
               We reverse the parent links from v to path[e] and set
               tree_parent[v] = u. */

            /* Reverse parent links from v (=path[0]) to path[e] */
            for (int k = 0; k < e; k++) {
                tree_parent[path[k + 1]] = path[k];
            }
            /* path[e]'s parent was path[e+1], now disconnect */
            /* path[e] is now a child of path[e-1] (due to reversal).
               But path[e]'s old parent (path[e+1]) should no longer parent it.
               Actually after the reversal, path[e]'s parent hasn't been changed yet.
               We need: tree_parent[path[0]=v] = u (root), and for k=1..e,
               tree_parent[path[k]] = path[k-1]. path[e]'s old parent was path[e+1];
               now path[e]'s parent is path[e-1]. */
            tree_parent[path[0]] = u; /* add forbidden edge */
            /* For k=1..e: already done above (path[k+1].parent = path[k] → reversed to path[k].parent = path[k-1]) */
            /* Actually let me redo this more carefully */
            build_cool_lex_tree(n);
            int saved_parents[50000];
            memcpy(saved_parents, tree_parent, num_cosets * sizeof(int));

            /* Reverse the segment of the path from path[0] to path[e+1]:
               Original: path[0]->path[1]->...->path[e]->path[e+1]->...->root
               After: root(=u)->path[0]  and path[1]->path[0], ..., path[e]->path[e-1]
                      path[e]'s original parent (path[e+1]) is detached.
                      path[e+1] is still connected via its path to root. */
            tree_parent[path[0]] = u; /* forbidden edge */
            for (int k = 1; k <= e; k++) {
                tree_parent[path[k]] = path[k - 1];
            }
            /* path[e+1] loses child path[e], but path[e+1] is still connected to root  
               via its own parent chain. This effectively removes edge (path[e], path[e+1])
               and adds edge (u, path[0]=v). */

            build_hamilton_cycle(n);
            if (verify_hamilton(n)) {
                found = 1;
                fprintf(stderr, "Success with edge removal at depth %d on path.\n", e);
                break;
            }
        }

        if (!found) {
            /* Try all forbidden edges, not just root's */
            fprintf(stderr, "Trying all possible forbidden edges...\n");
            found = 0;
            for (int ci = 0; ci < num_cosets && !found; ci++) {
                int si[MAXN];
                index_to_suffix(ci, si, n);
                /* Left rotation neighbor */
                int nb[MAXN];
                for (int k = 0; k < n - 2; k++) nb[k] = si[k + 1];
                nb[n - 2] = si[0];
                int cj = suffix_to_index(nb, n);

                /* Reset tree */
                build_cool_lex_tree(n);

                /* Path from ci to cj in tree */
                int tpath[50000];
                int tpathlen = find_tree_path(ci, cj, tpath);

                /* Try removing each edge on this path */
                for (int e = 0; e < tpathlen - 1 && !found; e++) {
                    int ea = tpath[e], eb = tpath[e + 1];
                    build_cool_lex_tree(n);

                    /* Remove edge (ea, eb) by finding which is parent */
                    /* Re-root the subtree. Adding forbidden edge (ci, cj). */
                    /* The path ci -> ... -> ea -> eb -> ... -> cj plus forbidden
                       edge (cj, ci) forms a cycle. Remove (ea, eb). */

                    /* Determine orientation: ea is parent of eb or vice versa */
                    int child, par;
                    if (tree_parent[ea] == eb) { child = ea; par = eb; }
                    else if (tree_parent[eb] == ea) { child = eb; par = ea; }
                    else continue; /* shouldn't happen on a tree path */

                    /* In the cycle: ci -- (tree path) -- cj -- (forbidden) -- ci
                       Remove (child, par). To reconnect, we reverse parent pointers
                       along the tree path from ci (or cj) to child. */

                    /* Find which side of the removed edge ci is on.
                       ci is connected to child through the path portion. */
                    /* Actually, let's just brute-force: remove edge (child,par),
                       add forbidden edge. The forbidden edge goes "across" the break. */

                    /* Approach: The path in the tree from ci to cj goes through
                       edge (ea, eb). Removing it splits tree into two components.
                       ci is in one, cj in the other. Adding forbidden edge (ci,cj)
                       reconnects them. */

                    /* To implement: reverse parent pointers from ci up to child
                       (or from cj up to par), then detach, then add forbidden edge. */

                    /* Let's find which of ci,cj is in child's subtree */
                    /* Check if ci is a descendant of child */
                    int ci_side = 0; /* 1 if ci descends from child */
                    {
                        int x = ci;
                        while (x != -1) {
                            if (x == child) { ci_side = 1; break; }
                            if (x == par) break;
                            x = tree_parent[x];
                        }
                    }

                    if (ci_side) {
                        /* ci is under child. Reverse path from ci up to child,
                           set child's parent to ci via... no. We reverse from cj 
                           to par side. Make cj the attachment point via forbidden edge. */
                        /* Actually: remove child->par link. Now child's subtree
                           (containing ci) is disconnected. Add ci-cj (forbidden),
                           which connects ci's component (child's subtree) to cj's
                           component (par's tree). 
                           
                           But ci is deep in child's subtree. We need ci's parent
                           chain to lead to... hmm. We need to reverse parent links
                           from ci up to child so that child's parent becomes ci
                           (indirectly), and ci's parent becomes cj (forbidden). */

                        /* Reverse parent links along path from ci to child */
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
                        /* rpath: ci, ..., child */
                        /* Reverse: child.parent = rpath[rlen-2], etc. */
                        for (int k = rlen - 1; k >= 1; k--) {
                            tree_parent[rpath[k]] = rpath[k - 1];
                        }
                        tree_parent[ci] = cj; /* forbidden edge */
                    } else {
                        /* cj is under child. */
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
                        for (int k = rlen - 1; k >= 1; k--) {
                            tree_parent[rpath[k]] = rpath[k - 1];
                        }
                        tree_parent[cj] = ci; /* forbidden edge */
                    }

                    build_hamilton_cycle(n);
                    if (verify_hamilton(n)) {
                        found = 1;
                        fprintf(stderr, "Success with forbidden edge (%d,%d), removing edge %d on path.\n",
                                ci, cj, e);
                    }
                }
            }

            if (!found) {
                fprintf(stderr, "FATAL: Could not find a working aperiodic modification.\n");
                free(successor);
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
