package lempels;

import java.util.ArrayList;

public class Lempels {

	/************************************************
	 * Enter values here
	 ************************************************/
	static int n = 8;
	static int k = 2;
	static int beta = 1;
	// de Bruijn sequence of order n over Z_k — weight must be 0 mod k
	// (guaranteed for n > 1, or n = 1 with odd k, by Lemma 3.7)
	static String sequence = "0000000010000001100000101000001110000100100001011000011010000111100010001001100010101000101110001100100011011000111010001111100100101001001110010101100101101001011110011001101010011011100111011001111010011111101010101110101101101011111011011110111011111111";

	public static void main(String[] args) {
		ArrayList<String> cycles = lempelLift(sequence, n, k, beta);

		System.out.println("k = " + k + " inverse cycles in B_" + (n+1) + "(" + k + "):");
		for (int i = 0; i < cycles.size(); i++)
			System.out.println("C_" + i + " = " + cycles.get(i));
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
	static ArrayList<String> lempelLift(String seqStr, int n, int k, int beta) {
		int betaInv = modInverse(beta, k);

		// check weight ≡ 0 mod k
		int w = 0;
		for (char c : seqStr.toCharArray()) w = (w + Character.getNumericValue(c)) % k;
		if (w != 0)
			throw new IllegalArgumentException("Sequence weight is not 0 mod k");

		int kn = seqStr.length(); // k^n

		// build C_0 via running prefix sum scaled by beta^{-1}
		// C_0[j] = beta^{-1} * (γ_1 + ... + γ_{j+1}) mod k  (Equation 4.1)
		int[] C0 = new int[kn];
		int sum = 0;
		for (int j = 0; j < kn; j++) {
			sum = (sum + Character.getNumericValue(seqStr.charAt(j))) % k;
			C0[j] = (betaInv * sum) % k;
		}

		// C_i = C_0 + i (mod k)  — translation by i (Theorem 3.2(c))
		ArrayList<String> cycles = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < kn; j++)
				sb.append((C0[j] + i) % k);
			cycles.add(sb.toString());
		}

		return cycles;
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
