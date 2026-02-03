package ccr2construction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CCR2Optimal {
	/************************************************
	 * Enter n and k values here to use the program *
	 ************************************************/
	public static int n = 6;
	public static int k = 2;
	
	public static long total;
	
	public static String sequence = ""; //The entire sequence will be concatenated here using recursion
	
	public static HashSet<String> reps = new HashSet<String>(); //Arraylist to store all conecklaces
	
	public static void main(String[] args) {
		function();
	}
	
	/****************************
	 * Main function migrated here to be used with other classes as well
	 ****************************/
	public static void function() {
		total = (long) Math.pow(k, n);
		findReps();
		
		System.out.println("Reps found");
		
		String start = "";
		
		for(int i = 0; i < n; i++) {
			start += "0";
		}
		
		start = extendString(start);
		
		int traversal = 1; //traversal type, 1 for left concatenation tree, 0 for right concatenation tree
		int changeIndex = n-1;
		
		recursiveConcat(start, changeIndex, traversal);
		
 		System.out.println(sequence);
 		
 		DBChecker(sequence);
	}
	
	/***************************************************
	 * Recursive method to concatenate strings based on RCL traversal
	 * @param extString - current node
	 * @param changeIndex - change index of the current node
	 * @param traversal - concatenation tree type, 1 for left, 0 for right
	 ***************************************************/
	public static void recursiveConcat(String extString, int changeIndex, int traversal) {
		//check right children
		for(int i = changeIndex + traversal; i < n; i++) {
			String child = findChildCCR2(extString, i);
			
			if(child != null) {
				recursiveConcat(child, i, traversal);
			}
		}
		
		sequence += " ";
		sequence += leastPeriod(extString);
		sequence += " ";
		
		//check left children
		for(int i = 0; i < changeIndex + traversal; i++) {
			String child = findChildCCR2(extString, i);
			
			if(child != null) {
				recursiveConcat(child, i, traversal);
			}
		}
	}
	
	/**************************************************************
	 * Method to find a child of a string at a given index based on CCR2 parent rule
	 * @param extString - extended parent string
	 * @param index - index to find a child at
	 * @return returns the child string OR null if no children were found
	 **************************************************************/
	public static String findChildCCR2(String extString, int index) {
		int value = extString.charAt(index) - '0'; //get the integer value of the index
		
		if(value == k-1 && value != 0) { //if the value is already the highest digit then it has no children
			return null;
		}
		
		//for loop to check if all zeros preceding the current index
		//If there is a zero before the current index then it cannot possibly have children there
		for(int i = 0; i < index; i++) {
			if(extString.charAt(i) != '0') {
				return null;
			}
		}
		
		for(int newValue = value + 1; newValue <= k-1; newValue++) { //loop through all possible value the child may have at that index
			StringBuilder firstSection = new StringBuilder(); //get the first section of possible child
			firstSection.append(extString, 0, index);
			firstSection.append(newValue);
			firstSection.append(extString, index+1, n);
			
			String newExtended = extendString(firstSection.toString()); //find extended version of possible child
			
			if(reps.contains(newExtended)) { //IF the extended possible child is a conecklace then it is a valid child
				return newExtended;
			}
		}
		
		return null; //Return null if we went through all possible children at this index
	}
	
	/**********************************************
	 * Method to check if a string is a DB sequence for a specific n,k universe
	 * This algorithm converts every n-length string into a base-k value, and uses the value to keep track of duplicates
	 * @param testStr - the string to be tested
	 * @return returns true if DB, false if not DB sequence
	 **********************************************/
	public static boolean DBChecker(String testStr) {
		testStr = testStr.replace(" ", "").replace("\\", "").replace("·", "");
		
	    int total = 1;
	    for (int i = 0; i < n; i++) total *= k;

	    // Length check
	    if (testStr.length() != total) {
	    	System.out.println("Not DB, doesn't satisfy length requirements, length: " + testStr.length() + ", expected: " + total);
	        return false;
	    }

	    // Seen substrings
	    boolean[] seen = new boolean[total];

	    // Extend string for cyclic wraparound
	    testStr = testStr + testStr.substring(0, n - 1);

	    int value = 0;
	    int base = 1;
	    for (int i = 0; i < n - 1; i++) base *= k;

	    // Initialize first window
	    for (int i = 0; i < n; i++) {
	        value = value * k + charToInt(testStr.charAt(i));
	    }

	    for (int i = 0; i < total; i++) {
	        if (seen[value]) {
	        	System.out.println("Not DB, duplicate string: " + testStr.substring(i, i+n));
	            return false; // duplicate substring
	        }
	        seen[value] = true;

	        // Rolling window update
	        if (i + n < testStr.length()) {
	            value = (value % base) * k + charToInt(testStr.charAt(i + n));
	        }
	    }
	    
	    System.out.println("String is a DB");
	    return true;
	}

	private static int charToInt(char c) {
	    return c - '0';
	}
	
	/****************************
	 * Method to populate reps list
	 ****************************/
	public static void findReps() {
		String currentStr = "";
		for(int i = 0; i < n; i++) {
			currentStr += "0";
		}
		
		int currentStringCount = 0;
		
		for(int i = 0; i < total; i++) {
			String rep = getRep(currentStr);
			
			if(reps.contains(rep)) { //if the representative is in the hashmap then move to the next one
				currentStr = nextLex(currentStr);
				continue;
			}
			
			currentStringCount += leastPeriod(rep).length();
			
			reps.add(rep);
			
			if(currentStringCount == total) {
				break;
			}
			
			 currentStr = nextLex(currentStr);
		}
	}
	
	/*************************************************
	 * Method to find the least period of a string to concatenate it - based entirely on the KMP algorithm
	 * @param extString - the extended string to find the period of
	 * @return returns the least period
	 *************************************************/
	public static String leastPeriod(String extString) {
		int kn = extString.length();
        int[] pi = new int[kn];
        
        // Build the prefix function (KMP)
        for (int i = 1; i < kn; i++) {
            int j = pi[i - 1];
            while (j > 0 && extString.charAt(i) != extString.charAt(j)) {
                j = pi[j - 1];
            }
            if (extString.charAt(i) == extString.charAt(j)) {
                j++;
            }
            pi[i] = j;
        }

        int periodLength = kn - pi[kn - 1];
        if (kn % periodLength != 0) {
            periodLength = kn;  // The whole string is the period
        }

        return extString.substring(0, periodLength);
	}
	
	/**************************************
	 * Method to find the representative k*n length string of an initial string
	 * @param str - initial string
	 * @return returns the representative
	 **************************************/
	public static String getRep(String str) {
		String extendedStr = extendString(str);
		String rep = lexLeast(extendedStr);
		
		return rep; //Return the found rep
	}
	
	/****************************************
	 * Method to find the lex least rotation of a string based on Booth's algorithm
	 * @param str - the initial string
	 * @return returns the lex least rotation of the string
	 ****************************************/
	public static String lexLeast(String str) {
		String ss = str + str;
		
		int i = 0, j = 1, m = 0;
		
		while (i < n && j < n && m < n) {
	        char a = ss.charAt(i + m);
	        char b = ss.charAt(j + m);

	        if (a == b) {
	            m++;
	        } else if (a > b) {
	            i = i + m + 1;
	            if (i <= j) i = j + 1;
	            m = 0;
	        } else {
	            j = j + m + 1;
	            if (j <= i) j = i + 1;
	            m = 0;
	        }
	    }
		
		int start = Math.min(i, j);
	    return ss.substring(start, start + n);
	}
	
	/********************************************
	 * Method to extend a n-length string to its CCR function based long string
	 * @param str - initial string
	 * @return returns the extended form of the string
	 ********************************************/
	public static String extendString(String str) {
		StringBuilder extendedStr = new StringBuilder(str); //start with the string itself
		
		if(extendedStr.length() == k*n) return extendedStr.toString();
		
		//extend the string
		for(int k2 = 1; k2 < k; k2++) {
			for(int n2 = 0; n2 < n; n2++) {
				extendedStr.append(modK(extendedStr.charAt(n2) + k2));
			}
		}
		
		return extendedStr.toString();
	}
	
	/****************************************
	 * Method to find the next char after a string using the CCR feedback function
	 * @param str - initial string
	 * @return returns the next char
	 ****************************************/
	public static String nextChar(String str) {
		String nextChar = "";
		nextChar += (str.charAt(0) - '0' + 1) % k; //add a_1 plus 1 modulus k
		
		return nextChar; 
	}
	
	/***********************
	 * Method to find number modulo k
	 * @param num - initial number
	 * @return returns the number modulo k
	 ***********************/
	public static int modK(int num) {
		int value = num % k;
		if(value < 0) {
			value = value + k;
		}
		
		return value;
	}
	
	/***************************************
	 * Finds the next string in lexicographic order of an n-length string
	 * @param str - initial string
	 * @return returns next string in lex order
	 ***************************************/
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
}
