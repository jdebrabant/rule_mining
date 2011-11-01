package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.eclat_and_charm.AlgoEclat;
import ca.pfv.spmf.frequentpatterns.eclat_and_charm.Context;
import ca.pfv.spmf.frequentpatterns.eclat_and_charm.Itemsets;

/**
 * Class to test the ECLAT algorithm .
 * @author Philippe Fournier-Viger - 2009
 */
public class MainTestEclat {

	public static void main(String [] arg){
		// Loading the binary context
		Context context = new Context();
		try {
			context.loadFile(fileToPath("contextPasquier99.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// Applying the ECLAT algorithm
		AlgoEclat algo = new AlgoEclat(context);
		Itemsets patterns = algo.runAlgorithm(0.4, false);
		// if you change use "true" in the line above, ECLAT will use
		// a triangular matrix  for counting support of itemsets of size 2.
		// For some datasets it should make the algorithm faster.
		
		patterns.printItemsets(context.size());
		algo.printStats();

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestEclat.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
