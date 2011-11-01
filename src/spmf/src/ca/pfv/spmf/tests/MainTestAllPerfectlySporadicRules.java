package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.associationrules.agrawal_Apriori_version.AlgoAgrawalFaster94;
import ca.pfv.spmf.associationrules.agrawal_Apriori_version.RulesAgrawal;
import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriInverse;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Class to test the algorithms AprioriInverse.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAllPerfectlySporadicRules {

	public static void main(String [] arg){
		// Loading the context file
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
		
		// STEP 2: Generate all sporadic 
		// rules from the set of perfectly rare itemsets (based on Agrawal & Srikant, 94)
		double  minconf = 0.60;
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94(minconf);
		RulesAgrawal rules = algoAgrawal.runAlgorithm(patterns);
		rules.printRules(context.size());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllPerfectlySporadicRules.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
