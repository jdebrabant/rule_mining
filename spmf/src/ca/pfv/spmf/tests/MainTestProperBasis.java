package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.associationrules.pasquier.AlgoProperBasisForApproxRulesFromFC;
import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriClose;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Class to test the algorithms AlgoGDBasisForExactRulesFromFC,  AlgoProperBasisForApproxRulesFromFC
 * and AlgoStructuralBasisForApproxRulesFromFC.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestProperBasis {

	public static void main(String [] arg){
		// Loading a binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextPasquier99.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();

		// Applying the Apriori-Close algorithm
		AlgoAprioriClose apriori2 = new AlgoAprioriClose(context);
		Itemsets frequents = apriori2.runAlgorithm(0.4);
		apriori2.printStats();
		
		// Applying the algorithm : ProperBasisForApproxRulesFromFC
		AlgoProperBasisForApproxRulesFromFC algo4 = new AlgoProperBasisForApproxRulesFromFC();
		algo4.runAlgorithm(frequents, 0.5);
		// Print results
		algo4.printStats(context.getObjects().size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestProperBasis.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
