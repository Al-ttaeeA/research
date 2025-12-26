package constructiontester;

import java.util.ArrayList;
import java.util.HashMap;

import ccr1construction.CCR1Construction;
import ccr2construction.CCR2Construction;
import ccr3construction.CCR3Construction;

public class ConstructionTester {
	/********************************************************
	 * Enter max n and max k values here to use the program *
	 ********************************************************/
	public static int maxN = 8;
	public static int maxK = 6;
	
	public static long total;
	
	public static ArrayList<String> reps = new ArrayList<String>(); //Arraylist to store all conecklaces
	public static HashMap<String, ArrayList<String>> cycles = new HashMap<String, ArrayList<String>>();
	
	public static void main(String[] args) {
		for(int n = 2; n < maxN; n++) {
			for(int k = 2; k < maxK; k++) {
				total = (long) Math.pow(k, n);
				
				CCR1Construction.n = n;
				CCR1Construction.k = k;
				CCR2Construction.n = n;
				CCR2Construction.k = k;
				CCR3Construction.n = n;
				CCR3Construction.k = k;
				
				CCR1Construction.findReps();
				
				CCR2Construction.reps = CCR1Construction.reps;
				CCR3Construction.reps = CCR1Construction.reps;
				
				CCR2Construction.cycles = CCR1Construction.cycles;
				CCR3Construction.cycles = CCR1Construction.cycles;
				
				
			}
		}
	}
	
	
}
