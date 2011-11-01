package ca.pfv.spmf.sequential_rules.rulegrowth;
 
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * RULEGROWTH algorithm. Optimized version. Save output to file.
 * 
 * @author Philippe Fournier-Viger, 2010
 */
public class AlgoRULEGROWTH {
	// statistics
	long timeStart = 0;
	long timeEnd = 0;
	
	double minConfidence; 
	int minsuppRelative;
	
	Map<Integer,  Map<Integer, Occurence>> mapItemCount;  // item, <tid, occurence>
	
	SequenceDatabase database;
	double maxMemory = 0;
	int ruleCount;

	BufferedWriter writer = null; 

	public AlgoRULEGROWTH() {
	}

	private void checkMemory() {
		double currentMemory = ( (double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
		if(currentMemory > maxMemory){
			maxMemory = currentMemory;
		}
//		System.out.println(maxMemory);
	}
	
	public void runAlgorithm(double minSupport, double minConfidence, String input, String output) throws IOException {
		try {
			database = new SequenceDatabase(); 
//			database.loadFileKosarakFormat(input,1);
			database.loadFile(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.minsuppRelative = (int) Math.ceil(minSupport * database.size());
		runAlgorithm(input, output, minsuppRelative, minConfidence);
	}
	
	public void runAlgorithm(String input, String output, int relativeMinsup, double minConfidence) throws IOException {
		this.minConfidence = minConfidence;
		ruleCount = 0;
		if(database == null){
			try {
				database = new SequenceDatabase(); 
				database.loadFile(input);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		maxMemory = 0;
		
		writer = new BufferedWriter(new FileWriter(output)); 
		
		this.minsuppRelative =  relativeMinsup;
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}

		timeStart = System.currentTimeMillis(); // for stats

		removeItemsThatAreNotFrequent(database);	
		
		// (1) generate all rules with 1-left-itemset and 1-right-itemset
		// NOTE ITEMS THAT ARE FREQUENT IN A LIST
		List<Integer> listFrequents = new ArrayList<Integer>();
		for(Entry<Integer,Map<Integer, Occurence>> entry : mapItemCount.entrySet()){
			if(entry.getValue().size() >= minsuppRelative){
				listFrequents.add(entry.getKey());
			}
		}
		
		// FOR EACH FREQUENT ITEM WE COMPARE WITH OTHER FREQUENT ITEM TO 
		// TRY TO GENERATE A RULE 1-1.
		for(int i=0; i< listFrequents.size(); i++){
			Integer intI = listFrequents.get(i);
			Map<Integer, Occurence> occurencesI = mapItemCount.get(intI);
			Set<Integer> tidsI = occurencesI.keySet();
			
			for(int j=i+1; j< listFrequents.size(); j++){
				Integer intJ = listFrequents.get(j);
				Map<Integer,Occurence> occurencesJ = mapItemCount.get(intJ);
				Set<Integer> tidsJ = occurencesJ.keySet();
				
				// (1) Build list of common  tids  and count occurences 
				// of i before j  and  j before i.
				Set<Integer> tidsIJ = new HashSet<Integer>();
				Set<Integer> tidsJI = new HashSet<Integer>();
				for(Entry<Integer, Occurence> entryOccI : occurencesI.entrySet()){
					Occurence occJ = occurencesJ.get(entryOccI.getKey());
					if(occJ !=  null){
						if(occJ.firstItemset < entryOccI.getValue().lastItemset){
							tidsJI.add(entryOccI.getKey());
						}
						if(entryOccI.getValue().firstItemset < occJ.lastItemset){
							tidsIJ.add(entryOccI.getKey());
						}
					}
				}
				
				// (2) check if the two itemsets have enough common tids
				// if not, we don't need to generate a rule for them.
				// create rule IJ
				if(tidsIJ.size() >= minsuppRelative){
					double confIJ = ((double)tidsIJ.size()) / occurencesI.size();
					int[] itemsetI = new int[1];
					itemsetI[0]= intI;
					int[] itemsetJ = new int[1];
					itemsetJ[0]= intJ;
					if(confIJ >= minConfidence){
						saveRule(tidsIJ, confIJ, itemsetI, itemsetJ);
					}
					expandLeft(itemsetI, itemsetJ, tidsI, tidsIJ, occurencesJ);
					expandRight(itemsetI, itemsetJ, tidsI, tidsJ, tidsIJ, occurencesI, occurencesJ);
				}
					
				// create rule JI
				if(tidsJI.size() >= minsuppRelative){
					int[] itemsetI = new int[1];
					itemsetI[0]= intI;
					int[] itemsetJ = new int[1];
					itemsetJ[0]= intJ;
					double confJI = ((double)tidsJI.size()) / occurencesJ.size();
					if(confJI >= minConfidence){
						saveRule(tidsJI, confJI, itemsetJ, itemsetI);
					}
					expandRight(itemsetJ, itemsetI, tidsJ,  tidsI, tidsJI, occurencesJ, occurencesI);
					expandLeft(itemsetJ, itemsetI, tidsJ, tidsJI, occurencesI);
				}
			}
		}
		timeEnd = System.currentTimeMillis(); // for stats
		
		// close the file
		writer.close();
		
		database = null;
	}

	private void saveRule(Set<Integer> tidsIJ, double confIJ, int[] itemsetI, int[] itemsetJ) throws IOException {
		ruleCount++;
		StringBuffer buffer = new StringBuffer();
		// write itemset 1
		for(int i=0; i<itemsetI.length; i++){
			buffer.append(itemsetI[i]);
			if(i != itemsetI.length -1){
				buffer.append(",");
			}
		}
		// write separator
		buffer.append(" ==> ");
		// write itemset 2
		for(int i=0; i<itemsetJ.length; i++){
			buffer.append(itemsetJ[i]);
			if(i != itemsetJ.length -1){
				buffer.append(",");
			}
		}
		// write separator
		buffer.append("  sup= ");
		// write support
		buffer.append(tidsIJ.size());
		// write separator
		buffer.append("  conf= ");
		// write confidence
		buffer.append(confIJ);
		writer.write(buffer.toString());
		writer.newLine();
		writer.flush();
	}


	/**
	 * This method search for items for expanding left side of a rule I --> J 
	 * with any item c. This results in rules of the form I U {c} --> J. The method makes sure that:
	 *   - c  is not already included in I or J
	 *   - c appear at least minsup time in tidsIJ before last occurence of J
	 *   - c is lexically bigger than all items in I
	 * @throws IOException 
	 */
    private void expandLeft(int [] itemsetI, int[] itemsetJ, Collection<Integer> tidsI, 
    						Collection<Integer> tidsIJ, 
    						Map<Integer, Occurence> occurencesJ) throws IOException {    	
    	// map-key: item   map-value: set of tids containing the item
    	Map<Integer, Set<Integer>> frequentItemsC  = new HashMap<Integer, Set<Integer>>();  
    	
    	// we scan the sequence where I-->J appear to search for items c that we could add.
    	// for each sequence containing I-->J
    	int left = tidsIJ.size();
    	for(Integer tid : tidsIJ){
    		Sequence sequence = database.getSequences().get(tid);
			Occurence end = occurencesJ.get(tid);
			
			// for each itemset before the last occurence of J
itemLoop:	for(int k=0; k < end.lastItemset; k++){
				Itemset itemset = sequence.get(k);
				// for each item
				for(int m=0; m< itemset.size(); m++){
					Integer itemC = itemset.get(m);
					// if lexical order is not respected or c is included in the rule already.			
					if(containsLEXPlus(itemsetI, itemC) ||  containsLEX(itemsetJ, itemC)){
						continue;
					}
						
					Set<Integer> tidsItemC = frequentItemsC.get(itemC);
					// optimization for sparse datasets...
					if(tidsItemC == null){ 
						if(left < minsuppRelative){
							continue itemLoop;
						}	
					}else if(tidsItemC.size() + left < minsuppRelative){
						tidsItemC.remove(itemC);
						continue itemLoop;
					}
					if(tidsItemC == null){
						tidsItemC = new HashSet<Integer>(tidsIJ.size());
						frequentItemsC.put(itemC, tidsItemC);
					}
					tidsItemC.add(tid);				
				}
			}
			left--;
		}
    	
     	// for each item c found, we create a rule	 	
    	for(Entry<Integer, Set<Integer>> entry : frequentItemsC.entrySet()){
    		Integer itemC = entry.getKey();
    		Set<Integer> tidsIC_J = entry.getValue();
    		
    		// if the support is enough      Sup(R)  =  sup(IC -->J)
    		if(tidsIC_J.size() >= minsuppRelative){ 
    			// Calculate tids containing IC
    			Set<Integer> tidsIC = new HashSet<Integer>(tidsI.size());
    	    	for(Integer tid: tidsI){
    	    		if(mapItemCount.get(itemC).containsKey(tid)){
    	    			tidsIC.add(tid);
    	    		}
    	    	}
    			
    			// Create rule and calculate its confidence:  Conf(r) = sup(IUC -->J) /  sup(IUC)			
				double confIC_J = ((double)tidsIC_J.size()) / tidsIC.size();
				int [] itemsetIC = new int[itemsetI.length+1];
				System.arraycopy(itemsetI, 0, itemsetIC, 0, itemsetI.length);
				itemsetIC[itemsetI.length] = itemC;
				
				// if confidence is enough
				if(confIC_J >= minConfidence){
					saveRule(tidsIC_J, confIC_J, itemsetIC, itemsetJ);
				}
				// recursive call to expand left side of the rule
				expandLeft(itemsetIC, itemsetJ, tidsIC, tidsIC_J, occurencesJ);
    		}
    	}
    	checkMemory();
	}
    
	/**
	 * This method search for items for expanding left side of a rule I --> J 
	 * with any item c. This results in rules of the form I --> J U {c}. The method makes sure that:
	 *   - c  is not already included in I or J
	 *   - c appear at least minsup time in tidsIJ after the first occurence of I
	 *   - c is lexically bigger than all items in J
	 * @throws IOException 
	 */
    private void expandRight(int [] itemsetI, int []itemsetJ,
							Set<Integer> tidsI, 
    						Collection<Integer> tidsJ, 
    						Collection<Integer> tidsIJ, 
    						Map<Integer, Occurence> occurencesI,
    						Map<Integer, Occurence> occurencesJ) throws IOException {
		
    	// map-key: item   map-value: set of tids containing the item
    	Map<Integer, Set<Integer>> frequentItemsC  = new HashMap<Integer, Set<Integer>>();  
    	
    	// we scan the sequence where I-->J appear to search for items c that we could add.
    	// for each sequence containing I-->J.
    	int left = tidsIJ.size();
    	for(Integer tid : tidsIJ){
    		Sequence sequence = database.getSequences().get(tid);
			Occurence first = occurencesI.get(tid);
			
			// for each itemset after the first occurence of I
			for(int k=first.firstItemset+1; k < sequence.size(); k++){
				Itemset itemset = sequence.get(k);
				// for each item
	itemLoop:	for(int m=0; m< itemset.size(); m++){
					Integer itemC = itemset.get(m);
					// if lexical order is not respected or c is included in the rule already.			
					if(containsLEX(itemsetI, itemC) ||  containsLEXPlus(itemsetJ, itemC)){
						continue;
					}
					Set<Integer> tidsItemC = frequentItemsC.get(itemC);
					// optimization for sparse datasets...
					if(tidsItemC == null){ 
						if(left < minsuppRelative){
							continue itemLoop;
						}	
					}else if(tidsItemC.size() + left < minsuppRelative){
						tidsItemC.remove(itemC);
						continue itemLoop;
					}
					if(tidsItemC == null){
						tidsItemC = new HashSet<Integer>(tidsIJ.size());
						frequentItemsC.put(itemC, tidsItemC);
					}
					tidsItemC.add(tid);					
				}
			}
			left--;
		}
    	
    	// for each item c found, we create each a rule	 	
    	for(Entry<Integer, Set<Integer>> entry : frequentItemsC.entrySet()){
    		Integer itemC = entry.getKey();
    		Set<Integer> tidsI_JC = entry.getValue();
    		
    		// if the support is enough      Sup(r)  =  sup(I -->JUC)
    		if(tidsI_JC.size() >= minsuppRelative){  
    			Set<Integer> tidsJC = new HashSet<Integer>(tidsJ.size());
    			Map<Integer, Occurence> occurencesJC = new HashMap<Integer, Occurence>();
    			
    			for(Integer tid: tidsJ){
    				Occurence occurenceC = mapItemCount.get(itemC).get(tid);
    	    		if(occurenceC != null){
    	    			tidsJC.add(tid);
    	    			Occurence occurenceJ = occurencesJ.get(tid);
    	    			if(occurenceC.lastItemset < occurenceJ.lastItemset){
    	    				occurencesJC.put(tid, occurenceC);
    	    			}else{
    	    				occurencesJC.put(tid, occurenceJ);
    	    			}
    	    		}
    	    	}
    			// Create rule and calculate its confidence:  Conf(r) = sup(I-->JC) /  sup(I)	
				double confI_JC = ((double)tidsI_JC.size()) / tidsI.size();
				int[] itemsetJC = new int[itemsetJ.length+1];
				System.arraycopy(itemsetJ, 0, itemsetJC, 0, itemsetJ.length);
				itemsetJC[itemsetJ.length]= itemC;
				
				// if the confidence is enough
				if(confI_JC >= minConfidence){
					saveRule(tidsI_JC, confI_JC, itemsetI, itemsetJC);
				}

				expandRight(itemsetI, itemsetJC, tidsI, tidsJC, tidsI_JC, occurencesI, occurencesJC);  // occurencesJ
				expandLeft(itemsetI, itemsetJC,  tidsI, tidsI_JC, occurencesJC);  // occurencesJ
    		}
    	}
    	checkMemory();
	}

    
	/**
	 * This method calculate the frequency of each item in one database pass.
	 * Then it remove all items that are not frequent.
	 * @param database : a sequence database 
	 * @return A map such that key = item
	 *                         value = a map  where a key = tid  and a value = Occurence
	 * This map allows knowing the frequency of each item and their first and last occurence in each sequence.
	 */
	private Map<Integer, Map<Integer, Occurence>> removeItemsThatAreNotFrequent(SequenceDatabase database) {
		// (1) Count the support of each item in the database in one database pass
		mapItemCount = new HashMap<Integer, Map<Integer, Occurence>>(); // <item, Map<tid, occurence>>
		
		// for each sequence
		for(int k=0; k< database.size(); k++){
			Sequence sequence = database.getSequences().get(k);
			// for each itemset
			for(short j=0; j< sequence.getItemsets().size(); j++){
				Itemset itemset = sequence.get(j);
				// for each item
				for(int i=0; i< itemset.size(); i++){
					Integer itemI = itemset.get(i);
					Map<Integer, Occurence> occurences = mapItemCount.get(itemI);
					if(occurences == null){
						occurences = new HashMap<Integer, Occurence>();
						mapItemCount.put(itemI, occurences);
					}
					Occurence occurence = occurences.get(k);
					if(occurence == null){
						occurence = new Occurence(j, j);
						occurences.put(k, occurence);
					}else{
						occurence.lastItemset = j;
					}
				}
			}
		}
//		System.out.println("NUMBER OF DIFFERENT ITEMS : " + mapItemCount.size());
		// (2) remove all items that are not frequent from the database
		for(Sequence sequence : database.getSequences()){
			int i=0;
			while(i < sequence.getItemsets().size()){
				Itemset itemset = sequence.getItemsets().get(i);
				int j=0;
				while(j < itemset.size()){
					if( mapItemCount.get(itemset.get(j)).size() < minsuppRelative){
						itemset.getItems().remove(j);
					}else{
						j++;
					}
				}
				i++;
			}
		}
		return mapItemCount;
	}

	/**
	 * 
	 * This method checks if the item "item" is in the itemset.
	 * It asumes that items in the itemset are sorted in lexical order
	 * This version also checks that if the item "item" was added it would be the largest one
	 * according to the lexical order
	 */
	public boolean containsLEXPlus(int[] itemset, int item) {
		for(int i=0; i< itemset.length; i++){
			if(itemset[i] == item){
				return true;
			}else if(itemset[i] > item){
				return true; // <-- xxxx
			}
		}
		return false;
	}
	
	/**
	 * This method checks if the item "item" is in the itemset.
	 * It asumes that items in the itemset are sorted in lexical order
	 * @param item
	 * @return
	 */
	public boolean containsLEX(int[] itemset, int item) {
		for(int i=0; i< itemset.length; i++){
			if(itemset[i] == item){
				return true;
			}else if(itemset[i] > item){
				return false;  // <-- xxxx
			}
		}
		return false;
	}
	
    
	public boolean contains(int[] itemset, int item) {
		for(int i=0; i<itemset.length; i++){
			if(itemset[i] == item){
				return true;
			}else if(itemset[i] > item){
				return false;
			}
		}
		return false;
	}
	
	public void printStats() {
		System.out.println("=============  RULEGROWTH - STATS ========");
		System.out.println("Sequential rules count: " + ruleCount);
		System.out.println("Total time: " + (timeEnd - timeStart) + " ms");
		System.out.println("Max memory: " + maxMemory);
		System.out.println("==========================================");
	}

	public double getTotalTime(){
		return timeEnd - timeStart;
	}

	

}
