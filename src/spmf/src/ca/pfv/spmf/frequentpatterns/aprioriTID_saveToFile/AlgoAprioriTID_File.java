package ca.pfv.spmf.frequentpatterns.aprioriTID_saveToFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;



/**
 * This is an implementation of the AprioriTID algorithm.
 * 
 * The AprioriTID algorithm finds all the frequents itemsets and their support
 * in a binary context.
 * 
 * AprioriTID is usually faster than Apriori and produce the same result.
 * 
 * @author Philippe Faournier-Viger, 2008
 */
public class AlgoAprioriTID_File {

	protected int k; // level
	
	// variables for counting support of items
	Map<Integer, Set<Integer>> mapItemTIDS = new HashMap<Integer, Set<Integer>>();
	
	int minSuppRelative;
	
	// Special parameter to set the maximum size of itemsets to be discovered
	int maxItemsetSize = Integer.MAX_VALUE;

	long startTimestamp = 0;
	long endTimeStamp = 0;
	BufferedWriter writer = null;

	private int itemsetCount;
	private int tidcount =0;
	
	public AlgoAprioriTID_File() {
	}

	public void runAlgorithm(String input, String output, double minsup) throws NumberFormatException, IOException {
		startTimestamp = System.currentTimeMillis();
		writer = new BufferedWriter(new FileWriter(output)); 

		// (1) count the tid set of each item in the database in one database pass
		mapItemTIDS = new HashMap<Integer, Set<Integer>>(); // id item, count
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		tidcount=0;
		while( ((line = reader.readLine())!= null)){ // for each transaction
			String[] lineSplited = line.split(" ");
			for(String stringItem : lineSplited){
				int item = Integer.parseInt(stringItem);
				Set<Integer> tids = mapItemTIDS.get(item);
				if(tids == null){
					tids = new HashSet<Integer>();
					mapItemTIDS.put(item, tids);
				}
				tids.add(tidcount);
			}
			tidcount++;
		}
		reader.close();
		
		this.minSuppRelative = (int) Math.ceil(minsup * tidcount);
		
		// To build level 1, we keep only the frequent items.
		// We scan the database one time to calculate the support of each candidate.
		k=1;
		List<Itemset> level = new ArrayList<Itemset>();
		// For each item
		Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator.next();
			if(entry.getValue().size() >= minSuppRelative){ // if the item is frequent
				Integer item = entry.getKey();
				Itemset itemset = new Itemset();
				itemset.addItem(item);
				itemset.setTransactioncount(mapItemTIDS.get(item));
				level.add(itemset);
				saveItemsetToFile(itemset);
			}else{
				iterator.remove();  // if the item is not frequent we don't 
				// need to keep it into memory.
			}
		}
		
		// sort itemsets of size 1 according to lexicographical order.
		Collections.sort(level, new Comparator<Itemset>(){
			public int compare(Itemset o1, Itemset o2) {
				return o1.get(0) - o2.get(0);
			}
		});
		
		// Generate candidates with size k = 1 (all itemsets of size 1)
		k = 2;
		// While the level is not empty
		while (!level.isEmpty()  && k <= maxItemsetSize) {
			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			level = generateCandidateSizeK(level);; // We keep only the last level... 
			k++;
		}
		
		// close the file
		writer.close();
		endTimeStamp = System.currentTimeMillis();
	}

	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1) throws IOException {
		List<Itemset> candidates = new ArrayList<Itemset>();

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
				Set<Integer> list = new HashSet<Integer>();
				for(Integer val1 : itemset1.getTransactionsIds()){
					if(itemset2.getTransactionsIds().contains(val1)){
						list.add(val1);
					}
				}
		
				if(list.size() >= minSuppRelative){
					// Create a new candidate by combining itemset1 and itemset2
					Itemset candidate = new Itemset();
					for(int k=0; k < itemset1.size(); k++){
						candidate.addItem(itemset1.get(k));
					}
					candidate.addItem(missing);
					candidate.setTransactioncount(list);
					candidates.add(candidate);
					saveItemsetToFile(candidate);
				}
			}
		}
		return candidates;
	}


	public void setMaxItemsetSize(int maxItemsetSize) {
		this.maxItemsetSize = maxItemsetSize;
	}
	
	public void saveItemsetToFile(Itemset itemset) throws IOException{
		writer.write(itemset.toString() + " Support: " + itemset.getTransactionsIds().size() + " / " + tidcount + " = " + itemset.getRelativeSupport(tidcount));
		writer.newLine();
		itemsetCount++;
	}
	
	public void printStats() {
		System.out
				.println("=============  APRIORI - STATS =============");
		System.out.println(" Transactions count from database : " + tidcount);
		System.out.println(" Frequent itemsets count : " + itemsetCount);

		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)+ " ms");
		System.out
				.println("===================================================");
	}
}
