package ca.pfv.spmf.highutility.two_phase_algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This is an implementation of the "Two-Phase Algorithm" for High-Utility Itemsets Mining
 * as described in the conference paper : 
 * 
 *  Liu, Y., Liao, W.-K., Choudhary, A. (2005) A Two-Phase Algorithm for Fast-Discovery of 
 *  High Utility Itemsets, Proceedings of PAKDD 2005, pp. 689-695.
 *  
 *  This implementation uses the Apriori algorithm as it seems to be suggested by the article, even if
 *  the Apriori algorithm is not mentionned explicitly in the article.
 * 
 * @author Philippe Fournier-Viger, 2010
 */
public class AlgoTwoPhase {

	private Itemsets highUtilityItemsets = null;
	protected Database database;
	
	// variables for counting support of items
	Map<Integer, Set<Integer>> mapItemCount = new HashMap<Integer, Set<Integer>>();
	
	int minUtility;
	
	long startTimestamp = 0;
	private int candidatesCount;
	
	public AlgoTwoPhase(Database context) {
		this.database = context;
	}

	public Itemsets runAlgorithm(int minUtility) {
		this.minUtility = minUtility;
		startTimestamp = System.currentTimeMillis();

		highUtilityItemsets = new Itemsets("HIGH UTILITY ITEMSETS");
		
		// ===================  PHASE 1: GENERATE CANDIDATES  =================== 
		
		// First, we create the level of candidate itemsets of size 1
		List<Itemset> candidatesSize1 = new ArrayList<Itemset>();
		
		// Scan database one time to get the tidset of each item and its utility for the whole database
		Map<Integer, Set<Integer>> mapItemTidsets = new HashMap<Integer, Set<Integer>>();
		Map<Integer, Integer> mapItemTWU = new HashMap<Integer, Integer>();
		int maxItem = Integer.MIN_VALUE;
		for(int i=0; i< database.size(); i++){
			Transaction transaction = database.getTransactions().get(i);
			
			for(Integer item : transaction.getItems()){
				if(item > maxItem){
					maxItem = item;
				}
				// add tid to tidset of item
				Set<Integer> tidset = mapItemTidsets.get(item);
				if(tidset == null){
					tidset = new HashSet<Integer>();
					mapItemTidsets.put(item, tidset);
				}
				tidset.add(i);
				
				// add transaction utility for this item
				Integer sumUtility = mapItemTWU.get(item);
				if(sumUtility == null){
					sumUtility = 0;
				}
				sumUtility += transaction.getTransactionUtility();
				mapItemTWU.put(item, sumUtility);
			}
		}
		
		// create a candidate itemset for each item having a TWU  >= minUtil
		for(int item=0; item<= maxItem; item++){
			Integer estimatedUtility = mapItemTWU.get(item);
			if(estimatedUtility != null && estimatedUtility >= minUtility){
				Itemset itemset = new Itemset();
				itemset.addItem(item);
				itemset.setTransactioncount(mapItemTidsets.get(item));
				candidatesSize1.add(itemset);
				highUtilityItemsets.addItemset(itemset, itemset.size());
			}
		}

		// From candidate of size 1, we recursively create candidates of greater size.
		List<Itemset> currentLevel = candidatesSize1;
		while (true) {
			// Generate candidates of size K+1
			int candidateCount = highUtilityItemsets.getItemsetsCount();
			currentLevel = generateCandidateSizeK(currentLevel, highUtilityItemsets);
			// if no new candidates are found, then we stop because no more candidates will be found.
			if(candidateCount == highUtilityItemsets.getItemsetsCount()){
				break;
			}
		}
		
		candidatesCount = highUtilityItemsets.getItemsetsCount();
		
		// ========================  PHASE 2: Calculate exact utility of each candidate =============
		for(List<Itemset> level : highUtilityItemsets.getLevels()){
			Iterator<Itemset> iterItemset = level.iterator();
			while(iterItemset.hasNext()){
				Itemset candidate = iterItemset.next();
				
				// calculate exact utility of itemset
				for(Transaction transaction : database.getTransactions()){
					int transactionUtility =0;
					int matchesCount =0;
					for(int i=0; i< transaction.size(); i++){
						if(candidate.getItems().contains(transaction.get(i))){
							transactionUtility += transaction.getItemsUtilities().get(i);
							matchesCount++;
						}
					}
					if(matchesCount == candidate.size()){
						candidate.incrementUtility(transactionUtility);
					}
				}
				
				if(candidate.getUtility() < minUtility){
					iterItemset.remove();
					highUtilityItemsets.decreaseCount();
				}
				
			}
		}

		return highUtilityItemsets; // Return all frequent itemsets found!
	}

	protected List<Itemset> createLevelWithCandidates1(Set<Integer> items, Map<Integer, Set<Integer>> mapItemCount, Itemsets candidatesHTWUI) {
		List<Itemset> levelK = new ArrayList<Itemset>();

		for(Integer item : items){
			Itemset itemset = new Itemset();
			itemset.addItem(item);
			itemset.setTransactioncount(mapItemCount.get(item));
			levelK.add(itemset);
			candidatesHTWUI.addItemset(itemset, itemset.size());
		}
		return levelK;
	}

	// Based on the description of Pasquier 99: "Efficient mining..."
	protected List<Itemset> generateCandidateSize1() {
		List<Itemset> candidates = new ArrayList<Itemset>(); // liste  d'itemsets
		for (Integer item : database.getAttributes()) {
			Itemset itemset = new Itemset();
			itemset.addItem(item);
			candidates.add(itemset);
		}
		return candidates;
	}

	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1, Itemsets candidatesHTWUI) {
		
	// For each itemset I1 and I2 of level k-1
	loop1:	for(int i=0; i< levelK_1.size(); i++){
				Itemset itemset1 = levelK_1.get(i);
	loop2:		for(int j=i+1; j< levelK_1.size(); j++){
					Itemset itemset2 = levelK_1.get(j);
			
				// we compare items of itemset1  and itemset2.
				// If they have all the same k-1 items and the last item of itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a candidate
				for(int k=0; k< itemset1.size(); k++){
					// if they are the last items
					if(k == itemset1.size()-1){ 
						// the one from itemset1 should be smaller (lexical order) 
						// and different from the one of itemset2
						if(itemset1.getItems().get(k) >= itemset2.get(k)){  
							continue loop1;
						}
					}
					// if they are not the last items, and 
					else if(itemset1.getItems().get(k) < itemset2.get(k)){ 
						continue loop2; // we continue searching
					}
					else if(itemset1.getItems().get(k) > itemset2.get(k)){ 
						continue loop1;  // we stop searching:  because of lexical order
					}
				}
				
				// NOW COMBINE ITEMSET 1 AND ITEMSET 2
				Integer missing = itemset2.get(itemset2.size()-1);

				// create list of common tids
				Set<Integer> tidset = new HashSet<Integer>();
				for(Integer val1 : itemset1.getTransactionsIds()){
					if(itemset2.getTransactionsIds().contains(val1)){
						tidset.add(val1);
					}
				}
				
				// calculate TWU of itemset
				int twu =0;
				for(Integer tid : tidset){
					twu += database.getTransactions().get(tid).getTransactionUtility();
				}
		
				if(twu >= minUtility){
					// Create a new candidate by combining itemset1 and itemset2
					Itemset candidate = new Itemset();
					for(int k=0; k < itemset1.size(); k++){
						candidate.addItem(itemset1.get(k));
					}
					candidate.addItem(missing);
					candidate.setTransactioncount(tidset);
					candidatesHTWUI.addItemset(candidate, candidate.size());
				}
			}
		}
		return candidatesHTWUI.getLevels().get(candidatesHTWUI.getLevels().size()-1);
	}
	
	public void printStats() {
		System.out
				.println("=============  TWO-PHASE ALGORITHM - STATS =============");
		long temps = System.currentTimeMillis() - startTimestamp;
		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Transactions count from database : "
				+ database.size());
		System.out.println(" Candidates count : " + candidatesCount); 
		System.out.println(" High-utility itemsets count : " + highUtilityItemsets.getItemsetsCount()); 

		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
}
