package ccr2periodic;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashMap;
import repfinder.RepFinder;

public class CCR2Periodic {
	/************************************************
	 * Enter n and k values here to use the program *
	 ************************************************/
	static int n = 8;
	static int k = 6;
	
	
	public static ArrayList<String> reps2;
	public static ArrayList<String> periodicReps = new ArrayList<String>();
	static TreeMap<String, ArrayList<String>> sortedColexCycles2;
	static HashMap<String, String> stringToParent = new HashMap<String, String>();
	public static ArrayList<String> periodicKeyParent = new ArrayList<>();
	static int periodicParentCount;
	
	public static void main(String[] args) {
		int count = 0;
		ArrayList<ArrayList<String>> periodicParents = new ArrayList<>();
		
		for(int i = 2; i <= 10; i++) {
			for(int j = 2; j <= 10; j++) {
				n = i;
				k = j;
				function();
				
				if(periodicParentCount != 0) {
					System.out.println("Periodic parent found in n=" + n + ", k=" + k);
					ArrayList<String> entry = new ArrayList<>();
					entry.add(n + "");
					entry.add(k + "");
					for(int m = 0; m < periodicParentCount; m++) {
						entry.add(periodicKeyParent.get(m));
					}
					
					periodicParents.add(entry);
				}
				
				RepFinder.reps.clear();
				RepFinder.cycles.clear();
				RepFinder.sortedColexCycles.clear();
				periodicReps.clear();
				stringToParent.clear();
				periodicKeyParent.clear();
			}
		}
		
		for(ArrayList<String> entry: periodicParents) {
			System.out.print("\nn=" + entry.get(0) + ", k=" + entry.get(1));
			for(int i = 2; i < entry.size(); i++) {
				System.out.print(", " + entry.get(i));
			}
		}
	}
	
	public static void function() {
		RepFinder.n = n;
		RepFinder.k = k;
		
		RepFinder.function();
		
		reps2 = RepFinder.reps;
		sortedColexCycles2 = RepFinder.sortedColexCycles;
		
		printPeriodics();
		
		findParents();
		
		System.out.println("\n\nString to parent map:");
		System.out.println(stringToParent);
		
		checkParents();
	}
	
	public static void checkParents() {
		System.out.println("\n\nList of periodic parents:");
		periodicParentCount = 0;
		
		for(String key: stringToParent.keySet()) {
			String parent = stringToParent.get(key);
			
			if(periodicReps.contains(parent)) {
				System.out.println(key + " --> " + parent);
				periodicKeyParent.add(key + " --> " + parent);
				
				periodicParentCount++;
			}
		}
		
		System.out.println("\nPeriodic parent found: " + periodicParentCount);
	}
	
	public static void findParents() {
		for(int i = 0; i < reps2.size(); i++) {
			String rep = reps2.get(i);
			int index = nonzeroIndex(rep);
			if(index == -1) continue;
			
			int value = rep.charAt(index) - '0';
			
			for(int j = 1; j <= value; j++) {
				String sub = rep.substring(0, index) + (value-j) + rep.substring(index+1,n);
				
				String extParent = RepFinder.extendString(sub);
				
				if(reps2.contains(extParent)) {
					stringToParent.put(rep, extParent);
					break;
				}
			}
		}
	}
	
	static int nonzeroIndex(String rep) {
		for(int i = 0; i < n; i++) {
			if(rep.charAt(i) != '0') {
				return i;
			}
		}
		
		return -1;
	}
	
	public static void printPeriodics() {
		System.out.println("\n\nRepresentatives of periodic classes:");
		int count = 0;
		
		for(String rep: reps2) {
			ArrayList<String> curEntry = sortedColexCycles2.get(rep);
			
			if(curEntry.size() != (n*k)) {
				periodicReps.add(rep);
				System.out.println(rep);
				count++;
			}
		}
		
		System.out.println("\n\nNumber of periodic classes: " + count);
	}
}
