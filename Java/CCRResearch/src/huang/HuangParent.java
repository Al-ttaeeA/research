package huang;

public class HuangParent {
	/************************************************
	 * Enter n value here to use the program 		*
	 ************************************************/
	public static int n = 6;
	
	public static void main(String[] args) {
		System.out.println(difference("001100"));
	}
	
	/*******************************************
	 * Method to find the parent rep of a child rep string
	 * @param childRep - the child rep
	 * @return returns the parent rep or null if the child rep is all 0s
	 *******************************************/
	public static String parent(String childRep) {
		String childRepExt = childRep + childRep.substring(0, n);
		
		for(int i = 0; i < 2*n; i++) {
			String curSubstring = childRepExt.substring(i, i+n);
			
			//Last digit has to be 1
			if(curSubstring.charAt(n-1) != '1') {
				continue;
			}
			
			String curReversePCR = reversePCR(curSubstring);
			
			
		}
	}
	
	/***************************************
	 * Method to find the number of differences of a n-length string
	 * @param str - initial string
	 * @return returns the number of differences in the string
	 ***************************************/
	public static int difference(String str) {
		str = str + complement(str.charAt(0));
		
		char dupChar = (char) (complement(str.charAt(0)) + '0');
		
		int difference = 0;
		
		for(int i = 0; i < n; i++) {
			if(str.charAt(i) != str.charAt(i+1)) {
				difference++;
			}
			else if(str.charAt(i) == dupChar) {
				difference += 2;
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
}
