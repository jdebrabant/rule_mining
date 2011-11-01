package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.associationrules.agrawal_Apriori_version.RulesAgrawal;
import ca.pfv.spmf.associationrules.closedrules.AlgoAgrawalFaster94ClosedRulesVersion;
import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriClose;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestClosedAssociationRules {

	public static void main(String [] arg){
		// Loading the binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextZart.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// STEP 1: Applying the APRIORI algorithm to find frequent closed itemsets
		double minsupp = 0.60;
		AlgoAprioriClose apriori = new AlgoAprioriClose(context);
		apriori.runAlgorithm(minsupp);
		Itemsets patterns = apriori.getFrequentClosed();
		patterns.printItemsets(context.size());
		
		// STEP 2: Generate all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		double  minconf = 0.60;
		AlgoAgrawalFaster94ClosedRulesVersion algoAgrawal = new AlgoAgrawalFaster94ClosedRulesVersion(minconf);
		RulesAgrawal rules = algoAgrawal.runAlgorithm(patterns);
		rules.printRules(context.size());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestClosedAssociationRules.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
