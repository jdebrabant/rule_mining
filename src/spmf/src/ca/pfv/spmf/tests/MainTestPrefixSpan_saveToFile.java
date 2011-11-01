package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.sequentialpatterns.prefixspan_saveToFile.AlgoPrefixSpan;
import ca.pfv.spmf.sequentialpatterns.prefixspan_saveToFile.SequenceDatabase;


/**
 * Class for testing the PrefixSpan algorithm
 * @author Philippe Fournier-Viger
 */
public class MainTestPrefixSpan_saveToFile {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		// print the database to console
		sequenceDatabase.print();
		
		// Create an instance of the algorithm with minsup = 50 %
		AlgoPrefixSpan algo = new AlgoPrefixSpan(); 
		
		int minsup = 2; // we use a minimum support of 2 sequences.
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase, "/Users/mrjd225/Desktop/sequences.txt", 2);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPrefixSpan_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}