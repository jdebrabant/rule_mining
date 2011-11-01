package ca.pfv.spmf.frequentpatterns.hmine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An implementation of the HMine algorithm for mining frequent itemsets
 * from a transaction database.
 * 
 * It is based on the description in:
 * 
 *    Pei et al. (2007) H-Mine: Fast and space-preserving frequent pattern 
 *    mining in large databases. IIE Transactions, 39, 593-605.
 * 
 * I tried to follow as much as possible the description in the article for HMine(mem).
 * One observation is that the links for an item in the header table are simply what 
 * is called a "tid set" in some other algorithms, because links always point to the first
 * element of a transaction. 
 * 
 * @author Philippe Fournier-Viger, 2011
 */

public class AlgoHMine {
		
	private int minsup; 
	BufferedWriter writer = null;
	private int frequentCount;  // the number of frequent itemsets found (for statistics)
	
	double maxMemory =0;
	long startTimestamp;
	
	List<List<Integer>> database;  // in-memory database

	public AlgoHMine(){	
	}
	
	public void runAlgorithm(String input, String output, int minsup) throws IOException {
		startTimestamp = System.currentTimeMillis();
		System.out.println("Running the H-Mine algorithm");

		writer = new BufferedWriter(new FileWriter(output)); 
		frequentCount = 0;
		maxMemory =0;
		this.minsup = minsup;
		
		// (1) Scan the database and count the support of each item (in a map)
		// for this map :  key = item  value = tidset
		Map<Integer, Integer> mapItemCount = new HashMap<Integer, Integer>();
		// scan the database
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while( ((line = reader.readLine())!= null)){ // for each transaction
			String[] lineSplited = line.split(" ");
			for(String itemString : lineSplited){  // for each item in the transaction
				// increase the support count of the item
				Integer item = Integer.parseInt(itemString);
				Integer count = mapItemCount.get(item);
				if(count == null){
					mapItemCount.put(item, 1);
				}else{
					mapItemCount.put(item, ++count);
				}
			}
		}
		reader.close();
		
		// (2) Scan the database again to construct in-memory database without unfrequent items
		// and to record the tidset of each item.
		database = new ArrayList<List<Integer>>();
		// for this map, key= an item   value= tidset of the item
		Map<Integer, List<Integer>> mapItemTidset = new HashMap<Integer, List<Integer>>();
		// TODO: For optimization, we could use a treemap sorted by descending order of support.

		BufferedReader reader2 = new BufferedReader(new FileReader(input));
		String line2;
		int tid =0;
		while( ((line2 = reader2.readLine())!= null)){
			String[] lineSplited = line2.split(" ");
			List<Integer> transaction = new ArrayList<Integer>();
			for(String itemString : lineSplited){ // for each transaction
				Integer item = Integer.parseInt(itemString);
				if(mapItemCount.get(item) >= minsup){
					// add to this transaction
					transaction.add(item);
					// update tidset of item
					List<Integer> tidset = mapItemTidset.get(item);
					if(tidset == null){
						tidset = new ArrayList<Integer>();
						mapItemTidset.put(item, tidset);
					}
					tidset.add(tid);
				}
			}
			database.add(transaction);
			tid++;
		}
		reader2.close();
		
		// (3)For each frequent item, save it to file, and then
		//    call the HMINE recursive method.
		for(Entry<Integer, List<Integer>> entry : mapItemTidset.entrySet()){
			int[] prefix = new int[1]; 
			prefix[0] = entry.getKey();
			writeOut(prefix, entry.getValue().size());  // save to file
			// CALL TO THE HMINE RECURSIVE METHOD
			hmine(prefix, entry.getKey(), entry.getValue());
		}
		// close the file
		writer.close();
	}


	private void hmine(int[] prefix, Integer itemProjection, List<Integer> links) throws IOException {
		// scan the projected database and calculate the links
		Map<Integer, List<Integer>> mapItemTidset = new HashMap<Integer, List<Integer>>();
		
		for(Integer tid : links){ // for each transaction containing item
			boolean seen = false;
			for(Integer item : database.get(tid)){ // 
				if(seen){
					List<Integer> tidset = mapItemTidset.get(item);
					if(tidset == null){
						tidset = new ArrayList<Integer>();
						mapItemTidset.put(item, tidset);
					}
					tidset.add(tid);
				}
				if(itemProjection.equals(item)){
					seen = true;
				}
			}
		}
		
		// For each item having the minimum support in the projected database,
		// we will save to file, and then recursively call H-Mine.
		for(Entry<Integer, List<Integer>> entry : mapItemTidset.entrySet()){
			if(entry.getValue().size() >= minsup){
				int [] newPrefix = new int[prefix.length+1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
				newPrefix[prefix.length] = entry.getKey();
				writeOut(newPrefix, entry.getValue().size()); 
				// START RECURSION 
				hmine(newPrefix, entry.getKey(), entry.getValue());
			}
		}
		checkMemory();
	}
	
	private void checkMemory() {
		double currentMemory = ( (double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
		if(currentMemory > maxMemory){
			maxMemory = currentMemory;
		}
	}
	
	/**
	 * Write a frequent itemset to the output file.
	 */
	private void writeOut(int[] itemset, int support) throws IOException {
		frequentCount++; // for statistics
		StringBuffer buffer = new StringBuffer();
		// WRITE ITEMS
		for(int i=0; i< itemset.length; i++){
			buffer.append(itemset[i]);
			if(i != itemset.length-1){
				buffer.append(' ');
			}
		}
		buffer.append(':');
		// WRITE SUPPORT
		buffer.append(support);
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	public void printStatistics(){

		System.out.println("========== HMINE - STATS ============");
		System.out.println(" Number of frequent  itemsets: " + frequentCount );
		System.out.println(" Total time ~: " + (System.currentTimeMillis() - startTimestamp) + " ms");
		System.out.println(" Max memory:" + maxMemory);
		System.out.println("=====================================");
	}
}
