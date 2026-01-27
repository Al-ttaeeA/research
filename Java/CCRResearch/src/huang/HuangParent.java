package huang;

public class HuangParent {
	/************************************************
	 * Enter n value here to use the program 		*
	 ************************************************/
	public static int n = 6;
	
	public static void main(String[] args) {
		System.out.println(parent("000100111011"));
	}
	
	/*******************************************
	 * Method to find the parent rep of a child rep string
	 * @param childRep - the child rep
	 * @return returns the parent rep or null if the child rep is all 0s
	 *******************************************/
	public static String parent(String childRep) {
		String childRepExt = childRep + childRep.substring(0, n);
		int maxValue = 0;
		String maxReversePCR = "";
		
		for(int i = 0; i < 2*n; i++) {
			String curSubstring = childRepExt.substring(i, i+n); //Bi*1
			
			if(curSubstring.charAt(n-1) != '1') {
				continue;
			}
			
			//System.out.println(curSubstring);
			
			String curReversePCR = reversePCR(curSubstring); //Bi
			
			int diffCur = difference(curSubstring);
			int diffReversePCR = difference(curReversePCR);
			
			int valueCur = binaryValue(curSubstring);
			
			//System.out.println(curReversePCR + "   " + diffCur + "   " + diffReversePCR + "   " + valueCur);
			
			if(diffReversePCR < diffCur && valueCur > maxValue) {
				maxReversePCR = curReversePCR;
				maxValue = valueCur;
			}
			
			//Condition g)
			if(diffReversePCR == diffCur && valueCur > maxValue) {
				String curComplement = curSubstring.substring(1) + complement(curSubstring.charAt(0)); //Bi*2
			
				int diffCurComplement = difference(curComplement);
				
				int valueCurComplement = binaryValue(curComplement);
				
				if(diffCurComplement == diffCur && valueCurComplement > maxValue) {
					maxReversePCR = curReversePCR;
					maxValue = valueCurComplement;
				}
			}
		}
		
		if(maxReversePCR.equals("")) {
			return null;
		}
		
		return getRep(maxReversePCR);
	}
	
	/****************************************
	 * Method to find the binary value of a n-length string
	 * @param str - the initial string
	 * @return returns the decimal value of the binary string
	 ****************************************/
	public static int binaryValue(String str) {
		int sum = 0;
		
		for(int i = 0; i < n; i++) {
			sum = sum * 2;
			
			if(str.charAt(i) == '1') {
				sum++;
			}
		}
		
		return sum;
	}
	
	/***************************************
	 * Method to find the number of differences of a n-length string
	 * @param str - initial string
	 * @return returns the number of differences in the string
	 ***************************************/
	public static int difference(String str) {
		str = str + complement(str.charAt(0));
		
		int difference = 0;
		
		for(int i = 0; i < n; i++) {
			if(str.charAt(i) != str.charAt(i+1)) {
				difference++;
			}
		}
		
		return difference;
	}
	
	/*************************************
	 * Method to find the complement of a digit in a string
	 * @param chr - the initial digit
	 * @return returns the complement of the digit
	 *************************************/
	public static int complement(char chr) {
		int value = chr - '0';
		
		return (value + 1) % 2;
	}
	
	/******************************************
	 * Method to find the string rotated right by 1
	 * @param str - initial string
	 * @return returns the initial string rotated right by 1
	 ******************************************/
	public static String reversePCR(String str) {
		return str.charAt(n-1) + str.substring(0, n-1);
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
		
		if(extendedStr.length() == 2*n) return extendedStr;
		
		//extend the string
		for(int i = 0; i < (2-1) * n; i++) {
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
		nextChar += (str.charAt(0) - '0' + 1) % 2; //add a_1 plus 1 modulus k
		
		return nextChar; 
	}
}
