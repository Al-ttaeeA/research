package cycletree;

import java.util.*;

class node {
	public String rep; //lex least rotation of of the MSR cycle, length is n
	public node parent;
	public int visited = 0;
	public ArrayList<node> children;
	
	node(String rep, node parent) {
		this.rep = rep;
		this.parent = parent;
		this.children = new ArrayList<node>();
	}
	
	public void addChild(node child) {
		this.children.add(child);
	}
	
	public boolean canVisit() {
		if(visited != CycleTreeConstruction.n) {
			visited++;
			return true;
		}
		else {
			return false;
		}
	}
}

public class CycleTreeConstruction {
	public static int n = 4;
	
	public static String UC = "";
	
	public static void main(String[] args) {
		System.out.println(lexLeastMSR("123"));
		//function();
	}
	
	public static void function() {
		
	}
	
	
	public static String lexLeastMSR(String str) {
		String lex = str;
		while(lex.charAt(0) != '1') {
			lex = nextRotation(lex);
		}
		
		return lex;
	}
	
	public static String nextRotation(String str) {
		String next = str.substring(1) + missingSymbol(str);
		
		return next;
	}
	
	public static char missingSymbol(String str) {
	    char xor = 0;

	    // XOR all expected digits: '1' to 'n'
	    for (int i = 1; i <= n; i++) {
	        xor ^= (char) ('0' + i);
	    }

	    // XOR all characters in the string
	    for (int i = 0; i < str.length(); i++) {
	        xor ^= str.charAt(i);
	    }

	    return xor;
	}
}
