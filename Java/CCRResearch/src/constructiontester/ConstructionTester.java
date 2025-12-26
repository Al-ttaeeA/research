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
	public static int maxN = 3;
	public static int maxK = 3;
	
	public static long total;
	
	public static void main(String[] args) {
		for(int n = 2; n <= maxN; n++) {
			for(int k = 2; k <= maxK; k++) {
				total = (long) Math.pow(k, n);
				
				CCR1Construction.n = n;
				CCR1Construction.k = k;
				CCR1Construction.total = total;
				CCR2Construction.n = n;
				CCR2Construction.k = k;
				CCR2Construction.total = total;
				CCR3Construction.n = n;
				CCR3Construction.k = k;
				CCR3Construction.total = total;
				
				CCR1Construction.findReps();
				
				System.out.println("Reps found for n = " + n + ", k = " + k);
				
				System.out.println(CCR1Construction.reps);
				
				CCR2Construction.reps = CCR1Construction.reps;
				CCR3Construction.reps = CCR1Construction.reps;
				
				CCR2Construction.cycles = CCR1Construction.cycles;
				CCR3Construction.cycles = CCR1Construction.cycles;
				
				String start = CCR1Construction.reps.get(0);
				int traversal = 1;
				int changeIndex = n-1;
				
				CCR1Construction.recursiveConcat(start, changeIndex, traversal);
				CCR2Construction.recursiveConcat(start, changeIndex, traversal);
				CCR3Construction.recursiveConcat(start, changeIndex, traversal);
				
				System.out.println("n = " + n + ", k = " + k + ":");
				System.out.print("CCR1: ");
				CCR1Construction.DBChecker(CCR1Construction.sequence);
				System.out.print("CCR2: ");
				CCR2Construction.DBChecker(CCR2Construction.sequence);
				System.out.print("CCR3: ");
				CCR3Construction.DBChecker(CCR3Construction.sequence);
			}
		}
	}
	
	
}
