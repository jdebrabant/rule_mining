package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.sequential_rules.rulegen.AlgoRuleGen;

/**
 * This is an implementation of the RuleGen algorithm proposed by Zaki et al. in the article:
 * 
 *    M. J. Zaki, “SPADE: An Efficient Algorithm for Mining Frequent Se-quences,”Machine Learning, vol. 42, no.1-2, pp. 31-60, 2001.
 * 
 * However note that instead of using the SPADE algorithm,  we use the PrefixSpan algorithm because 
 * (1) I don't have an implementation of SPADE and (2) PrefixSpan is very fast. 
 * 
 * @author Philippe Fournier-Viger
 */
public class MainTestRuleGen {

	public static void main(String [] arg) throws IOException{    

		String input = fileToPath("contextPrefixSpan.txt");  // the database
		String output = "/Users/mrjd225/Desktop/rules.txt";  // the path for saving the frequent itemsets found
		
		int minsup = 3; // we use a minimum support of 3 sequences.
		double minconf = 0.75; // we use a minimum confidence of 50 %.
		
		// STEP 2: Generate the sequential rules with the RuleGen algorithm
		AlgoRuleGen rulegen = new AlgoRuleGen();
		rulegen.runAlgorithm(minsup, minconf, input, output);
		
		rulegen.printStats();
		
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestRuleGen.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}