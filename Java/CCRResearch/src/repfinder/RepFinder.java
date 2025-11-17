package repfinder;

import java.util.*;

public class RepFinder {
	static ArrayList<String> reps = new ArrayList<String>();
	static HashMap<String, ArrayList<String>> cycles = new HashMap<String, ArrayList<String>>();
	static TreeMap<String, ArrayList<String>> sortedLexCycles;
	static TreeMap<String, ArrayList<String>> sortedColexCycles;
	static int n = 4;
	static int k = 3;
	static int total = (int) Math.pow(k, n);
	static int currentStringCount = 0;
	
	public static void main(String[] args) {
		findReps();
		
		System.out.println("Representatives in lex order:");
		for(String key: sortedLexCycles.keySet()) {
			System.out.println(key);
		}
		
		System.out.println("\n\n");
		
		sortColex();
		
		System.out.println("Representatives in colex order:");
		for(String key: sortedColexCycles.keySet()) {
			System.out.println(key);
		}
		
		System.out.println("\n\nNumber of representatives: " + sortedLexCycles.size());
	}
	
	public static void findReps() {
		String currentStr = "";
		for(int i = 0; i < n; i++) { //create an all 0s string as first
			currentStr += "0";
		}
		
		for(int i = 0; i < total; i++) { //loop through every possible string
			String rep = getRep(currentStr);
			
			if(cycles.containsKey(rep)) { //if the representative is in the hashmap then move to the next one
				currentStr = nextLex(currentStr);
				continue;
			}
			
			//Cycles list for this representative
			ArrayList<String> currentRepCycles = new ArrayList<String>();
			String currentExtended = extendString(currentStr); //start with the current string
			int j;
			for(j = 0; j < (n*k); j++) {
				currentRepCycles.add(currentExtended); //add the current 
				currentExtended = nextCycle(currentExtended); //then cycle to the next
				
				//if the next cycle is equal to the representative then exit loop
				if(currentExtended.equals(rep)) {
					j++;
					break;
				}
			}
			
			currentStringCount += j;
			
			cycles.put(rep, currentRepCycles); //add the list to the hashmap
			
			//if the current count of strings in the hashmap is equal to total then exit the loop
			if(currentStringCount == total) {
				break;
			}
			
			//otherwise get next lex string and continue
			currentStr = nextLex(currentStr);
		}
		
		sortedLexCycles = new TreeMap<String, ArrayList<String>>(cycles);
	}
	
	public static void sortColex() {
		// Create the map with custom comparator
        sortedColexCycles = new TreeMap<>(
            (a, b) -> {
                // Extract first n characters
                String aSub = a.substring(0, n);
                String bSub = b.substring(0, n);

                // Compare colexicographically (right-to-left)
                for (int i = n - 1; i >= 0; i--) {
                    char ac = aSub.charAt(i);
                    char bc = bSub.charAt(i);
                    if (ac != bc) {
                        return Character.compare(ac, bc);
                    }
                }

                // If first n chars are equal, compare full string as tie-breaker
                return a.compareTo(b);
            }
        );

        sortedColexCycles.putAll(cycles);
	}
	
	public static String getRep(String str) {
		String extendedStr = extendString(str);
		String rep = extendedStr; //start with declaring representative as the extended string
		
		for(int i = 0; i < extendedStr.length(); i++) {
			extendedStr = nextCycle(extendedStr); //get next cycle
			
			if(extendedStr.compareTo(rep) < 0) { //if the new cycle is lexicographically less than the rep, then switch the rep
				rep = extendedStr;
			}
		}
		
		return rep; //Return the found rep
	}
	
	/********************************************
	 * Method to extend a n-length string to its CCR function based long string
	 * @param str - initial string
	 * @return returns the extended form of the string
	 */
	public static String extendString(String str) {
		String extendedStr = str; //start with the string
		
		//extend the string
		for(int i = 0; i < (k-1) * n; i++) {
			extendedStr += nextChar(extendedStr.substring(i));
		}
		
		return extendedStr;
	}
	
	/***************************************
	 * Finds the next string in lexicographic order of an n-length string
	 * @param str - initial string
	 * @return returns next string in lex order
	 */
	public static String nextLex(String str) {
		char[] chars = str.toCharArray();
        int n = chars.length;

        // Start from the last character and "add 1" in base k
        for (int i = n - 1; i >= 0; i--) {
            int digit = chars[i] - '0';  // Convert char to integer
            if (digit < k - 1) {
                // If we can increment this digit, do it
                chars[i] = (char) ('0' + digit + 1);
                // Set all digits after i to 0 (reset)
                for (int j = i + 1; j < n; j++) {
                    chars[j] = '0';
                }
                return new String(chars);
            }
            // Otherwise, set this digit to 0 and carry to the next
            chars[i] = '0';
        }

        // If all digits were max (k-1), wrap around to all zeros
        return new String(chars);
	}
	
	/*****************************************
	 * Method to find the next cycle in the same equivalence class after a string using the CCR feedback function
	 * @param str - initial string
	 * @return returns the next cycle by CCR
	 */
	public static String nextCycleCCR(String str) {
		String nextStr = str.substring(1); //copy the last n-1 digits as the first n-1 digit
		nextStr += (str.charAt(0) - '0' + 1) % k; //add a_1 plus 1 modulus k
		
		return nextStr;
	}
	
	/*****************************************
	 * Method to rotate the string to the next cycle
	 * @param str - initial String
	 * @return return the next cycle
	 */
	public static String nextCycle(String str) {
		String nextStr = str.substring(1); //copy the last n-1 digits as the first n-1 digit
		nextStr += str.charAt(0); //copy the first
		
		return nextStr;
	}
	
	/****************************************
	 * Method to find the next char after a string using the CCR feedback function
	 * @param str - initial string
	 * @return returns the next char
	 */
	public static String nextChar(String str) {
		String nextChar = "";
		nextChar += (str.charAt(0) - '0' + 1) % k; //add a_1 plus 1 modulus k
		
		return nextChar; 
	}
}
