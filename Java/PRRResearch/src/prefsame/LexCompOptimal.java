package prefsame;

import java.util.ArrayList;

public class LexCompOptimal {
	/*****************************************
	 * Enter n value here to use the program *
	 *****************************************/
	public static int n = 7;
	
	public static int k = 2;
	
	public static long total;
	
	public static String sequence = ""; //The entire sequence will be concatenated here using recursion

	public static String actual = ""
			+ "0101010111111110000000011111101111100111110100000010000011000001011110001111001000011100001101111011000010011110101110001000111011100110001100111001011101101110100111010100001010001101000100100010110001010110011011001010011001001101011011010010010110101010"
			+ "";
	
	public static String defaultString = "";
	
	public static void main(String[] args) {
		//System.out.println(findChild("11111111110", 10, false));
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
		
		String start = "";

		if(n % 2 == 1) {
			for(int i = 0; i < n; i++) {
				start += "10";
			}
		}
		else {
			for(int i = 0; i < n/2; i++) {
				start += "10";
			}
		}
		
		int traversal = 0; //traversal type, 1 for left concatenation tree, 0 for right concatenation tree
		int changeIndex = 0;
		
		if(n % 2 == 1) {
			recursiveConcat(start, changeIndex, traversal, true);
		}
		else {
			recursiveConcat(start, changeIndex, traversal, false);
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
	public static void recursiveConcat(String extString, int changeIndex, int traversal, boolean CCR) {
		if(extString.equals(defaultString)) {
			sequence += defaultString.substring(0, n);
			sequence += "10";
			sequence += defaultString.substring(n, k*n);
			sequence += " ";
			return;
		}
		
		String least = leastPeriod(extString);
		int leastLength = least.length();
		
		sequence += " ";
		
		//outer loop runs for each n-length section
		for(int section = 0; section < Math.min((leastLength / n) + 1, k); section++) {
			//inner loop runs n times
			for(int index = changeIndex + traversal + section * n; index < Math.min(leastLength, ((section + 1) * n)); index++) {
				//Check child at each index
				String child = findChild(extString, index, CCR);
				
				if(child != null) {
					recursiveConcat(child, index % n, traversal, !CCR);
				}
				else if (index != changeIndex + section * n) {
					break;
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
	 * @return returns the child string OR null if no children were found
	 **************************************************************/
	public static String findChild(String extString, int index, boolean CCR) {
		int section = index / n;
		int sectionIndex = index % n;
		
		String subParent = extString.substring(section * n, (section + 1) * n);
		
		int firstValue = charToInt(subParent.charAt(0));
		
		String parentRunlength = getRunlength(subParent);
		
		int curIndex = 0;
		int i = 0;
		
		while(curIndex != sectionIndex) {
			curIndex += charToInt(parentRunlength.charAt(i));
			
			if(curIndex > sectionIndex) {
				return null;
			}
			
			i++;
		}
		
		String subChild;
		String childRunlength = "";
		
		if(i == 0) {
			subChild = constructStringFromRunlength(parentRunlength, modK(firstValue+1));
			childRunlength = parentRunlength;
		}
		else {
			childRunlength += parentRunlength.substring(0, i-1);
			
			int newRunlengthDigit = charToInt(parentRunlength.charAt(i-1)) + charToInt(parentRunlength.charAt(i));
			
			childRunlength += (char)(newRunlengthDigit + '0');
			
			childRunlength += parentRunlength.substring(i+1);
			
			subChild = constructStringFromRunlength(childRunlength, firstValue);
		}
		
		parentReturn parentReturnValue = parent(subChild, CCR);
		String expectedChild = parentReturnValue.child;
		String expectedParentRunlength = parentReturnValue.parentRunlength;
		int expectedChangeIndex = parentReturnValue.changeIndex;
		
		if(expectedChild.equals(subChild) && expectedParentRunlength.equals(parentRunlength) && expectedChangeIndex == (index % n)) {
			if(!CCR) {
				return extendCCR(subChild);
			}
			else {
				return subChild;
			}
		}
		
		return null;
	}
	
	/********************************************************
	 * method to find the parent runlength of a child node
	 * @param subChild - the child node
	 * @param CCR - the CCR status of the parent
	 * @return
	 ********************************************************/
	public static parentReturn parent(String subChild, boolean CCR) {
		String rotated = subChild;
		
		int rotations = 0;
		//Get to the correct form for runlength
		if(CCR) {
			while(rotated.charAt(0) == rotated.charAt(n-1)) {
				rotated = rotatePCR(rotated);
				rotations++;
			}
		}
		else {
			while(rotated.charAt(0) != rotated.charAt(n-1)) {
				rotated = rotateCCR(rotated);
				rotations++;
			}
		}
		
		String runlength = getRunlength(rotated);
		
		if(!CCR) {
			runlength += runlength;
			rotated = extendCCR(rotated);
		}
		
		int currentIndex = 0;
		int largest = 0;
		int largestIndex = 0;
		int largestDigit = 0;
		boolean conflict = false;
		ArrayList<conflictPair> conflictIndices = new ArrayList<conflictPair>();
		for(int i = 0; i < runlength.length(); i++) {
			int curDigit = charToInt(runlength.charAt(i));
			
			if(curDigit > largest) {
				largestIndex = i;
				largest = curDigit;
				largestDigit = charToInt(rotated.charAt(currentIndex));
				conflictIndices.clear();
				conflict = false;
			}
			else if(curDigit == largest) {
				conflictIndices.add(new conflictPair(i, charToInt(rotated.charAt(currentIndex))));
				conflict = true;
			}
			
			currentIndex += curDigit;
		}
		
		String parentRunlength = "";
		
		String childRunlength = runlength;
		
		if(!CCR) {
			childRunlength = runlength.substring(0, runlength.length() / 2);
		}
		
		int rotatedIndex = 0;
		for(int i = 0; i < largestIndex; i++) {
			childRunlength = rotatePCR(childRunlength);
			rotatedIndex += charToInt(runlength.charAt(i));
		}
		
		String child = constructStringFromRunlength(childRunlength, largestDigit);
		
		if(conflict) {
			for(int i = 0; i < conflictIndices.size(); i++) {
				conflictPair curPair = conflictIndices.get(i);
				int curIndex = curPair.index;
				int curDigit = curPair.digit;
				
				String cchildRunlength = childRunlength;
				
				for(int j = 0; j < curIndex - largestIndex; j++) {
					cchildRunlength = rotatePCR(cchildRunlength);
				}
				
				String cchild = constructStringFromRunlength(cchildRunlength, curDigit);
				
				//System.out.println(child + "   " + cchild);
				
				if(cchildRunlength.compareTo(childRunlength) > 0) {
					childRunlength = cchildRunlength;
					child = cchild;
				}
				else if(cchildRunlength.compareTo(childRunlength) == 0) {
					if(cchild.compareTo(child) > 0) {
						childRunlength = cchildRunlength;
						child = cchild;
					}
				}
			}
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
		
		//Finding change index to compare later
		int stringChangeIndex = -1;
		for(int i = 0; i <= changeIndex; i++) {
			stringChangeIndex += charToInt(childRunlength.charAt(i));
		}
		
		int actualIndex = (stringChangeIndex + rotations + rotatedIndex) % n;
		
		for(int i = 0; i < (n - (rotations + rotatedIndex) % n); i++) {
			child = rotatePCR(child);
		}
		
		return new parentReturn(child, parentRunlength, actualIndex);
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
