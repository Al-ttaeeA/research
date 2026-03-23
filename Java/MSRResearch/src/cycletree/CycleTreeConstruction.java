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
	
	public boolean inCycle(String str) {
		if(CycleTreeConstruction.findRep(str).equals(rep)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isParent(String str) {
		if(parent != null) {
			if(parent.rep.equals(str)) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public void print() {
		System.out.println(this.toString());
		for(Node child: children) {
			child.print();
		}
	}
	
	public String toString() {
		String str = rep + ", Children:";
		
		for(Node child: children) {
			str += "   " + child.rep;
		}
		
		return str;
	}
}

public class CycleTreeConstruction {
	public static int n = 4;
	
	public static String UC = "123124132134214324314234";
	
	public static HashMap<String, Node> map = new HashMap<String, Node>();
	
	public static void main(String[] args) {
		//System.out.println(findRep("341"));
		function();
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
		
		Node current = startNode;
		
		for(int i = 0; i < UC.length(); i++) {
			String window = UCextended.substring(i, n-1 + i);
			
			if(!current.inCycle(window)) {
				System.out.println("\n\n\nWindow not in current node: " + current.rep);
				System.exit(0);
			}
			
			if(!current.canVisit()) {
				System.out.println("\n\n\nToo many visits: " + current.rep);
				System.exit(0);
			}
			
			char expected = missingSymbol(window);
			char actual = UCextended.charAt(n-1+i);
			
			if(expected != actual) { // We changed cycles
				String newWindow = window.substring(1) + actual;
				
				String newRep = findRep(newWindow);
				
				if(current.isParent(newRep)) { // If the new window is from the parent
					if(current.canVisit()) { // If were not done with the current node theres an error
						System.out.println("\n\n\nNot done with node: " + current.rep);
						System.exit(0);
					}
					
					current = current.parent;
				}
				else { // If the new window is from the child
					Node newChild = new Node(newRep, current);
					
					map.put(newRep, newChild);
					
					current.addChild(newChild);
					
					current = newChild;
				}
			}
		}
		
		if(current != startNode) {
			System.out.println("\n\n\nCouldnt reach start node");
			System.exit(0);
		}
		
		current.print();
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
