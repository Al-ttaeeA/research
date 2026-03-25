package cycletree;

import java.util.ArrayList;
import java.util.HashMap;

class NodePCR {
	public String rep; //lex least rotation of of the PCR cycle, length is n
	public NodePCR parent;
	public int visited = 0;
	public ArrayList<NodePCR> children;
	
	NodePCR(String rep, NodePCR parent) {
		this.rep = rep;
		this.parent = parent;
		this.children = new ArrayList<NodePCR>();
	}
	
	public void addChild(NodePCR child) {
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
		String cycle = rep;
		
		for(int i = 0; i < CycleTreePCR.n; i++) {
			if(str.equals(cycle)) {
				return true;
			}
			
			System.out.println(cycle + "   " + str);
			
			cycle = CycleTreePCR.nextRotation(cycle);
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
		for(NodePCR child: children) {
			child.print();
		}
	}
	
	public String toString() {
		String str = rep + ", Children:";
		
		for(NodePCR child: children) {
			str += "   " + child.rep;
		}
		
		return str;
	}
}

public class CycleTreePCR {
	public static int n = 4;
	
	public static String UC = ""
			+ "123412351243124513241325134213452135214321452314231524153215342514352435142531453245312541354215432543154235412534152345"
			+ ""
			+ "";
	
	public static HashMap<String, NodePCR> map = new HashMap<String, NodePCR>();
	
	public static void main(String[] args) {
		//System.out.println(findRep("341"));
		function();
	}
	
	public static void function() {
		String start = "1234";
		
		NodePCR startNode = new NodePCR(start, null);
		
		map.put(start, startNode);
		
		String UCextended = UC + UC.substring(0, n);
		
		NodePCR current = startNode;
		
		for(int i = 0; i < UC.length(); i++) {
			String window = UCextended.substring(i, n + i);
			
			if(!current.inCycle(window)) {
				System.out.println("\n\n\nWindow not in current node: " + current.rep + "   at i:" + i);
				System.exit(0);
			}
			
			if(!current.canVisit()) {
				System.out.println("\n\n\nToo many visits: " + current.rep);
				System.exit(0);
			}
			
			char expected = nextSymbol(window);
			char actual = UCextended.charAt(n+i);
			
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
						current = map.get(newRep);
						continue;
					}
					
					NodePCR newChild = new NodePCR(newRep, current);
					
					map.put(newRep, newChild);
					
					current.addChild(newChild);
					
					System.out.println(i + ": CHILD, Child node: " + newChild);
					
					current = newChild;
				}
			}
		}
		
		System.out.println("\n\n\n\n");
		
		startNode.print();
	}
	
	
	
	
	
	public static String lexLeastPCR(String str) {
	    int n = str.length();
	    String s = str + str; // simulate rotations
	    int i = 0, j = 1, k = 0;

	    while (i < n && j < n && k < n) {
	        char a = s.charAt(i + k);
	        char b = s.charAt(j + k);

	        if (a == b) {
	            k++;
	        } else if (a > b) {
	            i = i + k + 1;
	            if (i <= j) i = j + 1;
	            k = 0;
	        } else {
	            j = j + k + 1;
	            if (j <= i) j = i + 1;
	            k = 0;
	        }
	    }

	    int start = Math.min(i, j);
	    return s.substring(start, start + n);
	}
	
	public static String nextRotation(String str) {
		String next = str.substring(1) + nextSymbol(str);
		
		return next;
	}
	
	public static String findRep(String str) {
		String lex = lexLeastPCR(str);
		
		return lex;
	}
	
	public static char nextSymbol(String str) {
	    return str.charAt(0);
	}
}
