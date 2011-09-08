package ca.pfv.spmf.frequentpatterns.vme;

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
 * This is an implementation of the VME algorithm (Deng and Xu, 2011).
 * 
 * The VME algorithm finds all the ereasable itemsets from a product database.
 * 
 * Actually, this algorithms is a only slight modification of the AprioriTID algorithm.
 * 
 * I have implemented mostly as described in the paper with some modifications to make
 * it more efficient.
 * First, the authors suggested to generate all candidates of a level before 
 * removing the unereasable ones. This is inefficient. Instead, in my implementation, 
 * I check the "gain" (loss of profit) directly after generating a candidate so I can eliminate
 * them right away. 
 * Second, it is unecessary to check the subsets like the authors
 * suggest because they use a vertical representation.  
 * Third, the authors suggest to store the profit of transactions 
 * in PID List. This is not memory efficient. For implementation it is better to 
 * store the profit of each transaction only once in a hashtable.
 * 
 * @author Philippe Fournier-Viger, 2011
 */
public class AlgoVME {
	
	// variables for counting support of items
	Map<Integer, Set<Integer>> mapItemTIDs = new HashMap<Integer, Set<Integer>>();
	// variables for storing the profit of each transaction
	Map<Integer, Integer> mapTransactionProfit = new HashMap<Integer, Integer>();
	
	int minSuppRelative;

	long startTimestamp = 0;
	
	double maxProfitLoss =0;
	double overallProfit = 0;
	
	private long endTimeStamp;
	private int erasableItemsetCount = 0;

	BufferedWriter writer = null;
	
	public AlgoVME() {
		
	}

	public void runAlgorithm(String input, String output, double threshold) throws NumberFormatException, IOException {
		startTimestamp = System.currentTimeMillis();
		
		// create writer
		writer = new BufferedWriter(new FileWriter(output)); 
		erasableItemsetCount = 0;
		
		overallProfit = 0;
		// Scan the database one time to get the overall profit
		// MODIFICATION: at the same time we record the profit of each transaction (product).
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		int i=0;
		while( ((line = reader.readLine())!= null)){ // for each transaction
			String[] lineSplited = line.split(" ");
			int profit = Integer.parseInt(lineSplited[0]);
			overallProfit += profit;
			mapTransactionProfit.put(i++, profit);
			
		}
		reader.close();
		
		// Calculate max profit loss
		maxProfitLoss  = overallProfit * threshold;
		
		// Scan the database to find erasable itemset of size 1 and their tid list.
		reader = new BufferedReader(new FileReader(input));
		i=0;
		while( ((line = reader.readLine())!= null)){ // for each transaction
			String[] lineSplited = line.split(" ");
			for(int j=1; j< lineSplited.length; j++){
				int item = Integer.parseInt(lineSplited[j]);
				Set<Integer> tids = mapItemTIDs.get(item);
				if(tids == null){
					tids = new HashSet<Integer>();
					mapItemTIDs.put(item, tids);
				}
				tids.add(i);
			}
			i++;
		}
		reader.close();
		
		// Find erasable of size 1 and delete items that are not erasable from memory
		List<Itemset> level = new ArrayList<Itemset>();

		Iterator<Entry<Integer, Set<Integer>>> iterator = mapItemTIDs.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, Set<Integer>> entry = (Map.Entry<Integer, Set<Integer>>) iterator.next();
			int loss =0;
			for(Integer tid : entry.getValue()){
				loss += mapTransactionProfit.get(tid);
			}
			if(loss <= maxProfitLoss){
				Itemset itemset = new Itemset();
				itemset.addItem(entry.getKey());
				itemset.setTransactioncount(mapItemTIDs.get(entry.getKey()));
				level.add(itemset);
				saveItemsetToFile(itemset, loss);
			}else{
				iterator.remove();  // not erasable so we remove from memory.
			}
		}
		
		// sort items
		Collections.sort(level, new Comparator<Itemset>(){
			public int compare(Itemset o1, Itemset o2) {
				return o1.get(0) - o2.get(0);
			}
		});
		
		// Generate candidates with size k = 1 (all itemsets of size 1)

		// While the level is not empty
		while (!level.isEmpty()) {
			// Generate candidates of size K
			level = generateCandidateSizeK(level);
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

				// create the union of tids
				Set<Integer> unionTIDS = new HashSet<Integer>(itemset1.getTransactionsIds());
				unionTIDS.addAll(itemset2.getTransactionsIds());
				
				// calculate loss
				int loss =0;
				for(Integer tid : unionTIDS){
					loss += mapTransactionProfit.get(tid);
				}
				if(loss <= maxProfitLoss){
					// Create a new candidate by combining itemset1 and itemset2
					Itemset candidate = new Itemset();
					for(int k=0; k < itemset1.size(); k++){
						candidate.addItem(itemset1.get(k));
					}
					candidate.addItem(missing);
					candidate.setTransactioncount(unionTIDS);
					candidates.add(candidate);
					saveItemsetToFile(candidate, loss);
				}
			}
		}
		return candidates;
	}
	
	public void saveItemsetToFile(Itemset itemset, int loss) throws IOException{
		writer.write("" + loss + " " + itemset.toString());
		writer.newLine();
		erasableItemsetCount++;
	}
	
	public void printStats() {
		System.out
				.println("=============  VME - STATS =============");
		long temps = endTimeStamp - startTimestamp;
//		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println("Overall profit: " + overallProfit);
		System.out.println("Maximum profit loss (over. profit x treshold): " + maxProfitLoss);
		System.out.println(" Frequent itemsets count : " + erasableItemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
}
