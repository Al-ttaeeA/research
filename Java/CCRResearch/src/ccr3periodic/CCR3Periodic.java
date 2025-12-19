package ccr3periodic;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashMap;
import repfinder.RepFinder;

public class CCR3Periodic {
	static int n = 4;
	static int k = 3;
	
	public static ArrayList<String> reps2;
	public static ArrayList<String> periodicReps = new ArrayList<String>();
	static TreeMap<String, ArrayList<String>> sortedColexCycles2;
	static HashMap<String, String> stringToParent = new HashMap<String, String>();
	public static ArrayList<String> periodicKeyParent = new ArrayList<>();
	static int periodicParentCount;
	
	public static void main(String[] args) {
		checker(); //call the checker
	}
	
	/***************************
	 * Method to check periodic parents upto n=8 and k=6
	 ***************************/
	public static void checker() {
		int count = 0;
		ArrayList<ArrayList<String>> periodicParents = new ArrayList<>(); //keep track of all child-periodic parent pairs
		
		/*********************************
		 * Enter max n and k values here *
		 *********************************/
		int maxN = 30;
		int maxK = 2;
		
		for(int i = 2; i <= maxN; i++) {
			for(int j = 2; j <= maxK; j++) {
				n = i;
				k = j;
				initiate(); //call function to initiate all variables according to n=i and k=j
				
				if(periodicParentCount != 0) { //if a periodic parent is found print it and add an entry to periodicParents
					System.out.println("Periodic parent found in n=" + n + ", k=" + k);
					ArrayList<String> entry = new ArrayList<>();
					entry.add(n + "");
					entry.add(k + "");
					for(int m = 0; m < periodicParentCount; m++) {
						entry.add(periodicKeyParent.get(m));
					}
					
					periodicParents.add(entry);
				}
				
				//clear all lists and maps
				RepFinder.reps.clear();
				RepFinder.cycles.clear();
				RepFinder.sortedColexCycles.clear();
				periodicReps.clear();
				stringToParent.clear();
				periodicKeyParent.clear();
			}
		}
		
		System.out.println("\n\n\n\n\n\n\n\nALL periodic parent pairs found: \n");
		//print every periodic parent found
		for(ArrayList<String> entry: periodicParents) {
			System.out.print("\nn=" + entry.get(0) + ", k=" + entry.get(1));
			for(int i = 2; i < entry.size(); i++) {
				System.out.print(", " + entry.get(i));
			}
		}
	}
	
	/****************************
	 * Method to initialize all lists and maps that are going to be used
	 * Also finds all periodic parents
	 ****************************/
	public static void initiate() {
		RepFinder.n = n;
		RepFinder.k = k;
		
		RepFinder.function(); //Call RepFinder's main to initialize all reps and cycles from there
		
		//initialize current class' lists to represent the other lists
		reps2 = RepFinder.reps;
		sortedColexCycles2 = RepFinder.sortedColexCycles;
		
		findPeriodics();
		
		findParents();
		
		checkParents();
	}
	
	/********************************
	 * Method to check every parent and add any periodic parents to a map
	 ********************************/
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
	
	/*******************************
	 * Method to find the parent of every representative, this method is based on the CCR3 parent rule
	 *******************************/
	public static void findParents() {
		for(int i = 0; i < reps2.size(); i++) {
			String rep = reps2.get(i);
			int index = n-1;
			if(index == -1) continue;
			
			int value = rep.charAt(index) - '0';
			
			int newValue;
			
			//CCR3 parent rule
			if(value == 0) {
				newValue = 1;
			}
			else {
				newValue = value - 1;
			}
			
			//find the first block of the parent
			String subParent = rep.substring(0, index) + newValue;
			//Extend and get the rep of the parent
			String repParent = RepFinder.getRep(subParent);
			
			//Add the pair to a map
			stringToParent.put(rep, repParent);
		}
	}
	
	/**********************************
	 * Method that finds the periodic reps and adds them to a list
	 **********************************/
	public static void findPeriodics() {
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
