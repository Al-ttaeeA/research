package cycletree;

import java.util.*;

class Node {
	public String rep; //lex least rotation of of the MSR cycle, length is n
	public Node parent;
	public int visited = 0;
	public ArrayList<Node> children;
	
	Node(String rep, Node parent) {
		this.rep = rep;
		this.parent = parent;
		this.children = new ArrayList<Node>();
	}
	
	public void addChild(Node child) {
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
	
	public static String UC = "123124132134214324314234";
	
	public static HashMap<String, Node> map = new HashMap<String, Node>();
	
	public static void main(String[] args) {
		System.out.println(findRep("341"));
		//function();
	}
	
	public static void function() {
		String start = "";
		
		for(int i = 1; i < n; i++) {
			start += i;
		}
		
		String startExtended = start + missingSymbol(start);
		
		Node startNode = new Node(startExtended, null);
		
		map.put(startExtended, startNode);
		
		String UCextended = UC + UC.substring(0, n-1);
		
		for(int i = 0; i < UC.length(); i++) {
			String window = UCextended.substring(i, n-1 + i);
			
			
		}
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
	
	public static String findRep(String str) {
		String lex = lexLeastMSR(str);
		
		lex += missingSymbol(lex);
		
		return lex;
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
