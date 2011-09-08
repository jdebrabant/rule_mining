package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings.AlgoPrefixSpanStrings;
import ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings.SequenceDatabase;


/**
 * Class for testing the PrefixSpan algorithm with Strings as input instead of integers.
 * @author Philippe Fournier-Viger
 */
public class MainTestPrefixSpanWithStrings {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpanStrings.txt"));
		// print the database to console
		sequenceDatabase.print();
		
		// Create an instance of the algorithm with minsup = 50 %
		AlgoPrefixSpanStrings algo = new AlgoPrefixSpanStrings(0.50); 
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPrefixSpanWithStrings.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}