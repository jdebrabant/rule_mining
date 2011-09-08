package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.hmine.AlgoHMine;

/**
 * Class to test the HMine algorithm .
 * @author Philippe Fournier-Viger, 2011.
 */
public class MainTestHMine {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("contextPasquier99.txt");  // the database
		String output = "C://frequent_itemsets.txt";  // the path for saving the frequent itemsets found
		
		int minsup = 2; // means a minsup of 2 transaction (we used a relative support)
		
		// Applying the  algorithm
		AlgoHMine algorithm = new AlgoHMine();
		algorithm.runAlgorithm(input, output, minsup);
		algorithm.printStatistics();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHMine.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
