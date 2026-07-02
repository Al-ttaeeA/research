package lempels;

import java.util.ArrayList;

public class Lempels {

	/** Returned as the sole list element when {@code checkWeight=true} and weight(C_0) ≠ 0 mod k. */
	public static final String WEIGHT_ERROR = "WEIGHT_ERROR";

	/************************************************
	 * Enter values here
	 ************************************************/
	static int n = 3;
	static int k = 2;
	static int beta = 1;
	static boolean punctured = false;
	// de Bruijn sequence of order n over Z_k — weight must be 0 mod k
	// (guaranteed for n > 1, or n = 1 with odd k, by Lemma 3.7)
	static String sequence = "00010111";

	public static void main(String[] args) {
		ArrayList<String> cycles = lempelLift(sequence, n, k, beta, false, false);

		System.out.println(cycles.size() + " inverse cycle(s) in B_" + (n+1) + "(" + k + "):");
		for (int i = 0; i < cycles.size(); i++)
			System.out.println("C_" + i + " = " + cycles.get(i));

		System.out.println("Length of each cycle: " + cycles.get(0).length());
	}

	/***************************************************
	 * Computes the k inverse cycles of a de Bruijn sequence under D_beta.
	 *
	 * By Theorem 3.2(c), when W(Γ_n) ≡ 0 (mod k), the preimage of Γ_n
	 * under D_beta is exactly k primitive vertex-disjoint cycles of length k^n.
	 *
	 * From Equation (4.1):
	 *   C_0[j] = beta^{-1} * (γ_1 + ... + γ_{j+1}) mod k
	 *   C_i    = C_0 + i  (mod k)
	 ***************************************************/
	/***************************************************
	 * When punctured == true the sequence has nonzero weight lambda, and its
	 * preimage under D_beta is a SINGLE cycle of length r * |seqStr| where
	 * r = k / gcd(lambda, k).  It is formed by concatenating r translated
	 * copies of C_0, where each copy j is C_0 shifted by j * delta (mod k)
	 * and delta = beta^{-1} * lambda mod k.
	 *
	 * When punctured == false the behaviour is unchanged: returns k disjoint
	 * translate-cycles C_0, ..., C_{k-1} (requires W == 0).
	 ***************************************************/
	public static ArrayList<String> lempelLift(String seqStr, int n, int k, int beta, boolean checkWeight, boolean punctured) {
		int betaInv = modInverse(beta, k);

		int kn = seqStr.length(); // k^n

		// build C_0 via running prefix sum scaled by beta^{-1}
		// C_0[j] = beta^{-1} * (γ_1 + ... + γ_{j+1}) mod k  (Equation 4.1)
		int[] C0 = new int[kn];
		int sum = 0;
		for (int j = 0; j < kn; j++) {
			sum = (sum + Character.getNumericValue(seqStr.charAt(j))) % k;
			C0[j] = (betaInv * sum) % k;
		}

		// Check weight of C0 is 0 mod k, as required by Theorem 3.2(c)
		if (checkWeight) {
			int wC0 = 0;
			for (int c : C0) wC0 = (wC0 + c) % k;
			if (wC0 != 0) {
				ArrayList<String> err = new ArrayList<>();
				err.add(WEIGHT_ERROR);
				return err;
			}
		}

		if (punctured) {
			// lambda = W(gamma) mod k  (the cycle weight, nonzero for a punctured sequence)
			// Equivalently: C0[kn-1] = betaInv * lambda, so lambda = (beta * C0[kn-1]) % k
			int lambda = 0;
			for (int j = 0; j < kn; j++)
				lambda = (lambda + Character.getNumericValue(seqStr.charAt(j))) % k;
			// per-round preimage shift: delta = beta^{-1} * lambda mod k
			int delta = (betaInv * lambda) % k;
			// r repetitions needed to close the cycle
			int r = k / gcd(lambda, k);
			// merge r translated copies of C_0 into one cycle of length r * kn
			StringBuilder merged = new StringBuilder();
			for (int j = 0; j < r; j++) {
				int shift = (j * delta) % k;
				for (int i = 0; i < kn; i++)
					merged.append(intToChar((C0[i] + shift) % k));
			}
			ArrayList<String> result = new ArrayList<>();
			result.add(merged.toString());
			return result;
		}

		// C_i = C_0 + i (mod k)  — translation by i (Theorem 3.2(c))
		ArrayList<String> cycles = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < kn; j++)
				sb.append(intToChar((C0[j] + i) % k));
			cycles.add(sb.toString());
		}

		return cycles;
	}
 
	static int gcd(int a, int b) {
		a = Math.abs(a); b = Math.abs(b);
		while (b != 0) { int t = b; b = a % b; a = t; }
		return a;
	}

	static char intToChar(int val) {
		if (val < 10) return (char) ('0' + val);
		return (char) ('A' + val - 10);
	}

	static int modInverse(int a, int m) {
		a = ((a % m) + m) % m;
		int r0 = m, r1 = a, s0 = 0, s1 = 1;
		while (r1 != 0) {
			int q = r0 / r1;
			int tmp = r0 - q * r1; r0 = r1; r1 = tmp;
			tmp = s0 - q * s1;     s0 = s1; s1 = tmp;
		}
		return ((s0 % m) + m) % m;
	}
}
