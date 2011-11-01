package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.multidimensionalpatterns.AlgoDimApriori;
import ca.pfv.spmf.multidimensionalsequentialpatterns.AlgoSeqDim;
import ca.pfv.spmf.multidimensionalsequentialpatterns.MDSequenceDatabase;
import ca.pfv.spmf.sequentialpatterns.AlgoFournierViger08;

/**
 * Test
 * @author Philippe Fournier-Viger
 */
public class MainTestSequentialPatternMining4 {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase(); //
		contextMDDatabase.loadFile(fileToPath("ContextMDSequence.txt"));
		contextMDDatabase.printContext();
		
		// NOTE ABOUT THE NEXT LINE: 
		// If the second boolean is true, the algorithm will use
		// CloStream instead of AprioriClose for mining frequent closed itemsets.
		// If the third boolean is true, the algorithm will use
		// CHARM instead of AprioriClose for mining frequent closed itemsets.
		// This options is offered because on some database, AprioriClose does not
		// perform very well. Other algorithms could be added.
		AlgoDimApriori algoDim = new AlgoDimApriori(true, false, false); // <-- here
		
		AlgoSeqDim algoSeqDim2 = new AlgoSeqDim();
		// Minimum absolute support = 50 %
		double minsupp = 0.50;
		
		// Apply algorithm
		AlgoFournierViger08 algoPrefixSpanHirateClustering 
		= new AlgoFournierViger08(minsupp,
				0, Long.MAX_VALUE, 0, Long.MAX_VALUE, null, true, true);  
		algoSeqDim2.runAlgorithm(contextMDDatabase, algoPrefixSpanHirateClustering, algoDim, true);
		
		// Print results
		algoSeqDim2.printStatistics(contextMDDatabase.size());
		// NOTE : IF YOU DON'T WANT TO MINE *CLOSED* MD-SEQUENCES, JUST CHANGE THE FOUR VALUES "true" for
		// "FALSE" in this example. 
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSequentialPatternMining4.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}


