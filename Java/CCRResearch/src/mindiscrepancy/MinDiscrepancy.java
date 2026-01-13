package mindiscrepancy;

import java.util.ArrayList;
import java.util.HashMap;

public class MinDiscrepancy {
	/************************************************
	 * Enter n and k values here to use the program *
	 ************************************************/
	public static int n = 4;
	public static int k = 3;
	
	public static long total;
	
	public static String sequence = ""; //The entire sequence will be concatenated here using recursion
	
	public static ArrayList<String> reps = new ArrayList<String>(); //Arraylist to store all conecklaces
	public static HashMap<String, ArrayList<String>> cycles = new HashMap<String, ArrayList<String>>();
	
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
		
		String start = reps.get(0);
		int traversal = 1; //traversal type, 1 for left concatenation tree, 0 for right concatenation tree
		int changeIndex = n-1;
		
		//System.out.println(findChild("222100021110", 6, 2));
		
		recursiveConcat(start, changeIndex, traversal, 0);
		
 		System.out.println(sequence);
		
 		DBChecker(sequence);
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
		
		sequence += " ";
		
		//outer loop runs for each n-length section
		for(int section = 0; section < Math.min((leastLength / n) + 1, k); section++) {
			//inner loop runs n times
			for(int index = Math.max(changeIndex + traversal, section * n); index < Math.min(leastLength, ((section + 1) * n)); index++) {
				//Check child at each index
				String child = findChild(extString, index, modK(depth+1));
				
				if(child != null) {
					recursiveConcat(child, index % n, traversal, modK(depth+1));
				}
			}
			
			//After checking right children of each section, concatenate the section
			sequence += least.substring(section * n, Math.min(leastLength, ((section + 1) * n)));
		}
		
		sequence += " ";
		
		//Left children
		for(int i = 0; i < changeIndex + traversal; i++) {
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
		
		//new value must match depth
		if(newValue != depth) {
			return null;
		}
		
		int section = index / n;
		int sectionIndex = index % n;
		
		String subParent = extString.substring(section * n, (section + 1) * n);
		
		String subChild = subParent.substring(0, sectionIndex) + newValue + subParent.substring(sectionIndex + 1, n);
		
		//System.out.println("subParent: " + subParent + ", subChild: " + subChild);
		
		//Get the child shifted, and the representative
		String child = extendString(subChild); //This is the possible child
		String childRep = getRep(child);
		
		//get the parent of the found child's rep, and the extString rep
		String foundParentRep = parent(childRep); //This is the actual parent of the possible child
		String actualParentRep = getRep(extString); //This is the parent we want
		
		//If the parent of the found child matches the extString's rep, then it is a child, we need to check if its at the correct index
		if(actualParentRep.equals(foundParentRep)) {
			ArrayList<String> childCycles = cycles.get(childRep);
			int childShift = childCycles.indexOf(child);
			
			int leastPeriod = leastPeriod(child).length();
			
			int foundChangeIndex = (childShift + sectionIndex) % leastPeriod; //found change index save for later
			
			String correctDiffArr = correctDifferenceArray(childRep.substring(0, n));
			
			String childRepDup = childRep + childRep.substring(0, n); //extended version of child rep to check each position
			
			int i;
			for(i = 0; i < k*n; i++) {
				String curSubstring = childRepDup.substring(i, i+n);
				String curDiffArr = differenceArray(curSubstring);
				int curValue = charToInt(curSubstring.charAt(n-1));
				
				if(curValue == depth && curDiffArr.equals(correctDiffArr)) {
					break;
				}
			}
			
			int expectedChangeIndex = (i + n - 1) % (leastPeriod);
			
			if(expectedChangeIndex == foundChangeIndex) {
				return child;
			}
		}
		
		return null;
	}
	
	/*******************************************
	 * Method to find the parent rep of a child rep string
	 * @param childRep - the child rep
	 * @return returns the parent rep or null if the child rep is all 0s
	 *******************************************/
	public static String parent(String childRep) {
		if(childRep.equals(reps.get(0))) {
			return null;
		}
		
		String lexDiffArr = leastDifferenceArray(childRep.substring(0,n));
		
		int firstNonZero = 0;
		int nextDigit = 0;
		int i;
		for(i = 0; i < n; i++) {
			if(lexDiffArr.charAt(i) != '0' && i != n-1) {
				firstNonZero = charToInt(lexDiffArr.charAt(i));
				nextDigit = charToInt(lexDiffArr.charAt(i+1));
				break;
			}
		}
		
		int newFirstdigit = firstNonZero - 1;
		int newNextDigit = modK(nextDigit + 1);
		
		String parentDiffArr = lexDiffArr.substring(0, i) + newFirstdigit + newNextDigit + lexDiffArr.substring(i+2, n);
		
		String parentStr = constructStringFromDiffArr(parentDiffArr);
		
		String parentRep = getRep(parentStr);
		
		return parentRep;
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
	
	/**************************************************************
	 * Method that constructs a kn length string from a given diffArr
	 * @param diffArr - lex least diffArr
	 * @return returns the actual string
	 */
	public static String constructStringFromDiffArr(String diffArr) {
		int[] intDiffArr = new int[n];
		
		for(int j = 0; j < n; j++) {
			intDiffArr[j] = charToInt(diffArr.charAt(j));
		}
		
		String first = "0"; //start with 0, concatenate based on the parent diffArr found
		int prevValue = 0;
		for(int j = 1; j < n; j++) {
			prevValue = modK(prevValue - intDiffArr[j]);
			first += prevValue;
		}
		
		//now we have the first section, extend it
		String str = extendString(first);
		
		return str;
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
	 * Method to find the lex least rotation of the difference array of a string
	 * @param str - initial n length string
	 * @return returns the lex least difference array
	 ****************************************************/
	public static String leastDifferenceArray(String str) {
		String diffArr = differenceArray(str);
		diffArr += diffArr;
		
		String lexDiffArr = diffArr;
		for(int i = 0; i < n; i++) {
			if(diffArr.substring(i, i+n).compareTo(lexDiffArr) < 0) {
				lexDiffArr = diffArr.substring(i, i+n);
			}
		}
		
		return lexDiffArr;
	}
	
	/****************************************
	 * Method to find the difference array of a length n string based on the Min-Discrepancy algorithm by N. Alvarez, V. Becher, and M. Mereb
	 * @param str - the length n string to find the difference array of
	 * @return returns the difference array
	 ****************************************/
	public static String differenceArray(String str) {
		String newStr = "";
		
		newStr += modK(charToInt(str.charAt(n-1)) - charToInt(str.charAt(0)) - 1);
		
		for(int i = 1; i < n; i++) {
			newStr += modK(charToInt(str.charAt(i-1)) - charToInt(str.charAt(i)));
		}
		
		return newStr;
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
	 * Method to find the least period of a string to concatenate it
	 * @param extString - the extended string to find the period of
	 * @return returns the least period
	 *************************************************/
	public static String leastPeriod(String extString) {
		//For loop to loop through all possible periodic lengths
		for(int len = 1; len <= k*n / 2; len++) {
			if(k*n % len == 0) {
				//pick the first substring and compare if it actually periodically concatenates to make the extString
				String period = extString.substring(0, len);
				String newString = "";
				
				int repetition = k*n / len;
				for(int i = 0; i < repetition; i++) {
					newString += period;
				}
				
				if(extString.equals(newString)) {
					return period;
				}
			}
		}
		
		return extString;
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
			
			if(cycles.containsKey(rep)) { //if the representative is in the hashmap then move to the next one
				currentStr = nextLex(currentStr);
				continue;
			}
					
			//Cycles list for this representative
			ArrayList<String> currentRepCycles = new ArrayList<String>();
			String currentExtended = extendString(currentStr);
			
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
			reps.add(rep);
			
			if(currentStringCount == total) {
				break;
			}
			
			 currentStr = nextLex(currentStr);
		}
	}
	
	/**************************************
	 * Method to find the representative k*n length string of an initial string
	 * @param str - initial string
	 * @return returns the representative
	 **************************************/
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
	 ********************************************/
	public static String extendString(String str) {
		String extendedStr = str; //start with the string
		
		if(extendedStr.length() == k*n) return extendedStr;
		
		//extend the string
		for(int i = 0; i < (k-1) * n; i++) {
			extendedStr += nextChar(extendedStr.substring(i));
		}
		
		return extendedStr;
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
	 * Method to find the next char after a string using the CCR feedback function
	 * @param str - initial string
	 * @return returns the next char
	 ****************************************/
	public static String nextChar(String str) {
		String nextChar = "";
		nextChar += (str.charAt(0) - '0' + 1) % k; //add a_1 plus 1 modulus k
		
		return nextChar; 
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
