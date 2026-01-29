package mindiscrepancy;

public class MinDiscrepancyOptimal {
	/************************************************
	 * Enter n and k values here to use the program *
	 ************************************************/
	public static int n = 8;
	public static int k = 6;
	
	public static long total;
	
	public static StringBuilder sequence = new StringBuilder(); //The entire sequence will be concatenated here using recursion
	
	public static void main(String[] args) {
		String start = "";
		
		for(int i = 0; i < n; i++) {
			start += "0";
		}
		
		start = extendString(start);
		
		recursiveConcat(start, 3, 1, 0);
		
		System.out.println(sequence);
		
		DBChecker(sequence.toString());
	}
	
	/***************************************************
	 * Recursive method to concatenate strings based on RCL traversal
	 * @param extString - current node
	 * @param changeIndex - change index of the current node
	 * @param traversal - concatenation tree type, 1 for left, 0 for right
	 * @param depth - depth of current node
	 ***************************************************/
	public static void recursiveConcat(String extString, int changeIndex, int traversal, int depth) {
		String least = leastPeriod(extString);
		int leastLength = least.length();
		
		sequence.append(" ");
		
		//outer loop runs for each n-length section
		for(int section = 0; section < Math.min((leastLength / n) + 1, k); section++) {
			//inner loop runs n times
			for(int index = Math.max(changeIndex + traversal, section * n); index < Math.min(leastLength, ((section + 1) * n)); index++) {
				int value = extString.charAt(index) - '0';
				
				int newValue = modK(value - 1);
				
				//new value must match depth
				if(newValue != modK(depth+1)) {
					continue;
				}
				
				//Check child at each index
				String child = findChild(extString, index, modK(depth+1));
				
				if(child != null) {
					recursiveConcat(child, index % n, traversal, modK(depth+1));
				}
			}
			
			//After checking right children of each section, concatenate the section
			sequence.append(least.substring(section * n, Math.min(leastLength, ((section + 1) * n))));
		}
		
		sequence.append(" ");
		
		//Left children
		for(int i = 0; i < changeIndex + traversal; i++) {
			int value = extString.charAt(i) - '0';
			
			int newValue = modK(value - 1);
			
			//new value must match depth
			if(newValue != modK(depth+1)) {
				continue;
			}
			
			String child = findChild(extString, i, modK(depth+1));
			
			if(child != null) {
				recursiveConcat(child, i, traversal, modK(depth+1));
			}
		}
	}
	
	/**************************************************************
	 * Method to find a child of a string at a given index based on CCR1 parent rule
	 * @param extString - extended parent string
	 * @param index - index to find a child at
	 * @param depth - depth of child
	 * @return returns the child string OR null if no children were found
	 **************************************************************/
	public static String findChild(String extString, int index, int depth) {
		int value = extString.charAt(index) - '0';
		
		int newValue = modK(value - 1);
		
		int section = index / n;
		int sectionIndex = index % n;
		
		String subParent = extString.substring(section * n, (section + 1) * n);
		
		String parentDiffArr = leastDifferenceArray(subParent);
		
		String subChild = subParent.substring(0, sectionIndex) + newValue + subParent.substring(sectionIndex + 1, n);
		
		String childDiffArr = leastDifferenceArray(subChild);
		
		String foundParentDiffArr = parentDiffArr(childDiffArr);
		
		//System.out.println(foundParentDiffArr + "   " + parentDiffArr + "   " + childDiffArr);
		
		if(foundParentDiffArr == null) {
			return null;
		}
		
		if(foundParentDiffArr.equals(parentDiffArr)) {
			String child = extendString(subChild);
			StringBuilder curChild = new StringBuilder();
			
			if(index < n) {
			    for(int i = index+1; i < n; i++) {
			        curChild.append(modK(charToInt(child.charAt(i)) - 1));
			    }
			    curChild.append(child, 0, index+1);
			} else {
			    curChild.append(child, index-n+1, index+1);
			}
			
			String curChildDiffArr = differenceArray(curChild.toString());
			
			String correctDiffArr = correctDifferenceArray(subChild);
			
			if(curChildDiffArr.equals(correctDiffArr)) {
				return child;
			}
		}
		
		return null;
	}
	
	/******************************************************
	 * Method to find the parent diff array of a child diff array
	 * @param childDiffArr - the child diff array
	 * @return returns the lex-least rotation of the parent diff array
	 ******************************************************/
	public static String parentDiffArr(String childDiffArr) {
		//Get the parent diff array
		int firstNonZero = 0;
		int nextDigit = 0;
		int i;
		for(i = 0; i < n; i++) {
			if(childDiffArr.charAt(i) != '0' && i != n-1) {
				firstNonZero = charToInt(childDiffArr.charAt(i));
				nextDigit = charToInt(childDiffArr.charAt(i+1));
				break;
			}
		}
		
		if(i > n-2) {
			return null;
		}
		
		int newFirstdigit = firstNonZero - 1;
		int newNextDigit = modK(nextDigit + 1);
		
		String parentDiffArr = childDiffArr.substring(0, i) + newFirstdigit + newNextDigit + childDiffArr.substring(i+2, n);
		
		String lexDiffArr = lexLeast(parentDiffArr);
		
		return lexDiffArr;
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
	        	System.out.println("Not DB, duplicate string: " + testStr.substring(i, i+n) + " at " + i);
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
	
	/******************************************************
	 * Gets the correct form of the difference array based on the algorithm
	 * @param str - the n length string
	 * @return returns the correct form of the difference array
	 ******************************************************/
	public static String correctDifferenceArray(String str) {
		String lexDiffArr = leastDifferenceArray(str);
		
		//Now we have the lex least rotation, find first non-zero index
		int i;
		for(i = 0; i < n; i++) {
			if(lexDiffArr.charAt(i) != '0') {
				break;
			}
		}
		i++;
		String correctArr = lexDiffArr.substring(i) + lexDiffArr.substring(0, i);
		
		return correctArr;
	}
	
	/****************************************************
	 * Method to find the lex least rotation of the difference array of a string - based on Booth's algorithm with O(n)
	 * @param str - initial n length string
	 * @return returns the lex least difference array
	 ****************************************************/
	public static String leastDifferenceArray(String str) {
		String diffArr = differenceArray(str);
		
		return lexLeast(diffArr);
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
	
	/****************************************
	 * Method to find the difference array of a length n string based on the Min-Discrepancy algorithm by N. Alvarez, V. Becher, and M. Mereb
	 * @param str - the length n string to find the difference array of
	 * @return returns the difference array
	 ****************************************/
	public static String differenceArray(String str) {
		StringBuilder newStr = new StringBuilder();
		
		newStr.append(modK(charToInt(str.charAt(n-1)) - charToInt(str.charAt(0)) - 1));
		
		for(int i = 1; i < n; i++) {
		    newStr.append(modK(charToInt(str.charAt(i-1)) - charToInt(str.charAt(i))));
		}
		
		return newStr.toString();
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
}
