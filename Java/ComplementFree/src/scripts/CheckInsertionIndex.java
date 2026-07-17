package scripts;

import construction.ConstructEvenEven;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CheckInsertionIndex {

    static final int MAX_SEQ_LEN = 5_000_000;
    static final String OUTPUT_FILE =
            "C:\\Users\\Admin\\research\\JSON Data\\insertion_indices.json";

    public static void main(String[] args) throws Exception {
        LinkedHashMap<String, Object> jsonRoot = new LinkedHashMap<>();

        // All even k, even n pairs with k + n < 16 and k,n >= 4
        for (int k = 4; k < 20; k += 2) {
            for (int n = 4; n < 20 - k; n += 2) {
                System.out.printf("Processing n=%d, k=%d ...%n", n, k);
                processAndCollect(n, k, jsonRoot);
            }
        }

        writeJson(jsonRoot, OUTPUT_FILE);
        System.out.println("Done. Results written to " + OUTPUT_FILE);
    }

    // -------------------------------------------------------------------------

    private static void processAndCollect(int n, int k,
                                          LinkedHashMap<String, Object> jsonRoot) {
        String key = "n=" + n + ",k=" + k;
        LinkedHashMap<String, Object> entry = new LinkedHashMap<>();

        if (Math.pow(k, n) / 2 > MAX_SEQ_LEN) {
            entry.put("skipped", "sequence too long");
            jsonRoot.put(key, entry);
            return;
        }

        // Build the base sequence (NAS lift + punctured cycle, before extra binary seqs)
        String baseSeq = ConstructEvenEven.constructBaseSequence(n, k);

        if (baseSeq == null || baseSeq.equals("EMPTY-STRING") || baseSeq.isEmpty()) {
            entry.put("error", "constructBaseSequence returned no sequence");
            jsonRoot.put(key, entry);
            return;
        }

        entry.put("original_sequence", baseSeq);

        entry.put("original_sequence_length", baseSeq.length());

        // Get the translated binary/CF sequences the construction would insert
        ArrayList<String> extraSeqs = ConstructEvenEven.getExtraSequences(n, k);

        LinkedHashMap<String, Object> seqsRequired = new LinkedHashMap<>();
        for (String seq : extraSeqs) {
            String compSeq = complementSequence(seq, k);
            ArrayList<Integer> indices =
                    ConstructEvenEven.findAllInsertionIndices(baseSeq, seq, n);
            ArrayList<Integer> compIndices =
                    ConstructEvenEven.findAllInsertionIndices(baseSeq, compSeq, n);
            LinkedHashMap<String, Object> seqEntry = new LinkedHashMap<>();
            seqEntry.put("length", seq.length());
            seqEntry.put("indices_that_can_be_inserted_at", indices);
            seqEntry.put("complement_sequence", compSeq);
            seqEntry.put("complement_indices_that_can_be_inserted_at", compIndices);
            seqsRequired.put(seq, seqEntry);
        }

        entry.put("sequences_required", seqsRequired);
        jsonRoot.put(key, entry);
    }

    // -------------------------------------------------------------------------
    // Minimal JSON serialiser (no external dependencies)
    // -------------------------------------------------------------------------

    private static void writeJson(LinkedHashMap<String, Object> root, String path)
            throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.print(toJson(root, 0));
            pw.println();
        }
    }

    @SuppressWarnings("unchecked")
    private static String toJson(Object obj, int depth) {
        String pad  = "    ".repeat(depth);
        String pad1 = "    ".repeat(depth + 1);

        if (obj instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) obj;
            if (map.isEmpty()) return "{}";
            StringBuilder sb = new StringBuilder("{\n");
            int i = 0, size = map.size();
            for (Map.Entry<String, Object> e : map.entrySet()) {
                sb.append(pad1)
                  .append('"').append(escJson(e.getKey())).append("\": ")
                  .append(toJson(e.getValue(), depth + 1));
                if (++i < size) sb.append(',');
                sb.append('\n');
            }
            sb.append(pad).append('}');
            return sb.toString();

        } else if (obj instanceof ArrayList) {
            ArrayList<?> list = (ArrayList<?>) obj;
            if (list.isEmpty()) return "[]";
            // Render integer lists inline for readability
            if (!list.isEmpty() && list.get(0) instanceof Integer) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    sb.append(list.get(i));
                    if (i + 1 < list.size()) sb.append(", ");
                }
                sb.append(']');
                return sb.toString();
            }
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append(pad1).append(toJson(list.get(i), depth + 1));
                if (i + 1 < list.size()) sb.append(',');
                sb.append('\n');
            }
            sb.append(pad).append(']');
            return sb.toString();

        } else if (obj instanceof Boolean) {
            return obj.toString();

        } else if (obj instanceof String) {
            return '"' + escJson((String) obj) + '"';

        } else {
            return String.valueOf(obj);
        }
    }

    private static String escJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // Complement every symbol in a k-ary sequence: s -> k-1-s
    private static String complementSequence(String seq, int k) {
        StringBuilder sb = new StringBuilder();
        for (char c : seq.toCharArray()) {
            int v  = (c >= 'A') ? (c - 'A' + 10) : (c - '0');
            int cv = k - 1 - v;
            sb.append(cv < 10 ? (char) ('0' + cv) : (char) ('A' + cv - 10));
        }
        return sb.toString();
    }
}

