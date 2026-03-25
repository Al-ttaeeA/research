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
		String cycle = rep.substring(0, CycleTreeConstruction.n - 1);
		
		for(int i = 0; i < CycleTreeConstruction.n; i++) {
			if(str.equals(cycle)) {
				return true;
			}
			
			System.out.println(cycle + "   " + str);
			
			cycle = CycleTreeConstruction.nextRotation(cycle);
		}
		
		return false;
	}
	
	public boolean isParent(String str) {
		if(parent != null) {
			if(parent.inCycle(str)) {
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
	public static int n = 5;
	
	public static String UC = ""
			+ "543215321452143514235421352134513425341254132513245324152431543125312451243524135412351234523415342154231523145314251432"
			+ "";
	
	public static HashMap<String, Node> map = new HashMap<String, Node>();
	
	public static boolean error = false;
	
	public static void main(String[] args) {
		//System.out.println(findRep("341"));
		function();
	}
	
	public static void function() {
		String start = "5432";
		
		String startExtended = start + missingSymbol(start);
		
		Node startNode = new Node(startExtended, null);
		
		map.put(startExtended, startNode);
		
		String UCextended = UC + UC.substring(0, n-1);
		
		Node current = startNode;
		
		for(int i = 0; i < UC.length(); i++) {
			String window = UCextended.substring(i, n-1 + i);
			
			if(!current.inCycle(window)) {
				System.out.println("\n\n\nWindow not in current node: " + current.rep + "   at i:" + i);
				System.exit(0);
			}
			
			if(!current.canVisit()) {
				System.out.println("\n\n\nToo many visits: " + current.rep);
				System.exit(0);
			}
			
			char expected = missingSymbol(window);
			char actual = UCextended.charAt(n-1+i);
			
			System.out.println("\n\n" + i + ": window: " + window + ", actual/expected: " + actual + " / " + expected);
			
			if(expected != actual) { // We changed cycles
				String newWindow = window.substring(1) + actual;
				
				String newRep = findRep(newWindow);
				
				System.out.println(i + ": New rep: " + newRep + ", current parent: " + current.parent);
				
				if(current.isParent(newWindow)) { // If the new window is from the parent
//					if(current.canVisit()) { // If were not done with the current node theres an error
//						System.out.println("\n\n\nNot done with node: " + current.rep);
//						System.exit(0);
//					}
					
					System.out.println(i + ": PARENT, Parent node: " + map.get(current.parent.rep));
					
					current = map.get(current.parent.rep);
				}
				else { // If the new window is from the child
					if(map.containsKey(newRep)) {
//						System.out.println("\n\nCHILD ALREADY VISITED!");
//						System.out.println(i + ": Current: " + current.rep + ", current parent: " + current.parent.rep);
//						System.out.println(i + ": CHILD: " + map.get(current));
//						break;
						
						//if at any point, a previously visited node is visited again from a non-child, inform the user
						error = true;
						
						current = map.get(newRep);
						continue;
					}
					
					Node newChild = new Node(newRep, current);
					
					map.put(newRep, newChild);
					
					current.addChild(newChild);
					
					System.out.println(i + ": CHILD, Child node: " + newChild);
					
					current = newChild;
				}
			}
		}
		
		System.out.println("\n\n\n\n");
		
		startNode.print();
		
		if(error) {
			System.out.println("\nThere was a node visited from neither its child nor parent");
		}
	}
	
	
	
	
	public static String lexLeastMSR(String str) {
		String lex = str;
		while(lex.charAt(0) != '5') {
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
