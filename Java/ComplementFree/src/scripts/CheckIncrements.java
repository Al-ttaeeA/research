package scripts;

import checker.Checker;

public class CheckIncrements {
    public static int n = 2;
    public static int k = 8;

    public static String sequence = "771213141516106623242520553430";

    public static void main(String[] args) {
        checkIncrements(sequence, k, n);
    }

    // Checks all k increments of the sequence to see if they are complement-free.
    public static void checkIncrements(String sequence, int k, int n) {
        

        for(int increment = 0; increment < k; increment++) {
            StringBuilder incrementedSequence = new StringBuilder();

            for (char c : sequence.toCharArray()) {
                int digit = c - '0';
                int incrementedDigit = (digit + increment) % k;
                incrementedSequence.append(incrementedDigit);
            }

            System.out.println("Increment " + increment + ": " + incrementedSequence);
            Checker.check(incrementedSequence.toString(), k, n);
        }
    }
}
