package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.associationrules.agrawal_Apriori_version.AlgoAgrawalFaster94;
import ca.pfv.spmf.associationrules.agrawal_Apriori_version.RulesAgrawal;
import ca.pfv.spmf.frequentpatterns.apriori.AlgoApriori;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Class to test the algorithms AlgoGDBasisForExactRulesFromFC,  AlgoProperBasisForApproxRulesFromFC
 * and AlgoStructuralBasisForApproxRulesFromFC.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAllAssociationRules {

	public static void main(String [] arg){
		// Loading the binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextIGB.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// STEP 1: Applying the APRIORI algorithm to find frequent itemsets
		double minsupp = 0.5;
		AlgoApriori apriori = new AlgoApriori(context);
		Itemsets patterns = apriori.runAlgorithm(minsupp);
		patterns.printItemsets(context.size());
		
		// STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		double  minconf = 0.60;
		AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94(minconf);
		RulesAgrawal rules = algoAgrawal.runAlgorithm(patterns);
		rules.printRules(context.size());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllAssociationRules.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
