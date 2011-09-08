package ca.pfv.spmf.frequentpatterns.dci_closed_optimized;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * This is an implementation of the "DCI_Closed" algorithm.  
 * The DCI_Closed algorithm finds all closed itemsets in a transaction database. 
 *   
 *  DCI_Closed was initially proposed in this article:
 * 
 *   Lucchese, C., Orlando, S. & Perego, Raffaele (2004), DCI_Closed: a fast and memory efficient
 *   algorithm to mine frequent closed itemsets, Proc. 2nd IEEE ICDM Workshop on Frequent Itemset
 *   Mining Implementations at ICDM 2004.
 * 
 *  Note: My implementation assumes that there is no item named "0".
 *  
 *  My implementation include several optimization:
 *   - the use of a bit matrix (as described in the TKDE paper)
 *   - projecting the database (as described in the TKDE paper)
 *   - intersecting bit by bit and stop at first different bit for inclusion check (similar to what is described in the TKDE paper, but check bits instead of words)
 * 
 * But more optimizations could be done:
 *  - intersecting word by word and stop at first different word for inclusion check (described in the TKDE paper)
 *  - reorder columns of the matrix (described in the TKDE paper)
 *  - reusing results of previous bitwise intersections (described in the TKDE paper)
 *  - changing for a breath-first DCI-like approach for dense datasets (as described in the TKDE paper)
 *  - ...
 *  - remove elements from postsets and use a linkedlist for postsets.
 *  - closedset could be an array.
 *  - etc.
 *  
 *  Some of these further optimizations would need to use a custom BitSet class
 *  instead of the BitSet class of Java, because the BitSet class of Java does not let us
 *  iterate over the words inside the BitSet directly.
 *  
 * @author Philippe Fournier-Viger, 2010
 */
public class AlgoDCI_Closed_Optimized {
	
	public int closedCount =0;
	public int maxItemId =1;  
	private int transactionCount = 0;
	
	private int minSuppRelative;
	BufferedWriter writer = null; 
	
	public AlgoDCI_Closed_Optimized() {
	}

	public void runAlgorithm(String input, String output, int minsup) throws IOException {
		long startTimestamp = System.currentTimeMillis();
		closedCount=0;
		System.out.println("Running the DCI-Closed algorithm");
		
		writer = new BufferedWriter(new FileWriter(output)); 
		
		this.minSuppRelative = minsup;
		
		// (0) SCAN TO KNOW THE DATABASE SIZE AND # OF ITEMS TO INITIALISE BIT-MATRIX
		firstScan(input);
		
		// create the bit matrix
		final BitMatrix matrix = new BitMatrix(maxItemId, transactionCount);

		// (1) CREATE VERTICAL DATABASE INTO MEMORY
		createVerticalDatabase(input, matrix);

		// (2) INITIAL VARIABLES FOR THE FIRST CALL TO THE "DCI_CLOSED" PROCEDURE
		List<Integer> closedset = new ArrayList<Integer>();
		BitSet closedsetTIDs = null;
		List<Integer> preset = new ArrayList<Integer>();
	 	List<Integer> postset = new ArrayList<Integer>(maxItemId);
		
		// create postset and sort it by descending order or support.
		for(int i=1; i<= maxItemId; i++){
			// we don't keep items that are not frequent!
			if(matrix.getSupportOfItemFirstTime(i) >= minSuppRelative){
				postset.add(i);
			}
		}
		
		// sort items by support ascending order. But use the lexicographical order if 
		// the support is the same for two items.
		Collections.sort(postset, new Comparator<Integer>(){
			public int compare(Integer item1, Integer item2) {
				if(matrix.getSupportOfItem(item1) == matrix.getSupportOfItem(item2)){
					return (item1 < item2) ? -1 : 1;
				}
				return matrix.getSupportOfItem(item1) - matrix.getSupportOfItem(item2);
			}
		});
		
		// (3) CALL THE "DCI_CLOSED" RECURSIVE PROCEDURE
		dci_closed(true, closedset, closedsetTIDs, postset, preset, matrix, matrix);
		
		// print statistics
		System.out.println("========== DCI_CLOSED - STATS ============");
		System.out.println(" Number of transactions: " + transactionCount );
		System.out.println(" Number of frequent closed itemsets: " + closedCount );
		System.out.println(" Total time ~: " + (System.currentTimeMillis() - startTimestamp) + " ms");
		// close the file
		writer.close();
	}
	
	private void firstScan(String input) throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		transactionCount =0;
		// Count the number of transaction and the number of items
		// by reading the file one time
		while( ((line = reader.readLine())!= null)){
			String[] lineSplited = line.split(" ");
			for(String itemString : lineSplited){
				Integer item = Integer.parseInt(itemString);
				if(item > maxItemId){
					maxItemId = item;
				}
			}
			transactionCount++;
		}
		reader.close();
	}

	/**
	 * The method "DCI_CLOSED" as described in the paper.
	 */
	private void dci_closed(boolean firstTime, List<Integer> closedset, BitSet bitset, 
			List<Integer> postset, List<Integer> preset, BitMatrix matrix, BitMatrix originalMatrix) throws IOException {

		//L2: for all i in postset
		for(Integer i : postset){
			// L4 check the tidset of newgen
			BitSet newgenTIDs;
			if(firstTime){
				newgenTIDs = matrix.getBitSetOf(i);
			}else{
				newgenTIDs = (BitSet)bitset.clone();
				newgenTIDs.and(matrix.getBitSetOf(i));
			}
			if(newgenTIDs.cardinality() >= minSuppRelative){
				// L3: newgen = closedset U {i}
				List<Integer> newgen = new ArrayList<Integer>(closedset.size()+1);
				newgen.addAll(closedset);
				newgen.add(i);
				
				// L5:
				if(is_dup(newgenTIDs, preset, matrix) == false){
					// L6: ClosedsetNew = newGen
					List<Integer> closedsetNew = new ArrayList<Integer>();
					closedsetNew.addAll(newgen);
					// calculate tidset
					BitSet closedsetNewTIDs = null;
					if(firstTime){
						closedsetNewTIDs = (BitSet)matrix.getBitSetOf(i).clone();
					}else{
						closedsetNewTIDs = (BitSet)newgenTIDs.clone();
					}
					
					// L7 : PostsetNew = emptyset
					List<Integer> postsetNew = new ArrayList<Integer>();
					// L8 for each j in Postset such that i _ j : 
					for(Integer j : postset){
						if(smallerAccordingToTotalOrder(i, j, originalMatrix)){
							// L9
							if(isAllContainedIn(newgenTIDs, matrix.getBitSetOf(j))){
								closedsetNew.add(j);
								// recalculate TIDS of closedsetNEW by intersection
								closedsetNewTIDs.and(matrix.getBitSetOf(j));
							}else{
								postsetNew.add(j);
							}
						}
					}
					
					// L15 : write out closedsetNew and its support
					int support = closedsetNewTIDs.cardinality();
					writeOut(closedsetNew, support);
					
					// L16: recursive call
					// FIXED: we have to make a copy of preset before the recursive call
					List<Integer> presetNew = new ArrayList<Integer>(preset);
					if(firstTime){
						// THIS IS THE "Dataset projection" optimization described in the TKDE paper.
						BitMatrix projectedMatrix = projectMatrix(matrix, closedsetNewTIDs, support);
						BitSet replacement = new BitSet(support);
						replacement.set(0, support, true);
						dci_closed(false, closedsetNew, replacement, postsetNew, presetNew, projectedMatrix, matrix);
					}else{
						dci_closed(false, closedsetNew, closedsetNewTIDs, postsetNew, presetNew, matrix, originalMatrix);
					}
					// L17 : Preset = Preset U {i}
					preset.add(i);
				}
			}	
		}
	}

	
	private BitMatrix projectMatrix(BitMatrix matrix, BitSet bitset, int projectedsize) {
		BitMatrix newMatrix = new BitMatrix(maxItemId, projectedsize);
		int newBit =0;
		for (int bit = bitset.nextSetBit(0); bit >= 0; bit = bitset.nextSetBit(bit+1)) {
			for(int item = 1; item <= maxItemId; item++){
				if(matrix.getBitSetOf(item).get(bit)){
					newMatrix.addTidForItem(item, newBit);
				}
			}
			newBit++;
		}
		return newMatrix;
	}

	/**
	 * Check if an item is smaller than another according to the support ascending order
	 * or if the support is the same, use the lexicographical order.
	 */
	private boolean smallerAccordingToTotalOrder(Integer i, Integer j, BitMatrix matrix) {
		if(matrix.getSupportOfItem(i) == matrix.getSupportOfItem(j)){
			return (i < j) ? true : false;
		}
		return matrix.getSupportOfItem(j) - matrix.getSupportOfItem(i) >0;
	}
	
//	int size1 = database.get(i).size();
//	int size2 = database.get(j).size();
//	if(size1 == size2){
//		return (i < j) ? true : false;
//	}
//	return size2 - size1 >0;

	/**
	 * Write a frequent closed itemset that is found to the output file.
	 */
	private void writeOut(List<Integer> closedset, int support) throws IOException {
//		System.out.println(closedset + "  sup: " + support);
		closedCount++;
		StringBuffer buffer = new StringBuffer();
//		buffer.append(": ");
		// WRITE ITEMS
		Iterator<Integer> iterItem = closedset.iterator();
		while(iterItem.hasNext()){
//			buffer.append(closedCount);
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
	private boolean is_dup(BitSet newgenTIDs, List<Integer> preset, BitMatrix matrix) {
		// L25
		for(Integer j : preset){
			// L26 :  check if tidset of newgen is included in tids of j	
			if(isAllContainedIn(newgenTIDs, matrix.getBitSetOf(j))){
				return true; // FIXED: IN ORIGINAL PAPER THEY WROTE FALSE, BUT IT SHOULD BE TRUE
			}
		}
		return false;  // FIXED: IN ORIGINAL PAPER THEY WROTE TRUE, BUT IT SHOULD BE FALSE
	}

	/**
	 * Checks if the TIDs set represented by bs1 is included in the TIDs set represented by bs2.
	 */
	private boolean isAllContainedIn(BitSet bs1, BitSet bs2) {
		for (int i = bs1.nextSetBit(0); i >= 0; i = bs1.nextSetBit(i+1)) {
		     if(bs2.get(i) == false){
		    	 return false;
		     }
		}
		return true;
	}

	private void createVerticalDatabase(String input, BitMatrix matrix) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		int tidCount =0;
		while( ((line = reader.readLine())!= null)){
			for(String itemString : line.split(" ")){
				matrix.addTidForItem(Integer.parseInt(itemString), tidCount);
			}
			tidCount++;
		}
		reader.close();
	}
}
