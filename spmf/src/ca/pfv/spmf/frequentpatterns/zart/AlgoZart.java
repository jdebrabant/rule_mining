package ca.pfv.spmf.frequentpatterns.zart;

import java.util.ArrayList;
import java.util.List;


/**
 * This is an implementation of Zart as described in the article : 
 * "Zart : a Multifunctional Itemset Mining Algorithm" de Laszlo Szathmary et al.
 * ZART finds all the frequent closed itemsets in a binary context, their associated
 * minimal generator(s) and their support. 
 * This algorithm could be optimized in various way as described in the article by Szathmary,
 * for example, by using the Trie data structure, but this was not done here.
 * @author Philippe Fournier-Viger, copyright 2008
 */

public class AlgoZart {

	private double minsupp;
	
	private ContextZart context = null;
	
	private TZTableClosed tableClosed = null;
	private TFTableFrequent tableFrequent = null;
	private TCTableCandidate tableCandidate = null;
	
	private List<ItemsetZart> frequentGeneratorsFG = null; // 2

	public AlgoZart(double minsupp) {
		this.minsupp = minsupp;
	}
	
	private ItemsetZart emptyset = new ItemsetZart();
	
	public TZTableClosed runAlgorithm(ContextZart context){
		
		this.context = context;
		
		frequentGeneratorsFG = new ArrayList<ItemsetZart>(); // 2
		tableClosed = new TZTableClosed();
		tableFrequent = new TFTableFrequent();
		tableCandidate = new TCTableCandidate();
		
		boolean fullCollumn = false; // 1
		
		fillCandidateWith1Itemset(); // 3
		
		calculateSupportForEachCandidate(tableCandidate.levels.get(0)); // 4
		
		// 5  F1 <- c elementOf TC1 | c.support >= minsupp
		for(ItemsetZart candidate : tableCandidate.levels.get(0)){
			if(candidate.getRelativeSupport(context.size()) >= minsupp){
				tableFrequent.addFrequentItemset(candidate);
			}
		}
		
		// 6 : Loops over the row of F1
		for(ItemsetZart l : tableFrequent.getLevelForZart(0)){
			tableFrequent.mapClosed.put(l, true); // 8
			if(l.getAbsoluteSupport() == context.getObjects().size()){ // 9
				tableFrequent.mapKey.put(l, false); // 10  The empty set is its generator (IMPORTANT)
				fullCollumn = true; // 11
			}else{
				tableFrequent.mapKey.put(l, true); // 13
			}
		}
		
		// 15 add empty set if necessary
		if(fullCollumn){
			frequentGeneratorsFG.add(emptyset);  
		}else{   // MODIFICATION BY PHILIPPE TO FIX ALGORITHM ========
			tableFrequent.addFrequentItemset(emptyset);
			tableFrequent.mapClosed.put(emptyset, true);
			tableFrequent.mapPredSupp.put(emptyset, context.size());
			tableClosed.addClosedItemset(emptyset);
			tableClosed.mapGenerators.put(emptyset, new ArrayList<ItemsetZart>());
			emptyset.setTransactioncount(context.size());
		}
		
		int i=1;
		for(; true; i++){  // 16
			zartGen(i); // 18   Ci+1 = ZartGen(Fi);
			
			if(tableCandidate.levels.get(i).size() == 0){ // 19
				break;
			}
			
			if(tableCandidate.thereisARowKeyValueIsTrue(i)){ // 20
				// 22 loop over the element of database o
				for(ItemsetZart o : context.getObjects()){ //22
					for(ItemsetZart s : subset(tableCandidate.levels.get(i), o)){ // 23, 24
						if(tableCandidate.mapKey.get(s)){
							s.increaseTransactionCount(); //25
						}
					}
				}	
			}
			
			for(ItemsetZart c : tableCandidate.levels.get(i)){ //28
				if(c.getRelativeSupport(context.size()) >= minsupp){
					//31 
					if(tableCandidate.mapKey.get(c) == true && c.getAbsoluteSupport() == tableCandidate.mapPredSupp.get(c)){
						tableCandidate.mapKey.put(c, false); //32
					}
					tableFrequent.addFrequentItemset(c); // 33
					tableFrequent.mapKey.put(c, tableCandidate.mapKey.get(c));  // WAS NOT EXPLICIT IN THE ALGORITHM
					tableFrequent.mapPredSupp.put(c, tableCandidate.mapPredSupp.get(c)); // WAS NOT EXPLICIT IN THE ALGORITHM
				}
			}
			
			for(ItemsetZart l : tableFrequent.getLevelForZart(i)){ // 36
				tableFrequent.mapClosed.put(l, true); //37
				for(ItemsetZart s : subset(tableFrequent.getLevelForZart(i-1), l)){ // 38, 39
					if(s.getAbsoluteSupport() == l.getAbsoluteSupport()){ // 40
						tableFrequent.mapClosed.put(s, false);
					}
				}
			}
			
			// 42
			tableClosed.levels.add(new ArrayList<ItemsetZart>());
			for(ItemsetZart l : tableFrequent.getLevelForZart(i-1)){
				if(tableFrequent.mapClosed.get(l) == true){
					tableClosed.getLevelForZart(i-1).add(l);
				}
			}
			
			findGenerators(tableClosed.getLevelForZart(i-1), i); // 43
		}
		
		//  ....  45
		tableClosed.levels.add(new ArrayList<ItemsetZart>());
		for(ItemsetZart l : tableFrequent.getLevelForZart(i-1)){
			tableClosed.getLevelForZart(i-1).add(l);
		}
		// .....  46
		findGenerators(tableClosed.getLevelForZart(i-1),  i);
		
		return tableClosed;
	}
	
	private void findGenerators(List<ItemsetZart> zi, int i) {
		for(ItemsetZart z : zi){ // 1
			List<ItemsetZart> s = subset(frequentGeneratorsFG, z);  // 3
			tableClosed.mapGenerators.put(z, s);  // 4
			frequentGeneratorsFG.removeAll(s); // 5
		}  
		// 7
		for(ItemsetZart l : tableFrequent.getLevelForZart(i-1)){
			if(tableFrequent.mapKey.get(l) == true && tableFrequent.mapClosed.get(l) == false){
				frequentGeneratorsFG.add(l);
			}
		}
	}

	/**
	 * Subsets function : this method gets a set of itemsets S, and an arbitrary
	 * itemset l. The function returns such elements of S that are subsets of l. 
	 * This function can be implemented very efficiently with the trie data structure.
	 */
	private List<ItemsetZart> subset(List<ItemsetZart> s, ItemsetZart l) {
		List<ItemsetZart> retour = new ArrayList<ItemsetZart>();
		for(ItemsetZart itemsetS : s){
			boolean allIncluded = true;
			for(ItemZart itemS : itemsetS.getItems()){
				if(!l.contains(itemS)){
					allIncluded = false;
				}
			}
			if(allIncluded){
				retour.add(itemsetS);
			}
		}
		return retour;
	}

	private void zartGen(int i) {
		// 1
		prepareCandidateSizeI(i);
		
		for(ItemsetZart c : new ArrayList<ItemsetZart>(tableCandidate.levels.get(i))){ // 2      // CONCURENT EXCEPTION : PATCH MER*IQUE!
			tableCandidate.mapKey.put(c, true); // 4
			tableCandidate.mapPredSupp.put(c, context.getObjects().size() + 1);
			// 7
			// To generate all sets of size k-1: S, we will proceed
			// by removing each element one by one.
			for(ItemZart item : c.getItems()){
				ItemsetZart s = c.cloneItemSetMinusOneItem(item);
				boolean found = false;
				for(ItemsetZart itemset2 : tableFrequent.getLevelForZart(i-1)){
					if(itemset2.isEqualTo(s)){
						found = true;
						break;
					}
				}
				if(found == false){ // a subset of S is not frequent
					tableCandidate.levels.get(i).remove(c);
				}else{
					ItemsetZart occurenceS = getPreviousOccurenceOfItemset(s, tableCandidate.levels.get(i-1));  // AJOUT NÉCESSAIRE
					if(occurenceS.getAbsoluteSupport() < tableCandidate.mapPredSupp.get(c)){ // 11
						tableCandidate.mapPredSupp.put(c, occurenceS.getAbsoluteSupport()); 
					}else{
						tableCandidate.mapPredSupp.put(c, tableCandidate.mapPredSupp.get(c));
					}
					if(tableFrequent.mapKey.get(occurenceS) == false){  // 12 
						tableCandidate.mapKey.put(c, false); 
					}
				}
			}

			// 15
			if(tableCandidate.mapKey.get(c) == false){
				c.setTransactioncount(tableCandidate.mapPredSupp.get(c));
			}
		}
	}
	
	public ItemsetZart getPreviousOccurenceOfItemset(ItemsetZart itemset, List<ItemsetZart> list){
		for(ItemsetZart itemset2 : list){
			if(itemset2.isEqualTo(itemset)){
				return itemset2;
			}
		}
		return null;
	}
	
	protected void prepareCandidateSizeI(int i) {
		tableCandidate.levels.add(new ArrayList<ItemsetZart>());
		
		// For each itemset I1 and  I2 of levelk-1		

		for(ItemsetZart itemset1 : tableFrequent.getLevelForZart(i-1)){
			for(ItemsetZart itemset2 : tableFrequent.getLevelForZart(i-1)){
				// If I1 is smaller than I2 according to lectical order
				// and that they have only one element that is different
//				Integer missing = itemset2.haveOneItemDifferent(itemset1);
				ItemZart missing = itemset2.shareFirstIItemsOnly(itemset1);
				if(missing != null){
//					System.out.print("itemset1 : "); itemset1.print();
//					System.out.print(" itemset2 : "); itemset2.print();
					// Create a candidate by combining I1 and I2
					ItemsetZart candidate = new ItemsetZart();
					for(ItemZart item : itemset2.getItems()){
						candidate.addItem(item);
					}
					candidate.addItem(missing);
					tableCandidate.levels.get(i).add(candidate);
				}
			}
		}
	}

	protected void fillCandidateWith1Itemset() {
		tableCandidate.levels.add(new ArrayList<ItemsetZart>());
		for (ItemZart item : context.getAttributes()) {
			ItemsetZart itemset = new ItemsetZart();
			itemset.addItem(item);
			tableCandidate.levels.get(0).add(itemset);
		}
	}
	
	protected void calculateSupportForEachCandidate(List<ItemsetZart> candidates) {
		for (ItemsetZart object : context.getObjects()) {
			for (ItemsetZart candidate : candidates) {
				if (candidate.includedIn(object)) {
					candidate.increaseTransactionCount();
				}
			}
		}
	}

	public TFTableFrequent getTableFrequent() {
		return tableFrequent;
	}
	
	
}
