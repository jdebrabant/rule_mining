package ca.pfv.spmf.frequentpatterns.uapriori;

import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of the U-Apriori algorithm as described by :
 * 
 *   Chui, C., Kao, B., Hung, E. (2007), Mining Frequent Itemsets fomr Uncertain Data, PAKDD 2007,  pp 47-58.
 * 
 * @author Philippe Fournier-Viger, 2010
 */
public class AlgoUApriori {

	protected Itemsets frequentItemsets = new Itemsets("FREQUENT ITEMSETS");
	protected ContextApriori context;
	protected int k; // level

	// stats
	protected int totalCandidateCount = 0;
	protected int databaseScanCount = 0;
	protected long startTimestamp;

	public AlgoUApriori(ContextApriori context) {
		this.context = context;
	}

	public Itemsets runAlgorithm(double minsupp) {
		resetStats();

		// Generate candidates with size k = 1 (all itemsets of size 1)
		k=1;
		Set<ItemsetApriori> candidatesSize1 = generateCandidateSize1();
		totalCandidateCount+=candidatesSize1.size();

		calculateSupportForEachCandidate(candidatesSize1);
		
//		addEmptySetToLevel0IfNecessary(candidatesSize1);

		// To build level 1, we keep only the frequent candidates.
		// We scan the database one time to calculate the support of each candidate.
		Set<ItemsetApriori> level = createLevelWithFrequentCandidates(minsupp,
				candidatesSize1);

		k = 2;
		// While the level is not empty
		while (!level.isEmpty()  ) {
			// Generate candidates of size K
			Set<ItemsetApriori> candidatesK = generateCandidateSizeK(level);
			totalCandidateCount+=candidatesK.size();

			// We scan the database one time to calculate the support
			// of each candidates.
			calculateSupportForEachCandidate(candidatesK);

			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			Set<ItemsetApriori> levelK = createLevelWithFrequentCandidates(
					minsupp, candidatesK);
			level = levelK; // We keep only the last level... 
			k++;
		}
		return frequentItemsets; // Return all frequent itemsets found!
	}

//	/**
//	 * Add the empty set to level 0 if no itemset of level 1 have a support
//	 * that is equal to the number of transactions in the binary context.
//	 * @param candidatesSize1 Candidates of level 1.
//	 */
//	protected void addEmptySetToLevel0IfNecessary(Set<ItemsetApriori> candidatesSize1) {
//		for(ItemsetApriori candidate : candidatesSize1){
//			if(candidate.getExpectedSupport() == context.size()){
//				return;
//			}
//		}
//		ItemsetApriori emptySet = new ItemsetApriori();
//		emptySet.setTransactioncount(context.size());
//		frequentItemsets.addItemset(emptySet, 0); // ajout ensemble vide
//	}

	protected Set<ItemsetApriori> createLevelWithFrequentCandidates(double minsupp,Set<ItemsetApriori> candidatesK) {
		Set<ItemsetApriori> levelK = new HashSet<ItemsetApriori>();
		for (ItemsetApriori candidate : candidatesK) { 
			if (candidate.getExpectedSupport() >= minsupp) {
				levelK.add(candidate);
				frequentItemsets.addItemset(candidate, k);
			}
		}
		return levelK;
	}

	protected void calculateSupportForEachCandidate(
			Set<ItemsetApriori> candidatesK) {
		databaseScanCount++;
		for (ItemsetApriori transaction : context.getObjects()) {
			// For each candidate of level K, we increase its support
			// if it is included in the transaction.
		candidateLoop : for (ItemsetApriori candidate : candidatesK) {
				double expectedSupport = 0;
				for(ItemApriori item : candidate.getItems()){
					boolean found = false;
					for(ItemApriori itemT : transaction.getItems()){
						if(item.getId() == itemT.getId()){
							found = true;
							if(expectedSupport == 0){
								expectedSupport = itemT.getProbability();
							}else{
								expectedSupport *= itemT.getProbability();
							}
							break;
						}
						else if (item.getId() < itemT.getId()){
							break;
						}
					}	
					if(found == false){
						continue candidateLoop;
					}
				}
				candidate.increaseTransactionCount(expectedSupport);
			}
		}
	}

	// Based on the description of Pasquier 99: "Efficient mining..."
	protected Set<ItemsetApriori> generateCandidateSize1() {
		Set<ItemsetApriori> candidates = new HashSet<ItemsetApriori>(); // liste  d'itemsets
		for (ItemApriori item : context.getAttributes()) {
			ItemsetApriori itemset = new ItemsetApriori();
			itemset.addItem(item);
			candidates.add(itemset);
		}
		return candidates;
	}

	protected Set<ItemsetApriori> generateCandidateSizeK(Set<ItemsetApriori> levelK_1) {
		Set<ItemsetApriori> candidates = new HashSet<ItemsetApriori>();

		// For each itemset I1 and I2 of level k-1
		Object[] itemsets = levelK_1.toArray();
		for(int i=0; i< levelK_1.size(); i++){
			ItemsetApriori itemset1 = (ItemsetApriori)itemsets[i];
			for(int j=0; j< levelK_1.size(); j++){
				ItemsetApriori itemset2 = (ItemsetApriori)itemsets[j];
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				ItemApriori missing = itemset1.allTheSameExceptLastItem(itemset2);
				if(missing != null ){
					// Create a new candidate by combining itemset1 and itemset2
					ItemsetApriori candidate = new ItemsetApriori();
					for(ItemApriori item : itemset1.getItems()){
						if(item.getId() > missing.getId() && missing != null){
							candidate.addItem(missing);
							missing = null;
						}
						candidate.addItem(item);
					}
					if(missing != null){
						candidate.addItem(missing);
					}
//					System.out.println(" " + itemset1.toString() + " + " + itemset2.toString() + " = " + candidate.toString());
	
					// The candidate is tested to see if its subsets of size k-1 are included in
					// level k-1 (they are frequent).
					if(allSubsetsOfSizeK_1AreFrequent(candidate,levelK_1)){
						candidates.add(candidate);
					}
				}
			}
		}
		return candidates;
	}

	protected boolean allSubsetsOfSizeK_1AreFrequent(ItemsetApriori candidate, Set<ItemsetApriori> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		if(candidate.size() == 1){
			return true;
		}
		for(ItemApriori item : candidate.getItems()){
			ItemsetApriori subset = candidate.cloneItemSetMinusOneItem(item);
			boolean found = false;
			for(ItemsetApriori itemset : levelK_1){
				if(itemset.isEqualTo(subset)){
					found = true;
					break;
				}
			}
			if(found == false){
				return false;
			}
		}
		return true;
	}

	protected void resetStats() {
		startTimestamp = System.currentTimeMillis();
		totalCandidateCount = 0;
		databaseScanCount = 0;
	}

	public void printStats() {
		System.out
				.println("=============  APRIORI - STATS =============");
		long temps = System.currentTimeMillis() - startTimestamp;
//		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Transactions count from database : "
				+ context.size());
		System.out.println(" Candidates count : " + totalCandidateCount);
		System.out.println(" Database scan count : " + databaseScanCount);
		System.out.println(" The algorithm stopped at size " + (k - 1)
				+ ", because there is no candidate");
		System.out.println(" Frequent itemsets count : " + frequentItemsets.getItemsetsCount()); 
		frequentItemsets.printItemsets();

		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}

	public Itemsets getItemsets() {
		return frequentItemsets;
	}
}
