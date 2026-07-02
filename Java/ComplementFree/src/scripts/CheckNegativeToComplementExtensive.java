package scripts;

import checker.Checker;
import construction.ConstructEvenEven;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CheckNegativeToComplementExtensive {

    static final int MAX_SEQ_LEN = 5_000_000;
    static final String OUTPUT_FILE =
            "C:\\Users\\Admin\\research\\JSON Data\\negative_to_complement_extensive.json";

    public static void main(String[] args) throws Exception {
        LinkedHashMap<String, Object> jsonRoot = new LinkedHashMap<>();

        for (int k = 4; k <= 14; k += 2) {
            for (int n = 4; n <= 8; n += 2) {
                System.out.printf("Processing n=%d, k=%d ...%n", n, k);
                processAndCollect(k, n, jsonRoot);
            }
        }

        writeJson(jsonRoot, OUTPUT_FILE);
        System.out.println("Done. Results written to " + OUTPUT_FILE);
    }

    // -------------------------------------------------------------------------

    private static void processAndCollect(int k, int n,
                                          LinkedHashMap<String, Object> jsonRoot) {
        String key = "n=" + n + ",k=" + k;
        LinkedHashMap<String, Object> entry = new LinkedHashMap<>();

        // ---- Step 1: generate CAS via ConstructEvenEven ------------------------------
        String cas = ConstructEvenEven.constructEvenEven(n, k);

        if (cas == null || cas.equals("EMPTY-STRING") || cas.isEmpty()) {
            entry.put("error", "constructEvenEven returned no sequence");
            jsonRoot.put(key, entry);
            return;
        }

        if (cas.length() > MAX_SEQ_LEN) {
            entry.put("skipped", "CAS too large (len=" + cas.length() + ")");
            jsonRoot.put(key, entry);
            return;
        }

        entry.put("CAS", cas);

        // ---- Step 2: collect missing complement pairs --------------------------------
        ArrayList<String[]> missing = Checker.getMissingPairs(cas, k, n);

        if (missing == null) {
            // CAS is not complement-free
            entry.put("CF", false);
            entry.put("MissingStrings", new LinkedHashMap<>());
        } else {
            entry.put("CF", true);
            LinkedHashMap<String, String> pairMap = new LinkedHashMap<>();
            for (String[] pair : missing) {
                pairMap.put(pair[0], pair[1]);
            }
            entry.put("MissingStrings", pairMap);
        }

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
}
