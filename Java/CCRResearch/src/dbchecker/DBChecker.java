package dbchecker;

import java.util.*;

public class DBChecker {
	/************************************************
	 * Enter n and k values here to use the program *
	 ************************************************/
	static int n = 3;
	static int k = 6;
	
	/************************************************
	 * 		    Enter String to test here 		    *
	 ************************************************/
	static String testStr = "000   101   312 423 534 045 150 201   412 523 034 145 250   321 432 543 054 105 210   301   512   024 135 240 351 402 513   023 134 245 350   421 532 043 154 205 310   401   212 323 434 545 050   111 222 333 444 555   002 113 224 335 440 551   003 114 225 330 441 552   004 115 220 331 442 553   005 110 221 332 443 554";
	
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
