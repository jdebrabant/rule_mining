package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.multidimensionalpatterns.AlgoDimApriori;
import ca.pfv.spmf.multidimensionalsequentialpatterns.AlgoSeqDim;
import ca.pfv.spmf.multidimensionalsequentialpatterns.MDSequenceDatabase;
import ca.pfv.spmf.sequentialpatterns.prefixspan_for_use_with_multidimensional_pattern_mining.AlgoPrefixSpanMDSPM;

/**
 * Test
 * @author Philippe Fournier-Viger
 */
public class MainTestMultiDimSequentialPatternMining {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase(); //
		contextMDDatabase.loadFile(fileToPath("ContextMDSequenceNoTime.txt"));
		contextMDDatabase.printContext();
		
		// If the second boolean is true, the algorithm will use
		// CloStream instead of AprioriClose for mining frequent closed itemsets.
		// If the third boolean is true, the algorithm will use
		// CHARM instead of AprioriClose for mining frequent closed itemsets.
		// This options is offered because on some database, AprioriClose does not
		// perform very well. Other algorithms could be added.
		AlgoDimApriori algoDim = new AlgoDimApriori(false, false, false);
		
		AlgoSeqDim algoSeqDim = new AlgoSeqDim();
		// Minimum absolute support = 50 %
		double minsupp = 0.75;
		
		// Apply algorithm
		AlgoPrefixSpanMDSPM prefixSpan = new AlgoPrefixSpanMDSPM(minsupp);  
		algoSeqDim.runAlgorithm(contextMDDatabase, prefixSpan, algoDim, false);
		
		// Print results
		algoSeqDim.printStatistics(contextMDDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMultiDimSequentialPatternMining.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}


