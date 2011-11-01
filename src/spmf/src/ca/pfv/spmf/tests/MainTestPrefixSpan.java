package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.sequentialpatterns.prefixspan.AlgoPrefixSpan;
import ca.pfv.spmf.sequentialpatterns.prefixspan.SequenceDatabase;


/**
 * Class for testing the PrefixSpan algorithm
 * @author Philippe Fournier-Viger
 */
public class MainTestPrefixSpan {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		// print the database to console
		sequenceDatabase.print();
		
		// Create an instance of the algorithm 
		AlgoPrefixSpan algo = new AlgoPrefixSpan(); 
		
		// execute the algorithm with minsup = 50 %
		algo.runAlgorithm(sequenceDatabase, 0.5);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPrefixSpan.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}