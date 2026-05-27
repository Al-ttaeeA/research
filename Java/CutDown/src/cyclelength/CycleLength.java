package cyclelength;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CycleLength {
    public static int n = 8;
    public static int k = 2;

    public static String sequence = ""
    		+ "0000000010001111011100001001001101101100100101110110100010011111011000001010011101011000101010110101010010101111010100001011001101001100101101110100100010111111010000001100011100111000110011110011000011010111001010001101111100100000111011110001000011111111"
    		+ ""
    		+ "";
    
    public static HashMap<String, Integer> cycleLengths = new HashMap<>();

    public static void main(String[] args) {
        function();
        fixCycleLengths();

        LinkedHashMap<String, Integer> sortedByKey = sortByKey();
        LinkedHashMap<String, Integer> sortedByCycleLength = sortByCycleLength();

        System.out.println("Sorted by key (lexicographic):");
        for (Map.Entry<String, Integer> entry : sortedByKey.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nSorted by cycle length (decreasing):");
        for (Map.Entry<String, Integer> entry : sortedByCycleLength.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static void fixCycleLengths() {
        int maxDiff = (int) Math.pow(k, n - 1);
        int seqLength = (int) Math.pow(k, n);
        for (Map.Entry<String, Integer> entry : cycleLengths.entrySet()) {
            if (entry.getValue() > maxDiff) {
                cycleLengths.put(entry.getKey(), seqLength - entry.getValue());
            }
        }
    }

    public static LinkedHashMap<String, Integer> sortByKey() {
        List<String> keys = new ArrayList<>(cycleLengths.keySet());
        Collections.sort(keys);
        LinkedHashMap<String, Integer> sorted = new LinkedHashMap<>();
        for (String key : keys) {
            sorted.put(key, cycleLengths.get(key));
        }
        return sorted;
    }

    public static LinkedHashMap<String, Integer> sortByCycleLength() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(cycleLengths.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        LinkedHashMap<String, Integer> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sorted.put(entry.getKey(), entry.getValue());
        }
        return sorted;
    }

    public static void function(){
    	sequence = sequence + sequence.substring(0, n-1);
    	
        int total = (int) Math.pow(k, n-1);

        int newN = n-1;

        int count = 0;
        int i = 0;

        while (count < total) {
            String current = sequence.substring(i, i+newN);
            
            System.out.println("Checking: " + current);

            if(cycleLengths.containsKey(current)){
            	i++;
                continue;
            }

            for(int j = i+1; j < sequence.length(); j++){
                if(sequence.substring(j, j+newN).equals(current)){
                    cycleLengths.put(current, j-i);
                    break;
                }
            }
            i++;
            count++;
            
            System.out.println("Count: " + count + ", Current: " + current);
            
            if(i + newN > sequence.length()){
				break;
			}
        }
    }
}
