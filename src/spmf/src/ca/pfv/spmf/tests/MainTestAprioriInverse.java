package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriInverse;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Class to test the AprioriInverse algorithm.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAprioriInverse {

	public static void main(String [] arg){
		// Loading a binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextInverse.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// Applying the APRIORI-Inverse algorithm to find perfectly rare itemsets
		AlgoAprioriInverse apriori2 = new AlgoAprioriInverse(context);
		Itemsets patterns = apriori2.runAlgorithm(0.001, 0.6);
		patterns.printItemsets(context.size());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriInverse.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
