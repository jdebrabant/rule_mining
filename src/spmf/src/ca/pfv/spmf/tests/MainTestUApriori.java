package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.uapriori.AlgoUApriori;
import ca.pfv.spmf.frequentpatterns.uapriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.uapriori.Itemsets;

/**
 * Class to test the U-APRIORI algorithm .
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestUApriori {

	public static void main(String [] arg){
		// Loading the binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextUncertain.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// Applying the U-APRIORI algorithm
		AlgoUApriori Uapriori = new AlgoUApriori(context);
		Itemsets patterns = Uapriori.runAlgorithm(0.1);
		patterns.printItemsets();
		Uapriori.printStats();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestUApriori.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
