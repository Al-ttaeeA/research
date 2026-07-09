package scripts;

public class GenerateLength2Pairs {
    public static int k = 4;

    public static void main(String[] args) {
        generatePairs(k);
    }

    // Generates all pairs of complementary strings of length 2 for a given k. 
    public static void generatePairs(int k) {
        int total = (int) Math.pow(k, 2);
        int seen[] = new int[total];
        for (int i = 0; i < total; i++) {
            if (seen[i] == 1) continue; // already seen this pair

            String s = indexToString(i, k, 2);
            String comp = complementString(s, k);
            int compIdx = stringToIndex(comp, k);

            System.out.println(s + " <-> " + comp);

            seen[i] = 1;
            seen[compIdx] = 1;
        }
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
