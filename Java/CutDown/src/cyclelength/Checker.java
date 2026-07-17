package cyclelength;

public class Checker {
    public static String sequence = "0000001111101111001111010001110010001001110101100110100100101010";

    public static int n = 7;
    public static int k = 2;

    public static void main(String[] args) {
        check(sequence, k, n);
    }

    // Checks if a cut down sequence is balanced
    public static void check(String sequence, int k, int n) {
        int len = sequence.length();
        String extended = sequence + sequence.substring(0, n - 1); // append first n-1 chars to end for wraparound


        int count1 = 0;
        int count0 = 0;
        for(int i = 0; i < len; i++) {
            char c = extended.charAt(i);
            if (c == '1') {
                count1++;
            }
            else if (c == '0') {
                count0++;
            }
        }
        if (count1 != count0) {
            System.out.println("Not balanced: " + sequence);
            return;
        }
        else {
            System.out.println("Balanced: " + sequence);
        }
    }
}
