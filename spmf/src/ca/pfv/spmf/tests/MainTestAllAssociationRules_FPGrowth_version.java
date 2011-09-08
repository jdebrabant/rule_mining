package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.associationrules.agrawal_FPGrowth_version.AlgoAgrawalFaster94_FPGrowth_version;
import ca.pfv.spmf.associationrules.agrawal_FPGrowth_version.RulesAgrawal;
import ca.pfv.spmf.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Database;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Itemsets;
/**
 * Class to test the algorithms AlgoGDBasisForExactRulesFromFC,  AlgoProperBasisForApproxRulesFromFC
 * and AlgoStructuralBasisForApproxRulesFromFC.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAllAssociationRules_FPGrowth_version {

	public static void main(String [] arg){
		// Loading the binary context
		Database database = new Database();
		try {
			database.loadFile(fileToPath("contextIGB.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		database.printContext();
		
		// STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
		double minsupp = 0.5;
		AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
		Itemsets patterns = fpgrowth.runAlgorithm(database, minsupp);
		patterns.printItemsets(database.size());
		
		// STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		double  minconf = 0.60;
		AlgoAgrawalFaster94_FPGrowth_version algoAgrawal = new AlgoAgrawalFaster94_FPGrowth_version(minconf);
		RulesAgrawal rules = algoAgrawal.runAlgorithm(patterns);
		rules.printRules(database.size());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllAssociationRules_FPGrowth_version.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
