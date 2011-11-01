package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.associationrules.pasquier.AlgoStructuralBasisForApproxRulesFromFC;
import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriClose;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Class to test the algorithms AlgoGDBasisForExactRulesFromFC,  AlgoProperBasisForApproxRulesFromFC
 * and AlgoStructuralBasisForApproxRulesFromFC.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestStructuralBasis {

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
		
		// Applying the APRIORI-CLOSE algorithm
		AlgoAprioriClose apriori2 = new AlgoAprioriClose(context);
		Itemsets frequents = apriori2.runAlgorithm(0.4);
		apriori2.printStats();

		// Applying the StructuralBasisForApproxRulesFromFC algorithm
		AlgoStructuralBasisForApproxRulesFromFC algo5 = new AlgoStructuralBasisForApproxRulesFromFC();
		algo5.runAlgorithm(frequents, 0.5);
		
		// Print results
		algo5.printStats(context.getObjects().size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestStructuralBasis.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
