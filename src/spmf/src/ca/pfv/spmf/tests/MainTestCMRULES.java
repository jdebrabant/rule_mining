package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.sequential_rules.cmrules.AlgoCMRules;


/**
 * Class to test the the CMRULES algorithm
 * @author Philippe Fournier-Viger (Copyright 2010)
 */
public class MainTestCMRULES {

	public static void main(String [] arg) throws IOException{
		// Load database
		
		String input = fileToPath("contextPrefixSpan.txt");  // the database
		String  output = "C://rules.txt";  // the path for saving the frequent itemsets found
		double minSup = 0.75;
		double minConf = 0.50; 
 
		AlgoCMRules algo = new AlgoCMRules();
		
		// TO SET MINIMUM / MAXIMUM SIZE CONSTRAINTS you can use the following lines:
//		algo.setMinLeftSize(1);
//		algo.setMaxLeftSize(2);
//		algo.setMinRightSize(1);
//		algo.setMaxRightSize(2);
		
		algo.runAlgorithm(input, output, minSup, minConf);
		
		algo.printStats();
	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCMRULES.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}