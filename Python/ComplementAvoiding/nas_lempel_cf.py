#!/usr/bin/env python3
"""
For every even k, n pair with 2 <= k,n < 10:
  1. Run NAS.exe at order (n-1) to get a negative avoiding sequence
  2. Apply Lempel's D_beta^{-1} lift (beta=1) to produce k candidate cycles
  3. Check each cycle for the complement-free property using n-grams of window n
"""

import subprocess

NAS_EXE = r'C:\Users\Admin\research\C\negative free\NAS.exe'


# ---------------------------------------------------------------------------
# Step 1 – generate NAS via the compiled C program
# ---------------------------------------------------------------------------

def run_nas(n, k):
    """Run NAS.exe with order n, alphabet size k.
    Returns the output sequence as a plain string of digit characters."""
    proc = subprocess.run(
        [NAS_EXE],
        input=f"{n} {k}\n",
        capture_output=True,
        text=True,
    )
    chars = []
    for line in proc.stdout.split('\n'):
        if 'Length' in line or 'Expected' in line:
            continue
        for ch in line:
            if ch.isdigit():
                chars.append(ch)
    return ''.join(chars)


# ---------------------------------------------------------------------------
# Step 2 – Lempel's lift  (mirrors Lempels.java exactly)
# ---------------------------------------------------------------------------

def _mod_inverse(a, m):
    """Modular inverse of a mod m via extended Euclidean algorithm."""
    a = ((a % m) + m) % m
    r0, r1, s0, s1 = m, a, 0, 1
    while r1:
        q = r0 // r1
        r0, r1 = r1, r0 - q * r1
        s0, s1 = s1, s0 - q * s1
    return ((s0 % m) + m) % m


def lempel_lift(seq, k, beta=1):
    """Apply Lempel's D_beta^{-1} to *seq* over Z_k.

    Implements Equation (4.1) from the paper:
        C_0[j] = beta^{-1} * (S[0] + ... + S[j])  mod k
        C_i    = C_0 + i                            mod k

    Returns a list of k strings (the k cycles), each the same length as *seq*.
    Raises ValueError when weight(C_0) != 0 mod k (Theorem 3.2(c) violated).
    """
    beta_inv = _mod_inverse(beta, k)

    # Build C_0 from the running prefix sum
    C0 = []
    running = 0
    for ch in seq:
        running = (running + int(ch)) % k
        C0.append((beta_inv * running) % k)

    # Theorem 3.2(c): weight of C_0 must be 0 mod k
    if sum(C0) % k != 0:
         raise ValueError(
             f"weight(C_0) ≡ {sum(C0) % k} (mod {k}), expected 0"
         )

    # Produce the k translated copies
    return [''.join(str((x + i) % k) for x in C0) for i in range(k)]


# ---------------------------------------------------------------------------
# Step 3 – complement-free check  (mirrors Checker.java exactly)
# ---------------------------------------------------------------------------

def _complement(s, k):
    """Map each digit d to k-1-d."""
    return ''.join(str(k - 1 - int(c)) for c in s)


def is_complement_free(seq, window, k):
    """Test whether *seq* is complement-free with cyclic wrap-around.

    For every consecutive *window*-gram, neither it nor its complement
    (digit-wise d -> k-1-d) may have appeared earlier.

    Returns (bool, message).
    """
    L = len(seq)
    if L < window:
        return False, f"sequence too short (len={L} < window={window})"

    ext = seq + seq[:window - 1]          # cyclic wrap-around
    seen = {}                              # gram -> first position
    for i in range(L):
        gram = ext[i : i + window]
        comp = _complement(gram, k)

        if gram in seen:
            return False, f"duplicate '{gram}' at {seen[gram]} and {i}"
        if comp in seen:
            return False, (
                f"complement pair: '{gram}' at {i} "
                f"and '{comp}' at {seen[comp]}"
            )
        seen[gram] = i

    return True, ""


# ---------------------------------------------------------------------------
# Main loop
# ---------------------------------------------------------------------------

def main():
    header = (
        f"{'k':>3}  {'n':>3}  {'|NAS(n-1,k)|':>14}  "
        f"{'cycle':>6}  {'CF?':>4}  note"
    )
    print(header)
    print('-' * len(header))

    for k in range(4, 10, 2):       # even k: 4, 6, 8
        for n in range(4, 10, 2):   # even n: 4, 6, 8

            nas_n = n - 1           # always odd

            # ---- Step 1 ----
            seq = run_nas(nas_n, k)
            if not seq:
                print(f"{k:>3}  {n:>3}  {'(empty NAS)':>14}")
                continue

            # ---- Step 2 ----
            try:
                cycles = lempel_lift(seq, k)
            except ValueError as e:
                print(
                    f"{k:>3}  {n:>3}  {len(seq):>14}  "
                    f"{'—':>6}  {'—':>4}  lift error: {e}"
                )
                continue

            # ---- Step 3 ----
            for i, cyc in enumerate(cycles):
                ok, note = is_complement_free(cyc, n, k)
                flag = 'YES' if ok else 'NO '
                detail = '' if ok else f'  [{note}]'
                print(
                    f"{k:>3}  {n:>3}  {len(seq):>14}  "
                    f"{i:>6}  {flag:>4}{detail}"
                )

        print()


if __name__ == '__main__':
    main()
