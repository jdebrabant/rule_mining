package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;
import ca.pfv.spmf.frequentpatterns.relim.AlgoRelim;

/**
 * Class to test the RELIM algorithm
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestRelim {

	public static void main(String [] arg){
		// Loading the binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextPasquier99.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// Applying the RELIM algorithm
		AlgoRelim algo = new AlgoRelim(context);
		Itemsets patterns = algo.runAlgorithm(0.4);
		patterns.printItemsets(context.size());
		algo.printStats();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestRelim.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
