package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.sequentialpatterns.spam.AlgoSPAM;


/**
 * Class for testing the SPAM algorithm
 * @author Philippe Fournier-Viger
 */
public class MainTestSPAM {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		String input = fileToPath("contextPrefixSpan.txt");
		String output = "C://sequential_patterns_SPAM.txt";
		
		// Create an instance of the algorithm 
		AlgoSPAM algo = new AlgoSPAM(); 
		
		// execute the algorithm with minsup = 2 sequences  (50 %)
		algo.runAlgorithm(input, output, 2);    
		algo.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSPAM.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}