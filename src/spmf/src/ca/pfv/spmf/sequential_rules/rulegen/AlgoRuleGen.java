package ca.pfv.spmf.sequential_rules.rulegen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import ca.pfv.spmf.sequentialpatterns.prefixspan.AlgoPrefixSpan;
import ca.pfv.spmf.sequentialpatterns.prefixspan.Sequence;
import ca.pfv.spmf.sequentialpatterns.prefixspan.SequenceDatabase;
import ca.pfv.spmf.sequentialpatterns.prefixspan.Sequences;

/**
 * This is an implementation of the RuleGen algorithm proposed by Zaki et al. in the article:
 * 
 *    M. J. Zaki, “SPADE: An Efficient Algorithm for Mining Frequent Se-quences,”Machine Learning, vol. 42, no.1-2, pp. 31-60, 2001.
 * 
 * However note that instead of using the SPADE algorithm,  we use the PrefixSpan algorithm because 
 * (1) I don't have an implementation of SPADE and (2) PrefixSpan is very fast. 
 * 
 * @author Philippe Fournier-Viger, 2011
 */
public class AlgoRuleGen {
	
	// for statistics
	private long startTime;
	private long endTime;
	
	private int patternCount;
	
	double maxMemory = 0;

	BufferedWriter writer = null;

	public AlgoRuleGen() {
		
	}
	
	private void checkMemory() {
		double currentMemory = ( (double)((double)(Runtime.getRuntime().totalMemory()/1024)/1024))- ((double)((double)(Runtime.getRuntime().freeMemory()/1024)/1024));
		if(currentMemory > maxMemory){
			maxMemory = currentMemory;
		}
	}
	
	/**
	 * @throws IOException 
	 */
	public void runAlgorithm(int minsup, double minconf, String input, String output) throws IOException {
		writer = new BufferedWriter(new FileWriter(output)); 
		startTime = System.currentTimeMillis();
		
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(input);
		
		// STEP 1: Apply the PrefixSpan algorithm
		AlgoPrefixSpan algo = new AlgoPrefixSpan(); 
		Sequences patternsLists = algo.runAlgorithm(minsup, sequenceDatabase);    
//		algo.printStatistics(sequenceDatabase.size());
		
		// STEP 2: Generate the rules of the form    a ==> b, where a and b are sequential patterns 
		// such that a is a subsequence of b.
		// For each rule
		for (int i = 0; i < patternsLists.levels.size(); i++) {
			for(int j=0; j < patternsLists.getLevel(i).size(); j++){
				Sequence pattern1 = patternsLists.getLevel(i).get(j);
				
				for(int m= j+1; m < patternsLists.getLevel(i).size(); m++){
					Sequence pattern2 = patternsLists.getLevel(i).get(m);
					
					// try to generate a rule
					tryToGenerateRule(pattern1, pattern2, minconf);
					tryToGenerateRule(pattern2, pattern1, minconf);
				}
				
				// we compare with each rule that appear after in the list
				for (int k = i+1; k < patternsLists.levels.size(); k++) {
					for(int m =0; m < patternsLists.getLevel(k).size(); m++){
						Sequence pattern2 = patternsLists.getLevel(k).get(m);
						
						// try to generate a rule
						tryToGenerateRule(pattern1, pattern2, minconf);
						tryToGenerateRule(pattern2, pattern1, minconf);
					}
				}
			}
		}
		checkMemory();
		endTime = System.currentTimeMillis();
		writer.close();	
	}
	
	
	
	/**
	 *  Try to generate a rule between two sequential patterns. The rule is generated if the
	 *  pattern1 is included in pattern2 and if the confidence is high enough.
	 * @param pattern1 a sequential pattern
	 * @param pattern2 another sequential pattern
	 * @throws IOException 
	 */
	private void tryToGenerateRule(Sequence pattern1, Sequence pattern2, double minconf) throws IOException {
		// CHECK IF PATTERN1 IS CONTAINED IN PATTERN2
		//if(strictlyContains(pattern2, pattern1) == false){
		//	return;
		//}
		
		// OUPTUT THE RULE TO THE FILE 
		patternCount++;
		StringBuffer buffer = new StringBuffer();
//		
		buffer.append(pattern1.itemsetsToString());
		buffer.append(" ==> ");
		buffer.append(pattern2.itemsetsToString());
//
//		// write support
//		buffer.append("  sup= ");
//		buffer.append(pattern2.getAbsoluteSupport());
//		
//		// write confidence
//		buffer.append("  conf= ");
//		buffer.append( ((double) pattern2.getAbsoluteSupport()) / pattern1.getAbsoluteSupport());
		
		writer.write(buffer.toString());
		writer.newLine();
		
	}
	
	/**
	 * @return true if the pattern1 strictly contains pattern2.
	 */
	public boolean strictlyContains(Sequence pattern1, Sequence pattern2) {
		return strictlyContainsHelper(pattern1, pattern2, 0, 0);
	}

	public boolean strictlyContainsHelper(Sequence sequence1, Sequence sequence2, int index, int index2) {
		if(index == sequence1.size()){
			return false;
		}
		
		if(sequence1.size() - index < sequence2.size() - index2){
			return false;
		}
		
		boolean returnValue = false;
		for(int i=index; i < sequence1.size(); i++)
		{
			if(sequence1.get(i).getItems().containsAll(sequence2.get(index2).getItems())){
				// We have found the last one 
				if(sequence2.size()-1 == index2){ 
					return true;
				}
				else{
					boolean contains = strictlyContainsHelper(sequence1, sequence2, i+1, index2+1);
					if (contains){
						returnValue = true;
					}
				}
			}
		} 
		return returnValue;
	}

	public void printStats() {
		System.out
				.println("=============  SEQUENTIAL RULES - STATS =============");
		System.out.println("Sequential rules count: " + patternCount);
		System.out.println("Total time : " + (endTime - startTime) + " ms");		
		System.out.println("Max memory: " + maxMemory);
		System.out
				.println("===================================================");
	}
}
