package prefsame;

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
	public static int n = 2; //This value is actually n-1 since the program is based on n-length strings, so the actual n value is n+1, but for ease of use we will just use n as the input value and treat it as n-1 in the program
	
	public static int k = 12;
	
	public static long total;
	
	public static String sequence = ""; //The entire sequence will be concatenated here using recursion

	public static String actual = ""
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
				//System.out.println("Checking child of " + extString + " at index " + index + " of section " + section + " with changeIndex " + changeIndex + " and traversal " + traversal);	
				
				//Check child at each index
				ChildReturn childReturn = findChild(extString, index, CCR, runlength);
				
				//System.out.println("Index " + index + " child return: " + childReturn);
				
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
		
		//System.out.println("Found child at index " + index + " of section " + section + " with changeIndex " + index % n);
		//System.out.println("Parent runlength: " + runlength + ", i: " + i + ", curIndex: " + curIndex);
		
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
		
		//System.out.println("Expected parent runlength: " + expectedParentRunlength + ", expected parent: " + expectedParent);
		
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
			
			//System.out.println("Rotation " + i + ": " + rotated + ", firstValue: " + curDigit + ", lexMaxRunlength: " + lexMaxRunlength);
		}
		
		//System.out.println("Lexicographically maximum runlength: " + lexMaxRunlength + ", rotations: " + rotations);
		
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
		
		//System.out.println("Parent runlength: " + parentRunlength + ", parent: " + parent + ", firstValue: " + firstValue);
		
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
