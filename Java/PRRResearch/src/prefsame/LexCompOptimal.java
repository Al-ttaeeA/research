package prefsame;

import java.util.ArrayList;

public class LexCompOptimal {
	static class ParentReturn {
		public String parent;
		public String parentRunlength;
		
		ParentReturn(String parent, String parentRunlength){
			this.parent = parent;
			this.parentRunlength = parentRunlength;
		}
		
		public String toString() {
			return "{" + parent + " , " + parentRunlength + "}";
		}
	}
	
	static class ChildReturn {
		public String child;
		public String childRunlength;
		
		ChildReturn(String child, String childRunlength){
			this.child = child;
			this.childRunlength = childRunlength;
		}
		
		public String toString() {
			return "{" + child + " , " + childRunlength + "}";
		}
	}
	
	
	
	/*****************************************
	 * Enter n value here to use the program *
	 *****************************************/
	public static int n = 12; //This value is actually n-1 since the program is based on n-length strings, so the actual n value is n+1, but for ease of use we will just use n as the input value and treat it as n-1 in the program
	
	public static int k = 2;
	
	public static long total;
	
	public static String sequence = ""; //The entire sequence will be concatenated here using recursion

	public static String actual = ""
			+ "11111111111110000000000000111111111110111111111100111111111101000000000001000000000011000000000010111111111000111111111001000000000111000000000110111111111011000000000100111111111010111111110000111111110001000000001111000000001110111111110011000000001100111111110010111111110111000000001000111111110110111111110100111111110101000000000101000000001101000000001001000000001011000000001010111111100000111111100001000000011111000000011110111111100011000000011100111111100010111111100111000000011000111111100110111111100100111111100101000000011101000000011001000000011011000000011010111111101111000000010000111111101110111111101100111111101101000000010001000000010011000000010010111111101000111111101001000000010111000000010110111111101011000000010100111111101010111111000000111111000001000000111110111111000011000000111100111111000010111111000111000000111000111111000110111111000100111111000101000000111101000000111001000000111011000000111010111111001111000000110000111111001110111111001100111111001101000000110001000000110011000000110010111111001000111111001001000000110111000000110110111111001011000000110100111111001010111111011111000000100000111111011110111111011100111111011101000000100001000000100011000000100010111111011000111111011001000000100111000000100110111111011011000000100100111111011010111111010000111111010001000000101111000000101110111111010011000000101100111111010010111111010111000000101000111111010110111111010100111111010101000000010101000000110101000000100101000000101101000000101001000000101011000000101010111110000011000001111100111110000010111110000111000001111000111110000110111110000100111110000101000001111101000001111001000001111011000001111010111110001111000001110000111110001110111110001100111110001101000001110001000001110011000001110010111110001000111110001001000001110111000001110110111110001011000001110100111110001010111110011110111110011100111110011101000001100001000001100011000001100010111110011000111110011001000001100111000001100110111110011011000001100100111110011010111110010000111110010001000001101111000001101110111110010011000001101100111110010010111110010111000001101000111110010110111110010100111110010101000001110101000001100101000001101101000001101001000001101011000001101010111110111110111100111110111101000001000001000011000001000010111110111000111110111001000001000111000001000110111110111011000001000100111110111010111110110000111110110001000001001111000001001110111110110011000001001100111110110010111110110111000001001000111110110110111110110100111110110101000001000101000001001101000001001001000001001011000001001010111110100001000001011110111110100011000001011100111110100010111110100111000001011000111110100110111110100100111110100101000001011101000001011001000001011011000001011010111110101111000001010000111110101110111110101100111110101101000001010001000001010011000001010010111110101000111110101001000001010111000001010110111110101011000001010100111110101010111100001111000011101111000011001111000011010000111100010000111100110000111100101111000010001111000010010000111101110000111101101111000010110000111101001111000010101111000111101111000111001111000111010000111000010000111000110000111000101111000110001111000110010000111001110000111001101111000110110000111001001111000110101111000100010000111011101111000100110000111011001111000100101111000101110000111010001111000101101111000101001111000101010000111101010000111001010000111011010000111010010000111010110000111010101111001111001111010000110000110000101111001110001111001110010000110001110000110001101111001110110000110001001111001110101111001100010000110011101111001100110000110011001111001100101111001101110000110010001111001101101111001101001111001101010000110001010000110011010000110010010000110010110000110010101111001000010000110111101111001000110000110111001111001000101111001001110000110110001111001001101111001001001111001001010000110111010000110110010000110110110000110110101111001011101111001011001111001011010000110100010000110100110000110100101111001010001111001010010000110101110000110101101111001010110000110101001111001010101111011110110000100001001111011110101111011100010000100011101111011100110000100011001111011100101111011101110000100010001111011101101111011101001111011101010000100001010000100011010000100010010000100010110000100010101111011000110000100111001111011000101111011001110000100110001111011001101111011001001111011001010000100111010000100110010000100110110000100110101111011011101111011011001111011011010000100100010000100100110000100100101111011010001111011010010000100101110000100101101111011010110000100101001111011010101111010000101111010001110000101110001111010001101111010001001111010001010000101110010000101110110000101110101111010011101111010011001111010011010000101100010000101100110000101100101111010010001111010010010000101101110000101101101111010010110000101101001111010010101111010111001111010111010000101000110000101000101111010110001111010110010000101001110000101001101111010110110000101001001111010110101111010100010000101011101111010100110000101011001111010100101111010101110000101010001111010101101111010101001111010101010000010101010000110101010000100101010000101101010000101001010000101011010000101010010000101010110000101010101110001110001110010001110001101110001110110001110001001110001110101110001100010001110011101110001100110001110011001110001100101110001101101110001101001110001101010001110001010001110011010001110010010001110010110001110010101110001000110001110111001110001000101110001001101110001001001110001001010001110111010001110110010001110110110001110110101110001011101110001011001110001011010001110100010001110100110001110100101110001010010001110101101110001010110001110101001110001010101110011100110001100011001110011100101110011101101110011101001110011101010001100011010001100010010001100010110001100010101110011000101110011001101110011001001110011001010001100111010001100110010001100110110001100110101110011011101110011011001110011011010001100100010001100100110001100100101110011010010001100101101110011010110001100101001110011010101110010001101110010001001110010001010001101110110001101110101110010011101110010011001110010011010001101100010001101100110001101100101110010010010001101101101110010010110001101101001110010010101110010111010001101000101110010110010001101001101110010110110001101001001110010110101110010100010001101011101110010100110001101011001110010100101110010101101110010101001110010101010001110101010001100101010001101101010001101001010001101011010001101010010001101010110001101010101110111011101100111011101101000100010001001100010001001011101110100100010001011011101110101100010001010011101110101011101100010011101100010100010011101011101100110011101100110100010011001100010011001011101100100100010011011011101100101100010011010011101100101011101101110100010010001011101101100100010010011011101101101100010010010011101101101011101101001100010010110011101101001011101101011011101101010011101101010100010001010100010011010100010010010100010010110100010010100100010010101100010010101011101000101100010111010011101000101011101001100100010110011011101001101100010110010011101001101011101001001100010110110011101001001011101001011011101001010011101001010100010111010100010110010100010110110100010110100100010110101100010110101011101011101011001100010100110011101011001011101011011011101011010011101011010100010100010100110100010100100100010100101100010100101011101010011011101010010011101010010100010101100100010101101100010101101011101010110011101010110100010101001100010101001011101010100100010101011011101010101100010101010011101010101011001100110011010011001100101100110010010011001101101100110010101100110110010011001001101100110110101100110100101100110101101100110101010011001101010011001001010011001011010011001010010011001010101100100110101100100100101100100101101100100101010011011001010011011011010011011010010011011010101100101100101101101100101101010011010011010010010011010010101100101001010011010110101100101011010011010100101100101010010011010101101100101010101101101101101010010010010010101101101001010010010110101101101011010010010100101101101010101101001011010010101011010110101011010100101011010101010100010101010100110101010100100101010100101101010100101001010100101010101010"
			+ ""
			+ "";
	
	public static String defaultString = "";
	
	public static void main(String[] args) {
		//System.out.println(findChild("11010010", 6, true, "211"));
		function();
	}
	
	/****************************
	 * Main function migrated here to be used with other classes as well
	 ****************************/
	public static void function() {
		total = (long) Math.pow(2, n);
		
		for(int i = 0; i < n; i++) {
			defaultString += "1";
		}
		
		defaultString = extendCCR(defaultString);
		
		String startRunlength = "";
		
		for(int i = 0; i < n; i++) {
			startRunlength += "1";
		}
		
		String startString = constructStringFromRunlength(startRunlength, 1);
		
		String start = startString;

		if(n % 2 == 1) {
			start = extendCCR(start);
		}
		
		int traversal = 0; //traversal type, 1 for left concatenation tree, 0 for right concatenation tree
		int changeIndex = 0;
		
		if(n % 2 == 1) {
			recursiveConcat(start, changeIndex, traversal, true, startRunlength);
		}
		else {
			recursiveConcat(start, changeIndex, traversal, false, startRunlength);
		}
		
		if(n % 2 == 1) {
			sequence += constructStringFromRunlength(startRunlength, 0);
		}
		
 		System.out.println(sequence);
		
 		DBChecker(sequence);
 		if(compare()) {
 			System.out.println("Same DB sequence");
 		}
 		else {
 			System.out.println("Different DB sequence");
 		}
	}
	
	/***************************************************
	 * Recursive method to concatenate strings based on RCL traversal
	 * @param extString - current node
	 * @param changeIndex - change index of the current node
	 * @param traversal - concatenation tree type, 1 for left, 0 for right
	 * @param CCR - 1 if the node is CCR based, 0 if PCR based
	 ***************************************************/
	public static void recursiveConcat(String extString, int changeIndex, int traversal, boolean CCR, String runlength) {
		if(extString.equals(defaultString)) {
			sequence += defaultString.substring(0, n);
			sequence += "10";
			sequence += defaultString.substring(n, k*n);
			sequence += " ";
			return;
		}
		
		boolean childFound = false;
		int numChildren = 0;
		int expectedChildren = runlength.length() - 1;
		
		if(CCR) {
			expectedChildren *= 2;
		}
		
		String least = leastPeriod(extString);
		int leastLength = least.length();
		
		sequence += " ";
		
		//outer loop runs for each n-length section
		for(int section = 0; section < Math.min((leastLength / n) + 1, k); section++) {
			childFound = false;
			
			//inner loop runs n times
			for(int index = changeIndex + traversal + section * n; index < Math.min(leastLength, ((section + 1) * n)); index++) {
				System.out.println("Checking child of " + extString + " at index " + index + " of section " + section + " with changeIndex " + changeIndex + " and traversal " + traversal);	
				
				//Check child at each index
				ChildReturn childReturn = findChild(extString, index, CCR, runlength);
				
				System.out.println("Index " + index + " child return: " + childReturn);
				
				if(childReturn != null) {
					String child = childReturn.child;
					String childRunlength = childReturn.childRunlength;
					numChildren++;
					childFound = true;
					recursiveConcat(child, index % n, traversal, !CCR, childRunlength);
				}
				else if(childFound) {
					break; //if we found a child before but not at this index, then we can stop checking for children in this section and concatenate the rest of the section	
				}
				else if(numChildren >= expectedChildren) {
					break; //if we found more children than expected, then we can stop checking for children in this section and concatenate the rest of the section
				}
			}
			
			//After checking right children of each section, concatenate the section
			sequence += least.substring(section * n, Math.min(leastLength, ((section + 1) * n)));
		}
		
		sequence += " ";
	}
	
	/**************************************************************
	 * Method to find a child of a string at a given index based on CCR1 parent rule
	 * @param extString - extended parent string
	 * @param index - index to find a child at
	 * @param CCR - The CCR status of the parent
	 * @param runlength - the runlength of the parent string
	 * @return returns the child string OR null if no children were found
	 **************************************************************/
	public static ChildReturn findChild(String extString, int index, boolean CCR, String runlength) {
		int section = index / n;
		int sectionIndex = index % n;
		
		if(extString.charAt(0) == '0') {
			return null; //if the first char is 0, then there are no children
		}
		
		String subParent = extString.substring(section * n, (section + 1) * n);
		
		int firstValue = charToInt(subParent.charAt(0));
		
		int curIndex = 0;
		int i = 0;
		
		while(curIndex != sectionIndex) {
			curIndex += charToInt(runlength.charAt(i));
			
			if(curIndex > sectionIndex) {
				return null;
			}
			
			i++;
		}
		
		if(i == 0) {
			return null;
		}
		
		System.out.println("Found child at index " + index + " of section " + section + " with changeIndex " + index % n);
		System.out.println("Parent runlength: " + runlength + ", i: " + i + ", curIndex: " + curIndex);
		
		String childRunlength = "";
		childRunlength += runlength.substring(0, i-1);
		
		int newDigit = charToInt(runlength.charAt(i-1)) + charToInt(runlength.charAt(i));
		
		childRunlength += (char)(newDigit + '0');
		
		childRunlength += runlength.substring(i+1);
		
		if(!childRunlength.equals(maxLexRotation(childRunlength))) {
			return null;
		}
		
		ParentReturn parentReturn = parent(childRunlength, (char) (firstValue + '0'), CCR);
		String expectedParentRunlength = parentReturn.parentRunlength;
		String expectedParent = parentReturn.parent;
		
		System.out.println("Expected parent runlength: " + expectedParentRunlength + ", expected parent: " + expectedParent);
		
		if(expectedParentRunlength.equals(runlength) && expectedParent.equals(subParent)) {
			String child = constructStringFromRunlength(childRunlength, firstValue);
			
			if(!CCR) {
				child = extendCCR(child);
			}
			
			ChildReturn childReturn = new ChildReturn(child, childRunlength);
			
			return childReturn;
		}
		
		return null;
	}
	
	
	
	/********************************************************
	 * method to find the parent runlength of a child node
	 * @param subChild - the child node
	 * @param CCR - the CCR status of the parent
	 * @return 
	 ********************************************************/
	public static ParentReturn parent(String childRunlength, char firstValue, boolean CCR) {
		String runlength = childRunlength;
		
		if(!CCR) {
			runlength += childRunlength;
		}
		
		String lexMaxRunlength = runlength;
		int rotations = 0;
		
		for(int i = 0; i < runlength.length(); i++) {
			String rotated = runlength.substring(i) + runlength.substring(0, i);
			
			int curDigit = (charToInt(firstValue) + i) % 2;
			
			if(rotated.compareTo(lexMaxRunlength) > 0) {
				lexMaxRunlength = rotated;
				rotations = i;
			}
			else if(rotated.compareTo(lexMaxRunlength) == 0 && curDigit == 1) {
				lexMaxRunlength = rotated;
				rotations = i;
			}
			
			System.out.println("Rotation " + i + ": " + rotated + ", firstValue: " + curDigit + ", lexMaxRunlength: " + lexMaxRunlength);
		}
		
		System.out.println("Lexicographically maximum runlength: " + lexMaxRunlength + ", rotations: " + rotations);
		
		if(rotations % 2 == 1) {
			firstValue = (char) ((charToInt(firstValue) + 1) % k + '0');
		}
		
		String parentRunlength = "";
		
		if(!CCR) {
			lexMaxRunlength = lexMaxRunlength.substring(0, lexMaxRunlength.length() / 2);
		}
		
		int changeIndex;
		for(changeIndex = childRunlength.length() - 1; changeIndex > 0; changeIndex--) {
			if(childRunlength.charAt(changeIndex) != '1') break;
		}
		
		int changeValue = charToInt(childRunlength.charAt(changeIndex));
		
		parentRunlength += childRunlength.substring(0, changeIndex);
		parentRunlength += (char) (changeValue - 1 + '0');
		parentRunlength += '1';
		parentRunlength += childRunlength.substring(changeIndex+1);
		
		String parent = constructStringFromRunlength(parentRunlength, charToInt(firstValue));
		
		System.out.println("Parent runlength: " + parentRunlength + ", parent: " + parent + ", firstValue: " + firstValue);
		
		return new ParentReturn(parent, parentRunlength);
	}
	
	
	
	
	
	/**********************************************************************************
	 * Method to construct the actual string from the runlength
	 * @param runlength - the runlength
	 * @param firstValue - the first digit
	 * @return returns the actual string
	 **********************************************************************************/
	public static String constructStringFromRunlength(String runlength, int firstValue) {
	    if (runlength == null || runlength.isEmpty()) {
	        return "";
	    }

	    StringBuilder result = new StringBuilder();
	    char current = (firstValue == 0) ? '0' : '1';

	    for (int i = 0; i < runlength.length(); i++) {
	        int count = runlength.charAt(i) - '0';

	        for (int j = 0; j < count; j++) {
	            result.append(current);
	        }

	        // flip bit
	        current = (current == '0') ? '1' : '0';
	    }

	    return result.toString();
	}
	
	/********************************************
	 * Method to find the runlength of a string
	 * @param str - the string
	 * @return returns the runlength string
	 ********************************************/
	public static String getRunlength(String str) {
	    if (str == null || str.isEmpty()) {
	        return "";
	    }

	    StringBuilder result = new StringBuilder();
	    int count = 1;

	    for (int i = 1; i < str.length(); i++) {
	        if (str.charAt(i) == str.charAt(i - 1)) {
	            count++;
	        } else {
	            result.append((char) ('0' + count));
	            count = 1;
	        }
	    }

	    // append final run
	    result.append((char) ('0' + count));

	    return result.toString();
	}
	
	/******************************
	 * Method used to compare the found DB sequence to the actual minDiscrepancy sequence
	 * @return return true if same, false if different
	 ******************************/
	public static boolean compare() {
	    String testStr = sequence.replace(" ", "")
	                             .replace("\\", "")
	                             .replace("·", "");

	    int minLen = Math.min(testStr.length(), actual.length());
	    boolean equal = true;

	    for (int i = 0; i < minLen; i++) {
	        if (testStr.charAt(i) != actual.charAt(i)) {
	            equal = false;

	            System.out.println("Difference at index " + i +
	                               " -> testStr: " + testStr.charAt(i) +
	                               ", actual: " + actual.charAt(i));

	            int start = Math.max(0, i - n + 1);
	            String window = testStr.substring(start, i + 1);

	            System.out.println("Window (length ≤ " + n + "): " + window);
	        }
	    }

	    // Handle different lengths
	    if (testStr.length() != actual.length()) {
	        equal = false;
	        System.out.println("Strings have different lengths.");
	        System.out.println("testStr length: " + testStr.length());
	        System.out.println("actual length: " + actual.length());
	    }

	    return equal;
	}
	
	/**********************************************
	 * Method to check if a string is a DB sequence for a specific n,k universe
	 * This algorithm converts every n-length string into a base-k value, and uses the value to keep track of duplicates
	 * @param testStr - the string to be tested
	 * @return returns true if DB, false if not DB sequence
	 **********************************************/
	public static boolean DBChecker(String testStr) {
		testStr = testStr.replace(" ", "").replace("\\", "").replace("·", "");
		
		int actualN = n+1;
		
	    int total = 1;
	    for (int i = 0; i < actualN; i++) total *= k;

	    // Length check
	    if (testStr.length() != total) {
	    	System.out.println("Not DB, doesn't satisfy length requirements, length: " + testStr.length() + ", expected: " + total);
	        //return false;
	    }

	    // Seen substrings
	    boolean[] seen = new boolean[total];

	    // Extend string for cyclic wraparound
	    testStr = testStr + testStr.substring(0, actualN - 1);

	    int value = 0;
	    int base = 1;
	    for (int i = 0; i < actualN - 1; i++) base *= k;

	    // Initialize first window
	    for (int i = 0; i < actualN; i++) {
	        value = value * k + charToInt(testStr.charAt(i));
	    }

	    for (int i = 0; i < total; i++) {
	        if (seen[value]) {
	        	System.out.println("Not DB, duplicate string: " + testStr.substring(i, i+actualN));
	            //return false; // duplicate substring
	        }
	        seen[value] = true;

	        // Rolling window update
	        if (i + actualN < testStr.length()) {
	            value = (value % base) * k + charToInt(testStr.charAt(i + actualN));
	        }
	    }
	    
	    System.out.println("String is a DB");
	    return true;
	}

	private static int charToInt(char c) {
	    return c - '0';
	}
	
	/*************************************************
	 * Method to find the lexicographically maximum rotation of a string using Booth's algorithm
	 * @param s - the input string
	 * @return returns the maximum rotation of the string
	 *************************************************/
	public static String maxLexRotation(String s) {
	    int n = s.length();
	    String ss = s + s;

	    int i = 0, j = 1, k = 0;

	    while (i < n && j < n && k < n) {
	        char a = ss.charAt(i + k);
	        char b = ss.charAt(j + k);

	        if (a == b) {
	            k++;
	        } else if (a < b) {
	            i = i + k + 1;
	            if (i <= j) i = j + 1;
	            k = 0;
	        } else { // a > b
	            j = j + k + 1;
	            if (j <= i) j = i + 1;
	            k = 0;
	        }
	    }

	    int start = Math.min(i, j);
	    return ss.substring(start, start + n);
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
	
	/***********************
	 * Method to find number modulo k
	 * @param num - initial number
	 * @return returns the number modulo k
	 ***********************/
	public static int modK(int num) {
		int value = num % 2;
		if(value < 0) {
			value = value + 2;
		}
		
		return value;
	}
	
	/********************************************
	 * Method to extend a n-length string to its CCR function based long string
	 * @param str - initial string
	 * @return returns the extended form of the string
	 ********************************************/
	public static String extendCCR(String str) {
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
	
	public static String rotateCCR(String str) {
		String newStr = str.substring(1) + modK(charToInt(str.charAt(0)) + 1);
		
		return newStr;
	}
	
	public static String rotatePCR(String str) {
		String newStr = str.substring(1) + str.charAt(0);
		
		return newStr;
	}

}
