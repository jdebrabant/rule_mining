package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Database;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Itemsets;


/**
 * Class to test the FPGROWTH algorithm
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestFPGrowth {

	public static void main(String [] arg){
		// Loading the binary context
		Database context = new Database();
		try {
			context.loadFile(fileToPath("contextPasquier99.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// Applying the FPGROWTH algorithmMainTestFPGrowth.java
		AlgoFPGrowth algo = new AlgoFPGrowth();
		Itemsets patterns = algo.runAlgorithm(context, 0.4);
		algo.printStats();
		patterns.printItemsets(context.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFPGrowth.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
