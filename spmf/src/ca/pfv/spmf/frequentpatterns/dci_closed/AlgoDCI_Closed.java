package ca.pfv.spmf.frequentpatterns.dci_closed;

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

/**
 * This is an implementation of the "DCI_Closed" algorithm.
 * 
 * The DCI_Closed algorithm finds all closed itemsets in a transaction database. 
 * 
 * This algorithm was originally proposed in the article:
 * 
 *   Lucchese, C., Orlando, S. & Perego, Raffaele (2004), DCI_Closed: a fast and memory efficient
 *   algorithm to mine frequent closed itemsets, Proc. 2nd IEEE ICDM Workshop on Frequent Itemset
 *   Mining Implementations at ICDM 2004.
 * 
 * Implementation note:  
 *  - My implementation assumes that there is no item named "0".
 * 
 * Possible optimizations:
 *  - use a bit matrix like it is suggested in the article
 *  - remove elements from postsets and use a linkedlist for postsets.
 *  - closedset could be an array.
 *  - etc.
 *  
 * @author Philippe Fournier-Viger, 2010
 */
public class AlgoDCI_Closed {
	
	public int closedCount =0;
	public int tidCount =0;
	public int maxItemId =1;  
	
	int minSuppRelative;
	
	Map<Integer, Set<Integer>> database = null;
	
	BufferedWriter writer = null; 
	
	public AlgoDCI_Closed() {
	}

	public void runAlgorithm(String input, String output, int minsup) throws IOException {
		long startTimestamp = System.currentTimeMillis();
		closedCount=0;
		System.out.println("Running the DCI-Closed algorithm");
		
		 writer = new BufferedWriter(new FileWriter(output)); 
		
		this.minSuppRelative = minsup;

		// (1) CREATE VERTICAL DATABASE INTO MEMORY
		createVerticalDatabase(input);

		// (2) INITIAL VARIABLES FOR THE FIRST CALL TO THE "DCI_CLOSED" PROCEDURE
		List<Integer> closedset = new ArrayList<Integer>();
		Set<Integer> closedsetTIDs = new HashSet<Integer>();
		List<Integer> preset = new ArrayList<Integer>();
	 	List<Integer> postset = new ArrayList<Integer>(maxItemId);
		
		// create postset and sort it by descending order or support.
		for(int i=1; i<= maxItemId; i++){
			Set<Integer> tidset = database.get(i); 
			// we don't keep items that are not frequent!
			if(tidset != null && tidset.size() >= minSuppRelative){
				postset.add(i);
			}
		}
		// sort items by support ascending order. But use the lexicographical order if 
		// the support is the same for two items.
		Collections.sort(postset, new Comparator<Integer>(){
			public int compare(Integer item1, Integer item2) {
				int size1 = database.get(item1).size();
				int size2 = database.get(item2).size();
				if(size1 == size2){
					return (item1 < item2) ? -1 : 1;
				}
				return size1 - size2;
			}
		});
//		System.out.println(postset);
		
		// (3) CALL THE "DCI_CLOSED" RECURSIVE PROCEDURE
		dci_closed(true, closedset, closedsetTIDs, postset, preset);
		
		// print statistics
		System.out.println("========== DCI_CLOSED - STATS ============");
		System.out.println(" Number of transactions: " + tidCount );
		System.out.println(" Number of frequent closed itemsets: " + closedCount );
		System.out.println(" Total time ~: " + (System.currentTimeMillis() - startTimestamp) + " ms");
		// close the file
		writer.close();
	}
	
	/**
	 * The method "DCI_CLOSED" as described in the paper.
	 */
	private void dci_closed(boolean firstTime, List<Integer> closedset, Set<Integer> closedsetTIDs, 
			List<Integer> postset, List<Integer> preset) throws IOException {
		
		//L2: for all i in postset
		for(Integer i : postset){
			// L4 check the tidset of newgen
			Set<Integer> newgenTIDs;
			if(firstTime){
				newgenTIDs = database.get(i);
			}else{
				newgenTIDs = intersectTIDset(closedsetTIDs, database.get(i));
			}
			if(newgenTIDs.size() >= minSuppRelative){
				// L3: newgen = closedset U {i}
				List<Integer> newgen = new ArrayList<Integer>(closedset.size()+1);
				newgen.addAll(closedset);
				newgen.add(i);
				
				// L5:
				if(is_dup(newgenTIDs, preset) == false){
					// L6: ClosedsetNew = newGen
					List<Integer> closedsetNew = new ArrayList<Integer>();
					closedsetNew.addAll(newgen);
					// calculate tidset
					Set<Integer> closedsetNewTIDs = new HashSet<Integer>();
					if(firstTime){
						closedsetNewTIDs = database.get(i);
					}else{
						closedsetNewTIDs.addAll(newgenTIDs);
					}
					
					// L7 : PostsetNew = emptyset
					List<Integer> postsetNew = new ArrayList<Integer>();
					// L8 for each j in Postset such that i _ j : 
					for(Integer j : postset){
						if(smallerAccordingToTotalOrder(i, j)){
							// L9
							if(database.get(j).containsAll(newgenTIDs)){
								closedsetNew.add(j);
								// recalculate TIDS of closedsetNEW by intersection
								Set<Integer> jTIDs = database.get(j);
								Iterator<Integer> iter = closedsetNewTIDs.iterator();
								while(iter.hasNext()){
									Integer tid = iter.next();
									if(jTIDs.contains(tid) == false){
										iter.remove();
									}
								}
							}else{
								postsetNew.add(j);
							}
						}
					}
					
					// L15 : write out Closed_setNew and its support
					writeOut(closedsetNew, closedsetNewTIDs.size());
					
					// L16: recursive call
					// FIXED: we have to make a copy of preset before the recursive call
					List<Integer> presetNew = new ArrayList<Integer>(preset);
					dci_closed(false, closedsetNew, closedsetNewTIDs, postsetNew, presetNew);
					
					// L17 : Preset = Preset U {i}
					preset.add(i);
					
				}
			}	
		}
	}

	/**
	 * Check if an item is smaller than another according to the support ascending order
	 * or if the support is the same, use the lexicographical order.
	 */
	private boolean smallerAccordingToTotalOrder(Integer i, Integer j) {
		int size1 = database.get(i).size();
		int size2 = database.get(j).size();
		if(size1 == size2){
			return (i < j) ? true : false;
		}
		return size2 - size1 >0;
	}

	/**
	 * Write a frequent closed itemset that is found to the output file.
	 */
	private void writeOut(List<Integer> closedset, int support) throws IOException {
		closedCount++;
		StringBuffer buffer = new StringBuffer();
		// WRITE ITEMS
		Iterator<Integer> iterItem = closedset.iterator();
		while(iterItem.hasNext()){
			buffer.append(iterItem.next());
			if(iterItem.hasNext()){
				buffer.append(' ');
			}else{
				break;
			}
		}

		buffer.append(':');
		// WRITE SUPPORT
		buffer.append(support);
		writer.write(buffer.toString());
		writer.newLine();
	}

	/**
	 * The method "is_dup" as described in the paper.
	 */
	private boolean is_dup(Set<Integer> newgenTIDs, List<Integer> preset) {
		// L25
		for(Integer j : preset){
			// L26 :  check if tidset of newgen is included in tids of j
			if(database.get(j).containsAll(newgenTIDs)){
				return true; // FIXED: IN ORIGINAL PAPER THEY WROTE FALSE, BUT IT SHOULD BE TRUE
			}
		}
		return false;  // FIXED: IN ORIGINAL PAPER THEY WROTE TRUE, BUT IT SHOULD BE FALSE
	}


	private Set<Integer> intersectTIDset(Set<Integer> tidset1,
			Set<Integer> tidset2) {
		Set<Integer> tidset = new HashSet<Integer>();
		if(tidset1.size() > tidset2.size()){
			for(Integer tid : tidset2){
				if(tidset1.contains(tid)){
					tidset.add(tid);
				}
			}
		}else{
			for(Integer tid : tidset1){
				if(tidset2.contains(tid)){
					tidset.add(tid);
				}
			}
		}
		return tidset;
	}

	private void createVerticalDatabase(String input) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		tidCount =0;
		// the vertical database is a map: key= item  value= tidset
		database = new HashMap<Integer, Set<Integer>>();
		while( ((line = reader.readLine())!= null)){
			String[] lineSplited = line.split(" ");
			for(String itemString : lineSplited){
				Integer item = Integer.parseInt(itemString);
				Set<Integer> tidset = database.get(item);
				if(tidset == null){
					tidset = new HashSet<Integer>();
					database.put(item, tidset);
				}
				tidset.add(tidCount);
				
				if(item > maxItemId){
					maxItemId = item;
				}
			}
			tidCount++;
		}
	}
}
