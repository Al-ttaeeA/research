package ccr2construction;

import java.util.ArrayList;

public class CCR2Construction {
	/************************************************
	 * Enter n and k values here to use the program *
	 ************************************************/
	public static int n = 4;
	public static int k = 3;
	
	public static long total;
	
	public static String sequence = ""; //The entire sequence will be concatenated here using recursion
	
	public static ArrayList<String> reps = new ArrayList<String>(); //Arraylist to store all conecklaces
	
	public static void main(String[] args) {
		total = (long) Math.pow(k, n);
		findReps();
		
		String start = reps.get(0);
		int traversal = 1; //traversal type, 1 for left concatenation tree, 0 for right concatenation tree
		int changeIndex = n-1;
		
		System.out.println(findChildCCR2("002011012212", 0));
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
			
		}
		
		sequence += " ";
		sequence += leastPeriod(extString);
		sequence += " ";
		
		//check left children
		for(int i = 0; i < changeIndex + traversal; i++) {
			
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
		
		if(value == k-1) { //if the value is already the highest digit then it has no children
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
			String firstSection = extString.substring(0, index) + newValue + extString.substring(index+1, n); //get the first section of possible child
			
			String newExtended = extendString(firstSection); //find extended version of possible child
			
			if(reps.contains(newExtended)) { //IF the extended possible child is a conecklace then it is a valid child
				return newExtended;
			}
		}
		
		return null; //Return null if we went through all possible children at this index
	}
	
	/*************************************************
	 * Method to find the least period of a string to concatenate it
	 * @param extString - the extended string to find the period of
	 * @return returns the least period
	 *************************************************/
	public static String leastPeriod(String extString) {
		for(int len = 1; len < k*n; len++) {
			if(k*n % len == 0) {
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
			
			if(reps.contains(rep)) {
				currentStr = nextLex(currentStr);
				continue;
			}
					
			String currentExtended = extendString(currentStr);
			
			int j;
			for(j = 0; j < (n*k); j++) {
				currentExtended = nextCycle(currentExtended); //cycle to the next
				
				//if the next cycle is equal to the representative then exit loop
				if(currentExtended.equals(rep)) {
					j++;
					break;
				}
			}
			
			currentStringCount += j;
			
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
