package ca.pfv.spmf.sequentialpatterns.spam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*** 
 * This is an implementation of the SPAM algorithm. 
 * 
 * The SPAM algorithm was originally described in this paper:
 * 
 *     Jay Ayres, Johannes Gehrke, Tomi Yiu, and Jason Flannick. Sequential PAttern Mining Using Bitmaps. 
 *     In Proceedings of the Eighth ACM SIGKDD International Conference on Knowledge Discovery and Data Mining. 
 *     Edmonton, Alberta, Canada, July 2002.
 * 
 * I tried to do what is indicated in that paper but some optimizations are not described with enough details in the paper.
 * So my implementation does not include these optimizations for example:
 * - lookup tables for bitmaps
 * - compression of bitmaps.
 * 
 * Moreover, in this implementation each sequence is represented by a fixed number of bits (32 bits) instead of 
 * a variable number of bits. That means that I assume that sequences do not contains more than 32 itemsets. 
 * If you want to work with longer sequences, you could modify the variable BIT_PER_SECTION in the Bitmap.java file.
 * 
 * @author Philippe Fournier-Viger, 2011
 **/

public class AlgoSPAM{
		
	// for statistics
	private long startTime;
	private long endTime;
	private int patternCount;
	double maxMemory = 0;
	
	// minsup
	private int minsup = 0;

	BufferedWriter writer = null;
	
	// Vertical database
	Map<Integer, Bitmap> verticalDB = new HashMap<Integer, Bitmap>();
		
	public AlgoSPAM(){
	}

	public void runAlgorithm(String input, String outputFilePath, int minsup) throws IOException {
		writer = new BufferedWriter(new FileWriter(outputFilePath)); 
		patternCount =0;
		maxMemory = 0;
		this.minsup = minsup;
		
		startTime = System.currentTimeMillis();
		spam(input);
		endTime = System.currentTimeMillis();
		writer.close();
	}
	
	/**
	 * @param contexte The initial context.
	 * @throws IOException 
	 */
	private void spam(String input) throws IOException{
		verticalDB = new HashMap<Integer, Bitmap>();
		
		// STEP1: SCAN THE DATABASE TO CREATE THE BITMAP VERTICAL DATABASE REPRESENTATION
		try {
			FileInputStream fin = new FileInputStream(new File(input));
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
			String thisLine;
			int sid =0;
			while ((thisLine = reader.readLine()) != null) {
				int tid =0;
				// for each sequence
				for(String integer:  thisLine.split(" ")){
					if(integer.equals("-1")){ // indicate the end of an itemset
						tid++;
					}else if(integer.equals("-2")){ // indicate the end of a sequence
//						determineSection(bitindex - previousBitIndex);  // register the sequence length for the bitmap
						sid++;
					}else{  // indicate an item
						// Get the bitmap for this item. If none, create one.
						Integer item = Integer.parseInt(integer);
						Bitmap bitmapItem = verticalDB.get(item);
						if(bitmapItem == null){
							bitmapItem = new Bitmap();
							verticalDB.put(item, bitmapItem);
						}
						// Register the bit in the bitmap for this item
						bitmapItem.registerBit(sid, tid);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// STEP2: REMOVE INFREQUENT ITEMS FROM THE DATABASE BECAUSE THEY WILL NOT APPEAR IN ANY FREQUENT SEQUENTIAL PATTERNS
		List<Integer> frequentItems = new ArrayList<Integer>();
		Iterator<Entry<Integer, Bitmap>> iter = verticalDB.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, Bitmap> entry = (Map.Entry<Integer, Bitmap>) iter.next();
			if(entry.getValue().getSupport() < minsup){
//				System.out.println(entry.getKey() + " has not the support" + entry.getValue().toString());
				iter.remove();
			}else{
				savePattern(entry.getKey(), entry.getValue());
				frequentItems.add(entry.getKey());
			}
		}
		
		// STEP3: PERFORM THE DEPTH FIRST SEARCH!
		
		for(Entry<Integer, Bitmap> entry: verticalDB.entrySet()){
			Prefix prefix = new Prefix();
			prefix.addItemset(new Itemset(entry.getKey()));
			dfsPruning(prefix, entry.getValue(), frequentItems, frequentItems, entry.getKey());
		}
	}
	

	private void dfsPruning(Prefix prefix, Bitmap prefixBitmap, List<Integer> sn, List<Integer> in, int hasToBeGreaterThanForIStep) throws IOException {
		//  ======  S-STEPS ======
		List<Integer> sTemp = new ArrayList<Integer>();
		List<Bitmap> sTempBitmaps = new ArrayList<Bitmap>();
		
		for(Integer i : sn){
			Bitmap newBitmap = prefixBitmap.createNewBitmapSStep(verticalDB.get(i));
			if(newBitmap.getSupport() >= minsup){
				sTemp.add(i);
				sTempBitmaps.add(newBitmap);
			}
		}
		for(int k=0; k < sTemp.size(); k++){
			int item = sTemp.get(k);
			// create the new prefix
			Prefix prefixSStep = prefix.cloneSequence();
			prefixSStep.addItemset(new Itemset(item));
			// create the new bitmap
			Bitmap newBitmap = sTempBitmaps.get(k);

			savePattern(prefixSStep, newBitmap);
			dfsPruning(prefixSStep, newBitmap, sTemp, sTemp, item);
		}
		
		// ========  I STEPS =======
		List<Integer> iTemp = new ArrayList<Integer>();
		List<Bitmap> iTempBitmaps = new ArrayList<Bitmap>();
		
		for(Integer i : in){
			if(i > hasToBeGreaterThanForIStep){
				Bitmap newBitmap = prefixBitmap.createNewBitmapIStep(verticalDB.get(i));
				if(newBitmap.getSupport() >= minsup){
					iTemp.add(i);
					iTempBitmaps.add(newBitmap);
				}
			}
		}
		for(int k=0; k < iTemp.size(); k++){
			int item = iTemp.get(k);
			// create the new prefix
			Prefix prefixIStep = prefix.cloneSequence();
			prefixIStep.getItemsets().get(prefixIStep.size()-1).addItem(item);
			// create the new bitmap
			Bitmap newBitmap = iTempBitmaps.get(k);
			
			savePattern(prefixIStep, newBitmap);
			dfsPruning(prefixIStep, newBitmap, sTemp, iTemp, item);
		}	
		
		checkMemory();
	}

	private void savePattern(Integer item, Bitmap bitmap) throws IOException {
		patternCount++;
		StringBuffer r = new StringBuffer("");
		r.append(item);
		r.append(" -1 ");
		r.append("SUP: ");
		r.append(bitmap.getSupport());
		writer.write(r.toString());
		writer.newLine();
	}
	
	private void savePattern(Prefix prefix, Bitmap bitmap) throws IOException {
		patternCount++;
		
		StringBuffer r = new StringBuffer("");
		for(Itemset itemset : prefix.getItemsets()){
//			r.append('(');
			for(Integer item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append("-1 ");
		}

		r.append("SUP: ");
		r.append(bitmap.getSupport());
		
		writer.write(r.toString());
		writer.newLine();
	}

	

	private void checkMemory() {
		double currentMemory = ( (double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
		if(currentMemory > maxMemory){
			maxMemory = currentMemory;
		}
	}

	public void printStatistics() {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : " + patternCount);
		r.append('\n');
		r.append(" Max memory (mb) : " );
		r.append(maxMemory);
		r.append(patternCount);
		r.append('\n');
		r.append("===================================================\n");
		System.out.println(r.toString());
	}


}
