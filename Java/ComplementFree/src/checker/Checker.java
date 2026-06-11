package checker;

import java.util.ArrayList;

public class Checker {
    public static String sequence = "00010020030110120130210220311112";

    public static int length = sequence.length();

    public static int n = 3;
    public static int k = 4;

    public static void main(String[] args) {
        sequence = sequence + sequence.substring(0, n-1); // append first n-1 chars to end for wraparound

        ArrayList<String> strings = new ArrayList<>(); 

        for(int i = 0; i < length; i++) {
            String s = sequence.substring(i, i+n);

            if(strings.contains(s)) {
                System.out.println("Duplicate string: " + s + " at positions " + i + " and " + strings.indexOf(s));
                System.out.println(strings);
                return;
            }
            
            if(strings.contains(complementString(s))) {
                System.out.println("Not complement-free: " + s + " and " + complementString(s) + " at positions " + i + " and " + strings.indexOf(complementString(s)));
                System.out.println(strings);
                return;
            }
            strings.add(s);
        }

        System.out.println("Sequence is complement-free.");

        // Check all possible single-digit insertions
        System.out.println("\nChecking all possible single-digit insertions...");
        String originalSeq = sequence.substring(0, length);
        boolean foundInsertion = false;
        for (int pos = 0; pos <= length; pos++) {
            for (int digit = 0; digit < k; digit++) {
                String newSeq = originalSeq.substring(0, pos) + digit + originalSeq.substring(pos);
                if (checkComplementFree(newSeq, length + 1)) {
                    System.out.println("Valid insertion: digit " + digit + " at position " + pos + " -> " + newSeq);
                    foundInsertion = true;
                }
            }
        }
        if (!foundInsertion) {
            System.out.println("No valid single-digit insertion found.");
        }
    }

    public static String complementString(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(complement(c));
        }
        return sb.toString();
    }

    public static char complement(char c) {
        int val = charToInt(c);
        int compVal = k - 1 - val; // complement in Z_k
        return (char) (compVal + '0'); // convert back to char
    }

    public static int charToInt(char c) {
        return Character.getNumericValue(c);
    }

    public static boolean checkComplementFree(String seq, int len) {
        String extended = seq + seq.substring(0, n - 1);
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            String s = extended.substring(i, i + n);
            if (strings.contains(s) || strings.contains(complementString(s))) {
                return false;
            }
            strings.add(s);
        }
        return true;
    }

    public static int modK(int num) {
		int value = num % k;
		if(value < 0) {
			value = value + k;
		}
		
		return value;
	}
}
