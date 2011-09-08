package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriClose;
import ca.pfv.spmf.frequentpatterns.apriori.AlgoPseudoClosedItemsets;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Class to test the class "AlgoGenIGBClosed".
 * 
 * @author Philippe Fournier-Viger, 2008
 */
public class MainTestPseudoClosedItemsets {

	public static void main(String [] arg){
		// Load binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextPasquier99.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// Apply the APRIORI-CLOSE algorithm to find frequent closed itemsets
		AlgoAprioriClose apriori2 = new AlgoAprioriClose(context);
		Itemsets frequents = apriori2.runAlgorithm(0.2);
		apriori2.printStats();
		
		// Mine pseudo-closed itemsets
		AlgoPseudoClosedItemsets algo4 = new AlgoPseudoClosedItemsets();
		Itemsets pseudoClosed = algo4.runAlgorithm(frequents, apriori2.getFrequentClosed(), context.getObjects().size());
		// Print results
		pseudoClosed.printItemsets(context.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPseudoClosedItemsets.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
