package checker;

import java.util.ArrayList;
import java.util.Arrays;

public class NegativeChecker {

    public static String sequence = "005553332232242243363364476627731154415527742276647751174420065546650073327764432376742307063420176763431210264621305742410205753532437105764654721405042510305053632537205064754762541431432627375154052631521530420421541542654163163275306521743206431076663331110010020021141142254405517732273305520054425537752206643324436651105542210154520165641206754541217076042407163520276063531310215763542432507263620376163631410315063642532540327217210405153732630417307316206207327320432741741053164307521064217654441117776676606607727720032263375510051163306632203315530064421102214437763320076732306743427064532327075654620265741306054641317176073541320210365041406154741417276173641420310326105075076263731510416275165174064065105106210527527631742165307642075432227775554454464465505506610041153376637741164410061173316642207760072215541106654510164521205642310105653432406043527164632427175754651327106076143627264732527275054751427206176104763653654041517376274053743752642643763764076305305417520743165420653211166644433433533544744755077300422655266300533077500622055311766577611044300755434070534101745312070745423213757324160535213160646435402160757650325161536214161647436403161750650736525425437304062651637426326415315326526537652742743064176320543175421077744422211211311322522533655166200433044166311655366400633177544355477622166533212656312767523170656523201071535102746313071746424213260746535436103747314072747425214261747536436514303203215162640437415204104273173104304315430520521642754106321753207655522200077077177100300311433744066211622744177433144266411755322133255400744311070434170545301756434301067657313760524171657524202071046524313214761525172650525203072047525314214372161061073740426215273062762051751762162173216306307420532764107531065433300066655655755766166177211522644077400522755211722044277533100711033266522177656212756323167534212167645435171546302757435302060657624302171072547303750436303061650625303172072150747647651526204073051640540637537540740751074164165206310542765317643210";

    public static int n = 4;
    public static int k = 8;

    public static void main(String[] args) {
        extensiveCheck(sequence, k, n);
    }

    public static void check(String sequence, int k, int n) {
        int len = sequence.length();
        if (len == 0) {
            System.out.println("Sequence is empty.");
            return;
        }
        String extended = sequence + sequence.substring(0, n - 1);

        int total = (int) Math.pow(k, n);
        int[] seen = new int[total];
        Arrays.fill(seen, -1);

        for (int i = 0; i < len; i++) {
            String s = extended.substring(i, i + n);
            int idx = stringToIndex(s, k);
            int negIdx = negativeIndex(idx, k, n);

            if (idx == negIdx) {
                System.out.println("Self-negative string found (invalid): " + s + " at position " + i);
                return;
            }

            if (seen[idx] != -1) {
                System.out.println("Duplicate string: " + s + " at positions " + i + " and " + seen[idx]);
                return;
            }

            if (seen[negIdx] != -1) {
                System.out.println("Not negative-free: " + s + " and " + negativeString(s, k) + " at positions " + i + " and " + seen[negIdx]);
                return;
            }

            seen[idx] = i;
        }

        System.out.println("Sequence is negative-free.");
    }

    public static void extensiveCheck(String sequence, int k, int n) {
        int len = sequence.length();
        if (len == 0) {
            System.out.println("Sequence is empty.");
            return;
        }
        String extended = sequence + sequence.substring(0, n - 1);

        int total = (int) Math.pow(k, n);
        int[] seen = new int[total];
        Arrays.fill(seen, -1);

        for (int i = 0; i < len; i++) {
            String s = extended.substring(i, i + n);
            int idx = stringToIndex(s, k);
            int negIdx = negativeIndex(idx, k, n);

            if (idx == negIdx) {
                System.out.println("Self-negative string found (invalid): " + s + " at position " + i);
                return;
            }

            if (seen[idx] != -1) {
                System.out.println("Duplicate string: " + s + " at positions " + i + " and " + seen[idx]);
                return;
            }

            if (seen[negIdx] != -1) {
                System.out.println("Not negative-free: " + s + " and " + negativeString(s, k) + " at positions " + i + " and " + seen[negIdx]);
                return;
            }

            seen[idx] = i;
        }

        System.out.println("Sequence is negative-free.");

        int maxLength = (k % 2 == 1) ? (total - 1) / 2 : total / 2;

        if (len == maxLength) {
            System.out.println("Sequence has maximum length (" + maxLength + ").");
            return;
        }

        System.out.println("Sequence length " + len + " is less than maximum " + maxLength + ".");

        // Report all self-negative strings (these are always excluded from a negative-free sequence)
        System.out.println("\nSelf-negative strings (cannot appear in any negative-free sequence):");
        for (int idx = 0; idx < total; idx++) {
            if (negativeIndex(idx, k, n) == idx) {
                System.out.println("  " + indexToString(idx, k, n));
            }
        }

        // Report negative pairs where neither member is present in the sequence
        System.out.println("\nMissing negative pairs (neither string present in sequence):");
        boolean[] printed = new boolean[total];
        for (int idx = 0; idx < total; idx++) {
            int negIdx = negativeIndex(idx, k, n);
            if (idx == negIdx) continue; // skip self-negatives
            if (printed[idx] || printed[negIdx]) continue;
            printed[idx] = true;
            printed[negIdx] = true;
            if (seen[idx] == -1 && seen[negIdx] == -1) {
                System.out.println("  " + indexToString(idx, k, n) + " / " + indexToString(negIdx, k, n));
            }
        }
    }

    /**
     * Returns the index of the negative of the string at the given index.
     * The negative maps each digit d to (k - d) % k.
     */
    public static int negativeIndex(int idx, int k, int n) {
        int[] digits = new int[n];
        int temp = idx;
        for (int i = n - 1; i >= 0; i--) {
            digits[i] = temp % k;
            temp /= k;
        }
        int negIdx = 0;
        for (int i = 0; i < n; i++) {
            negIdx = negIdx * k + ((k - digits[i]) % k);
        }
        return negIdx;
    }

    public static String negativeString(String s, int k) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(negative(c, k));
        }
        return sb.toString();
    }

    public static char negative(char c, int k) {
        int val = charToInt(c);
        int negVal = (k - val) % k;
        return intToChar(negVal);
    }

    public static String indexToString(int idx, int k, int n) {
        char[] result = new char[n];
        for (int i = n - 1; i >= 0; i--) {
            result[i] = intToChar(idx % k);
            idx /= k;
        }
        return new String(result);
    }

    public static int stringToIndex(String s, int k) {
        int idx = 0;
        for (char c : s.toCharArray()) {
            idx = idx * k + charToInt(c);
        }
        return idx;
    }

    public static boolean checkNegativeFree(String seq, int len, int n, int k) {
        String extended = seq + seq.substring(0, n - 1);
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            String s = extended.substring(i, i + n);
            String neg = negativeString(s, k);
            if (s.equals(neg) || strings.contains(s) || strings.contains(neg)) {
                return false;
            }
            strings.add(s);
        }
        return true;
    }

    public static int charToInt(char c) {
        return Character.getNumericValue(c);
    }

    public static char intToChar(int val) {
        if (val < 10) return (char) ('0' + val);
        return (char) ('A' + val - 10);
    }

    public static int modK(int num, int k) {
        int value = num % k;
        if (value < 0) {
            value = value + k;
        }
        return value;
    }
}
