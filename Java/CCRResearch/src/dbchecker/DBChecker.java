package dbchecker;

import java.util.*;

public class DBChecker {
	/************************************************
	 * Enter n and k values here to use the program *
	 ************************************************/
	static int n = 3;
	static int k = 3;
	
	/************************************************
	 * 		    Enter String to test here 		    *
	 ************************************************/
	static String testStr = "000   211 022 100   111 222   010 121 202";
	
	static long total = (long) Math.pow(k, n);
	static ArrayList<String> strings = new ArrayList<String>();
	
	public static void main(String[] args) {
		//Trim string from all spaces and slashes
		testStr = testStr.replace(" ", "").replace("\\", "").replace("·", "");
		
		//Test string length
		if(testStr.length() != total) {
			System.out.println("Not DB, doesn't satisfy length requirements, length: " + testStr.length() + ", expected: " + total);
			return;
		}
		
		//Add the first n to the end
		testStr = testStr + testStr.substring(0, n);
		
		//Add the string at each index to the list and check for duplicates
		for(int i = 0; i < Math.pow(k, n); i++) {
			if(strings.contains(testStr.substring(i, i+n))) {
				System.out.println("Not DB, duplicate string: " + testStr.substring(i, i+n));
				return;
			}
			
			strings.add(testStr.substring(i, i+n));
		}
		
		if(strings.size() == total) {
			System.out.println("String is a DB");
		}
		else {
			System.out.println("Not DB, doesn't cover all strings");
		}
	}
}
