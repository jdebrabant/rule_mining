package ca.pfv.spmf.associationrules.agrawal_FPGrowth_version;

import java.util.HashSet;
import java.util.Set;

import ca.pfv.spmf.frequentpatterns.fpgrowth.Itemset;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Itemsets;

/**
 * This is an implementation of the "faster algorithm" described in 
 * Agrawal & al. 1994, IBM Research Report RJ9839, June 1994. 
 * @author Philippe Fournier-Viger, 2008
 */

public class AlgoAgrawalFaster94_FPGrowth_version {
	private Itemsets patterns;
	private RulesAgrawal rules;
	
	private double minconf;
	
	public AlgoAgrawalFaster94_FPGrowth_version(double minconf){
		this.minconf = minconf;
	}

	public RulesAgrawal runAlgorithm(Itemsets patterns) {
		this.patterns = patterns;
		rules = new RulesAgrawal("All association rules");
		
		//For each frequent itemset of size >=2
		for(int k=2; k< patterns.getLevels().size(); k++){
			for(Itemset lk : patterns.getLevels().get(k)){
				// create H1
				Set<Itemset> H1 = new HashSet<Itemset>();
				for(Itemset itemsetSize1 : patterns.getLevels().get(1)){
					if(lk.contains(itemsetSize1.getItems().get(0))){
						H1.add(itemsetSize1);
					}
				}
//				lk.print(); // DEBUG
//				System.out.println(); // DEBUG
				
				/// ================ I ADDED THIS BECAUSE THE ALGORITHM AS DESCRIBED BY AGRAWAL94
				/// ================ DID NOT GENERATE ALL  THE ASSOCIATION RULES
				Set<Itemset> H1_for_recursion  = new HashSet<Itemset>();
				for(Itemset hm_P_1 : H1){
					Itemset itemset_Lk_minus_hm_P_1 = lk.cloneItemSetMinusAnItemset(hm_P_1);

					// double conf = supp(lk)  / supp (lk - hm+1)
					calculateSupport(itemset_Lk_minus_hm_P_1);   //  THIS COULD BE OPTIMIZED ? OR DONE ANOTHER WAY ?
					double conf = ((double)lk.getAbsoluteSupport()) / ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport());
					
					if(conf >= minconf){
						RuleAgrawal rule = new RuleAgrawal(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
						rules.addRule(rule);
						H1_for_recursion.add(hm_P_1);// for recursion
					}
				}
				// ================ END OF WHAT I HAVE ADDED

				// call apGenRules
				apGenrules(k, 1, lk, H1_for_recursion);
			}
		}
		
		return rules;
	}

	// apGenRules, p14. from Agrawal
	private void apGenrules(int k, int m, Itemset lk, Set<Itemset> Hm) {
//		System.out.println(" " + lk.toString() + "  " + Hm.toString());
		if(k > m+1){
			Set<Itemset> Hm_plus_1 = generateCandidateSizeK(Hm);
			Set<Itemset> Hm_plus_1_for_recursion = new HashSet<Itemset>();
			for(Itemset hm_P_1 : Hm_plus_1){
				Itemset itemset_Lk_minus_hm_P_1 = lk.cloneItemSetMinusAnItemset(hm_P_1);

				calculateSupport(itemset_Lk_minus_hm_P_1);   // THIS COULD BE DONE ANOTHER WAY ?
				                                             // IT COULD PERHAPS BE IMPROVED....
				double conf = ((double)lk.getAbsoluteSupport()) / ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport());
				
				if(conf >= minconf){
					RuleAgrawal rule = new RuleAgrawal(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
					rules.addRule(rule);
					Hm_plus_1_for_recursion.add(hm_P_1);
				}
			}
			apGenrules(k, m+1, lk, Hm_plus_1_for_recursion);
		}
	}

	/**
	 * Calculate the support of an itemset by looking at the frequent patterns of the same size.
	 * @param itemset_Lk_minus_hm_P_1   The itemset.
	 */
	private void calculateSupport(Itemset itemset_Lk_minus_hm_P_1) {
		// loop over all the patterns of the same size.
		for(Itemset itemset : patterns.getLevels().get(itemset_Lk_minus_hm_P_1.size())){
			//If the pattern is found
			if(itemset.allTheSame(itemset_Lk_minus_hm_P_1)){
				// set its support to the same value.
				itemset_Lk_minus_hm_P_1.setTransactioncount(itemset.getAbsoluteSupport());
				return;
			}
		}
	}

	/**
	 * Generating candidate itemsets of size k from frequent itemsets of size k-1.
	 *  This is called "apriori-gen" in the paper by agrawal.  This method  is also
	 *  used by the Apriori algorithm for generating candidates.
	 * @param levelK_1
	 * @return
	 */
	protected Set<Itemset> generateCandidateSizeK(Set<Itemset> levelK_1) {
		Set<Itemset> candidates = new HashSet<Itemset>();

		// For each itemset I1 and I2 of level k-1
		for(Itemset itemset1 : levelK_1){
			for(Itemset itemset2 : levelK_1){
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				Integer missing = itemset1.allTheSameExceptLastItem(itemset2);
				if(missing != null ){
					// Create a new candidate by combining itemset1 and itemset2
					Itemset candidate = new Itemset();
					for(Integer item : itemset1.getItems()){
						if(item > missing && missing != null){
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
	
	/**
	 * This method checks if all the subsets of size "k" of the itemset "candidate" are frequent.
	 * @param candidate An itemset of size "k".
	 * @param levelK_1  The frequent itemsets of size "k-1".
	 * @return
	 */
	protected boolean allSubsetsOfSizeK_1AreFrequent(Itemset candidate, Set<Itemset> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		if(candidate.size() == 1){
			return true;
		}
		for(Integer item : candidate.getItems()){
			Itemset subset = candidate.cloneItemSetMinusOneItem(item);
			boolean found = false;
			for(Itemset itemset : levelK_1){
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


	
}
