package repfinder;

import java.util.*;

public class RepFinder {
	static ArrayList<String> reps = new ArrayList<String>();
	static HashMap<String, ArrayList<String>> cycles = new HashMap<String, ArrayList<String>>();
	static int n = 4;
	static int k = 2;
	static int total = (int) Math.pow(k, n);
	
	public static void main(String[] args) {
		String str = "1101";
		System.out.println(getRep(str));
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
			
			
		}
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
