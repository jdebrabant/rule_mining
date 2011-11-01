package ca.pfv.spmf.frequentpatterns.apriori;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This is an implementation of the Apriori algorithm as described by :
 * 
 *   Pasquier, N., Bastide, Y., Taouil, R., Lakhal, L., (1999). Efficient Mining of Association Rules using 
 *   Closed Itemset Lattices. Information Systems, Elsevier Science, 24(1), pages 25-46 .
 *   
 * and the original Apriori article:
 * 
 *   Agrawal R, Srikant R. "Fast Algorithms for Mining Association Rules", VLDB. Sep 12-15 1994, Chile, 487-99,
 * 
 * The Apriori algorithm finds all the frequents itemsets and their support
 * in a binary context.
 * 
 * @author Philippe Fournier-Viger, 2008
 */
public class AlgoAprioriClose {

	protected Itemsets frequentItemsets = new Itemsets("FREQUENT ITEMSETS");
	private Itemsets frequentClosed = new Itemsets("FREQUENT CLOSED ITEMSETS");

	protected ContextApriori context;
	protected int k; // level
	

	public AlgoAprioriClose(ContextApriori context) {
		this.context = context;
	}

	public Itemsets runAlgorithm(double minsupp) {

		// Generate candidates of size k =1 (all itemsets of size)
		k=1;
		List<ItemsetApriori> candidatesSize1 = generateCandidateSize1();

		calculateSupportForEachCandidate(candidatesSize1);
		
		addEmptySetToLevel0IfNecessary(candidatesSize1);
		
		Collection<ItemsetApriori> lastLevel = frequentItemsets.getLevels().get(0); // DIFF

		// To build level 1, we keep only the frequent candidates.
		// We scan the database...
		List<ItemsetApriori> level = createLevelWithFrequentCandidates(minsupp,
				candidatesSize1);

		k = 2;
		// While the level is not empty
		while (level.size() != 0 ) {

			// Generate candidates of size K
			List<ItemsetApriori> candidatesK = generateCandidateSizeK(level);

			// We scan the database one time, to calculate the support
			// of each candidate.
			calculateSupportForEachCandidate(candidatesK);

			// we create level k with all the candidates that have 
			// a support higher than minsupp
			List<ItemsetApriori> levelK = createLevelWithFrequentCandidates(
					minsupp, candidatesK);
			
			// We check all sets of level k-1 for closure
			checkIfItemsetsK_1AreClosed(level, levelK);
			
			lastLevel = level;
			level = levelK; //We keep only one level.
			k++;
		}
		
		//  We label all items of the last level as closed, if they are not. 
		for(ItemsetApriori itemset : lastLevel){   
			if(!itemset.isClose()){
				itemset.setClose(true);  
				frequentClosed.addItemset(itemset, k-2);  
			}
		} 
		return frequentItemsets;
	}
	
	private void checkIfItemsetsK_1AreClosed(Collection<ItemsetApriori> level,
			List<ItemsetApriori> levelK) {
		for(ItemsetApriori itemset : level){
			boolean isClosed = true;
			for(ItemsetApriori itemsetK : levelK){
				if(itemsetK.getAbsoluteSupport() == itemset.getAbsoluteSupport() &&
						itemset.includedIn(itemsetK)){
					isClosed = false; 
					break;
				}
			}
			if(isClosed){
				itemset.setClose(true);
				frequentClosed.addItemset(itemset, k-1);
			}
		}
	}

	protected void addEmptySetToLevel0IfNecessary(List<ItemsetApriori> candidatesSize1) {
		// We add the emptyset to L0, if no itemset of level 1 has
		// its support equal to the number of transactions in the binary context.
		for(ItemsetApriori candidate : candidatesSize1){
			if(candidate.getAbsoluteSupport() == context.size()){
				return;
			}
		}
		ItemsetApriori emptySet = new ItemsetApriori();
		emptySet.setTransactioncount(context.size());
		frequentItemsets.addItemset(emptySet, 0); // add empty set
		emptySet.setClose(true);              
		frequentClosed.addItemset(emptySet, 0); // add empty set
	}

	protected List<ItemsetApriori> createLevelWithFrequentCandidates(double minsupp,List<ItemsetApriori> candidatesK) {
		List<ItemsetApriori> levelK = new ArrayList<ItemsetApriori>();
		for (ItemsetApriori candidate : candidatesK) { 
			if (candidate.getRelativeSupport(context.size()) >= minsupp) {
				levelK.add(candidate);
				frequentItemsets.addItemset(candidate, k);
			}
		}
		return levelK;
	}

	protected void calculateSupportForEachCandidate(
			List<ItemsetApriori> candidatesK) {
		for (ItemsetApriori transaction : context.getObjects()) {
			// For each candidate of level K, we increase its support
			// if it is included in the transaction.
			for (ItemsetApriori candidate : candidatesK) {
				if (candidate.includedIn(transaction)) {
					candidate.increaseTransactionCount();
				}
			}
		}
	}

	// Based on the description of Pasquier 99: "Efficient mining..."
	protected List<ItemsetApriori> generateCandidateSize1() {
		// sort the items in ascending order.
		List<ItemApriori> list = new ArrayList<ItemApriori>(context.getAttributes());
		Collections.sort(list, new Comparator<ItemApriori>(){
			public int compare(ItemApriori o1, ItemApriori o2) {
				if(o1.getId() == o2.getId()){
					return 0;
				}
				return o1.getId() - o2.getId();
			}
		});
		List<ItemsetApriori> candidates = new ArrayList<ItemsetApriori>(); // liste  d'itemsets
		for (ItemApriori item : list) {
			ItemsetApriori itemset = new ItemsetApriori();
			itemset.addItem(item);
			candidates.add(itemset);
		}
		return candidates;
	}

	protected List<ItemsetApriori> generateCandidateSizeK(List<ItemsetApriori> levelK_1) {
		List<ItemsetApriori> candidates = new ArrayList<ItemsetApriori>();
		
		// For each itemset I1 and I2 of level k-1
		loop1:	for(int i=0; i< levelK_1.size(); i++){
					ItemsetApriori itemset1 = (ItemsetApriori)levelK_1.get(i);
		loop2:		for(int j=i+1; j< levelK_1.size(); j++){
						ItemsetApriori itemset2 = (ItemsetApriori)levelK_1.get(j);
				
				// we compare items of itemset1  and itemset2.
				// If they have all the same k-1 items and the last item of itemset1 is smaller than
				// the last item of itemset2, we will combine them to generate a candidate
				for(int k=0; k< itemset1.size(); k++){
					// if they are the last items
					if(k == itemset1.size()-1){ 
						// the one from itemset1 should be smaller (lexical order) 
						// and different from the one of itemset2
						if(itemset1.getItems().get(k).getId() >= itemset2.get(k).getId()){  
							continue loop1;
						}
					}
					// if they are not the last items, and 
					else if(itemset1.getItems().get(k).getId() < itemset2.get(k).getId()){ 
						continue loop2; // we continue searching
					}
					else if(itemset1.getItems().get(k).getId() > itemset2.get(k).getId()){ 
						continue loop1;  // we stop searching:  because of lexical order
					}
				}
				ItemApriori missing = itemset2.get(itemset2.size()-1);
				// Now we will combine itemset1 and itemset2
				// Create a new candidate by combining itemset1 and itemset2
				ItemsetApriori candidate = new ItemsetApriori();
				for(int k=0; k < itemset1.size(); k++){
					candidate.addItem(itemset1.get(k));
				}
				candidate.addItem(missing);
				// The candidate is tested to see if its subsets of size k-1 are included in
				// level k-1 (they are frequent).
				if(allSubsetsOfSizeK_1AreFrequent(candidate,levelK_1)){
					candidates.add(candidate);
				}
			}
		}
		return candidates;
	}

	protected boolean allSubsetsOfSizeK_1AreFrequent(ItemsetApriori candidate, List<ItemsetApriori> levelK_1) {
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


	public void printStats() {
		System.out
				.println("==========  APRIORI-CLOSE - STATS  =========="); 
		System.out.println(" Database transactions count : "
				+ context.size());
		System.out.println(" The algorithm stopped at level  " + (k - 1)
				+ ", because there is no candidate");

		System.out.println(" Frequent itemsets count : " + frequentItemsets.getItemsetsCount()); 
		Double percentClosed = ((double)frequentClosed.getItemsetsCount() / (double)frequentItemsets.getItemsetsCount());
		System.out.println(" Frequent closed itemsets count : " + frequentClosed.getItemsetsCount()   
				+ " (" + percentClosed + "% of frequents itemsets )"); 
		frequentItemsets.printItemsets(context.size());

		frequentClosed.printItemsets(context.size());
		System.out
				.println("===================================================");
	}

	public Itemsets getFrequentClosed() {
		return frequentClosed;
	}

	public Itemsets getItemsets() {
		return frequentItemsets;
	}
}
