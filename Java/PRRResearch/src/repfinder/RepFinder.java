package repfinder;

import java.util.*;

public class RepFinder {
	/*****************************************
	 * Enter n value here to use the program *
	 *****************************************/
	public static int n = 8;
	
	public static ArrayList<String> RLreps = new ArrayList<String>();
	public static HashMap<String, ArrayList<String>> cycles = new HashMap<String, ArrayList<String>>();
	
	static long total;
	static long currentStringCount = 0;
	
	public static void main(String[] args) {
		function();
	}
	
	public static void function() {
		total = (long) Math.pow(2, n);
		
		findReps(); //populate cycles
		
		System.out.println("Representatives:");
		for(String str: RLreps) {
			System.out.println(str);
		}
		
		System.out.println("\n\nNumber of representatives: " + sortedLexCycles.size());
	}
	
	/****************************
	 * Method to populate cycles map and create a lex ordered tree map
	 ****************************/
	public static void findReps() {
		String currentStr = "";
		for(int i = 0; i < n; i++) { //create an all 0s string as first
			currentStr += "0";
		}
		
		
	}
	
	public static String getRLrep(String str) {
		String runlength = getRunLength(str);
		
		
	}
	
	/********************************************
	 * Method to find the runlength of a string
	 * @param str - the initial string
	 * @return returns  the runlength of the string
	 ********************************************/
	public static String getRunLength(String str) {
	    if (str == null || str.length() == 0) {
	        return "";
	    }

	    StringBuilder result = new StringBuilder();
	    int count = 1;

	    for (int i = 1; i < str.length(); i++) {
	        if (str.charAt(i) == str.charAt(i - 1)) {
	            count++;
	        } else {
	            result.append(count);
	            count = 1;
	        }
	    }

	    // Append last run
	    result.append(count);

	    return result.toString();
	}
	
	/***************************************
	 * Finds the next string in lexicographic order of an n-length string
	 * @param str - initial string
	 * @return returns next string in lex order
	 ***************************************/
	public static String nextLex(String str) {
	    char[] arr = str.toCharArray();
	    int i = arr.length - 1;

	    // Move left past trailing '1's
	    while (i >= 0 && arr[i] == '1') {
	        arr[i] = '0';
	        i--;
	    }

	    // If we ran out of digits, no next string exists
	    if (i < 0) {
	        return null; // or throw exception
	    }

	    // Flip the first '0' we encounter
	    arr[i] = '1';

	    return new String(arr);
	}
	
	/*****************************************
	 * Method to find the next cycle in the same equivalence class after a string using the CCR feedback function
	 * @param str - initial string
	 * @return returns the next cycle by CCR
	 *****************************************/
	public static String nextCyclePRR(String str) {
		String nextStr = str.substring(1); //copy the last n-1 digits as the first n-1 digit
		nextStr += nextChar(str); //add a_1 plus 1 modulus k
		
		return nextStr;
	}
	
	/*****************************************
	 * Method to rotate the string to the next cycle
	 * @param str - initial String
	 * @return return the next cycle
	 *****************************************/
	public static String nextCycle(String str) {
		String nextStr = str.substring(1); //copy the last n-1 digits as the first n-1 digit
		nextStr += str.charAt(0); //copy the first
		
		return nextStr;
	}
	
	/****************************************
	 * Method to find the next char after a string using the PRR feedback function
	 * @param str - initial string
	 * @return returns the next char
	 ****************************************/
	public static char nextChar(String str) {
		int next = charToInt(str.charAt(0)) + charToInt(str.charAt(1)) + charToInt(str.charAt(n-1));
		next = next % 2;
		
		char nextChar = (char) next;
		
		return nextChar; 
	}
	
	private static int charToInt(char c) {
	    return c - '0';
	}
}
