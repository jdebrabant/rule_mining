package ca.pfv.spmf.multidimensionalpatterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is an implementation of the algorithm proposed by Helen Pinto et al (2001)
 * to extract MD-patterns (multi-dimensional pattern) from an MD-Database.
 * As the article is not very detailled, this implementation may not be
 * exactly as imagined by the authors.
 * @author Philippe Fournier-Viger 
 **/
public class AlgoDim {
	private MDPatterns patterns = new MDPatterns("Frequent MD Patterns");
	private int minsupRelative;
	private long timeStart;
	private int databasePassesCount;
	
	private final boolean findClosedMDPatterns;
	
	public AlgoDim(boolean findClosedMDPatterns){
		this.findClosedMDPatterns = findClosedMDPatterns;
	}

	public MDPatterns runAlgorithm(MDPatternsDatabase contexte, double minsupp) {
//		System.out
//		.println("=============  EXECUTION - DIM =============");
		// Initial parameters for DIM
		MDPattern prefixeInitial = new MDPattern(0);   // ID = 0 par défaut
		
		this.minsupRelative = (int) Math.round(minsupp * contexte.size());
		resetStatistiques();
		
		// First call to SPM algorithm; start with dimension 0.
		dim(prefixeInitial, 0, contexte);
		
		// Extra step:  Filter patterns that are not closed if required.
		if(findClosedMDPatterns){
			filterPatternsThatAreNonClosed(contexte);
		}
		
		return patterns;
	}
	
	/**
	 * This method delete patterns that are not closed.
	 */
	private void filterPatternsThatAreNonClosed(MDPatternsDatabase contexte) {
		int k = contexte.getDimensionCount();
		List<MDPattern> liste = new ArrayList<MDPattern>(patterns.getLevel(patterns.getLevelCount()-1));
		// Loop to compare each pair of patterns only one time.
        for (int i = 0; i < liste.size(); i++) {
            MDPattern pattern1 = liste.get(i); // pattern 1
            for (int j = i + 1; j < liste.size(); j++) {
                MDPattern pattern2 = liste.get(j); // pattern 2
                // Check if the pattern is closed
            	boolean closedcheck = !findClosedMDPatterns 
            	        || pattern1.getPatternsID().size() == pattern2.getPatternsID().size();
            	// Check if pattern1 is more general than pattern2
                if(subsume(pattern1, pattern2) && closedcheck){
                	if(!containsOnlyWildcards(pattern2)){
                		patterns.removePattern(pattern2, k);
                	}
                // Check if pattern 2 is more general than pattern1
                }else if(subsume(pattern2, pattern1) && closedcheck){
                	if(!containsOnlyWildcards(pattern1)){
                    	patterns.removePattern(pattern1, k);
                	}
                }
            }
        }
	}
	
	private boolean containsOnlyWildcards(MDPattern pattern){
		for(int i=0; i< pattern.size(); i++){
			if(pattern.get(i) != MDPattern.WILDCARD){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check if a pattern is more general than another
	 * @param pattern1 the first pattern
	 * @param pattern2 the second pattern
	 * @return true if pattern 1 is more general.
	 */
	private boolean subsume(MDPattern pattern1, MDPattern pattern2) {
		for(int i=0; i< pattern1.size(); i++){
			if(pattern1.get(i) != pattern2.get(i) && pattern2.get(i) != MDPattern.WILDCARD){
				return false;
			}
		}
		return true;
	}

	/**
	 * Main method of the DIM algorith. For one dimension, the algorithm find all the
	 * frequent values. For each of those values, the algorithm create the projected database
	 * and perform a recursion for the next dimension. By doing this, the algorithm can find
	 * all the MD-Patterns of an MD-Database. The principle of projecting is similar to that
	 * of PrefixSpan.
	 * @param prefixe The prefix that is used for building the projected database.
	 * @param k The current dimension to consider.
	 * @param context The context
	 */
	private void dim(MDPattern prefixe, int k, MDPatternsDatabase context) {
		// (0) Stoping condition
		if(k >= context.getDimensionCount()){
			return;
		}
		
		// (1) Calculate the support of each value for the dimension k
		Map<Integer, HashSet<Integer>> supportMap = calculateSupportForEachValueForEachDimension(context, k);
		
		// (2) For each value with a support > minsup,
		// we will add the MD-Pattern, create the projected database and make a recursive call.
		int nextK = k+1; // next dimension
		for(Entry<Integer, HashSet<Integer>> entry : supportMap.entrySet()){ // keyset
			HashSet<Integer> patternsIds = entry.getValue();
			if(patternsIds.size() >= minsupRelative){
				MDPattern pattern = createPattern(prefixe, k, context.getDimensionCount(), entry.getKey(), patternsIds);
				
				// (b) Create the projected database
				MDPatternsDatabase projectedContext = createProjectedDabatase(patternsIds, context);
				
				// (c) Make the recursive call
				dim(pattern, nextK, projectedContext);
				
				// (a) Add the MD-Pattern
				if(k == context.getDimensionCount()-1){
					patterns.addPattern(pattern, context.getDimensionCount());
				}
			}
		}
	}
	
	/**
	 * Create a projected database from a set of ID of MD-patterns to keep.
	 * @param patternsIds The list of MD-patterns to keep.
	 * @param contexte The original context.
	 * @return  A copy of the original context with only the MD-Patterns to keep.
	 */
	private MDPatternsDatabase createProjectedDabatase(HashSet<Integer> patternsIds,
			MDPatternsDatabase contexte) {
		MDPatternsDatabase projectedDatabase = new MDPatternsDatabase();
		for(MDPattern pattern : contexte.getMDPatterns()){
			if(patternsIds.contains(pattern.getId())){
				projectedDatabase.addMDPattern(pattern);
			}
		}
		return projectedDatabase;
	}

	/**
	 * Create a full pattern from a prefix and a value to add to a dimension.
	 * @param prefixe The prefix
	 * @param k  The index of the dimension
	 * @param dimensionCount The dimension count.
	 * @param value The value to be added
	 * @param patternsIds 
	 */
	private MDPattern createPattern(MDPattern prefixe, int k, int dimensionCount, int value, HashSet<Integer> patternsIds) {
		MDPattern pattern = new MDPattern(0);
		for(int i=0; i< dimensionCount; i++){
			if(i < k){
				pattern.addInteger(prefixe.getValue(i));
			}else if(i == k){
				pattern.addInteger(value);
			}else{
				pattern.addInteger(MDPattern.WILDCARD);
			}
		}	
		pattern.setPatternsIDList(patternsIds);
		return pattern;
	}

	/**
	 * Calculate the support of each value for a dimension k.
	 * @param contexte A database containing some MDPatterns.
	 * @param k A dimension
	 * @return A map (value, list of ids containing the value).
	 */
	private Map<Integer, HashSet<Integer>> calculateSupportForEachValueForEachDimension(
			MDPatternsDatabase contexte, int k) {
		databasePassesCount++;
		// Structure to keep the support of each value for a dimension k, and the list
		// of IDs of patterns that contains this value.
		Map<Integer, HashSet<Integer>> supportMap = new HashMap<Integer, HashSet<Integer>>();
		// Structure that contains the ID of all patterns
		HashSet<Integer> allPatternsID = new HashSet<Integer>();

		// For each pattern,
		for(MDPattern pattern : contexte.getMDPatterns()){
			// for the value of the dimension k of the pattern,
			// we increase its support in the structure for counting.
			int value = pattern.getValue(k);
			if(value != MDPattern.WILDCARD){
				HashSet<Integer> patternsIds = supportMap.get(value);
				if(patternsIds == null){
					patternsIds = new HashSet<Integer>();
					patternsIds.add(pattern.getId());
					supportMap.put(value, patternsIds);
				}else{
					patternsIds.add(pattern.getId());
				}
			}
			allPatternsID.add(pattern.getId());
		}
		// We add the wildcard "*" to the map.
		supportMap.put(MDPattern.WILDCARD, allPatternsID);

		return supportMap;
	}

	private void resetStatistiques() {
		timeStart = System.currentTimeMillis();
		databasePassesCount = 0;
	}
	
	public void printStats(int objectsCount) {
		System.out.println("=============  DIM - Statistics =============");
		long temps = System.currentTimeMillis() - timeStart;
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Database passes count : " + databasePassesCount);
		System.out.println(" Frequent patterns count : " + patterns.size()); 
		patterns.printFrequentPatterns(objectsCount);
		System.out.println("===================================================");
	}

	public int getDatabasePassesCount() {
		return databasePassesCount;
	}

}