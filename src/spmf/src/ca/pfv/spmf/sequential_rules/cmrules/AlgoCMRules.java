package ca.pfv.spmf.sequential_rules.cmrules;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.general.datastructures.triangularmatrix.TriangularMatrix;

/**
 * The CMRules algorithm for mining sequential rules common to several sequences.
 * 
 * This algorithm is described in:
 * 
 * Fournier-Viger, P., Faghihi, U., Nkambou, R. & Mephu Nguifo, E. (2010). 
 * CMRules: An Efficient Algorithm for Mining Sequential Rules Common to Several Sequences. 
 * Proceedings of the 23th International Florida Artificial Intelligence Research Society Conference 
 * (FLAIRS 2010). AAAI press. 
 * 
 * @author Philippe Fournier-Viger, 2009
 */

public class AlgoCMRules {
	
//	Rules rules =  new Rules("All sequential rules");
	
	// statistics
//	public int sequentialRulesCount = 0;
	public int associationRulesCount = 0 ;
	long timeStart = 0;
	long timeEnd = 0;
	public long timeEndConvert = 0;
	public long timeEndApriori = 0;
	public long timeEndAgrawal = 0;
	public long timeEndSequentialMeasures = 0;
	public long timeBeginCalculateSequentialMeasures = 0;
	public long timeEndPreprocessing = 0;
	
	double maxMemory = 0;

	public int minCSupRelative = 0;
	public double minSeqConfidence;
	int maxItemId = 0;
	Map<Integer, Set<Integer>> mapItemCount = new HashMap<Integer, Set<Integer>>();
	List<Integer> listFrequentsSize1 = new ArrayList<Integer>();
	SequenceDatabase sequences;
	
	private Itemsets patterns;
	
	private TriangularMatrix matrix;
	
	// Special parameter to set the size of rules to be discovered
	int minLeftSize = 0;  // min size of left part of the rule
	int maxLeftSize = 500; // max size of left part of the rule
	int minRightSize = 0; // min  size of right part of the rule
	int maxRightSize = 500; // max size of right part of the rule
	
	int ruleCount;

	BufferedWriter writer = null;

	public AlgoCMRules() {
		
	}
	
	private void checkMemory() {
		double currentMemory = ( (double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
		if(currentMemory > maxMemory){
			maxMemory = currentMemory;
		}
	}
	
	/**
	 * For this method, the minsup is a percentage value (ex.: 0.05 =  5 % of all sequences in the database)
	 * @throws IOException 
	 */
	public void runAlgorithm(String input, String output, double absoluteMinSupport, double minConfidence) throws IOException {
		sequences = new SequenceDatabase();
		sequences.loadFile(input);
		
		this.minCSupRelative = (int) Math.ceil(absoluteMinSupport * sequences.size());
		runAlgorithm(minCSupRelative, minConfidence, input, output);
	}

	/**
	 * For this method, the minsup is a number of sequences (ex.: 5 =  5 sequences of the database)
	 * @throws IOException 
	 */
	public void runAlgorithm(int relativeSupport, double minSeqConfidence, String input, String output) throws IOException {
		maxMemory = 0;

		this.minSeqConfidence = minSeqConfidence;
		this.minCSupRelative = relativeSupport;
		
		if(this.minCSupRelative == 0){ // protection
			this.minCSupRelative = 1;
		}
		
		if(sequences == null){
			sequences = new SequenceDatabase();
			sequences.loadFile(input);
		}
		
		writer = new BufferedWriter(new FileWriter(output)); 
		
		timeStart = System.currentTimeMillis(); // for stats

		// STEP 0 : Pre-processing
		System.out.println("STEP 0");
		removeItemsThatAreNotFrequent(sequences);
		
		// NOTE ITEMS THAT ARE FREQUENT IN A LIST THAT IS LEXICOGRAPHICALLY ORDERED 
		for(int i=0; i<= maxItemId; i++){
			if(mapItemCount.get(i) != null && mapItemCount.get(i).size() >= minCSupRelative){
				listFrequentsSize1.add(i);
			}
		}
		
		Collections.sort(listFrequentsSize1);
		
		timeEndPreprocessing = System.currentTimeMillis(); // for stats
		
		//STEP 1 : Transform sequence database in a binary context
		//(1.a)
		Context context = convert(sequences);
		
		// (1.b) create the triangular matrix for counting the support of itemsets of size 2
		// for optimization purposes.
//		matrix = new TriangularMatrix(maxItemId+1);
		// for each transaction, take each itemset of size 2,
//		// and update the triangular matrix.
//		for(Itemset itemset : context.getObjects()){
//			Object[] array = itemset.getItems().toArray();
//			for(int i=0; i< itemset.size(); i++){
//				Integer itemI = (Integer) array[i];
//				for(int j=i+1; j< itemset.size(); j++){
//					Integer itemJ = (Integer) array[j];
//					// update the matrix
//					matrix.incrementCount(itemI, itemJ);
//				}
//			}
//		}
//		
		timeEndConvert = System.currentTimeMillis(); // for stats
		
		// STEP 2: Applying the APRIORI algorithm to find frequent itemsets
		System.out.println("STEP2");
		AlgoAprioriTID apriori = new AlgoAprioriTID(context, matrix);
		apriori.setMaxItemsetSize(maxLeftSize +  maxRightSize);  // ss
		patterns = apriori.runAlgorithm(minCSupRelative, listFrequentsSize1, mapItemCount);
		checkMemory();
		
		timeEndApriori = System.currentTimeMillis(); // for stats
 		
		
		System.out.println("STEP3 " + patterns.getItemsetsCount());
  		// STEP 3: Generate all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		runAlgorithm(patterns);
		checkMemory();

		timeEndAgrawal = System.currentTimeMillis(); // for stats
		
		timeEnd = System.currentTimeMillis(); // for stats
		sequences = null;
		writer.close();
	}


	private Map<Integer, Set<Integer>> removeItemsThatAreNotFrequent(SequenceDatabase sequences) {
		// (1) count the support of each item in the database in one database pass
		mapItemCount = new HashMap<Integer, Set<Integer>>(); // id item, count
		
		for(Sequence sequence : sequences.getSequences()){
			for(Itemset itemset : sequence.getItemsets()){
				for(int i=0; i< itemset.size(); i++){
					Set<Integer> ids = mapItemCount.get(itemset.get(i));
					if(ids == null){
						ids = new HashSet<Integer>();
						mapItemCount.put(itemset.get(i), ids);
						if(itemset.get(i) > maxItemId){
							maxItemId = itemset.get(i);
						}
					}
					ids.add(sequence.getId());
				}
			}
		}
		System.out.println("NUMBER OF DIFFERENT ITEMS : " + mapItemCount.size());
		// (2) remove all items that are not frequent from the database
		
		for(Sequence sequence : sequences.getSequences()){
			int i=0;
			while(i < sequence.getItemsets().size()){
				Itemset itemset = sequence.getItemsets().get(i);
				int j=0;
				while(j < itemset.size()){
					double count = mapItemCount.get(itemset.get(j)).size();
					
					if(count < minCSupRelative){
						itemset.getItems().remove(j);
					}else{
						j++;
					}
				}
				if(itemset.size() == 0){
					sequence.getItemsets().remove(i);
				}else{
					i++;
				}
			}
		}
		return mapItemCount;
	}

	private void calculateSequentialMeasures( Rule rule, Sequence sequence) {
		Set<Integer> setAlreadySeen = new HashSet<Integer>(rule.getItemset1().size() * 3); // could be replaced with a flag on items
		int i=0;
		firstpass:{
			for(; i< sequence.getItemsets().size(); i++){ // TO MATCH LEFT PART OF THE RULE
				int j=0;
				Itemset itemset = sequence.get(i);
				for(; j< itemset.getItems().size(); j++){ // FOR EACH ITEM OF THIS SEQUENCE
					int item = itemset.get(j);
					if(rule.getItemset1().contains(item)){ // left part of rule
						setAlreadySeen.add(item);
						if(setAlreadySeen.size() == rule.getItemset1().size()){
							 break firstpass;
						}
					}
				 }
			}
		}
		
		i++; // go to next itemset...
		setAlreadySeen.clear();
		for(; i< sequence.getItemsets().size(); i++){ // TO MATCH RIGHT PART OF THE RULE
			int j=0;
			Itemset itemset = sequence.get(i);
			for(; j< itemset.getItems().size(); j++){ // FOR EACH ITEM OF THIS SEQUENCE
				int item = itemset.get(j);
				if(rule.getItemset2().contains(item)){ // right part of rule
					setAlreadySeen.add(item);
					if(setAlreadySeen.size() == rule.getItemset2().size()){
						rule.sequentialTransactionCount++;
						return;
					}
				 }
			 }
		 }
	}

	private Context convert(SequenceDatabase sequences) {
		Context context = new Context();
		for(Sequence sequence : sequences.getSequences()){
			Itemset itemsetContext = new Itemset();
			for(Itemset itemset : sequence.getItemsets()){
//				for(Integer item : itemset.getItems()){
//					itemsetContext.addItemOrderedWithNoDuplicate(item); // could maybe be optimized, but not enough important.
//				}
				itemsetContext.getItems().addAll(itemset.getItems());
			}
			context.addItemset(itemsetContext);
		}
		return context;
	}

	public void printStats() {
		System.out.println("=============  SEQUENTIAL RULES - STATS =============");
		System.out.println("Association rules count: " + associationRulesCount);
		System.out.println("Sequential rules count: " + ruleCount);
		System.out.println("Total time : " + (timeEnd - timeStart) + " ms");
		System.out.println("Max memory: " + maxMemory);
		System.out
				.println("===================================================");
	}
	
	public void checkRule(Rule rule) throws IOException{
		associationRulesCount++;
		for(Integer seqId : rule.getItemset1().getTransactionsIds()){ // FOR EACH SEQUENCE that is relevant for this rule..
			calculateSequentialMeasures(rule, sequences.getSequences().get(seqId));
		}

		if(rule.sequentialTransactionCount >= minCSupRelative
				&& rule.getSequentialConfidence() >= minSeqConfidence){
			saveRule(rule);
		}
	}
	
	//************************************************  AGRAWAL
	
	public void runAlgorithm(Itemsets patterns) throws IOException {
		
		//For each frequent itemset of size >=2
		for(int k=2; k< patterns.getLevels().size(); k++){
			for(Itemset lk : patterns.getLevels().get(k)){
				// create H1
				Set<Itemset> H1 = new HashSet<Itemset>();
				for(Itemset itemsetSize1 : patterns.getLevels().get(1)){
					if(lk.contains(itemsetSize1.getItems().get(0))){
						H1.add(itemsetSize1);
					}
				}
//				lk.print(); // DEBUG
//				System.out.println(); // DEBUG
				
				/// ================ I ADDED THIS BECAUSE THE ALGORITHM AS DESCRIBED BY AGRAWAL94
				/// ================ DID NOT GENERATE ALL  THE ASSOCIATION RULES
				Set<Itemset> H1_for_recursion  = new HashSet<Itemset>();
				for(Itemset hm_P_1 : H1){
					
					Itemset itemset_Lk_minus_hm_P_1 = lk.cloneItemSetMinusAnItemset(hm_P_1);

					// double conf = supp(lk)  / supp (lk - hm+1)
					calculateSupport(itemset_Lk_minus_hm_P_1);   //  THIS COULD BE OPTIMIZED ? OR DONE ANOTHER WAY ?
					double conf = ((double)lk.getAbsoluteSupport()) / ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport());
					
					if(conf >= minSeqConfidence){
						int leftsize = lk.size() - 1;
						if(leftsize <= maxLeftSize && leftsize >= minLeftSize && 1 >= minRightSize && 1 <= maxRightSize){
							Rule rule = new Rule(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
							checkRule(rule);
						}
						if(1 != maxRightSize && leftsize != minLeftSize){
							H1_for_recursion.add(hm_P_1);// for recursion
						}
					}
				}
				// ================ END OF WHAT I HAVE ADDED

				// call apGenRules
				if(1 != maxRightSize && lk.size() - 1 != minLeftSize){
					apGenrules(k, 1, lk, H1_for_recursion);
				}
			}
		}
	}
	
	
	private void saveRule(Rule rule) throws IOException {
		ruleCount++;
		StringBuffer buffer = new StringBuffer();
		// write itemset 1
		for(int i=0; i<rule.getItemset1().size(); i++){
			buffer.append(rule.getItemset1().get(i));
			if(i != rule.getItemset1().size() -1){
				buffer.append(",");
			}
		}
		// write separator
		buffer.append(" ==> ");
		// write itemset 2
		for(int i=0; i<rule.getItemset2().size(); i++){
			buffer.append(rule.getItemset2().get(i));
			if(i != rule.getItemset2().size() -1){
				buffer.append(",");
			}
		}
		// write separator
		buffer.append("  sup= ");
		// write support
		buffer.append(rule.getAbsoluteSeqSupport());
		// write separator
		buffer.append("  conf= ");
		// write confidence
		buffer.append(rule.getConfidence());
		writer.write(buffer.toString());
		writer.newLine();
	}

	private void apGenrules(int k, int m, Itemset lk, Set<Itemset> Hm) throws IOException {
//		System.out.println(" " + lk.toString() + "  " + Hm.toString());
		if(k > m+1){
			int leftsize = lk.size() - (1 + m);
			
			Set<Itemset> Hm_plus_1 = generateCandidateSizeK(Hm);
			Set<Itemset> Hm_plus_1_for_recursion = new HashSet<Itemset>();
			for(Itemset hm_P_1 : Hm_plus_1){
				
				Itemset itemset_Lk_minus_hm_P_1 = lk.cloneItemSetMinusAnItemset(hm_P_1);

				calculateSupport(itemset_Lk_minus_hm_P_1);   // THIS COULD BE DONE ANOTHER WAY ?
				                                             // IT COULD PERHAPS BE IMPROVED....
				double conf = ((double)lk.getAbsoluteSupport()) / ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport());
				
				if(conf >= minSeqConfidence){
					if(leftsize <= maxLeftSize && leftsize >= minLeftSize && m+1 >= minRightSize && m+1 <= maxRightSize){
						Rule rule = new Rule(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
						checkRule(rule);
					}
					if(1+m != maxRightSize && leftsize != minLeftSize){
						Hm_plus_1_for_recursion.add(hm_P_1);
					}
				}
			}
			if(1+m != maxRightSize && leftsize != minLeftSize){
				apGenrules(k, m+1, lk, Hm_plus_1_for_recursion); 
			}
		}
		checkMemory();
	}

	private void calculateSupport(Itemset itemset_Lk_minus_hm_P_1) {
		for(Itemset itemset : patterns.getLevels().get(itemset_Lk_minus_hm_P_1.size())){
			if(itemset.allTheSame(itemset_Lk_minus_hm_P_1)){
				itemset_Lk_minus_hm_P_1.setTransactioncount(itemset.getTransactionsIds());
				return;
			}
		}
	}

	protected Set<Itemset> generateCandidateSizeK(Set<Itemset> levelK_1) {
		Set<Itemset> candidates = new HashSet<Itemset>();

		// For each itemset I1 and I2 of level k-1
		for(Itemset itemset1 : levelK_1){
			for(Itemset itemset2 : levelK_1){
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				Integer missing = itemset1.allTheSameExceptLastItem(itemset2);
				if(missing != null ){
					// Create a new candidate by combining itemset1 and itemset2
					Itemset candidate = new Itemset();
					for(Integer item : itemset1.getItems()){
						if(item > missing && missing != null){
							candidate.addItem(missing);
							missing = null;
						}
						candidate.addItem(item);
					}
					if(missing != null){
						candidate.addItem(missing);
					}
//					System.out.println(" " + itemset1.toString() + " + " + itemset2.toString() + " = " + candidate.toString());
	
					// The candidate is tested to see if its subsets of size k-1 are included in
					// level k-1 (they are frequent).
					if(allSubsetsOfSizeK_1AreFrequent(candidate,levelK_1)){
						candidates.add(candidate);
					}
				}
			}
		}
		return candidates;
	}
	
	protected boolean allSubsetsOfSizeK_1AreFrequent(Itemset candidate, Set<Itemset> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		if(candidate.size() == 1){
			return true;
		}
		for(Integer item : candidate.getItems()){
			Itemset subset = candidate.cloneItemSetMinusOneItem(item);
			boolean found = false;
			for(Itemset itemset : levelK_1){
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

	public void setMinLeftSize(int minLeftSize) {
		this.minLeftSize = minLeftSize;
	}

	public void setMaxLeftSize(int maxLeftSize) {
		this.maxLeftSize = maxLeftSize;
	}

	public void setMinRightSize(int minRightSize) {
		this.minRightSize = minRightSize;
	}

	public void setMaxRightSize(int maxRightSize) {
		this.maxRightSize = maxRightSize;
	}
}
